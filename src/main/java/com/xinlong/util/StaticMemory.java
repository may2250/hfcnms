package com.xinlong.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import wl.hfc.common.CDevForCMD;
import wl.hfc.common.NetTypes;
import wl.hfc.online.PDUServerForOneDev;
import wl.hfc.online.ReceiverSnmpPrevail;
import wl.hfc.topd.MainKernel;

public class StaticMemory {
	//save web sessions
	public static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();
	//客户端需求实时数据的设备列表
	private Hashtable<String, ObjSnmpPreail> realTimeDevHashtable= new Hashtable<String, ObjSnmpPreail>();
	private static Logger log = Logger.getLogger(StaticMemory.class);
	public void AddSession(Session session){
		synchronized(this) { 
			webSocketClients.add(session);
		}		
	}
	
	public void RemoveSession(Session session){
		synchronized(this) { 
			webSocketClients.remove(session);
			removeRealTimeDev(session.getId());
		}		
	}
	
	public int getCount(){
		return webSocketClients.size();
	}
	
	public synchronized void addRealTimeDev(JSONObject jsondata){
		String netaddr = jsondata.get("ip").toString();
		String sessionID = jsondata.get("sessionid").toString();		
		if(realTimeDevHashtable.containsKey(netaddr)){
			ObjSnmpPreail osp = realTimeDevHashtable.get(netaddr);
			if(!osp.sessionList.contains(sessionID)){
				osp.sessionList.add(sessionID);
				System.out.println("----add new SessionID");
			}
		}else{
			ObjSnmpPreail osp = new ObjSnmpPreail();
			String devtype = jsondata.get("devtype").toString();
			if(devtype.equalsIgnoreCase("EDFA")){
				
			}else if(devtype.equalsIgnoreCase("Trans")){
				
			}else if(devtype.equalsIgnoreCase("rece_workstation")){
				osp.snmpPreail = new ReceiverSnmpPrevail(".1");
			}else if(devtype.equalsIgnoreCase("OSW")){
				
			}else if(devtype.equalsIgnoreCase("RFSW")){
				
			}else if(devtype.equalsIgnoreCase("PreAMP")){
				
			}else if(devtype.equalsIgnoreCase("wos")){
				
			}else{
				osp.snmpPreail = new ReceiverSnmpPrevail(".1");
			}			
			osp.snmpPreail.thisDev = new CDevForCMD();
			osp.snmpPreail.sver = new PDUServerForOneDev(0);
			osp.snmpPreail.thisDev.mNetAddress = netaddr;
			osp.snmpPreail.thisDev.mNetType = MainKernel.me.getStringToNetType(jsondata.get("devtype").toString());
			osp.snmpPreail.thisDev.ROCommunity = jsondata.get("rcommunity").toString();
			osp.snmpPreail.thisDev.RWCommunity = jsondata.get("wcommunity").toString();
			osp.sessionList.add(sessionID);
			realTimeDevHashtable.put(netaddr, osp);
			System.out.println("----add new RealTimeDev=="+ realTimeDevHashtable.size());
		}		
	}
	
	public synchronized ObjSnmpPreail getRealTimeDev(String netaddr){
		if(realTimeDevHashtable.containsKey(netaddr)){
			return realTimeDevHashtable.get(netaddr);
		}else{
			return null;
		}		
	}
	
	public synchronized Hashtable<String, ObjSnmpPreail> getAllRealTimeDev(){
		return this.realTimeDevHashtable;
	}
	
	public synchronized void removeRealTimeDev(String netaddr, String sessionID){
		if(realTimeDevHashtable.containsKey(netaddr)){
			ObjSnmpPreail osp = realTimeDevHashtable.get(netaddr);
			if(osp.sessionList.size() > 0){
				osp.sessionList.remove(sessionID);
				if(osp.sessionList.size() == 0){
					realTimeDevHashtable.remove(netaddr);
					System.out.println("----del RealTimeDev==" + realTimeDevHashtable.size());
				}
			}
		}		
	}
	
	public synchronized void removeRealTimeDev(String sessionID){
		for (Map.Entry<String, ObjSnmpPreail> entry : realTimeDevHashtable.entrySet()) {
			ObjSnmpPreail osp = (ObjSnmpPreail)entry.getValue();
			if(osp.sessionList.size() > 0){
				osp.sessionList.remove(sessionID);
				if(osp.sessionList.size() == 0){
					realTimeDevHashtable.remove(entry.getKey());
					System.out.println("----del RealTimeDev==" + realTimeDevHashtable.size());
				}
			}
		}		
	}
	
	public Session getSessionByID(String sessionid){
		synchronized(this) {
			for (Session session : webSocketClients) {
	            try {
	                synchronized (session) {
	                    if(session.getId().equalsIgnoreCase(sessionid)){
	                    	//System.out.println("Session Got::" + sessionid);
	                    	return session;
	                    }
	                }
	            } catch (Exception ex) {
	            	webSocketClients.remove(session);
	            	removeRealTimeDev(sessionid);
	            	System.out.println("Connection closed::::" + webSocketClients.size());
	            	ex.printStackTrace();
	                try {
	                    session.close();
	                } catch (IOException e1) {
	                	e1.printStackTrace();
	                }                
	                
	            }
	        }
			return null;
		}		
	}
	
	public void sendRemoteStr(String message, String sessionid){
		Session ses = getSessionByID(sessionid);
		if(ses != null){
			try {
				ses.getBasicRemote().sendText(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				log.info(e.getMessage());
			}
		}else{
			System.out.println("No Session Found::::");
		}	
	}
	
	public void broadCast(String message) {
		synchronized(this) { 
			for (Session session : webSocketClients) {
	            try {
	                synchronized (session) {
	                    session.getBasicRemote().sendText(message);
	                }
	            } catch (IOException e) {
	            	webSocketClients.remove(session);
	            	removeRealTimeDev(session.getId());
	            	System.out.println("Connection closed::::" + webSocketClients.size());
	                try {
	                    session.close();
	                } catch (IOException e1) {
	                	e1.printStackTrace();
	                }
	                
	            }
	        }
		}
        
    }
	
}
