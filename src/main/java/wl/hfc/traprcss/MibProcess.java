package wl.hfc.traprcss;

import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;


public class MibProcess {

    private static MibOperations MibOperObj;

    //����MIB���������


    public MibProcess(String path)
    {
        //shut by noju
         initMibOperObj(path);

    }
    public static void initMibOperObj(String StartupPath)
    {
    	try {  	
        	MibOperObj = new MibOperations();
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-ALARMS-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-COMMON-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-PROPERTY-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-OPTICALSWITCH-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-OPTICALAMPLIFIER-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-OPTICALTRANSMITTERDIRECTLY-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-DOWNSTREAMOPTICALRECEIVER-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-DOWNSTREAMOPTICALRECEIVER-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-FIBERNODE-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-AMPLIFIER-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-RFSWITCH-MIB");
            MibOperObj.loadMibModule(StartupPath + "/NSCRTV-HFCEMS-EXTERNALOPTICALTRANSMITTER-MIB");

           
		}catch(Exception e){
			e.printStackTrace();
			//redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
	         
    	

    }

    public static String getLabel(SnmpOID oid)
    {
        if (MibOperObj.getNearestNode(oid) == null)
        {
            return "";
        }
        else
        {
            return MibOperObj.getNearestNode(oid).getLabel();
        }
    }

    public static Boolean isNodeExsist(SnmpOID oid)
    {

        MibNode fnode = MibOperObj.getNearestNode(oid);
        if (fnode == null)
        {
            return false;
        }
        else
        {

            return true;
        }

    }
    public static int[] getOID(SnmpOID oid)
    {
        return MibOperObj.getNearestNode(oid).getOID();
    }


    public static SnmpOID getOID(String label)
    {
        return MibOperObj.getSnmpOID(label);
    }

}


