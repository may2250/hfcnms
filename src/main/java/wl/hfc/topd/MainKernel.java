package wl.hfc.topd;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;
import com.xinlong.util.UserSession;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.*;
import wl.hfc.common.NlogType.AuthResult;
import wl.hfc.online.PDUServer;
import wl.hfc.online.ParamKernel;
import wl.hfc.online.Realtime_param_call;
import wl.hfc.server.Sstatus;
import wl.hfc.traprcss.TrapPduServer;

public class MainKernel {
	private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
	private static final String HFCALARM_MESSAGE = "currentalarm.message";
	private static Logger log = Logger.getLogger(MainKernel.class);
	public static MainKernel me;
	


	@SuppressWarnings("rawtypes")
	public Hashtable listDevHash = new Hashtable();
	
	@SuppressWarnings("rawtypes")
	public Hashtable listGrpHash = new Hashtable();

	private boolean isTopodInit;
	private CDatabaseEngine ICDatabaseEngine1;
	private LNode rootListNode;

	public MainKernel() {

		// Integer xx=null;
		// log.info(xx.toString());
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

	private JedisPubSub jedissubSub = new JedisPubSub() {
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
				phraseMSG(msg);

			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
			}

		}

	};

	private void phraseMSG(String message) throws InterruptedException, ParseException, IOException {
		System.out.println(" [x] MainKernel Received: '" + message + "'");
		
		
		JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		String cmd = jsondata.get("cmd").toString();
		JSONObject rootjson = new JSONObject();

		if (!this.isTopodInit) {
			staticmemory.sendRemoteStr(getDBstatus(), jsondata.get("sessionid").toString());
			return;

		}
		if (cmd.equalsIgnoreCase("loginAuth")) {
			userAuth(jsondata);
		} else if (cmd.equalsIgnoreCase("getgrouptree")) {
			staticmemory.sendRemoteStr(getGroupTree(rootjson), jsondata.get("sessionid").toString());
		} else if (cmd.equalsIgnoreCase("nodeadd")) {
			handleInsertGrp(jsondata);
		} else if (cmd.equalsIgnoreCase("nodeedit")) {
			if (jsondata.get("type").toString().equalsIgnoreCase("group")) {
				handleUpdGrp(jsondata);
			} else {
				handleUpdateDev(jsondata);
			}
		} else if (cmd.equalsIgnoreCase("nodedel")) {
			if (jsondata.get("type").toString().equalsIgnoreCase("group")) {
				handleDelGrp(jsondata);
			} else {
				handleDeleteDev(jsondata);
			}
		} else if (cmd.equalsIgnoreCase("deviceadd")) {
			handleInsertDev(jsondata);
		} else if (cmd.equalsIgnoreCase("lazyLoad")) {
			staticmemory.sendRemoteStr(getLazyNodes(jsondata), jsondata.get("sessionid").toString());
		} else if (cmd.equalsIgnoreCase("devstatus")) {
			staticmemory.broadCast(handleOnlineInfo(jsondata));
		} else if (cmd.equalsIgnoreCase("alarm_message")) {
			staticmemory.broadCast(message);
		} else if (cmd.equalsIgnoreCase("dbclosed")) {
			staticmemory.broadCast(message);
		} else if (cmd.equalsIgnoreCase("severstatus")) {
			staticmemory.sendRemoteStr(message, jsondata.get("sessionid").toString());
		} else if (cmd.equalsIgnoreCase("getuserlist")) {
			staticmemory.sendRemoteStr(handleGetAllUsers(jsondata), jsondata.get("sessionid").toString());
		} else if (cmd.equalsIgnoreCase("handleuser")) {
			handleUsers(jsondata);
		}
	}

	@SuppressWarnings("static-access")
	public void start() {

		log.info("[#3] .....MainKernel starting.......");
		ClsLanguageExmp.init(true, true);
		ICDatabaseEngine1 = new CDatabaseEngine(redisUtil);

		initTopodData();

		// CurrentAlarmModel.me.logEngine=ICDatabaseEngine1;
		CurrentAlarmModel cam = new CurrentAlarmModel();
		cam.logEngine=ICDatabaseEngine1;
		cam.setRedisUtil(redisUtil);
		cam.setStaticMemory(staticmemory);

		PDUServer.me.listDevHash = this.listDevHash;
		TrapPduServer.me.listDevHash = this.listDevHash;
		ParamKernel.me.listDevHash = this.listDevHash;

		cam.start();
		TrapPduServer.me.start();
		PDUServer.me.start();
		Realtime_param_call.me.start();
		ParamKernel.me.start();

		Sstatus stsengine = new Sstatus(redisUtil);
		stsengine.start();

		Jedis jedis = null;
		
		//以下阻塞
		try {

			jedis = redisUtil.getConnection();
			jedis.psubscribe(jedissubSub, MAINKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());

			redisUtil.getJedisPool().returnResource(jedis);

		}


	}

	private void userAuth(JSONObject jsondata) {
		JSONObject rootjson = new JSONObject();
		AuthResult rst = handleAuthUser(jsondata);
		String sessionid = jsondata.get("sessionid").toString();
		if (rst == AuthResult.SUCCESS) {
			jsondata.put("Authed", true);
			staticmemory.sendRemoteStr(jsondata.toJSONString(), sessionid);
			// 初始化
			staticmemory.sendRemoteStr(getInitTree(rootjson), sessionid);
			//staticmemory.sendRemoteStr(getDBstatus(), sessionid);
			rootjson = new JSONObject();
			rootjson.put("cmd", "getLoginInit");
			rootjson.put("sessionid", sessionid);
			sendToQueue(rootjson.toJSONString(), HFCALARM_MESSAGE);
			
			
		} else {
			jsondata.put("Authed", false);
			if(rst == AuthResult.PASSWD_NOT_MATCH){
				jsondata.put("desc", "Password Error!");
			}else if(rst == AuthResult.USER_NOT_EXIST){
				jsondata.put("desc", "User not Exist!");
			}			
			staticmemory.sendRemoteStr(jsondata.toJSONString(), sessionid);
			staticmemory.RemoveSession(staticmemory.getSessionByID(sessionid));
		}

	}

	private String getInitTree(JSONObject rootjson) {
		rootjson.put("cmd", "getInitTree");
		JSONArray jsonarray = new JSONArray();

		jsonarray = getSubTree(rootListNode);
		rootjson.put("treenodes", jsonarray);
		rootjson.put("sfversion", "Server:"+Sstatus.versionString);
		String jsonString = rootjson.toJSONString();
		// System.out.println("jsonString==" + jsonString);
		return jsonString;
	}

	private String getGroupTree(JSONObject rootjson) {
		rootjson.put("cmd", "getgrouptree");
		JSONArray jsonarray = new JSONArray();

		jsonarray = getSubGroup(rootListNode);
		rootjson.put("treenodes", jsonarray);
		String jsonString = rootjson.toJSONString();
		// System.out.println("jsonString==" + jsonString);
		return jsonString;
	}

	private JSONArray getSubGroup(LNode pnode) {
		JSONObject subjson;
		JSONArray jsonarray = new JSONArray();
		for (Iterator iter = pnode.Nodes.iterator(); iter.hasNext();) {
			LNode node = (LNode) iter.next();
			InodeInterface InodeInterface1 = (InodeInterface) node;
			if (InodeInterface1.isGroup()) {
				subjson = new JSONObject();
				devGroup group = (devGroup) InodeInterface1;
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

	private String getDBstatus() {
		JSONObject rootjson = new JSONObject();
		rootjson.put("cmd", "dbclosed");
		rootjson.put("flag", !ICDatabaseEngine1.flag);
		return rootjson.toJSONString();
	}

	private JSONArray getSubTree(LNode pnode) {
		JSONObject subjson;
		JSONArray jsonarray = new JSONArray();
		for (Iterator iter = pnode.Nodes.iterator(); iter.hasNext();) {
			LNode node = (LNode) iter.next();
			InodeInterface InodeInterface1 = (InodeInterface) node;
			if (InodeInterface1.isGroup()) {
				subjson = new JSONObject();
				devGroup group = (devGroup) InodeInterface1;
				UserGroupTableRow usergroup = group.BindUserGroupTableRow;
				subjson.put("key", usergroup.UserGroupID);
				subjson.put("pkey", usergroup.ParentGroupID);
				subjson.put("title", usergroup.UserGroupName);
				subjson.put("type", "group");
				subjson.put("isFolder", true);
				subjson.put("expand", true);
				subjson.put("isAlarm", group.isAlarm == 0 ? false : true);
				subjson.put("icon", "images/net_center.png");
				JSONArray subjsonarray = new JSONArray();
				subjsonarray = getSubTree(node);
				if (usergroup.ParentGroupID == -1) {
					subjson.put("lazy", false);
					subjson.put("children", subjsonarray);
				} else {
					if (subjsonarray.size() < 50) {
						subjson.put("lazy", false);
						subjson.put("children", subjsonarray);
					} else {
						subjson.put("lazy", true);
					}
				}

				jsonarray.add(subjson);
			} else {
				DevTopd dev = (DevTopd) InodeInterface1;
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
				String imgstr = "images/devoff.png";
				switch (dev.HFCType1) {
				case HfcMinWorkstation:
				case HfcWorkstation:
					imgstr = "images/treeRece.png";
					break;
				case EDFA:
					imgstr = "images/treeEDFA.png";
					break;
				case Trans1310DM:
				case Trans1550DM:
				case TransEM:
					imgstr = "images/treeTrans.png";
					break;
				case Unknown:
					imgstr = "images/device.png";
					break;
				case OSW:
					imgstr = "images/device.png";
					break;
				default:
					imgstr = "images/device.png";
					break;
				}
				subjson.put("icon", dev.isOline ? imgstr : "images/devoff.png");
				infojson.put("title", "<span class='net-info' >" + dev._NetAddress + "</span>");
				infojson.put("icon", "images/net_info.ico");
				subjsonarray.add(infojson);
				infojson = new JSONObject();
				infojson.put("key", dev.HFCType1.ordinal());
				infojson.put("title", "<span class='net-info' >" + DProcess.getDevDISCRIPTIONByNettypeString(dev.mNetType) + "</span>");
				infojson.put("hfctype", dev.HFCType1.ordinal());
				infojson.put("icon", "images/net_info.ico");
				subjsonarray.add(infojson);
				subjson.put("children", subjsonarray);
				jsonarray.add(subjson);
			}
		}
		return jsonarray;
	}

	private String getLazyNodes(JSONObject jsondata) {
		JSONObject subjson;
		JSONObject rootjson = new JSONObject();
		JSONArray jsonarray = new JSONArray();
		rootjson.put("cmd", "lazyLoad");
		rootjson.put("key", jsondata.get("key").toString());
		int usergroupID = Integer.parseInt(jsondata.get("key").toString());// get
																			// goupid
																			// from
																			// jsondata
		try {
			devGroup grp = (devGroup) listGrpHash.get(usergroupID);
			rootjson.put("lazyNodes", getSubTree(grp));
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}
		return rootjson.toJSONString();
	}

	public void initTopodData() {
		try {
			Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
			Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();
			// List<CDataBasePropery.nojuDeviceTableRow> SlotRowsList =
			// ICDatabaseEngine1.slotTableGetAllRows();
			rootListNode = this.offerTopodModel(devHash, grpHash);
			isTopodInit = true;

		} catch (Exception e) {
			isTopodInit = false;
			// TODO: handle exception
		}

		// print rootListNode;
	}

	// by group and device collection args
	private LNode offerTopodModel(Hashtable devLists, Hashtable grpLists) {

		listDevHash.clear();
		listGrpHash.clear();
		LNode result = new LNode();
		result.fullpath = "设备中心";
		result.Level = 0;
		createTree(devLists, grpLists, result);
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

			group.Level = 1;
			rootNode.Nodes.add(group);
			group.parent = rootNode;
			group.fullpath = rootNode.fullpath + "/" + group.BindUserGroupTableRow.UserGroupName;
			group.Tag = group;
			// log.info(group.fullpath);
			listGrpHash.put(group.BindUserGroupTableRow.UserGroupID, group);

			CreateTreeNode(group, groupLists, devLists);

		}

	}

	private void CreateTreeNode(devGroup pgroup, Hashtable groupLists, Hashtable devLists) {
		// select all the child row in the grouptable
		LinkedList<UserGroupTableRow> rows = new LinkedList<UserGroupTableRow>();

		Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow dr = (UserGroupTableRow) e.nextElement();
			if (dr.ParentGroupID == pgroup.BindUserGroupTableRow.UserGroupID) {

				// add the group as child
				devGroup newgroup = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);

				newgroup.BindUserGroupTableRow = dr;
				newgroup.Level = pgroup.Level + 1;
				pgroup.Nodes.add(newgroup);
				newgroup.parent = pgroup;
				newgroup.fullpath = pgroup.fullpath + "/" + newgroup.BindUserGroupTableRow.UserGroupName;
				newgroup.Tag = newgroup;
				listGrpHash.put(newgroup.BindUserGroupTableRow.UserGroupID, newgroup);
				CreateTreeNode(newgroup, groupLists, devLists);

				// System.out.println(newgroup.fullpath);
			}

		}

		e = devLists.elements();
		while (e.hasMoreElements()) {
			nojuDeviceTableRow dr1 = (nojuDeviceTableRow) e.nextElement();
			if (dr1.UserGroupID == pgroup.BindUserGroupTableRow.UserGroupID) {

				DevTopd dev = new DevTopd(dr1);
				dev.BindnojuDeviceTableRow = dr1;

				dev.Level = pgroup.Level + 1;
				pgroup.Nodes.add(dev);
				dev.parent = dev;
				dev.fullpath = pgroup.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;
				dev.Tag = dev;
				listDevHash.put(dev._NetAddress, dev);
				dev.isOline = false;
				dev.OnlineCount = 0;

				// System.out.println(dev.fullpath);

			}
		}

	}

	@SuppressWarnings("unchecked")
	public boolean handleInsertGrp(JSONObject jsondata) {

		boolean mStatus = false;
		JSONObject rootjson = new JSONObject();

		// //get the group information from jsondata ,build a new
		// UserGroupTableRow ***************
		UserGroupTableRow mDevGrpTableRow = new UserGroupTableRow(0, jsondata.get("value").toString(), Integer.parseInt(jsondata.get("key").toString()));

		System.out.println(jsondata.get("value").toString());

		if (this.ICDatabaseEngine1.UserGroupTableInsertRow(mDevGrpTableRow) > 0) {
			mStatus = true;
		}

		if (mStatus) {
			LNode pGrp;
			if (mDevGrpTableRow.ParentGroupID == -1) {
				pGrp = this.rootListNode;
			} else {
				pGrp = (LNode) listGrpHash.get(mDevGrpTableRow.ParentGroupID);

				/*
				 * if (pGrp == null) { return false; }
				 */
			}

			UserGroupTableRow dr = mDevGrpTableRow;

			devGroup rootGroup = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
			rootGroup.BindUserGroupTableRow = dr;
			rootGroup.parent = pGrp;
			rootGroup.fullpath = pGrp.fullpath + "/" + rootGroup.BindUserGroupTableRow.UserGroupName;
			rootGroup.Level = rootGroup.parent.Level + 1;

			listGrpHash.put(rootGroup.BindUserGroupTableRow.UserGroupID, rootGroup);
			pGrp.Nodes.add(rootGroup);

			rootjson.put("cmd", "nodeadd");
			rootjson.put("key", mDevGrpTableRow.UserGroupID);// node UserGroupID
			rootjson.put("pkey", jsondata.get("key").toString()); // ParentGroupID
			rootjson.put("title", mDevGrpTableRow.UserGroupName);// UserGroupName
			rootjson.put("type", "group");
			rootjson.put("isFolder", true);
			rootjson.put("expand", true);
			rootjson.put("icon", "images/net_center.png");
			staticmemory.broadCast(rootjson.toJSONString());

			// for syslog
			rootjson = new JSONObject();
			rootjson.put("cmd", "grpaddlog");
			rootjson.put("title", mDevGrpTableRow.UserGroupName);
			
			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");

		}

		return mStatus;

	}

	public boolean handleDelGrp(JSONObject jsondata) {
		boolean mStatus = false;

		int usergroupID = Integer.parseInt(jsondata.get("key").toString());// get
																			// goupid
																			// from
																			// jsondata
		devGroup delgrp = (devGroup) listGrpHash.get(usergroupID);

		UserGroupTableRow mDevGrpTableRow = delgrp.BindUserGroupTableRow;

		/*
		 * if (!this.ICDatabaseEngine1.clearUserDevGrpInfo("",
		 * mDevGrpTableRow.UserGroupID)) { return false; } if
		 * (!this.ICDatabaseEngine1
		 * .clearElemetTableByGID(cmd.mDevGrpTableRow.UserGroupID)) { return
		 * false; }
		 */

		// have child node
		if (delgrp.Nodes.size() > 0) {
			return false;

		}

		mStatus = this.ICDatabaseEngine1.UserGroupTableDeleteRow(mDevGrpTableRow.UserGroupID);
		if (mStatus) {
			LNode pGrp;
			if (mDevGrpTableRow.ParentGroupID == -1) {
				pGrp = this.rootListNode;
			} else {
				pGrp = (LNode) listGrpHash.get(mDevGrpTableRow.ParentGroupID);
			}

			pGrp.Nodes.remove(delgrp);
			listGrpHash.remove(mDevGrpTableRow.UserGroupID);

			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "nodedel");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("pkey", jsondata.get("pkey").toString());
			staticmemory.broadCast(rootjson.toJSONString());

			// for syslog
			rootjson = new JSONObject();
			rootjson.put("cmd", "grpdellog");
			rootjson.put("title", delgrp.BindUserGroupTableRow.UserGroupName);

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");

			return true;

		} else {
			log.info("Del Grp :" + mDevGrpTableRow.UserGroupName + " Error!");
		}

		return false;

	}

	public boolean handleUpdGrp(JSONObject jsondata) {
		boolean mStatus = false;

		int userid = Integer.parseInt(jsondata.get("key").toString());// get 缁処D
																		// from
																		// jsondata
		devGroup rootGroup = (devGroup) listGrpHash.get(userid);

		UserGroupTableRow mDevGrpTableRow = rootGroup.BindUserGroupTableRow;

		String tmpNameString = mDevGrpTableRow.UserGroupName;
		mDevGrpTableRow.UserGroupName = jsondata.get("title").toString();

		mStatus = this.ICDatabaseEngine1.UserGroupTableUpdateRow(mDevGrpTableRow);
		if (mStatus) {

			rootGroup.BindUserGroupTableRow = mDevGrpTableRow;
			// rootGroup.name = rootGroup.BindUserGroupTableRow.UserGroupName;
			rootGroup.fullpath = rootGroup.parent.fullpath + "/" + rootGroup.BindUserGroupTableRow.UserGroupName;
			// Hashtable effectPathList = new Hashtable();
			// effectPathList.Add(rootGroup.BindUserGroupTableRow.UserGroupID.ToString(),
			// grp.fullpath);
			// reflashPath(grp, effectPathList);
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "nodeedit");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("title", mDevGrpTableRow.UserGroupName);
			rootjson.put("type", "group");
			staticmemory.broadCast(rootjson.toJSONString());

			rootjson = new JSONObject();
			rootjson.put("cmd", "grpeditlog");
			rootjson.put("title", jsondata.get("title").toString());

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			// for syslog
			sendToQueue(rootjson.toJSONString(), "currentalarm.message");

		} else {
			mDevGrpTableRow.UserGroupName = tmpNameString;

		}

		return mStatus;
	}

	public DevTopd handleInsertDev(JSONObject jsondata) {
		boolean mStatus = false;
		int usergroupID = Integer.parseInt(jsondata.get("key").toString());// get
																			// goupid
																			// from
																			// jsondata

		devGroup grp = (devGroup) listGrpHash.get(usergroupID);
		String devtypestr = jsondata.get("devtype").toString();

		if (grp == null)//
		{
			return null;
		}

		InetAddress addr;
		try {
			addr = InetAddress.getByName(jsondata.get("netip").toString());
		} catch (Exception e) {// invalid ip
			return null;
		}
		 if(listDevHash.containsKey(addr.getHostAddress()))
		{
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "devaddfalse");
			rootjson.put("netip",addr.getHostAddress());		
			rootjson.put("desc", "Device already exist");		
			staticmemory.sendRemoteStr(rootjson.toJSONString(), jsondata.get("sessionid").toString());
			return null;
		}
		nojuDeviceTableRow mDeviceTableRow = new nojuDeviceTableRow(addr.getHostAddress(), DProcess.netTypeFromStringNetTypes(devtypestr));
		mDeviceTableRow.UserGroupID = usergroupID;
		mDeviceTableRow._ROCommunity = jsondata.get("rcommunity").toString();
		mDeviceTableRow._RWCommunity = jsondata.get("wcommunity").toString();
		mDeviceTableRow.Name = jsondata.get("devname").toString();

		mStatus = this.ICDatabaseEngine1.DeviceTableInsertRow(mDeviceTableRow);
		if (mStatus) {
			DevTopd dev = new DevTopd(mDeviceTableRow);
			dev.isOline = false;

			dev.parent = grp;
			dev.fullpath = grp.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;

			dev.OnlineCount = 0;
			// dev.Pm = null;

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
			rootnodejson.put("icon", dev.isOline ? "images/device.png" : "images/devoff.png");
			subjson.put("title", "<span class='net-info' >" + dev._NetAddress + "</span>");
			subjson.put("hfctype", dev.HFCType1.toString());
			subjson.put("icon", "images/net_info.ico");
			subjsonarray.add(subjson);
			subjson = new JSONObject();
			subjson.put("key", dev.mNetType.toString());
			subjson.put("title", "<span class='net-info' >" + DProcess.getDevDISCRIPTIONByNettypeString(dev.mNetType) + "</span>");
			subjson.put("icon", "images/net_info.ico");
			subjsonarray.add(subjson);
			rootnodejson.put("children", subjsonarray);
			jsonarray.add(rootnodejson);
			rootjson.put("devnodes", jsonarray);

			staticmemory.broadCast(rootjson.toJSONString());

			// for syslog
			rootjson = new JSONObject();
			rootjson.put("cmd", "devaddlog");
			rootjson.put("key", dev._NetAddress);
			rootjson.put("title", dev.BindnojuDeviceTableRow.Name);

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");

			return dev;

		}
	

		return null;
	}

	public boolean handleUpdateDev(JSONObject jsondata) {
		boolean mStatus = false;

		String netaddr = jsondata.get("key").toString();// get netaddr from
														// jsondata
		DevTopd dev = (DevTopd) listDevHash.get(netaddr);

		if (dev == null) {

			return false;

		}

		nojuDeviceTableRow mDeviceTableRow = dev.BindnojuDeviceTableRow;

		// edit the mDeviceTableRow property here from jsondata
		String tmpNameString = mDeviceTableRow.Name;
		String _tmpRO = mDeviceTableRow._ROCommunity;
		String _tmpRW = mDeviceTableRow._RWCommunity;

		mDeviceTableRow.Name = jsondata.get("title").toString();
		mDeviceTableRow._ROCommunity = jsondata.get("rcommunity").toString();
		mDeviceTableRow._RWCommunity = jsondata.get("wcommunity").toString();
		mStatus = this.ICDatabaseEngine1.DeviceTableUpdateRow(mDeviceTableRow);

		if (mStatus) {
			staticmemory.SetRealTimeDevCommunity(netaddr, jsondata.get("rcommunity").toString(), jsondata.get("wcommunity").toString());
			dev.BindnojuDeviceTableRow = mDeviceTableRow;

			dev.fullpath = dev.parent.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;

			staticmemory.broadCast(jsondata.toJSONString());

			// for syslog
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "deveditlog");
			rootjson.put("key", dev.BindnojuDeviceTableRow.get_NetAddress());
			rootjson.put("title", dev.BindnojuDeviceTableRow.Name);

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");
		} else {

			mDeviceTableRow.Name = tmpNameString;
			mDeviceTableRow._ROCommunity = _tmpRO;
			mDeviceTableRow._RWCommunity = _tmpRW;
		}

		return mStatus;

	}

	public boolean handleDeleteDev(JSONObject jsondata) {

		boolean mStatus = false;

		String devAddr = jsondata.get("key").toString();// get ip from jsondata
		DevTopd delDev = (DevTopd) listDevHash.get(devAddr);

		if (delDev == null) {

			return false;

		}

		mStatus = this.ICDatabaseEngine1.DeviceTableDeleteRow(delDev.BindnojuDeviceTableRow);

		if (mStatus) {
			DevTopd dev = (DevTopd) listDevHash.get(delDev.BindnojuDeviceTableRow.get_NetAddress());
			devGroup grp = (devGroup) listGrpHash.get(delDev.BindnojuDeviceTableRow.UserGroupID);
			grp.Nodes.remove(dev);
			listDevHash.remove(delDev.BindnojuDeviceTableRow.get_NetAddress());

			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "nodedel");
			rootjson.put("key", jsondata.get("key").toString());
			rootjson.put("pkey", jsondata.get("pkey").toString());
			staticmemory.broadCast(rootjson.toJSONString());

			// for syslog

			// for syslog
			rootjson = new JSONObject();
			rootjson.put("cmd", "devdellog");
			rootjson.put("key", delDev.BindnojuDeviceTableRow.get_NetAddress());
			rootjson.put("title", delDev.BindnojuDeviceTableRow.Name);

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");

		}

		return mStatus;
	}

	private String handleOnlineInfo(JSONObject jsondata) {
		String ipaddr = jsondata.get("ip").toString();// GET FROM JOSNDATA

		DevTopd lNode = (DevTopd) listDevHash.get(ipaddr);

		if (lNode == null) {
			return jsondata.toJSONString();
		}

		// hi锛寈inglong锛孒FCType1锛孖D,MD,SN,DEVICEID,ISONLINE?杩欎簺淇℃伅璇锋彁浜ゅ埌鍓嶇

		/*
		 * ScMessage scMessage = new ScMessage(dev.IsOline, lNode.HFCType1,
		 * dev.NetAddress); scMessage.ID = lNode.ID; scMessage.MD = lNode.MD;
		 * scMessage.SN = lNode.SN; scMessage.DEVICEID = lNode.DEVICEID;
		 * 
		 * 
		 * OlineeInforCmd OlineeInforCmd1 = new OlineeInforCmd(scMessage,
		 * CMDType.OLINE_INFO); OlineeInforCmd1.mTimeStamp = DateTime.Now.Ticks;
		 * this.Notify("DbEngine.newCMD", OlineeInforCmd1);
		 */
		jsondata.put("hfctype", lNode.HFCType1.ordinal());
		jsondata.put("id", lNode.ID);
		jsondata.put("md", lNode.MD);
		jsondata.put("sn", lNode.SN);
		log.info("------->>>" + jsondata.toJSONString());
		return jsondata.toJSONString();
	}

	private String handleGetAllUsers(JSONObject jsondata) {
		JSONArray jsonarray = new JSONArray();
		JSONObject subjson;
		ArrayList<nojuUserAuthorizeTableRow> mUserAuthorizeTableRowList;
		try {
			mUserAuthorizeTableRowList = ICDatabaseEngine1.UserAuthorizeTableGetAllRows();

		} catch (Exception e) {
			return jsondata.toJSONString();
		}

		for (nojuUserAuthorizeTableRow prow : mUserAuthorizeTableRowList) {
			subjson = new JSONObject();
			subjson.put("userid", prow.UserID);
			subjson.put("username", prow.UserName);
			subjson.put("level", prow.AuthTotal);
			subjson.put("istrap", prow.Encrypted);
			jsonarray.add(subjson);
		}
		jsondata.put("userlist", jsonarray);
		return jsondata.toJSONString();
	}
	
	private void handleUsers(JSONObject jsondata) {
		String cmd = jsondata.get("target").toString();
		String uname = jsondata.get("username").toString();
		if(cmd.equalsIgnoreCase("getuserinfo")){
			try {
				nojuUserAuthorizeTableRow uatr = ICDatabaseEngine1.UserAuthorizeTableFindUser(uname);
				jsondata.put("AuthTotal", uatr.AuthTotal);
				jsondata.put("PassWord1", uatr.PassWord);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
		}else if(cmd.equalsIgnoreCase("modifypassword_admin")){
			handleUpdateUser_admin(jsondata);
		}else if(cmd.equalsIgnoreCase("modifypassword")){
			handleUpdateUser(jsondata);
		}else if(cmd.equalsIgnoreCase("deluser")){
			handleDeleteUser(jsondata);
		}else if(cmd.equalsIgnoreCase("adduser")){
			handleInsertUser(jsondata);
		}
	}

	private AuthResult handleAuthUser(JSONObject jsondata) {
		String username = jsondata.get("username").toString();
		String password = jsondata.get("password").toString();
		AuthResult rst = AuthResult.SUCCESS;
		boolean isExist = false;
		ArrayList<nojuUserAuthorizeTableRow> mUserAuthorizeTableRowList;
		try {
			mUserAuthorizeTableRowList = ICDatabaseEngine1.UserAuthorizeTableGetAllRows();

		} catch (Exception e) {

			rst = AuthResult.USER_NOT_EXIST;
			return rst;
			// TODO: handle exception
		}

		for (nojuUserAuthorizeTableRow prow : mUserAuthorizeTableRowList) {

			if (prow.UserName.equalsIgnoreCase(username)) {
				isExist = true;
				if (prow.PassWord.equalsIgnoreCase(password)) {
					// login success
					rst = AuthResult.SUCCESS;
					jsondata.put("level", prow.AuthTotal);
				} else {
					rst = AuthResult.PASSWD_NOT_MATCH;

				}
				break;
			}

		}

		if (!isExist) {
			rst = AuthResult.USER_NOT_EXIST;
		}
		return rst;
	}

	private void handleInsertUser(JSONObject jsondata) {

		boolean mStatus = false;
		Byte AuthTotal = Byte.parseByte(jsondata.get("AuthTotal").toString());
		//AuthTotal++;
		nojuUserAuthorizeTableRow newRow = new nojuUserAuthorizeTableRow(-1, jsondata.get("username").toString(), AuthTotal, jsondata.get("password")
				.toString());

		if (this.ICDatabaseEngine1.UserAuthorizeTableInsertRow(newRow) > 0) {
			mStatus = true;
		}

		if (mStatus) {
			jsondata.put("key", newRow.UserID); // ParentGroupID
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
			
			// for syslog
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "insertuserlog");
			rootjson.put("title",newRow.UserName);

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			

			sendToQueue(rootjson.toJSONString(), "currentalarm.message");
			
		}
	}

	private void handleDeleteUser(JSONObject jsondata) {
		String uname = jsondata.get("username").toString();// userid
		nojuUserAuthorizeTableRow uatr;
		boolean mStatus = true;
		try {
			uatr = ICDatabaseEngine1.UserAuthorizeTableFindUser(uname);
			mStatus = this.ICDatabaseEngine1.UserAuthorizeTableDeleteRow(uatr.UserID);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mStatus = false;
		}

		if (mStatus) {
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
			// for syslog
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "deluserlog");
			rootjson.put("title",jsondata.get("username").toString());

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			
			sendToQueue(rootjson.toJSONString(), "currentalarm.message");
		} else {
			log.info("Del user :" + uname + " Error!");
		}

	}
	
	private void handleUpdateUser_admin(JSONObject jsondata) {
		String uname = jsondata.get("username").toString();// userid
		nojuUserAuthorizeTableRow uatr;
		boolean mStatus = true;
		try {
			uatr = ICDatabaseEngine1.UserAuthorizeTableFindUser(uname);
			Byte AuthTotal = Byte.parseByte(jsondata.get("AuthTotal").toString());			
			//AuthTotal++;
			mStatus = this.ICDatabaseEngine1.UserAuthorizeTableUpdateRow(uatr.UserID, jsondata.get("password").toString(), AuthTotal);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mStatus = false;
		}		

		if (mStatus) {
			
		     System.out.println("update user password ok ");
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
			
			// for syslog
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "updateuserlog");
			rootjson.put("title",jsondata.get("username").toString());

			UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
			rootjson.put("operater", userSession.username);			
			sendToQueue(rootjson.toJSONString(), "currentalarm.message");
		}
	}


	private void handleUpdateUser(JSONObject jsondata) {
		String uname = jsondata.get("username").toString();// userid
		nojuUserAuthorizeTableRow uatr;
		boolean mStatus = true;
		try {
			uatr = ICDatabaseEngine1.UserAuthorizeTableFindUser(uname);
			Byte AuthTotal = Byte.parseByte(jsondata.get("AuthTotal").toString());
			
			if(jsondata.get("oldpassword").toString().equalsIgnoreCase(uatr.PassWord)){
				if(AuthTotal != 0){
					mStatus = this.ICDatabaseEngine1.UserAuthorizeTableUpdateRow(uatr.UserID, jsondata.get("password").toString(), AuthTotal);
				}else{
					mStatus = this.ICDatabaseEngine1.UserAuthorizeTableUpdateRow(uatr.UserID, jsondata.get("password").toString(), uatr.AuthTotal);
				}
				
			}else{
				mStatus = false;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mStatus = false;
		}
		

		if (mStatus) {
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
			// for syslog
						JSONObject rootjson = new JSONObject();
						rootjson.put("cmd", "updateuserlog");
						rootjson.put("title",jsondata.get("username").toString());

						UserSession userSession=staticmemory.getUserSessionByID( jsondata.get("sessionid").toString());			
						rootjson.put("operater", userSession.username);			
						sendToQueue(rootjson.toJSONString(), "currentalarm.message");
		}
	}

	private void handleGetAllUserssdfsdf(JSONObject jsondata) {

		JSONObject logjson;

		JSONArray jsonarray = new JSONArray();

		ArrayList<nojuUserAuthorizeTableRow> mUserAuthorizeTableRowList = new ArrayList<nojuUserAuthorizeTableRow>();
		try {
			mUserAuthorizeTableRowList = ICDatabaseEngine1.UserAuthorizeTableGetAllRows();

		} catch (Exception e) {

			// TODO: handle exception
		}

		for (nojuUserAuthorizeTableRow prow : mUserAuthorizeTableRowList) {
			logjson = new JSONObject();
			logjson.put("key", prow.UserID);
			logjson.put("UserName", prow.UserID);
			logjson.put("AuthTotal", prow.AuthTotal);
			logjson.put("PassWord1", prow.PassWord);
			jsonarray.add(logjson);

		}
		jsondata.put("userlist", jsonarray);
		staticmemory.broadCast(jsondata.toJSONString());

	}

	public void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(queue, msg);

		} catch (Exception e) {
			log.info(e.getMessage());

		} finally {
			redisUtil.closeConnection(jedis);
		}
	}

}
