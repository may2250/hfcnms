package wl.hfc.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

/*import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.PreparedStatement;*/

public class CDatabaseEngine {


	public  Connection getConnection() {
		String url = "jdbc:mysql://localhost:3306/hfctraplogs";
		// ����������
		String driver = "com.mysql.jdbc.Driver";
		String dbuser = "bobo";
		String dbpass = "123456";
		try {
			if (con == null) {
				Class.forName(driver);
				con = DriverManager.getConnection(url, dbuser, dbpass);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			// log.info(e.getMessage());
		}

		return con;
	}

	public  Connection con;

	
	
	
    public int isDevGroupExsit(String gpName)
    {

        Hashtable groupLists = UserGroupTableGetAllRows();
        
        
    	Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow item = (UserGroupTableRow) e.nextElement();

			if (item.UserGroupName.equals(gpName)) {
			     return item.UserGroupID;
			}

		}
        

        return -1;

    }
	public  int UserGroupTableInsertRow(UserGroupTableRow row) {

		int copyIndex = 1;
		String newName = row.UserGroupName;
		ResultSet rs = null;
		int lastId = -1;
		
		  while (isDevGroupExsit(newName) != -1) { newName = row.UserGroupName
		  + "(" + copyIndex + ")"; copyIndex++;
		  
		  }
		 
		row.UserGroupName = newName;

		String sqlInsert = "INSERT INTO UserGroupTable(UserGroupName,ParentGroupID) VALUES('" + row.UserGroupName + "'," + row.ParentGroupID + ')';

		//sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert,Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.UserGroupID = lastId;
			return row.UserGroupID;
		} catch (Exception EX) {
			System.out.println(EX);
			return lastId;

		}

	}
	public  boolean UserGroupTableDeleteRow(int thisID){


		  String sqlInsert = "DELETE FROM UserGroupTable WHERE UserGroupID=" + thisID;
		//sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);			
	         if (pstmt.executeUpdate() > 0)
                 return true;
	
		} catch (Exception EX) {
			  sqlInsert = "DELETE FROM UserGroupTable WHERE UserGroupID=" + thisID;
			return false;

		}
		
		return false;

	}
	
	public  boolean UserGroupTableUpdateRow(UserGroupTableRow row){

        if (isDevGroupExsit(row.UserGroupName) != -1)
        {
            if (isDevGroupExsit(row.UserGroupName) != row.UserGroupID)
            {
                return false;//直接不允许修改
            }
        }

        String sqlInsert = "UPDATE UserGroupTable SET UserGroupName='" + row.UserGroupName +
                "',ParentGroupID=" + row.ParentGroupID +
                ",Txa=" + row.x1 + ",Txb=" + row.x2 + ",Txc=" + row.y1 + ",Txd=" + row.y2+ ",isTx=" + (row.isTx?"1":"0") +
                " WHERE UserGroupID=" + row.UserGroupID;

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);			
	         if (pstmt.executeUpdate() > 0)
               return true;
	
		} catch (Exception EX) {

		}
		
		return false;

	}
	public  Hashtable UserGroupTableGetAllRows() {
		PreparedStatement pstmt;
		Hashtable retList = new Hashtable();
		ResultSet rs = null;
		try {
			String sqlInsert = "SELECT * FROM UserGroupTable";

			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			rs = pstmt.executeQuery(sqlInsert);

			while (rs.next()) {
				UserGroupTableRow tmpRow = new UserGroupTableRow(rs.getInt(1), rs.getString(2), rs.getInt(3));

				int i = 3;
				tmpRow.x1 = rs.getFloat(i++);
				tmpRow.x2 = rs.getFloat(i++);
				tmpRow.y1 = rs.getFloat(i++);
				tmpRow.y2 = rs.getFloat(i++);
				int tmpInt = rs.getInt(i++);
				if (tmpInt > 0) {
					tmpRow.isTx = true;
				} else {

					tmpRow.isTx = false;
				}
				retList.put(tmpRow.UserGroupID, tmpRow);

			}

		} catch (Exception ex) {

			String xxxString = ex.toString();
		}

		return retList;
	}

	public  Hashtable DeviceTableGetAllRows() {
		PreparedStatement pstmt;
		Hashtable retList = new Hashtable();
		ResultSet rs = null;
		try {
			String sqlInsert = "SELECT * FROM DeviceTable";

			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			rs = pstmt.executeQuery(sqlInsert);

			while (rs.next()) {
				nojuDeviceTableRow tmpRow = new nojuDeviceTableRow(rs.getString(1), NetTypes.values()[ rs.getInt(2)]);

				int i = 3;

				tmpRow.UserGroupID = rs.getInt(i++);
				tmpRow.HeadAddress = rs.getString(i++);
				tmpRow.Name = rs.getString(i++);
				tmpRow._ROCommunity = rs.getString(i++);
				tmpRow._RWCommunity = rs.getString(i++);
				tmpRow.remark = rs.getString(i++);
				tmpRow._IsRegister = true;

				tmpRow.x1 = rs.getFloat(i++);
				tmpRow.x2 = rs.getFloat(i++);
				tmpRow.y1 = rs.getFloat(i++);
				tmpRow.y2 = rs.getFloat(i++);
				int tmpInt = rs.getInt(i++);
				if (tmpInt > 0) {
					tmpRow.isTx = true;
				} else {

					tmpRow.isTx = false;
				}

		          
                retList.put(tmpRow.get_NetAddress(), tmpRow);

			}

		} catch (Exception ex) {

			String xxxString = ex.toString();
		}

		return retList;
	}
	


    public String isDevExsit(String Name)
    {
        Hashtable groupLists = DeviceTableGetAllRows();       
      
        
    	Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			nojuDeviceTableRow item = (nojuDeviceTableRow) e.nextElement();

			if (item.Name.endsWith(Name)) {
			     return item.get_NetAddress();
			}

		}      


        return "";

    }

	public  boolean DeviceTableInsertRow(nojuDeviceTableRow row) {

		int copyIndex = 1;
		String newName = row.Name;
        while (!isDevExsit(newName).equals(""))
        {
            newName = row.Name + "(" + copyIndex+ ")";
            copyIndex++;
        }

        row.Name = newName;
        String sqlInsert = "INSERT INTO DeviceTable(NetAddress,NetType," +
                "UserGroupID,HeadAddress,Name,ROCommunity,RWCommunity,RemarkText) VALUES('" +
                row.get_NetAddress() + "'," + row.get_NetType().ordinal()+ ',' +
                row.UserGroupID + ",'" + row.HeadAddress + "','" + row.Name + "','" +
                row._ROCommunity + "','" +row._RWCommunity + "','" + " " + "')";
		//sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);			
	         if (pstmt.executeUpdate() > 0)
              return true;
		} catch (Exception EX) {
			System.out.println(EX);
			return false;

		}
		return false;
	}
	
	
	public  boolean DeviceTableDeleteRow(nojuDeviceTableRow row){


	      String sqlInsert = "DELETE FROM DeviceTable WHERE NetAddress='" + row.get_NetAddress() + '\'';
		//sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);			
	         if (pstmt.executeUpdate() > 0)
               return true;
	
		} catch (Exception EX) {


		}
		
		return false;

	}
	
	public  boolean DeviceTableUpdateRow(nojuDeviceTableRow row){
	       String rstIsDevExsit = isDevExsit(row.Name);

           if (!rstIsDevExsit.equals(""))
           {
               if (!rstIsDevExsit.equals(row.get_NetAddress()))
               {
                   return false;//直接不允许修改
               }
           }

           String sqlInsert = "UPDATE DeviceTable SET NetType=" + row.get_NetType().ordinal() + ",UserGroupID=" +
                   row.UserGroupID + ",HeadAddress='" + row.HeadAddress + "',Name='" + row.Name +
                   "',ROCommunity='" + row._ROCommunity + "',RWCommunity='" + row._RWCommunity + "',RemarkText='" + row.remark +
                    "',Txa=" + row.x1 + ",Txb=" + row.x2+ ",Txc=" + row.y1+ ",Txd=" + row.y2+ ",isTx=" + (row.isTx ? "1" : "0") +
                   " WHERE NetAddress='" + row.get_NetAddress() + '\'';

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);			
	         if (pstmt.executeUpdate() > 0)
               return true;
	
		} catch (Exception EX) {

		}
		
		return false;

	}
	
}