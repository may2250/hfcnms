package wl.hfc.topd;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;

import com.xinlong.util.RedisUtil;

import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.*;
import wl.hfc.online.*;
import wl.hfc.traprcss.TrapPduServer;
import wl.hfc.traprcss.TrapProCenter;

public class Onlinetest {
	/*
	 * public static void searchTest() {
	 * 
	 * PDUServer sver = new PDUServer(0);
	 * 
	 * DeviceSearchEngine.searcher = sver;
	 * 
	 * DeviceSearchEngine.SearchAgentByIpAddressAnyc("192.168.1.197", "public",
	 * 1);
	 * 
	 * while (true) { try { Thread.sleep(1000); System.out.println("123123123");
	 * } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * }
	 */

	public static void RECEIVERTEST() {
		// loadDXml();
		new pmls();

		JSONObject json = new JSONObject();
		ReceiverSnmpPrevail snmpInstance7 = new ReceiverSnmpPrevail(".1");
		snmpInstance7.thisDev = new CDevForCMD("public", "public", "192.168.1.243");
		snmpInstance7.sver = new PDUServerForOneDev(0);
		try {
			System.out.println(json.toString());
			json = snmpInstance7.getPmWithModelNumber(json);

			JSONObject rootjson = new JSONObject();
			// ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnOpticalReceiverPower",rootjson);
			// ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnDCPowerVoltage",rootjson,0);
			ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnRFPortOutputRFLevel", rootjson, 0);

			System.out.println(json.toString());
			System.out.println(rootjson.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return;

		}

		while (true) {
			try {
				Thread.sleep(1000);
				System.out.println("123123123");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public static void EDFATEST() {
		// loadDXml();
		new pmls();

		JSONObject json = new JSONObject();
		EDFASnmpPrevail snmpInstance7 = new EDFASnmpPrevail(".1");
		snmpInstance7.thisDev = new CDevForCMD("public", "public", "192.168.1.170");
		snmpInstance7.sver = new PDUServerForOneDev(0);
		try {
			System.out.println(json.toString());
			json = snmpInstance7.getPmWithModelNumber(json);

			JSONObject rootjson = new JSONObject();
			// ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnOpticalReceiverPower",rootjson);
			// ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnDCPowerVoltage",rootjson,0);
			ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnRFPortOutputRFLevel", rootjson, 0);

			System.out.println(json.toString());
			System.out.println(rootjson.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return;

		}

		while (true) {
			try {
				Thread.sleep(1000);
				System.out.println("123123123");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void CommonVariablesGetTest() {

		try {
			// loadDXml();
			new pmls();
			JSONObject json = new JSONObject();
			CommonSnmpPrevail snmpInstance7 = new CommonSnmpPrevail(".0");
			snmpInstance7.thisDev = new CDevForCMD("public", "public", "192.168.1.243");
			snmpInstance7.sver = new PDUServerForOneDev(0);

			// get
			snmpInstance7.getPmWithModelNumber(json);

			snmpInstance7.setStringVars("commonAgentTrapIP", "123123", 1);
			// to view
			// JSONObject json = new JSONObject();
			// SnmpEngine.snmpVarToJason(mjVariables, json);
			System.out.println(json.toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void trapTest() {

		RedisUtil redisUtil = new RedisUtil();
		CDatabaseEngine ICDatabaseEngine1 = new CDatabaseEngine(redisUtil);
		// ICDatabaseEngine1.getConnection();

		Date dt = new Date();
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(dt);
		rightNow.add(Calendar.DATE, -1);
		Date dt2 = rightNow.getTime();
		ICDatabaseEngine1.getTrapRowsWithTime(dt2, dt, "");
		CurrentAlarmModel CurrentAlarmModel1 = new CurrentAlarmModel();
		CurrentAlarmModel1.setRedisUtil(redisUtil);
		CurrentAlarmModel1.logEngine = ICDatabaseEngine1;
		String nowpath; // ��ǰtomcat��binĿ¼��·��
		nowpath = System.getProperty("user.dir");
		nowpath = nowpath + "\\" + "mibs";

		TrapProCenter trpcss = new TrapProCenter(true, nowpath);
		TrapPduServer.trpcss = trpcss;
		TrapPduServer.realTrapResponder = CurrentAlarmModel1;
		new TrapPduServer();

		while (true) {
			try {
				Thread.sleep(1000);
				System.out.println("123123123");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]) {

		// new PDUServer(md1DevGrpModel.listDevHash);

		VariableSnmpVar.AlarmSatOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.3";
		VariableSnmpVar.AlarmEnOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.2";
		VariableSnmpVar.analogAlarmDeadband = ".1.3.6.1.4.1.17409.1.1.1.1.8";
		VariableSnmpVar.analogAlarmHIHI = ".1.3.6.1.4.1.17409.1.1.1.1.4";
		VariableSnmpVar.analogAlarmHI = ".1.3.6.1.4.1.17409.1.1.1.1.5";
		VariableSnmpVar.analogAlarmLo = ".1.3.6.1.4.1.17409.1.1.1.1.6";
		VariableSnmpVar.analogAlarmLoLo = ".1.3.6.1.4.1.17409.1.1.1.1.7";

		// searchTest();
		// loadDXml();
		new pmls();

/*		Float xxx = Float.valueOf("11.2");
		Float yy = 0.1f;
		Float rst = xxx / yy;
*/
	//	int bbb = rst.intValue();
		try {

			// trapTest();
			//RECEIVERTEST();
			//CommonVariablesGetTest();
			EDFATEST();

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

}
