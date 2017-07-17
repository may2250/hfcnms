package com.xinlong.Services;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
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
import sun.misc.BASE64Decoder;



@ServerEndpoint("/websocketservice/{username}/{password}")
public class Services_Websocket {
	private static Logger log = Logger.getLogger(Services_Websocket.class);
	private static final String  MAINKERNEL_MESSAGE =  "mainkernel.message";
	private static final String  PARAMKERNEL_MESSAGE =  "paramkernel.message";
	private static final String HFCALARM_MESSAGE = "currentalarm.message";
	private static final String deskey = "prevail0";
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
        System.out.println("Services_Websocket Received: " + message);
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
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);				
			}else if(cmd.equalsIgnoreCase("getgrouptree")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("getInitLog")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
				sendToQueue(jsondata.toJSONString(), HFCALARM_MESSAGE);
			}else if(cmd.equalsIgnoreCase("nodeadd")){
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("nodeedit")){
				sendToQueue(message, MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("nodedel")){
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("lazyLoad")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("deviceadd")){				
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("hfcvalueset")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("getdevicedetail")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("devsearch")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("deviceclose")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			}else if(cmd.equalsIgnoreCase("alarmsearch")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), HFCALARM_MESSAGE);
			}else if(cmd.equalsIgnoreCase("optlogsearch")){
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), HFCALARM_MESSAGE);
			}else{
				sendToQueue(message, MAINKERNEL_MESSAGE);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}
    	
    }
    
   
    private void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(queue, msg);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}finally{
			redisUtil.closeConnection(jedis);
		}
	}    
    

    @OnOpen
    public void onOpen(@PathParam("username") String username,
			@PathParam("password") String password,Session session) {
    	JSONObject rootjson = new JSONObject();
		rootjson.put("cmd", "loginAuth");
    	if(username.equalsIgnoreCase("undefined") || password.equalsIgnoreCase("undefined")){    		
    		rootjson.put("Authed", false);
        	try {
    			session.getBasicRemote().sendText(rootjson.toJSONString());
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}else{    		
    		try {
    			//解密
				String passWord = decrypt(password, deskey);
				System.out.println("username::des::" + username + ":::password:des::"+ passWord);
				//用户认证
	    		//TODO
				
				if(true){
					//认证成功
					rootjson.put("Authed", true);
		    		staticmemory.AddSession(session);
		            System.out.println("Client connected::::" + staticmemory.getCount());
				}else{
					rootjson.put("Authed", false);
				}				
		    	try {
					session.getBasicRemote().sendText(rootjson.toJSONString());
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	rootjson = new JSONObject();
		    	rootjson.put("cmd", "getLoginInit");
		    	rootjson.put("sessionid", session.getId());
		    	sendToQueue(rootjson.toJSONString(), MAINKERNEL_MESSAGE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		
    	}
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
    
    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws IOException,
            Exception {
        if (data == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buf = decoder.decodeBuffer(data);
        byte[] bt = decrypt(buf,key.getBytes());
        return new String(bt);
    }
    
    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
 
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
 
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
 
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
 
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
 
        return cipher.doFinal(data);
    }

}