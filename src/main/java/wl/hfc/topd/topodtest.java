package wl.hfc.topd;

import wl.hfc.common.*;



public class topodtest {
	
	
	public static void main(String args[]) {
		System.out.println("Hello World!");
		CDatabaseEngine	ICDatabaseEngine1=new CDatabaseEngine();
		ICDatabaseEngine1.getConnection();
		
		DevGrpModel md1DevGrpModel=new DevGrpModel(ICDatabaseEngine1);
		md1DevGrpModel.initTopodData();
	}

}
