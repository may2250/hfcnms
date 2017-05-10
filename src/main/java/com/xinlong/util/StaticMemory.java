package com.xinlong.util;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

public class StaticMemory {
	//save web sessions
	public static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();
	
	public void AddSession(Session session){
		webSocketClients.add(session);
	}
	
	public void RemoveSession(Session session){
		webSocketClients.remove(session);
	}
	
	public int getCount(){
		return webSocketClients.size();
	}
	
	public static void broadCast(String message) {
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
