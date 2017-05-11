package wl.hfc.common;


public class DevTopd extends LNode implements InodeInterface
{


	public boolean isOline;

    public int isAlarm = 0;



    public boolean isTx = false;
    public String mac = "";
    public boolean related = false;
    /// <summary>
    /// 获取网管地址，包括ＩＰ地址或ＭＡＣ地址。
    /// </summary>
    ///
    public String _NetAddress = "";
 



    public nojuDeviceTableRow BindnojuDeviceTableRow;
    public NetTypes mNetType =NetTypes.other;
    public DevTopd(nojuDeviceTableRow pRow)
    {
        this._NetAddress = pRow.get_NetAddress();
        // this._Name = Name;
        //  this.UserGroupID = groupID;
        this.mNetType = pRow.get_NetType();

      //  base.type = HFCTypes.Unknown;

        BindnojuDeviceTableRow = pRow;
       // childSlotRows = new List<CDataBasePropery.nojuDeviceTableRow>();
    }



    public boolean isGroup()
    {

        return false;
    }

    public boolean isPropety()
    {
        return false;
    }
}
