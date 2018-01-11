package wl.hfc.traprcss;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.snmp4j.*;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;

import redis.clients.jedis.Jedis;

import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.DevTopd;
import wl.hfc.common.nojuTrapLogTableRow;
import wl.hfc.online.pmls;
import wl.hfc.server.SmsgList;
import wl.hfc.server.Sstatus;

import com.xinlong.util.RedisUtil;

//TrapPduServer 只负责告警监听，不响应发布订阅消息机制。
public class TrapPduServer extends Thread {
	private static final String HFCALARM_MESSAGE = "currentalarm.message";
	public static TrapProCenter trpcss;
	public static String TRAP_ADDRESS = "udp:0.0.0.0/";
	// private static final String TRAP_SERVER_PORT_KEY = "global:trapserver:port";
	// true:is valid; false:is invalid
	public static boolean TrapPduServer_status = false;
	private static Snmp snmp = null;

	public static CurrentAlarmModel realTrapResponder;
	private static Logger logger = Logger.getLogger(TrapPduServer.class);
	public static TrapPduServer me;
	private static RedisUtil redisUtil;

	private Address listenAddress;
	public Hashtable listDevHash;

	public TrapPduServer() {		

		logger.info("******      ******      ******     **************    *********");			
		logger.info("                       |||||||||||                             ");	
		logger.info("******      ******      ******     **************    *********");	
		logger.info("construct  TrapPduServer");	
		String filePath = pmls.class.getResource("/").toString();
		filePath = filePath.substring(filePath.indexOf("file:") + 5);
		filePath = filePath + "mibs";
		// System.out.println("----------------path--->>>" + filePath);

		this.trpcss = new TrapProCenter(true, filePath);

		this.setName("TrapPduServer");
		me = this;

	}

	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		TrapPduServer.redisUtil = redisUtil;
	}

	// hfc_client_udp
	private Address targetAddress = null;

	public void run() {

	
		if (!MibProcess.MibProcess_status) {
			TrapPduServer_status = false;
			logger.info(this.getName() + "....no need starting.......");
			return;
		}
		logger.info(this.getName() + "  run.......");

		try {

			String trapport = "162";
			TRAP_ADDRESS = TRAP_ADDRESS + trapport;
			// System.out.println("+++++++++TRAP_ADDRESS=" + TRAP_ADDRESS);

			listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", TRAP_ADDRESS));
			TransportMapping transport;

			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);

			snmp = new Snmp(transport);

			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
			snmp.listen();

			TrapPduServer_status = true;

		} catch (Exception e) {
			TrapPduServer_status = false;
			e.printStackTrace();
			logger.error(e.getMessage());
			return;

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

				if (listDevHash == null) {

					return;
				}

				DevTopd lNode = (DevTopd) listDevHash.get(ipaddr);
				if (lNode == null)
					return;

				hfcalarmhash.put("ip", ipaddr);
				try {
					nojuTrapLogTableRow traprst = trpcss.ProcessTrapRequestPduHandler(hfcalarmhash, 0, inPdu);
					traprst.neName = lNode.fullpath;
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

					// realTrapResponder.insertTrapLog(traprst);

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

		if (Sstatus.isRedis) {
			Jedis jedis = null;
			try {
				jedis = redisUtil.getConnection();
				jedis.publish(queue, msg);
				redisUtil.closeConnection(jedis);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (jedis != null)
					redisUtil.getJedisPool().returnBrokenResource(jedis);

			}
		} else {
			if (queue.equalsIgnoreCase(HFCALARM_MESSAGE)) {
				synchronized (SmsgList.alarmstorage) {
					SmsgList.alarmstorage.add(msg);
					SmsgList.alarmstorage.notify();

				}

			}

		}

	}

}
