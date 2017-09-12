package wl.hfc.common;

import java.util.Hashtable;

public class trapDataForWHF {
	
	

    public static Hashtable paramNamesHash;        

    private static String LevelOid= ".1.3.6.1.4.1.17409.1.9.5.1.1";
    private static String AGCGoOid = ".1.3.6.1.4.1.17409.1.9.5.1.2";
    private static String AGCEnalbeOid = ".1.3.6.1.4.1.17409.1.9.5.1.3";
    private static String MGCOid = ".1.3.6.1.4.1.17409.1.9.5.1.4";

    private static String ZBLPumpIndex = ".1.3.6.1.4.1.17409.1.11.4.1.1";
    private static String ZBLPumpBIAS = ".1.3.6.1.4.1.17409.1.11.4.1.2";
    private static String ZBLPumpTEC = ".1.3.6.1.4.1.17409.1.11.4.1.3";
    private static String ZBLPumpTemp = ".1.3.6.1.4.1.17409.1.11.4.1.4";




    //for WT-1550-DM cviewtrans

    private static String insertOutputPower = ".1.3.6.1.4.1.17409.1.6.3.1.16";
    private static String majorOutputPower = ".1.3.6.1.4.1.17409.1.6.3.1.17";

    private static String insertAGCOid = ".1.3.6.1.4.1.17409.1.6.3.1.18";//插播光衰减 dB (读写) 手动
    private static String powerRateOid = ".1.3.6.1.4.1.17409.1.6.3.1.19";//主副光功率比 dB (读写) 自动
    private static String agcModeOid = ".1.3.6.1.4.1.17409.1.6.3.1.20";// 光衰减模式选择 0--手动  1--自动 （读写）



    //CglViewOPTRVForChongqin onlyfnAGCGOvalue
    private static String dorRFOutSlopeControl  = ".1.3.6.1.4.1.17409.1.10001.9.1.1.1";// 射频斜率
    private static String fnAGCGOvalue = ".1.3.6.1.4.1.17409.1.10001.9.3";// 射频斜率
    private static String fnChanelNum = ".1.3.6.1.4.1.17409.1.10001.9.2";// 射频斜率


    //for linyuntianbo recev
    private static String lytbdorOutputIndex = ".1.3.6.1.4.1.17409.1.9.5.1.1";
    private static String commonAgenttrapstatus = ".1.3.6.1.4.1.17409.1.3.3.1.7.1.4";



    //for SCGD OS SWITCH
    private static String opSwPortIn1Pwr = ".1.3.6.1.4.1.48000.100.2202.2.1.4";
    private static String opSwPortIn2Pwr = ".1.3.6.1.4.1.48000.100.2202.2.1.5";
    private static String opSwPortOutPwr = ".1.3.6.1.4.1.48000.100.2202.2.1.6";
    private static String opSwDCPowerVoltage = ".1.3.6.1.4.1.48000.100.2202.5.1.2";
    
    public trapDataForWHF(Boolean isEn)
    {

        nojuParmsTableRow rowS;
        paramNamesHash = new Hashtable();
 

        rowS = new nojuParmsTableRow("insertOutputPower", insertOutputPower,"insertOutputPower", true, (float)0.1, "F1", "dBm");
        paramNamesHash.put("insertOutputPower", rowS);


        rowS = new nojuParmsTableRow("majorOutputPower", majorOutputPower,"majorOutputPower" ,true, (float)0.1, "F1", "dBm");
        paramNamesHash.put("majorOutputPower", rowS);


        rowS = new nojuParmsTableRow("insertAGCOid", insertAGCOid,"insertAGCOid",true, (float)0.1, "F0", "dB");
        paramNamesHash.put("insertAGCOid", rowS);


        rowS = new nojuParmsTableRow("powerRateOid", powerRateOid,"powerRateOid",true, (float)0.1, "F0", "dB");
        paramNamesHash.put("powerRateOid", rowS);


        rowS = new nojuParmsTableRow("agcModeOid", agcModeOid,"agcModeOid",false, (float)1, "F", "");
        paramNamesHash.put("agcModeOid", rowS);

    }


}
