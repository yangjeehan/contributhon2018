package com.onycom.crawler.DB;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DBConnect {

	private String path;
	public HashMap<String, String> getDBInfo(String path){
		HashMap<String, String> map = new HashMap<String, String>();
		
		FileReader fr;
		BufferedReader br;
		
		String line = null;
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line.split("\t")[0], line.split("\t")[1]);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return map;
	}
	public DBConnect(String path){
		HashMap<String, String> dbInfoMap = getDBInfo(path);
		this.path = path;

		try {
			Class.forName(dbInfoMap.get("driver"));
		} catch (ClassNotFoundException e1) {
			System.out.println("오류가 발생했습니다."); 
			e1.printStackTrace();
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbInfoMap.get("url"),dbInfoMap.get("id"),dbInfoMap.get("password"));
			
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		this.conn = conn;
	}
	public DBConnect(){
		HashMap<String, String> dbInfoMap = getDBInfo();
		
		try {
			Class.forName(dbInfoMap.get("driver"));
		} catch (ClassNotFoundException e1) {
			System.out.println("오류가 발생했습니다."); 
			e1.printStackTrace();
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbInfoMap.get("url"),dbInfoMap.get("id"),dbInfoMap.get("password"));
			
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		this.conn = conn;
	}
	public ResultSet getDBData(String sql){		
		Statement stmt;		
		ResultSet rs = null;
		
		try {
			if(conn == null || conn.isClosed()){
				conn = getConnection();
			}
			
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery(sql);
			
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return rs;
	}
		
	public Connection getConnection(){
		HashMap<String, String> dbInfoMap = getDBInfo();
		
		try {
			Class.forName(dbInfoMap.get("driver"));
		} catch (ClassNotFoundException e1) {
			System.out.println("getConnection 1 오류가 발생했습니다."); 
			e1.printStackTrace();
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbInfoMap.get("url"),dbInfoMap.get("id"),dbInfoMap.get("password"));
			
		} catch (SQLException e) {
			System.out.println("getConnection 2 오류가 발생했습니다."); 
			
		}
		
		this.conn = conn;
		return conn;
	}
	
	public Connection conn;
	
	public void DBInput(PreparedStatement pstmt){
		try {
			pstmt.executeUpdate();
			
			pstmt.close();
			
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	public void DBInput(String sql, HashMap<Integer, String> map){
		DBConnect db = new DBConnect();
		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			for(int i = 0 ;  i < map.size() ; i++){
				pstmt.setString(i+1, map.get(i+1));
			}
			pstmt.executeUpdate();
			
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	
	public HashMap<String, String> getDBInfo(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		FileReader fr;
		BufferedReader br;
		
		String line = null;
		try {
			fr = new FileReader("CUBRIDDB.conf");
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line.split("\t")[0], line.split("\t")[1]);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return map;
	}
}
