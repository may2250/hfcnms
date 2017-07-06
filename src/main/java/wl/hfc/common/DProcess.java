package wl.hfc.common;

import java.net.MalformedURLException;
import java.net.URL;

import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class DProcess {
	
	
	public static boolean IsIp(String ipStr) {
		try {
			URL testUrl = new URL("http://" + ipStr);
			return true;
		} catch (MalformedURLException e) {
			System.out.println("testIp() error:" + e.toString());
			return false;
		} catch (Exception e) {
			System.out.println("testIp() unknow error:" + e.toString());
			return false;
		}
	}
	   public static NetTypes getNetTypes(HFCTypes pHFCTypes)
       {
		  
           switch (pHFCTypes)
           {
               case EDFA:
               case HfcFlyEdfa:
               case HfcEdfaWE_HD_SWITCH://带切换
                   return NetTypes.EDFA;
               case TransOld:
               case Trans1310DM:
               case Trans1550DM:
               case TransEM:
                   return NetTypes.Trans;
               case OSW:
                   return NetTypes.OSW;
               case RFSW:
                   return NetTypes.RFSW;
               case PreAMP:
                   return NetTypes.PreAMP;
               case HfcOptrv:
               case HfcWorkstation:
               case HfcReceSXGDS8602J:
               case HfcMinWorkstation:
               case HfcReceJDS:
               case HfcPR0WorkStation:
               case HfcWorkStationWR8604HJ:
               case HfcSTDWorkStation:
               case HfcWR1001JS:
               case RTN_WR2004RJ:
               case HfcMinWorkstationJKQ1G1:
               case HfcWR1004SJL:
               case HfcWR1004SJLofHJS1G2:
               case HfcWR1004SJLMLD1G4GD:
                   return NetTypes.rece_workstation;
               case wos2000:
               case Wos3000:
               case wos4000:
               case wos3000SCTE:
               case wos5000:
                   return NetTypes.wos;
               default:
                   return NetTypes.other;//其他设备
           }

       }


	   
	   
       public static String getDevDISCRIPTIONByNettypeString(NetTypes pNetTypes)
       {
           switch (pNetTypes)
           {
               case other:
                   return ClsLanguageExmp.viewGet("其他设备");
               case EDFA:
                   return "EDFA";
               case Trans:
                   return ClsLanguageExmp.viewGet("光发射机");
               case rece_workstation:
                   return ClsLanguageExmp.viewGet("光接收机")+"/"+ClsLanguageExmp.viewGet("光工作站");
               case OSW:
                   return ClsLanguageExmp.viewGet("光切换开关");
               case RFSW:
                   return ClsLanguageExmp.viewGet("射频切换开关");
               case PreAMP:
                   return ClsLanguageExmp.viewGet("前置放大器");
               case wos:
                   return ClsLanguageExmp.viewGet("光平台");
               default:
                   return ClsLanguageExmp.viewGet("其他设备");

           }
       }

       

   	public static NetTypes netTypeFromStringNetTypes(String tpString)
       {
   		
   		
   		if (tpString.equalsIgnoreCase(NetTypes.rece_workstation.toString())) {
            return NetTypes.rece_workstation;	
		}
   		else if(tpString.equalsIgnoreCase(NetTypes.EDFA.toString()))
   		{   			
   			
   		   return NetTypes.EDFA;
   		}
 		else if(tpString.equalsIgnoreCase(NetTypes.Trans.toString()))
   		{   			
   			
   		   return NetTypes.Trans;
   		}
 		else if(tpString.equalsIgnoreCase(NetTypes.OSW.toString()))
   		{   			
   			
   		   return NetTypes.OSW;
   		}
 		else if(tpString.equalsIgnoreCase(NetTypes.PreAMP.toString()))
   		{   			
   			
   		   return NetTypes.PreAMP;
   		}
 		else if(tpString.equalsIgnoreCase(NetTypes.RFSW.toString()))
   		{   			
   			
   		   return NetTypes.RFSW;
   		}
   		else {
			
   		   return NetTypes.other;  		
		}
       
         

       }
       
}
