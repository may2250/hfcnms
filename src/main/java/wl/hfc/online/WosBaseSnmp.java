package wl.hfc.online;

import org.json.simple.JSONObject;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.*;

import wl.hfc.common.*;

import java.util.ArrayList;
import java.util.HashMap;




import java.util.Iterator;

public class WosBaseSnmp {

	public static String entPhysicalSerialNum = ".1.3.6.1.2.1.47.1.1.1.1.11";
	public int snmpVersion;
	public int slotIndex;
	public boolean isAdmin;
	public String entPhysicalDescrIndex;
	public String physicalIndex1;
	public String physicalIndex2;

	public HashMap<String, VariableSnmpVar> paramHashTable;

	public IPduSevr sver;
	public CDevForCMD thisDev;
	public Object getMJPmLocker = new Object();
	public Object getTBLocker = new Object();

	public WosBaseSnmp(String pPhysicalIndex) {
		this.physicalIndex1 = pPhysicalIndex;

		this.physicalIndex2 = ".1";
		snmpVersion = 0;
		paramHashTable = new HashMap();

	}

	private boolean _isInParamSetting = false;
	public boolean isInParamSetting;

	public void setParam(ArrayList<VariableBinding> vblist) {
		_isInParamSetting = true;

		if (vblist.size() == 0) {
			return;
		}
		PDU SetVarPdu = new PDU();
		SetVarPdu.setType(PDU.SET);
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.RWCommunity, SnmpConstants.version1);

		Iterator<VariableBinding> it = vblist.iterator();
		while (it.hasNext()) {
			SetVarPdu.add(it.next());
		}

		sver.SyncSendSnmpPdu(SetVarPdu, cTgt);

	}

	public void setSingleValue(VariableBinding bindValue) {

		PDU SetVarPdu = new PDU();
		SetVarPdu.setType(PDU.SET);
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SetVarPdu.add(bindValue);
		sver.SyncSendSnmpPdu(SetVarPdu, cTgt);

	}

	public void getSubVarsWithTagInfo(VariableSnmpVar tmpTagInfo) {
		PDU outPDU;
		PDU inPDU;

		try {

			CommunityTarget cTgt;

			cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
			outPDU = SnmpEngine.createThredParamPDU(tmpTagInfo);

			inPDU = sver.SyncSendSnmpPdu(outPDU, cTgt);

			if (inPDU == null) {
				throw new Exception("paramGetException,Failed!");
			}

			// prase pdu to cache logic
			SnmpEngine.ParseBasicVars(tmpTagInfo.subVariableSnmpVarS, inPDU);

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public void getSubVarsWithTagInfo(VariableSnmpVar tmpTagInfo, int rowNumber) {
		PDU outPDU;
		PDU inPDU;

		try {

			CommunityTarget cTgt;

			cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
			outPDU = SnmpEngine.createTableThredParamPDU(tmpTagInfo, rowNumber + 1);

			inPDU = sver.SyncSendSnmpPdu(outPDU, cTgt);

			if (inPDU == null) {
				throw new Exception("paramGetException,Failed!");
			}

			// prase pdu to cache logic
			SnmpEngine.ParseBasicVars(tmpTagInfo.subTableVariableSnmpVarSS.get(rowNumber), inPDU);

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public void getSubVarsBYparamname(String paramname, JSONObject jsobj) {

	  SnmpEngine.ThreadPramVarToJason(paramHashTable.get(paramname), jsobj);

	}

	public void getSubVarsBYparamname(String paramname, JSONObject jsobj, int rowNumber) {

		SnmpEngine.ThreadPramVarToJason(paramHashTable.get(paramname), jsobj, rowNumber);

	}

	public void setSubVarsBYparamname(String paramname, JSONObject jsondata) {

		VariableSnmpVar tmpTagInfo = paramHashTable.get(paramname);
		//
		String hihi = jsondata.get("hihi").toString();
		String hi = jsondata.get("hi").toString();
		String lo = jsondata.get("lo").toString();
		String lolo = jsondata.get("lolo").toString();
		String deadb = jsondata.get("deadb").toString();

		byte en = Byte.class.cast(jsondata.get("en"));

		// String hihi = "9.0";
		// String hi = "9.0";
		// String lo = "-1.0";
		// String lolo = "-7.2";
		// String deadb = "-1.0";		
		WosParamForSetInfo wosParamForSetInfo1 = new WosParamForSetInfo();
		int i = 0;
		try {
			Float rest = Float.valueOf(hihi) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(hi) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lo) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lolo) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(deadb) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			// wosParamForSetInfo1.sByte=Byte.parseByte(en);
			wosParamForSetInfo1.sByte = en;
			ArrayList<VariableBinding> lists = SnmpEngine.cutMajorVaribaleWithThold(wosParamForSetInfo1, paramHashTable.get(paramname));

			this.setParam(lists);

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	// get sigle param variable
	public void setVars(String paramname, String pValue) {
		VariableSnmpVar tmpTagInfo = paramHashTable.get(paramname);
		WosParamForSetInfo wosParamForSetInfo1 = new WosParamForSetInfo();
		Float rest = Float.valueOf(pValue) / tmpTagInfo.VarInfo.FormatCoff;
		wosParamForSetInfo1.pmSetList[0] = rest.intValue();
		ArrayList<VariableBinding> lists = SnmpEngine.cutMajorVaribaleSingle(wosParamForSetInfo1,tmpTagInfo);

		this.setParam(lists);

	}

	public void setTableVars(String paramname, String pValue, int row) {
		VariableSnmpVar tmpTagInfo = paramHashTable.get(paramname);
		WosParamForSetInfo wosParamForSetInfo1 = new WosParamForSetInfo();
		Float rest = Float.valueOf(pValue) / tmpTagInfo.VarInfo.FormatCoff;
		wosParamForSetInfo1.pmSetList[0] = rest.intValue();
		
		if(tmpTagInfo.isformatter)
		{
			  if (wosParamForSetInfo1.pmSetList[0] < tmpTagInfo.minValue || wosParamForSetInfo1.pmSetList[0] > tmpTagInfo.maxValue)
                 return;
			  
              if (tmpTagInfo.setpvalue != 0)
			  {
                  if (wosParamForSetInfo1.pmSetList[0] % tmpTagInfo.setpvalue != 0)
                      return;
			  
			  }
      
			
		}

		ArrayList<VariableBinding> lists = SnmpEngine.cutTableVaribaleSingle(wosParamForSetInfo1, tmpTagInfo, row);

		this.setParam(lists);

	}
	

	public void setStringVars(String paramname, String pValue, int row) {
		
		
		VariableSnmpVar tmpTagInfo = paramHashTable.get(paramname);

		String oidrs;
		ArrayList<VariableBinding> result = new ArrayList<VariableBinding>();

		oidrs = tmpTagInfo.VarInfo.ParamMibOID.toString() + "." + (row+1);
		result.add(new VariableBinding(new OID(oidrs), new org.snmp4j.smi.IpAddress(pValue)));
		this.setParam(result);

		


	}


	public void setSubVarsTableBYparamname(String paramname, JSONObject jsondata, int rowNumber) {

		VariableSnmpVar tmpTagInfo = paramHashTable.get(paramname);
		//
		String hihi = jsondata.get("hihi").toString();
		String hi = jsondata.get("hi").toString();
		String lo = jsondata.get("lo").toString();
		String lolo = jsondata.get("lolo").toString();
		String deadb = jsondata.get("deadb").toString();

		byte en = Byte.class.cast(jsondata.get("en"));
		

		// String hihi = "9.0";
		// String hi = "9.0";
		// String lo = "-1.0";
		// String lolo = "-7.2";
		// String deadb = "-1.0";
		WosParamForSetInfo wosParamForSetInfo1 = new WosParamForSetInfo();
		int i = 0;
		try {
			Float rest = Float.valueOf(hihi) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(hi) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lo) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(lolo) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			rest = Float.valueOf(deadb) / tmpTagInfo.VarInfo.FormatCoff;
			wosParamForSetInfo1.pmSetList[i++] = rest.intValue();
			// wosParamForSetInfo1.sByte=Byte.parseByte(en);
			wosParamForSetInfo1.sByte = en;
			ArrayList<VariableBinding> lists = SnmpEngine.cutTableVaribaleWithThold(wosParamForSetInfo1, paramHashTable.get(paramname), rowNumber);

			this.setParam(lists);

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	

	public JSONObject getPmWithModelNumber(JSONObject pJson) throws Exception 
	{
		
		
		return pJson;
	}
}
