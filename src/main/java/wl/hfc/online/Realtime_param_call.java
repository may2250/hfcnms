package wl.hfc.online;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.xinlong.util.ObjSnmpPreail;
import com.xinlong.util.StaticMemory;


public class Realtime_param_call {
	private static Logger log = Logger.getLogger(Realtime_param_call.class);

	private static StaticMemory staticmemory;
	
	public Realtime_param_call()
    {
    	  
    }
	
	public static void setStaticMemory(StaticMemory staticmemory) {
		Realtime_param_call.staticmemory = staticmemory;
	}
	@SuppressWarnings("static-access")
	public void start() throws InterruptedException{
		
		log.info("[#3] .....Realtime_param_call starting.......");
		
		while (true) {
			if(!staticmemory.getAllRealTimeDev().isEmpty()){
				//System.out.println("=====begin to realtime process=====");
				Enumeration<String> e1 = staticmemory.getAllRealTimeDev().keys();
				while (e1.hasMoreElements()) {   
					String key = e1.nextElement();  
					try {
						JSONObject json = new JSONObject();
						JSONObject commonjson = new JSONObject();
						ObjSnmpPreail osp = staticmemory.getRealTimeDev(key);
						if(osp != null){
							json.put("cmd", "realtime-device");
							String devtype = osp.snmpPreail.thisDev.mNetType.toString();
							json.put("devtype", osp.snmpPreail.thisDev.mNetType.toString());
							if(devtype.equalsIgnoreCase("EDFA")){
								
							}else if(devtype.equalsIgnoreCase("Trans")){
								
							}else if(devtype.equalsIgnoreCase("rece_workstation")){
								json = ((ReceiverSnmpPrevail)osp.snmpPreail).getPmWithModelNumber(json);
								commonjson=((CommonSnmpPrevail)osp.commonSnmpPreail).getPmWithModelNumber(commonjson);
							}else if(devtype.equalsIgnoreCase("OSW")){
								
							}else if(devtype.equalsIgnoreCase("RFSW")){
								
							}else if(devtype.equalsIgnoreCase("PreAMP")){
								
							}else if(devtype.equalsIgnoreCase("wos")){
								
							}else{
								json = ((ReceiverSnmpPrevail)osp.snmpPreail).getPmWithModelNumber(json);
							}			
							
							if(json == null)
								continue;
							json.put("common", commonjson);
							String jsonstr = json.toJSONString();
							//System.out.println(jsonstr);
							for(Iterator it2 = osp.sessionList.iterator();it2.hasNext();){
								 staticmemory.sendRemoteStr(jsonstr, it2.next().toString());
					        }
						}
					} catch (Exception e) {
						//e.printStackTrace();
						//log.info("......[x1]Realtime_param_call Done!" + e.getMessage());			

					}
				}
			}			
			Thread.sleep(1500);
		}
	}
}
