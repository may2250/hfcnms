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
import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.hfc.common.*;


//DevGrpModel将承担拓扑的组建，维护，以及组，设备的增删查改的响应
public class MainKernel {
	private static final String  MAINKERNEL_MESSAGE =  "mainkernel.message";
	public  CDatabaseEngine ICDatabaseEngine1;
	private LNode rootListNode;//设备树总节点（虚拟）
	public Hashtable listDevHash = new Hashtable();
    public Hashtable listGrpHash = new Hashtable();
    
    public static MainKernel me;
    public MainKernel()
    {

    }
    public MainKernel(CDatabaseEngine pICDatabaseEngine)
    {

        this.ICDatabaseEngine1 = pICDatabaseEngine;
        me = this;
    }
    
    private static Logger log = Logger.getLogger(MainKernel.class);
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
		if(cmd.equalsIgnoreCase("getInitTree")){			
			Session ses = staticmemory.getSessionByID(jsondata.get("sessionid").toString());
			if(ses != null){
				ses.getBasicRemote().sendText(getInitTree(rootjson));
			}else{
				System.out.println("No Session Found::::");
			}
		}else if(cmd.equalsIgnoreCase("nodeadd")){
			rootjson.put("cmd", "nodeadd");
			rootjson.put("key", "1");//node UserGroupID
			rootjson.put("pkey", jsondata.get("key").toString()); //ParentGroupID
			rootjson.put("title", jsondata.get("value").toString());//UserGroupName
			rootjson.put("type", "group");
			rootjson.put("isFolder", true);
			rootjson.put("expand", true);
			rootjson.put("icon", "images/net_center.png");
			staticmemory.broadCast(rootjson.toJSONString());
		}else if(cmd.equalsIgnoreCase("nodeedit")){
			rootjson.put("cmd", "nodeedit");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("title", jsondata.get("value").toString());
			rootjson.put("type", "group");
			rootjson.put("isFolder", true);
			rootjson.put("expand", true);
			rootjson.put("icon", "images/net_center.png");
			staticmemory.broadCast(rootjson.toJSONString());
		}else if(cmd.equalsIgnoreCase("nodedel")){
			rootjson.put("cmd", "nodedel");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("pkey", jsondata.get("pkey").toString());
			staticmemory.broadCast(rootjson.toJSONString());
		}else if(cmd.equalsIgnoreCase("lazyLoad")){
			rootjson.put("cmd", "lazyLoad");
			rootjson.put("key", jsondata.get("key").toString());
			JSONArray jsonarray = new JSONArray();
			JSONObject sysjson = new JSONObject();
			sysjson.put("key", "3");
			sysjson.put("pkey", jsondata.get("key").toString());			
			sysjson.put("title", "LazyLoadNode");
			sysjson.put("type", "device");
			sysjson.put("isFolder", true);
			sysjson.put("expand", false);
			sysjson.put("icon", "images/device.png");
			JSONArray subjsonarray = new JSONArray();
			JSONObject subjson = new JSONObject();
			subjson.put("title", "192.168.1.120");	
			subjson.put("icon", "images/net_info.png");
			subjson.put("isFolder", false);
			subjsonarray.add(subjson);
			subjson = new JSONObject();
			subjson.put("title", "光发射机");
			subjson.put("icon", "images/net_info.png");
			subjson.put("isFolder", false);
			subjsonarray.add(subjson);
			sysjson.put("children", subjsonarray);
			jsonarray.add(sysjson);
			rootjson.put("lazynodes", jsonarray);
			Session ses = staticmemory.getSessionByID(jsondata.get("sessionid").toString());
			if(ses != null){
				ses.getBasicRemote().sendText(rootjson.toJSONString());
			}else{
				System.out.println("No Session Found::::");
			}			
		}else if(cmd.equalsIgnoreCase("hfcvalueset")){
			hfcValueSet(jsondata);			
		}
	}
	
	private void hfcValueSet(JSONObject jsondata){
		JSONObject rootjson = new JSONObject();
		String target = jsondata.get("target").toString();
		if(target.equalsIgnoreCase("devicetrapedit")){
			//修改设备Trap主机地址
			rootjson.put("cmd", "hfcvalueset");
			rootjson.put("target", "devicetrapedit");
			rootjson.put("domstr", jsondata.get("domstr").toString());
			rootjson.put("value", jsondata.get("value").toString());
			//TODO
			//发送到设备
			
		}else if(target.equalsIgnoreCase("devicechannel")){
			//修改设备频道数
			rootjson.put("cmd", "hfcvalueset");
			rootjson.put("target", "devicechannel");
			rootjson.put("domstr", jsondata.get("domstr").toString());
			rootjson.put("value", jsondata.get("value").toString());
			//TODO
			//发送到设备
			
		}
		Session ses = staticmemory.getSessionByID(jsondata.get("sessionid").toString());
		if(ses != null){
			try {
				ses.getBasicRemote().sendText(rootjson.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.info(e.getMessage());
			}
		}else{
			System.out.println("No Session Found::::");
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
			jedis = redisUtil.getConnection();		 
			jedis.psubscribe(jedissubSub, MAINKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis); 
			  
		}catch(Exception e){
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);
			
		}
		
	}
    
    private String getInitTree(JSONObject rootjson){
    	rootjson.put("cmd", "getInitTree");
		JSONArray jsonarray = new JSONArray();
		//获取设备树结构
		/*for(Iterator iter = rootListNode.Nodes.iterator(); iter.hasNext();){
			LNode node = (LNode)iter.next();
			InodeInterface InodeInterface1 = (InodeInterface)node;
			if (InodeInterface1.isGroup())
            {
				devGroup group = (devGroup)InodeInterface1;
				UserGroupTableRow usergroup = group.BindUserGroupTableRow;
				sysjson.put("key", usergroup.UserGroupID);
				sysjson.put("pkey", usergroup.ParentGroupID);
				sysjson.put("title", usergroup.UserGroupName);
				sysjson.put("type", "group");
				sysjson.put("isFolder", true);
				sysjson.put("expand", true);
				sysjson.put("isAlarm", group.isAlarm == 0?false:true);				
				sysjson.put("icon", "images/net_center.png");
				JSONArray subjsonarray = getSubTree(node, sysjson,jsonarray);
				if(!subjsonarray.isEmpty()){
					sysjson.put("lazy", false);
					sysjson.put("children", subjsonarray);
				}else{
					sysjson.put("lazy", true);
				}
				jsonarray.add(sysjson);
            }
		}*/
		jsonarray = getSubTree(rootListNode);
		rootjson.put("treenodes", jsonarray);
		String jsonString = rootjson.toJSONString();
		return jsonString;
    }
    
    private JSONArray getSubTree(LNode pnode){
    	JSONObject subjson = new JSONObject();
    	JSONArray jsonarray = new JSONArray();
    	for(Iterator iter = pnode.Nodes.iterator(); iter.hasNext();){
			LNode node = (LNode)iter.next();
			InodeInterface InodeInterface1 = (InodeInterface)node;
			if (InodeInterface1.isGroup())
            {
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
				JSONArray subjsonarray = new JSONArray();
				subjsonarray = getSubTree(node);
				if(!subjsonarray.isEmpty()){
					subjson.put("lazy", false);
					subjson.put("children", subjsonarray);
				}else{
					subjson.put("lazy", true);
				}
				jsonarray.add(subjson);
            }
		}
    	return jsonarray;
    }


	public  void initTopodData() {
		Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
		Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();
		// List<CDataBasePropery.nojuDeviceTableRow> SlotRowsList =
		// ICDatabaseEngine1.slotTableGetAllRows();
		rootListNode = this.offerTopodModel(devHash, grpHash);			

		System.out.println("Have Init rootListNode::::size===" + rootListNode.Nodes.size());
		
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
			System.out.println(	group.fullpath);
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
		
	
    public devGroup handleInsertGrp(Object message)
    {
    	 
    	 Object msgObject=message;         

    	 return new devGroup(1, "", -1);
    }


   
	
}
