package com.onycom.crawler.classifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections.bag.HashBag;
 



import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DOC_DEF;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.YesterDayGenerate;
import com.onycom.crawler.etc.InsertCautionData;

public class NewsDetector {

	
	public void detectDuartion(int idx, int max){
		Dictionary dic = new Dictionary();
		badMap = dic.getDBDic2("bad.txt");
		
		
		for(int i = 0 ; i < max; i++){
			core(idx-1-i);
		}
		
//		nd.core();
	}
	
	HashMap<String, HashMap<String, Integer>> badMap;
	public void core(int idx, int max){
		nlp = new NLP();
		
		kMap = nlp.dicMap;
		bMap = nlp.badMap;
		//전날 발생된 식품관련 뉴스 문서 수집 및 대표 식품 키워드 추출
		ArrayList<DOC_DEF> list = getNEWSDate(max-idx);
		//식품위해 단어 포함 여부 확인
		cautionFiltering(list);
		//식품별 식품 위해 문서 발생 건 수
		HashMap<String, ArrayList<String>> cfoodMap = countingProvider(list);
		//전전일 발생 여부 확인
		ArrayList<DOC_DEF> list2 = getNEWSDate(max-idx+1);
		cautionFiltering(list2);
		Caution2Day(cfoodMap, list2);
		
		ArrayList<DOC_DEF> retList = setCautionNewsDoc(list);
		
		InsertCautionData idc = new InsertCautionData();
		for(int i = 0; i < retList.size() ; i++){
			DOC_DEF news = retList.get(i);
			DocumentDEF doc = new DocumentDEF();
			
			doc.date = news.date;
			doc.title = news.title;
			doc.contents = news.contents;
			
			if(news.contents != null){
				doc.bagList = nlp.extractBag(news.contents);
				doc.food = news.food;
				doc.classLabel = news.cClass;
				
				idc.insertCautionNews(doc, news.nid);
			}
		}
	}
	
	
	
	public void core(int day){
		nlp = new NLP();
		
		System.out.println(day);
		
		kMap = nlp.dicMap;
		bMap = nlp.badMap;
		//전날 발생된 식품관련 뉴스 문서 수집 및 대표 식품 키워드 추출
		ArrayList<DOC_DEF> list = getNEWSDate(day);
		//식품위해 단어 포함 여부 확인
		cautionFiltering(list);
		//식품별 식품 위해 문서 발생 건 수
		HashMap<String, ArrayList<String>> cfoodMap = countingProvider(list);
		//전전일 발생 여부 확인
		ArrayList<DOC_DEF> list2 = getNEWSDate(day+1);
		cautionFiltering(list2);
		Caution2Day(cfoodMap, list2);
		
//		
		ArrayList<DOC_DEF> retList = setCautionNewsDoc(list);
		
		InsertCautionData idc = new InsertCautionData();
		for(int i = 0; i < retList.size() ; i++){
			DOC_DEF news = retList.get(i);
			DocumentDEF doc = new DocumentDEF();
			
			doc.date = news.date;
			doc.title = news.title;
			doc.contents = news.contents;
			doc.bagList = nlp.extractBag(news.contents);
			doc.food = news.food;
			doc.classLabel = news.cClass;
			doc.titleCList = news.titleCList;
			
			idc.insertCautionNews(doc, news.nid);
		}
	}
	
	private ArrayList<DOC_DEF> setCautionNewsDoc(ArrayList<DOC_DEF> list){
		Object[] kList = kMap.keySet().toArray();
		HashMap<String, String> cFoodMap = new HashMap<String, String>();
		for(int i = 0; i < kList.length; i++){
			if(kMap.get(kList[i]) > 1){
				cFoodMap.put((String)kList[i], getCautionLevel(kMap.get(kList[i])));
			}
		}
		ArrayList<DOC_DEF> retList = new ArrayList<DOC_DEF>();
		
		int size = list.size();
		for(int i = 0; i < size; i++){
			DOC_DEF news = list.get(i);
			if(cFoodMap.containsKey(news.food)){
				int len = news.titleList.length;
				for(int j = 0; j<len; j++){
					if(bMap.containsKey(news.titleList[j])){
						news.cClass = getCautionLevel(kMap.get(news.food));
//						news.keyList = nlp.extractNoun(news.contents).split(",");
						retList.add(news);
					}
				}
			}
		}
		return retList;
	}
	private String getCautionLevel(int i){
		if(i == 4){
			return "경계";
		} else if (i == 3){
			return "주의";
		} else if (i == 2){
			return "관심";
		} else {
			return "안전";
		}
	}

	private void Caution2Day(HashMap<String, ArrayList<String>> cfoodMap, ArrayList<DOC_DEF> list) {
		// TODO Auto-generated method stub
		
		int size = list.size();
		
		for(int i = 0; i < size; i++){
			DOC_DEF news = list.get(i);
			if(!news.food.equals("NULL") && cfoodMap.containsKey(news.food) && kMap.get(news.food) > 1){
				ArrayList<String> pList = cfoodMap.get(news.food);
				
				for(int j = 0; j < pList.size(); j++){
					if(pList.get(j).equals(news.provider)){
						kMap.put(news.food, 4);
					}
				}
			}
		}
	}

	public HashMap<String, Integer> kMap;
	public HashMap<String, Integer> bMap;
	public NLP nlp;
		
	public ArrayList<DOC_DEF> getNEWSDate(int day){
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(day);
		
//		String sql = "select DISTINCT nid, rdate, title, contents, [ref] from food_news where rdate = '"+yd1+"'";
		String sql = "select DISTINCT nid, rdate, title, contents, [ref] from food_news where rdate = ?";
		
		String delSql = "delete from foodrisk_news_set where nid = ?";
		
		DBConnect db = new DBConnect("CUBRIDDB.conf");
		Connection conn = db.getConnection();
		
		DBConnect db2 = new DBConnect("CUBRIDDB.conf");
		Connection conn2 = db2.getConnection();
		
		ArrayList<DOC_DEF> list = new ArrayList<DOC_DEF>();
		
		HashBag bag = new HashBag();
		try {
//			Statement stmt = conn.createStatement();
			PreparedStatement stmt = conn2.prepareStatement(sql);
			stmt.setString(1, yd1);
			
			PreparedStatement pstmt = conn2.prepareStatement(delSql);
			
			ResultSet rs = stmt.executeQuery(sql);
			
			String tList[];
//			String cList[];
			
			int tCnt = 0;
			int cCnt = 0;
			
			while(rs.next()){
				//nid를 활용하여 DB 삭제 후 다시 채워넣기
				
				//
				
				bag = new HashBag();
				DOC_DEF news = new DOC_DEF();
				news.nid = rs.getString(1);
				news.date = rs.getString(2);
				news.title = rs.getString(3);
				news.titleCList = nlp.extractBag(news.title, 1);
				news.contents = rs.getString(4);
				news.provider = rs.getString(5);
				
				pstmt.setString(1, news.nid);
				pstmt.executeUpdate();
				
				tList = nlp.extractNoun(news.title).split(",");
//				cList = nlp.extractNoun(news.contents).split(",");
				
//				news.keyList = cList;
				news.titleList = tList;
				
				int len = tList.length;
				
				
				//위해 뉴스 문서 식품명 추출 로직
				for(int i = 0; i < len ; i++){
					if(tList[i].length()>1){
						if(kMap.containsKey(tList[i])){
							news.food = tList[i];
//							tCnt++;
//							break;
						}
						
						if(tList[i].equals("식빵")){
						}
					}
				}
				
				if(news.food == null){

					news.food = "NULL";

				}
				list.add(news);
			}
			rs.close();
			stmt.close();
			conn.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("오류가 발생했습니다."); 
		}
		return list;
	}
	
	
	
	public void cautionFiltering(ArrayList<DOC_DEF> list){
		int size = list.size();
		boolean isCaution = false;
		
		String cautionWord = new String();

		for(int i = 0; i < size ; i++){
			isCaution = false;
			DOC_DEF news = list.get(i);
			int len = news.titleList.length;
			
			boolean isBad = false;
			boolean isSynonym = false;
			
			if(!news.food.equals("NULL")){
				for(int j = 0; j < len; j++){
					cautionWord = new String();
					
					if(news.titleList != null && badMap.containsKey(news.titleList[j])){
						
						isBad = true;
						
						
						HashMap<String, Integer> subMap = badMap.get(news.titleList[j]);
						if(subMap.size() > 0){
							Object keyList[] = subMap.keySet().toArray();
							for(int k = 0; k < keyList.length; k++){
								if(news.title.contains((String)keyList[k])){
									isSynonym = true;
								}
							}
							

						}
					}
					

					if(isBad && !isSynonym){
						isCaution = true;
						cautionWord = news.titleList[j];
					}
				}
				
				if(isCaution){
					//관심
					kMap.put(news.food, 2);
				} else {
					//안전
					kMap.put(news.food, 1);
				}
				list.set(i, news);
			}
		}
	}
	
	public HashMap<String, ArrayList<String>> countingProvider(ArrayList<DOC_DEF> list){
		int size = list.size();
		
		ArrayList<String> provider = new ArrayList<String>();
		
		HashMap<String, ArrayList<String>> cfoodMap = new HashMap<String, ArrayList<String>>();
		
		for(int i = 0; i < size ; i++){
			DOC_DEF news = list.get(i);
			int len = news.titleList.length;
			
			if(!news.food.equals("NULL") && kMap.get(news.food) > 1){
				for(int j = 0; j < len; j++){
					if(cfoodMap.containsKey(news.food)){
						provider = cfoodMap.get(news.food);
						boolean isNoneProvider = false;
						for(int k = 0; k < provider.size(); k++){
							if(provider.get(k).equals(news.provider)){
								isNoneProvider = true;
							}
						}
						if(!isNoneProvider){
							provider.add(news.provider);
							cfoodMap.put(news.food, provider);
							//주의
							kMap.put(news.food, 3);
						}
					} else {
						provider = new ArrayList<String>();
						provider.add(news.provider);
							
						cfoodMap.put(news.food, provider);	
					}
				}
			}
		}
		return cfoodMap;
	}
}
