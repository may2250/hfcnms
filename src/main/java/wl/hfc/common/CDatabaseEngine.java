package wl.hfc.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

/*import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.PreparedStatement;*/

public class CDatabaseEngine {


	public  Connection getConnection() {
		String url = "jdbc:mysql://localhost:3306/hfctraplogs";
		// ����������
		String driver = "com.mysql.jdbc.Driver";
		String dbuser = "root";
		String dbpass = "prevail";
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

	public  int UserGroupTableInsertRow(UserGroupTableRow row) {

		int copyIndex = 1;
		String newName = row.UserGroupName;
		ResultSet rs = null;
		int lastId = -1;
		/*
		 * while (isDevGroupExsit(newName) != -1) { newName = row.UserGroupName
		 * + "(" + copyIndex.ToString() + ")"; copyIndex++;
		 * 
		 * }
		 */
		row.UserGroupName = newName;

		String sqlInsert = "INSERT INTO UserGroupTable(UserGroupName,ParentGroupID) VALUES('" + row.UserGroupName + "'," + row.ParentGroupID + ')';

		sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.UserGroupID = lastId;
			return row.UserGroupID;
		} catch (Exception EX) {

			return lastId;

		}

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

}