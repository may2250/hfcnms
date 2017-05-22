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

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;



@ServerEndpoint("/websocketservice")
public class Services_Websocket {
	private static Logger log = Logger.getLogger(Services_Websocket.class);
	private static final String  MAINKERNEL_MESSAGE =  "mainkernel.message";
	
	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		Services_Websocket.redisUtil = redisUtil;
	}
	
	public static void setStaticMemory(StaticMemory staticmemory) {
		Services_Websocket.staticmemory = staticmemory;
	}
	
	
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {

        // Print the client message for testing purposes
        System.out.println("Received: " + message);
        parseWebMessage(message, session);        

    }
    
    @SuppressWarnings("unchecked")
	private void parseWebMessage(String message, Session session) throws IOException, InterruptedException{
    	try {
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String cmd = jsondata.get("cmd").toString();
			JSONObject rootjson = new JSONObject();
			if(cmd.equalsIgnoreCase("getInitTree")){
				//获取设备数结构
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString());				
			}else if(cmd.equalsIgnoreCase("getInitLog")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("nodeadd")){
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("nodeedit")){
				rootjson.put("cmd", "nodeedit");
				rootjson.put("key", jsondata.get("key").toString());
				rootjson.put("title", jsondata.get("value").toString());
				sendToQueue(rootjson.toJSONString());
			}else if(cmd.equalsIgnoreCase("nodedel")){
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("lazyLoad")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("deviceadd")){				
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("hfcvalueset")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString());
			}else if(cmd.equalsIgnoreCase("getdevicedetail")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString());
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
			}else{
				sendToQueue(message);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}
    	
    }
    
   
    private void sendToQueue(String msg) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(MAINKERNEL_MESSAGE, msg);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}finally{
			redisUtil.closeConnection(jedis);
		}
	}    
    

    @OnOpen
    public void onOpen(Session session) {
    	staticmemory.AddSession(session);
        System.out.println("Client connected::::" + staticmemory.getCount());
    }
    
    @OnError
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }


    @OnClose
    public void onClose(Session session) {
    	staticmemory.RemoveSession(session);
        System.out.println("Connection closed::::" + staticmemory.getCount());        
    }

}