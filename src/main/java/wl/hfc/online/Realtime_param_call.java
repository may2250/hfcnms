package wl.hfc.online;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.xinlong.util.ObjSnmpPreail;
import com.xinlong.util.StaticMemory;

public class Realtime_param_call extends Thread {
	private static Logger log = Logger.getLogger(Realtime_param_call.class);

	private static StaticMemory staticmemory;
	public static Realtime_param_call me;

	public Realtime_param_call() {
		this.setName("Realtime_param_call");
		me = this;
	}

	public static void setStaticMemory(StaticMemory staticmemory) {
		Realtime_param_call.staticmemory = staticmemory;
	}

	@SuppressWarnings("static-access")
	public void run() {

		log.info(this.getName() + "....starting.......");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while (true) {
			if (!staticmemory.getAllRealTimeDev().isEmpty()) {
				// System.out.println("RealTimeDev length is: "+
				// staticmemory.getAllRealTimeDev().size());
				Enumeration<String> e1 = staticmemory.getAllRealTimeDev().keys();
				while (e1.hasMoreElements()) {
					String key = e1.nextElement();
					// System.out.println("go to get param of ip ::: "+ key);
					try {
						Thread.sleep(100);
						JSONObject json = new JSONObject();
						JSONObject commonjson = new JSONObject();
						ObjSnmpPreail osp = staticmemory.getRealTimeDev(key);
						if (osp != null) {						
								
					
							json.put("cmd", "realtime-device");
							json.put("mytime", sdf.format(new Date()));
							
							
							json.put("devtype", osp.snmpPreail.thisDev.HFCType1.toString());
							json.put("icon", "../images/" + osp.snmpPreail.thisDev.imagePath + ".png");

							json = osp.snmpPreail.getPmWithModelNumber(json);
							
							commonjson = ((CommonSnmpPrevail) osp.commonSnmpPreail).getPmWithModelNumber(commonjson);

							if (json == null) {				
								continue;
							}
							json.put("common", commonjson);
							
							
							String jsonstr = json.toJSONString();
							
							//emtrans param json string
						/*	String ss="{\"dctable\":[{\"otxDCPowerVoltage_row\":\"23.5V\",\"otxDCPowerName_row\":\"+24V DC Power\"},{\"otxDCPowerVoltage_row\":\"11.9V\",\"otxDCPowerName_row\":\"+12V DC Power\"},{\"otxDCPowerVoltage_row\":\"-11.4V\",\"otxDCPowerName_row\":\"-12V DC Power\"},{\"otxDCPowerVoltage_row\":\"5.0V\",\"otxDCPowerName_row\":\"2b:35:56:20:44:43:20:50:6f:77:65:72:00\"},{\"otxDCPowerVoltage_row\":\"-4.9V\",\"otxDCPowerName_row\":\"2d:35:56:20:44:43:20:50:6f:77:65:72:00\"}],\"dctablerownum\":5,\"otxLaserOutputPower06\":\"1\",\"icon\":\"images\\/transEM.png\",\"otxLaserTecCurrent06\":\"1\",\"otxDCPowerVoltage36\":\"1\",\"otxDCPowerVoltage26\":\"1\",\"outtablerownum\":1,\"otxDCPowerVoltage46\":\"1\",\"otxLaserCurrent06\":\"1\",\"devtype\":\"TransEM\",\"otxDCPowerVoltage16\":\"1\",\"otxDCPowerVoltage06\":\"1\",\"otxInputRFLevel06\":\"5\",\"intable\":[{\"otxConfigurationAGCMode_row\":\"2\",\"otxConfigurationOmi_row\":\"0.0dB\",\"otxConfigurationChannelDistance_row\":\"8.0MHz\",\"otxInputRFLevel_row\":\"58.2dBuV\",\"otxConfigurationSbsSuppression_row\":\"14.0dBm\",\"otxConfigurationRfGain_row\":\"5.5dB\"}],\"cmd\":\"realtime-device\",\"outtable\":[{\"otxModuleIndex_row\":\"1\",\"otxLaserCurrent_row\":\"217.0mA\",\"otxConfigurationItuFrequency_row\":\"193500.0GHz\",\"otxLaserTecCurrent_row\":\"-260.0mA\",\"otxLaserControl_row\":\"1\",\"otxLaserOutputPower_row\":\"8.0mW\"}],\"intablerownum\":1,\"devtype\":\"emtrans\"}";
							Object obj=JSONValue.parse(ss);
							JSONObject array=(JSONObject)obj;
							jsonstr=array.toJSONString();*/
							System.out.println("参数发送"+jsonstr);
							for (Iterator it2 = osp.sessionList.iterator(); it2.hasNext();) {
								staticmemory.sendRemoteStr(jsonstr, it2.next().toString());
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						// log.info("......[x1]Realtime_param_call Done!" + e.getMessage());

					}
				}
			}
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		// log.error("Realtime_param_call STOP WORK");
	}
}
