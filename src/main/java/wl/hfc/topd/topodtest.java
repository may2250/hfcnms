package wl.hfc.topd;

import java.util.Hashtable;
import java.util.concurrent.Delayed;

import org.json.simple.JSONObject;

import wl.hfc.common.*;
import wl.hfc.online.PDUServer;



public class topodtest {
	
	
	public static void main(String args[]) {
		System.out.println("Hello World!");
		CDatabaseEngine	ICDatabaseEngine1=new CDatabaseEngine();
		ICDatabaseEngine1.getConnection();
		Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
		Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();
		
		
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
	
	
	
	MainKernel md1DevGrpModel=new MainKernel(ICDatabaseEngine1);
	md1DevGrpModel.initTopodData();
	
	
	//md1DevGrpModel.handleInsertGrp(new JSONObject());
	
	//md1DevGrpModel.handleDelGrp(new JSONObject());
//	md1DevGrpModel.handleUpdGrp(new JSONObject());
	
	//md1DevGrpModel.handleInsertDev(new JSONObject());
	
	
	//md1DevGrpModel.handleUpdateDev(new JSONObject());
	//md1DevGrpModel.handleDeleteDev(new JSONObject());
	
	

	}

}
