package wl.hfc.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetDataProcess {
	public static int CompareIpAddress(InetAddress ip1, InetAddress ip2)
    {
        if (ip1.equals(ip2)) return 0;

        byte[] byteip1 = ip1.getAddress();
        byte[] byteip2 = ip2.getAddress();

        for (int i = 0; i < 4; i++)
        {
            if (byteip1[i] == byteip2[i])
                continue;
            if (byteip1[i] < byteip2[i])
                return -1;
            else
                return 1;
        }

        return 0;
    }
    
  /// <summary>
    /// 计算ip2-ip1的值。
    /// </summary>
    /// <param name="ip1"></param>
    /// <param name="ip2"></param>
    /// <returns></returns>
    public static long IpAddressDispersion(InetAddress ip1, InetAddress ip2)
    {
        byte[] aryip1 = ip1.getAddress();
        byte[] aryip2 = ip2.getAddress();
        long lngip1, lngip2;        
        lngip1 = aryip1[3] + aryip1[2] * 256 + aryip1[1] * 256 * 256 + aryip1[0] * 256 * 256 * 256;
        lngip2 = aryip2[3] + aryip2[2] * 256 + aryip2[1] * 256 * 256 + aryip2[0] * 256 * 256 * 256;
        
        return lngip2 - lngip1;
    }
    
    public static InetAddress IncIpAddress(InetAddress ip) throws UnknownHostException
    {
        byte[] ipbytes = ip.getAddress();        
        {
            if (++ipbytes[3] == 0)
                if (++ipbytes[2] == 0)
                    if (++ipbytes[1] == 0)
                        ++ipbytes[0];
        }
        return InetAddress.getByAddress(ipbytes);
    }
    
    public static byte[] ParseIPAddress(String ipaddress)
    {

        String tmpstring;

        byte[] t = new byte[4];
        int i0, i1, i2;
        i0 = ipaddress.indexOf('.', 0);
        tmpstring = ipaddress.substring(0, i0);
        t[0] = (byte)Byte.parseByte(tmpstring);
        i1 = ipaddress.indexOf('.', i0 + 1);
        tmpstring = ipaddress.substring(i0 + 1, i1 - i0 - 1);
        t[1] = (byte)Byte.parseByte(tmpstring);
        i2 = ipaddress.indexOf('.', i1 + 1);
        tmpstring = ipaddress.substring(i1 + 1, i2 - i1 - 1);
        t[2] = (byte)Byte.parseByte(tmpstring);
        tmpstring = ipaddress.substring(i2 + 1, ipaddress.length() - i2 - 1);
        t[3] = (byte)Byte.parseByte(tmpstring);


        return t;


    }
}
