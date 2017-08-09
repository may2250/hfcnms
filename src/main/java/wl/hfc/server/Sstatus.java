package wl.hfc.server;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.hfc.common.CDatabaseEngine;
import wl.hfc.online.PDUServer;
import wl.hfc.traprcss.TrapPduServer;

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;


// common class for response  the  other informations of
public class Sstatus extends Thread{	

		private static final String  Sstatus_MESSAGE =  "sstatus.message";
		private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
		private static Logger log = Logger.getLogger(Sstatus.class);	
		
		public static String versionString="V1.03";
		private    RedisUtil redisUtil;
		public  static boolean redisStartus=true;
	    public Sstatus( RedisUtil predisUtil)
	    {	    	
	    	redisUtil=predisUtil;
	    }

		public void run() {
			Jedis jedis = null;
			try {

				jedis = redisUtil.getConnection();
				jedis.psubscribe(jedissubSub, Sstatus_MESSAGE);
				redisUtil.getJedisPool().returnResource(jedis);


			} catch (Exception e) {
				redisStartus=false;

				e.printStackTrace();
				log.info(e.getMessage());

				redisUtil.getJedisPool().returnResource(jedis);

			}
		}
		
		private   JedisPubSub jedissubSub = new JedisPubSub() {
			public void onUnsubscribe(String arg0, int arg1) {

	        }
			public void onSubscribe(String arg0, int arg1) {

	        }
			 public void onMessage(String arg0, String arg1) {
		       
		     }
			 public void onPUnsubscribe(String arg0, int arg1) {

		        }
			 public void onPSubscribe(String arg0, int arg1) {

		        } 

	      public void onPMessage(String arg0, String arg1, String msg) {
	      	try {  			
	  			phraseMSG(msg);
	  			
	  		}catch(Exception e){
	  			e.printStackTrace();	
	  			log.info(e.getMessage());
	  		}
	  		
	      }

		};
		
		private void phraseMSG(String message) throws InterruptedException, ParseException, IOException{
			System.out.println(" [x] sstatus Received: '" + message + "'");			
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String cmd = jsondata.get("cmd").toString();

			if(cmd.equalsIgnoreCase("severstatus")){
				//trap监听模块
				jsondata.put("TrapPduServerstatus", TrapPduServer.TrapPduServer_status);//trap listen status
				
				//数据库状态
				jsondata.put("CDatabaseEngineflag", CDatabaseEngine.flag);//last time  database status		
/*				
				//参数轮询模块
				jsondata.put("PDUServerstatus", PDUServer.PDUServer_status);//	pduserver init		
				*/
				
				//已连接客户端数量
				jsondata.put("clientNum", StaticMemory.webSocketClients.size());//	pduserver init			
				
				
				//send to mainkernel
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
				//staticmemory.sendRemoteStr(jsondata, jsondata.get("sessionid").toString());	
		}

		}
		
		public void sendToQueue(String msg, String queue) {
			Jedis jedis = null;
			try {
				jedis = redisUtil.getConnection();
				jedis.publish(queue, msg);

			} catch (Exception e) {
				log.info(e.getMessage());

			} finally {
				redisUtil.closeConnection(jedis);
			}
		}

		public boolean testJedis(){
	    	Jedis jedis=null;
			try {		
				jedis = redisUtil.getConnection();	
				return true;
				  
			}catch(Exception e){
				e.printStackTrace();
				redisUtil.getJedisPool().returnBrokenResource(jedis);
				return false;
			}
			
		}
		
	}
