package wl.hfc.online;

import org.json.simple.JSONArray;
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

public class CommonSnmpPrevail extends WosBaseSnmp {


	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] trapAddrsVariables;
	// pdus
	private PDU majorVarPdu;
	private PDU trapAddrPdu;
	
	//private SnmpTableInfo trapTableInfo;

	// single model
	public static CommonSnmpPrevail me;



	public CommonSnmpPrevail(String phsicIndex) {
		super(phsicIndex);
		mjVariables = new VariableSnmpVar[11];
		trapAddrsVariables = new VariableSnmpVar[2];

		majorVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);
		

	

		// major
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tab1.get("sysName");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tab1.get("sysObjectID");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));


		row1 = pmls.tab1.get("sysDescr");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		row1 = pmls.tab1.get("sysContact");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		row1 = pmls.tab1.get("sysLocation");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		
		row1 = pmls.tab1.get("sysUpTime");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		
		
		
		//hfc ����		
		row1 = pmls.tab1.get("commonInternalTemperature");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtInteger, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tab1.get("commonNELogicalID");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		row1 = pmls.tab1.get("commonNEModelNumber");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		
		row1 = pmls.tab1.get("commonNESerialNumber");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));
		
		
		
		row1 = pmls.tab1.get("commonDeviceMACAddress");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtString, false);
		this.majorVarPdu.add(new VariableBinding(
				mjVariables[vIns++].FullSnmpOid));

		
		
		//trap addr info
		trapAddrPdu = new PDU();
		trapAddrPdu.setType(PDU.GETNEXT);

		row1 = pmls.tab1.get("commonAgentTrapIndex");
		trapAddrsVariables[0] = new VariableSnmpVar(row1);
		trapAddrsVariables[0].ToValueMode1 = ToValueMode.FmtString;

		row1 = pmls.tab1.get("commonAgentTrapIP");
		trapAddrsVariables[1] = new VariableSnmpVar(row1);
		trapAddrsVariables[1].ToValueMode1 = ToValueMode.FmtString;
		paramHashTable.put(row1.ParamMibLabel, trapAddrsVariables[1]);		
		
		int begincol = 0;
		int endcol = 1;

		VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol + 1];
		int enumi;
		for (enumi = 0; enumi < headerinfos.length; enumi++) {
			headerinfos[enumi] = (VariableSnmpVar) trapAddrsVariables[enumi + begincol];
			trapAddrPdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
		}
		// majorVarPdu=tablepdu;
		me = this;

	}
	private JSONObject tabVarToJason(SnmpTableInfo tabVariables, JSONObject pJson) {
		int enumi, enumj;
		int i = 0;
		JSONArray al = new JSONArray();
		JSONObject itemjson;
		for (enumi = 0; enumi < tabVariables.RowNum; enumi++) {
			itemjson = new JSONObject();
			for (enumj = 0; enumj < tabVariables.ColNum; enumj++) {				
				String vale = trapAddrsVariables[enumj].ToDispString(tabVariables.TableCells.get(i));
				itemjson.put(trapAddrsVariables[enumj].VarInfo.ParamMibLabel + "_row", vale);
				i++;				
			}		
			al.add(itemjson);
		}
		pJson.put("traptbl", al);
		return pJson;

	}
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
		
		
		
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables((PDU) this.trapAddrPdu.clone(), cTgt, sver);
		pJson.put("trapTablerownum", reTable.RowNum);	
		this.tabVarToJason(reTable, pJson);
		
		return pJson;

	}
	public JSONObject getPmTrap(JSONObject pJson) throws Exception {
		CommunityTarget cTgt;
	//	SnmpEngine.snmpVarToJason(getPmWithModelNumber(), pJson);
		
		// table
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables((PDU) this.trapAddrPdu.clone(), cTgt, sver);
		pJson.put("trapTablerownum", reTable.RowNum);	
		this.tabVarToJason(reTable, pJson);
		

		return pJson;
	}
}
