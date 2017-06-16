package wl.hfc.topd;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;












import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.CDatabaseEngine;
import wl.hfc.common.CDevForCMD;
import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;
import wl.hfc.online.PDUServer;
import wl.hfc.online.PDUServerForOneDev;
import wl.hfc.online.ReceiverSnmpPrevail;
import wl.hfc.online.pmls;
import wl.hfc.traprcss.TrapPduServer;
import wl.hfc.traprcss.TrapProCenter;


public class Onlinetest {
/*	public static void searchTest() {

		PDUServer sver = new PDUServer(0);

		DeviceSearchEngine.searcher = sver;

		DeviceSearchEngine.SearchAgentByIpAddressAnyc("192.168.1.197", "public", 1);

		while (true) {
			try {
				Thread.sleep(1000);
				System.out.println("123123123");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}*/

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
			//ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnOpticalReceiverPower",rootjson);
			//ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnDCPowerVoltage",rootjson,0);
			ReceiverSnmpPrevail.me.getSubVarsBYparamname("fnRFPortOutputRFLevel",rootjson,0);


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

	public static void trapTest() {
		
		
		
		CDatabaseEngine	ICDatabaseEngine1=new CDatabaseEngine();
		ICDatabaseEngine1.getConnection();
		
		Date dt=new Date();
	    Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DATE,-1);
        Date dt2=rightNow.getTime();
		ICDatabaseEngine1.getTrapRowsWithTime(dt2, dt, "");
		
		CurrentAlarmModel CurrentAlarmModel1=new CurrentAlarmModel(ICDatabaseEngine1);
		String nowpath; // ��ǰtomcat��binĿ¼��·��
		nowpath = System.getProperty("user.dir");
		nowpath = nowpath + "\\" + "mibs";

		TrapProCenter trpcss = new TrapProCenter(true, nowpath);
		TrapPduServer.trpcss = trpcss;
		TrapPduServer.realTrapResponder=CurrentAlarmModel1;
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
		
		
		
		//new PDUServer(md1DevGrpModel.listDevHash);
		
		VariableSnmpVar.AlarmSatOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.3";
		VariableSnmpVar.AlarmEnOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.2";
		VariableSnmpVar.analogAlarmDeadband = ".1.3.6.1.4.1.17409.1.1.1.1.8";
		VariableSnmpVar.analogAlarmHIHI = ".1.3.6.1.4.1.17409.1.1.1.1.4";
		VariableSnmpVar.analogAlarmHI = ".1.3.6.1.4.1.17409.1.1.1.1.5";
		VariableSnmpVar.analogAlarmLo = ".1.3.6.1.4.1.17409.1.1.1.1.6";
		VariableSnmpVar.analogAlarmLoLo = ".1.3.6.1.4.1.17409.1.1.1.1.7";
		
		 //searchTest();
		// loadDXml();
		new pmls();

		try {

          trapTest();
		//  RECEIVERTEST();
		//	CommonVariablesGetTest();

			// ReceiverSnmpPrevail.me.getSubVarsWithTagInfoBYparamname(
			// "hfc_ingonglv", json);

			// ReceiverSnmpPrevail.me.getSubvarsTableWithTagInfo(ReceiverSnmpPrevail.me.cInputVariables[1],
			// 1);
			//
			// ReceiverSnmpPrevail.me.ThreadTablePramVarToJasonWithParamString(ReceiverSnmpPrevail.me.cInputVariables[1],
			// json,1);


		
			// ReceiverSnmpPrevail.me.setSubVarsWithTagInfoBYparamnameFromJson(
			// "hfc_ingonglv", json);

			// WosParamForSetInfo wosParamForSetInfo1 = new
			// WosParamForSetInfo();
			// int i = 0;
			// wosParamForSetInfo1.pmSetList[i++] = 100;
			// wosParamForSetInfo1.pmSetList[i++] = 90;
			// wosParamForSetInfo1.pmSetList[i++] = -30;
			// wosParamForSetInfo1.pmSetList[i++] = -90;
			// wosParamForSetInfo1.pmSetList[i++] = 20;
			// ArrayList<VariableBinding> lists = SnmpEngine
			// .cutMajorVaribaleWithThold(wosParamForSetInfo1,
			// snmpInstance7.mjVariables[1]);
			// snmpInstance7.setParam(lists);

			// System.out.println(json.toString());
			// VariableSnmpVar[]
			// mjVariables=snmpInstance7.getPmWithModelNumber();
			// ReceiverSnmpPrevail.me.snmpVarToJason(mjVariables,json);
			//
			//
			//
			// SnmpTableInfo reTable= snmpInstance7.getPmWithModelNumberTf();
			// SnmpTableInfo reTable1= snmpInstance7.getPmWithModelNumberTs();
			// ReceiverSnmpPrevail.me.tabVarToJason(reTable,reTable1,json);

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

}
