package wl.hfc.online;




import org.json.simple.JSONObject;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;
















import java.util.ArrayList;

import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;

import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;
import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.common.VariableSnmpVar.ToValueMode;
import wl.hfc.online.pmls;

public class EDFASnmpPrevail extends WosBaseSnmp {
	// 0����snmpv1��1����snmpv2

	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] commonVariables;
	public VariableSnmpVar[] cInputVariables;
	public VariableSnmpVar[] cOutputVariables;



	// pdus
	private PDU majorVarPdu;
	private PDU CommonVarPdu;
	private PDU tableDCPdu;
	private PDU tableOutpdu;

	// single model
	public static EDFASnmpPrevail me;


	public EDFASnmpPrevail(String phsicIndex,String PDeviceID) {
		super(phsicIndex);
		mjVariables = new VariableSnmpVar[2];
		// tables
		cInputVariables = new VariableSnmpVar[2];
		cOutputVariables = new VariableSnmpVar[4];
		commonVariables = new VariableSnmpVar[4];
		majorVarPdu = new PDU();
		CommonVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);
		CommonVarPdu.setType(PDU.GET);

		// major
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tab1.get("oaInputOpticalPower");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		row1 = pmls.tab1.get("oaOutputOpticalPower");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));

		
		
		

		tableDCPdu = new PDU();
		tableDCPdu.setType(PDU.GETNEXT);

		row1 = pmls.tab1.get("oaDCPowerName");
		cInputVariables[0] = new VariableSnmpVar(row1);
		cInputVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tab1.get("oaDCPowerVoltage");
		cInputVariables[1] = new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[1]);


		int begincol = 0;
		int endcol = 1;

		VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol
				+ 1];
		int enumi;
		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cInputVariables[enumi
					+ begincol];
			tableDCPdu.add(new VariableBinding(
					headerinfos[enumi].MibDefinedOid));
		}

		this.tableOutpdu = new PDU();
		tableOutpdu.setType(PDU.GETNEXT);

		row1 = pmls.tab1.get("oaPumpIndex");
		cOutputVariables[0] = new VariableSnmpVar(row1);
		cOutputVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tab1.get("oaPumpBIAS");
		cOutputVariables[1] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[1].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[1]);


		row1 = pmls.tab1.get("oaPumpTEC");
		cOutputVariables[2] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[2].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[2]);

		row1 = pmls.tab1.get("oaPumpTemp");
		cOutputVariables[3] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[3].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[3]);

		begincol = 0;
		endcol = 3;

		headerinfos = new VariableSnmpVar[endcol - begincol + 1];

		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cOutputVariables[enumi
					+ begincol];
			tableOutpdu.add(new VariableBinding(
					headerinfos[enumi].MibDefinedOid));
		}


		me = this;

	}

	@Override 
	public JSONObject getPmWithModelNumber(JSONObject pJson) throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);

		inPDU = sver.SyncSendSnmpPdu(this.majorVarPdu, cTgt);
		if (inPDU == null) {
			throw new Exception("paramGetException,Failed!");
		}
		SnmpEngine.ParseBasicVars(this.mjVariables, inPDU);
		SnmpEngine.snmpVarToJason(mjVariables, pJson);
		
		for (int i = 0; i < this.mjVariables.length; i++) {
			getSubVarsWithTagInfo(this.mjVariables[i]);

		}		
		
		for (int i = 0; i < mjVariables.length; i++) {
			if (this.mjVariables[i].withNoThreashold) {
				SnmpEngine.ThreadPramVarToJason(mjVariables[i], pJson, true);
			}

		}
		
		
		

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableDCPdu.clone(), cTgt, sver);
		pJson.put("dctablerownum", reTable.RowNum);
		
		
		

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable1 = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableOutpdu.clone(), cTgt, sver);

		pJson.put("outtablerownum", reTable1.RowNum);
		
		
		SnmpEngine.tabVarToJason(this.cInputVariables,this.cOutputVariables,reTable, reTable1, pJson);

		
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
		return pJson;

	}

	public SnmpTableInfo getPmWithModelNumberTf() throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo result = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableDCPdu.clone(), cTgt, sver);
		

	
		return result;

	}

	public SnmpTableInfo getPmWithModelNumberTs() throws Exception {
		PDU outPDU;
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo result = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableOutpdu.clone(), cTgt, sver);

		return result;

	}

	


	public void getSubVarsWithTagInfoBYparamname(String paramname,
			JSONObject jsobj) {
		if (paramname.equalsIgnoreCase("hfc_ingonglv")) {

			getSubVarsWithTagInfo(this.mjVariables[1]);

			SnmpEngine.ThreadPramVarToJason(this.mjVariables[1], jsobj);

		} else if (paramname.equalsIgnoreCase("hfc_powerv2")
				|| paramname.equalsIgnoreCase("hfc_powerv1")) {

			String rowString = paramname.substring(paramname.length() - 1,
					paramname.length());

			int row = Integer.parseInt(rowString);

		//	this.getSubvarsTableWithTagInfo(this.cInputVariables[1], row);

			//SnmpEngine.ThreadPramVarToJason(this.cInputVariables[1], jsobj);

		}
	}

	private void setSubVarsWithTagInfoBYparamname(String paramname,
			WosParamForSetInfo wosParamForSetInfo1) {

		if (paramname.equalsIgnoreCase("hfc_ingonglv")) {

			ArrayList<VariableBinding> lists = SnmpEngine
					.cutMajorVaribaleWithThold(wosParamForSetInfo1,
							this.mjVariables[1]);

			this.setParam(lists);

		} else if (paramname.equalsIgnoreCase("hfc_powerv2")
				|| paramname.equalsIgnoreCase("hfc_powerv1")) {

			String rowString = paramname.substring(paramname.length() - 1,
					paramname.length());

			int row = Integer.parseInt(rowString);

			ArrayList<VariableBinding> lists = SnmpEngine
					.cutTableVaribaleWithThold(wosParamForSetInfo1,	this.cInputVariables[1],row);

			this.setParam(lists);

		}

	}

	public void setSubVarsWithTagInfoBYparamnameFromJson(String paramname,
			JSONObject jsondata) {
		//
		 String hihi = jsondata.get("hihi").toString();
		 String hi = jsondata.get("hi").toString();
		 String lo = jsondata.get("lo").toString();
		 String lolo = jsondata.get("lolo").toString();
		 String deadb = jsondata.get("deadb").toString();
		
		 byte en =Byte.class.cast(jsondata.get("en"));
		

//		String hihi = "9.0";
//		String hi = "9.0";
//		String lo = "-1.0";
//		String lolo = "-7.2";
//		String deadb = "-1.0";
		WosParamForSetInfo wosParamForSetInfo1 = new WosParamForSetInfo();
		int i = 0;
		try {
			Float rest = Float.valueOf(hihi)
					/ this.mjVariables[1].VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(hi) / this.mjVariables[1].VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lo) / this.mjVariables[1].VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lolo) / this.mjVariables[1].VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(deadb)/ this.mjVariables[1].VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			// wosParamForSetInfo1.sByte=Byte.parseByte(en);
			wosParamForSetInfo1.sByte=en;
			setSubVarsWithTagInfoBYparamname(paramname, wosParamForSetInfo1);

		} catch (Exception e) {

			e.printStackTrace();
		}


	}
}
