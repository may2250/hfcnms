package wl.commonComponent;

import java.net.InetAddress;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.smi.*;

import wl.hfc.common.NetDataProcess;
import wl.hfc.common.IPduSevr;


public class IPSetEngne {
	public static String IPAdddressStrOid = ".1.3.6.1.4.1.17409.1.100.1.0";
    public static String GetwayStrOid = ".1.3.6.1.4.1.17409.1.100.2.0";
    public static String SubnetStrOid = ".1.3.6.1.4.1.17409.1.100.3.0";

    public IPduSevr pduSevr;



    /// <summary>
    /// 解析失败
    /// </summary>
    /// <param name="inpdu"></param>
    /// <returns></returns>
    private String ReParseNetParam(PDU inpdu) throws Exception
    {

        byte[] tmp = new byte[4];
        try
        {
            byte[] tmpip = inpdu.get(0).toString().getBytes();
            for (int i = 0; i < 4; i++)
            {
                tmp[i] = (byte)tmpip[i];
            }
            
            return InetAddress.getByAddress(tmp).toString();
        }
        catch (Exception e)
        {
            throw new Exception("从inpdu解析IP信息错误");

        }

    }

    public void setNMSNetInfo(InetAddress sIP, InetAddress sGw, InetAddress sSubnet, String destDeviceIP, String RWCommunity)
    {

    	VariableBinding vb0, vb1, vb2;
        try
        {
            PDU outpdu = new PDU();
            CommunityTarget cyt = new CommunityTarget();
            cyt.setCommunity(new OctetString(RWCommunity));
            cyt.setAddress(new UdpAddress(destDeviceIP+"/161"));        
            outpdu.setType(PDU.GET);

            byte[] ipaddress1, getway1, subnet1 = new byte[4];

            ipaddress1 = NetDataProcess.ParseIPAddress(sIP.toString());
            getway1 = NetDataProcess.ParseIPAddress(sGw.toString());
            subnet1 = NetDataProcess.ParseIPAddress(sSubnet.toString());


            vb0 = new VariableBinding(new OID(IPAdddressStrOid), new OctetString(ipaddress1));
            vb1 = new VariableBinding(new OID(GetwayStrOid), new OctetString(getway1));
            vb2 = new VariableBinding(new OID(SubnetStrOid), new OctetString(subnet1));
            outpdu.add(vb0);
            outpdu.add(vb1);
            outpdu.add(vb2);
            pduSevr.SyncSendSnmpPdu(outpdu, cyt);

        }
        catch (Exception e)
        {
            // toolStripStatusLabelMsg.Text = ex.Message;
        }







    }


    public void resetNmsCell(String destDeviceIP, String RWCommunity)
    {
    	VariableBinding vb0 = new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.3.1.2.0"), new Integer32(1));
        try
        {
            PDU outpdu = new PDU();
            CommunityTarget cyt = new CommunityTarget();
            cyt.setCommunity(new OctetString(RWCommunity));
            cyt.setAddress(new UdpAddress(destDeviceIP+"/161"));  
            outpdu.setType(PDU.GET);
            outpdu.add(vb0);
            pduSevr.SyncSendSnmpPdu(outpdu, cyt);

        }
        catch (Exception e)
        {
            // toolStripStatusLabelMsg.Text = ex.Message;
        }


    }

    public String getNetInfor(String destDeviceIP, String ROCommunity, String oidString)
    {

        PDU outpdu = new PDU();
        CommunityTarget cyt = new CommunityTarget();
        cyt.setCommunity(new OctetString(ROCommunity));
        cyt.setAddress(new UdpAddress(destDeviceIP+"/161"));
        outpdu.setType(PDU.GET);


        OID snmpOID1 = new OID(oidString);
        outpdu.add(new VariableBinding(snmpOID1));

        PDU inpdu = pduSevr.SyncSendSnmpPdu(outpdu, cyt);
        if (inpdu == null)
        {
            return null;
        }
        else
        {
            try
            {
                String result= this.ReParseNetParam(inpdu);
                return result;
            }
            catch (Exception e)
            {

                return null;
            }


        }

    }
}
