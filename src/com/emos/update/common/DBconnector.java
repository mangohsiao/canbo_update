package com.emos.update.common;



import java.sql.Connection;
import java.sql.DriverManager;


public class DBconnector {

	private final static String host = "127.0.0.1"; 
	private final static String port = "3306"; 
	private final static String dbname = "canbo"; 
	private final static String username = "root"; 
	private final static String pswd = "root"; 
	
	
	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url="jdbc:mysql://"+host+":"+port+"/"+dbname;
			String user = "root";
			String password = "root";
			Connection con = DriverManager.getConnection(url,user,password);		
			return con;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}
}
