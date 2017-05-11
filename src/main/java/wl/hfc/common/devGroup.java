package wl.hfc.common;

import wl.hfc.common.nojuDeviceTableRow.HFCTypes;
import wl.hfc.common.nojuDeviceTableRow.UserDevGrpPriv;

public class devGroup extends LNode implements InodeInterface
{

    public int isAlarm = 0;
    public boolean isTx = false;
    public String mac = "";
    public boolean related = false;


    public UserGroupTableRow BindUserGroupTableRow;


    public UserDevGrpPriv userDevGrpPriv;

    public devGroup(int thisid, String name, int parent)
    {
        //UserGroupID = thisid;
        //UserGroupName = name;
        //ParentGroupID = parent;

        userDevGrpPriv = UserDevGrpPriv.NOPRIV;

       // base.type = HFCTypes.Home;
    }

    public boolean isGroup()
    {
        return true;
    }

    public boolean isPropety()
    {
        return false;
    }


}
