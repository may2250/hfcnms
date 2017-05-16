package wl.hfc.common;

import java.util.LinkedList;

import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class LNode{	

    public LinkedList<LNode> Nodes;
    public int Level;
    
    public InodeInterface Tag ;
    public LNode parent;

    //public LinkedList<nojuTrapLogTableRow> alarmList { get; set; }
    public String fullpath = "";
    public String ID = "";
    public String MD = "";
    public String SN = "";
    public String DEVICEID = "";
  //  public HFCTypes HFCType1 = HFCTypes.Unknown;


    //确保devgroup和listnode告警标志同步的清晰写法；
    private int _isAlarm = 0;
/*    public int isAlarm
    {
        get { return _isAlarm; }
        set
        {
            _isAlarm = value;
            if (Tag!=null)
            {
                    InodeInterface InodeInterface1 = (InodeInterface)Tag;
                    if (InodeInterface1.isGroup())
                    {
                        devGroup grp = (devGroup)InodeInterface1;
                        grp.isAlarm = _isAlarm;
                    }
                    else
                    {
                        DevTopd dev = (DevTopd)InodeInterface1;
                        dev.isAlarm = _isAlarm;
                    }
            
            }
          
        
        
        }
    }*/
    public int alarmChildNumber = 0;//当前node下面有多少child处于告警状态的跟踪记录


    public int OnlineCount;


  //  public CDeviceParam pm;

    public LNode()
    {
        this.Nodes = new LinkedList<LNode>();
     //   alarmList = new List<NlogType.nojuTrapLogTableRow>();
    }


}
