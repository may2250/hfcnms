package wl.hfc.common;

import java.math.BigDecimal;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import java.util.ArrayList;



public class VariableSnmpVar {

	public boolean withNoThreashold;
	public boolean readWritable;
	public boolean isTableVar;
	public VariableSnmpVar[] subVariableSnmpVarS;
    public ArrayList<VariableSnmpVar[]> subTableVariableSnmpVarSS;
	public nojuParmsTableRow VarInfo;

	public OID AlarmSatOid;
	public String LoOid;
	public String HiOid;

	// ����Ӧ�ó����ʱ����snmpEngine�б�ָ��
	public static String analogAlarmHIHI = ".1.3.6.1.4.1.17409.1.1.1.1.4";
	public static String analogAlarmHI = ".1.3.6.1.4.1.17409.1.1.1.1.5";
	public static String analogAlarmLo = ".1.3.6.1.4.1.17409.1.1.1.1.6";
	public static String analogAlarmLoLo = ".1.3.6.1.4.1.17409.1.1.1.1.7";
	public static String analogAlarmDeadband = ".1.3.6.1.4.1.17409.1.1.1.1.8";
	public static String AlarmEnOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.2";
	public static String AlarmSatOidStr = ".1.3.6.1.4.1.17409.1.1.1.1.3";

	public int maxValue = 0;
	public int minValue = 0;
	public int setpvalue = 0;

	public boolean isformatter = false;

	private String ExtraOidString = ".1";
	
	

    public void setExtraOidString(String extraoid) { //id的可写属性
        if (ExtraOidString.equalsIgnoreCase(extraoid))
            return;
        ExtraOidString = extraoid;
        this.FullSnmpOid = new OID(this.VarInfo.ParamMibOID + ExtraOidString);
       
    }



	

	public OID FullSnmpOid;
	public OID MibDefinedOid;
	public VariableBinding CurrentVarBind;


	// / <summary>
	// / ��ȡ��������ʾ�����ĸ�ʽ��ģʽ��
	// / </summary>

	   public ToValueMode ToValueMode1;
 
	public VariableSnmpVar(nojuParmsTableRow varinfo) {		
		this.VarInfo = varinfo;
		this.MibDefinedOid = new OID(varinfo.ParamMibOID);
		this.FullSnmpOid = new OID(varinfo.ParamMibOID + ExtraOidString);
	
	}

	
	  public VariableSnmpVar(nojuParmsTableRow varinfo,String pExtraOidString, ToValueMode PmODE,boolean pWithThreashold)
		  
	  {
		  this(varinfo);


          this.withNoThreashold = pWithThreashold;
          this.ExtraOidString = pExtraOidString;
        	this.FullSnmpOid = new OID(varinfo.ParamMibOID + ExtraOidString);
          this.ToValueMode1 = PmODE;

          //����
          paramInfor newParamInfor = new paramInfor(this.VarInfo.IsFormatEnable,this.VarInfo.FormatCoff,this.VarInfo.FormatText,"");
          int vIns = 0;
          subVariableSnmpVarS = new VariableSnmpVar[7];
          OID tmpOID=new OID(this.VarInfo.ParamMibOID);          
          int OIDlgth=tmpOID.toByteArray().length+1;
          String extraoid = "." +OIDlgth+this.VarInfo.ParamMibOID;
          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmHIHI + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = PmODE;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;

          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmHI + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = PmODE;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;

          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmLo + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = PmODE;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;


          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmLoLo + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = PmODE;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;


          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmDeadband + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = PmODE;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;



          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(AlarmEnOidStr + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = ToValueMode.FmtString;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;


          subVariableSnmpVarS[vIns] = new VariableSnmpVar(new nojuParmsTableRow(AlarmSatOidStr + extraoid, newParamInfor));
          subVariableSnmpVarS[vIns].ToValueMode1 = ToValueMode.FmtString;
          subVariableSnmpVarS[vIns].setExtraOidString(pExtraOidString);
          vIns++;
          
          
          subTableVariableSnmpVarSS = new  ArrayList<VariableSnmpVar[]>();

  		for (int i = 0; i < 7; i++) {
  		      vIns = 0;
  			  VariableSnmpVar[] itemSnmpVars= new VariableSnmpVar[7];
  			  

  	          
  			  itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmHIHI + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = PmODE;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;


  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmHI + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = PmODE;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;

  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmLo + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = PmODE;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;


  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmLoLo + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = PmODE;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;


  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(analogAlarmDeadband + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = PmODE;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;



  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(AlarmEnOidStr + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = ToValueMode.FmtString;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;


  	          itemSnmpVars[vIns] = new VariableSnmpVar(new nojuParmsTableRow(AlarmSatOidStr + extraoid, newParamInfor));
  	          itemSnmpVars[vIns].ToValueMode1 = ToValueMode.FmtString;
  	          itemSnmpVars[vIns].setExtraOidString(pExtraOidString);
  	          vIns++;
  	          
  	          subTableVariableSnmpVarSS.add(itemSnmpVars);
  		}


		  
		  
	  }
	  private String TodbmvTodbuvString(VariableBinding tmpvb) {
//		if (tmpvb == null)
//			return "";
//		if (tmpvb.getVariable().getType() != SnmpAPI.INTEGER)
//			return "";
//
//		String retval;
//
//		int intval = int.class.cast(tmpvb.getVariable().toValue());
//		// dbuv=dbmv+60
//		intval += 600;
//
//		int scale = 2;// ����λ��
//		int roundingMode = 4;// ��ʾ�������룬����ѡ��������ֵ��ʽ������ȥβ���ȵ�.
//
//		if (_VarInfo.IsFormatEnable) {
//			float rest = float.class.cast(_VarInfo.FormatCoff * intval);
//			BigDecimal bd = new BigDecimal((double) rest);
//			bd = bd.setScale(scale, roundingMode);
//			float ft = bd.floatValue();
//			retval = String.valueOf(ft);
//		} else {
//			retval = String.valueOf(intval);
//		}
//
//		return retval;
		return "123123";
	}

    public String ToDispString()
    {
  
        switch (this.ToValueMode1)
        {
            case FmtInteger:
                return ToFormattedIntegerValue(this.CurrentVarBind);
            case FmtString:
            	 return ToFormattedFmtString(this.CurrentVarBind);
               default:
                return ToFormattedIntegerValue(this.CurrentVarBind);
        }
    }
    
    
    public String ToDispString(VariableBinding CurrentVarBind)
    {
  
        switch (this.ToValueMode1)
        {
            case FmtInteger:
                return ToFormattedIntegerValue(CurrentVarBind);
            case FmtString:
            	 return ToFormattedFmtString(CurrentVarBind);
               default:
                return ToFormattedIntegerValue(CurrentVarBind);
        }
    }
    private String ToFormattedIntegerValue(VariableBinding tmpvb)
    {
		if (tmpvb == null)
		return "";

        String retval;
        int intval = tmpvb.getVariable().toInt();
    	int scale = 2;// ����λ��
		int roundingMode = 4;// ��ʾ�������룬����ѡ��������ֵ��ʽ������ȥβ���ȵ�.

        if (VarInfo.IsFormatEnable)
        {
			Float rest = Float.class.cast(VarInfo.FormatCoff * intval);
			BigDecimal bd = new BigDecimal((double) rest);
			bd = bd.setScale(scale, roundingMode);
			Float ft = bd.floatValue();
			retval = String.valueOf(ft)+VarInfo.FormatUnit;
        
        }
        else
        {
        	retval = String.valueOf(intval);
        }

        return retval;
    }

    
    private String ToFormattedFmtString(VariableBinding tmpvb)
    {
    
		if (tmpvb == null)
		return "";

        String retval;
        retval = tmpvb.getVariable().toString();
        
        //Byte.parseByte(retval);

        return retval;
    }
    
    
    private String ToBytes(VariableBinding tmpvb)
    {
    
		if (tmpvb == null)
		return "";

        String retval;
        retval = tmpvb.getVariable().toString();
        Byte.parseByte(retval);

        return retval;
    }
    public enum ToValueMode {
		Default, PhyAddr, FmtInteger, FmtString, Gugue, dbmvTodbuv, opsitATT,bytes
	}

}
