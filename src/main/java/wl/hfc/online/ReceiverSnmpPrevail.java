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

public class ReceiverSnmpPrevail extends WosBaseSnmp {
	// 0����snmpv1��1����snmpv2
	private static Logger log = Logger.getLogger(ReceiverSnmpPrevail.class);
	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] commonVariables;
	public VariableSnmpVar[] cInputVariables;
	public VariableSnmpVar[] cOutputVariables;

	private SnmpTableInfo InputTableInfo;
	private SnmpTableInfo outputTable;

	// pdus
	private PDU majorVarPdu;
	// private PDU CommonVarPdu;
	private PDU tableInputPdu;
	private PDU tableOutpdu;

	// single model
	public static ReceiverSnmpPrevail me;

	// basic
	private static String heCommonTemperature = ".1.3.6.1.4.1.5591.1.11.2.1.1.1.1.1.1.2";
	// ChannelAlone(1) indicates that all channels of the OPRX is working,
	// Backup(2) indicates that OPRX is working on backup status.
	private static String heOpRxUnitWorkSetting = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.33.1.1";

	// 1 alc;2 mlc inter
	private static String heOpRxUnitDriveSetting = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.33.1.2";

	// output
	private static String heOpRxOutputIndex = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.2.1.1";
	private static String heOpRxOutputControl = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.2.1.2";
	private static String heOpRxOutputALCLevel = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.2.1.34";
	private static String heOpRxOutputMLCLevel = ".1.3.6.1.4.1.5591.1.11.1.2.1.1.2.1.35";

	public ReceiverSnmpPrevail(String phsicIndex) {
		super(phsicIndex);
		try{
			mjVariables = new VariableSnmpVar[2];
			// tables
			cInputVariables = new VariableSnmpVar[2];
			cOutputVariables = new VariableSnmpVar[4];
			//commonVariables = new VariableSnmpVar[4];
			majorVarPdu = new PDU();
			majorVarPdu.setType(PDU.GET);
			// major
			int vIns = 0;			
			nojuParmsTableRow row1 = pmls.paramxml1.tab1.get("fnRFChannelNum");			
			mjVariables[vIns] = new VariableSnmpVar(row1);			
			mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));
			row1 = pmls.paramxml1.tab1.get("fnOpticalReceiverPower");
			mjVariables[vIns] = new VariableSnmpVar(row1, ".1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
			this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));

			tableInputPdu = new PDU();
			tableInputPdu.setType(PDU.GETNEXT);

			row1 = pmls.paramxml1.tab1.get("fnDCPowerName");
			cInputVariables[0] = new VariableSnmpVar(row1);
			cInputVariables[0].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtString;

			row1 = pmls.paramxml1.tab1.get("fnDCPowerVoltage");
			cInputVariables[1] = new VariableSnmpVar(row1, ".1", VariableSnmpVar.ToValueMode.FmtInteger, true);
			paramHashTable.put(row1.ParamMibLabel, cInputVariables[1]);

			int begincol = 0;
			int endcol = 1;

			VariableSnmpVar[] headerinfos = new VariableSnmpVar[endcol - begincol + 1];
			int enumi;
			for (enumi = 0; enumi < headerinfos.length; enumi++) {
				headerinfos[enumi] = (VariableSnmpVar) cInputVariables[enumi + begincol];
				tableInputPdu.add(new VariableBinding(headerinfos[enumi].MibDefinedOid));
			}

			this.tableOutpdu = new PDU();
			tableOutpdu.setType(PDU.GETNEXT);

			row1 = pmls.paramxml1.tab1.get("fnRFPortName");
			cOutputVariables[0] = new VariableSnmpVar(row1);
			cOutputVariables[0].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtString;

			row1 = pmls.paramxml1.tab1.get("fnOutputRFlevelatt");
			cOutputVariables[1] = new VariableSnmpVar(row1);
			cOutputVariables[1].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;

			row1 = pmls.paramxml1.tab1.get("fnOutputRFleveleq");
			cOutputVariables[2] = new VariableSnmpVar(row1);
			cOutputVariables[2].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;

			row1 = pmls.paramxml1.tab1.get("fnRFPortOutputRFLevel");
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

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress, this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable1 = SnmpEngine.GetMibTableVariables((PDU) this.tableOutpdu.clone(), cTgt, sver);
		pJson.put("pumptablerownum", reTable1.RowNum);

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

	private JSONObject receivertabVarToJason(SnmpTableInfo tabVariables, SnmpTableInfo pOutVariables, JSONObject pJson) {
		int enumi, enumj;
		int i = 0;

		for (enumi = 0; enumi < tabVariables.RowNum; enumi++) {
			for (enumj = 0; enumj < tabVariables.ColNum; enumj++) {

				String vale = cInputVariables[enumj].ToDispString(tabVariables.TableCells.get(i));
				pJson.put(cInputVariables[enumj].VarInfo.ParamMibLabel + "_row" + enumi, vale);
				i++;
			}
		}

		i = 0;

		for (enumi = 0; enumi < pOutVariables.RowNum; enumi++) {
			for (enumj = 0; enumj < pOutVariables.ColNum; enumj++) {

				String vale = cOutputVariables[enumj].ToDispString(pOutVariables.TableCells.get(i));
				pJson.put(cOutputVariables[enumj].VarInfo.ParamMibLabel + "_row" + enumi, vale);
				i++;
			}
		}

		return pJson;

	}

	

	private void setSubVarsWithTagInfoBYparamname(String paramname, WosParamForSetInfo wosParamForSetInfo1) {

		if (paramname.equalsIgnoreCase("hfc_ingonglv")) {

			ArrayList<VariableBinding> lists = SnmpEngine.cutMajorVaribaleWithThold(wosParamForSetInfo1, this.mjVariables[1]);

			this.setParam(lists);

		} else if (paramname.equalsIgnoreCase("hfc_powerv2") || paramname.equalsIgnoreCase("hfc_powerv1")) {

			String rowString = paramname.substring(paramname.length() - 1, paramname.length());

			int row = Integer.parseInt(rowString);

			ArrayList<VariableBinding> lists = SnmpEngine.cutTableVaribaleWithThold(wosParamForSetInfo1, this.cInputVariables[1], row);

			this.setParam(lists);

		}

	}

}
