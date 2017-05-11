package com.xinlong.util;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

public class StaticMemory {
	//save web sessions
	public static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();
	
	public void AddSession(Session session){
		synchronized(this) { 
			webSocketClients.add(session);
		}		
	}
	
	public void RemoveSession(Session session){
		synchronized(this) { 
			webSocketClients.remove(session);
		}		
	}
	
	public int getCount(){
		return webSocketClients.size();
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
	
	public void broadCast(String message) {
		synchronized(this) { 
			for (Session session : webSocketClients) {
	            try {
	                synchronized (session) {
	                    session.getBasicRemote().sendText(message);
	                }
	            } catch (IOException e) {
	            	webSocketClients.remove(session);;
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
