package wl.hfc.online;

import org.json.simple.JSONObject;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;

import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;
import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.common.VariableSnmpVar.ToValueMode;
import wl.hfc.online.pmls;

public class EDFASnmpPrevail extends WosBaseSnmp {
	private static String heOpRxUnitSwitchMode = ".1.3.6.1.4.1.17409.1.11.9";
	private static String heOpRxUnitCurChan = ".1.3.6.1.4.1.17409.1.11.8";
	private static String heOpRxUnitoOpticalThreshold = ".1.3.6.1.4.1.17409.1.11.10";
	private static String heOpRxUnitoOpticalInputPower = ".1.3.6.1.4.1.17409.1.11.11";
	private static String heOpRxUnitoOpticalInputPowerAlarmBchanel = ".1.3.6.1.4.1.17409.1.11.4.1.5.1";

	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] cInputVariables;
	public VariableSnmpVar[] cOutputVariables;

	public VariableSnmpVar[] swichVariables;

	// pdus
	private PDU majorVarPdu;
	private PDU tableDCPdu;
	private PDU tableOutpdu;
	private PDU switchVarPdu;
	// single model
	public static EDFASnmpPrevail me;

	private String pdeviceIDString = "";
	
	//private boolean isSwicher=false;

	public EDFASnmpPrevail(String phsicIndex, String PDeviceID) {
		super(phsicIndex);
		pdeviceIDString = PDeviceID;
		mjVariables = new VariableSnmpVar[3];
		// tables
		cInputVariables = new VariableSnmpVar[2];
		cOutputVariables = new VariableSnmpVar[4];

		swichVariables = new VariableSnmpVar[4];

		majorVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);
		switchVarPdu = new PDU();
		switchVarPdu.setType(PDU.GET);
		// major
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tabch.get("oaInputOpticalPower");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tabch.get("oaOutputOpticalPower");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tabch.get("oaOptAtt");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		// switch params
		if (PDeviceID.equalsIgnoreCase("WE-YZ-SWITCH")) {
			heOpRxUnitoOpticalInputPower = ".1.3.6.1.4.1.17409.1.11.7";
			heOpRxUnitoOpticalInputPowerAlarmBchanel = ".1.3.6.1.4.1.17409.1.11.7";
		
		}

		vIns = 0;
		row1 = new nojuParmsTableRow("heOpRxUnitSwitchMode", heOpRxUnitSwitchMode, "heOpRxUnitSwitchMode", true,
				(float) 1, "F0", "");
		swichVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, swichVariables[vIns]);
		this.switchVarPdu.add(new VariableBinding(swichVariables[vIns++].FullSnmpOid));

		row1 = new nojuParmsTableRow("heOpRxUnitCurChan", heOpRxUnitCurChan, "heOpRxUnitCurChan", true, (float) 1, "F0",
				"");
		swichVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, swichVariables[vIns]);
		this.switchVarPdu.add(new VariableBinding(swichVariables[vIns++].FullSnmpOid));

		row1 = new nojuParmsTableRow("heOpRxUnitoOpticalThreshold", heOpRxUnitoOpticalThreshold,
				"heOpRxUnitoOpticalThreshold", true, (float) 0.1, "F1", "dBm");
		swichVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, swichVariables[vIns]);
		this.switchVarPdu.add(new VariableBinding(swichVariables[vIns++].FullSnmpOid));

		// B通道输入光功率
		row1 = new nojuParmsTableRow("heOpRxUnitoOpticalInputPower", heOpRxUnitoOpticalInputPower,
				"heOpRxUnitoOpticalInputPower", true, (float) 0.1, "F", "dBm");
		swichVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, swichVariables[vIns]);
		this.switchVarPdu.add(new VariableBinding(swichVariables[vIns++].FullSnmpOid));

		
		
		
		
		// dc table
		tableDCPdu = new PDU();
		tableDCPdu.setType(PDU.GETNEXT);

		row1 = pmls.tabch.get("oaDCPowerName");
		cInputVariables[0] = new VariableSnmpVar(row1);
		cInputVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tabch.get("oaDCPowerVoltage");
		cInputVariables[1] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[1]);

		int begincol = 0;
		int endcol = 1;

		VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol + 1];
		int enumi;
		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cInputVariables[enumi + begincol];
			tableDCPdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
		}

		this.tableOutpdu = new PDU();
		tableOutpdu.setType(PDU.GETNEXT);

		row1 = pmls.tabch.get("oaPumpIndex");
		cOutputVariables[0] = new VariableSnmpVar(row1);
		cOutputVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tabch.get("oaPumpBIAS");
		cOutputVariables[1] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, true);
		cOutputVariables[1].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[1]);

		row1 = pmls.tabch.get("oaPumpTEC");
		cOutputVariables[2] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, true);
		cOutputVariables[2].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[2]);

		row1 = pmls.tabch.get("oaPumpTemp");
		cOutputVariables[3] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, true);
		cOutputVariables[3].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[3]);

		begincol = 0;
		endcol = 3;

		headerinfos = new VariableSnmpVar[endcol - begincol + 1];

		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cOutputVariables[enumi + begincol];
			tableOutpdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
		}

		me = this;

	}

	@Override
	public JSONObject getPmWithModelNumber(JSONObject pJson) throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);

		inPDU = sver.SyncSendSnmpPdu(this.majorVarPdu, cTgt);
		if (inPDU == null) {
			throw new Exception("paramGetException,Failed!");
		}
		SnmpEngine.ParseBasicVars(this.mjVariables, inPDU);
		SnmpEngine.snmpVarToJason(mjVariables, pJson);

		for (int i = 0; i < this.mjVariables.length; i++) {
			if (this.mjVariables[i].withNoThreashold) {
				getSubVarsWithTagInfo(this.mjVariables[i]);
			}

		}

		for (int i = 0; i < mjVariables.length; i++) {
			if (this.mjVariables[i].withNoThreashold) {
				SnmpEngine.ThreadPramVarToJason(mjVariables[i], pJson, true);
			}

		}

		// table params
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables((PDU) this.tableDCPdu.clone(), cTgt, sver);
		pJson.put("dctablerownum", reTable.RowNum);

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable1 = SnmpEngine.GetMibTableVariables((PDU) this.tableOutpdu.clone(), cTgt, sver);
		pJson.put("outtablerownum", reTable1.RowNum);

		SnmpEngine.tabVarToJason(this.cInputVariables, this.cOutputVariables, reTable, reTable1, pJson);

		// table thread
		for (int j = 0; j < this.cInputVariables.length; j++) {

			if (this.cInputVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cInputVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cInputVariables[j], pJson, i, true);
				}
			}
		}

		for (int j = 0; j < this.cOutputVariables.length; j++) {

			if (this.cOutputVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable1.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cOutputVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cOutputVariables[j], pJson, i, true);
				}

			}

		}

		if (this.thisDev.DEVICEID.equalsIgnoreCase("WE-HD-SWITCH")
				|| thisDev.DEVICEID.equalsIgnoreCase("WE-YZ-SWITCH")) {
			cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);

			inPDU = sver.SyncSendSnmpPdu(this.switchVarPdu, cTgt);
			if (inPDU == null) {
				throw new Exception("paramGetException,Failed!");
			}
			SnmpEngine.ParseBasicVars(this.swichVariables, inPDU);
			SnmpEngine.snmpVarToJason(swichVariables, pJson);

			for (int i = 0; i < this.swichVariables.length; i++) {
				if (this.swichVariables[i].withNoThreashold) {
					getSubVarsWithTagInfo(this.swichVariables[i]);
				}

			}

			for (int i = 0; i < swichVariables.length; i++) {
				if (this.swichVariables[i].withNoThreashold) {
					SnmpEngine.ThreadPramVarToJason(mjVariables[i], pJson, true);
				}

			}

		}

		// append view
		if (this.thisDev.MD.contains("HB") || thisDev.MD.contains("HS") || thisDev.MD.contains("YZ")
				|| thisDev.MD.contains("HD") || thisDev.DEVICEID.contains("YZ") || thisDev.DEVICEID.contains("HD")) {
			pJson.put("ViewATT", 1);

		} else {
			pJson.put("ViewATT", 0);
		}

		if (this.thisDev.DEVICEID.equalsIgnoreCase("WE-HD-SWITCH")
				|| thisDev.DEVICEID.equalsIgnoreCase("WE-YZ-SWITCH")) {
			pJson.put("ViewSwtich", 1);// append switch params

		} else {
			pJson.put("ViewSwtich", 0);

		}

		return pJson;

	}

	public SnmpTableInfo getPmWithModelNumberTf() throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo result = SnmpEngine.GetMibTableVariables((PDU) this.tableDCPdu.clone(), cTgt, sver);

		return result;

	}

	public SnmpTableInfo getPmWithModelNumberTs() throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo result = SnmpEngine.GetMibTableVariables((PDU) this.tableOutpdu.clone(), cTgt, sver);

		return result;

	}

}
