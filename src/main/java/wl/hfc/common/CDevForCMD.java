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

		switch (HFCType1) {
		case EDFA:
			if (MD.equalsIgnoreCase("WE-1550B"))
				return "EDFA/WE_1550B";// 野外型图片
			else if (MD.contains("YW") || DEVICEID.contains("YW"))
				return "EDFA/EDFA_YW";// 野外
			else if (MD.contains("YZ") || DEVICEID.contains("YZ"))
				return "EDFA/EDFA_YZ";// EDFA_YZ;// 多路
			else if (MD.contains("HD") || DEVICEID.contains("HD"))
				return "EDFA/EDFA_HD";//
			// return "EDFA/edfa";//
			else if (DEVICEID.contains("HE"))
				return "EDFA/EDFA_HE";//
			else
				return "EDFA/edfa";//
		case TransEM:
			return "transEM";//
		case OSW:
			return "OSW";//
		case HfcMinWorkstation:
			if (DEVICEID.equalsIgnoreCase("RJ-1G-2-II")) {
				return "oprv0002jse";//
			} else if (DEVICEID.equalsIgnoreCase("J-1G-2") || DEVICEID.equalsIgnoreCase("JL-1G-2")
					|| DEVICEID.equalsIgnoreCase("WR1001J"))
				return "oprv1001j";
		default:
			return "unknown";//
		}

		// subjson.put("icon", "images/net_center.png");
	}

}
