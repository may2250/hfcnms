package com.xinlong.Services;

import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xinlong.util.MySqlUtil;
import com.xinlong.util.RedisUtil;

public class ServicesDB {
	private static final String  HFCDB_MESSAGE =  "servicedb.message";
	private static final String DB_QUEUE_NAME = "db_queue_message";
	private static Logger log = Logger.getLogger(ServicesDB.class);
	private static RedisUtil redisUtil;
	private static MySqlUtil mysqlUtil = new MySqlUtil();
	private String message;
	public Connection con; 

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServicesDB.redisUtil = redisUtil;
	}
  	
  	
	@SuppressWarnings("static-access")
	public void start(){
		
		log.info("[#3] ..... service DB starting");
		Jedis jedis=null;		
		try {		
			 jedis = redisUtil.getConnection();
			 con = mysqlUtil.getConnection();
			 while(true){
				 //从数据库队列中获取消息并处理
				 message = jedis.rpop(DB_QUEUE_NAME);
				 if(message != "" && message != null){
					 //System.out.println("[#7] ..... ServicesDB get a message working......");
					 parsemessage(message);			 
				 }else{
					 //System.out.println("[#6] ..... ServicesDB nomessage waitting......");
					 Thread.sleep(1000);
				 }
			 }			 			 
		}catch(Exception e){
			e.printStackTrace();
			log.info("[#0] ..... ServicesDB done");
			if(jedis != null)
				redisUtil.getJedisPool().returnResource(jedis);			
		}		
		
	}
	
	private void parsemessage(String message) {
		try {
			JSONParser parser = new JSONParser();
		
			ContainerFactory containerFactory = new ContainerFactory(){
		    public List<?> creatArrayContainer() {
		      return new LinkedList<Object>();
		    }

		    public Map<?, ?> createObjectContainer() {
		      return new LinkedHashMap<Object, Object>();
		    }
		                        
		  };
		  		  
		  Map<String, String> jsonmessage = (Map<String, String>)parser.parse(message, containerFactory);
		  
		 
		  dowork(jsonmessage);
		}
		catch(Exception e){
			e.printStackTrace();
			log.info(e.getMessage());
		}
	}
	
	private void dowork(Map<String,String> jsonmessage) throws IOException{
		
	}
	
}
