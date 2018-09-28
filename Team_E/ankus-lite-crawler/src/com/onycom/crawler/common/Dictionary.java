package com.onycom.crawler.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DB.DBConnect;

public class Dictionary {
	private Connection conn;
	
	private String foodSql =  "SELECT DISTINCT item from worddic where dtype = '식품'";
	private String foodSimSql = "SELECT synonym, item FROM worddic_synonym";
	private String badSql = "SELECT item, synonym from worddic WHERE dtype = '식품위해'";
	private String foodNewsSql =  "SELECT brand, item from inner_data_demand WHERE NVL(brand,'')<>'' AND NVL(item,'')<>''  UNION SELECT word, NULL  FROM worddic WHERE wtype='관심'";
	private String neuSql = "SELECT item from worddic WHERE category = '중성' OR category = '부정'";
	private String synSql = "SELECT a.item, b.synonym from worddic a, worddic_synonym b WHERE a.dtype = '식품위해' AND a.item = b.item";
	private String bestSql1 = "select scrap_date from naver_best_item_depth1 order by scrap_date desc limit 1";
	private String bestSql2 = "select distinct depth1 from naver_best_item_depth1 where scrap_date = ?";
	
	private String newsSql = "SELECT brand, item from inner_data_demand WHERE NVL(brand,'')<>'' AND NVL(item,'')<>''  UNION SELECT word, NULL  FROM worddic WHERE wtype='관심'";
//	private String newsSql = "SELECT word, NULL  FROM worddic WHERE wtype='관심'";
	
	public HashMap<String, String> getDBSimDic(){
		HashMap<String, String> map = new HashMap<String, String>();
		


		DBConnect db = new DBConnect();

		Statement stmt;
		
		
		try {
			ResultSet rs = null;
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			stmt = conn.createStatement();
			
			
			rs = stmt.executeQuery(foodSimSql);
			 
			int cnt = 0;
			while(rs.next()){
				map.put(rs.getString(1), rs.getString(2));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
		}
		
		return map;
	}
	public HashMap<String, Integer> getDBDic(String path){
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		

		
		DBConnect db = new DBConnect();

		Statement stmt;
		ResultSet rs = null;
		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			conn = db.getConnection();
			
			stmt = conn.createStatement();
			
			if(path.equals("dic.news") || path.equals("dic.blog") ){
				rs = stmt.executeQuery(foodNewsSql);
			} else if (path.equals("dic.txt")){
				rs = stmt.executeQuery(foodNewsSql);
			} else if(path.equals("bad.txt")){
				rs = stmt.executeQuery(badSql);
			} else if(path.equals("neutral")){
				rs = stmt.executeQuery(neuSql);
			}
			int cnt = 0;
			
			while(rs.next()){
				if(path.equals("dic.news") || path.equals("dic.txt") || path.equals("dic.blog") ){
					if(rs.getString(1) != null){
						map.put(rs.getString(1), 1);
					}
					
					if(rs.getString(2) != null){
						map.put(rs.getString(2), 2);
					}
				} else {
					map.put(rs.getString(1), cnt);
					cnt++;
				}
				
				if(path.equals("sim")){
					
				}
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Dictionary getDBDic 오류가 발생했습니다."); 
		}
		
		
		return map;
	}
	
	
	public HashMap<String, HashMap<String, Integer>> getDBDic2(String path){
		HashMap<String, HashMap<String, Integer>> dicMap = new HashMap<String, HashMap<String, Integer>>();
		

		
		DBConnect db = new DBConnect();

		Statement stmt;
		
		try {
			ResultSet rs = null;
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			stmt = conn.createStatement();
			
			if(path.equals("bad.txt")){
				rs = stmt.executeQuery(synSql);
				
				
				int cnt = 0;
				
				String item = new String();
				String synonym = new String();
				
				
				HashMap<String, Integer> subMap = new HashMap<String, Integer>();
				
				while(rs.next()){
					
					
					subMap = new HashMap<String, Integer>();
					
					if(item.equals(rs.getString(1))){
						item = rs.getString(1);
						synonym = rs.getString(2);
						
						subMap = dicMap.get(item);
						if(synonym.length() > 0){
							String[] synList = synonym.split(",");
							for(int i = 0; i < synList.length ; i++){
								subMap.put(synList[i], i);
							}
						}
						
						dicMap.put(item, subMap);
					} else if(cnt == 0){
						item = rs.getString(1);
						synonym = rs.getString(2);
						
						subMap = new HashMap<String, Integer>();
						
						if(synonym.length() > 0){
							String[] synList = synonym.split(",");
							for(int i = 0; i < synList.length ; i++){
								subMap.put(synList[i], i);
							}
						}

						dicMap.put(item, subMap);
					} else {
						item = rs.getString(1);
						synonym = rs.getString(2);
						
						subMap = new HashMap<String, Integer>();
						if(synonym.length() > 0){
							String[] synList = synonym.split(",");
							for(int i = 0; i < synList.length ; i++){
								subMap.put(synList[i], i);
							}
						}
						
						dicMap.put(item, subMap);
					}
				}
				
				rs = stmt.executeQuery(badSql);
				
				while(rs.next()){
					
					item = rs.getString(1);
					synonym = rs.getString(2);

					if(!dicMap.containsKey(item)){
						dicMap.put(item, new HashMap<String, Integer>());
					}
					cnt++;
				}
			} 
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
		}
		
		
		return dicMap;
	}
	
	
	public ArrayList<String> getDBDicList(String path){
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		ArrayList<String> list = new ArrayList<String>();
			
		DBConnect db = new DBConnect();

		PreparedStatement stmt = null;
		
		String brand = new String();
		
		try {
			ResultSet rs = null;
			if(conn == null || conn.isClosed()){
				
			}
			conn = db.getConnection();

//			if(path.equals("dic.txt")){
//				stmt = conn.prepareStatement(foodSql);
//				rs = stmt.executeQuery();
//				
//			} else 
				if(path.equals("bad.txt")){
				stmt = conn.prepareStatement(badSql);
				rs = stmt.executeQuery();
				
			} else if (path.equals("best")){
				stmt = conn.prepareStatement(bestSql1);
				rs = stmt.executeQuery();
				rs.next();
				
				String date = rs.getString(1);
				stmt = conn.prepareStatement(bestSql2);
				stmt.setString(1, date);
				rs = stmt.executeQuery();
				
			} else if (path.equals("neutral")){
				stmt = conn.prepareStatement(neuSql);
				rs = stmt.executeQuery();
				
			} else if (path.equals("dic.news")){
				stmt = conn.prepareStatement(newsSql);
				rs = stmt.executeQuery();
			} else if (path.equals("dic.blog")){
				stmt = conn.prepareStatement(newsSql);
				rs = stmt.executeQuery();
			}
			int cnt = 0;
			
			if(path.equals("dic.news")){
				while(rs.next()){
					if(brand.equals(rs.getString(1))){
						list.add(rs.getString(1));
						list.add(rs.getString(2));
						
						brand = rs.getString(1);
					} else {
						list.add(rs.getString(2));
					}
					
				}
			} else if (path.equals("dic.blog")){
				while(rs.next()){
					if(rs.getString(1) == null || rs.getString(1).length() == 0){
						list.add(" \t"+rs.getString(2));
					}
					
					if(rs.getString(2) == null || rs.getString(2).length() == 0){
						list.add(rs.getString(1)+"\t ");
					}
				}
			} else {
				while(rs.next()){
					list.add(rs.getString(1));
				}
			}
//			
//			for(int i = 0; i < list.size() ; i++){
//				System.out.println(list.get(i));
//			}
			
			stmt.close();
			rs.close();
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("getDBDicList 오류가 발생했습니다."); 
			
		}
		return list;
	}
	
	public HashMap getDicMap(String path){
		if(path.equals("dic.news")){
			return getDBDic(path);
		} else if(path.equals("dic.txt")){
			return getDBDic(path);
		} else if(path.equals("bad.txt")){
			return getDBDic(path);
		} else if(path.equals("sim")){
			return getDBSimDic();
		} else {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			
			FileReader fr;
			BufferedReader br;
			
			String line;
			
			int cnt = 0;
			try {
				fr = new FileReader(path);
				br = new BufferedReader(fr);
				
				while((line = br.readLine())!=null){
						map.put(line, cnt);
						cnt++;
				}
				br.close();
				fr.close();
			} catch (FileNotFoundException e) {
				System.out.println("NLP get DicMap 1 - 오류가 발생했습니다."); 
				
			} catch (IOException e) {
				System.out.println("NLP get DicMap 2 - 오류가 발생했습니다."); 
				
			}
			return map;
		}
		
	}
	
	public HashMap<String, String> getJoinDicMap(String path){
		HashMap<String, String> map = new HashMap<String, String>();
		
		FileReader fr;
		BufferedReader br;
		
		String line;
		
		int cnt = 0;
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line.split("\t")[0], line.split("\t")[1]);
				cnt++;
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
	
	public ArrayList<String> getDicList(String path){
		
		if(path.equals("dic.news")){
			return getDBDicList(path);
		} else if(path.equals("dic.blog")){
			return getDBDicList(path);
		} else if(path.equals("bad.txt")){
			return getDBDicList(path);
		} else if(path.equals("best")){
			return getDBDicList(path);
		} else {
			ArrayList<String> list = new ArrayList<String>();
			
			FileReader fr;
			BufferedReader br;
			
			String line;
			
			try {
				fr = new FileReader(path);
				br = new BufferedReader(fr);
				
				while((line = br.readLine())!=null){
					list.add(line);
				}
				
				br.close();
				fr.close();
			} catch (FileNotFoundException e) {
				System.out.println("getDicList 오류가 발생했습니다."); 
				
			} catch (IOException e) {
				System.out.println("getDicList 오류가 발생했습니다."); 
				
			}
			return list;
		}
	}
}
