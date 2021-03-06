package wl.hfc.alarmlog;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;

import wl.hfc.common.*;
import wl.hfc.common.NlogType.OperLogTypes;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.server.SmsgList;
import wl.hfc.server.Sstatus;

//CurrentAlarmModel
public class CurrentAlarmModel extends Thread {
	private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
	private static final String HFCALARM_MESSAGE = "currentalarm.message";
	private static Logger log = Logger.getLogger(CurrentAlarmModel.class);
	
	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static CurrentAlarmModel me;


	private int MAX_TRAPNUMBER = 100;
	public CDatabaseEngine logEngine;

	// the real model,current trap rows
	public CopyOnWriteArrayList<nojuTrapLogTableRow> allRows;
	// public Hashtable allRowsTable;

	public ArrayList<nojuTrapLogTableRow> invalidRows;
	// public Hashtable invalidRowsTable;



	private int pageNumMAX = 50;
	private int versionNum = 0;
	private List<List<nojuTrapLogTableRow>> traprowss = new ArrayList<List<nojuTrapLogTableRow>>();

	public static void setRedisUtil(RedisUtil redisUtil) {
		CurrentAlarmModel.redisUtil = redisUtil;
	}

	public static void setStaticMemory(StaticMemory staticmemory) {
		CurrentAlarmModel.staticmemory = staticmemory;
	}

	public CurrentAlarmModel() {

		log.info("construct  CurrentAlarmModel");	
		allRows = new CopyOnWriteArrayList<nojuTrapLogTableRow>();
		// allRowsTable = new Hashtable();

		invalidRows = new ArrayList<nojuTrapLogTableRow>();
		// invalidRowsTable = new Hashtable();

		this.setName("CurrentAlarmModel");		

		me = this;

	}

	private List<String> tmplist = new ArrayList<String>();

	public void run() {
		log.info("CurrentAlarmModel run.......");	
		
		if (Sstatus.isRedis) {
			Jedis jedis = null;
			
			try {
				jedis = redisUtil.getConnection();
				jedis.psubscribe(jedissubSub, HFCALARM_MESSAGE);
				redisUtil.getJedisPool().returnResource(jedis);

			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.getMessage());

				redisUtil.getJedisPool().returnResource(jedis);

			}			

			
		}else
		{
			while (true) {
				synchronized (SmsgList.alarmstorage) {

					if (SmsgList.alarmstorage.isEmpty()) {
						try {
							SmsgList.alarmstorage.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					tmplist.clear();
					for (String attribute : SmsgList.alarmstorage) {
						tmplist.add(attribute);
					}
					SmsgList.alarmstorage.clear();

				}
				for (String msg : tmplist) {
					phraseMSG(msg);
				}

			}
			
			
			
		}
	

	}

	private void phraseMSG(String msg) {

		try {

			log.debug("Received: '" + msg + "'");
			JSONObject jsondata = (JSONObject) new JSONParser().parse(msg);
			String cmd = jsondata.get("cmd").toString();
			JSONObject rootjson = new JSONObject();
			if (cmd.equalsIgnoreCase("getLoginInit")) {
				staticmemory.sendRemoteStr(getInitLog(rootjson), jsondata.get("sessionid").toString());
			} else if (cmd.equalsIgnoreCase("newalarm")) {

				parseAlarm(msg);

			} else if (cmd.equalsIgnoreCase("alarmsearch")) {
				String ispage = jsondata.get("ispage").toString();
				if (ispage.equalsIgnoreCase("0"))// 第一次
					staticmemory.sendRemoteStr(getHistoryTraplogs(jsondata), jsondata.get("sessionid").toString());
				else// 页面next触发
				{

					String sss = getPageHtytraplogs(jsondata);
					if (sss != null) {
						staticmemory.sendRemoteStr(sss, jsondata.get("sessionid").toString());
					}
				}

			} else if (cmd.equalsIgnoreCase("grpaddlog")) {

				InsertOperLog(OperLogTypes.UserGroupOpration,
						ClsLanguageExmp.formGet("创建分组") + "： " + jsondata.get("title").toString(),
						jsondata.get("operater").toString());

			} else if (cmd.equalsIgnoreCase("devaddLog")) {

				InsertOperLog(
						OperLogTypes.DeviceOpration, ClsLanguageExmp.formGet("创建IP设备") + "： "
								+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
						jsondata.get("operater").toString());

			} else if (cmd.equalsIgnoreCase("grpdellog")) {

				InsertOperLog(OperLogTypes.UserGroupOpration,
						ClsLanguageExmp.formGet("删除") + "： " + jsondata.get("title").toString(),
						jsondata.get("operater").toString());

			} else if (cmd.equalsIgnoreCase("devdellog")) {

				InsertOperLog(
						OperLogTypes.DeviceOpration, ClsLanguageExmp.formGet("删除") + "： "
								+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
						jsondata.get("operater").toString());
			} else if (cmd.equalsIgnoreCase("grpeditlog")) {

				InsertOperLog(OperLogTypes.UserGroupOpration,
						(ClsLanguageExmp.isEn ? "Update: " : "更新：") + jsondata.get("title").toString(),
						jsondata.get("operater").toString());

			} else if (cmd.equalsIgnoreCase("deveditlog")) {

				InsertOperLog(
						OperLogTypes.DeviceOpration, (ClsLanguageExmp.isEn ? "Update: " : "更新：") + "： "
								+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
						jsondata.get("operater").toString());
			} else if (cmd.equalsIgnoreCase("insertuserlog")) {

				InsertOperLog(OperLogTypes.UserOpration,
						(ClsLanguageExmp.isEn ? "Create user: " : "创建用户：") + "： " + jsondata.get("title").toString(),
						jsondata.get("title").toString());
			} else if (cmd.equalsIgnoreCase("deluserlog")) {

				InsertOperLog(OperLogTypes.UserOpration,
						(ClsLanguageExmp.isEn ? "Delete user: " : "删除用户：") + "： " + jsondata.get("title").toString(),
						jsondata.get("operater").toString());
			} else if (cmd.equalsIgnoreCase("updateuserlog")) {

				InsertOperLog(OperLogTypes.UserOpration,
						(ClsLanguageExmp.isEn ? "Update user: " : "更新用户：") + "： " + jsondata.get("title").toString(),
						jsondata.get("operater").toString());
			} else if (cmd.equalsIgnoreCase("userlogin")) {

				InsertOperLog(OperLogTypes.UserOpration,
						(ClsLanguageExmp.isEn ? "User login: " : "用户登陆：") + "： " + jsondata.get("title").toString(),
						jsondata.get("title").toString());
			} else if (cmd.equalsIgnoreCase("optlogsearch")) {

				staticmemory.sendRemoteStr(getHistoryoperlogs(jsondata), jsondata.get("sessionid").toString());

			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
		}

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
				System.out.println(" [x] CurrentAlarmModel Received: '" + msg + "'");

				JSONObject jsondata = (JSONObject) new JSONParser().parse(msg);
				String cmd = jsondata.get("cmd").toString();
				JSONObject rootjson = new JSONObject();
				if (cmd.equalsIgnoreCase("getLoginInit")) {
					staticmemory.sendRemoteStr(getInitLog(rootjson), jsondata.get("sessionid").toString());
				} else if (cmd.equalsIgnoreCase("newalarm")) {

					parseAlarm(msg);

				} else if (cmd.equalsIgnoreCase("alarmsearch")) {
					String ispage = jsondata.get("ispage").toString();
					if (ispage.equalsIgnoreCase("0"))// 第一次
						staticmemory.sendRemoteStr(getHistoryTraplogs(jsondata), jsondata.get("sessionid").toString());
					else// 页面next触发
					{

						String sss = getPageHtytraplogs(jsondata);
						if (sss != null) {
							staticmemory.sendRemoteStr(sss, jsondata.get("sessionid").toString());
						}
					}

				} else if (cmd.equalsIgnoreCase("grpaddlog")) {

					InsertOperLog(OperLogTypes.UserGroupOpration,
							ClsLanguageExmp.formGet("创建分组") + "： " + jsondata.get("title").toString(),
							jsondata.get("operater").toString());

				} else if (cmd.equalsIgnoreCase("devaddLog")) {

					InsertOperLog(
							OperLogTypes.DeviceOpration, ClsLanguageExmp.formGet("创建IP设备") + "： "
									+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
							jsondata.get("operater").toString());

				} else if (cmd.equalsIgnoreCase("grpdellog")) {

					InsertOperLog(OperLogTypes.UserGroupOpration,
							ClsLanguageExmp.formGet("删除") + "： " + jsondata.get("title").toString(),
							jsondata.get("operater").toString());

				} else if (cmd.equalsIgnoreCase("devdellog")) {

					InsertOperLog(
							OperLogTypes.DeviceOpration, ClsLanguageExmp.formGet("删除") + "： "
									+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
							jsondata.get("operater").toString());
				} else if (cmd.equalsIgnoreCase("grpeditlog")) {

					InsertOperLog(OperLogTypes.UserGroupOpration,
							(ClsLanguageExmp.isEn ? "Update: " : "更新：") + jsondata.get("title").toString(),
							jsondata.get("operater").toString());

				} else if (cmd.equalsIgnoreCase("deveditlog")) {

					InsertOperLog(
							OperLogTypes.DeviceOpration, (ClsLanguageExmp.isEn ? "Update: " : "更新：") + "： "
									+ jsondata.get("title").toString() + " @ " + jsondata.get("key").toString(),
							jsondata.get("operater").toString());
				} else if (cmd.equalsIgnoreCase("insertuserlog")) {

					InsertOperLog(OperLogTypes.UserOpration, (ClsLanguageExmp.isEn ? "Create user: " : "创建用户：") + "： "
							+ jsondata.get("title").toString(), jsondata.get("title").toString());
				} else if (cmd.equalsIgnoreCase("deluserlog")) {

					InsertOperLog(OperLogTypes.UserOpration, (ClsLanguageExmp.isEn ? "Delete user: " : "删除用户：") + "： "
							+ jsondata.get("title").toString(), jsondata.get("operater").toString());
				} else if (cmd.equalsIgnoreCase("updateuserlog")) {

					InsertOperLog(OperLogTypes.UserOpration, (ClsLanguageExmp.isEn ? "Update user: " : "更新用户：") + "： "
							+ jsondata.get("title").toString(), jsondata.get("operater").toString());
				} else if (cmd.equalsIgnoreCase("userlogin")) {

					InsertOperLog(OperLogTypes.UserOpration,
							(ClsLanguageExmp.isEn ? "User login: " : "用户登陆：") + "： " + jsondata.get("title").toString(),
							jsondata.get("title").toString());
				} else if (cmd.equalsIgnoreCase("optlogsearch")) {

					staticmemory.sendRemoteStr(getHistoryoperlogs(jsondata), jsondata.get("sessionid").toString());

				}

			} catch (Exception e) {
				e.printStackTrace();
				log.info(e.getMessage());
			}

		}
	};

	private String getInitLog(JSONObject rootjson) {
		rootjson = new JSONObject();
		JSONObject logjson;
		rootjson.put("cmd", "getInitLog");
		JSONArray jsonarray = new JSONArray();

		// System.out.println("CurrentAlarmModel.me.allRows.size()==" +
		// CurrentAlarmModel.me.allRows.size());
		for (nojuTrapLogTableRow prow : CurrentAlarmModel.me.allRows) {
			logjson = new JSONObject();
			logjson.put("id", prow.TrapLogID);
			logjson.put("level", NlogType.getAlarmString(prow.TrapLogType));
			logjson.put("addr", prow.TrapDevAddress);
			logjson.put("path", prow.neName);
			logjson.put("type", prow.TrapLogType.toString());
			logjson.put("paramname", prow.parmName);
			logjson.put("paramvalue", prow.paramValue);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logjson.put("eventtime", sdf.format(prow.TrapLogTime));
			logjson.put("solved", prow.TrapTreatMent);
			logjson.put("solvetime", prow.isTreated);
			jsonarray.add(logjson);
		}
		rootjson.put("alarms", jsonarray);
		// invalid alarms
		jsonarray = new JSONArray();
		for (nojuTrapLogTableRow prow : CurrentAlarmModel.me.invalidRows) {
			logjson = new JSONObject();
			logjson.put("id", prow.TrapLogID);
			logjson.put("level", NlogType.getAlarmString(prow.TrapLogType));
			logjson.put("addr", prow.TrapDevAddress);
			logjson.put("path", prow.neName);
			logjson.put("type", prow.TrapLogType.toString());
			logjson.put("paramname", prow.parmName);
			logjson.put("paramvalue", prow.paramValue);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logjson.put("eventtime", sdf.format(prow.TrapLogTime));
			logjson.put("solved", prow.TrapTreatMent);
			logjson.put("solvetime", prow.isTreated);
			jsonarray.add(logjson);
		}
		rootjson.put("invalidalarms", jsonarray);
		return rootjson.toJSONString();
	}

	// .....1
	private String getPageHtytraplogs(JSONObject jsondata) {

		int logversion = Integer.parseInt(jsondata.get("versionnum").toString());
		int pagenum = Integer.parseInt(jsondata.get("pagenum").toString());
		int isleft = Integer.parseInt(jsondata.get("isleft").toString());
		if (isleft == 1)// up page
		{
			pagenum--;
		} else// next page
		{
			pagenum++;
		}
		if (pagenum <= 0) {
			return null;
		}
		JSONObject rootjson = new JSONObject();
		String descr = "";
		rootjson.put("cmd", jsondata.get("cmd").toString());
		JSONObject logjson;
		JSONArray jsonarray = new JSONArray();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (this.versionNum == logversion) {

			if (traprowss.size() >= pagenum) {
				List<nojuTrapLogTableRow> currenPage = traprowss.get(pagenum - 1);

				for (nojuTrapLogTableRow prow : currenPage) {
					logjson = new JSONObject();
					logjson.put("id", prow.TrapLogID);
					logjson.put("level", NlogType.getAlarmString(prow.TrapLogType));
					// logjson.put("level", prow.level);
					logjson.put("addr", prow.TrapDevAddress);
					logjson.put("path", prow.neName);
					logjson.put("type", prow.TrapLogType.toString());
					logjson.put("paramname", prow.parmName);
					logjson.put("paramvalue", prow.paramValue);
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					logjson.put("eventtime", sdf.format(prow.TrapLogTime));
					logjson.put("solved", prow.TrapTreatMent);
					logjson.put("solvetime", prow.isTreated);
					jsonarray.add(logjson);
				}

			} else// page over range
			{
				return null;
			}

			// descr=sdf.format(datestart)+" --- "+ sdf.format(datestart)+"
			// "+jsondata.get("source").toString();

		} else {
			return null;
		}

		rootjson.put("alarms", jsonarray);
		rootjson.put("descr", descr);
		rootjson.put("versionnum", versionNum);
		rootjson.put("pagenum", pagenum);

		return rootjson.toJSONString();

	}

	private String getHistoryTraplogs(JSONObject jsondata) {
		JSONObject rootjson = new JSONObject();
		JSONObject logjson;
		String descr = "";
		Date datestart;
		Date dateend;

		rootjson.put("cmd", jsondata.get("cmd").toString());
		JSONArray jsonarray = new JSONArray();

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String custSlectindexString = jsondata.get("customdate").toString();

			if (custSlectindexString.equalsIgnoreCase("0")) {
				datestart = sdf.parse(jsondata.get("start").toString());// 已经是零点
				dateend = sdf.parse(jsondata.get("end").toString());

				// 结束日的24点
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateend);
				calendar.add(calendar.DATE, 1);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				dateend = calendar.getTime();
			} else {

				int daysNeedToSHUT = 0;
				if (custSlectindexString.equalsIgnoreCase("1")) {
					daysNeedToSHUT = -1;
				} else if (custSlectindexString.equalsIgnoreCase("2")) {
					daysNeedToSHUT = -7;
				} else if (custSlectindexString.equalsIgnoreCase("3")) {
					daysNeedToSHUT = -30;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				calendar.add(calendar.DATE, daysNeedToSHUT);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				datestart = calendar.getTime();

				calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				calendar.add(calendar.DATE, daysNeedToSHUT);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				datestart = calendar.getTime();

				dateend = new Date();

			}

			int level = Integer.parseInt(jsondata.get("level").toString());

			int statusss = Integer.parseInt(jsondata.get("treatment").toString());
			ArrayList<nojuTrapLogTableRow> traprow = this.logEngine.getTrapRowsWithTime(datestart, dateend,
					jsondata.get("source").toString(), level, statusss);

			// System.out.println("-------------traprow-size =" +
			// traprow.size());

			traprowss = createList(traprow, pageNumMAX);
			versionNum++;
			if (traprowss.size() > 0) {
				List<nojuTrapLogTableRow> firstPage = traprowss.get(0);// 第一页

				for (nojuTrapLogTableRow prow : firstPage) {
					logjson = new JSONObject();
					logjson.put("id", prow.TrapLogID);
					logjson.put("level", NlogType.getAlarmString(prow.TrapLogType));
					// logjson.put("level", prow.level);
					logjson.put("addr", prow.TrapDevAddress);
					logjson.put("path", prow.neName);
					logjson.put("type", prow.TrapLogType.toString());
					logjson.put("paramname", prow.parmName);
					logjson.put("paramvalue", prow.paramValue);
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					logjson.put("eventtime", sdf.format(prow.TrapLogTime));
					logjson.put("solved", prow.TrapTreatMent);
					logjson.put("solvetime", prow.isTreated);
					jsonarray.add(logjson);
				}
			}

			descr = sdf.format(datestart) + " --- " + sdf.format(datestart) + "  " + jsondata.get("source").toString();

		} catch (Exception ex) {
			ex.printStackTrace();

		}

		rootjson.put("alarms", jsonarray);
		rootjson.put("descr", descr);

		rootjson.put("pagenum", 1);
		rootjson.put("versionnum", versionNum);

		return rootjson.toJSONString();

	}

	public static List<List<nojuTrapLogTableRow>> createList(List<nojuTrapLogTableRow> targe, int size) {
		List<List<nojuTrapLogTableRow>> listArr = new ArrayList<List<nojuTrapLogTableRow>>();
		// 获取被拆分的数组个数
		int arrSize = targe.size() % size == 0 ? targe.size() / size : targe.size() / size + 1;
		for (int i = 0; i < arrSize; i++) {
			List<nojuTrapLogTableRow> sub = new ArrayList<nojuTrapLogTableRow>();
			// 把指定索引数据放入到list中
			for (int j = i * size; j <= size * (i + 1) - 1; j++) {
				if (j <= targe.size() - 1) {
					sub.add(targe.get(j));
				}
			}
			listArr.add(sub);
		}
		return listArr;
	}

	private String getHistoryoperlogs(JSONObject jsondata) {
		JSONObject rootjson = new JSONObject();
		JSONObject logjson;
		String descr = "";
		rootjson.put("cmd", jsondata.get("cmd").toString());
		JSONArray jsonarray = new JSONArray();
		Date datestart;
		Date dateend;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			datestart = sdf.parse(jsondata.get("start").toString());// 已经是零点
			dateend = sdf.parse(jsondata.get("end").toString());

			// 结束日的24点
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateend);
			calendar.add(calendar.DATE, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			dateend = calendar.getTime();

			ArrayList<nojuOperLogTableRow> traprow = this.logEngine.getOperRowsWithTime(datestart, dateend,
					jsondata.get("optname").toString());
			System.out.println("-------------traprow-size =" + traprow.size());
			for (nojuOperLogTableRow prow : traprow) {
				logjson = new JSONObject();
				logjson.put("id", prow.OperLogID);
				logjson.put("user", prow.OperLogUser);
				logjson.put("type", prow.OperLogType.toString());
				logjson.put("content", prow.OperLogContent);
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				logjson.put("time", sdf.format(prow.OperLogTime));
				jsonarray.add(logjson);
			}

			descr = sdf.format(datestart) + " --- " + sdf.format(datestart) + "  " + jsondata.get("optname").toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		rootjson.put("oplogs", jsonarray);
		rootjson.put("descr", descr);
		return rootjson.toJSONString();
	}

	private void parseAlarm(String message) {
		// System.out.println(" [x] CurrentAlarmModel Received: '" + message +
		// "'");
		nojuTrapLogTableRow newObj = null;
		try {
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String redStr = java.net.URLDecoder.decode(jsondata.get("val").toString(), "UTF-8");
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			newObj = (nojuTrapLogTableRow) objectInputStream.readObject();
			objectInputStream.close();
			byteArrayInputStream.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		insertTrapLog(newObj);
	}

	public nojuTrapLogTableRow insertTrapLog(TrapLogTypes type, String addr, String neName, String content, Date time,
			String paramName, String pValue, int pSlotIndex) {
		nojuTrapLogTableRow aCurrentrow = new nojuTrapLogTableRow(NlogType.getAlarmLevel(type), type, addr, neName,
				content, time, "", "", paramName, pValue);
		aCurrentrow.slotIndex = pSlotIndex;
		if (NlogType.getAlarmLevel(type) == 0) {
			aCurrentrow.TrapTreatMent = ClsLanguageExmp.commonGet("无需处理");
			aCurrentrow.isTreated = time.toString();
			if (aCurrentrow.TrapLogType == TrapLogTypes.TestOnline) {
				kickOffLineTrap(aCurrentrow);
			} else {
				kickParamTrap(aCurrentrow);
			}

		} else if (NlogType.getAlarmLevel(type) == 1 || NlogType.getAlarmLevel(type) == 2) {
			aCurrentrow.TrapTreatMent = "";
			aCurrentrow.isTreated = "";

			aCurrentrow.TrapLogID = this.logEngine.trapLogInsertRow(aCurrentrow);

			if (aCurrentrow.TrapLogType == TrapLogTypes.Offline) {
				coverOnlineTrap(aCurrentrow);

			} else {
				coverParamTrap(aCurrentrow);

			}

			allRows.add(aCurrentrow);
			// allRowsTable.put(aCurrentrow.TrapLogID, aCurrentrow);
			if (allRows.size() > MAX_TRAPNUMBER) {
				editTreatMent(allRows.get(0).TrapLogID, ClsLanguageExmp.commonGet("超时默认失效"));
			}

			JSONObject logjson = new JSONObject();
			logjson.put("cmd", "alarm_message");
			logjson.put("opt", true);
			logjson.put("id", aCurrentrow.TrapLogID);
			logjson.put("level", NlogType.getAlarmString(type));
			logjson.put("addr", aCurrentrow.TrapDevAddress);
			logjson.put("path", aCurrentrow.neName);
			logjson.put("type", aCurrentrow.TrapLogType.toString());
			logjson.put("paramname", aCurrentrow.parmName);
			logjson.put("paramvalue", aCurrentrow.paramValue);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logjson.put("eventtime", sdf.format(aCurrentrow.TrapLogTime));
			logjson.put("solved", aCurrentrow.TrapTreatMent);
			logjson.put("solvetime", aCurrentrow.isTreated);
			// System.out.println(" [x]------------------------------=" +
			// logjson.toJSONString());
			sendToQueue(logjson.toJSONString(), MAINKERNEL_MESSAGE);

		}

		return aCurrentrow;

	}

	public nojuTrapLogTableRow insertTrapLog(nojuTrapLogTableRow pRow) {
		return insertTrapLog(pRow.TrapLogType, pRow.TrapDevAddress, pRow.neName, pRow.TrapLogContent, pRow.TrapLogTime,
				pRow.parmName, pRow.paramValue, 0);

	}

	private void kickOffLineTrap(nojuTrapLogTableRow aCurrentrow) {
		int treatTid = -1;

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress)
					&& item.TrapLogType == TrapLogTypes.Offline) {
				treatTid = item.TrapLogID;
				break;

			}
		}

		if (treatTid != -1) {
			editTreatMent(treatTid, ClsLanguageExmp.commonGet("自动恢复"));
		}

	}

	private void kickParamTrap(nojuTrapLogTableRow aCurrentrow) {
		int treatTid = -1;

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress)
					&& item.parmName.equalsIgnoreCase(aCurrentrow.parmName)) {
				treatTid = item.TrapLogID;
				break;

			}
		}

		if (treatTid != -1) {
			editTreatMent(treatTid, ClsLanguageExmp.commonGet("自动恢复"));
		}

	}

	private void coverOnlineTrap(nojuTrapLogTableRow aCurrentrow) {
		int treatTid = -1;

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress)
					&& item.TrapLogType == TrapLogTypes.Offline) {
				treatTid = item.TrapLogID;
				break;

			}
		}

		if (treatTid != -1) {
			editTreatMent(treatTid, ClsLanguageExmp.commonGet("过时失效"));
		}

	}

	private void coverParamTrap(nojuTrapLogTableRow aCurrentrow) {
		int treatTid = -1;

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress)
					&& item.parmName.equalsIgnoreCase(aCurrentrow.parmName)) {
				treatTid = item.TrapLogID;
				break;

			}
		}

		if (treatTid != -1) {
			editTreatMent(treatTid, ClsLanguageExmp.commonGet("过时失效"));
		}

	}

	public void editTreatMent(int TrapLogID, String content) {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String IsTreatMent = sdf.format(new Date());

		try {
			int rst = this.logEngine.trapLogEditRow(TrapLogID, content, IsTreatMent);
		} catch (Exception e) {
			return;

		}

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapLogID == TrapLogID) {

				allRows.remove(item);
				// allRowsTable.remove(item.TrapLogID);

				invalidRows.add(item);
				// invalidRowsTable.put(item.TrapLogID, item);
				if (invalidRows.size() > MAX_TRAPNUMBER)// //超出最大值
				{
					// 丢入历史告警
					nojuTrapLogTableRow removeRow = invalidRows.get(0);
					invalidRows.remove(removeRow);
					// invalidRowsTable.remove(removeRow.TrapLogID);
				}

				item.isTreated = IsTreatMent;// new

				item.TrapTreatMent = content;

				JSONObject logjson = new JSONObject();
				logjson.put("cmd", "alarm_message");
				logjson.put("opt", false);
				logjson.put("id", item.TrapLogID);
				logjson.put("level", NlogType.getAlarmString(item.TrapLogType));
				logjson.put("addr", item.TrapDevAddress);
				logjson.put("path", item.neName);
				logjson.put("type", item.TrapLogType.toString());
				logjson.put("paramname", item.parmName);
				logjson.put("paramvalue", item.paramValue);
				sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				logjson.put("eventtime", sdf.format(item.TrapLogTime));
				logjson.put("solved", item.TrapTreatMent);
				logjson.put("solvetime", item.isTreated);
				// System.out.println(" [x]------------------------------=" +
				// logjson.toJSONString());
				sendToQueue(logjson.toJSONString(), MAINKERNEL_MESSAGE);

				break;
			}

		}

	}

	public void editTreatMentss(ArrayList<Integer> trapIDss, String content) {

		for (int i = 0; i < trapIDss.size(); i++) {
			editTreatMent(trapIDss.get(i), content);
		}

	}

	public void editTreatMentByAddr(String addr) {
		ArrayList<Integer> trapIDss = new ArrayList<Integer>();

		Iterator it1 = allRows.iterator();
		while (it1.hasNext()) {
			nojuTrapLogTableRow item = (nojuTrapLogTableRow) it1.next();
			if (item.TrapDevAddress.equalsIgnoreCase(addr)) {
				trapIDss.add(item.TrapLogID);
			}

		}

		editTreatMentss(trapIDss, ClsLanguageExmp.commonGet("璁惧琚垹闄ゅ悗娓呯┖"));

	}

	public void InsertOperLog(OperLogTypes type, String content, String usrName) {
		nojuOperLogTableRow row = new nojuOperLogTableRow(type, content, new Date(), usrName);

		row.OperLogID = this.logEngine.operLogInsertRow(row);

		if (row.OperLogID == -1) {
			return;
		}
		// send row to clinet
		JSONObject logjson = new JSONObject();
		logjson.put("cmd", "log_message");
		logjson.put("id", row.OperLogID);
		logjson.put("user", row.OperLogUser);
		logjson.put("type", row.OperLogType.toString());
		logjson.put("content", row.OperLogContent);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		logjson.put("time", sdf.format(row.OperLogTime));
		staticmemory.broadCast(logjson.toJSONString());

	}

	private void sendToQueue(String msg, String queue) {
		if (Sstatus.isRedis) {
			Jedis jedis = null;
			try {
				jedis = redisUtil.getConnection();
				jedis.publish(queue, msg);
				redisUtil.closeConnection(jedis);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				if (jedis != null)
					redisUtil.getJedisPool().returnBrokenResource(jedis);

			}

		} else {
			if (queue.equalsIgnoreCase(MAINKERNEL_MESSAGE)) {
				synchronized (SmsgList.storage) {
					SmsgList.storage.add(msg);
					SmsgList.storage.notify();

				}

			}

		}

	}

}
