package com.onycom.crawler.pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.common.NLP;

public class BlogPOS {
	
	NLP nlp;
	private Connection conn;
	
	public BlogPOS(){
		nlp = new NLP();
	}
	
	public void core(){
		this.nlp = new NLP();
		DBConnect db = new DBConnect();

		Statement stmt;
		
		String sql = "select sn, doc_sj, doc_cn from blog_info ";
		
		try {
			ResultSet rs = null;
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			 
			while(rs.next()){
				String tKeyword = extractPOS(rs.getString(2));
				String cKeyword = extractPOS(rs.getString(3));
				int sn = rs.getInt(1);
				
				System.out.println(tKeyword);
				
				updateData(sn, tKeyword, cKeyword);
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
		}
	}
	
	private void updateData(int sn, String tKeyword, String cKeyword) throws SQLException {
		// TODO Auto-generated method stub
		String sql = "UPDATE blog_info SET kwrd = ?, kwrd_sj = ? WHERE sn = ?";
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();

		PreparedStatement stmt = conn.prepareStatement(sql);
			
		stmt.setString(1, cKeyword);
		stmt.setString(2, tKeyword);
		stmt.setInt(3, sn);
		
		stmt.executeUpdate();
		
		stmt.close();
		conn.close();

	}
	
	public static void main(String[] args){
		BlogPOS posTest = new BlogPOS();
		
		posTest.core();
		
		//System.out.println(posTest.extractPOS("동해물과 백두산이 마르고 닳도록 동해물과 백두산이 마르고 닳도록"));
	}

	public String extractPOS(String val){
		
		String pos = nlp.extractNoun(val);
	
		
		return pos;
	}
}
