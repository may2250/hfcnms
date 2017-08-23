package wl.hfc.online;

import java.io.IOException;
import java.net.InetAddress;

import java.util.Hashtable;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.util.*;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import wl.commonComponent.DeviceSearchEngine;
import wl.hfc.common.DevTopd;

//DevGrpModel将承担拓扑的组建，维护，以及组，设备的增删查改的响应
public class ParamKernel extends Thread {
	private static final String PARAMKERNEL_MESSAGE = "paramkernel.message";
	private static Logger log = Logger.getLogger(ParamKernel.class);
	public Hashtable listDevHash;

	public static ParamKernel me;

	public ParamKernel() {
		new pmls();
		this.setName("ParamKernel");
		me = this;

	}

	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		ParamKernel.redisUtil = redisUtil;
	}

	public static void setStaticMemory(StaticMemory staticmemory) {
		ParamKernel.staticmemory = staticmemory;
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

		System.out.println(" [x] ParamKernel Received: '" + message + "'");

		JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		String cmd = jsondata.get("cmd").toString();

		if (cmd.equalsIgnoreCase("hfcvalueset")) {
			hfcValueSet(jsondata);
		} else if (cmd.equalsIgnoreCase("getdevicedetail")) {//clinet open device view
			if (!jsondata.get("predev").toString().equalsIgnoreCase("")) {
				staticmemory.removeRealTimeDev(jsondata.get("predev").toString(), jsondata.get("sessionid").toString());
			}
			DevTopd lNode = (DevTopd) listDevHash.get(jsondata.get("ip").toString());
			jsondata.put("nojuhfctype", lNode.HFCType1.ordinal());
			jsondata.put("deviceid", lNode.DEVICEID);
			jsondata.put("md", lNode.MD);
			staticmemory.addRealTimeDev(jsondata);
		} else if (cmd.equalsIgnoreCase("deviceclose")) {
			staticmemory.removeRealTimeDev(jsondata.get("ip").toString(), jsondata.get("sessionid").toString());
		} else if (cmd.equalsIgnoreCase("devsearch")) {
			devSerach(jsondata);
		}
	}

	private void hfcValueSet(JSONObject jsondata) {
		JSONObject rootjson = new JSONObject();
		String target = jsondata.get("target").toString();
		String netaddr = jsondata.get("ip").toString();
		ObjSnmpPreail osp = staticmemory.getRealTimeDev(netaddr);
		// WosBaseSnmp snmpPreail = osp.snmpPreail;
		if (target.equalsIgnoreCase("setVars")) {

			if (jsondata.get("isRow").toString().equalsIgnoreCase("false")) {// 普通参数

				osp.snmpPreail.setVars(jsondata.get("domstr").toString(), jsondata.get("value").toString());
			} else// 表内参数
			{
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.setTableVars(jsondata.get("domstr").toString(), jsondata.get("value").toString(), row);

			}
			// staticmemory.sendRemoteStr(rootjson.toJSONString(),
			// jsondata.get("sessionid").toString());
		} else if (target.equalsIgnoreCase("setTrapHost")) {

			String rowString = jsondata.get("rowNum").toString();
			int row = Integer.parseInt(rowString);
			osp.commonSnmpPreail.setStringVars(jsondata.get("domstr").toString(), jsondata.get("value").toString(),
					row);

		} else if (target.equalsIgnoreCase("getalarmThreshold")) {

			if (jsondata.get("isRow").toString().equalsIgnoreCase("false")) {// 普通参数

				osp.snmpPreail.getSubVarsBYparamname(jsondata.get("domstr").toString(), rootjson);
			} else// 表内参数
			{
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.getSubVarsBYparamname(jsondata.get("domstr").toString(), rootjson, row);

			}
			jsondata.put("detail", rootjson);
			staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());
		} else if (target.equalsIgnoreCase("setalarmThreshold")) {

			rootjson.put("hihi", jsondata.get("HIHI").toString());
			rootjson.put("hi", jsondata.get("HI").toString());
			rootjson.put("lo", jsondata.get("LO").toString());
			rootjson.put("lolo", jsondata.get("LOLO").toString());
			rootjson.put("deadb", jsondata.get("DEAD").toString());
			byte en = 0;
			if (jsondata.get("ISLOLO").toString().equalsIgnoreCase("true")) {
				en = (byte) (en | 0x01);
			}
			if (jsondata.get("ISLO").toString().equalsIgnoreCase("true")) {
				en = (byte) (en | 0x02);
			}
			if (jsondata.get("ISHI").toString().equalsIgnoreCase("true")) {
				en = (byte) (en | 0x04);
			}
			if (jsondata.get("ISHIHI").toString().equalsIgnoreCase("true")) {
				en = (byte) (en | 0x08);
			}
			rootjson.put("en", en);
			if (jsondata.get("isRow").toString().equalsIgnoreCase("false")) {// 普通参数

				osp.snmpPreail.setSubVarsBYparamname(jsondata.get("domstr").toString(), rootjson);
			} else// 表内参数
			{
				String rowString = jsondata.get("rowNum").toString();
				int row = Integer.parseInt(rowString);
				osp.snmpPreail.setSubVarsTableBYparamname(jsondata.get("domstr").toString(), rootjson, row);

			}
		}
	}

	private void devSerach(JSONObject jsondata) throws NumberFormatException, IOException {
		String devtype = jsondata.get("devtype").toString();

		SearchIpInfo searchinfo = new SearchIpInfo(InetAddress.getByName(jsondata.get("startip").toString()),
				InetAddress.getByName(jsondata.get("endip").toString()), false, Integer.parseInt(devtype));
		searchinfo.sessionid = jsondata.get("sessionid").toString();

		if (!DeviceSearchEngine.isInSerchProgress) {
			DeviceSearchEngine.isInSerchProgress = true;
			DeviceSearchEngine dse = new DeviceSearchEngine(searchinfo, staticmemory);
			dse.start();
		}

	}

	public void run() {

		log.info(this.getName() + "....starting.......");
		Jedis jedis = null;
		try {

			jedis = redisUtil.getConnection();
			jedis.psubscribe(jedissubSub, PARAMKERNEL_MESSAGE);
			redisUtil.getJedisPool().returnResource(jedis);

		} catch (Exception e) {
			e.printStackTrace();
			redisUtil.getJedisPool().returnBrokenResource(jedis);

		}

	}

}
