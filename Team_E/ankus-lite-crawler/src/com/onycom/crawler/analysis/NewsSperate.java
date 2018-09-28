package com.onycom.crawler.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.NLP;

public class NewsSperate {
	private String dbTableName = "csvmerge";
	private String searchKeywordColumn = "searchkeyword";
	
	public static void main(String[] args){
		NewsSperate bs = new NewsSperate();
		bs.outputResult(bs.getDocFromDB());
	}
	
	
	/**
	 * writeFile Doc
	 */
	public void outputFoodResult(ArrayList<DocumentDEF> list){
		
			ArrayList<String> prtListNormal = new ArrayList<String>();
			
			
			CSVFileWriter cfw = new CSVFileWriter();
			
			int size = list.size();
						
			for(int j = 0; j < size; j++){
				DocumentDEF doc = list.get(j);
				if(doc.keyList.length>0)
					prtListNormal.add(doc.toStringBag2());
			}
			cfw.outputFile(prtListNormal, "news-"+dbTableName+"-Food.txt");
	}
	
	/**
	 * writeFile Doc
	 */
	public void outputResult(ArrayList<DocumentDEF> list){
		
			ArrayList<String> prtListCaution = new ArrayList<String>();
			ArrayList<String> prtListNormal = new ArrayList<String>();
			
			
			CSVFileWriter cfw = new CSVFileWriter();
			
			int size = list.size();
						
			for(int j = 0; j < size; j++){
				DocumentDEF doc = list.get(j);
				if(doc.badStr.length() == 0){
					prtListNormal.add(doc.toStringBag2());
				} else {
					prtListCaution.add(doc.toStringBag2());
				}
			}
			cfw.outputFile(prtListNormal, "news-"+dbTableName+"-Normal.txt");
			cfw.outputFile(prtListCaution, "news-"+dbTableName+"-Caution.txt");
	}

	public ArrayList<DocumentDEF> getDocFromDB(){
		ArrayList<DocumentDEF> list = new ArrayList<DocumentDEF>();
		
		String sql = "SELECT gendate, title, contents FROM "+dbTableName+" where gendate > '2016-06-30'";
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
				doc.keyStr = nlp.extractDic(doc.contents, 0);
				doc.keyList = doc.keyStr.split(",");
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
