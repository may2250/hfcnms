package wl.hfc.online;




import org.json.simple.JSONObject;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;

import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;
import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.common.trapDataForWHF;
import wl.hfc.common.VariableSnmpVar.ToValueMode;
import wl.hfc.online.pmls;


//带色散补偿的意思：
//
public class TransDMSnmpPrevail extends WosBaseSnmp {


	// VariableSnmpVars
	public VariableSnmpVar[] mjVariables;
	public VariableSnmpVar[] insertVariables;
	public VariableSnmpVar[] cDCVariables;
	public VariableSnmpVar[] cInputVariables;
	public VariableSnmpVar[] cOutputVariables;



	// pdus
	private PDU majorVarPdu;
	private PDU tableDCPdu;
	private PDU tableInputPdu;
	private PDU tableOutpdu;
	private PDU insertVarPdu;
	// single model
	public static TransDMSnmpPrevail me;

	
	private int tableOutPlength=7;
	private int tableInputPlength=4;
	private String pdeviceIDString="";

	

	private int mgcMax=0;
	private int mgcMin =0;
	private int agcMax =0;
    private int agcMin =0;
    
   // private boolean isNeedGetInsertParam=false;

	public TransDMSnmpPrevail(String phsicIndex,String PDeviceID) {
		super(phsicIndex);
		pdeviceIDString=PDeviceID;
		mjVariables = new VariableSnmpVar[2];
		insertVariables = new VariableSnmpVar[5];
		// tables
		cDCVariables = new VariableSnmpVar[2];
		cInputVariables = new VariableSnmpVar[tableInputPlength];
		cOutputVariables = new VariableSnmpVar[tableOutPlength];

		majorVarPdu = new PDU();
		majorVarPdu.setType(PDU.GET);



		this.insertVarPdu = new PDU();
		insertVarPdu.setType(PDU.GET);


        if (PDeviceID.equalsIgnoreCase("1310_2.6G"))
        {
            mgcMax = 15;
            mgcMin = 0;
            agcMax = 3;
            agcMin = -3;

          //  this.labelChanelNumber.Visible = false;
          //  this.textBoxChanleNum.Visible = false;
        
        }
        else if (PDeviceID.contains("1550-DM"))
        {
            mgcMax = 20;
            mgcMin = 0;
            agcMax = 3;
            agcMin = -3;
           // isNeedGetInsertParam=true;
        }
        else
        {
            mgcMax = 10;
            mgcMin = 0;
            agcMax = 5;
            agcMin = -5;
        }

		// major**************************
		int vIns = 0;
		nojuParmsTableRow row1 = pmls.tabch.get("otdConfigurationRFChannels");	
		
		mjVariables[vIns] = new VariableSnmpVar(row1);			
		mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		
		mjVariables[vIns].isformatter=true;
		mjVariables[vIns].maxValue=84;
		mjVariables[vIns].minValue=0;		
		mjVariables[vIns].setpvalue=0;
		
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));		
		

		row1 = pmls.tabch.get("otdInputRFAttenuationRange");	
		
		mjVariables[vIns] = new VariableSnmpVar(row1);			
		mjVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, mjVariables[vIns]);
		this.majorVarPdu.add(new VariableBinding(mjVariables[vIns++].FullSnmpOid));		
		
		
		

		//INSERT VARI*************************************
		vIns=0;
		row1 = (nojuParmsTableRow)trapDataForWHF.paramNamesHash.get("insertOutputPower");
	//	row1.ParamMibOID = row1.ParamOrignalOID;
		this.insertVariables[vIns]= new VariableSnmpVar(row1);
		insertVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, insertVariables[vIns]);
        this.insertVarPdu.add(new VariableBinding(insertVariables[vIns++].FullSnmpOid));	
        
        
        
    	row1 = (nojuParmsTableRow)trapDataForWHF.paramNamesHash.get("majorOutputPower");
		//row1.ParamMibOID = row1.ParamOrignalOID;
		this.insertVariables[vIns]= new VariableSnmpVar(row1);
		insertVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, insertVariables[vIns]);
        this.insertVarPdu.add(new VariableBinding(insertVariables[vIns++].FullSnmpOid));	
        
    	row1 = (nojuParmsTableRow)trapDataForWHF.paramNamesHash.get("insertAGCOid");
		//row1.ParamMibOID = row1.ParamOrignalOID;
		this.insertVariables[vIns]= new VariableSnmpVar(row1);
		insertVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, insertVariables[vIns]);
        this.insertVarPdu.add(new VariableBinding(insertVariables[vIns++].FullSnmpOid));	
        
    	row1 = (nojuParmsTableRow)trapDataForWHF.paramNamesHash.get("powerRateOid");
	//	row1.ParamMibOID = row1.ParamOrignalOID;
		this.insertVariables[vIns]= new VariableSnmpVar(row1);
		insertVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, insertVariables[vIns]);
        this.insertVarPdu.add(new VariableBinding(insertVariables[vIns++].FullSnmpOid));	
        
    	row1 = (nojuParmsTableRow)trapDataForWHF.paramNamesHash.get("agcModeOid");
	//	row1.ParamMibOID = row1.ParamOrignalOID;
		this.insertVariables[vIns]= new VariableSnmpVar(row1);
		insertVariables[vIns].ToValueMode1 = VariableSnmpVar.ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, insertVariables[vIns]);
        this.insertVarPdu.add(new VariableBinding(insertVariables[vIns++].FullSnmpOid));	
        
        

		
		
		//DC TABLE
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
        if (PDeviceID.equalsIgnoreCase("1310_2.6G"))
        {
        	cInputVariables[i].VarInfo.FormatUnit = "dBm";
        }
        else
        {
          	cInputVariables[i].VarInfo.FormatUnit = "dBuV/ch";        
        }
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[i++]);
		
		
		row1 = pmls.me.tabch.get("otdAGCControl");
		cInputVariables[i] = new VariableSnmpVar(row1);
		cInputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[i++]);		

		row1 = pmls.me.tabch.get("otdConfigurationDriveLevel");//AGC OFFSET
		cInputVariables[i] = new VariableSnmpVar(row1);
		cInputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		
		cInputVariables[i].isformatter=true;
		cInputVariables[i].maxValue=agcMax*10;
		cInputVariables[i].minValue=agcMin*10;		
		cInputVariables[i].setpvalue=0;
		
		paramHashTable.put(row1.ParamMibLabel, cInputVariables[i++]);		
		


		row1 = pmls.me.tabch.get("otdConfigurationRFAttenuation");//MGC
		cInputVariables[i] = new VariableSnmpVar(row1);
		cInputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		cInputVariables[i].isformatter=true;
		cInputVariables[i].maxValue=mgcMax*10;
		cInputVariables[i].minValue=mgcMin*10;		
		cInputVariables[i].setpvalue=0;
		
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
		
		
		
		//output table*********************************
		i=0;
		this.tableOutpdu = new PDU();
		tableOutpdu.setType(PDU.GETNEXT);

		row1 = pmls.tabch.get("otdIndex");//序列
		cOutputVariables[i] = new VariableSnmpVar(row1);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtString;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);

		row1 = pmls.tabch.get("otdLaserTemp");//温度
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);

		row1 = pmls.tabch.get("otdLaserCurrent");//bias
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		
		
		

		row1 = pmls.tabch.get("otdOpicalOutputPower");//输出光功率
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		row1 = pmls.tabch.get("otdTecCurrent");//TEC
		cOutputVariables[i] =new VariableSnmpVar(row1, ".1",
				ToValueMode.FmtInteger, true);
		cOutputVariables[i].ToValueMode1 = ToValueMode.FmtInteger;
		paramHashTable.put(row1.ParamMibLabel, cOutputVariables[i++]);
		
		row1 = pmls.tabch.get("otdLaserWavelength");//波长
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

		
		
	    if (PDeviceID.equalsIgnoreCase("1310_2.6G"))
        {
	    	exinfor.put("channelview", "0");
	    	
        }
        else 
        {
	    	exinfor.put("channelview", "1");
        }
  
		exinfor.put("otdConfigurationRFChannels", "[0,84]");
		
		exinfor.put("otdConfigurationDriveLevel", "["+agcMin+","+agcMax+"]");
		exinfor.put("otdConfigurationRFAttenuation", "["+mgcMin+","+mgcMax+"]");
		
	    if (PDeviceID.equalsIgnoreCase("WT-1550-DM"))
        {
	    	exinfor.put("otdAGCControl", "AGC,MGC");

        
        }
        else 
        {
	     	exinfor.put("otdAGCControl", "MGC,AGC");
        }
  
		
		exinfor.put("otdConfigurationRFAttenuation", "["+mgcMin+","+mgcMax+"]");
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
			if (this.mjVariables[i].withNoThreashold) {
				getSubVarsWithTagInfo(this.mjVariables[i]);
			}			

		}		
		
		for (int i = 0; i < mjVariables.length; i++) {
			if (this.mjVariables[i].withNoThreashold) {
				SnmpEngine.ThreadPramVarToJason(mjVariables[i], pJson, true);
			}
		}
		
		
		//insert pdu
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);

		inPDU = sver.SyncSendSnmpPdu(this.insertVarPdu, cTgt);
		if (inPDU == null) {
			throw new Exception("paramGetException,Failed!");
		}
		SnmpEngine.ParseBasicVars(this.insertVariables, inPDU);
		SnmpEngine.snmpVarToJason(insertVariables, pJson);		
		
			
			
		

		//table params
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableDCPdu.clone(), cTgt, sver);
		pJson.put("dctablerownum", reTable.RowNum);
		
		
		
		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo inreTable = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableInputPdu.clone(), cTgt, sver);
		pJson.put("intablerownum", inreTable.RowNum);		
		
		
		

		cTgt = SnmpEngine.createMajorPDU(thisDev.mNetAddress,
				this.thisDev.ROCommunity, SnmpConstants.version1);
		SnmpTableInfo reTable1 = SnmpEngine.GetMibTableVariables(
				(PDU) this.tableOutpdu.clone(), cTgt, sver);
		pJson.put("outtablerownum", reTable1.RowNum);
		
		
		

		SnmpEngine.tabVarToJason(this.cDCVariables,reTable, pJson,"dctable");

		SnmpEngine.tabVarToJason(this.cInputVariables,inreTable, pJson,"intable");
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
	
		for (int j = 0; j < this.cOutputVariables.length; j++) {

			if (this.cOutputVariables[j].withNoThreashold) {
				for (int i = 0; i < reTable1.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cOutputVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cOutputVariables[j], pJson, i, true);
				}

			}

		}
		
		for (int j = 0; j < this.cInputVariables.length; j++) {
			if (this.cInputVariables[j].withNoThreashold) {
				for (int i = 0; i < inreTable.RowNum; i++) {
					this.getSubVarsWithTagInfo(this.cInputVariables[j], i);
					SnmpEngine.ThreadPramVarToJason(this.cInputVariables[j], pJson, i, true);
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

	
}
