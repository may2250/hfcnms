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
public class Sstatus {	

		private static final String  Sstatus_MESSAGE =  "sstatus.message";
		private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
		private static Logger log = Logger.getLogger(Sstatus.class);	
		public static boolean isRedis=false;
		public static String versionString="V1.05";
		public static String Supporteddevices="Supported devices：WE-HD,WR1001J,WR1002RJ-II,WR1002JSE,EM30,WT-1550-DM-I,Optical switch";

		private    RedisUtil redisUtil;
		public  static boolean redisStartus=true;
	    public Sstatus( RedisUtil predisUtil)
	    {	    	
	    	redisUtil=predisUtil;
	    }
		
	}
