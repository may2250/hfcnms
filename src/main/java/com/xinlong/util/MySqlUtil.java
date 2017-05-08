package com.xinlong.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class MySqlUtil {
	private static Logger log = Logger.getLogger(MySqlUtil.class);
	public Connection con;  
 
    
	//��ȡ���ݿ������  
    public Connection getConnection()  
    {  
        String url="jdbc:mysql://localhost:3306/hfcdb";  
        // ����������
        String driver = "com.mysql.jdbc.Driver";
        String dbuser="root";  
        String dbpass="prevail";  
        try {
        	if(con==null)  
            {  
                Class.forName(driver);  
                con=DriverManager.getConnection(url, dbuser, dbpass);  
            }  
        
        }catch (Exception e) {
           // TODO: handle exception
            e.printStackTrace();
            log.info(e.getMessage());
        }
        
        return con;  
    } 
    
    //�ر����ݿ�����
    public void DBclose()  
    {          
        try {  
            con.close();  
        } catch (SQLException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            log.info(e.getMessage());
        }  
    }  
    
    
}
