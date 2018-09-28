package com.onycom.crawler.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.NLP;

public class BlogSperate {
	private String dbTableName = "blogdata";
	private String searchKeywordColumn = "searchkeyword";
	
	public static void main(String[] args){
		BlogSperate bs = new BlogSperate();
//		ArrayList<String> keywordList = bs.getSearchKeyword();
		ArrayList<String> keywordList = new ArrayList<String>();
		keywordList.add("인니다이어트");
		
		bs.outputResult(bs.getDocFromDB(keywordList));
	}
	
	/**
	 * writeFile Doc
	 */
	public void outputResult(HashMap<String, ArrayList<DocumentDEF>> map){
		Object[] keyList = map.keySet().toArray();
		int len = keyList.length;
		
		for(int i = 0; i < len ; i++){
			ArrayList<DocumentDEF> list = map.get((String)keyList[i]);
			ArrayList<String> prtListCaution = new ArrayList<String>();
			ArrayList<String> prtListNormal = new ArrayList<String>();
			
			
			CSVFileWriter cfw = new CSVFileWriter();
			
			int size = list.size();
						
			for(int j = 0; j < size; j++){
				DocumentDEF doc = list.get(j);
				if(doc.badStr.length() == 0){
					prtListNormal.add(doc.toStringExceptKeyword());
				} else {
					prtListCaution.add(doc.toStringBag());
				}
			}
			cfw.outputFile(prtListNormal, "blog-"+keyList[i]+"-Normal.txt");
			cfw.outputFile(prtListCaution, "blog-"+keyList[i]+"-Caution.txt");
		}
	}
	
	/**
	 * get Data from DB group by keyword
	 */
	public HashMap<String, ArrayList<DocumentDEF>> getDocFromDB(ArrayList<String> keyList){
		HashMap<String, ArrayList<DocumentDEF>> map = new HashMap<String, ArrayList<DocumentDEF>>();
		
		ArrayList<DocumentDEF> list = new ArrayList<DocumentDEF>();
		int keySize = keyList.size();
		
		for(int i = 0; i < keySize ; i++){
			String searchKeyword = keyList.get(i);
			map.put(keyList.get(i), getDocFromDB(keyList.get(i)));
		}
		
		return map;
	}
	public ArrayList<DocumentDEF> getDocFromDB(String keyword){
		ArrayList<DocumentDEF> list = new ArrayList<DocumentDEF>();
		
		String sql = "SELECT gendate, title, contents, httplink FROM blogdata WHERE searchkeyword = '"+keyword+"'";
		System.out.println(sql);
		DBConnect conn = new DBConnect();
		ResultSet rs = conn.getDBData(sql);
		
		NLP nlp = new NLP();
		try {
			while(rs.next()){
				DocumentDEF doc = new DocumentDEF();

				doc.date = rs.getString(1);
				doc.title = rs.getString(2);
				doc.titleList =  nlp.extractNoun(doc.title).split(",");
				doc.contents = rs.getString(3);
				doc.link = rs.getString(4);
				doc.badStr = nlp.extractDic(doc.contents, 1);
				doc.badList = doc.badStr.split(",");
				
				list.add(doc);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * ����Ÿ���̽��� ������ Ű���� ��� �޾ƿ��� �Լ�
	 */
	public ArrayList<String> getSearchKeyword(){
		String sql = "select distinct "+searchKeywordColumn+" from "+dbTableName;
		System.out.println(sql);
		DBConnect conn = new DBConnect();
		ResultSet rs = conn.getDBData(sql);
		
		ArrayList<String> list = new ArrayList<String>();
		try {
			while(rs.next()){
				list.add(rs.getString(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
}
