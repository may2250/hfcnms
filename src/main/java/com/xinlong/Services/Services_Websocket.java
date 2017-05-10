package com.xinlong.Services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@ServerEndpoint("/websocketservice")
public class Services_Websocket {
	private static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();

	private static Logger log = Logger.getLogger(Services_Websocket.class);
	
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {

        // Print the client message for testing purposes
        System.out.println("Received: " + message);
		try {
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String cmd = jsondata.get("cmd").toString();
			JSONObject rootjson = new JSONObject();
			if(cmd.equalsIgnoreCase("getInitTree")){
				//获取设备数结构				
				JSONArray jsonarray = new JSONArray();
				rootjson.put("cmd", "getInitTree");
				JSONObject sysjson = new JSONObject();
				sysjson.put("key", "0");
				sysjson.put("pkey", "");
				sysjson.put("title", "中心机房");
				sysjson.put("type", "system");
				sysjson.put("isFolder", "true");
				sysjson.put("expand", "true");
				sysjson.put("icon", "images/net_center.png");
				jsonarray.add(sysjson);
				rootjson.put("treenodes", jsonarray);
				String jsonString = rootjson.toJSONString();
				session.getBasicRemote().sendText(jsonString);
				System.out.println(jsonString);
			}else if(cmd.equalsIgnoreCase("nodeadd")){
				rootjson.put("cmd", "nodeadd");
				rootjson.put("key", "1");
				rootjson.put("pkey", jsondata.get("key").toString());
				rootjson.put("title", jsondata.get("value").toString());
				rootjson.put("type", "custom");
				rootjson.put("isFolder", true);
				rootjson.put("expand", true);
				rootjson.put("icon", "images/net_center.png");
				broadCast(rootjson.toJSONString());
			}else if(cmd.equalsIgnoreCase("test")){
				// Send the first message to the client
				rootjson.put("cmd", "test");
				rootjson.put("message", "This is the first server message");
		        session.getBasicRemote().sendText(rootjson.toJSONString());
		        
		        // Send 3 messages to the client every 5 seconds
		        int sentMessages = 0;
		        while (sentMessages < 3) {
		            Thread.sleep(5000);
		            rootjson.put("message", "This is an intermediate server message. Count: " + sentMessages);
		            session.getBasicRemote().sendText(rootjson.toJSONString());
		            sentMessages++;
		        }        
		        // Send a final message to the client
		        rootjson.put("message", "This is the last server message");
		        session.getBasicRemote().sendText(rootjson.toJSONString());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}
		
        

    }
    
    private static void broadCast(String message) {
        for (Session session : webSocketClients) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
            	webSocketClients.remove(session);
            	System.out.println("Connection closed::::" + webSocketClients.size());
                try {
                    session.close();
                } catch (IOException e1) {
                }
                
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
    	webSocketClients.add(session);
        System.out.println("Client connected::::" + webSocketClients.size());
    }
    
    @OnError
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }


    @OnClose
    public void onClose(Session session) {
    	webSocketClients.remove(session);
        System.out.println("Connection closed::::" + webSocketClients.size());        
    }

}