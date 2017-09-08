package wl.hfc.common;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import redis.clients.jedis.Jedis;

import com.xinlong.util.RedisUtil;

import wl.hfc.common.NlogType.OperLogTypes;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.online.pmls;

/*import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.PreparedStatement;*/

public class CDatabaseEngine {
	private static final String MAINKERNEL_MESSAGE = "mainkernel.message";
	private static Logger log = Logger.getLogger(CDatabaseEngine.class);
	private static RedisUtil redisUtil;
	public static boolean flag = false; // 数据库连接状态
	public static CDatabaseEngine me;
	// private boolean isFirstTimeSucedCnt = true;
	private boolean lastTrapInsertIsSucced = false;
	public Connection trapcon;
	private static Document doc;
	private static Document docen;
	private String dbuser="hfcnms";
	private String dbpass="999999";
	public CDatabaseEngine(RedisUtil predisUtil) {
		redisUtil = predisUtil;
		//loadDXml();
		me = this;
	}
	
	public boolean loadDXml() {

		String filePath = pmls.class.getResource("/").toString();
		filePath = filePath.substring(filePath.indexOf("file:") + 5);
		// log.info("----------------path--->>>" + filePath+ "phs.xml");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			File f = new File(filePath + "hconfig.xml");
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(f);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			return false;
		} catch (SAXException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			return false;
		}
		Element rootElement = doc.getDocumentElement();
		NodeList rootNode = rootElement.getChildNodes();
		
		
		Node node = rootNode.item(0);
		Element elt = (Element) node;
		String uid = elt.getAttribute("mysqlUserID");

		String pwd = elt.getAttribute("mysqlUserID");
		
		return true;
	}


	public Connection offNewCoon()// 因为增删查改不需要关注或者维护“数据库连接”这个层面，本来不需要offnewcon，因为存在连接长期不用可能失效的情况，直接暴力新建短连接。
	{
		boolean tmpFlag = flag;

		String url = "jdbc:mysql://localhost:3306/hfctraplogs?characterEncoding=UTF-8&useSSL=false";
		// ����������
		String driver = "com.mysql.jdbc.Driver";

		Connection con = null;
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, dbuser, dbpass);
			flag = true;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.error(e.getMessage());

			flag = false;

		}
		return con;
	}

	public void offNewTrapCoon() {

		String url = "jdbc:mysql://localhost:3306/hfctraplogs?characterEncoding=UTF-8&useSSL=false";
		// ����������
		String driver = "com.mysql.jdbc.Driver";
		try {
			Class.forName(driver);
			trapcon = DriverManager.getConnection(url, dbuser, dbpass);

		} catch (Exception e) {

			// TODO: handle exception
			e.printStackTrace();
			log.info(e.getMessage());
			trapcon = null;

		}

	}

	/*
	 * private boolean isDBConnected(){ try {
	 * 
	 * //重连一次 con = this.getConnection(); if(con.isClosed()){ //发送数据库失去连接信息到前端
	 * 
	 * }else{ //发送数据库失去连接信息到前端 JSONObject rootjson = new JSONObject();
	 * rootjson.put("cmd", "dbOpend"); rootjson.put("flag", true);
	 * sendToQueue(rootjson.toJSONString(), MAINKERNEL_MESSAGE); flag=true; } } }
	 * catch (SQLException e) { e.printStackTrace(); JSONObject rootjson = new
	 * JSONObject(); rootjson.put("cmd", "dbclosed"); rootjson.put("flag", true);
	 * sendToQueue(rootjson.toJSONString(), MAINKERNEL_MESSAGE); this.flag = false;
	 * }
	 * 
	 * return this.flag; }
	 */

	public void sendToQueue(String msg, String queue) {
		Jedis jedis = null;
		try {
			jedis = redisUtil.getConnection();
			jedis.publish(queue, msg);

		} catch (Exception e) {
			log.info(e.getMessage());

		} finally {
			redisUtil.closeConnection(jedis);
		}
	}

	public int grpWithPname(String gpName, Hashtable groupLists) {

		try {

			Enumeration e = groupLists.elements();

			while (e.hasMoreElements()) {

				UserGroupTableRow item = (UserGroupTableRow) e.nextElement();

				if (item.UserGroupName.equals(gpName)) {
					return item.UserGroupID;
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		return -1;
	}

	public int UserGroupTableInsertRow(UserGroupTableRow row) {
		int lastId = -1;
		int copyIndex = 1;

		Connection con = offNewCoon();
		if (con == null) {
			return -1;

		}

		String newName = row.UserGroupName;
		ResultSet rs = null;
		Hashtable groupLists;
		try {
			groupLists = UserGroupTableGetAllRows();
		} catch (Exception e) {
			return -1;
			// TODO: handle exception
		}

		while (grpWithPname(newName, groupLists) != -1) {
			newName = row.UserGroupName + "(" + copyIndex + ")";
			copyIndex++;

		}

		row.UserGroupName = newName;

		String sqlInsert = "INSERT INTO usergrouptable(UserGroupName,ParentGroupID) VALUES('" + row.UserGroupName + "',"
				+ row.ParentGroupID + ')';

		// sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.UserGroupID = lastId;
			return row.UserGroupID;
		} catch (Exception EX) {
			EX.printStackTrace();
			return lastId;

		}

	}

	public boolean UserGroupTableDeleteRow(int thisID) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}
		String sqlInsert = "DELETE FROM usergrouptable WHERE UserGroupID=" + thisID;
		// sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {
			EX.printStackTrace();
			return false;

		}

		return false;

	}

	public boolean UserGroupTableUpdateRow(UserGroupTableRow row) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}

		Hashtable groupLists;
		try {
			groupLists = UserGroupTableGetAllRows();
		} catch (Exception EX) {
			EX.printStackTrace();
			return false;
			// TODO: handle exception
		}

		if (grpWithPname(row.UserGroupName, groupLists) != -1) {
			if (grpWithPname(row.UserGroupName, groupLists) != row.UserGroupID) {
				return false;// 直接不允许修改
			}
		}

		PreparedStatement pstmt;
		// ResultSet rs = null;

		String sqlInsert;
		/*
		 * //is name exsit ? String sqlInsert =
		 * "SELECT * FROM usergrouptable where UserGroupName='"+row.UserGroupName+"'";
		 * 
		 * try { pstmt = (PreparedStatement) con.prepareStatement(sqlInsert); rs =
		 * pstmt.executeQuery(sqlInsert); rs.last(); int rowCount = rs.getRow();
		 * 
		 * if (rowCount>0) {
		 * 
		 * //name exsit，return return false; }
		 * 
		 * 
		 * } catch (Exception e) { return false; }
		 */

		sqlInsert = "UPDATE usergrouptable SET UserGroupName='" + row.UserGroupName + "',ParentGroupID="
				+ row.ParentGroupID + ",Txa=" + row.x1 + ",Txb=" + row.x2 + ",Txc=" + row.y1 + ",Txd=" + row.y2
				+ ",isTx=" + (row.isTx ? "1" : "0") + " WHERE UserGroupID=" + row.UserGroupID;

		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return false;

	}

	public Hashtable UserGroupTableGetAllRows() throws SQLException {
		PreparedStatement pstmt;
		Hashtable retList = new Hashtable();
		ResultSet rs = null;

		Connection con = offNewCoon();

		String sqlInsert = "SELECT * FROM usergrouptable";

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

		return retList;
	}

	public Hashtable DeviceTableGetAllRows() throws SQLException {
		PreparedStatement pstmt;
		Hashtable retList = new Hashtable();
		ResultSet rs = null;

		Connection con = offNewCoon();

		String sqlInsert = "SELECT * FROM devicetable";

		pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
		rs = pstmt.executeQuery(sqlInsert);

		while (rs.next()) {
			nojuDeviceTableRow tmpRow = new nojuDeviceTableRow(rs.getString(1), NetTypes.values()[rs.getInt(2)]);

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

		return retList;
	}

	public String devWithPname(String Name, Hashtable groupLists) {
		try {
			Enumeration e = groupLists.elements();
			while (e.hasMoreElements()) {
				nojuDeviceTableRow item = (nojuDeviceTableRow) e.nextElement();
				if (item.Name.equalsIgnoreCase(Name)) {
					return item.get_NetAddress();
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		return "";

	}

	public boolean DeviceTableInsertRow(nojuDeviceTableRow row) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}
		int copyIndex = 1;
		String newName = row.Name;
		Hashtable devLists;
		try {
			devLists = DeviceTableGetAllRows();
		} catch (Exception e) {
			return false;
			// TODO: handle exception
		}

		while (!devWithPname(newName, devLists).equals("")) {
			newName = row.Name + "(" + copyIndex + ")";
			copyIndex++;
		}

		row.Name = newName;
		String sqlInsert = "INSERT INTO devicetable(NetAddress,NetType,"
				+ "UserGroupID,HeadAddress,Name,ROCommunity,RWCommunity,RemarkText) VALUES('" + row.get_NetAddress()
				+ "'," + row.get_NetType().ordinal() + ',' + row.UserGroupID + ",'" + row.HeadAddress + "','" + row.Name
				+ "','" + row._ROCommunity + "','" + row._RWCommunity + "','" + " " + "')";
		// sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;
		} catch (Exception EX) {
			EX.printStackTrace();
			return false;

		}
		return false;
	}

	public boolean DeviceTableDeleteRow(nojuDeviceTableRow row) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}
		String sqlInsert = "DELETE FROM devicetable WHERE NetAddress='" + row.get_NetAddress() + '\'';
		// sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return false;

	}

	public boolean DeviceTableUpdateRow(nojuDeviceTableRow row) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}

		Hashtable groupLists;
		try {
			groupLists = DeviceTableGetAllRows();
		} catch (Exception EX) {
			EX.printStackTrace();
			return false;
			// TODO: handle exception
		}

		String rstNm = devWithPname(row.Name, groupLists);

		if (!rstNm.equals("")) {
			if (!rstNm.equals(row.get_NetAddress())) {
				return false;// 直接不允许修改
			}
		}

		String sqlInsert = "UPDATE devicetable SET NetType=" + row.get_NetType().ordinal() + ",UserGroupID="
				+ row.UserGroupID + ",HeadAddress='" + row.HeadAddress + "',Name='" + row.Name + "',ROCommunity='"
				+ row._ROCommunity + "',RWCommunity='" + row._RWCommunity + "',RemarkText='" + row.remark + "',Txa="
				+ row.x1 + ",Txb=" + row.x2 + ",Txc=" + row.y1 + ",Txd=" + row.y2 + ",isTx=" + (row.isTx ? "1" : "0")
				+ " WHERE NetAddress='" + row.get_NetAddress() + '\'';

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return false;

	}

	// log
	// | TrapLogID | TrapLogType | TrapLogTypeNm | TrapDevAddress | NEName |
	// TrapLogContent| TrapLogTime | TrapTreatMent | IsTreatMent | ParamName |
	// ParamValue |
	public int trapLogInsertRow(nojuTrapLogTableRow row) {
		ResultSet rs = null;
		int lastId = -1;
		if (!lastTrapInsertIsSucced) {
			this.offNewTrapCoon();
		}
		boolean tmpFlag = flag;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(row.TrapLogTime);
		String sqlInsert = "insert into traplogtable values (" + null + "," + row.TrapLogType.ordinal() + ",'"
				+ row.TrapLogType.toString() + "','" + row.TrapDevAddress + "','" + row.neName + "','"
				+ row.TrapLogContent + "','" + currentTime + "','" + row.TrapTreatMent + "','" + row.isTreated + "','"
				+ row.parmName + "','" + row.paramValue + "')";

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) trapcon.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.TrapLogID = lastId;
			lastTrapInsertIsSucced = true;
			flag = true;

		} catch (Exception EX) {
			lastTrapInsertIsSucced = false;
			flag = false;
			EX.printStackTrace();

		}

		return lastId;
	}

	public int trapLogEditRow(int TrapLogID, String treatment, String IsTreatMent) {

		Connection con = offNewCoon();
		if (con == null) {
			return -1;

		}
		String sqlInsert = "UPDATE traplogtable SET TrapTreatMent='" + treatment + "', IsTreatMent='" + IsTreatMent
				+ "' WHERE TrapLogID='" + TrapLogID + "'";

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			return pstmt.executeUpdate();

		} catch (Exception EX) {
			EX.printStackTrace();
			return -1;
		}

	}

	public ArrayList<nojuTrapLogTableRow> getTrapRowsWithTime(Date beginTime, Date endTime, String ip, int level,
			int status) {
		ArrayList<nojuTrapLogTableRow> results = new ArrayList<nojuTrapLogTableRow>();

		Connection con = offNewCoon();
		if (con == null) {
			return results;

		}
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String endString = sdf.format(endTime);
		String bENGString = sdf.format(beginTime);

		PreparedStatement pstmt;
		ResultSet rs = null;
		try {
			String sqlInsert;

			if (ip.equalsIgnoreCase("")) {
				sqlInsert = "SELECT traplogtable.*FROM traplogtable WHERE TrapLogTime>'" + bENGString
						+ "' AND TrapLogTime<'" + endString + "';";
			} else {
				InetAddress addr;
				try {
					addr = InetAddress.getByName(ip);
				} catch (Exception e) {// invalid ip
					return results;
				}
				sqlInsert = "SELECT traplogtable.*FROM traplogtable WHERE TrapLogTime>'" + bENGString
						+ "' AND TrapLogTime<'" + endString + "' AND TrapDevAddress='" + addr.getHostAddress() + "';";
			}

			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			rs = pstmt.executeQuery(sqlInsert);

			while (rs.next()) {

				// 通过reader["列名"]来取得值
				TrapLogTypes type1 = TrapLogTypes.values()[rs.getInt(2)];
				int i = 4;
				nojuTrapLogTableRow newURow = new nojuTrapLogTableRow(NlogType.getAlarmLevel(type1), type1,
						rs.getString(i++), rs.getString(i++), rs.getString(i++), rs.getTimestamp(i++),
						rs.getString(i++), rs.getString(i++), rs.getString(i++), rs.getString(i++));
				newURow.TrapLogID = rs.getInt(1);

				if (passStatus(status, newURow) && passLevel(level, newURow))// 未处理
				{
					results.add(newURow);
				}

			}

		} catch (Exception EX) {
			EX.printStackTrace();

		}

		return results;

	}

	public static boolean passType(int type, nojuTrapLogTableRow pRow)// return true，就是这条告警想通过过滤的强烈愿望
	{
		if (type == 0) {
			return true;
		}

		if (type == 2) {
			if (pRow.TrapLogType == TrapLogTypes.Offline) {
				return true;
			}
		}

		if (type == 1) {
			if (pRow.TrapLogType != TrapLogTypes.Offline) {
				return true;
			}
		}

		return false;

	}

	public static boolean passStatus(int status, nojuTrapLogTableRow pRow)// return true，就是这条告警想通过过滤的强烈愿望
	{
		if (status == 0) {
			return true;
		}

		if (status == 1)// 已处理
		{
			if (!pRow.isTreated.equalsIgnoreCase("")) {
				return true;
			}
		}

		if (status == 2) {
			if (pRow.isTreated.equalsIgnoreCase("")) {
				return true;
			}
		}

		return false;

	}

	public static boolean passLevel(int level, nojuTrapLogTableRow pRow)// return true，就是这条告警想通过过滤的强烈愿望
	{
		if (level == -1) {
			return true;
		}

		if (level == pRow.level) {
			return true;

		}

		return false;

	}

	public static boolean passNename(String neName, nojuTrapLogTableRow pRow)// return true，就是这条告警想通过过滤的强烈愿望
	{
		if (neName.equalsIgnoreCase("")) {
			return true;
		}

		if (pRow.neName.contains(neName))// 已处理
		{
			return true;
		}

		return false;

	}

	public int operLogInsertRow(nojuOperLogTableRow row) {

		ResultSet rs = null;
		int lastId = -1;

		Connection con = offNewCoon();
		if (con == null) {
			return -1;

		}

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(row.OperLogTime);
		String sqlInsert = "insert into operlogtable values (" + null + "," + row.OperLogType.ordinal() + ",'"
				+ row.OperLogType.toString() + "','" + row.OperLogContent + "','" + currentTime + "','"
				+ row.OperLogUser + "')";

		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.OperLogID = lastId;
			return row.OperLogID;
		} catch (Exception EX) {
			EX.printStackTrace();

			return lastId;

		}

	}

	public ArrayList<nojuOperLogTableRow> getOperRowsWithTime(Date beginTime, Date endTime, String userNme) {

		ArrayList<nojuOperLogTableRow> results = new ArrayList<nojuOperLogTableRow>();

		Connection con = offNewCoon();
		if (con == null) {
			return results;

		}

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String endString = sdf.format(endTime);
		String bENGString = sdf.format(beginTime);

		PreparedStatement pstmt;
		ResultSet rs = null;
		try {
			String sqlInsert;

			if (userNme.equalsIgnoreCase("")) {
				sqlInsert = "SELECT operlogtable.*FROM operlogtable WHERE operLogTime>'" + bENGString
						+ "' AND operLogTime<'" + endString + "';";
			} else {

				sqlInsert = "SELECT operlogtable.*FROM operlogtable WHERE operLogTime>'" + bENGString
						+ "' AND operLogTime<'" + endString + "' AND OperLogUser='" + userNme + "';";
			}

			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			rs = pstmt.executeQuery(sqlInsert);

			while (rs.next()) {

				// 通过reader["列名"]来取得值
				OperLogTypes type1 = OperLogTypes.values()[rs.getInt(2)];
				int i = 4;

				nojuOperLogTableRow newURow = new nojuOperLogTableRow(type1, rs.getString(i++), rs.getTimestamp(i++),
						rs.getString(i++));

				newURow.OperLogID = rs.getInt(1);

				results.add(newURow);
			}

		} catch (Exception EX) {
			EX.printStackTrace();
		}

		return results;

	}

	public int UserAuthorizeTableInsertRow(nojuUserAuthorizeTableRow row) {
		int lastId = -1;
		int copyIndex = 1;

		Connection con = offNewCoon();
		if (con == null) {
			return -1;

		}

		PreparedStatement pstmt;
		ResultSet rs = null;

		String sqlInsert;
		// is name exsit ?
		sqlInsert = "SELECT * FROM userauthorizetable where UserName='" + row.UserName + "'"; // username exsit

		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			rs = pstmt.executeQuery(sqlInsert);
			rs.last();
			int rowCount = rs.getRow();

			if (rowCount > 0) {
				System.out.println("UserAuthorizeTableInsertRow-->" + row.UserName + "is already exsit");
				log.info(row.UserName + "is exsit");
				return -1;
			}

		} catch (Exception EX) {
			EX.printStackTrace();
			return -1;
		}

		Byte ismsgByte = 1;
		sqlInsert = "INSERT INTO userauthorizetable(UserName,password1,phoneNmber,smtpAddress,AuthTotal,IsMsgDefi) VALUES("
				+ "'" + row.UserName + "','" + row.PassWord + "','" + row.PhoneNmbr + "','" + row.smtpAddress + "',"
				+ row.AuthTotal + "," + ismsgByte + ")";
		// sqlInsert += ";select @@IDENTITY";

		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();// 这一句代码就是得到插入的记录的id
			while (rs.next()) {
				lastId = rs.getInt(1);
			}
			row.UserID = lastId;
			return row.UserID;
		} catch (Exception EX) {
			EX.printStackTrace();
			// isDBConnected(false);
			return lastId;

		}

	}

	public boolean UserAuthorizeTableDeleteRow(int UserID) {
		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}
		String sqlInsert = "DELETE FROM userauthorizetable WHERE UserID=" + UserID;
		// sqlInsert += ";select @@IDENTITY";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {

			EX.printStackTrace();
		}

		return false;

	}

	public boolean UserAuthorizeTableUpdateRow(int UserID, String pwd, Byte AuthTotal) {

		Connection con = offNewCoon();
		if (con == null) {
			return false;

		}

		PreparedStatement pstmt;
		// ResultSet rs = null;

		String sqlInsert = "UPDATE userauthorizetable SET AuthTotal =" + AuthTotal + ",PassWord1='" + pwd
				+ "' WHERE UserID=" + UserID;

		try {
			pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
			if (pstmt.executeUpdate() > 0)
				return true;

		} catch (Exception EX) {

			EX.printStackTrace();
		}

		return false;

	}

	public ArrayList<nojuUserAuthorizeTableRow> UserAuthorizeTableGetAllRows() throws SQLException {
		PreparedStatement pstmt;
		ArrayList<nojuUserAuthorizeTableRow> retList = new ArrayList<nojuUserAuthorizeTableRow>();

		ResultSet rs = null;

		Connection con = offNewCoon();
		String sqlInsert = "SELECT * FROM userauthorizetable";
		pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
		rs = pstmt.executeQuery();

		while (rs.next()) {
			nojuUserAuthorizeTableRow newURow = new nojuUserAuthorizeTableRow(rs.getInt(1), rs.getString(2),
					rs.getByte(3), rs.getString(4));

			retList.add(newURow);
		}

		return retList;
	}

	public nojuUserAuthorizeTableRow UserAuthorizeTableFindUser(String username) throws SQLException {
		PreparedStatement pstmt;

		ResultSet rs = null;

		Connection con = offNewCoon();
		String sqlInsert = "SELECT * FROM userauthorizetable WHERE UserName='" + username + "'";
		// System.out.println("------>>>"+sqlInsert);
		pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
		rs = pstmt.executeQuery();
		nojuUserAuthorizeTableRow newURow = null;
		while (rs.next()) {
			newURow = new nojuUserAuthorizeTableRow(rs.getInt(1), rs.getString(2), rs.getByte(3), rs.getString(4));

		}
		return newURow;

	}

}