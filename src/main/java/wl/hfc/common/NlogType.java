package wl.hfc.common;

public class NlogType
{
    

    //基于和应答器协议
    public static TrapLogTypes GetTrapLogType(byte num)
    {
        switch (num)
        {
            case 1:
                return TrapLogTypes.Normal;
            case 2:
                return TrapLogTypes.HiHi;
            case 3:
                return TrapLogTypes.Hi;
            case 4:
                return TrapLogTypes.Lo;
            case 5:
                return TrapLogTypes.LoLo;
            case 6:
                return TrapLogTypes.Wos3000Alarm;
            default:
                return TrapLogTypes.GenericInfor;
        }
    }
    /// <summary>
    /// if unknow ,return 99;
    /// </summary>
    /// <param name="type"></param>
    public static int getAlarmLevel(TrapLogTypes type)
    {
        int level1 = 99;
        switch (type)
        {
            case TestOnline:
            case Normal: 
                level1 = 0;//普通，次要
                break;
            case Wos3000Alarm:
            case Lo:
            case Hi:
            case Offline:
                level1 = 1;//重要
                break;
            case LoLo:
            case HiHi:
                level1 = 2;//紧急
                break;
            default:
                return level1;

        }
        return level1;

    }


    public static String getAlarmString(TrapLogTypes type)
    {
    	String alarmString = "";
        switch (type)
        {
            case TestOnline:
            case Normal:
                return ClsLanguageExmp.commonGet("普通信息");
            case LoLo:
            case HiHi:
                return ClsLanguageExmp.commonGet("紧急告警");
            case Wos3000Alarm:
            case Lo:
            case Hi:
            case Offline:
                return ClsLanguageExmp.commonGet("重要告警");
            default:
                return ClsLanguageExmp.commonGet("普通信息");//先作为华数要求的“次要告警”

        }


    }

    public static String getOPString(OperLogTypes type)
    {
        String alarmString = "";


        if (ClsLanguageExmp.isEn)
        {
            switch (type)
            {
                case Unknown:
                    return "Unknown";

                case System:
                    return "System";

                case UserOpration:
                    return "User Opration";

                case UserGroupOpration:
                    return "UserGroup Opration";
                case DeviceOpration:
                    return "Device Opration";
                case BackupLog:
                    return "Backup Log";
                case SearchDevice:
                    return "Search Device";
                case Acknowledge:
                case ClearAcknowledge:
                case ClearAllAcknowledge:
                case SetSystemSounder:
                    return "Common";
                default:
                    return "Unknown";

            }
        }
        else
        {

            switch (type)
            {
                case Unknown:
                    return "未知";

                case System:
                    return "系统";

                case UserOpration:
                    return "用户管理";

                case UserGroupOpration:
                    return "设备组操作";
                case DeviceOpration:
                    return "设备操作";
                case BackupLog:
                    return "日志备份";
                case SearchDevice:
                    return "设备搜索";
                case Acknowledge:
                case ClearAcknowledge:
                case ClearAllAcknowledge:
                case SetSystemSounder:
                    return "普通";
                default:
                    return "未知";

            }

        }




    }

    public enum OperLogTypes 
    {
        Unknown,
        System,

        UserOpration,
        UserGroupOpration,
        DeviceOpration,
        BackupLog,

        SearchDevice,
        Acknowledge,
        ClearAcknowledge,
        ClearAllAcknowledge,
        SetSystemSounder,

    }

    public enum TrapLogTypes
    {

        //eventInfor alarmlevel 1

        ColdStart,
        WarmStart, 

        Offline,

        Hi,
        Lo,
        HiHi,
        LoLo,
        Wos3000Alarm,


        GenericInfor,  
        TestOnline,
        Normal,



    }

    public enum AuthResult { SUCCESS, USER_NOT_EXIST, PASSWD_NOT_MATCH, USER_ALREADYLOGIN }
    
}




