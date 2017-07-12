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
public class Sstatus{	

		private static final String  Sstatus_MESSAGE =  "sstatus.message";
		private static Logger log = Logger.getLogger(Sstatus.class);	
		private    RedisUtil redisUtil;
		
	    public Sstatus( RedisUtil redisUtil)
	    {	    	
	    	  
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
			System.out.println(" [x] ParamKernel Received: '" + message + "'");			
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String cmd = jsondata.get("cmd").toString();

			if(cmd.equalsIgnoreCase("severstatus")){
				jsondata.put("TrapPduServerstatus", TrapPduServer.TrapPduServer_status);//trap listen status
				jsondata.put("CDatabaseEngineflag", CDatabaseEngine.flag);//last time  database status		
				jsondata.put("PDUServerstatus", PDUServer.PDUServer_status);//	pduserver init		
				
				//send to client
				//staticmemory.sendRemoteStr(jsondata, jsondata.get("sessionid").toString());	
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
