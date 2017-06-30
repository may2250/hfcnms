package wl.hfc.traprcss;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import redis.clients.jedis.Jedis;
import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.DevTopd;
import wl.hfc.common.PduSevr;
import wl.hfc.common.nojuTrapLogTableRow;
import wl.hfc.online.pmls;
import wl.hfc.server.Sstatus;
import wl.hfc.topd.MainKernel;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.*;

import com.xinlong.Services.TrapReceiverBean;
import com.xinlong.util.RedisUtil;


//TrapPduServer 只负责告警监听，不响应发布订阅消息机制。
public class TrapPduServer {
	private static final String  HFCALARM_MESSAGE =  "currentalarm.message" ;
	public static TrapProCenter trpcss;
	public static String TRAP_ADDRESS = "udp:0.0.0.0/";
	//private static final String TRAP_SERVER_PORT_KEY = "global:trapserver:port";
	//true:is valid; false:is invalid
	public static boolean TrapPduServer_status=false;
	private static Snmp snmp = null;
	private Address listenAddress;
	private Hashtable listDevHash;
	public static CurrentAlarmModel realTrapResponder;
	private static Logger logger = Logger.getLogger(TrapPduServer.class);
	
	private static RedisUtil redisUtil;
	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		TrapPduServer.redisUtil = redisUtil;
	}

	//hfc_client_udp  	    
	private Address targetAddress = null;

	public void start() {
		logger.info("TrapPduServer.start() action called, start trap receivering..........");
		String filePath = pmls.class.getResource("/").toString();
		filePath = filePath.substring(filePath.indexOf("file:") + 5);
		filePath = filePath + "mibs";
		System.out.println("----------------path--->>>" + filePath);
        TrapProCenter trpcss = new TrapProCenter(true, filePath);
        this.trpcss = trpcss;
    	this.listDevHash = MainKernel.me.listDevHash;
		try {
			// get trap port from db

			String trapport = "162";
			TRAP_ADDRESS = TRAP_ADDRESS + trapport;
			System.out.println("+++++++++TRAP_ADDRESS=" + TRAP_ADDRESS);

			listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", TRAP_ADDRESS));
			TransportMapping transport;

			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);

			snmp = new Snmp(transport);

			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
			snmp.listen();
			
			
			TrapPduServer_status=true;

		} catch (Exception e) {
			TrapPduServer_status=false;
			e.printStackTrace();
			
		}

		CommandResponder pduHandler = new CommandResponder() {
			public synchronized void processPdu(CommandResponderEvent e) {

				// doWork
				doReceive(e);
			}

		};

		snmp.addCommandResponder(pduHandler);

		// while(true)
		// {
		// try
		// {
		// Thread.sleep(1000);
		// System.out.println("123123123");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		
	}

	public TrapPduServer() {

		

	}

	@SuppressWarnings("unchecked")
	public void doReceive(CommandResponderEvent event) {
		// /process response
		if (event != null && event.getPDU() != null) {
			PDU inPdu = event.getPDU();
			Vector<VariableBinding> recVBs = (Vector<VariableBinding>) inPdu.getVariableBindings();

			if (event.getSecurityModel() == 2) {
				// trapv2

			} else if (event.getSecurityModel() == 1) {
				int status = ((PDUv1) inPdu).getSpecificTrap();
				int traptype = ((PDUv1) inPdu).getGenericTrap();
				OID enterprise = ((PDUv1) inPdu).getEnterprise();
				// logger.info("--traptype---->>>"+((PDUv1)event.getPDU()).getGenericTrap());
				Map<String, String> hfcalarmhash = new LinkedHashMap();
				hfcalarmhash.put("status", String.valueOf(status));
				hfcalarmhash.put("traptype", String.valueOf(traptype));
				hfcalarmhash.put("enterprise", enterprise.toString());
				
				
				String ipaddr = event.getPeerAddress().toString();
				ipaddr = ipaddr.substring(0, ipaddr.indexOf("/"));
				DevTopd lNode = (DevTopd) listDevHash.get(ipaddr);
			    if (lNode == null)
                    return ;

				hfcalarmhash.put("ip", ipaddr);
				try {
					nojuTrapLogTableRow traprst = trpcss.ProcessTrapRequestPduHandler(hfcalarmhash, 0, inPdu);
					if (traprst.isValid) {
					
						String serStr = null;
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
			            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);  
			            objectOutputStream.writeObject(traprst);    
			            serStr = byteArrayOutputStream.toString("ISO-8859-1");  
			            serStr = java.net.URLEncoder.encode(serStr, "UTF-8");  
			              
			            objectOutputStream.close();  
			            byteArrayOutputStream.close();
						JSONObject json = new JSONObject();
						json.put("cmd", "newalarm");
						json.put("val", serStr);

						sendToQueue(json.toJSONString(), HFCALARM_MESSAGE);
					}
	           
					//realTrapResponder.insertTrapLog(traprst);
					   
  
		
				} catch (Exception e) {
					e.printStackTrace();
				}

				// for (int i = 0; i < recVBs.size(); i++) {
				// VariableBinding recVB = recVBs.elementAt(i);
				// String content = recVB.getVariable().toString();
				// }

				return;
			}

		}

	}
	
	private void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(HFCALARM_MESSAGE, msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			if(jedis != null)
				redisUtil.getJedisPool().returnBrokenResource(jedis);

		}
	}

}
