package wl.hfc.online;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.Services.ServiceHfcAlarmProcessor;
import com.xinlong.Services.Services_Websocket;
import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.hfc.common.*;
import wl.hfc.topd.MainKernel;


//DevGrpModel将承担拓扑的组建，维护，以及组，设备的增删查改的响应
public class ParamKernel {
	private static final String  PARAMKERNEL_MESSAGE =  "paramkernel.message";
	private static Logger log = Logger.getLogger(ParamKernel.class);

    public ParamKernel()
    {

    }

	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		ParamKernel.redisUtil = redisUtil;
	}
	
	public static void setStaticMemory(StaticMemory staticmemory) {
		ParamKernel.staticmemory = staticmemory;
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
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();	
  			log.info(e.getMessage());
  		}
  		
      }

	};
	
	private void servicestart(String message) throws InterruptedException, ParseException, IOException{
		System.out.println(" [x] ParamKernel Received: '" + message + "'");			
		JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		String cmd = jsondata.get("cmd").toString();
		JSONObject rootjson = new JSONObject();
		if(cmd.equalsIgnoreCase("hfcvalueset")){
			hfcValueSet(jsondata);			
		}else if(cmd.equalsIgnoreCase("getdevicedetail")){
			staticmemory.sendRemoteStr(hfcDeviceDetail(jsondata), jsondata.get("sessionid").toString());				
		}
	}
	

	
	private String hfcDeviceDetail(JSONObject jsondata){
		//获取设备详细信息		
		String netaddr = jsondata.get("ip").toString();
		String devtype = jsondata.get("devtype").toString();		 
		JSONObject rootjson = new JSONObject();
    	rootjson.put("cmd", "getdevicedetail");
    	
    	DevTopd dev = (DevTopd)MainKernel.me.listDevHash.get(netaddr);
    	rootjson.put("key", netaddr);
      	if (dev==null) {   
      		rootjson.put("isonline", false);
      		return rootjson.toJSONString();			
		}
      	
    	nojuDeviceTableRow mDeviceTableRow = dev.BindnojuDeviceTableRow;
    	rootjson.put("isonline", dev.isOline);
		if(devtype.equalsIgnoreCase("other")){
			
		}else if(devtype.equalsIgnoreCase("EDFA")){
			
		}else if(devtype.equalsIgnoreCase("Trans")){
			
		}else if(devtype.equalsIgnoreCase("rece_workstation")){
			
		}else if(devtype.equalsIgnoreCase("OSW")){
			
		}else if(devtype.equalsIgnoreCase("RFSW")){
			
		}else if(devtype.equalsIgnoreCase("PreAMP")){
			
		}else if(devtype.equalsIgnoreCase("wos")){
			
		}
		return rootjson.toJSONString();
	}
	
	private void hfcValueSet(JSONObject jsondata){
		JSONObject rootjson = new JSONObject();
		String target = jsondata.get("target").toString();
		if(target.equalsIgnoreCase("devicetrapedit")){
			//修改设备Trap主机地址
			rootjson.put("cmd", "hfcvalueset");
			rootjson.put("target", "devicetrapedit");
			rootjson.put("domstr", jsondata.get("domstr").toString());
			rootjson.put("value", jsondata.get("value").toString());
			//TODO
			//发送到设备
			
			
			staticmemory.sendRemoteStr(rootjson.toJSONString(), jsondata.get("sessionid").toString());
		}else if(target.equalsIgnoreCase("devicechannel")){
			//修改设备频道数
			rootjson.put("cmd", "hfcvalueset");
			rootjson.put("target", "devicechannel");
			rootjson.put("domstr", jsondata.get("domstr").toString());
			rootjson.put("value", jsondata.get("value").toString());
			//TODO
			//发送到设备
			
			
			staticmemory.sendRemoteStr(rootjson.toJSONString(), jsondata.get("sessionid").toString());
		}
	}
  	
    
    @SuppressWarnings("static-access")
	public void start(){
		
		log.info("[#3] .....ParamKernel starting.......");
		Jedis jedis=null;
		try {		
			jedis = redisUtil.getConnection();		 
			jedis.psubscribe(jedissubSub, PARAMKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis); 
			  
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
}
