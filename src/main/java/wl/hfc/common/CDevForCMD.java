package wl.hfc.common;

import java.util.ArrayList;

import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class CDevForCMD {

	public String mNetAddress = null;
    public NetTypes mNetType =NetTypes.other;
	public String ROCommunity;
	public String RWCommunity;
	public String DEVICEID;
	public HFCTypes HFCType1;
	public String ID = "";
	public String MD = "";
	public String SN = "";
	public String DisperString = "";
    public ArrayList sessionList=new  ArrayList();
	public CDevForCMD() {

	}

	public CDevForCMD(String ROCommunity, String RWCommunity, String NetAddress) {

		this.mNetAddress = NetAddress;

		this.ROCommunity = ROCommunity;
		this.RWCommunity = RWCommunity;
	    //mNetType = type;

	}
	
	public void newSeesion(int session)	
	{
		
		//edit seesionlist
	
	}



}
