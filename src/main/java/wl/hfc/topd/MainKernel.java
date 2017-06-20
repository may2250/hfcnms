package wl.hfc.topd;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.Services.ServiceHfcAlarmProcessor;
import com.xinlong.Services.Services_Websocket;
import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.*;


//DevGrpModel将承担拓扑的组建，维护，以及组，设备的增删查改的响应
public class MainKernel {
	private static final String  MAINKERNEL_MESSAGE =  "mainkernel.message";
	private static final String  PARAMKERNEL_MESSAGE =  "paramkernel.message";
	private static Logger log = Logger.getLogger(MainKernel.class);
	public  CDatabaseEngine ICDatabaseEngine1;
	private LNode rootListNode;//设备树总节点（虚拟）
	public Hashtable listDevHash = new Hashtable();
    public Hashtable listGrpHash = new Hashtable();
    
    public static MainKernel me;
    public MainKernel()
    {
    	me = this;
    }
    public MainKernel(CDatabaseEngine pICDatabaseEngine)
    {

        this.ICDatabaseEngine1 = pICDatabaseEngine;
        me = this;
    }
    
   // private static Logger log = Logger.getLogger(MainKernel.class);
	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		MainKernel.redisUtil = redisUtil;
	}
	
	public static void setStaticMemory(StaticMemory staticmemory) {
		MainKernel.staticmemory = staticmemory;
	}
	
	private   JedisPubSub jedissubSub = new JedisPubSub() {
		public void onUnsubscribe(String arg0, int arg1) {

        }
		public void onSubscribe(String arg0, int arg1) {

        }
		 public void onMessage(String arg0, String arg1) {
	       
	     }
		 public void onPUnsubscribe(String arg0, int arg1) {

	        }
		 public void onPSubscribe(String arg0, int arg1) {

	        } 

      public void onPMessage(String arg0, String arg1, String msg) {
      	try {  			
  			servicestart(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();	
  			log.info(e.getMessage());
  		}
  		
      }

	};
	
	private void servicestart(String message) throws InterruptedException, ParseException, IOException{
		System.out.println(" [x] MainKernel Received: '" + message + "'");			
		JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		String cmd = jsondata.get("cmd").toString();
		JSONObject rootjson = new JSONObject();
		if(cmd.equalsIgnoreCase("getLoginInit")){	
			staticmemory.sendRemoteStr(getInitTree(rootjson), jsondata.get("sessionid").toString());	
			staticmemory.sendRemoteStr(getInitLog(rootjson), jsondata.get("sessionid").toString());	
		}else if(cmd.equalsIgnoreCase("getgrouptree")){
			staticmemory.sendRemoteStr(getGroupTree(rootjson), jsondata.get("sessionid").toString());
		}else if(cmd.equalsIgnoreCase("nodeadd")){
			handleInsertGrp(jsondata);			
		}else if(cmd.equalsIgnoreCase("nodeedit")){
			if(jsondata.get("type").toString().equalsIgnoreCase("group")){
				handleUpdGrp(jsondata);
			}else{
				handleUpdateDev(jsondata);
			}		
		}else if(cmd.equalsIgnoreCase("nodedel")){
			if(jsondata.get("type").toString().equalsIgnoreCase("group")){
				handleDelGrp(jsondata);
			}else{
				handleDeleteDev(jsondata);
			}			
		}else if(cmd.equalsIgnoreCase("deviceadd")){
			handleInsertDev(jsondata);
		}else if(cmd.equalsIgnoreCase("lazyLoad")){
			staticmemory.sendRemoteStr(getLazyNodes(jsondata), jsondata.get("sessionid").toString());					
		}else if(cmd.equalsIgnoreCase("devstatus")){			
			staticmemory.broadCast(handleOnlineInfo(jsondata));				
		}else if(cmd.equalsIgnoreCase("alarm_message")){			
			staticmemory.broadCast(message);				
		}
	}
	
	public static String getNetTypeTostring(NetTypes pNetTypes)
    {
        switch (pNetTypes)
        {
            case other:
                return ClsLanguageExmp.viewGet("其他设备");
            case EDFA:
                return "EDFA";
            case Trans:
                return ClsLanguageExmp.viewGet("光发射机");
            case rece_workstation:
                return ClsLanguageExmp.viewGet("光接收机")+"/"+ClsLanguageExmp.viewGet("光工作站");
            case OSW:
                return ClsLanguageExmp.viewGet("光切换开关");
            case RFSW:
                return ClsLanguageExmp.viewGet("射频切换开关");
            case PreAMP:
                return ClsLanguageExmp.viewGet("前置放大器");
            case wos:
                return ClsLanguageExmp.viewGet("光平台");
            default:
                return ClsLanguageExmp.viewGet("其他设备");

        }
    }
	
	public static NetTypes getStringToNetType(String pNetTypes)
    {
        switch (pNetTypes)
        {
            case "other":
                return NetTypes.other;
            case "EDFA":
                return NetTypes.EDFA;
            case "Trans":
                return NetTypes.Trans;
            case "rece_workstation":
                return NetTypes.rece_workstation;
            case "OSW":
                return NetTypes.OSW;
            case "RFSW":
                return NetTypes.RFSW;
            case "PreAMP":
                return NetTypes.PreAMP;
            case "wos":
                return NetTypes.wos;
            default:
                return NetTypes.other;

        }
    }
  	
    
    @SuppressWarnings("static-access")
	public void start(){
		
		log.info("[#3] .....MainKernel starting.......");
		Jedis jedis=null;
		try {			
			ICDatabaseEngine1=new CDatabaseEngine();
			ICDatabaseEngine1.getConnection();
			initTopodData();
			CurrentAlarmModel cam = new CurrentAlarmModel(ICDatabaseEngine1, redisUtil);
			cam.start();
			jedis = redisUtil.getConnection();		 
			jedis.psubscribe(jedissubSub, MAINKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis); 
			  
		}catch(Exception e){
			e.printStackTrace();
			log.info(e.getMessage());
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
    
    private String getInitTree(JSONObject rootjson){
    	rootjson.put("cmd", "getInitTree");
		JSONArray jsonarray = new JSONArray();
		//获取设备树结构
		jsonarray = getSubTree(rootListNode);
		rootjson.put("treenodes", jsonarray);
		String jsonString = rootjson.toJSONString();
		System.out.println("jsonString==" + jsonString);
		return jsonString;
    }
    
    private String getGroupTree(JSONObject rootjson){
    	rootjson.put("cmd", "getgrouptree");
		JSONArray jsonarray = new JSONArray();
		//获取设备树结构
		jsonarray = getSubGroup(rootListNode);
		rootjson.put("treenodes", jsonarray);
		String jsonString = rootjson.toJSONString();
		System.out.println("jsonString==" + jsonString);
		return jsonString;
    }
    
    private JSONArray getSubGroup(LNode pnode){
    	JSONObject subjson;
    	JSONArray jsonarray = new JSONArray();
    	for(Iterator iter = pnode.Nodes.iterator(); iter.hasNext();){
			LNode node = (LNode)iter.next();
			InodeInterface InodeInterface1 = (InodeInterface)node;
			if (InodeInterface1.isGroup())
            {
				subjson = new JSONObject();
				devGroup group = (devGroup)InodeInterface1;
				UserGroupTableRow usergroup = group.BindUserGroupTableRow;
				subjson.put("key", usergroup.UserGroupID);
				subjson.put("pkey", usergroup.ParentGroupID);
				subjson.put("title", usergroup.UserGroupName);
				subjson.put("type", "group");
				subjson.put("isFolder", true);
				subjson.put("expand", true);
				subjson.put("icon", "images/net_center.png");	

				JSONArray subjsonarray = new JSONArray();
				subjsonarray = getSubGroup(node);
				subjson.put("children", subjsonarray);			
				jsonarray.add(subjson);
            }
		}
    	return jsonarray;
    }
    
    private String getInitLog(JSONObject rootjson){
    	JSONObject logjson = new JSONObject();
    	rootjson.put("cmd", "getInitLog");
		JSONArray jsonarray = new JSONArray();
		//获取发往WEB的设备告警及日志信息
		//TODO
		//test alarms
		
		logjson.put("DT_RowId", "row_1");
		logjson.put("id", "1");
		logjson.put("level", "1");
		logjson.put("source", "grp1/xxxx");
		logjson.put("path", "grp1/xxxx");
		logjson.put("type", "warn");
		logjson.put("paramname", "name");
		logjson.put("paramvalue", "grp1");
		logjson.put("eventtime", "2017-5-22");
		logjson.put("solved", "yes");
		logjson.put("solvetime", "2017-5-22");
		jsonarray.add(logjson);
		logjson = new JSONObject();
		logjson.put("DT_RowId", "row_2");
		logjson.put("id", "2");
		logjson.put("level", "2");
		logjson.put("source", "grp1/xxxx");
		logjson.put("path", "grp1/xxxx");
		logjson.put("type", "warn");
		logjson.put("paramname", "name");
		logjson.put("paramvalue", "grp1");
		logjson.put("eventtime", "2017-5-22");
		logjson.put("solved", "yes");
		logjson.put("solvetime", "2017-5-22");
		jsonarray.add(logjson);
		rootjson.put("alarms", jsonarray);
		//test logs
		logjson = new JSONObject();
		logjson.put("id", "1");
		logjson.put("type", "test");
		logjson.put("content", "test log!!");
		logjson.put("time", "2017-5-22");
		jsonarray = new JSONArray();
		jsonarray.add(logjson);
		rootjson.put("logs", jsonarray);
		return rootjson.toJSONString();
    }
    
    private JSONArray getSubTree(LNode pnode){
    	JSONObject subjson;
    	JSONArray jsonarray = new JSONArray();
    	for(Iterator iter = pnode.Nodes.iterator(); iter.hasNext();){
			LNode node = (LNode)iter.next();
			InodeInterface InodeInterface1 = (InodeInterface)node;
			if (InodeInterface1.isGroup())
            {
				subjson = new JSONObject();
				devGroup group = (devGroup)InodeInterface1;
				UserGroupTableRow usergroup = group.BindUserGroupTableRow;
				subjson.put("key", usergroup.UserGroupID);
				subjson.put("pkey", usergroup.ParentGroupID);
				subjson.put("title", usergroup.UserGroupName);
				subjson.put("type", "group");
				subjson.put("isFolder", true);
				subjson.put("expand", true);
				subjson.put("isAlarm", group.isAlarm == 0?false:true);
				subjson.put("icon", "images/net_center.png");	
				//第三级开始启用延迟加载功能
				if(usergroup.ParentGroupID == -1){
					JSONArray subjsonarray = new JSONArray();
					subjsonarray = getSubTree(node);
					subjson.put("lazy", false);
					subjson.put("children", subjsonarray);
				}else{
					subjson.put("lazy", true);
				}
				
				jsonarray.add(subjson);
            }else{
            	DevTopd dev = (DevTopd)InodeInterface1;
            	JSONArray subjsonarray = new JSONArray();
            	subjson = new JSONObject();
            	JSONObject infojson = new JSONObject();
            	subjson.put("key", dev._NetAddress);
				subjson.put("pkey", dev.BindnojuDeviceTableRow.UserGroupID);
				subjson.put("title", dev.BindnojuDeviceTableRow.Name);
				subjson.put("type", "device");
				subjson.put("rcommunity", dev.BindnojuDeviceTableRow._ROCommunity);
				subjson.put("wcommunity", dev.BindnojuDeviceTableRow._RWCommunity);
				subjson.put("isonline", dev.isOline);
				subjson.put("icon", dev.isOline?"images/device.png":"images/devoff.png");         	
				infojson.put("title", dev._NetAddress);						
				infojson.put("icon", "images/net_info.png");
				subjsonarray.add(infojson);
				infojson = new JSONObject();
				infojson.put("key", dev.mNetType.toString());
				infojson.put("title", getNetTypeTostring(dev.mNetType));
				infojson.put("hfctype", dev.HFCType1.toString());
				infojson.put("icon", "images/net_info.png");
				subjsonarray.add(infojson);
				subjson.put("children", subjsonarray);
				jsonarray.add(subjson);
            }
		}
    	return jsonarray;
    }
    
    private String getLazyNodes(JSONObject jsondata){
    	JSONObject subjson;
    	JSONObject rootjson = new JSONObject();
    	JSONArray jsonarray = new JSONArray();
    	rootjson.put("cmd", "lazyLoad");
		rootjson.put("key", jsondata.get("key").toString());
    	int usergroupID= Integer.parseInt(jsondata.get("key").toString());//get  goupid from jsondata
    	try{
    		devGroup grp = (devGroup)listGrpHash.get(usergroupID); 
    		rootjson.put("lazyNodes", getSubTree(grp));          	
    	}catch(Exception ex){
    		log.info(ex.getMessage());
    	}
    	return rootjson.toJSONString();
    }


	public  void initTopodData() {
		Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
		Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();		
		// List<CDataBasePropery.nojuDeviceTableRow> SlotRowsList =
		// ICDatabaseEngine1.slotTableGetAllRows();
		rootListNode = this.offerTopodModel(devHash, grpHash);			
		
		//print rootListNode;
	}

	

	// by group and device collection args
	private  LNode offerTopodModel(Hashtable devLists, Hashtable grpLists) {
		
		listDevHash.clear();
		listGrpHash.clear();
		LNode result = new LNode();
        result.fullpath="设备树";
		result.Level = 0;
		createTree(devLists, grpLists,result);
		return result;

	}

	private void createTree(Hashtable devLists, Hashtable groupLists, LNode rootNode) {

		LinkedList<UserGroupTableRow> rows = new LinkedList<UserGroupTableRow>();

		Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow item = (UserGroupTableRow) e.nextElement();

			if (item.ParentGroupID == -1) {
				rows.add(item);
			}

		}
		// select the all root nodes
		for (UserGroupTableRow dr : rows) {
			// add the child group and the device
			devGroup group = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
			group.BindUserGroupTableRow = dr;
	
			group.Level=1;
			rootNode.Nodes.add(group);
			group.parent=rootNode;
			group.fullpath=rootNode.fullpath + "/" + group.BindUserGroupTableRow.UserGroupName;
			group.Tag = group;
			log.info(group.fullpath);
            listGrpHash.put(group.BindUserGroupTableRow.UserGroupID, group);

			CreateTreeNode(group, groupLists, devLists);

		}

	}

	
    private void CreateTreeNode(devGroup pgroup,Hashtable groupLists, Hashtable devLists)
    {
        //select all the child row in the grouptable
    	LinkedList<UserGroupTableRow> rows = new LinkedList<UserGroupTableRow>(); 	
    	
    	
		Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow dr = (UserGroupTableRow) e.nextElement();
            if (dr.ParentGroupID == pgroup.BindUserGroupTableRow.UserGroupID)
            {

                //add the group as child
        
                devGroup newgroup = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
         /*       newgroup.x1 = dr.x1;
                newgroup.x2 = dr.x2;
                newgroup.y1 = dr.y1;
                newgroup.y2 = dr.y2;
                newgroup.isTx = dr.isTx;*/
               // newgroup.name = dr.UserGroupName;
                newgroup.BindUserGroupTableRow = dr;
                
                newgroup.Level=pgroup.Level+1;
                pgroup.Nodes.add(newgroup);
                newgroup.parent=pgroup;
                newgroup.fullpath=pgroup.fullpath + "/" + newgroup.BindUserGroupTableRow.UserGroupName;
                newgroup.Tag = newgroup;
        		System.out.println(	newgroup.fullpath);
        	    listGrpHash.put(newgroup.BindUserGroupTableRow.UserGroupID, newgroup);
                CreateTreeNode(newgroup, groupLists, devLists);
            }

		}

    	
 
        //add the device to this group
        //List<CDataBasePropery.nojuDeviceTableRow> rows2 = new List<CDataBasePropery.nojuDeviceTableRow>();
       
		   	
		e = devLists.elements();

		while (e.hasMoreElements()) {
			nojuDeviceTableRow dr1 = (nojuDeviceTableRow) e.nextElement();
            if (dr1.UserGroupID == pgroup.BindUserGroupTableRow.UserGroupID)
            {
       
                
                DevTopd dev = new DevTopd(dr1); 
                dev.BindnojuDeviceTableRow = dr1;
                
                dev.Level=pgroup.Level+1;
                pgroup.Nodes.add(dev);
                dev.parent=dev;
                dev.fullpath = pgroup.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;
                dev.Tag = dev;
        		System.out.println(	dev.fullpath);
         /*       device.x1 = dr1.x1;
                device.x2 = dr1.x2;
                device.y1 = dr1.y1;
                device.y2 = dr1.y2;
                device.isTx = dr1.isTx;*/
               // device.name = dr1.Name;

                // UserGroup newgroup = new UserGroup((int)dr["UserGroupID"], (string)dr["UserGroupName"], (int)dr["ParentGroupID"]);
                listDevHash.put(dev._NetAddress, dev);
                dev.isOline = false;
                dev.OnlineCount = 0;

            }
        }

    }
		
	
    public boolean handleInsertGrp(JSONObject jsondata)
    {
    	boolean mStatus =false;
    	JSONObject rootjson = new JSONObject();
    	
    	////get the  group information from  jsondata ,build a new UserGroupTableRow ***************
    	UserGroupTableRow mDevGrpTableRow =new UserGroupTableRow(0, jsondata.get("value").toString(),  Integer.parseInt(jsondata.get("key").toString()));
    	
    	System.out.println(jsondata.get("value").toString());

        if (this.ICDatabaseEngine1.UserGroupTableInsertRow(mDevGrpTableRow) > 0)
        {
            mStatus = true;
        }

        if (mStatus)
        {
            LNode pGrp;
            if (mDevGrpTableRow.ParentGroupID == -1)
            {
                pGrp = this.rootListNode;
            }
            else
            {
                pGrp = (LNode)listGrpHash.get(mDevGrpTableRow.ParentGroupID);
         /*       if (pGrp == null)//设备组不存在
                {
                    return null;
                }*/
            }

            UserGroupTableRow dr = mDevGrpTableRow;

   
            devGroup  rootGroup = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
            rootGroup.BindUserGroupTableRow = dr;
            rootGroup.parent = pGrp;
            rootGroup.fullpath = pGrp.fullpath + "/" + rootGroup.BindUserGroupTableRow.UserGroupName;
            rootGroup.Level = rootGroup.parent.Level + 1;

            listGrpHash.put(rootGroup.BindUserGroupTableRow.UserGroupID, rootGroup);
            pGrp.Nodes.add(rootGroup);




            //  this.Notify("DbEngine.grpOP", cmd);//notify runtimedev
            
            rootjson.put("cmd", "nodeadd");
			rootjson.put("key", mDevGrpTableRow.UserGroupID);//node UserGroupID
			rootjson.put("pkey", jsondata.get("key").toString()); //ParentGroupID
			rootjson.put("title", jsondata.get("value").toString());//UserGroupName
			rootjson.put("type", "group");
			rootjson.put("isFolder", true);
			rootjson.put("expand", true);
			rootjson.put("icon", "images/net_center.png");
			staticmemory.broadCast(rootjson.toJSONString());

        }
        
        return mStatus;

    }

    public boolean handleDelGrp(JSONObject jsondata)
    {
      	boolean mStatus =false;
      	
      	
    	int usergroupID= Integer.parseInt(jsondata.get("key").toString());//get  goupid from jsondata
      	devGroup delgrp = (devGroup)listGrpHash.get(usergroupID); 


    	UserGroupTableRow mDevGrpTableRow = delgrp.BindUserGroupTableRow;    	

    	
    	
 /*       if (!this.ICDatabaseEngine1.clearUserDevGrpInfo("", mDevGrpTableRow.UserGroupID))
        {
            return false;
        }
        if (!this.ICDatabaseEngine1.clearElemetTableByGID(cmd.mDevGrpTableRow.UserGroupID))
        {
            return false;
        }
*/

        mStatus = this.ICDatabaseEngine1.UserGroupTableDeleteRow(mDevGrpTableRow.UserGroupID);
        if (mStatus)
        {
            LNode pGrp;
            if (mDevGrpTableRow.ParentGroupID == -1)
            {
                pGrp = this.rootListNode;
            }
            else
            {
                pGrp = (LNode)listGrpHash.get(mDevGrpTableRow.ParentGroupID);
            }

            pGrp.Nodes.remove(delgrp);
            listGrpHash.remove(mDevGrpTableRow.UserGroupID);
            
            JSONObject rootjson = new JSONObject();
            rootjson.put("cmd", "nodedel");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("pkey", jsondata.get("pkey").toString());
			staticmemory.broadCast(rootjson.toJSONString());
            return true;

        }else{
        	log.info("Del Grp :" + mDevGrpTableRow.UserGroupName + " Error!");
        }


        return false;


    }

    public boolean handleUpdGrp(JSONObject jsondata)
    {
    	boolean mStatus =false;
    	
    	int userid= Integer.parseInt(jsondata.get("key").toString());//get 组ID from jsondata
      	devGroup rootGroup = (devGroup)listGrpHash.get(userid); 
      	
    	UserGroupTableRow mDevGrpTableRow = rootGroup.BindUserGroupTableRow;
    	
    	mDevGrpTableRow.UserGroupName = jsondata.get("value").toString();
    	
        mStatus = this.ICDatabaseEngine1.UserGroupTableUpdateRow(mDevGrpTableRow);
        if (mStatus)
        {

           
            rootGroup.BindUserGroupTableRow = mDevGrpTableRow;
         //   rootGroup.name = rootGroup.BindUserGroupTableRow.UserGroupName;
            rootGroup.fullpath = rootGroup.parent.fullpath + "/" + rootGroup.BindUserGroupTableRow.UserGroupName;
            //Hashtable effectPathList = new Hashtable();
            //effectPathList.Add(rootGroup.BindUserGroupTableRow.UserGroupID.ToString(), grp.fullpath);
          //  reflashPath(grp, effectPathList);
            JSONObject rootjson = new JSONObject();
        	rootjson.put("cmd", "nodeedit");
    		rootjson.put("key", jsondata.get("key").toString());
    		rootjson.put("title", jsondata.get("value").toString());
    		rootjson.put("type", "group");
    		staticmemory.broadCast(rootjson.toJSONString());
            return true;
        }

        return false;
    }



    public DevTopd handleInsertDev(JSONObject jsondata)
    {      	
    	boolean mStatus =false;  	
      	int usergroupID=Integer.parseInt(jsondata.get("key").toString());//get  goupid from jsondata

      	devGroup grp = (devGroup)listGrpHash.get(usergroupID); 
      	String devtypestr = jsondata.get("devtype").toString();

        if (grp == null)//父设备组不存在
        {
            return null;
        }

        nojuDeviceTableRow mDeviceTableRow=new nojuDeviceTableRow(jsondata.get("netip").toString(), getStringToNetType(devtypestr));
        mDeviceTableRow.UserGroupID=usergroupID;
        mDeviceTableRow._ROCommunity = jsondata.get("rcommunity").toString();
        mDeviceTableRow._RWCommunity = jsondata.get("wcommunity").toString();
        mDeviceTableRow.Name = jsondata.get("devname").toString();
        
        mStatus = this.ICDatabaseEngine1.DeviceTableInsertRow(mDeviceTableRow);
        if (mStatus)
        {
            DevTopd dev = new DevTopd(mDeviceTableRow);         
            dev.isOline = false;


            dev.parent = grp;
            dev.fullpath = grp.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;

            dev.OnlineCount = 0;
          //  dev.Pm = null;

            listDevHash.put(dev._NetAddress, dev);
            grp.Nodes.add(dev);
            JSONArray jsonarray = new JSONArray();
            JSONArray subjsonarray = new JSONArray();
            JSONObject rootnodejson = new JSONObject();
            JSONObject rootjson = new JSONObject();
            JSONObject subjson = new JSONObject();
        	rootjson.put("cmd", "deviceadd");
        	rootjson.put("pkey", jsondata.get("key").toString());
        	rootnodejson.put("key", dev._NetAddress);
        	rootnodejson.put("pkey", jsondata.get("key").toString());
        	rootnodejson.put("title", dev.BindnojuDeviceTableRow.Name);
        	rootnodejson.put("type", "device");
        	rootnodejson.put("rcommunity", dev.BindnojuDeviceTableRow._ROCommunity);
        	rootnodejson.put("wcommunity", dev.BindnojuDeviceTableRow._RWCommunity);
        	rootnodejson.put("isonline", dev.isOline);
        	rootnodejson.put("icon", dev.isOline?"images/device.png":"images/devoff.png");         	
    		subjson.put("title", dev._NetAddress);	
    		subjson.put("hfctype", dev.HFCType1.toString());	
			subjson.put("icon", "images/net_info.png");
			subjsonarray.add(subjson);
			subjson = new JSONObject();	
			subjson.put("key", dev.mNetType.toString());
			subjson.put("title", getNetTypeTostring(dev.mNetType));
			subjson.put("icon", "images/net_info.png");
			subjsonarray.add(subjson);
			rootnodejson.put("children", subjsonarray);
			jsonarray.add(rootnodejson);
			rootjson.put("devnodes", jsonarray);
			
    		staticmemory.broadCast(rootjson.toJSONString());
            return dev;

        }

        return null;
    }

    public boolean handleUpdateDev(JSONObject jsondata)
    {
    	boolean mStatus =false;
    	
    	String netaddr= jsondata.get("key").toString();//get  netaddr from jsondata
      	DevTopd dev = (DevTopd)listDevHash.get(netaddr); 

      	if (dev==null) {
      		
      		return false;
			
		}
      	
    	nojuDeviceTableRow mDeviceTableRow = dev.BindnojuDeviceTableRow;
    	
        //edit the  mDeviceTableRow property here from jsondata    	
     	mDeviceTableRow.Name= jsondata.get("title").toString();
     	mDeviceTableRow._ROCommunity = jsondata.get("rcommunity").toString();
     	mDeviceTableRow._RWCommunity = jsondata.get("wcommunity").toString();
        mStatus = this.ICDatabaseEngine1.DeviceTableUpdateRow(mDeviceTableRow);

        if (mStatus)
        {

        	dev.BindnojuDeviceTableRow = mDeviceTableRow;

        	dev.fullpath = dev.parent.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;

    		staticmemory.broadCast(jsondata.toJSONString());

        }


        return mStatus;


    }

    public boolean handleDeleteDev(JSONObject jsondata)
    {

      	boolean mStatus =false;
      	
      	
    	String devAddr=jsondata.get("key").toString();//get  ip from jsondata
      	DevTopd  delDev = (DevTopd)listDevHash.get(devAddr); 
      	
    	if (delDev==null) {
      		
      		return false;
			
		}

/*        if (!this.ICDatabaseEngine1.clearElemetTableByLinked(cmd.mDeviceTableRow.NetAddress))
        {
            return false;
        }

        
        if (cmd.mDeviceTableRow.NetType==NetTypes.wos)
        {
            if (!this.ICDatabaseEngine1.clearSlotTableByNetAddress(cmd.mDeviceTableRow.NetAddress))
            {
                return false;
            }
        }*/


        mStatus = this.ICDatabaseEngine1.DeviceTableDeleteRow(delDev.BindnojuDeviceTableRow);
  

        if (mStatus)
        {
        	DevTopd dev = (DevTopd)listDevHash.get(delDev.BindnojuDeviceTableRow.get_NetAddress());
            devGroup grp = (devGroup)listGrpHash.get(delDev.BindnojuDeviceTableRow.UserGroupID);
            grp.Nodes.remove(dev);
            listDevHash.remove(delDev.BindnojuDeviceTableRow.get_NetAddress());

            JSONObject rootjson = new JSONObject();
            rootjson.put("cmd", "nodedel");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("pkey", jsondata.get("pkey").toString());
			staticmemory.broadCast(rootjson.toJSONString());
        }

        return mStatus;
    }

    private String handleOnlineInfo(JSONObject jsondata)
    {    	
    	String ipaddr=jsondata.get("ip").toString();//GET FROM JOSNDATA

        DevTopd lNode = (DevTopd)listDevHash.get(ipaddr);

        if (lNode == null)
        {
            return jsondata.toJSONString();
        }

       //hi，xinglong，HFCType1，ID,MD,SN,DEVICEID,ISONLINE?这些信息请提交到前端
        
    /*    ScMessage scMessage = new ScMessage(dev.IsOline, lNode.HFCType1, dev.NetAddress);
        scMessage.ID = lNode.ID;
        scMessage.MD = lNode.MD;
        scMessage.SN = lNode.SN;
        scMessage.DEVICEID = lNode.DEVICEID;


        OlineeInforCmd OlineeInforCmd1 = new OlineeInforCmd(scMessage, CMDType.OLINE_INFO);
        OlineeInforCmd1.mTimeStamp = DateTime.Now.Ticks;
        this.Notify("DbEngine.newCMD", OlineeInforCmd1);
*/
        jsondata.put("hfctype", lNode.HFCType1.toString());
        jsondata.put("id", lNode.ID);
        jsondata.put("md", lNode.MD);
        jsondata.put("sn", lNode.SN);
        log.info("------->>>" + jsondata.toJSONString());
        return jsondata.toJSONString();
    }
    
    public void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(queue, msg);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());

		}finally{
			redisUtil.closeConnection(jedis);
		}
	}    
    
}
