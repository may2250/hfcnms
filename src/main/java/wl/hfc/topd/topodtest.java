package wl.hfc.topd;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Delayed;

import org.json.simple.JSONObject;

import com.xinlong.util.RedisUtil;

import wl.hfc.alarmlog.CurrentAlarmModel;
import wl.hfc.common.*;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.online.PDUServer;



public class topodtest {
	
	
	public static void main(String args[]) {
		System.out.println("Hello World!");
		RedisUtil redisUtil=new RedisUtil();
		CDatabaseEngine	ICDatabaseEngine1=new CDatabaseEngine(redisUtil);
		//ICDatabaseEngine1.getConnection();
		try {
			Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
			Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();
			
			  ArrayList<nojuUserAuthorizeTableRow> rowssss=ICDatabaseEngine1.UserAuthorizeTableGetAllRows();
				int rossns=1;
				
				
				rossns++;
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	

		//grp test
//	UserGroupTableRow row=new UserGroupTableRow(999,"bb8",-1);
//	ICDatabaseEngine1.UserGroupTableInsertRow(row);
	
	
	//edit grp
//	row.UserGroupName=	row.UserGroupName+"index";
	//ICDatabaseEngine1.UserGroupTableUpdateRow(row);
		
	
	//del grp
		//ICDatabaseEngine1.UserGroupTableDeleteRow(5);
	
	
	// dev test
	//nojuDeviceTableRow row=new nojuDeviceTableRow("1.1.1.1",NetTypes.other);
	//ICDatabaseEngine1.DeviceTableInsertRow(row);
	
	//row.Name="123123";
	//ICDatabaseEngine1.DeviceTableUpdateRow(row);
	
	//ICDatabaseEngine1.DeviceTableDeleteRow(row);
	
		
		
		
		//log test
    //nojuTrapLogTableRow aCurrentrow = new nojuTrapLogTableRow(1, TrapLogTypes.Lo, "1.1.1.1", "Nename", "content", new Date(), "", "", "paramName", "pvalue");
	//ICDatabaseEngine1.trapLogInsertRow(aCurrentrow);
	
		
	//CurrentAlarmModel CurrentAlarmModel1=new CurrentAlarmModel(ICDatabaseEngine1,redisUtil);
	//CurrentAlarmModel1.insertTrapLog( TrapLogTypes.Lo, "1.1.1.1", "neName", "content", new Date());
	
	//CurrentAlarmModel1.insertTrapLog( TrapLogTypes.Lo, "1.1.1.1", "neName", "content", new Date());
//	MainKernel md1DevGrpModel=new MainKernel(ICDatabaseEngine1);
	//md1DevGrpModel.initTopodData();
	
	
	//md1DevGrpModel.handleInsertGrp(new JSONObject());
	
	//md1DevGrpModel.handleDelGrp(new JSONObject());
//	md1DevGrpModel.handleUpdGrp(new JSONObject());
	
	//md1DevGrpModel.handleInsertDev(new JSONObject());
	
	
	//md1DevGrpModel.handleUpdateDev(new JSONObject());
	//md1DevGrpModel.handleDeleteDev(new JSONObject());
	
	


	}

}
