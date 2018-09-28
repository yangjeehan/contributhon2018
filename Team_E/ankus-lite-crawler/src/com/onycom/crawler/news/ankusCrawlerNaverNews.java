package com.onycom.crawler.news;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.bag.HashBag;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DOC_DEF;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.classifier.NewsDetector;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.HDFSDriver;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.YesterDayGenerate;
import com.onycom.crawler.common.conf;
import com.onycom.crawler.etc.InsertCautionData;

///Users/ankus/Documents
public class ankusCrawlerNaverNews {
	
	public NaverNewsVal mVal;
	
	public ankusCrawlerNaverNews(){
		mVal = new NaverNewsVal();
	}
	
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
	}
	
	public int getListPageInfo(String oid, String getDate){
		if(oid.length() == 1){
			oid = "00"+oid;
		} else if(oid.length() == 2){
			oid = "0"+oid;
		}
		
		String url = "http://news.naver.com/main/list.nhn?oid="+oid+"&mode=LPOD&date="+getDate.replace("-", "");
		
		System.out.println("System:\t"+url);
		BufferedReader br = null;
		String split = null;
		
		while(br == null){
			br = crawler(url);
			String line = null;
			StringBuffer sb = new StringBuffer();
			
			try {
				while(br != null && (line = br.readLine())!=null){
					sb.append(line+"\r\n");
				}
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("getListPageInfo-1-오류가 발생했습니다."); 
			}
			
			Document doc = Jsoup.parse(sb.toString());
			Elements eList = null;
	
			eList = doc.getElementsByClass(mVal.list1);
			String[] andSplitList = eList.outerHtml().split("&");
			
			
			for(int i = 0; i < andSplitList.length; i++){
				
				split = andSplitList[i];
				
				if(split.contains("aid")){
					break;
				}
			}
//			System.out.println(url);
			//hear 
			try {
				//idle타임 
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println("getListPageInfo-3-오류가 발생했습니다."); 
				
			}
		}
		
		return Integer.parseInt(split.split(";")[1].split("\"")[0].split("=")[1]);
	}	
	
	public String htmlFileWriter(String mPath, String id, StringBuffer buf){
		FileWriter fw;
		BufferedWriter bw;
		File f = new File(mPath+id);
		f.getParentFile().mkdirs();
	
		try {
			fw = new FileWriter(mPath+id);
			bw = new BufferedWriter(fw);
								
			bw.append(buf.toString());
			bw.flush();
			fw.flush();
				
			bw.close();
			fw.close();
			
//			HDFSDriver hd = new HDFSDriver();
//			String[] args = new String[1];
//			args[0] = mPath+id;
//			try {
//				hd.run(args);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f.getAbsolutePath();
	}
	
	/**
	 * 뉴스 페이지에서 기간내 마지막 문서 ID를 추출 
	 * @param oid
	 * @param yd
	 * @return
	 */
	public int getlastAidNumber(String oid, String yd){
		return getListPageInfo(oid, yd);
	}
	
	/**
	 * 뉴스 페이지에서 기간내 시작 문서 ID를 추출 
	 * @param oid
	 * @param yd
	 * @return
	 */
	public int getFirstAidNumber(String oid, String yd){
		return getListPageInfo(oid, yd)+1;
	}
	
	NLP nlp2;
	
	/**
	 * DB에서 수집 대상인 뉴스 사이트의 ID로드 
	 * @return
	 */
	public HashMap<String, String> getCrawlingConf(){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		HashMap<String, String> cMap = new HashMap<String, String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(mVal.oidSql);
			
			while(rs.next()){
				cMap.put(rs.getString(2), rs.getString(1));
				
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("getListPageInfo-2-오류가 발생했습니다."); 
			
		}
		return cMap;
	}
	public void newsCrawler(int dueDate, int limDate) {
		nlp2 = new NLP();
		long curTime = System.currentTimeMillis();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(limDate);
		String yd2 = yd.getYesterDay(dueDate);
		
		HashMap<String, String> cMap = new HashMap<String, String>();

		Dictionary dic = new Dictionary();
		HashMap<String, Integer> fMap = dic.getDicMap("dic.news");
		
		//oid 목록 로드 
		cMap = getCrawlingConf();
		
		Object[] oidList = cMap.keySet().toArray();
		
		System.out.println("뉴스 수집기간: "+yd1+"~"+yd2);
		
		ArrayList<DOC_DEF> finList = new ArrayList<DOC_DEF>();
		
		Object[] keyList = fMap.keySet().toArray();
		for( int o = 0; o < oidList.length; o++){
			String oid = (String)oidList[o];
			System.out.println(cMap.get(oid)+"\t News Crawling Start");
			
			if(oid.length() == 1){
				oid = "00"+oid;
			} else if(oid.length() == 2){
				oid = "0"+oid;
			}
			
			String provider = cMap.get(oid);
			
			int start = getFirstAidNumber(oid, yd2)+1;
			int end = getlastAidNumber(oid, yd1)+2;
			
			System.out.println("수집기간 내 존재하는 문서 ID: "+start+"~"+end);
			
			String addr = "http://news.naver.com/main/read.nhn?mode=LPOD&oid="+oid+"&aid=????";
						
			for(int i = start ; i < end ; i++){
				String addrI = getAddr(addr, i);
				BufferedReader br = crawler(addrI);
				
//				System.out.println("수집 주소:\t"+addrI);
				//문서내 정보 추출 
				HashMap<String, String> map = getContents(br, addrI, oid, provider, i);
				
				//정보가 온전하면 문서 분석 및 저장 
				if(map.size() > 0 && map.get("exist").equals("1")){
					String keywords = map.get("content");
					
					boolean isFood = false;
					String foodName = new String();
					
//					System.out.println(keywords+"\t"+keyList.length);
					for(int j = 0; j < keyList.length ;j++){
						if(keywords != null && keywords.contains((String)keyList[j])){
							isFood = true;
							foodName = (String)keyList[j];
							break;
						}
					}
					
					//문서 내 식품명이 있으면 저장 
					if(isFood){
						System.out.println(foodName);
						String id = DBInputKeyword(map);
					}
				}
			
				try {
					//idle타임 
					Thread.sleep(2);
				} catch (InterruptedException e) {
					System.out.println("getListPageInfo-3-오류가 발생했습니다."); 
					
				}				
			}
		}
		
		//System.out.println("Caution News Detect Process Running...");
//		NewsDetector ndc = new NewsDetector();
//		ndc.detectDuartion(dueDate, limDate);                      
		//System.out.println("Process Time:\t"+(System.currentTimeMillis()-curTime));
	}
	
	
	public void newsCrawler(int dueDate) {
		
		nlp2 = new NLP();
		long curTime = System.currentTimeMillis();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(1);
		String yd2 = yd.getYesterDay(dueDate+1);
		
		HashMap<String, String> cMap = new HashMap<String, String>();

		Dictionary dic = new Dictionary();
		HashMap<String, Integer> fMap = dic.getDicMap("dic.news");
		
		cMap = getCrawlingConf();

		String[] oidList = (String[])cMap.keySet().toArray();
		
		ArrayList<DOC_DEF> finList = new ArrayList<DOC_DEF>();
		for( int o = 0; o < oidList.length; o++){
			String oid = oidList[o];
			String provider = cMap.get(oid);
			
			//System.out.println(oid+"\t News Crawling Start");
			int start = getFirstAidNumber(oid, yd2)+1;
			int end = getlastAidNumber(oid, yd1)+2;
			
			//System.out.println(start);
			//System.out.println(end);
			
			String addr = "http://news.naver.com/main/read.nhn?mode=LPOD&mid=shm&oid="+oid+"&aid=????";
		
			for(int i = start ; i < end ; i++){
				String addrI = getAddr(addr, i);
				BufferedReader br = crawler(addrI);
//				//System.out.println(addrI);
				HashMap<String, String> map = getContents(br, addrI, oid, provider, i);
				
//				//System.out.println(map.size());
				if(map.size() > 0 && map.get("exist").equals("1")){
					String keywords = map.get("keyList");
					
//					//System.out.println("keywords"+keywords);
					String[] keyList = keywords.split("\t");
					
					int len = keyList.length;
					String[] strList = new String[len];
					
					boolean isFood = false;
					String foodName = new String();
					String brand = new String();
					
					for(int j = 0; j < len ;j++){
						strList[j] = keyList[j].split(":")[0];
						
						if(fMap.containsKey(strList[j])){
							isFood = true;
							if(fMap.get(strList[j]) == 1){
								brand = strList[j];
							} else if (fMap.get(strList[j]) == 2){
								foodName = strList[j];
							}
						}
					}
					
					if(isFood){
						String id = DBInputKeyword(map, foodName, brand);
					}
				}
			
				try {
					//int random = (int)(Math.random() * (max - min +1)+min);
//					//System.out.println(random);
					Thread.sleep(2);
				} catch (InterruptedException e) {
					System.out.println("getListPageInfo-4-오류가 발생했습니다."); 
					
				}				
			}
			//last date record
			insertLastDT(oid);
//			setConfFile(cMap);
		}
		//System.out.println("Crawling News Process Finish...");
		//System.out.println("Caution News Detect Process Running...");
		NewsDetector ndc = new NewsDetector();
		ndc.core(dueDate);                      
		//System.out.println("Process Time:\t"+(System.currentTimeMillis()-curTime));
	}
	private String DBInputKeyword(HashMap<String, String> map, String foodName, String brand) {
		// TODO Auto-generated method stub
		DBConnect db = new DBConnect();
		PreparedStatement stmt;
		ResultSet rs;

		
		String bagList[] = map.get("keyList").split("\t");
		String id = null;
		int count = -1;
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			String pSql = mVal.countSql;
			stmt = conn.prepareStatement(pSql);
			stmt.setString(1, map.get("title"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			count = rs.getInt(1);
//			//System.out.println("중복카운트:\t"+count);
			if(count == 0){
				PreparedStatement pstmt = conn.prepareStatement(mVal.insertQeuery);
				
	//			stmt = conn.createStatement();
	//			stmt.execute(
	//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
	//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
				pstmt.setString(1, map.get("provider"));
				pstmt.setString(2, map.get("date").split(" ")[0]);
				pstmt.setString(3, map.get("link"));
				pstmt.setString(4, map.get("title"));
				pstmt.setString(5, map.get("content"));
				pstmt.setString(6, map.get("filelink"));
				if(brand != null){
					pstmt.setString(7, brand);
				} else {
					pstmt.setString(7, "");
				}
				
				if(foodName != null){
					pstmt.setString(8, foodName);
				} else {
					pstmt.setString(8, "");
				}
				
				pstmt.executeUpdate();
				
				pstmt.close();
				
				stmt = conn.prepareStatement("select last_insert_id()");
				rs = stmt.executeQuery();
				rs.next();
				id = rs.getString(1);
				
				for(int i = 0; i < bagList.length; i++){
					pstmt = conn.prepareStatement(mVal.kwrdInput);
					pstmt.setString(2, bagList[i].split(":")[0]);
					pstmt.setString(3, bagList[i].split(":")[1]);
					pstmt.setString(1, id);
					pstmt.executeUpdate();
					pstmt.close();
				}

			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("getListPageInfo-9-오류가 발생했습니다."); 
			
		}
		
		if(count != 0){
			return "-1";
		} else {
			return id;
		}
	}
	public void newsDection(int dueDate){
		//System.out.println("Caution News Detect Only Process Running...");
		NewsDetector ndc = new NewsDetector();
		ndc.core(dueDate);                      
	}
	
	public void insertLastDT(String oid){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE news_oid SET crawling_dt = NOW() WHERE [oid] = '"+oid+"'");
			
			stmt.close();
			conn.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("getListPageInfo-5-오류가 발생했습니다.");  
			
		}
	}

	private Connection conn;
	
	public String saveFile(StringBuffer buf, String oid, String date, int idx){
		FileWriter fw;
		BufferedWriter bw;
		
		date = date.split(" ")[0];
		File f = new File("news/"+oid+"/"+date+"/");
//		//System.out.println(f.getAbsolutePath());
		f.mkdirs();
		String path = "news/"+oid+"/"+date+"/"+idx+".html";
		
		try {
			fw = new FileWriter(path);
			bw = new BufferedWriter(fw);
			bw.append(buf.toString());
			bw.flush();
			fw.flush();
			
			bw.close();
			fw.close();
			
//			HDFSDriver hd = new HDFSDriver();
//			String[] args = new String[1];
//			args[0] = path;
//			try {
//				hd.run(args);
//			} catch (Exception e) {
//				System.out.println("getListPageInfo-6-오류가 발생했습니다."); 
//			}
			
			f.delete();
		} catch (IOException e) {
			System.out.println("getListPageInfo-7-오류가 발생했습니다.");  
			
		}
		
		return path;
	}
	
	public void DBInput(HashMap<String, String> map){
		DBConnect db = new DBConnect();
		Statement stmt;
		ResultSet rs;
			
//		resMap.put("title", at.text());
//		resMap.put("provider", newsProvider);
//		resMap.put("link", link);
//		resMap.put("date", t11.text());
//		resMap.put("content", abc.text());
//		resMap.put("exist", "1");
//		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO newsdata(provider, gendate, httplink, title, contents, filelink) VALUES (?,?,?,?,?,?)");
			
//			stmt = conn.createStatement();
//			stmt.execute(
//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
			pstmt.setString(1, map.get("provider"));
			pstmt.setString(2, map.get("date"));
			pstmt.setString(3, map.get("link"));
			pstmt.setString(4, map.get("title"));
			pstmt.setString(5, map.get("content").replace("'", ""));  
			pstmt.setString(6, map.get("filelink"));
			pstmt.executeUpdate();
			
			pstmt.close();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select last_insert_id()");
			rs.next();
			String id = rs.getString(1);	
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("getListPageInfo-8-오류가 발생했습니다."); 
			
		}
		
	}
	
	public String DBInputKeyword(HashMap<String, String> map){
		DBConnect db = new DBConnect();
		PreparedStatement stmt;
		ResultSet rs;

		String bagList[] = map.get("keyList").split(",");
		System.out.println(map.get("keyList"));
		
		HashBag bag = new HashBag();
		for(int i = 0; i < bagList.length; i++){
			bag.add(bagList[i]);
		}
		
		String id = null;
		int count = -1;
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			String pSql = mVal.countSql;
			stmt = conn.prepareStatement(pSql);
			stmt.setString(1, map.get("title"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			count = rs.getInt(1);

			if(count == 0){
				PreparedStatement pstmt = conn.prepareStatement(mVal.insertQeuery);
				
	//			stmt = conn.createStatement();
	//			stmt.execute(
	//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
	//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
				pstmt.setString(1, map.get("provider"));
				pstmt.setString(2, map.get("date").split(" ")[0]);
				pstmt.setString(3, map.get("link"));
				pstmt.setString(4, map.get("title"));
				pstmt.setString(5, map.get("content"));
				pstmt.setString(6, map.get("filelink"));
				
//				System.out.println(map.get("title"));
//				System.out.println(map.get("content"));
				
				pstmt.executeUpdate();
				
				pstmt.close();
				
				stmt = conn.prepareStatement("select last_insert_id()");
				rs = stmt.executeQuery();
				rs.next();
				id = rs.getString(1);
				
				Object keyList[] = bag.toArray();
				HashMap<String, Integer> uniqueMap = new HashMap<String, Integer>();
				for(int i = 0; i < keyList.length; i++){
					
					if(!uniqueMap.containsKey((String)keyList[i])){
						pstmt = conn.prepareStatement(mVal.kwrdInput);
						pstmt.setString(2, (String)keyList[i]);
						pstmt.setString(3, ""+bag.getCount((String)keyList[i]));
						pstmt.setString(1, id);
						pstmt.executeUpdate();
						pstmt.close();
						
						uniqueMap.put((String)keyList[i], 1);
//						System.out.println("DB Input");
					}
				}
				
//				for(int i = 0; i < bagList.length; i++){
//					pstmt = conn.prepareStatement("Insert into food_risk_word(nid, keyword_type, cnt, rword) values(?,?,?,?)");
//					pstmt.setString(4, bagList[i].split(":")[0]);
//					pstmt.setInt(3, Integer.parseInt(bagList[i].split(":")[1]));
//
//					if (nlp2.badMap.containsKey(bagList[i].split(":")[0])){
//						pstmt.setString(2, "caution");
//					} else {
//						pstmt.setString(2, "normal");
//					}
//					
//					pstmt.setInt(1, Integer.parseInt(id));
//					pstmt.executeUpdate();
//					pstmt.close();
//				}
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("getListPageInfo-9-오류가 발생했습니다."); 
			
		}
		
		if(count != 0){
			return "-1";
		} else {
			return id;
		}
		
		
	}
	
	public ArrayList<String> getKeyword(String path){
		FileReader fr;
		BufferedReader br;
		
		ArrayList<String> map = new ArrayList<String>();
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			String line = null;
			
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				map.add(line);
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("getListPageInfo-10-오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("getListPageInfo-12-오류가 발생했습니다."); 
			
		}
		
		
		return map;
	}
	NLP nlp;
	public HashMap<String, String> getContents(BufferedReader br, String link, String oid, String newsProvider, int idx){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while(br !=null && (line = br.readLine())!=null){
//				System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("getListPageInfo-13-오류가 발생했습니다.");  
			
		}
		
		Document doc = Jsoup.parse(sb.toString());
		
		if(nlp == null){
			nlp = new NLP();
		}
		try{
			if( doc.getElementsByClass(mVal.contents1).size() > 0 ){
				Element t11 = doc.getElementsByClass(mVal.contents1).get(0);
//				System.out.println(t11.toString());
		//			t11 = t11.get(0);
				Element abc = doc.getElementById(mVal.contents2);
					
				Element at = doc.getElementById(mVal.contents3);
		
		//			for(int j = 0; j < size ; j++){
		
				if(at != null){
					resMap.put("title", at.text().replace("'", ""));
					resMap.put("tKeyword", nlp.extractNoun(at.text().replace("'", "")));
					resMap.put("provider", newsProvider);
					resMap.put("link", link);
					resMap.put("date", t11.text());
					resMap.put("content", abc.text().replace("'", ""));
								
					if(abc.text() != null || abc.text().length() > 0)
						resMap.put("keyList", nlp.extractNoun(abc.text()));
					else {
						resMap.put("keyList", "");
					}
					resMap.put("exist", "1");
					resMap.put("filelink", saveFile(sb, newsProvider, t11.text(), idx));	
				} else {
					System.out.println(link);
					resMap.put("exist", "0");
				}
			} else {
				resMap.put("exist", "0");
			}
		} catch (Exception e){
//			Date date = new Date();
//			
//			String path = "logs/";
//			String fileName = "crawler_"+date.getYear()+date.getMonth()+date.getDay()+".txt";
//				
//			StringBuffer buf = new StringBuffer();
//				
//			buf.append("네이버 블로그 웹페이지 변경 또는 주소체계 변경으로 웹문서가 수집되지 않으므로 유지보수가 필요합니다.");			
//			htmlFileWriter(path, fileName, buf);
			
			e.printStackTrace();
			System.out.println("네이버 블로그 웹페이지 변경 또는 주소체계 변경으로 웹문서가 수집되지 않으므로 유지보수가 필요합니다.");
		}
		try {
			//idle타임 
			Thread.sleep(50);
		} catch (InterruptedException e) {
			System.out.println("getListPageInfo-3-오류가 발생했습니다."); 
			
		}
		return resMap;
	}
	
	public void setConfFile(HashMap<String, String> map){
		FileWriter fw;
		BufferedWriter bw;
		
		try {
			fw = new FileWriter("news.conf");
			bw = new BufferedWriter(fw);
			
			bw.append("oid\t");
			bw.append(map.get("oid")+"\r\n");
			
			bw.append("addrstart\t");
			bw.append(map.get("addrend")+"\r\n");
			
			bw.append("addrend\t");
			bw.append((Integer.parseInt(map.get("addrend"))+100)+"\r\n");
			
			bw.flush();
			fw.flush();
			
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("getListPageInfo-14-오류가 발생했습니다."); 
			
		}
		
	}
	
	public void removeHTML(String targetPath, String fileName){
		FileReader fr;
		BufferedReader br;
		
		FileWriter fw;
		BufferedWriter bw;
		
		try {
			fr = new FileReader(targetPath+"/"+fileName+".html");
			br = new BufferedReader(fr);
			
			fw = new FileWriter(targetPath+"/"+fileName+".txt");
			bw = new BufferedWriter(fw);
			String line = null;
			StringBuffer sb = new StringBuffer();
			String regExp = "!\"#[$]%&\\(\\)\\{\\}@`[*]:[+];-.<>,\\^~|'\\[\\]";
			
			while((line = br.readLine())!=null){
//				bw.append(line.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
				line = line.replaceAll("<[^>]*>", "");
				line = line.replaceAll("[a-zA-Z]", "");
				line = line.replaceAll("[0-9]", "");
//				//System.out.println("pre: "+line);
				line = spCharRid(line);
//				//System.out.println("aft: "+line);
				sb.append(line);
				sb.append("\r\n");
			}
			
			 Document doc = Jsoup.parse(sb.toString());
//			 Document doc = Jsoup.parse(html);
			 br.close();
			 fr.close();
//			 //System.out.println(doc.text());
//			 //System.out.println(doc.textNodes());
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			System.out.println("getListPageInfo-15-오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("getListPageInfo-16-오류가 발생했습니다."); 
		}
		
	}
	
	public String getAddr(String addr, int pageNum){
		String pnStr = new String().valueOf(pageNum);
		int len = pnStr.length();
		int zeroLen = 10 - len;
		
		StringBuffer zeroStr = new StringBuffer();
		for(int i = 0 ; i < zeroLen ; i++){
			zeroStr.append("0");
		}
		//9999999999
		//1234567890
		
		zeroStr.append(pnStr);
		addr = addr.replace("????", zeroStr.toString());

		return addr;
	}
	public String spCharRid(String strInput){
//		  //System.out.println("@spCharRid original: "+ strInput);
		  String strWork = strInput;
		  String[] spChars = {
				    "`", "-", "=", ";", "'", "/", "~", "!", "@", 
				    "#", "$", "%", "^", "&", "|", ":", "<", ">", 
				    //"\",
				    "*", 
				    "+", 
				    "{", 
				    "}",",","[","]","_","\"","\\",
				    "?","(",")",
				    "."
				  };
		  
		  int spCharLen = spChars.length; 
		  
		   for(int i = 0; i < spCharLen; i++){
//		    //System.out.println("@for @proceed : "+i);
//		    //System.out.println("@spCharRid @target is : " + spChars[i]);
		    strWork = strWork.replace(spChars[i], "");
//		    //System.out.println("@spCharRid @replaceAll: "+ strWork);
		   }

//		  //System.out.println("@spCharRid output  : "+ strWork);
		  return strWork;
		 }
	public BufferedReader crawler(String addr){
		HttpGet http = new HttpGet(addr);

	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
		    HttpEntity entity = response.getEntity();
		    
		    ContentType contentType = ContentType.getOrDefault(entity);
	        Charset charset = contentType.getCharset();
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		} catch (IOException e) {
			System.out.println("getListPageInfo-18-오류가 발생했습니다.");  
			
		}
	    
	    return retBr;
	}
	
	public void saveHtml(BufferedReader br, String targetPath, String fileName){
		FileWriter fw;
		try {
			fw = new FileWriter(targetPath +"/" + fileName+".html");
			BufferedWriter bw = new BufferedWriter(fw);
			
			String line = "";
		    while((line=br.readLine()) != null){
		    	bw.append(line+"\n");
		    	bw.flush();
		    	fw.flush();
		    }
		    
		    bw.close();
		    fw.close();
		} catch (IOException e) {
			System.out.println("getListPageInfo-19-오류가 발생했습니다."); 
			
		}
	}
}