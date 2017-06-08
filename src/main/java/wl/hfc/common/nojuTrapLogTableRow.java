package wl.hfc.common;

import wl.hfc.common.NlogType.TrapLogTypes;

public class nojuTrapLogTableRow
{

    public int TrapLogID;
    public int level;
    public TrapLogTypes TrapLogType;
    public String TrapDevAddress;
    public String TrapLogContent;
      public java.util.Date TrapLogTime;
    public String neName;

    public int slotIndex = 99;
    public String parmName ="";
    public String paramValue = "";
    public String isTreated ="";
    public String TrapTreatMent = "未处理";   
    public boolean isValid=true;   


    public nojuTrapLogTableRow(int level, TrapLogTypes type, String addr, String neName, String content, java.util.Date time, String tMent, String isTreatMent, String paramName, String pValue)
    {
        TrapLogID = 0;
        this.level = level;
        TrapLogType = type;
        TrapDevAddress = addr;
        TrapLogContent = content;
        TrapLogTime = time;
        this.neName = neName;


        this.TrapTreatMent = tMent;
        this.isTreated = isTreatMent;

        this.parmName = paramName;
        this.paramValue = pValue;


    }
    
    public nojuTrapLogTableRow(boolean isValid)
    {
    	this.isValid=isValid;

    }



}