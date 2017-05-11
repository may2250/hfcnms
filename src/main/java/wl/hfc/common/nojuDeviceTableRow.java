package wl.hfc.common;


public class nojuDeviceTableRow
{
    public static final int nameMaxLen=50;
    public static final int ROCMMaxLen = 20;
    public static final int RWCMMaxLen = 20;


    public boolean trapShield = false;

    private String _NetAddress;//readonly参数只能在构造函数的时候被赋值。
    /// <summary>
    /// 获取网管地址，包括ＩＰ地址或ＭＡＣ地址。
    /// </summary>
    public String get_NetAddress()
    {
       return _NetAddress;
    }


    public int slotIndex ;
    private NetTypes _NetType;

	public NetTypes get_NetType() {
		return _NetType;
	}
	

	public int UserGroupID = 0;
    /// <summary>
    /// 获取或设置所在的用户分组ＩＤ。
    /// </summary>

	public String Name = "";
    /// <summary>
    /// 获取或设置设备名称。
    /// </summary>

	public String _ROCommunity = "public";

	public String _RWCommunity = "public";

	public boolean _IsRegister = false;

    public String remark;

    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public boolean isTx;


    public String HeadAddress = "";

    public nojuDeviceTableRow(String netaddr, NetTypes nettype)
    {
        this._IsRegister = false;
        this._NetAddress = netaddr;
        this._NetType=nettype;
        this.Name = netaddr;
        this.remark ="";

    }
    
    
    public enum HFCTypes 
    {
        Unknown,

        EDFA,
        HfcFlyEdfa,

        TransOld,
        Trans1310DM,
        Trans1550DM,
        TransEM,
        TransDM_SCTE,

        OSW,
        RFSW,

        PreAMP,

        HfcOptrv,
        HfcWorkstation,
        HfcReceSXGDS8602J,
        HfcMinWorkstation,
        HfcReceJDS,
        HfcPR0WorkStation,
        HfcWorkStationWR8604HJ,
        HfcSTDWorkStation,
        HfcWR1001JS,
        HfcWR1004SJL,
        RTN_WR2004RJ,
        HfcMinWorkstationJKQ1G1,
        HfcWR1004SJLofHJS1G2,
        HfcWR1004SJLMLD1G4GD,
        JLED1202,

        wos2000,
        Wos3000,
        wos4000,
        wos3000SCTE,
        wos5000,


        SignalGenarator,

        WR2G600R,
        ZBLEDFA,
        HfcOptrvFofChongqin,
        WR1082_ONU,
        HfcEdfaWE_HD_SWITCH,

        ES26,
        CiscoEDFA,
        Cisco_64657T,

        RTL1550Transmitter,
        wos4000LuTong,

        OTECWos,
        OTECEDFA_AOA1550,
        OTECEDFA_EOA1522,
        OTECAOT_1310M_10,
        OTECAOR1000CGMII,
        OcomclientEDFA,

        LYTB_TBF1000,
        LYTBEDFA,
        LYTBOSswitch,
        LYTB_MTRAN2000,

        WR2G600R6_Port,
        WT2G600R6_Port,
        Home, //非设备1~10
        Line,
        Fiber3,
        Source_A,
        Source_B,
        Branch,
        OSBranch,
        ATT,
        Multiplexer

    }
    public enum UserDevGrpPriv { NOPRIV, INPATH, LIST, INCHILD }

}


