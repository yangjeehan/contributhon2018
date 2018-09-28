package com.onycom.crawler.etc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;

public class InsertCautionData {
	public InsertCautionData(){
	
	}
	

	public void insertCautionNews(DocumentDEF doc, String id){
		nlp = new NLP();
		Dictionary dic = new Dictionary();
		
		HashMap<String, Integer> fMap = dic.getDicMap("dic.txt");
		HashMap<String, Integer> bMap = dic.getDicMap("bad.txt");
		
		HashMap<String, String> map = new HashMap();
		map.put("date", doc.date);
		map.put("title", doc.title);
		map.put("contents", doc.contents);
		map.put("bagList", doc.bagList);
		map.put("food", doc.food);
		map.put("label", doc.classLabel);
		map.put("sj_kwd", doc.titleCList);
		
		DBInputCautionNews(map, fMap, bMap, id);
	}
	
	NLP nlp;
	public void core(){
		nlp = new NLP();
		CSVFileReaderToDEF cr = new CSVFileReaderToDEF();
		cr.setHeader("classLabel", 0);
		cr.setHeader("date", 2);
		cr.setHeader("title", 3);
		cr.setHeader("bagList", 4);
		cr.setHeader("contentsLast", 0);
		cr.setHeader("food", 1);
		
		ArrayList<DocumentDEF> list = cr.getList("KBS_1_ForDB.txt", "\t", "");
		
		ArrayList<HashMap> mList = new ArrayList<HashMap>();
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> fMap = dic.getDicMap("dic.txt");
		HashMap<String, Integer> bMap = dic.getDicMap("bad.txt");
		
		for( int i = 0; i < list.size() ; i++){
			DocumentDEF doc = list.get(i);
			if(doc.classLabel.equals("yes")){
				HashMap<String, String> map = new HashMap();
				map.put("date", doc.date);
				map.put("title", doc.title);
				map.put("contents", doc.contents);
				map.put("bagList", doc.bagList);
				
				if(nlp.simMap.containsKey(doc.food)){
					map.put("food", nlp.simMap.get(doc.food));
				} else {
					map.put("food", "NULL");
				}
				
				
				DBInputKeyword(map, fMap, bMap);
			}
		}
	}
	
	public void DBInputCautionNews(HashMap<String, String> map, HashMap<String, Integer> fMap, HashMap<String, Integer> bMap, String id){
		DBConnect db = new DBConnect();
		PreparedStatement stmt;
		ResultSet rs;
		
		String bagList[] = map.get("bagList").split("\t");
		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			PreparedStatement pstmt;
			
			for(int i = 0; i < bagList.length; i++){
				pstmt = conn.prepareStatement("Insert into food_risk_word(nid, keyword_type, cnt, rword) values(?,?,?,?)");
				pstmt.setString(4, bagList[i].split(":")[0]);
				pstmt.setInt(3, Integer.parseInt(bagList[i].split(":")[1]));
				
				if (bMap.containsKey(bagList[i].split(":")[0])){
					pstmt.setString(2, "caution");
				} else {
					pstmt.setString(2, "normal");
				}
				
				pstmt.setInt(1, Integer.parseInt(id));
				pstmt.executeUpdate();
				pstmt.close();
			}
			
			String[] titleCList = map.get("sj_kwd").split("\t");
			for(int i = 0; i < titleCList.length; i++){
				pstmt = conn.prepareStatement("Insert into food_risk_word_title (nid, keyword_type, cnt, rword) values(?,?,?,?)");
				
				pstmt.setString(4, titleCList[i].split(":")[0]);
				pstmt.setInt(3, Integer.parseInt(titleCList[i].split(":")[1]));
				pstmt.setString(2, "caution");
				pstmt.setInt(1, Integer.parseInt(id));
				pstmt.executeUpdate();
				pstmt.close();
			}
			
			
			stmt = conn.prepareStatement("select count(nid) from foodrisk_news_set where nid= ?");
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			rs.next();
			if(rs.getInt(1) > 0){
				pstmt = conn.prepareStatement("update foodrisk_news_set set mfood = ?, rtype = ?, rlevel = ? where nid = ?");
							pstmt.setInt(4, Integer.parseInt(id));
							if(nlp.simMap.containsKey(map.get("food"))){
								pstmt.setString(1, map.get("food"));
							} else {
								pstmt.setString(1, map.get("NULL"));
							}
							pstmt.setString(2, "");
							pstmt.setString(3, map.get("label"));
							pstmt.executeUpdate();
							pstmt.close();
			} else {
				pstmt = conn.prepareStatement("Insert into foodrisk_news_set(nid, mfood, rtype, rlevel) values(?,?,?,?)");
				pstmt.setInt(1, Integer.parseInt(id));
				if(nlp.simMap.containsKey(map.get("food"))){
					pstmt.setString(2, map.get("food"));
				} else {
					pstmt.setString(2, map.get("NULL"));
				}
				pstmt.setString(3, "");
				pstmt.setString(4, map.get("label"));
				pstmt.executeUpdate();
				pstmt.close();
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
	}
	
	private Connection conn;
	public void DBInputKeyword(HashMap<String, String> map, HashMap<String, Integer> fMap, HashMap<String, Integer> bMap){
		DBConnect db = new DBConnect();
		
		/**
		 * CREATE TABLE newsdata(
			rno bigint AUTO_INCREMENT(1,1),
			PROVIDER character varying(4096),
			GENDATE timestamp,
			ORILINK character varying(4096),
			TITLE character varying(4096),
			CONTENTS character varying(4096),
			KEYWORDS character varying(4096) COLLATE utf8_bin 
			) COLLATE utf8_bin ;
		 * 
		 * 
		 */
		Statement stmt;
		ResultSet rs;
			
		String bagList[];
		if(map.get("bagList") == null){
			bagList = null;
		} else {
			bagList = map.get("bagList").split(",");
		
			try {
				if(conn == null || conn.isClosed()){
					conn = db.getConnection();
				}
				String q = "insert into food_news(rdate, title, contents, [ref]) value(?,?,?,?)";
				PreparedStatement pstmt = conn.prepareStatement(q);
				
	//			stmt = conn.createStatement();
	//			stmt.execute(
	//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
	//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
				pstmt.setString(4, "KBS");
				pstmt.setString(1, map.get("date").split(" ")[0]);
				pstmt.setString(2, map.get("title"));
				pstmt.setString(3, map.get("contents"));
				pstmt.executeUpdate();
				
				pstmt.close();
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery("select last_insert_id()");
				rs.next();
				String id = rs.getString(1);
				
				for(int i = 0; i < bagList.length; i++){
					pstmt = conn.prepareStatement("Insert into food_risk_word(nid, keyword_type, cnt, rword) values(?,?,?,?)");
					pstmt.setString(4, bagList[i].split(":")[0]);
					pstmt.setInt(3, Integer.parseInt(bagList[i].split(":")[1]));

					if (bMap.containsKey(bagList[i].split(":")[0])){
						pstmt.setString(2, "caution");
					} else {
						pstmt.setString(2, "normal");
					}
					
					pstmt.setInt(1, Integer.parseInt(id));
					pstmt.executeUpdate();
					pstmt.close();
				}
				
				pstmt = conn.prepareStatement("Insert into food_risk_news(nid, mfood, rtype, rlevel) values(?,?,?,?)");
				pstmt.setInt(1, Integer.parseInt(id));
				pstmt.setString(2, map.get("food"));
				pstmt.setString(3, "");
				pstmt.setString(4, "");
				pstmt.executeUpdate();
				pstmt.close();
				
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				System.out.println("오류가 발생했습니다."); 
				
			}
		}
	}
}
