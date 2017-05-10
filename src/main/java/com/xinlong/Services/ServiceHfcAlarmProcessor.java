package com.xinlong.Services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;


public class ServiceHfcAlarmProcessor {
	private static final String  HFCALARM_MESSAGE =  "servicehfcalarm.message" ;
	private static final String DB_QUEUE_NAME = "db_queue_message";//���ݿ���Ϣ����
	private static Logger log = Logger.getLogger(ServiceHfcAlarmProcessor.class);
	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		ServiceHfcAlarmProcessor.redisUtil = redisUtil;
	}
	
	public static void setStaticMemory(StaticMemory staticmemory) {
		ServiceHfcAlarmProcessor.staticmemory = staticmemory;
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

      	//System.out.println("[x]ServiceHearbertProcesser  Subscribing....pmessage....now receive on msgarge1 [" + arg1 + "] arg2=["+msg +"]");
      	try {
  			//arg2 is mssage now is currenti p
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();			
  		}
  		
      }

	};
  	
  	
	@SuppressWarnings("static-access")
	public void start(){
		
		log.info("[#3] ..... service HfcAlarm starting");
		Jedis jedis=null;
		try {			
			  jedis = redisUtil.getConnection();		 
			  jedis.psubscribe(jedissubSub, HFCALARM_MESSAGE);
			  redisUtil.getJedisPool().returnResource(jedis);
		  
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
	
	private void servicestart(String message) throws InterruptedException, ParseException, IOException{
		System.out.println(" [x] ServiceHfcAlarmProcessor Received: '" + message + "'");			
		//dowork(message);		
		//test TODO
		JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		String cmd = jsondata.get("cmd").toString();
		if(cmd.equalsIgnoreCase("nodeadd")){
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "nodeadd");
			rootjson.put("key", "1");
			rootjson.put("pkey", jsondata.get("pkey").toString());
			rootjson.put("title", jsondata.get("title").toString());
			rootjson.put("type", "custom");
			rootjson.put("isFolder", true);
			rootjson.put("expand", true);
			rootjson.put("icon", "images/net_center.png");
			staticmemory.broadCast(rootjson.toJSONString());
		}
		

	}
	
	private void dowork(String message) throws ParseException, IOException{
		JSONParser parser = new JSONParser();
		
		ContainerFactory containerFactory = new ContainerFactory(){
		    public List<?> creatArrayContainer() {
		      return new LinkedList<Object>();
		    }

		    public Map<?, ?> createObjectContainer() {
		      return new LinkedHashMap<Object, Object>();
		    }
		                        
		  };
		  
		  Map<String, String> alarm = (Map<String, String>)parser.parse(message, containerFactory);
		  
		  dohfcalarm(alarm);
	}
	
	private void dohfcalarm(Map<String,String> alarm) throws IOException{
		String devmac = alarm.get("mac");		
		String traptype = alarm.get("traptype");
		String enterprise = alarm.get("enterprise");
		//log.info("------------->>>---traptype---"+traptype+"----enterprise---"+enterprise);
		try{
			if(Integer.valueOf(traptype)!= 6){
				ProcessGenericTraps(Integer.valueOf(traptype),devmac);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.17409.1")){
				ProcessHFCTraps(alarm);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.17409.8888.1")){
				ProcessWosTraps(alarm);
			}else if(enterprise.equalsIgnoreCase("1.3.6.1.4.1.2000.1.3000")){
				ProcessWos3kTraps(alarm);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	private void ProcessWosTraps(Map<String,String> alarm)
    {
		String status = alarm.get("status");
        switch (Integer.valueOf(status))
        {
            case 1://wosTrapRestart
                ParseTrapWosTrapRestart(alarm);
                return;
            case 2://wosTrapDeviceUp
                ParseTrapWosTrapDeviceUp(alarm);
                return;
            case 3://wosTrapDeviceDown
                ParseTrapWosTrapDeviceDown(alarm);
                return;
        }
    }
    private void ProcessWos3kTraps(Map<String,String> alarm)
    {
    	String status = alarm.get("status");
        switch (Integer.valueOf(status))
        {
            case 1://wosTrapRestart
                ParseTrapWosTrapRestart(alarm);
                return;
            case 2://wosTrapDeviceUp
                ParseTrapWosTrapDeviceUp(alarm);
                return;
            case 3://wosTrapDeviceDown
                ParseTrapWosTrapDeviceDown(alarm);
                return;
            case 4:   
                //ParseTrapWos3kAlarmEvent(alarm);
               return;
        }
    }
	
	 public void ProcessHFCTraps(Map<String,String> alarm) throws IOException
     {
		 String logicalid = alarm.get("logicalid");
		 String alarminfo = alarm.get("alarminfo");
		 String status = alarm.get("status");
		 String devmac = alarm.get("mac");
		 switch(Integer.valueOf(status)){
			case 0://hfcColdstart
				ParseTrapHfcColdStart(devmac,logicalid);
				break;
			case 1://hfcAlarmevent
				ParseTrapHfcAlarmEvent(devmac,logicalid,alarminfo);
				break;
			case 8686://osSwitchevnet
				ParseTrapHfcOsSwitchEvent(devmac,logicalid,alarminfo);
				break;
			default:
				break;		
		}
     }
	
	public void ProcessGenericTraps(int traptype, String mac)
    {
        String cntrapstring = "";
        String entrapstring = "";
        Map<String, String> hash = new LinkedHashMap();
        switch (traptype)
        { 
            case 0:
            	cntrapstring = "标准冷启�?";
            	entrapstring = "Standard ColdStart";
            	hash.put("alarmlevel", "2");
                break;
            case 1:
            	cntrapstring = "标准热启�?";
            	entrapstring = "Standard WarmStart";
            	hash.put("alarmlevel", "3");
                break;
            case 2:
            	cntrapstring = "标准连接断开";
            	entrapstring = "Standard UnLink";
            	hash.put("alarmlevel", "2");
                break;
            case 3:
            	cntrapstring = "标准连接成功";
            	entrapstring = "Standard Connect";
            	hash.put("alarmlevel", "6");
                break;
            /*
            case 4:
                trapstring = "标准签名错误";
                break;
             */
            case 5:
            	cntrapstring = "标准目标丢失";
            	entrapstring = "Standard Lose";
            	hash.put("alarmlevel", "3");
                break;
            default:
                return;
        }
        
		long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("mac", mac);
		hash.put("alarmcode", "200940");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);		
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));

    }
	
	public void ParseTrapWosTrapRestart(Map<String,String> alarm)
    {
        String cntrapstring = "WOS光平台重启动�?";
        String entrapstring = "WOS PlatForm Restart�?";
        String devmac = alarm.get("mac");
        String logicalid = alarm.get("logicalid");
        cntrapstring += ",物理地址�?" + devmac;
        cntrapstring += " ,软件版本�?" + Float.valueOf(logicalid) / 100.0f;
        entrapstring += ",MAC�?" + devmac;
        entrapstring += " ,Version�?" + Float.valueOf(logicalid) / 100.0f;
        Map<String, String> hash = new LinkedHashMap();
        long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("mac", devmac);
		hash.put("alarmcode", "200940");
		hash.put("alarmlevel", "2");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);		
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		sendToAlarmQueue(JSONValue.toJSONString(hash));
    }
	
	public void ParseTrapWosTrapDeviceUp(Map<String,String> alarm)
    {

    }

    public void ParseTrapWosTrapDeviceDown(Map<String,String> alarm)
    {

    }

	
	public void ParseTrapHfcColdStart(String mac,String logicid){
		String cntrapstring = "";
		String entrapstring = "";
		cntrapstring = "HFC设备冷启�?,";
		cntrapstring += "逻辑ID:"+ logicid;
		entrapstring = "HFC Cold Start,";
		entrapstring += "Logical ID:"+ logicid;
		Map<String, String> hash = new LinkedHashMap();
		long alarmtime = System.currentTimeMillis();
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			 			 
		String alarmtimes = format.format(date);
		hash.put("mac", mac);
		hash.put("alarmcode", "200940");
		hash.put("lalarmtime", Long.toString(alarmtime));
		hash.put("salarmtime", alarmtimes);
		hash.put("alarmlevel", "2");
		hash.put("cnalarminfo", cntrapstring);
		hash.put("enalarminfo", entrapstring);
		//����WEBǰ��
		sendToAlarmQueue(JSONValue.toJSONString(hash));
	}
	
	public void ParseTrapHfcAlarmEvent(String mac,String logicid,String alarminfo) throws IOException{
		
	}
	
	public void ParseTrapHfcOsSwitchEvent(String mac,String logicid,String alarminfo){
		
	}
	
	public String GetAlarmEnumString(byte num){
		switch(num){
		case 1:
			return "NORMAL";
		case 2:
			return "HIHI";
		case 3:
			return "HI";
		case 4:
			return "LO";
		case 5:
			return "LOLO";
		case 6:
			return "Discrete Major";
		case 7:
			return "Discrete Minor";
		default:
			return "Unkown Alarm";
		}	
		
	}
	
	
	/** 
	 * Convert hex string to byte[] 
	 * @param hexString the hex string 
	 * @return byte[] 
	 */  
	public byte[] hexStringToBytes(String hexString) {  
		
		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}  

	 private void sendToAlarmQueue(String msg) {
		try {
			Jedis jedis = redisUtil.getConnection();
			jedis.publish("servicealarm.new", msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("TrapReceiverBean:sendToQueue error");

		}
	}
	 
	 //���洢��Ϣ�������ݿ����
	 private void sendToDBQueue(String msg) {
			try {
				Jedis jedis = redisUtil.getConnection();
				jedis.lpush(DB_QUEUE_NAME, msg);
				redisUtil.closeConnection(jedis);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//System.out.println("TrapReceiverBean:sendToQueue error");

			}
		}
}
