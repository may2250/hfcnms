package com.xinlong.util;
import java.net.*;

import wl.hfc.common.NetDataProcess;

public class SearchIpInfo {
	public InetAddress ipbegin;
    public InetAddress ipend;
    public int searchnumbers;
    public String community;
    public String sessionid;
    public Boolean isBroadCast;
    public int destiType;

    public SearchIpInfo(InetAddress ip1, InetAddress ip2, Boolean isBrdcst, int destiType)
    {
        community = "public";
        if (NetDataProcess.CompareIpAddress(ip1, ip2) <= 0)
        {
        	ipbegin = ip2;
            ipend = ip1;
        }
        else
        {
        	ipbegin = ip1;
            ipend = ip2;            
        }
        searchnumbers = 0;
        /*long retval = NetDataProcess.IpAddressDispersion(ipbegin, ipend);
        if (retval <= 65535)
        {
            searchnumbers = (int)retval + 1;
        }*/
        long retval = NetDataProcess.getIP(ipbegin) - NetDataProcess.getIP(ipend);
        
        if (retval <= 65535)
        {
            searchnumbers = Integer.parseInt(String.valueOf(retval - 1));
        }
        this.isBroadCast = isBrdcst;
        this.destiType = destiType;
    }
    
    
}
