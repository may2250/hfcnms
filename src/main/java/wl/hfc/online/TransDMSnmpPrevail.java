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

public class TransDMSnmpPrevail extends WosBaseSnmp {


	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] cDCVariables;
	public VariableSnmpVar[] cInputVariables;
	public VariableSnmpVar[] cOutputVariables;



	// pdus
	private PDU majorVarPdu;
	private PDU tableDCPdu;
	private PDU tableInputPdu;
	private PDU tableOutpdu;

	// single model
	public static TransDMSnmpPrevail me;

	
	private int tableOutPlength=7;
	private int tableInputPlength=2;
	private String pdeviceIDString="";

	public TransDMSnmpPrevail(String phsicIndex,String PDeviceID) {
		super(phsicIndex);
		pdeviceIDString=PDeviceID;
		mjVariables = new VariableSnmpVar[3];
		// tables
		cDCVariables = new VariableSnmpVar[2];
		cInputVariables = new VariableSnmpVar[5];
		cOutputVariables = new VariableSnmpVar[7];

		majorVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);


		// major
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tabch.get("otdConfigurationRFChannels");	
		
		mjVariables[vIns] = new VariableSnmpVar(row1);			
		mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));		
		


		
		

		tableDCPdu = new PDU();
		tableDCPdu.setType(PDU.GETNEXT);

		row1 = pmls.tabch.get("otdDCPowerName");
		cDCVariables[0] = new VariableSnmpVar(row1);
		cDCVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tabch.get("otdDCPowerVoltage");
		cDCVariables[1] = new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, cDCVariables[1]);


		int begincol = 0;
		int endcol = 1;

		VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol
				+ 1];
		int enumi;
		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cDCVariables[enumi
					+ begincol];
			tableDCPdu.add(new VariableBinding(
					headerinfos[enumi].MibDefinedOid));
		}

		
		//input table		
		tableInputPdu = new PDU();
		tableInputPdu.setType(PDU.GETNEXT);

		int i=0;


		row1 = pmls.tabch.get("otdInputRFLevel");//激励电平
		cInputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cInputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[i++]);
		
		
		row1 = pmls.me.tabch.get("otdAGCControl");
		cInputVariables[i] = new VariableSnmpVar(row1);
		cInputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[i++]);
		
		



		begincol = 0;
		endcol = this.tableInputPlength-1;

		headerinfos = new VariableSnmpVar[endcol - begincol + 1];

		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) cInputVariables[enumi
					+ begincol];
			this.tableInputPdu.add(new VariableBinding(
					headerinfos[enumi].MibDefinedOid));
		}

		
		
		
		
		
		//output table
		i=0;
		this.tableOutpdu = new PDU();
		tableOutpdu.setType(PDU.GETNEXT);

		row1 = pmls.tabch.get("otdIndex");//序列
		cOutputVariables[i] = new VariableSnmpVar(row1);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtString;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);

		row1 = pmls.tabch.get("otdLaserTemp");
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);

		row1 = pmls.tabch.get("otdLaserCurrent");//bias
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		
		
		

		row1 = pmls.tabch.get("otdOpicalOutputPower");
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		row1 = pmls.tabch.get("otdTecCurrent");
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		row1 = pmls.tabch.get("otdLaserWavelength");
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, false);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtString;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);		
		
		
		
		row1 = pmls.tabch.get("otdLaserContrlMode");
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, false);
		cOutputVariables[i].ToValueMode1 = ToValueMode.Default;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);	
		

		begincol = 0;
		endcol = this.tableOutPlength-1;

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

	/*	inPDU = sver.SyncSendSnmpPdu(this.majorVarPdu, cTgt);
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
		*/
		
		//table params
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
		
		
		

		//SnmpEngine.tabVarToJason(this.cDCVariables,reTable, pJson,"dctable");
		SnmpEngine.tabVarToJason(this.cOutputVariables,reTable1, pJson,"outtable");
		
		//table thread
		for (int j = 0; j < this.cDCVariables.length; j++) {

			if (this.cDCVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cDCVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cDCVariables[j], pJson, i, true);
				}
			}
		}		
/*		
		for (int j = 0; j < this.cOutputVariables.length; j++) {

			if (this.cOutputVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable1.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cOutputVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cOutputVariables[j], pJson, i, true);
				}

			}

		}
		*/
		

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

	
}
