package com.onycom.crawler.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.TagRemover;

public class KeywordUpdater {
	public static void main(String[] args){
		KeywordUpdater ku = new KeywordUpdater();
		
		ku.keywordUpdateBlog();
	}
	public void keywordUpdateCivil(){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		String sql = "select rno, contents from civildata";
		
		String uSql = "update civildata set keywords = ? where rno = ?";
		try {
			Statement stmt = conn.createStatement();
			PreparedStatement pstmt;
			ResultSet rs = stmt.executeQuery(sql);
			
			int sn = -1;
			String contents = new String();
			String bagStr = new String();
			
			NLP nlp = new NLP();
			
			TagRemover tr = new TagRemover();
			
			int cnt = 0; 
			long startTime = System.currentTimeMillis();
			while(rs.next()){
				
				pstmt = conn.prepareStatement(uSql);
				sn = rs.getInt(1);
				contents = rs.getString(2);
				
				bagStr= nlp.extractBag(tr.removeHTMLTag(contents), 1);
				
				pstmt.setString(1, bagStr);
				pstmt.setInt(2, sn);
				
				pstmt.executeUpdate();
				pstmt.close();
				
				if(cnt% 100 == 0){
					System.out.println(cnt+"\t"+(System.currentTimeMillis()-startTime));
					startTime = System.currentTimeMillis();
				}
				cnt++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public void keywordUpdateNews(){
		System.out.println("News Updater");
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		String sql = "select nid from food_risk_news";
		
		String cSql = "select contents from food_news where nid = '";
		
		String uSql = "insert into food_risk_word (nid, keyword_type, rword, cnt) values(?,?,?,?)";
		
		String dSql = "delete from food_risk_word where nid = ?";
		
		try {
			Statement stmt = conn.createStatement();
			Statement cStmt = conn.createStatement();
			
			PreparedStatement pstmt;
			ResultSet rs = stmt.executeQuery(sql);
			ResultSet cRs;
			
			int sn = -1;
			String contents = new String();
			String bagStr = new String();
			
			NLP nlp = new NLP();
			
			TagRemover tr = new TagRemover();
			
			int cnt = 0; 
			long startTime = System.currentTimeMillis();
			while(rs.next()){
				
				
				sn = rs.getInt(1);
				
				cRs = cStmt.executeQuery(cSql+sn+"'");
				
				while(cRs.next()){
					pstmt = conn.prepareStatement(dSql);
					pstmt.setInt(1, sn);
					
					pstmt.executeUpdate();
					pstmt.close();
					
					
					contents = cRs.getString(1);					
					bagStr = nlp.extractBag(tr.removeHTMLTag(contents));
					String[] bagList = bagStr.split("\t");
					
					int len = bagList.length;
					pstmt = conn.prepareStatement(uSql);
					
					for(int i = 0; i < len ; i++){
						String type = "normal";
						String word = bagList[i].split(":")[0];
						int count = Integer.parseInt(bagList[i].split(":")[1]);
						
						if(nlp.badMap.containsKey(word)){
							type = "caution";
						} 
						
						pstmt.setInt(1, sn);
						pstmt.setString(2, type);
						pstmt.setString(3, word);
						pstmt.setInt(4, count);
						
						pstmt.executeUpdate();
						
					}
					
					pstmt.close();
				}
				
				
				if(cnt% 100 == 0){
					System.out.println(cnt+"\t"+(System.currentTimeMillis()-startTime));
					startTime = System.currentTimeMillis();
				}
				cnt++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void keywordUpdateBlog(){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		String sql = "select sn, doc_sj from blog_info where doc_cret_dt > '2016-12-01'";
		
		String uSql = "update blog_info set caution = ? where sn = ?";
		try {
			Statement stmt = conn.createStatement();
			PreparedStatement pstmt;
			ResultSet rs = stmt.executeQuery(sql);
			
			int sn = -1;
			String contents = new String();
			String bagStr = new String();
			
			NLP nlp = new NLP();
			
			TagRemover tr = new TagRemover();
			
			int cnt = 0; 
			long startTime = System.currentTimeMillis();
			while(rs.next()){
				
				pstmt = conn.prepareStatement(uSql);
				sn = rs.getInt(1);
				contents = rs.getString(2).replace(" : 네이버 블로그", "");
				
				bagStr= nlp.extractBag(tr.removeHTMLTag(contents), 1);
				
				String caution = new String();
				
				
				if(bagStr.length() > 3){
					caution = "Y";
					pstmt.setString(1, "Y");
					pstmt.setInt(2, sn);
					
					pstmt.executeUpdate();
					pstmt.close();
				} else {
					caution = "N";
					pstmt.setString(1, "N");
					pstmt.setInt(2, sn);
					
					pstmt.executeUpdate();
					pstmt.close();
				}
				if(caution.equals("Y")){
					System.out.println(caution+"\t"+contents+"\t"+bagStr+"");
				}
				if(cnt% 100 == 0){
					System.out.println(cnt+"\t"+(System.currentTimeMillis()-startTime));
					startTime = System.currentTimeMillis();
				}
				cnt++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
