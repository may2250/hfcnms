package com.xinlong.Services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.PDUv1;

import redis.clients.jedis.Jedis;

import com.xinlong.util.RedisUtil;


public class TrapReceiverBean {
	private static final String  HFCALARM_MESSAGE =  "servicehfcalarm.message" ;
	public static String TRAP_ADDRESS = "udp:0.0.0.0/";	
	private static Snmp snmp = null;
	private Address listenAddress;	
	
	private static Logger logger = Logger.getLogger(TrapReceiverBean.class);
	
	private static RedisUtil redisUtil;

	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	public static void setRedisUtil(RedisUtil redisUtil) {
		TrapReceiverBean.redisUtil = redisUtil;
	}

	//hfc_client_udp  	    
	private Address targetAddress = null;

	public void start() {
		logger.info("trapreceiver.start() action called, start trap receivering..........");

		targetAddress = GenericAddress.parse("udp:127.0.0.1/2250");		
		//test
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(HFCALARM_MESSAGE, "test message............");
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			if(jedis != null)
				redisUtil.getJedisPool().returnBrokenResource(jedis);

		}
		
		doWork();
		
	}

	private void doWork() {

		try {
			//get trap port from db
			TRAP_ADDRESS = TRAP_ADDRESS + "162";
			System.out.println("+++++++++TRAP_ADDRESS=" + TRAP_ADDRESS);
			
			listenAddress = GenericAddress.parse(System.getProperty(
					"snmp4j.listenAddress", TRAP_ADDRESS));
			TransportMapping transport;

			transport = new DefaultUdpTransportMapping(
					(UdpAddress) listenAddress);

			snmp = new Snmp(transport);

			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.listen();

		} catch (Exception e) {
			e.printStackTrace();
		}

		CommandResponder pduHandler = new CommandResponder() {
			public synchronized void processPdu(CommandResponderEvent e) {			
				
				//doWork				
				doReceive(e);
			}

		};

		snmp.addCommandResponder(pduHandler);

	}

	

	@SuppressWarnings("unchecked")
	public void doReceive(CommandResponderEvent event) {
		// /process response
		if (event != null && event.getPDU() != null) {
			Vector<VariableBinding> recVBs = (Vector<VariableBinding>) event.getPDU()
					.getVariableBindings();
			
			if(event.getSecurityModel() == 2){
				//trapv2
				
			}else if(event.getSecurityModel() == 1){
				//trapv1
				//hfc alarm			
				if((recVBs.size() == 3)||(recVBs.size() == 2)){
					int status = ((PDUv1)event.getPDU()).getSpecificTrap();
					int traptype = ((PDUv1)event.getPDU()).getGenericTrap();
					OID enterprise = ((PDUv1)event.getPDU()).getEnterprise();
					
					Map<String, String> hfcalarmhash=new LinkedHashMap();
					hfcalarmhash.put("status", String.valueOf(status));
					hfcalarmhash.put("traptype", String.valueOf(traptype));
					hfcalarmhash.put("enterprise", enterprise.toString());
					for (int i = 0; i < recVBs.size(); i++) {
						VariableBinding recVB = recVBs.elementAt(i);
						String content = recVB.getVariable().toString();
						 //System.out.println("SNMP4j traper: content=" + content);

						switch (i) {
						case 0:						
							hfcalarmhash.put("mac", content);
							break;
						case 1:						
							hfcalarmhash.put("logicalid", content);
							break;
						case 2:						
							hfcalarmhash.put("alarminfo", content);
							break;				
						default:
							System.out.println("not correct");
							break;
						}
					}
					
					doHfcAlarm(hfcalarmhash );
					return;
				}
				return;
			}			
		}
	}
	
	
	private void doHfcAlarm(Map alarm) {
		String msgservice = JSONValue.toJSONString(alarm);

		sendToHfcAlarmQueue(msgservice);
	}
	
	private void sendToHfcAlarmQueue(String msg) {
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
