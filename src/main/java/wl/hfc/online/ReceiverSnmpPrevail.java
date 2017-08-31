package wl.hfc.online;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;

import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;
import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.common.VariableSnmpVar.ToValueMode;
import wl.hfc.topd.MainKernel;

//***wr1001j:
//fnAGCGOvalue [-9,-4] or [-9,-7]  +/-1dBm
//channel [0,200]
//att [0,20]
//eq [0,15]
	
	
	
public class ReceiverSnmpPrevail extends WosBaseSnmp {
	// 0����snmpv1��1����snmpv2
	private static Logger log = Logger.getLogger(ReceiverSnmpPrevail.class);
	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] commonVariables;
	public VariableSnmpVar[] cDCVariables;
	public VariableSnmpVar[] cOutputVariables;


	// pdus
	private PDU majorVarPdu;
	// private PDU CommonVarPdu;
	private PDU tableInputPdu;
	private PDU tableOutpdu;

	// single model
	public static ReceiverSnmpPrevail me;

	public ReceiverSnmpPrevail(String phsicIndex,String PDeviceID) {
		super(phsicIndex);
		try{
			mjVariables = new VariableSnmpVar[5];
			// tables
			cDCVariables = new VariableSnmpVar[2];
			cOutputVariables = new VariableSnmpVar[4];

			
	
			majorVarPdu = new PDU();
			majorVarPdu.setType(PDU.GET);
			// major
			int vIns = 0;			
			nojuParmsTableRow row1 = pmls.me.tabch.get("fnRFChannelNum");			
			mjVariables[vIns] = new VariableSnmpVar(row1);			
			mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			
			mjVariables[vIns].isformatter=true;
			mjVariables[vIns].setpvalue=0;
			mjVariables[vIns].maxValue=200;
			mjVariables[vIns].minValue=0;	
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			
	
			
			
			
			row1 = pmls.me.tabch.get("fnOpticalReceiverPower");
			mjVariables[vIns] = new VariableSnmpVar(row1, ".1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			
			
			
			row1 = pmls.me.tabch.get("fnReverseOpticalPower");
			mjVariables[vIns] = new VariableSnmpVar(row1, ".7.1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			
			
			
			row1 = pmls.me.tabch.get("fnReturnLaserCurrent");
			mjVariables[vIns] = new VariableSnmpVar(row1, ".1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			
			
			
			row1 = pmls.me.tabch.get("fnAGCGOvalue");			
			mjVariables[vIns] = new VariableSnmpVar(row1);			
			mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			
			mjVariables[vIns].isformatter=true;
			mjVariables[vIns].setpvalue=0;
			
		    if (PDeviceID.equalsIgnoreCase("J-1G-2")||PDeviceID.equalsIgnoreCase("JL-1G-2"))
		    {
				mjVariables[vIns].maxValue=-40;
				mjVariables[vIns].minValue=-90;	
		    }
		    else
		    {
				mjVariables[vIns].maxValue=-70;
				mjVariables[vIns].minValue=-90;	
		    
		    }			
			
			
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			
			
			
	

			tableInputPdu = new PDU();
			tableInputPdu.setType(PDU.GETNEXT);

			row1 = pmls.me.tabch.get("fnDCPowerName");
			cDCVariables[0] = new VariableSnmpVar(row1);
			cDCVariables[0].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtString;

			row1 = pmls.me.tabch.get("fnDCPowerVoltage");
			cDCVariables[1] = new VariableSnmpVar(row1, ".1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, cDCVariables[1]);

			int begincol = 0;
			int endcol = 1;

			VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol + 1];
			int enumi;
			for (enumi = 0; enumi < headerinfos.length; enumi++) {
				headerinfos[enumi] = (VariableSnmpVar) cDCVariables[enumi + begincol];
				tableInputPdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
			}

			this.tableOutpdu = new PDU();
			tableOutpdu.setType(PDU.GETNEXT);

			row1 = pmls.me.tabch.get("fnRFPortName");
			cOutputVariables[0] = new VariableSnmpVar(row1);
			cOutputVariables[0].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtString;

			row1 = pmls.me.tabch.get("fnOutputRFlevelatt");
			cOutputVariables[1] = new VariableSnmpVar(row1);
			cOutputVariables[1].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			paramHashTable.put(row1.ParamMibLabel, cOutputVariables[1]);
			
			cOutputVariables[1].isformatter=true;
			cOutputVariables[1].setpvalue=0;
			cOutputVariables[1].maxValue=200;
			cOutputVariables[1].minValue=0;	
		
			
			
			row1 = pmls.me.tabch.get("fnOutputRFleveleq");
			cOutputVariables[2] = new VariableSnmpVar(row1);
			cOutputVariables[2].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			paramHashTable.put(row1.ParamMibLabel, cOutputVariables[2]);
			
			
			
	/*		   if ((num < 0 || num > 10) && (dev.MD == "WR8602JL" || dev.MD == "WR8604JL" || dev.MD == "WR8604DJ" 
                       || dev.MD == "WR8602RJ" || dev.MD == "WR8604RJL" || dev.MD == "WR8602RJL" || dev.MD == "WR8602JL-CM"
                       || dev.DEVICEID == "JL-86-2" || dev.DEVICEID == "JL-86-4" || dev.DEVICEID == "DJ-86-4" || dev.DEVICEID == "RJL-86-2"
                        || dev.DEVICEID == "RJL-86-4" || dev.DEVICEID == "RJ-86-2"))
                       throw new Exception(ClsLanguageExmp.formGet("参数超出允许范围"));
                   else if ((num < 0 || num > 15))
                       throw new Exception(ClsLanguageExmp.formGet("参数超出允许范围"));
			*/
			
			cOutputVariables[2].isformatter=true;
			cOutputVariables[2].setpvalue=0;
			cOutputVariables[2].maxValue=150;
			cOutputVariables[2].minValue=0;	
		
			
			
			row1 = pmls.me.tabch.get("fnRFPortOutputRFLevel");
			cOutputVariables[3] = new VariableSnmpVar(row1, ".1",ToValueMode.FmtInteger, true);
			cOutputVariables[3].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			paramHashTable.put(row1.ParamMibLabel, cOutputVariables[3]);

			begincol = 0;
			endcol = 3;

			headerinfos = new VariableSnmpVar[endcol - begincol + 1];

			for (enumi = 0; enumi < headerinfos.length; enumi++) {
				headerinfos[enumi] = (VariableSnmpVar) cOutputVariables[enumi + begincol];
				tableOutpdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}
		// majorVarPdu=tablepdu;
		me = this;

	}


    @Override 
	public JSONObject getPmWithModelNumber(JSONObject pJson) throws Exception {
		PDU inPDU;
		CommunityTarget cTgt;

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);

		inPDU = sver.SyncSendSnmpPdu(this.majorVarPdu, cTgt);
		if (inPDU == null) {
			//throw new Exception("paramGetException,Failed!");
			return null;
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

		// table
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables((PDU) this.tableInputPdu.clone(), cTgt, sver);
		pJson.put("dctablerownum", reTable.RowNum);
		
		
		SnmpEngine.tabVarToJason(this.cDCVariables,reTable, pJson,"powertbl");

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable1 = SnmpEngine.GetMibTableVariables((PDU) this.tableOutpdu.clone(), cTgt, sver);
		pJson.put("pumptablerownum", reTable1.RowNum);

		
		SnmpEngine.tabVarToJason(this.cOutputVariables,reTable1, pJson,"pumptbl");

		for (int j = 0; j < this.cDCVariables.length; j++) {

			if (this.cDCVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cDCVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cDCVariables[j], pJson, i, true);
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

    
    


}
