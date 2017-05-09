package com.xinlong.util;

public class UserGroupTableRow
{
    public static  int nameMaxLen = 50;
    public int UserGroupID;
    public String UserGroupName;
    public int ParentGroupID;
    public String fullpath;

    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public boolean isTx;
    public UserGroupTableRow()
    {
        fullpath = "";
    }

    public UserGroupTableRow(int thisid, String name, int parent)
    {
        UserGroupID = thisid;
        UserGroupName = name;
        ParentGroupID = parent;
        fullpath = "";
    }
}
