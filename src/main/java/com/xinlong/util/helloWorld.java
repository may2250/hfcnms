package com.xinlong.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

/*import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;*/

public class helloWorld { 
    public static void main(String args[]) { 
        System.out.println("Hello World!"); 
        getConnection();
        UserGroupTableGetAllRows();
    } 
    public static Connection getConnection()  
    {  
        String url="jdbc:mysql://localhost:3306/hfctraplogs";  
        // ����������
        String driver = "com.mysql.jdbc.Driver";
        String dbuser="bobo";  
        String dbpass="123456";  
        try {
        	if(con==null)  
            {  
                Class.forName(driver);  
                con=DriverManager.getConnection(url, dbuser, dbpass);  
            }  
        
        }catch (Exception e) {
           // TODO: handle exception
            e.printStackTrace();
          //  log.info(e.getMessage());
        }
        
        return con;  
    } 
    
	public static Connection con;  
    public static int UserGroupTableInsertRow(UserGroupTableRow row)
    {

        int copyIndex = 1;
        String newName = row.UserGroupName;
        ResultSet rs=null;
        int lastId=-1;
   /*     while (isDevGroupExsit(newName) != -1)
        {
            newName = row.UserGroupName + "(" + copyIndex.ToString() + ")";
            copyIndex++;

        }
*/
      row.UserGroupName = newName;

        String sqlInsert = "INSERT INTO UserGroupTable(UserGroupName,ParentGroupID) VALUES('" +
                   row.UserGroupName + "'," + row.ParentGroupID + ')';

        sqlInsert += ";select @@IDENTITY";
         PreparedStatement pstmt;
        try
        {
    	  pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
    	  pstmt.executeUpdate();
    	   rs=pstmt.getGeneratedKeys();//这一句代码就是得到插入的记录的id
    	   while(rs.next()){
    		   lastId=rs.getInt(1);
    	   }
            row.UserGroupID = lastId;
            return row.UserGroupID;
        }
        catch (Exception EX)
        {

        	return lastId;

        }      




    }




    public static  Hashtable UserGroupTableGetAllRows()
    {       
    	PreparedStatement pstmt;
        Hashtable retList = new Hashtable();
        ResultSet rs=null;
        try
        {
            String sqlInsert = "SELECT * FROM UserGroupTable";
	
	      	  pstmt = (PreparedStatement) con.prepareStatement(sqlInsert);
	      	rs= pstmt.executeQuery(sqlInsert);

	      	   while(rs.next()){
	      	      UserGroupTableRow tmpRow = new UserGroupTableRow(
	      	    		  rs.getInt(1),
	      	   		  rs.getString(2),
	      	  	  rs.getInt(3));

                  int i = 3;
                  tmpRow.x1 = rs.getFloat(i++);
                  tmpRow.x2 = rs.getFloat(i++);
                  tmpRow.y1 = rs.getFloat(i++);
                  tmpRow.y2 = rs.getFloat(i++);
                  int tmpInt =  rs.getInt(i++);
                  if (tmpInt > 0)
                  {
                      tmpRow.isTx = true;
                  }
                  else
                  {

                      tmpRow.isTx = false;
                  }
                  retList.put(tmpRow.UserGroupID, tmpRow);

	      	   }

   
        }
        catch (Exception ex)
        {

               String xxxString=ex.toString();
        }

        return retList;
    }



  

}