package wl.hfc.common;

import java.util.Date;

import wl.hfc.common.NlogType.OperLogTypes;

public class nojuOperLogTableRow
{
    public int OperLogID;
 //   public int OperLogTypeLevel;
    public OperLogTypes OperLogType;
    public String OperLogContent;
    public Date OperLogTime;
    public String OperLogUser;

    public nojuOperLogTableRow(OperLogTypes type, String content, Date time, String logUser)
    {
        OperLogID = 0;
      // = int();
        OperLogType = type;
        OperLogUser = logUser;
        OperLogContent = content;
        OperLogTime = time;
    }
}