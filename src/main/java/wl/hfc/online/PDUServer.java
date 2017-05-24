package wl.hfc.online;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

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

import redis.clients.jedis.Jedis;
import wl.hfc.common.*;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class PDUServer {
	// config
	public static int OnlineInterval=0;// 单位:S;设备树在线轮询一遍总时间
	public int MinInterval; // 设备数量所决定的最低轮询一遍的时间；

	// private EnumLogoVersion logoVersion;//当前网管定制版本
	private Snmp session;
	private boolean isOnlineThreadRun = true;
	private Hashtable listDevHash;

	public PDUServer(Hashtable pListDevHash) {

		// this.logoVersion = plogoVersion;
		this.listDevHash = pListDevHash;
		try {
			initSnmpAPI();
			OnlineTestThread();

		} catch (Exception e) {
			// TODO: handle exception
		}



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
				System.out.println("---------->开始异步解析<------------");
				if (event != null && event.getResponse() != null) {
					try {
						readResponse(event);
					} catch (Exception e) {
						e.printStackTrace();
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

		// System.Diagnostics.Stopwatch stopwatch = new Stopwatch();
		// stopwatch.Start(); // 开始监视代码

		// Console.WriteLine(DateTime.Now.ToString("h:mm:ss    ") +
		// "收到一个trap包来自::" + pdu.Address.ToString());
		// System.Console.Out.WriteLine(pdu.PrintVarBinds());

		DevTopd lNode;
		
		String ipaddr=respEvnt.getPeerAddress().toString();
		ipaddr = ipaddr.substring(0,ipaddr.indexOf("/"));
		lNode = (DevTopd)listDevHash.get(ipaddr);
		if (lNode == null)
			return;
		Vector<VariableBinding> recVBs= (Vector<VariableBinding>)respEvnt.getResponse().getVariableBindings();
		if (lNode != null) {
			
			HFCTypes devtype = OidToHFCType.getType(recVBs);
			if (devtype == HFCTypes.Unknown) // 处理未知设备
			{
				return;
			}

			lNode.HFCType1 = devtype;

			if (lNode.mNetType != DProcess.getNetTypes(lNode.HFCType1) && lNode.mNetType != NetTypes.other)// 和数据库指定的不匹配
			{
				return;
			}

			// dev.mNetType = (int)CDevForCMD.getNetTypes(devtype);
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
				
				//hi,xinglong ,send to Mainkernel ip+isonline?  message		

			}

		}

		// stopwatch.Stop(); // 停止监视
		// TimeSpan timeSpan = stopwatch.Elapsed; // 获取总时间
		// Console.WriteLine(timeSpan.ToString() + "result resultresultresult");
	}

	private void OnlineTestThread() {
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

		while (true) {		
			
		
			try {
				
				
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
				int psrint = 0;

				for (Iterator iter = testdevlist.iterator(); iter.hasNext();) {
					DevTopd lNode = (DevTopd) iter.next();

					AyncSendSnmpPdu(outpdu, lNode._NetAddress, lNode.BindnojuDeviceTableRow._ROCommunity);

					Thread.currentThread().sleep(intervel1);
					lNode.OnlineCount--;

				}

				if (!isOnlineThreadRun) {
					Thread.currentThread().sleep(3000);
				}
				else {

					for (Iterator iter = testdevlist.iterator(); iter.hasNext();) {
						DevTopd dev = (DevTopd) iter.next();


						// notify runtime data
						if (dev.OnlineCount <= 0) {
							if (dev.isOline) {
								dev.isOline = false;						
								
								//hi,xinglong ,send to Mainkernel ip+isonline?  message			
								


							}

							dev.OnlineCount = 0;

						}

					}

				}
			} catch (Exception ex1) {
				// log4net.LogManager.GetLogger("prgLog").Info("Exception from the OnlineTestThread");
				// log4net.LogManager.GetLogger("prgLog").Info(ex1.ToString() +
				// ex1.Message);
			}
/*
			try {
				Thread.currentThread().sleep(10000);
			} catch (Exception e) {
				// TODO: handle exception
			}*/
			

			// stopwatch.Stop(); // 停止监视
			// TimeSpan timeSpan = stopwatch.Elapsed; // 获取总时间
			// this.MinInterval = timeSpan.Seconds;
			// int rstSC = timeSpan.Minutes;
			// Console.WriteLine(MinInterval +
			// "                    pdu server  askwhile min second   " +
			// rstSC.ToString());

		}

	}

}
