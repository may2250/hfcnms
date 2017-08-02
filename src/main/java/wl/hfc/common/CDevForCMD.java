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

	public void bornImagePath() {

/*		switch (HFCType1) 
		{
	  	case HfcFlyEdfa:
			if (MD.equalsIgnoreCase("WE-1550B"))
				return "WE_1550B";// 野外型图片
			else if (dev.MD.Contains("YW") || dev.DEVICEID.Contains("YW"))
				pictureBox1.Image = VDevs.Model.ResourceProdt.EDFA_YW;// 野外
			else if (dev.MD.Contains("YZ") || dev.DEVICEID.Contains("YZ"))
				pictureBox1.Image = VDevs.Model.ResourceProdt.EDFA_YZ;// 多路
			else if (dev.MD.Contains("HD") || dev.DEVICEID.Contains("HD"))
				pictureBox1.Image = VDevs.Model.ResourceProdt.EDFA_HD;
			else if (dev.DEVICEID.Contains("HE"))
				pictureBox1.Image = Properties.Resources.EDFA_HE;// 野外型图片
			else
				pictureBox1.Image = VDevs.Model.ResourceProdt.edfa; // 默认
			break;
		default:
			break;
		}*/

		// subjson.put("icon", "images/net_center.png");
	}

}
