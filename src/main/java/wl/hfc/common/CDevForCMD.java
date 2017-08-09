package wl.hfc.common;

import java.util.ArrayList;

import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class CDevForCMD {

	public String mNetAddress = null;
	public NetTypes mNetType = NetTypes.other;
	public String ROCommunity;
	public String RWCommunity;
	public String DEVICEID;
	public HFCTypes HFCType1;
	public String ID = "";
	public String MD = "";
	public String SN = "";
	public String DisperString = "";
	public ArrayList sessionList = new ArrayList();
	public String imagePath = "";

	public CDevForCMD() {

	}

	public CDevForCMD(String ROCommunity, String RWCommunity, String NetAddress) {

		this.mNetAddress = NetAddress;

		this.ROCommunity = ROCommunity;
		this.RWCommunity = RWCommunity;
		// mNetType = type;

	}

	public void newSeesion(int session) {

		// edit seesionlist

	}

	public String bornImagePath() {

	switch (HFCType1) 
		{
	    	case EDFA:
			if (MD.equalsIgnoreCase("WE-1550B"))
				return "WE_1550B";// 野外型图片
			else if (MD.contains("YW") || DEVICEID.contains("YW"))
				return "EDFA_YW";// 野外
			else if (MD.contains("YZ") || DEVICEID.contains("YZ"))
				return "EDFA_YZ";// EDFA_YZ;// 多路
			else if (MD.contains("HD") || DEVICEID.contains("HD"))
				return "EDFA_HD";// 
			else if (DEVICEID.contains("HE"))
				return "EDFA_HE";// 
			else
				return "edfa";// 

		default:
			return "edfa";// 	
		}

		// subjson.put("icon", "images/net_center.png");
	}

}
