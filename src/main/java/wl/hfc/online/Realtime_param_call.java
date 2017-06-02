package wl.hfc.online;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import wl.hfc.common.CDevForCMD;

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
				System.out.println("=====begin to realtime process=====");
				for (Map.Entry<String, CDevForCMD> entry : staticmemory.getAllRealTimeDev().entrySet()) {					
					try {
						JSONObject json = new JSONObject();
						CDevForCMD cfc = (CDevForCMD)entry.getValue();
						System.out.println("=====netaddr====="+ cfc.mNetAddress);
						ReceiverSnmpPrevail receiverSnmpPrevail1Prevai11ll = new ReceiverSnmpPrevail(".1");
						receiverSnmpPrevail1Prevai11ll.thisDev = cfc;
						receiverSnmpPrevail1Prevai11ll.sver = new PDUServerForOneDev(0);
						json.put("cmd", "realtime-device");
						json.put("devtype", cfc.mNetAddress);
						json = receiverSnmpPrevail1Prevai11ll.getPmWithModelNumber(json);
						System.out.println(json.toJSONString());
						for(Iterator it2 = cfc.sessionList.iterator();it2.hasNext();){
							 staticmemory.sendRemoteStr(json.toJSONString(), it2.next().toString());
				        }

					} catch (Exception e) {
						e.printStackTrace();
						log.info("......[x1]Realtime_param_call Done!" + e.getMessage());
						return;

					}

				}    		
			}			
			Thread.sleep(3000);
		}
	}
}
