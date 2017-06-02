package com.xinlong.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import wl.hfc.common.CDevForCMD;
import wl.hfc.topd.MainKernel;

public class StaticMemory {
	//save web sessions
	public static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();
	//客户端需求实时数据的设备列表
	private Hashtable<String, CDevForCMD> realTimeDevHashtable=new Hashtable<String, CDevForCMD>();
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
			CDevForCMD cfc = realTimeDevHashtable.get(netaddr);
			if(!cfc.sessionList.contains(sessionID)){
				cfc.sessionList.add(sessionID);
				System.out.println("----add new SessionID");
			}
		}else{
			CDevForCMD cfc = new CDevForCMD();
			cfc.mNetAddress = netaddr;
			cfc.mNetType = MainKernel.me.getStringToNetType(jsondata.get("devtype").toString());
			cfc.ROCommunity = jsondata.get("rcommunity").toString();
			cfc.RWCommunity = jsondata.get("wcommunity").toString();
			cfc.sessionList.add(sessionID);
			realTimeDevHashtable.put(cfc.mNetAddress, cfc);
			System.out.println("----add new RealTimeDev");
		}		
	}
	
	public synchronized CDevForCMD getRealTimeDev(String netaddr){
		if(realTimeDevHashtable.containsKey(netaddr)){
			return realTimeDevHashtable.get(netaddr);
		}else{
			return null;
		}		
	}
	
	public synchronized Hashtable<String, CDevForCMD> getAllRealTimeDev(){
		return this.realTimeDevHashtable;
	}
	
	public synchronized void removeRealTimeDev(String netaddr, String sessionID){
		if(realTimeDevHashtable.containsKey(netaddr)){
			CDevForCMD cfc = realTimeDevHashtable.get(netaddr);
			if(cfc.sessionList.size() > 0){
				cfc.sessionList.remove(sessionID);
				if(cfc.sessionList.size() == 0){
					realTimeDevHashtable.remove(netaddr);
				}
			}
		}		
	}
	
	public synchronized void removeRealTimeDev(String sessionID){
		for (Map.Entry<String, CDevForCMD> entry : realTimeDevHashtable.entrySet()) {
			CDevForCMD cfc = (CDevForCMD)entry.getValue();
			if(cfc.sessionList.size() > 0){
				cfc.sessionList.remove(sessionID);
				if(cfc.sessionList.size() == 0){
					realTimeDevHashtable.remove(entry.getKey());
				}
			}
		}		
	}
	
	public Session getSessionByID(String sessionid){
		synchronized(this) {
			for (Session session : webSocketClients) {
	            try {
	                synchronized (session) {
	                	System.out.println("SessionID::::" + session.getId() + "::id::" + sessionid);
	                    if(session.getId().equalsIgnoreCase(sessionid)){
	                    	System.out.println("Session Got!");
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
				e.printStackTrace();
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
