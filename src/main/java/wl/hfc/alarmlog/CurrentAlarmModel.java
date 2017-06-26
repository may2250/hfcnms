package wl.hfc.alarmlog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.xinlong.util.RedisUtil;

import wl.hfc.common.*;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.online.pmls;
import wl.hfc.traprcss.TrapPduServer;
import wl.hfc.traprcss.TrapProCenter;


//CurrentAlarmModel负责告警的插入，历史告警的查询，并向前端推送
public class CurrentAlarmModel extends Thread
{
	private static final String  MAINKERNEL_MESSAGE =  "mainkernel.message";
    public static CurrentAlarmModel me;
    public static int MAX_TRAPNUMBER = 500;
    private static Logger log = Logger.getLogger(CurrentAlarmModel.class);
    private static final String  HFCALARM_MESSAGE =  "currentalarm.message" ;
  // private log4net.ILog ErrorLog;
  //  private log4net.ILog SysLog;


   public CDatabaseEngine logEngine;

    //the real model,current trap rows
    public CopyOnWriteArrayList<nojuTrapLogTableRow> allRows;
    public Hashtable allRowsTable;


    public ArrayList<nojuTrapLogTableRow> invalidRows;
    public Hashtable invalidRowsTable;

    private static RedisUtil redisUtil;
    private Thread ptd;

    public CurrentAlarmModel(CDatabaseEngine dEngine, RedisUtil redisUtil)
    {    	
        this.logEngine = dEngine;
        allRows = new CopyOnWriteArrayList<nojuTrapLogTableRow>();
        allRowsTable = new Hashtable();

        invalidRows = new ArrayList<nojuTrapLogTableRow>();
        invalidRowsTable = new Hashtable();
        this.redisUtil = redisUtil;
        
        me = this;        
    }
    
    public void run(){
    	Jedis jedis=null;
        jedis = redisUtil.getConnection();		 
		jedis.psubscribe(jedissubSub, HFCALARM_MESSAGE);
		redisUtil.getJedisPool().returnResource(jedis); 
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
  			parseMessage(msg);
  			
  		}catch(Exception e){
  			e.printStackTrace();	
  			log.info(e.getMessage());
  		}
  		
      }

	};
	
	private void parseMessage(String message){
		//System.out.println(" [x] CurrentAlarmModel Received: '" + message + "'");	
		nojuTrapLogTableRow newObj = null;
		try {
			String redStr = java.net.URLDecoder.decode(message, "UTF-8");  
	        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));  
	        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);    
			newObj = (nojuTrapLogTableRow)objectInputStream.readObject();
			objectInputStream.close();  
	        byteArrayInputStream.close();
		} catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
		/*JSONObject jsondata = (JSONObject) new JSONParser().parse(message);
		int level = Integer.parseInt(jsondata.get("level").toString()); 
		TrapLogTypes TrapLogType = (TrapLogTypes)jsondata.get("TrapLogType"); 
		String addr = jsondata.get("TrapDevAddress").toString(); 
		String neName = jsondata.get("neName").toString(); 
		String TrapLogContent = jsondata.get("TrapLogContent").toString(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");  
	    Date TrapLogTime = sdf.parse(jsondata.get("TrapLogTime").toString());  
	    String TrapTreatMent = jsondata.get("TrapTreatMent").toString();
	    String TrapTreatMent = jsondata.get("TrapTreatMent").toString(); 
		nojuTrapLogTableRow pRow = new nojuTrapLogTableRow(level,TrapLogType,addr,neName,TrapLogContent,TrapLogTime,TrapTreatMent,false,);
		*/
		//这里开始处理告警 
        insertTrapLog(newObj);
	}
    
    public nojuTrapLogTableRow insertTrapLog(nojuTrapLogTableRow pRow)
    {
        return insertTrapLog(pRow.TrapLogType, pRow.TrapDevAddress, pRow.neName, pRow.TrapLogContent, pRow.TrapLogTime,pRow.parmName, pRow.paramValue, 0);

    }

    private void kickOffLineTrap(nojuTrapLogTableRow aCurrentrow)
    {
        int treatTid = -1;
        
        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress) && item.TrapLogType == TrapLogTypes.Offline)
            {
                treatTid = item.TrapLogID;
                break;

            }
        }
        


        if (treatTid != -1)
        {
            editTreatMent(treatTid, ClsLanguageExmp.commonGet("自动恢复"));
        }

    }



    private void kickParamTrap(nojuTrapLogTableRow aCurrentrow)
    {
        int treatTid = -1;
        
        
        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress) && item.parmName.equalsIgnoreCase(aCurrentrow.parmName))
            {
                treatTid = item.TrapLogID;
                break;

            }
        }        


        if (treatTid != -1)
        {
            editTreatMent(treatTid, ClsLanguageExmp.commonGet("自动恢复"));
        }

    }


    private void coverOnlineTrap(nojuTrapLogTableRow aCurrentrow)
    {
        int treatTid = -1;
        
        

        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress) && item.TrapLogType == TrapLogTypes.Offline)
            {
                treatTid = item.TrapLogID;
                break;

            }
        }        


        if (treatTid != -1)
        {
            editTreatMent(treatTid, ClsLanguageExmp.commonGet("过时失效"));
        }



    }


    private void coverParamTrap(nojuTrapLogTableRow aCurrentrow)
    {
        int treatTid = -1;

        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapDevAddress.equalsIgnoreCase(aCurrentrow.TrapDevAddress) &&  item.parmName.equalsIgnoreCase(aCurrentrow.parmName))
            {
                treatTid = item.TrapLogID;
                break;

            }
        }        

 

        if (treatTid != -1)
        {
            editTreatMent(treatTid, ClsLanguageExmp.commonGet("过时失效"));
        }


    }


    private int commonLogIns = -1;
    public nojuTrapLogTableRow insertTrapLog(TrapLogTypes type, String addr, String neName, String content, Date time, String paramName, String pValue, int pSlotIndex)
    {    	
        nojuTrapLogTableRow aCurrentrow = new nojuTrapLogTableRow(NlogType.getAlarmLevel(type), type, addr, neName, content, time, "", "", paramName, pValue);
        aCurrentrow.slotIndex = pSlotIndex;
        if (NlogType.getAlarmLevel(type) == 0)
        {
            aCurrentrow.TrapTreatMent = ClsLanguageExmp.commonGet("无需处理");
            aCurrentrow.isTreated = time.toString();
            if (aCurrentrow.TrapLogType == TrapLogTypes.TestOnline)
            {
                kickOffLineTrap(aCurrentrow);
            }
            else
            {
                kickParamTrap(aCurrentrow);
            }

        }
        else if (NlogType.getAlarmLevel(type) == 1 || NlogType.getAlarmLevel(type) == 2)
        {
            aCurrentrow.TrapTreatMent = "";
            aCurrentrow.isTreated = "";
            //日志不记录0级别告警
            //int newTrapid = this.dEngine.trapLogInsertRow(aCurrentrow);
            //if (newTrapid==-1)
            //{
            //    return
            //}
            aCurrentrow.TrapLogID = this.logEngine.trapLogInsertRow(aCurrentrow);

            if (aCurrentrow.TrapLogType == TrapLogTypes.Offline)
            {
                coverOnlineTrap(aCurrentrow);

            }
            else
            {
                coverParamTrap(aCurrentrow);

            }

            allRows.add(aCurrentrow);
            allRowsTable.put(aCurrentrow.TrapLogID, aCurrentrow);
            if (allRows.size() > MAX_TRAPNUMBER)
            {
                ////当前告警数量超过最大值，强制处理纳入数据库
                editTreatMent(allRows.get(0).TrapLogID, ClsLanguageExmp.commonGet("超时默认失效"));
            }
            
            //hi ,xinglong ,把该新告警发送到前端，在客户端的告警列表增加该告警
            JSONObject logjson = new JSONObject();
            logjson.put("cmd", "alarm_message");
            logjson.put("opt", true);
            logjson.put("id", aCurrentrow.TrapLogID);
    		logjson.put("level", aCurrentrow.level);
    		//logjson.put("source", aCurrentrow.neName);
    		logjson.put("path", "grp1/xxxx");
    		logjson.put("type", aCurrentrow.TrapLogType.toString());
    		logjson.put("paramname", aCurrentrow.parmName);
    		logjson.put("paramvalue", aCurrentrow.paramValue);
    		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
    		logjson.put("eventtime", sdf.format(aCurrentrow.TrapLogTime));
    		logjson.put("solved", aCurrentrow.TrapTreatMent);
    		logjson.put("solvetime", aCurrentrow.isTreated);
    		//System.out.println(" [x]------------------------------=" + logjson.toJSONString());
    		sendToQueue(logjson.toJSONString(), MAINKERNEL_MESSAGE);
            
            //通知 客户端
 /*           if (view1 != null)
                view1.appendnewTrapLogRow(aCurrentrow);
            if (view2 != null)
                view2.appendnewTrapLogRow(aCurrentrow);
            if (view3 != null)
                view3.appendOneTrap(aCurrentrow);
            if (viewDevGrpModel != null)
                viewDevGrpModel.appendOneTrap(aCurrentrow);
            if (smtpEngine != null)
            {
                WiseCommand cmd = new WiseCommand(aCurrentrow.TrapLogContent, CMDType.catchNewTrap);
                cmd.Property2 = aCurrentrow.neName + "  " + aCurrentrow.TrapDevAddress;//path
                smtpEngine.EnqueueCmd(cmd);
            }

*/

        }





        return aCurrentrow;

    }

    public void editTreatMent(int TrapLogID, String content)
    {
        //DataRow ros;
        try
        {
            int rst = logEngine.trapLogEditRow(TrapLogID, content);
        }
        catch (Exception ex)
        {
            return;

        }

        
        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapLogID == TrapLogID)
            {
         
                    allRows.remove(item);
                    allRowsTable.remove(item.TrapLogID);
                    
                    //通知拓扑树删除该告警
                 /*   if (view3 != null)
                        view3.TreatedTrap(item);
                    if (viewDevGrpModel != null)
                        viewDevGrpModel.TreatedTrap(item);*/

           
            
                    invalidRows.add(item);
                    invalidRowsTable.put(item.TrapLogID, item);
                    if (invalidRowsTable.size() > MAX_TRAPNUMBER)//超出最大值
                    {
                        //丢入历史告警
                        nojuTrapLogTableRow removeRow = invalidRows.get(0);
                        invalidRows.remove(removeRow);
                        invalidRowsTable.remove(removeRow.TrapLogID);
                    }
    
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    item.isTreated=df.format(new Date());// new Date()为获取当前系统时间
                    item.TrapTreatMent = content;
                    
                    
                    //hi ,xinglong ,把该失效（过时）的告警发送到前端，在客户端的告警列表中删除对应ID的告警
                    JSONObject logjson = new JSONObject();
                    logjson.put("cmd", "alarm_message");
                    logjson.put("opt", false);
                    logjson.put("id", item.TrapLogID);
            		logjson.put("level", item.level);
            		logjson.put("path", "grp1/xxxx");
            		logjson.put("type", item.TrapLogType.toString());
            		logjson.put("paramname", item.parmName);
            		logjson.put("paramvalue", item.paramValue);
            		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");  
            		logjson.put("eventtime", sdf.format(item.TrapLogTime));
            		logjson.put("solved", item.TrapTreatMent);
            		logjson.put("solvetime", item.isTreated);
            		//System.out.println(" [x]------------------------------=" + logjson.toJSONString());
            		sendToQueue(logjson.toJSONString(), MAINKERNEL_MESSAGE);
              
                break;
            }

        }        

        


        //int xss = int.MaxValue;

    }

    public void editTreatMentss(ArrayList<Integer> trapIDss, String content)
    {
    	
    	for (int i = 0; i < trapIDss.size(); i++) {
    		 editTreatMent(trapIDss.get(i), content);
		}
  

    }

    public void editTreatMentByAddr(String addr)
    {
    	ArrayList<Integer>  trapIDss = new ArrayList<Integer> ();


        Iterator it1 = allRows.iterator();
        while(it1.hasNext()){
        	nojuTrapLogTableRow item=(nojuTrapLogTableRow)it1.next();
            if (item.TrapDevAddress.equalsIgnoreCase(addr))
            {
            	trapIDss.add(item.TrapLogID);
            }            
            
        }

        editTreatMentss(trapIDss, ClsLanguageExmp.commonGet("设备被删除后清空"));
        
        
    }

    public String getPath()
    {

        return "";
    }



    public void clearAllTrapLogRows()
    {
        this.allRows.clear();
        allRowsTable.clear();  



    }




    public void addLogTest()
    {
        TrapLogTypes type = TrapLogTypes.Lo; // TODO: Initialize to an appropriate value
        String addr = "192.168.1.13"; // TODO: Initialize to an appropriate value
        String content = "a test trap"; // TODO: Initialize to an appropriate value
        Date time = new Date(); // TODO: Initialize to an appropriate value
       // this.insertTrapLog(type, addr, "myNeName", content, time);
    }


    public void notifyTrap()
    {
/*        if (view3 != null)
            view3.appendTRAP(this.allRows);*/
    }

/*
    public List<nojuTrapLogTableRow> getallrows()
    {
        return this.allRows;

    }*/

    private void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(queue, msg);
			redisUtil.closeConnection(jedis);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			if(jedis != null)
				redisUtil.getJedisPool().returnBrokenResource(jedis);

		}
	}
}




