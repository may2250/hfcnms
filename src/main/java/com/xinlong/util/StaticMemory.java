package com.xinlong.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import wl.hfc.common.CDevForCMD;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;
import wl.hfc.online.*;


public class StaticMemory {
	// save web sessions
	public static CopyOnWriteArraySet<Uhandle> webSocketClients = new CopyOnWriteArraySet<Uhandle>();
	// 客户端需求实时数据的设备列表
	private Hashtable<String, ObjSnmpPreail> realTimeDevHashtable = new Hashtable<String, ObjSnmpPreail>();
	private static Logger log = Logger.getLogger(StaticMemory.class);

	public void AddSession(Uhandle pHandle) {
		synchronized (this) {
			webSocketClients.add(pHandle);
			log.info("now add the user  "+pHandle.username+":::" + pHandle.getId());
			System.out.println("Uhandless Size::::" + webSocketClients.size());
		}
	}

	public void RemoveSession(Session session) {		
		synchronized (this) {
			for (Uhandle pHandle : webSocketClients) {
				if(session.getId() == pHandle.getId()){
					webSocketClients.remove(pHandle);
					removeRealTimeDev(session.getId());
					log.info("now remove the user  "+pHandle.username+":::" + session.getId());
					System.out.println("Uhandless Size::::" + webSocketClients.size());
		
				}
			}
			
		}
	}
	
	public int getCount() {
		return webSocketClients.size();
	}

	public synchronized void addRealTimeDev(JSONObject jsondata) {
	
		String netaddr = jsondata.get("ip").toString();
		String sessionID = jsondata.get("sessionid").toString();
		if (realTimeDevHashtable.containsKey(netaddr)) {
			ObjSnmpPreail osp = realTimeDevHashtable.get(netaddr);
			if (!osp.sessionList.contains(sessionID)) {
				osp.sessionList.add(sessionID);
				//System.out.println("----add new SessionID");
			}
		} else {

			ObjSnmpPreail osp = new ObjSnmpPreail();
			HFCTypes hfctyp1 =HFCTypes.values()[Integer.valueOf(jsondata.get("nojuhfctype").toString())];
			if (hfctyp1==HFCTypes.HfcMinWorkstation) {
				osp.snmpPreail = new ReceiverSnmpPrevail(".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			else if (hfctyp1==HFCTypes.EDFA) {
				osp.snmpPreail = new EDFASnmpPrevail(".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			else if (hfctyp1==HFCTypes.HfcEdfaWE_HD_SWITCH) {
				osp.snmpPreail = new EDFASnmpPrevail(".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			else if (hfctyp1==HFCTypes.TransEM) {
				osp.snmpPreail = new EMSnmpPrevail (".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			else if (hfctyp1==HFCTypes.Trans1550DM) {
				osp.snmpPreail = new TransDMSnmpPrevail (".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			else if (hfctyp1==HFCTypes.OSW) {
				osp.snmpPreail = new OSWSnmp(".1",jsondata.get("deviceid").toString());
				osp.commonSnmpPreail = new CommonSnmpPrevail(".0");
			}
			
			else {

				return;
			}
			

			CDevForCMD devmmd = new CDevForCMD(jsondata.get("rcommunity").toString(), jsondata.get("wcommunity").toString(), netaddr);
			devmmd.HFCType1 =hfctyp1;
			devmmd.MD=jsondata.get("md").toString();
			devmmd.DEVICEID=jsondata.get("deviceid").toString();
			devmmd.imagePath=devmmd.bornImagePath();			
			PDUServerForOneDev PDUServerForOneDev1 = new PDUServerForOneDev(0);
			osp.snmpPreail.thisDev = devmmd;
			osp.snmpPreail.sver = PDUServerForOneDev1;

			osp.commonSnmpPreail.thisDev = devmmd;
			osp.commonSnmpPreail.sver = PDUServerForOneDev1;

			osp.sessionList.add(sessionID);
			realTimeDevHashtable.put(netaddr, osp);
			System.out.println("----add new RealTimeDev==" + realTimeDevHashtable.size());
		}
	}

	public synchronized ObjSnmpPreail getRealTimeDev(String netaddr) {
		if (realTimeDevHashtable.containsKey(netaddr)) {
			return realTimeDevHashtable.get(netaddr);
		} else {
			return null;
		}
	}

	public synchronized void SetRealTimeDevCommunity(String netaddr, String ROCommunity, String RWCommunity) {
		if (realTimeDevHashtable.containsKey(netaddr)) {
			ObjSnmpPreail objsnmp = realTimeDevHashtable.get(netaddr);
			objsnmp.snmpPreail.thisDev.ROCommunity = ROCommunity;
			objsnmp.snmpPreail.thisDev.RWCommunity = RWCommunity;
			objsnmp.commonSnmpPreail.thisDev.ROCommunity = ROCommunity;
			objsnmp.commonSnmpPreail.thisDev.RWCommunity = RWCommunity;
		} else {
			return;
		}
	}

	public synchronized Hashtable<String, ObjSnmpPreail> getAllRealTimeDev() {
		return this.realTimeDevHashtable;
	}

	public synchronized void removeRealTimeDev(String netaddr, String sessionID) {
		if (realTimeDevHashtable.containsKey(netaddr)) {
			ObjSnmpPreail osp = realTimeDevHashtable.get(netaddr);
			if (osp.sessionList.size() > 0) {
				osp.sessionList.remove(sessionID);
				if (osp.sessionList.size() == 0) {
					realTimeDevHashtable.remove(netaddr);
				//	System.out.println("----del RealTimeDev==" + realTimeDevHashtable.size());
				}
			}
		}
	}

	public synchronized void removeRealTimeDev(String sessionID) {
		for (Map.Entry<String, ObjSnmpPreail> entry : realTimeDevHashtable.entrySet()) {
			ObjSnmpPreail osp = (ObjSnmpPreail) entry.getValue();
			if (osp.sessionList.size() > 0) {
				osp.sessionList.remove(sessionID);
				if (osp.sessionList.size() == 0) {
					realTimeDevHashtable.remove(entry.getKey());
				//	System.out.println("----del RealTimeDev==" + realTimeDevHashtable.size());
				}
			}
		}
	}

	public Session getSessionByID(String id) {
		synchronized (this) {
			for (Uhandle clientItem : webSocketClients) {
				try {
					synchronized (clientItem) {
						if (clientItem.getId().equalsIgnoreCase(id)) {
							// System.out.println("Session Got::" + sessionid);
							return clientItem.session;
						}
					}
				} catch (Exception ex) {
					webSocketClients.remove(clientItem);
					removeRealTimeDev(id);
					System.out.println("getSessionByID  Connection closed::::" + webSocketClients.size());
					ex.printStackTrace();
					try {
						clientItem.session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
			return null;
		}
	}
	
	public boolean getSessionByuser(String username) {		
		synchronized (this) {
			for (Uhandle usession : webSocketClients) {
				if(username.equalsIgnoreCase(usession.username)){
					return true;
				}
			}
			
		}
		return false;
	}
	public Uhandle getUserSessionByID(String id) {
		synchronized (this) {
			for (Uhandle session : webSocketClients) {
				try {
					synchronized (session) {
						if (session.getId().equalsIgnoreCase(id)) {
							// System.out.println("Session Got::" + sessionid);
							return session;
						}
					}
				} catch (Exception ex) {
					webSocketClients.remove(session);
					removeRealTimeDev(id);
					System.out.println("getSessionByID Connection closed::::" + webSocketClients.size());
					ex.printStackTrace();
					try {
						session.session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
			return null;
		}
	}


	public void sendRemoteStr(String message, String sessionid) {
		Session ses = getSessionByID(sessionid);
		if (ses != null) {
			try {
				ses.getBasicRemote().sendText(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				log.info(e.getMessage());
			}
		} else {
			System.out.println("No Session Found::::");
		}
	}

	public void broadCast(String message) {
		synchronized (this) {
			for (Uhandle session : webSocketClients) {
				try {
					synchronized (session) {
						session.session.getBasicRemote().sendText(message);
					}
				} catch (IOException e) {
					webSocketClients.remove(session);
					removeRealTimeDev(session.getId());
					System.out.println("Connection closed::::" + webSocketClients.size());
					try {
						session.session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		}

	}

}
