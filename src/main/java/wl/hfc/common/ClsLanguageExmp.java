package wl.hfc.common;

import java.util.Hashtable;



public class ClsLanguageExmp
{
	public enum EnumLogoVersion
	{
	    prevail,
	    nurture,
	    otec,
	    BraunTelecom,
	    EnOcom,
	    Aurora,
	    ASTRO,
	    guoan,
	    Single_guangdongshantou,
	    AdvancedMedia,
	    ascent,
	    DIGI,
	    Integra_Optics,
	    huiteer,
	    IndiaUnited

	}
    protected static Hashtable formlangTable;//网管总层面，各模块的界面常用label，名称
    protected static Hashtable commonlangTable;//程序逻辑中的常用语
    protected static Hashtable viewlangTable;//主要是HFC相关设备界面的参数label，名称
    protected static Hashtable messagelangTable;
    protected static Hashtable trapDiscrlangTable;
    public static boolean isEn ;
    public static boolean istrapEn ;

    
    //在外面判断中英文，若英文则调用该函数
    public static String viewGet(String name)
    {

        if (isEn)
        {
        	String rtnStr = (String)viewlangTable.get(name);
            if (rtnStr == null)
                return name;
            else
                return rtnStr;

        }
        else
        {
            return name;
        }


        //if (rtnStr == string.Empty)
        //    throw new System.Exception("！！！！未定义语言！！！！" + name);
        //else
        //    return rtnStr;
    }
    public static String formGet(String name)
    {

        if (isEn)
        {
        	String rtnStr = (String)formlangTable.get(name);
            if (rtnStr == null)
                return name;
            else
                return rtnStr;

        }
        else
        {
            return name;
        }

        //if (rtnStr == String.Empty)
        //    throw new System.Exception("！！！！未定义语言！！！！" + name);
        //else
        //    return rtnStr;
    }
    public static String commonGet(String name)
    {
        //和viewget不一样，这个在类内绑定封装变换
        if (isEn)
        {
            String rtnStr = (String)commonlangTable.get(name);
            if (rtnStr == null)
                return name;
            else
                return rtnStr;

        }
        else
        {
            return name;
        }

        //if (rtnStr == String.Empty)
        //    throw new System.Exception("！！！！未定义语言！！！！" + name);
        //else
        //    return rtnStr;
    }
    public static String trapDiscrGet(String name)
    {
        //和viewget不一样，这个在类内绑定封装变换
        if (istrapEn)
        {
            String rtnStr = (String)trapDiscrlangTable.get(name);
            if (rtnStr == null)
                return name;
            else
                return rtnStr;

        }
        else
        {
            return name;
        }

        //if (rtnStr == String.Empty)
        //    throw new System.Exception("！！！！未定义语言！！！！" + name);
        //else
        //    return rtnStr;
    }

    public static void init(Boolean pisEn,Boolean pistrapEn)
    {
        isEn = pisEn;
        istrapEn = pistrapEn;
        formlangTable = new Hashtable();
        viewlangTable = new Hashtable();
        commonlangTable = new Hashtable();
        trapDiscrlangTable = new Hashtable();

        //fvalue on off
        viewlangTable.put("设置", "Set");
        viewlangTable.put("参数名", "Parameter Name");
        viewlangTable.put("参数值", "Value");
        viewlangTable.put("确定", "OK");
        viewlangTable.put("返回", "Return");

        //wos4000 backplane
        viewlangTable.put("风扇状态表", "Fan index");
        viewlangTable.put("风扇状态", "Fan status");
        viewlangTable.put("风扇控制", "Fan ON/OFF");


        //wos 4000 main pannel
        viewlangTable.put("子设备选择面板", "Sub-equipment selection panel");
        viewlangTable.put("插槽", "slot");
        viewlangTable.put("空槽", "Empty slot");
        viewlangTable.put("Trap地址设置", "Trap Address");

        viewlangTable.put("主机参数", "Host Parameters");
        viewlangTable.put("IP地址", "IP");
        viewlangTable.put("网关", "Gateway");
        viewlangTable.put("子网掩码", "Subnet mask");
        viewlangTable.put("DNS地址", "DNS");
        viewlangTable.put("MAC地址", "MAC");
        viewlangTable.put("工作时间", "Total working time");
        viewlangTable.put("Trap主机", "Trap host");


        viewlangTable.put("输入表", "Input table");
        viewlangTable.put("输出表", "Output table");

        //device name
        viewlangTable.put("电源", "Power");

        viewlangTable.put("光切换开关", "Optical Switch");
        viewlangTable.put("射频切换开关", "RF Switch");
        viewlangTable.put("前置放大器", "Pre-amplifier ");

        viewlangTable.put("光放大器", "EDFA");
        viewlangTable.put("光放大器带切换", "EDFA with Switch");

        viewlangTable.put("光发射机", "Optical Transmitters");
        viewlangTable.put("插播光发射机", "Inter-cut Transmitters");

        viewlangTable.put("光接收机", "Optical Receiver");
        viewlangTable.put("反向光接收机", "Reverse Optical Receiver");
        viewlangTable.put("双备份光接收机", "Double backup optical receiver");
        viewlangTable.put("反向光收带射频切换", "Reverse Optical Receiver with RF Switch");
        viewlangTable.put("光收带切换", "Optical Receiver with Switch");
        viewlangTable.put("光工作站", "Optical workstation");
        viewlangTable.put("光工作站带切换", "Optical workstation with Switch");

        viewlangTable.put("光平台", "Optical Platform");
        viewlangTable.put("未知子设备类型", "Unknown");
        viewlangTable.put("未知设备", "Unknown");
        viewlangTable.put("其他设备", "Other");
        viewlangTable.put("分支分配器", "Branch");
        viewlangTable.put("光分路器", "Optical splitter");
        //Transmitter
        viewlangTable.put("型号", "Model");
        viewlangTable.put("型号温度", "Module temp");
        viewlangTable.put("系统频道数", "Channel number");


        viewlangTable.put("插播输入光功率", "Inter input power");
        viewlangTable.put("衰减后光功率", "Output power after ATT");
        viewlangTable.put("衰减控制模式", "ATT control mode");
        viewlangTable.put("差值光功率", "Output power difference");

        viewlangTable.put("光衰减值", "ATT value");
        viewlangTable.put("衰减值", "ATT");


        viewlangTable.put("RF控制模式", "RF control mode");
        viewlangTable.put("RF输入电平", "Input RF power");
        viewlangTable.put("AGC偏移量", "AGC Offset");
        viewlangTable.put("MGC衰减量", "MGC");

        viewlangTable.put("激光器表", "Laser table");
        viewlangTable.put("温度", "Temp");
        viewlangTable.put("偏置电流", "Bias");
        viewlangTable.put("输出光功率", "Output power");
        viewlangTable.put("制冷电流", "Cooling current");
        viewlangTable.put("光波长", "Wavelength");
        viewlangTable.put("激光器开关", "Laser ON/OFF");


        //rf switch
        viewlangTable.put("通道切换模式", "Switch mode");
        viewlangTable.put("通道选择", "Switch");
        viewlangTable.put("切换阀值", "Switch threshold");
        viewlangTable.put("输入电平", "Input RF level");



        //recev
        viewlangTable.put("模块温度", "Module temp");
        viewlangTable.put("当前工作通道", "Current channel");
        viewlangTable.put("输入光功率", "Input power");
        viewlangTable.put("AGC起控光功率", "AGC valid power");
        viewlangTable.put("RF输出电平", "RF output level");
        viewlangTable.put("EQ均衡", "EQ");
        viewlangTable.put("ATT", "ATT");
        viewlangTable.put("AGC使能", "AGC enable");
        viewlangTable.put("RF输出控制模式", "RF control mode");
        viewlangTable.put("RF输出开关", "RF ON/OFF");


        //powersupply
        viewlangTable.put("电源输出表", "DC table");
        viewlangTable.put("输出电压", "Output voltage");


        //optic switch
        viewlangTable.put("工作波长", "Wavelength");
        viewlangTable.put("频道数", "Channel number");


        //FDA EDFA
        viewlangTable.put("EQ均衡量", "EQ");
        viewlangTable.put("输出电平", "Output level");
        viewlangTable.put("泵浦开关", "Pump ON/OFF");

        //wos3000 old
        viewlangTable.put("程序版本", "Software version");



        //varAlarm
        viewlangTable.put("门限", "Threshold");
        viewlangTable.put("死区", "Dead zone");
        viewlangTable.put("使能", "Enable");



        //auth
        viewlangTable.put("用户管理", "User Management");
        viewlangTable.put("用户名", "Name");
        viewlangTable.put("用户ID", "User ID");
        viewlangTable.put("超级管理员", "Is administrator");
        viewlangTable.put("接受手机短信告警", "Receive SMS");
        viewlangTable.put("用户组", "Group");
        viewlangTable.put("帐号", "Account");
        viewlangTable.put("密码", "Password");
        viewlangTable.put("新密码", "New password");

        viewlangTable.put("取消", "Quit");
        viewlangTable.put("添加", "Add");
        viewlangTable.put("重复", "Repeat");
        viewlangTable.put("修改", "Modify");
        viewlangTable.put("删除", "Delete");
        viewlangTable.put("编辑", "Edit");
        viewlangTable.put("清空", "Clear");   
        viewlangTable.put("权限管理", "User management");


        //DEVICE SERARCH
        viewlangTable.put("开始", "Start");
        viewlangTable.put("结束", "Stop");
        viewlangTable.put("起始IP", "Begin IP");
        viewlangTable.put("结束IP", "Stop IP");
        viewlangTable.put("搜索", "Search");
        viewlangTable.put("团体名", "Community");

       //ALARMLOG CViewTrapLog
        viewlangTable.put("告警 (0)", "Alarm (0)");
        viewlangTable.put("级别", "Level");
        viewlangTable.put("类型", "Type");
        viewlangTable.put("来源", "Source");
        viewlangTable.put("内容", "Content");
        viewlangTable.put("处理意见", "Confirmation");
        viewlangTable.put("发生时间", "Time");
        viewlangTable.put("确认时间|消失时间", "Confirm time");
        viewlangTable.put("路径", "Path");

        viewlangTable.put("告警通知", "Warning Notification");
        //HFC COMMON
        viewlangTable.put("逻辑ID", "Logic ID");
        viewlangTable.put("序列号", "Serial number");
        //sauth


      
        //CMenuMain
        formlangTable.put("文件", "File");
        formlangTable.put("配置", "Set");
        formlangTable.put("管理", "Manage");
        formlangTable.put("工具", "Tools");
        formlangTable.put("视图", "View");
        formlangTable.put("帮助", "Help");

        //for device search software
        formlangTable.put("开始", "Start");
        formlangTable.put("结束", "Stop");
        formlangTable.put("起始IP", "Begin IP");
        formlangTable.put("结束IP", "Stop IP");
        formlangTable.put("搜索", "Search");
        formlangTable.put("团体名", "Community");
        formlangTable.put("确定", "OK");

        //for aboutForm
        formlangTable.put("产品名称", "Product Name");
        formlangTable.put("版本", "Version");
        formlangTable.put("版权", "Copyright");
        formlangTable.put("公司名称", "Company Name");
        formlangTable.put("描述", "Description");


        //MENU TOOLBAR
        formlangTable.put("设备树形管理视图", "Tree view");
        formlangTable.put("系统操作日志视图", "System Log View");
        formlangTable.put("设备公共属性视图", "Public attributes");
        formlangTable.put("设备告警记录视图", "Alarm list view");
        formlangTable.put("设备描述文本视图", "description of equipment view");
        formlangTable.put("地址段搜索设备", "Addresses search");
        formlangTable.put("广播搜索设备", "Broadcast Search");
        formlangTable.put("退出系统", "Exit");
        formlangTable.put("声讯告警控制", "Alarm sound On/Off");
        formlangTable.put("历史告警日志", "History alarm log");
        formlangTable.put("历史操作/事件日志", "Activity / Event log");
        formlangTable.put("权限管理", "User management");
        formlangTable.put("实时参数分析器", "Real-time Parameter Analyzer");
        formlangTable.put("显示所有停靠视图", "Show all docked view");
        formlangTable.put("隐藏所有停靠视图", "Hide all docked view");
        formlangTable.put("广播团体名", "Broadcasting Search");
        formlangTable.put("请在下面的框中输入团体名", "Please enter the community");
        formlangTable.put("关于", "About");
        formlangTable.put("您确定要退出", "Exit");
        formlangTable.put("操作确认", "Option");
        formlangTable.put("启动", "Startup");
        formlangTable.put("日志", "Log");



        formlangTable.put("创建IP设备", "Create IP equipment");
        formlangTable.put("创建分组", "Create sub-group");
        formlangTable.put("刷新", "Refresh");
        formlangTable.put("更名", "Rename");
        formlangTable.put("删除", "Delete");
        formlangTable.put("查找", "Search");

        formlangTable.put("地址段搜索II类设备", "Addresses section search II equipment");
        formlangTable.put("广播包搜索II类设备", "Broadcasting package search II equipment");
        formlangTable.put("清空未注册设备", "Clear unregistered equipment");
        formlangTable.put("修改设备IP", "Modify the device IP");

        formlangTable.put("显示参数视图", "Display Parameter View");
        formlangTable.put("修改只读团体名", "Modify read-only Community");
        formlangTable.put("修改读写团体名", "Modify read-write Community");
        formlangTable.put("手动创建I类设备", "Startup");
        formlangTable.put("自动搜索I类设备", "Startup");
        formlangTable.put("注册该设备", "register the device");       
        formlangTable.put("请输入用户分组的名称", "New Group");
        formlangTable.put("更改用户分组名称", "Change the name of the user sub-group");           




        formlangTable.put("请输入要查找的设备IP或MAC地址", "Please enter the equipment  IP or MAC address need to find");
        formlangTable.put("找不到指定地址的设备", "Equipment can not find the specified address");


        formlangTable.put("该分组包含了子分组或设备，不允许删除", "Does not allow the deletion of the sub-node");
        formlangTable.put("您确定要删除", "Are you sure to delete");
        formlangTable.put("删除用户分组", "Delete user sub-group");
        formlangTable.put("定位不到指定地址的设备", "Can't find the equipment");




        formlangTable.put("设置失败", "Read-only Community set failed");
        formlangTable.put("修改", "Modify");
        formlangTable.put("的只读团体名", "read-only Community");
        formlangTable.put("请在下面的框中输入只读团体名", "Please enter the read-only Community in the following box");
        formlangTable.put("请在下面的框中输入读写团体名", "Please enter the read-write Community in the following box");
        formlangTable.put("的读写团体名", "Reading and writing groups");


        formlangTable.put("设备目前已经失效，无法设置参数", "Equipment has been ineffective, unable to set the parameters");
        formlangTable.put("的IP地址", "IP address");
        formlangTable.put("修改设备", "Modify equipment");
        formlangTable.put("新IP", "New IP");
        formlangTable.put("新网关", "New gateway");
        formlangTable.put("请在下面的框中输入新的设备名称", "Please enter the name of the new equipment in the box below");
        formlangTable.put("创建MAC类设备", "Create a MAC-type equipment");


        formlangTable.put("注册搜索到的设备", "Register equipment searched");
        formlangTable.put("您确定要清空所有未注册设备吗", "Are you sure you want to empty it of all non-registered equipment");



        //comprty
        formlangTable.put("属性名称", "Name");
        formlangTable.put("属性值", "Value");
        formlangTable.put("系统名称", "Name");
        formlangTable.put("系统OID", "OID");
        formlangTable.put("系统描述", "Description");
        formlangTable.put("联系信息", "Contact");
        formlangTable.put("系统方位", "Position");
        formlangTable.put("上电时间", "Power-time");

        //regist device
        formlangTable.put("只读团体名", "RO Community");
        formlangTable.put("读写团体名", "RW Community");
        formlangTable.put("该设备的自定义名称", "Device name");
        formlangTable.put("选择的分组名称", "Selected group");
        formlangTable.put("在此输入该设备的名称", "Input the  new device's name");
        formlangTable.put("将此设备移动到", "Move this  device to");



        //sauth form
        formlangTable.put("超级管理员", "Super Admin");
        formlangTable.put("管理员", "Admin");
        formlangTable.put("观察员", "Guest");
        formlangTable.put("来宾", "Guest");
        formlangTable.put("密码不能为空", "Password can not be empty");
        formlangTable.put("两次密码不一致", "Enter the new password twice inconsistent");
        formlangTable.put("密码必须为数字或者字母，长度介于6-16", "Password with numbers or letters，the length must be in 6~16");

        formlangTable.put("确定删除用户", "To confirm the deletion of this User?");
        formlangTable.put("提示", "Information");
        formlangTable.put("是", "Yes");
        formlangTable.put("否", "No");


       //alarmlog
        formlangTable.put("实时告警", "Real time alarm");
        formlangTable.put("设备参数视图", "Parameter view");
        formlangTable.put("当前用户没有该权限", "No authority");

        formlangTable.put("参数超出允许范围", "Out of range");


        commonlangTable.put("空槽", "Empty slot");
        commonlangTable.put("参数值", "Value");
        commonlangTable.put("请输入有效的参数值", "Invalid parameter");
        commonlangTable.put("获取参数失败", "Failed to get Parameters");
        commonlangTable.put("关", "OFF");
        commonlangTable.put("开", "ON");
        commonlangTable.put("无效", "Invalid");
        commonlangTable.put("无效IP", "Invalid IP");
        commonlangTable.put("无效DNS", "Invalid DNS");
        commonlangTable.put("无效网关", "Invalid Gateway");
        commonlangTable.put("无效子网掩码", "Invalid Subnet mask");
        commonlangTable.put("当前通道", "Current channel");
        commonlangTable.put("步进", "step");
        commonlangTable.put("运行", "Run");
        commonlangTable.put("关闭", "Stop");

        commonlangTable.put("自动", "Auto");

        commonlangTable.put("手动", "Manual");

        commonlangTable.put("AGC使能开", "AGC enable");
        commonlangTable.put("AGC使能关", "AGC disable");
        commonlangTable.put("未知", "Unknown");


        //aut login
        commonlangTable.put("用户名或密码错误", "Incorrect password or Incorrect name！");
        commonlangTable.put("失败", "Failed");
        commonlangTable.put("该用户名已存在", "Account already exists");
        commonlangTable.put("两次密码输入不一致", "The password must be identical to the above/following one");
        commonlangTable.put("成功", "Successful");
        commonlangTable.put("原密码不正确", "Incorrect old password");
        commonlangTable.put("管理员权限", "Admin");
        commonlangTable.put("一般权限", "Guest");
        commonlangTable.put("确定删除该用户组？", "Are you sure to delete this user？");
        commonlangTable.put("权限修改成功", "Successful");


        commonlangTable.put("无需处理", "No need to acknowledge");
        commonlangTable.put("超时默认失效", "Overtime");
        commonlangTable.put("过时失效", "Overtime");
        commonlangTable.put("设备被删除后清空", "Device has been deleted");
        commonlangTable.put("自动恢复", "Automatic recovery");
  

        
        //trap log
        commonlangTable.put("普通信息", "General information");
        commonlangTable.put("紧急告警", "Urgent alarm");
        commonlangTable.put("重要告警", "Secondary alarm");

        commonlangTable.put("已处理", "Acknowledged");
        commonlangTable.put("未处理", "Unacknowledged");
        commonlangTable.put("所有", "All");
        commonlangTable.put("所有告警", "All");

        commonlangTable.put("重启", "Restart");
        commonlangTable.put("小时", "Hour");


  
        //TRAP generical
        trapDiscrlangTable.put("标准冷启动", "Standard cold boot");
        trapDiscrlangTable.put("标准热启动", "Standard warm boot");
        trapDiscrlangTable.put("标准连接断开", "Standard link cut");
        trapDiscrlangTable.put("标准连接成功", "Standard link succeed");
        trapDiscrlangTable.put("标准目标丢失", "Standard lost");

        //nomal hfc trap
        trapDiscrlangTable.put("参数告警", "Parameter Alarm");
        trapDiscrlangTable.put("下线告警", "Offline Alarm");
        trapDiscrlangTable.put("物理地址", "MAC Address");
        trapDiscrlangTable.put("逻辑ID", "Logical ID");
        trapDiscrlangTable.put("类型", "type");
        trapDiscrlangTable.put("参数名称", "Parameter Name");
        trapDiscrlangTable.put("参数值", "value");
        trapDiscrlangTable.put("名称", "name");
        trapDiscrlangTable.put("值", "value");




        trapDiscrlangTable.put("HFC设备冷启动", "HFC equipment cold boot");
        trapDiscrlangTable.put("HFC设备热启动", "HFC equipment warm boot");


        trapDiscrlangTable.put("WOS3000光平台告警", "WOS3000 optical platform alarm");
        trapDiscrlangTable.put("插槽", "Slot");
        trapDiscrlangTable.put("模块名", "Name");
        trapDiscrlangTable.put("输入光功率", "Input power");


        //wos3k
        trapDiscrlangTable.put("WOS光平台重启动", "WOS opitical platform reboot");
        trapDiscrlangTable.put("软件版本", "Sofeware version");
        trapDiscrlangTable.put("子设备上线", "subset equipment online");
        trapDiscrlangTable.put("子设备下线", "subset equipment offline");

       // subname
        trapDiscrlangTable.put("电源", "Power");
        trapDiscrlangTable.put("光发射机", "Optical Transmitters");
        trapDiscrlangTable.put("光接收机", "Optical Receiver");
        trapDiscrlangTable.put("反向光接收机", "Reverse Optical Receiver");
        trapDiscrlangTable.put("光切换开关", "Optical Switch");
        trapDiscrlangTable.put("射频切换开关", "RF Switch");
        trapDiscrlangTable.put("双备份光接收机", "Double backup optical receiver");
        trapDiscrlangTable.put("前置放大器", "Pre-amplifier ");
        trapDiscrlangTable.put("插播光发机", "Inter-cut Transmitters");
        trapDiscrlangTable.put("未知子设备类型", "Unknown");



        //switch
        trapDiscrlangTable.put("通道切换", "switch event");
        trapDiscrlangTable.put("万隆光开关通道切换", "switch event");
        trapDiscrlangTable.put("切换到通道", "switch to channel");


        // trapDiscrlangTable.put("HFC设备热启动", "HFC equipment warm boot");

        //trapDiscrlangTable.put("参数值", "Standard lost");
        //trapDiscrlangTable.put("参数值", "Standard lost");

    }

}
