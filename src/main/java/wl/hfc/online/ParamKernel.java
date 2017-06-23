package wl.hfc.online;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.util.ObjSnmpPreail;
import com.xinlong.util.RedisUtil;
import com.xinlong.util.SearchIpInfo;
import com.xinlong.util.StaticMemory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.commonComponent.DeviceSearchEngine;


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
		}else if(cmd.equalsIgnoreCase("devsearch")){
			devSerach(jsondata);
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
		String netaddr = jsondata.get("ip").toString();
		ObjSnmpPreail osp = staticmemory.getRealTimeDev(netaddr);
	//	WosBaseSnmp snmpPreail = osp.snmpPreail;
		if(target.equalsIgnoreCase("setVars")){

			if(jsondata.get("isRow").toString().equalsIgnoreCase("false")){//普通参数
				
				osp.snmpPreail.setVars(jsondata.get("domstr").toString(),jsondata.get("value").toString());
			}
			else//表内参数
			{			
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.setTableVars(jsondata.get("domstr").toString(),jsondata.get("value").toString(),row);
				
			}
			//staticmemory.sendRemoteStr(rootjson.toJSONString(), jsondata.get("sessionid").toString());
		}else if(target.equalsIgnoreCase("setTrapHost"))
		{
			
			String rowString = jsondata.get("rowNum").toString();
			int row = Integer.parseInt(rowString);
			osp.commonSnmpPreail.setStringVars(jsondata.get("domstr").toString(),jsondata.get("value").toString(),row);
			
		
		}		
		else if(target.equalsIgnoreCase("getalarmThreshold")){

			if(jsondata.get("isRow").toString().equalsIgnoreCase("false")){//普通参数
				
				osp.snmpPreail.getSubVarsBYparamname(jsondata.get("domstr").toString(),rootjson);
			}
			else//表内参数
			{			
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.getSubVarsBYparamname(jsondata.get("domstr").toString(),rootjson,row);
				
			}		
			jsondata.put("detail", rootjson);		
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
		}else if(target.equalsIgnoreCase("setalarmThreshold")){			

			rootjson.put("hihi", jsondata.get("HIHI").toString());
			rootjson.put("hi", jsondata.get("HI").toString());
			rootjson.put("lo", jsondata.get("LO").toString());
			rootjson.put("lolo", jsondata.get("LOLO").toString());
			rootjson.put("deadb", jsondata.get("DEAD").toString());
			byte en = 0;
			if(jsondata.get("ISLOLO").toString().equalsIgnoreCase("true")){
				en = (byte)(en | 0x01);
			}
			if(jsondata.get("ISLO").toString().equalsIgnoreCase("true")){
				en = (byte)(en | 0x02);
			}
			if(jsondata.get("ISHI").toString().equalsIgnoreCase("true")){
				en = (byte)(en | 0x04);
			}
			if(jsondata.get("ISHIHI").toString().equalsIgnoreCase("true")){
				en = (byte)(en | 0x08);
			}
			rootjson.put("en", en);
			if(jsondata.get("isRow").toString().equalsIgnoreCase("false")){//普通参数
				
				osp.snmpPreail.setSubVarsBYparamname(jsondata.get("domstr").toString(),rootjson);
			}
			else//表内参数
			{			
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.setSubVarsTableBYparamname(jsondata.get("domstr").toString(),rootjson,row);
				
			}
		}
	}
	


	
	private void devSerach(JSONObject jsondata) throws NumberFormatException, IOException{
		String devtype = jsondata.get("devtype").toString();
		
		SearchIpInfo searchinfo = new SearchIpInfo(InetAddress.getByName(jsondata.get("startip").toString()) , InetAddress.getByName(jsondata.get("endip").toString()),false, Integer.parseInt(devtype));
		searchinfo.sessionid = jsondata.get("sessionid").toString();
		DeviceSearchEngine dse = new DeviceSearchEngine(searchinfo, staticmemory);
		dse.start();
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
