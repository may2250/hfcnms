package wl.hfc.online;

import java.io.*;
import java.util.*;


import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.xinlong.util.RedisUtil;
import redis.clients.jedis.Jedis;
import wl.hfc.common.*;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;


public class PDUServer extends Thread {
	// config
	private static final String HFCALARM_MESSAGE = "currentalarm.message";
	public static int OnlineInterval = 3;// 单位:S;设备树在线轮询一遍总时间
	public int MinInterval; // 设备数量所决定的最低轮询一遍的时间；
	private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
	private static Logger log = Logger.getLogger(PDUServer.class);
	private static Logger log2 = Logger.getLogger("myTest1");
	// private EnumLogoVersion logoVersion;//当前网管定制版本
	private Snmp session;
	private boolean isOnlineThreadRun = true;
	public  Hashtable listDevHash;
	public static boolean PDUServer_status = false;
	private static RedisUtil redisUtil;

	public static void setRedisUtil(RedisUtil redisUtil) {
		PDUServer.redisUtil = redisUtil;
	}
	public static PDUServer me;
	
	
	
	public PDUServer() {

		try {
			initSnmpAPI();
			PDUServer_status = true;
		

		} catch (Exception ex1) {
			PDUServer_status = false;
			ex1.printStackTrace();
			log.error(ex1.getMessage());
		}
		
		this.setName("PDUServer");
		me=this;
	}



	private void initSnmpAPI() throws IOException {

		// 创建SNMP协议通信引擎对象。
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
		session = new Snmp(transport);
		session.listen();

	}

	public PDU SyncSendSnmpPdu(PDU outpdu, CommunityTarget cTgt) {

		PDU response = null;//

		try {
			// response PDU

			ResponseEvent responseEvent = this.session.send(outpdu, cTgt);

			response = responseEvent.getResponse();

			if (response != null) {

				if (response.getErrorIndex() == response.noError && response.getErrorStatus() == response.noError) {

					return response;

				}
			}

		} catch (Exception ex) {
			// Console.Write(ex.Source);
			return response;

		}
		return response;
		// System.Console.Out.WriteLine("进行了一次同步发送");

	}

	public boolean AyncSendSnmpPdu(final PDU outpdu, final String ipaddr, String rcommunity) {

		// 设置 目标
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(rcommunity));
		target.setAddress(new UdpAddress(ipaddr + "/" + "161"));
		target.setRetries(2);
		target.setTimeout(2 * 1000);
		target.setVersion(SnmpConstants.version1);

		ResponseListener listener = new ResponseListener() {
			public void onResponse(ResponseEvent event) {
				// System.out.println("---------->开始异步解析<------------");
				if (event != null && event.getResponse() != null) {
					try {
						readResponse(event);
					} catch (Exception e) {
						e.printStackTrace();
						log2.info(e.getMessage());
					}
					session.cancel(outpdu, this);

				}
			}
		};

		// 发送报文
		try {
			session.send(outpdu, target, null, listener);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public void readResponse(ResponseEvent respEvnt) {
		// 解析Response
		// System.out.println("------------>解析Response<----------");

		if (respEvnt != null && respEvnt.getResponse() != null) {
			Vector<VariableBinding> recVBs = respEvnt.getResponse().getVariableBindings();

			VariableBinding recVB = recVBs.elementAt(0);
			if (!(recVB.getVariable().toString().equalsIgnoreCase("Null"))) {

				TestOnlineMessageCallback(respEvnt);
			}
		}

	}

	private void TestOnlineMessageCallback(ResponseEvent respEvnt) {

		DevTopd lNode;

		String ipaddr = respEvnt.getPeerAddress().toString();
		ipaddr = ipaddr.substring(0, ipaddr.indexOf("/"));
		lNode = (DevTopd) listDevHash.get(ipaddr);
		if (lNode == null)
			return;
		@SuppressWarnings("unchecked")
		Vector<VariableBinding> recVBs = (Vector<VariableBinding>) respEvnt.getResponse().getVariableBindings();
		if (lNode != null) {

			HFCTypes devtype = OidToHFCType.getType(recVBs);
			if (devtype == HFCTypes.Unknown) // 处理未知设备
			{
				return;
			}

			lNode.HFCType1 = devtype;

			if (lNode.mNetType != DProcess.getNetTypes(lNode.HFCType1) && lNode.mNetType != NetTypes.other)// 和数据库指定的不匹配
			{
				// System.out.println("----lnode.type==" + lNode.mNetType +
				// "-----hfctype---" + DProcess.getNetTypes(lNode.HFCType1));
				return;
			}

			lNode.OnlineCount = 3;

			if (!lNode.isOline) {

				if (devtype == HFCTypes.ES26 || devtype == HFCTypes.Cisco_64657T || devtype == HFCTypes.CiscoEDFA || devtype == HFCTypes.OTECWos
						|| devtype == HFCTypes.wos3000SCTE || devtype == HFCTypes.wos4000 || devtype == HFCTypes.LYTB_MTRAN2000 || devtype == HFCTypes.wos5000
						|| devtype == HFCTypes.TransDM_SCTE) {

					lNode.ID = "";
					lNode.MD = "";
					lNode.SN = "";
					lNode.DEVICEID = "";
				} else {

					lNode.ID = recVBs.elementAt(1).getVariable().toString();
					lNode.MD = recVBs.elementAt(2).getVariable().toString();
					lNode.SN = recVBs.elementAt(3).getVariable().toString();
					lNode.DEVICEID = recVBs.elementAt(6).getVariable().toString();

				}

				lNode.isOline = true;

				JSONObject rootjson = new JSONObject();
				rootjson.put("cmd", "devstatus");
				rootjson.put("ip", ipaddr);
				rootjson.put("isonline", true);
				sendToSub(rootjson.toJSONString());

				nojuTrapLogTableRow traprst = new nojuTrapLogTableRow(NlogType.getAlarmLevel(TrapLogTypes.TestOnline), TrapLogTypes.TestOnline, ipaddr,
						lNode.fullpath, (ClsLanguageExmp.isEn ? "Device online" : "设备上线"), new Date(), "", "", ClsLanguageExmp.isEn ? "Device online" : "设备上线",
						"");
				try {
					// send to CurrentAlarmModel
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

				} catch (Exception e) {	
					// TODO: handle exception					
					log2.info(e.getMessage());
				}

			}

		}

	
	}

	@SuppressWarnings("static-access")
	public void run() {

		// OnlineTestThread();
		log.info(this.getName()+ "....starting.......");
		LinkedList<DevTopd> testdevlist = new LinkedList<DevTopd>();

		PDU outpdu = new PDU();
		outpdu.setType(PDU.GET);
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.2.0"))); // mac
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.1.0")));
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.3.0")));
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.4.0")));
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.18.0")));
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.5.0")));
		outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.19.0")));

		PDUServer_status = true;
		while (true) {		
			


			
			try {
				if (this.listDevHash==null) {			
					
					Thread.currentThread().sleep(3000);
					continue;
				}

				if (!isOnlineThreadRun) {
					Thread.currentThread().sleep(3000);
					continue;
				}
				testdevlist.clear();

				Enumeration e = this.listDevHash.elements();

				while (e.hasMoreElements()) {

					DevTopd item = (DevTopd) e.nextElement();
					testdevlist.add(item);

				}

				if (testdevlist == null || testdevlist.size() == 0) {

					Thread.currentThread().sleep(1000);
					continue;
				}

				// if (OnlineInterval==0)
				// {
				// OnlineInterval = 2;//设备数量过少的时候
				// }

				// 不管多少台设备，每台设备被轮到之上间隔2秒，
				// dev.OnlineCount1=3初始值，也就是2*3，每台设备要被询问3次后无回包才判断下线

				int intervel1 = (OnlineInterval + 1) * 1000 / testdevlist.size();// +`1是因为默认值0的时候，如果设备很少，太过敏捷了

				if (intervel1 < 100)
					intervel1 = 100;

				if (intervel1 > 10000)
					intervel1 = 10000;

				for (Iterator iter = testdevlist.iterator(); iter.hasNext();) {
					DevTopd lNode = (DevTopd) iter.next();

					AyncSendSnmpPdu(outpdu, lNode._NetAddress, lNode.BindnojuDeviceTableRow._ROCommunity);

					Thread.currentThread().sleep(intervel1);
					lNode.OnlineCount--;

				}

				if (!isOnlineThreadRun) {
					Thread.currentThread().sleep(3000);
				} else {

					for (Iterator iter = testdevlist.iterator(); iter.hasNext();) {
						DevTopd dev = (DevTopd) iter.next();

						// notify runtime data
						if (dev.OnlineCount <= 0) {
							if (dev.isOline) {
								dev.isOline = false;

								// onlien infor
								JSONObject rootjson = new JSONObject();
								rootjson.put("cmd", "devstatus");
								rootjson.put("ip", dev._NetAddress);
								rootjson.put("isonline", false);
								sendToSub(rootjson.toJSONString());

								nojuTrapLogTableRow traprst = new nojuTrapLogTableRow(NlogType.getAlarmLevel(TrapLogTypes.Offline), TrapLogTypes.Offline,
										dev._NetAddress, dev.fullpath, ClsLanguageExmp.isEn ? "Device offline" : "设备下线", new Date(), "", "",
										ClsLanguageExmp.isEn ? "Device offline" : "设备下线", "");								

								// online log
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

							dev.OnlineCount = 0;

						}

					}

				}
			} catch (Exception ex1) {
				// log4net.LogManager.GetLogger("prgLog").Info("Exception from the OnlineTestThread");
				// log4net.LogManager.GetLogger("prgLog").Info(ex1.ToString()
				// +
				// ex1.Message);
				ex1.printStackTrace();
			//	log.info(ex1.getMessage());
			}
			
			


		}
		//log.error("PDUSERVER STOP WORK");
		

	}

	private void sendToSub(String msg) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(MAINKERNEL_MESSAGE, msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			if (jedis != null)
				redisUtil.getJedisPool().returnBrokenResource(jedis);

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
			// e.printStackTrace();
			if (jedis != null)
				redisUtil.getJedisPool().returnBrokenResource(jedis);

		}
	}
}
