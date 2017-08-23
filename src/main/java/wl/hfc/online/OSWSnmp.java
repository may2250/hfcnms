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

public class OSWSnmp extends WosBaseSnmp {

	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;

	// pdus
	private PDU majorVarPdu;

	
	// single model
	public static OSWSnmp me;

	private String pdeviceIDString = "";
	
	//private boolean isSwicher=false;

	public OSWSnmp(String phsicIndex, String PDeviceID) {
		super(phsicIndex);
		pdeviceIDString = PDeviceID;
		mjVariables = new VariableSnmpVar[6];


		majorVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);

		// major
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tabch.get("osInputOpticalPowerA");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tabch.get("osInputOpticalPowerB");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".0", ToValueMode.FmtInteger, true);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

	
		row1 = pmls.tabch.get("osSwitchReference");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		
		
		row1 = pmls.tabch.get("osWavelength");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1", ToValueMode.Default, false);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
		
		
		
		row1 = pmls.tabch.get("osAutoControl");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

		row1 = pmls.tabch.get("osCurrentWorkChannel");
		mjVariables[vIns] = new VariableSnmpVar(row1, ".1", ToValueMode.FmtInteger, false);
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
		
		

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

		return pJson;

	}





}
