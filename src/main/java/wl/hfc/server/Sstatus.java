package wl.hfc.server;

import org.apache.log4j.Logger;
import com.xinlong.util.RedisUtil;



// common class for response  the  other informations of
public class Sstatus {	

		private static final String  Sstatus_MESSAGE =  "sstatus.message";
		private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
		private static Logger log = Logger.getLogger(Sstatus.class);	
		public static boolean isRedis=false;
		public static String versionString="V1.07";
		public static String Supporteddevices="Supported devices：WE-HD,WR1001J,WR1002RJ-II,WR1002JSE,EM30,WT-1550-DM-I,Optical switch";

		private    RedisUtil redisUtil;
		public  static boolean redisStartus=true;
	    public Sstatus( RedisUtil predisUtil)
	    {	    	
	    	redisUtil=predisUtil;
	    }
		
	}
