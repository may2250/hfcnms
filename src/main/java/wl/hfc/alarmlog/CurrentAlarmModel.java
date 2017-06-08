package wl.hfc.alarmlog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import wl.hfc.common.*;
import wl.hfc.common.NlogType.TrapLogTypes;

public class CurrentAlarmModel 
{
    public static CurrentAlarmModel me;
    public static int MAX_TRAPNUMBER = 500;

  // private log4net.ILog ErrorLog;
  //  private log4net.ILog SysLog;


   public CDatabaseEngine logEngine;

    //the real model,current trap rows
    public ArrayList<nojuTrapLogTableRow> allRows;
    public Hashtable allRowsTable;


    public ArrayList<nojuTrapLogTableRow> invalidRows;
    public Hashtable invalidRowsTable;


    private Thread ptd;

    public CurrentAlarmModel(CDatabaseEngine dEngine)
    {    	
        this.logEngine = dEngine;
        allRows = new ArrayList<nojuTrapLogTableRow>();
        allRowsTable = new Hashtable();

        invalidRows = new ArrayList<nojuTrapLogTableRow>();
        invalidRowsTable = new Hashtable();
        me = this;

    }


    //public CurrentAlarmModel(FTrapLogRt view)
    //{
    //    this.view = view;
    //    allRows = new List<nojuTrapLogTableRow>();

    //}

    public nojuTrapLogTableRow insertTrapLog(nojuTrapLogTableRow pRow)
    {
        return insertTrapLog(pRow.TrapLogType, pRow.TrapDevAddress, pRow.neName, pRow.TrapLogContent, pRow.TrapLogTime, "", "", 0);

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
                    
                    
                    //通知告警日志新增该无效告警                 
/*                    if (view3 != null)
                        view3.appendOneInvalidTrap(item);*/

              
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


}




