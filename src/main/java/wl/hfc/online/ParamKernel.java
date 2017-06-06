package wl.hfc.online;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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
		if(cmd.equalsIgnoreCase("hfcvalueset")){
			hfcValueSet(jsondata);			
		}else if(cmd.equalsIgnoreCase("getdevicedetail")){
			hfcDeviceDetail(jsondata);				
		}else if(cmd.equalsIgnoreCase("deviceclose")){			
			staticmemory.removeRealTimeDev(jsondata.get("ip").toString(),jsondata.get("sessionid").toString());
		}
	}
	

	
	private void hfcDeviceDetail(JSONObject jsondata){
		//获取设备详细信息	
		//每个客户端只能打开一台设备，删除原来打开设备记录	
		if(!jsondata.get("predev").toString().equalsIgnoreCase("")){
			staticmemory.removeRealTimeDev(jsondata.get("predev").toString(),jsondata.get("sessionid").toString());
		}		
		staticmemory.addRealTimeDev(jsondata);
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
		}else if(target.equalsIgnoreCase("getalarmThreshold")){
			//获取告警门限			
			staticmemory.sendRemoteStr(getAlarmThreshold(jsondata), jsondata.get("sessionid").toString());
		}else if(target.equalsIgnoreCase("setalarmThreshold")){
			//设置告警门限	
			setAlarmThreshold(jsondata);
			//staticmemory.sendRemoteStr(getAlarmThreshold(jsondata), jsondata.get("sessionid").toString());
		}
	}
	
	private String getAlarmThreshold(JSONObject jsondata){
		JSONObject rootjson = new JSONObject();
		String netaddr = jsondata.get("ip").toString();
		if(jsondata.get("domstr").toString().equalsIgnoreCase("detail_temper")){
			//获取温度相关门限信息
			
		}else if(jsondata.get("domstr").toString().equalsIgnoreCase("tbl_powerv1")){
			
		}else if(jsondata.get("domstr").toString().equalsIgnoreCase("tbl_powerv2")){
			
		}
		//TODO
		//test
		rootjson.put("ISHIHI", true);
		rootjson.put("HIHI", "60");
		rootjson.put("ISHI", true);
		rootjson.put("HI", "55");
		rootjson.put("ISLO", true);
		rootjson.put("LO", "30");
		rootjson.put("ISLOLO", true);
		rootjson.put("LOLO", "10");
		rootjson.put("ISDEAD", true);
		rootjson.put("DEAD", "0");
		jsondata.put("detail", rootjson);
		return jsondata.toJSONString();
	}
	
	private void setAlarmThreshold(JSONObject jsondata){
		String netaddr = jsondata.get("ip").toString();
		String ISHIHI = jsondata.get("ISHIHI").toString();
		String HIHI = jsondata.get("HIHI").toString();
		String ISHI = jsondata.get("ISHI").toString();
		String HI = jsondata.get("HI").toString();
		String ISLO = jsondata.get("ISLO").toString();
		String LO = jsondata.get("LO").toString();
		String ISLOLO = jsondata.get("ISLOLO").toString();
		String LOLO = jsondata.get("LOLO").toString();
		String ISDEAD = jsondata.get("ISDEAD").toString();
		String DEAD = jsondata.get("DEAD").toString();
		if(jsondata.get("domstr").toString().equalsIgnoreCase("detail_temper")){
			//设置温度相关门限信息
			//TODO
			
		}else if(jsondata.get("domstr").toString().equalsIgnoreCase("tbl_powerv1")){
			
		}else if(jsondata.get("domstr").toString().equalsIgnoreCase("tbl_powerv2")){
			
		}
	}
  	
    
    @SuppressWarnings("static-access")
	public void start() throws InterruptedException{
		
		log.info("[#3] .....ParamKernel starting.......");
		Jedis jedis=null;
		try {		
			new pmls();
			jedis = redisUtil.getConnection();		 
			jedis.psubscribe(jedissubSub, PARAMKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis); 
			  
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
}
