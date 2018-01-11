package com.xinlong.Services;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.xinlong.util.RedisUtil;
import com.xinlong.util.StaticMemory;
import com.xinlong.util.Uhandle;

import redis.clients.jedis.Jedis;
import wl.hfc.common.CDatabaseEngine;
import wl.hfc.server.SmsgList;
import wl.hfc.server.Sstatus;
import wl.hfc.traprcss.TrapPduServer;

// @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成		一个websocket服务器端,
@ServerEndpoint("/websocketservice/{username}/{password}")
public class Services_Websocket {
	private static Logger log = Logger.getLogger(Services_Websocket.class);
	private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
	private static final String PARAMKERNEL_MESSAGE = "paramkernel.message";
	private static final String HFCALARM_MESSAGE = "currentalarm.message";

	
	
	private static final String deskey = "prevail0";
	private static RedisUtil redisUtil;
	private static StaticMemory staticmemory;

	public static void setRedisUtil(RedisUtil redisUtil) {
		Services_Websocket.redisUtil = redisUtil;
	}

	public static void setStaticMemory(StaticMemory staticmemory) {	
		Services_Websocket.staticmemory = staticmemory;
	}

	
//	在使用spring框架配置AOP的时候，不管是通过XML配置文件还是注解的方式都需要定义pointcut"切入点"  
//	例如定义切入点表达式 execution(* com.sample.service.impl..*.*(..))  
//	execution()是最常用的切点函数，其语法如下所示：  
//	 整个表达式可以分为五个部分：  
//	 1、execution(): 表达式主体。  
//	 2、第一个*号：表示返回类型，*号表示所有的类型。  
//	 3、包名：表示需要拦截的包名，后面的两个句点表示当前包和当前包的所有子包，com.sample.service.impl包、子孙包下所有类的方法。  
//	 4、第二个*号：表示类名，*号表示所有的类。  
//	 5、*(..):最后这个星号表示方法名，*号表示所有的方法，后面括弧里面表示方法的参数，两个句点表示任何
	
/*	public Services_Websocket()
	{		
		new SmsgList();
	}*/
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, InterruptedException {

		// Print the client message for testing purposes
		log.debug("Received: '" + message + "'");
		parseWebMessage(message, session);		

	}

	private void sendToQueue(String msg, String queue) {

		if (Sstatus.isRedis) {
			Jedis jedis = null;
			try {
				jedis = redisUtil.getConnection();
				jedis.publish(queue, msg);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} finally {
				redisUtil.closeConnection(jedis);
			}

		} else {
			

			if (queue.equalsIgnoreCase(MAINKERNEL_MESSAGE)) {
				synchronized (SmsgList.storage) {
					SmsgList.storage.add(msg);
					SmsgList.storage.notify();

				}
			}

			if (queue.equalsIgnoreCase(HFCALARM_MESSAGE)) {
				synchronized (SmsgList.alarmstorage) {
					SmsgList.alarmstorage.add(msg);
					SmsgList.alarmstorage.notify();

				}

			}

			if (queue.equalsIgnoreCase(PARAMKERNEL_MESSAGE)) {
				synchronized (SmsgList.paknelstorage) {
					SmsgList.paknelstorage.add(msg);
					SmsgList.paknelstorage.notify();

				}

			}

		}

		
	}

	@OnOpen//他们定义了当一个新用户连接和断开的时候所调用的方法。
	public void onOpen(@PathParam("username") String username, @PathParam("password") String password,
			Session session) {

		log.info("onOpen   id:" + session.getId());
		JSONObject rootjson = new JSONObject();
		rootjson.put("cmd", "loginAuth");
		if (username.equalsIgnoreCase("undefined") || password.equalsIgnoreCase("undefined")) {
			rootjson.put("Authed", false);
			try {
				session.getBasicRemote().sendText(rootjson.toJSONString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				// 解密
				String passWord = password;// decrypt(password, deskey);
						// 用户认证
				if (staticmemory.getSessionByuser(username)) {
					// 已有同名用户登录
					rootjson.put("Authed", false);
					rootjson.put("desc", "User Already login!");
					session.getBasicRemote().sendText(rootjson.toJSONString());
				} else {
					Uhandle usession = new Uhandle(username, session);
					staticmemory.AddSession(usession);// 增加客户端名称
		
					// send to mainkernel to auth this user
					rootjson.put("sessionid", session.getId());
					rootjson.put("username", username);
					rootjson.put("password", passWord);
					sendToQueue(rootjson.toJSONString(), MAINKERNEL_MESSAGE);

					/*
					 * // for syslog rootjson = new JSONObject(); rootjson.put("cmd", "userlogin");
					 * rootjson.put("title",username); rootjson.put("operater",username);
					 * sendToQueue(rootjson.toJSONString(), "currentalarm.message");
					 */
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@OnError
	public void onError(Throwable throwable) {
		System.out.println(throwable.getMessage());
	}

	@OnClose
	public void onClose(Session session) {

		log.info("onClose   id:" + session.getId());
		staticmemory.RemoveSession(session);
		// System.out.println("Connection closed::::" + staticmemory.getCount());
	}

	@SuppressWarnings("unchecked")
	private void parseWebMessage(String message, Session session) throws IOException, InterruptedException {
		try {
			JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
			String cmd = jsondata.get("cmd").toString();
			// JSONObject rootjson = new JSONObject();
			if (cmd.equalsIgnoreCase("getgrouptree")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("nodeadd")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("nodeedit")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("nodedel")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("lazyLoad")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("deviceadd")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("hfcvalueset")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("getdevicedetail")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("devsearch")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("deviceclose")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), PARAMKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("alarmsearch")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), HFCALARM_MESSAGE);
			} else if (cmd.equalsIgnoreCase("optlogsearch")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), HFCALARM_MESSAGE);
			} else if (cmd.equalsIgnoreCase("getuserlist")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("handleuser")) {
				jsondata.put("sessionid", session.getId());
				sendToQueue(jsondata.toJSONString(), MAINKERNEL_MESSAGE);
			} else if (cmd.equalsIgnoreCase("severstatus")) {
				jsondata.put("sessionid", session.getId());

				// trap监听模块
				jsondata.put("TrapPduServerstatus", TrapPduServer.TrapPduServer_status);// trap listen status

				// 数据库状态
				jsondata.put("CDatabaseEngineflag", CDatabaseEngine.flag);// last time database status

				// 已连接客户端数量
				jsondata.put("clientNum", StaticMemory.webSocketClients.size());// pduserver init

				// redis连接状态
				// jsondata.put("redisStatus",Sstatus.redisStartus);// pduserver init

				staticmemory.sendRemoteStr(jsondata.toJSONString(), jsondata.get("sessionid").toString());

			} else {
				sendToQueue(message, MAINKERNEL_MESSAGE);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e.getMessage());
		}
	}

}