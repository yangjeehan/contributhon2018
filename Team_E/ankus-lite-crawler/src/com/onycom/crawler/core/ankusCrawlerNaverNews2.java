package com.onycom.crawler.core;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.analysis.NewsClassifier;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.HDFSDriver;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.YesterDayGenerate;
import com.onycom.crawler.common.conf;
import com.onycom.crawler.etc.InsertCautionData;

///Users/ankus/Documents
public class ankusCrawlerNaverNews2 {
	
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
	}
	public int getListPageInfo(String oid, String getDate){
		String url = "http://news.naver.com/main/list.nhn?oid="+oid+"&mid=sec&mode=LPOD&date="+getDate.replace("-", "");
		
		BufferedReader br = crawler(url);
		String line = null;
		StringBuffer sb = new StringBuffer();
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Document doc = Jsoup.parse(sb.toString());
		Elements eList = doc.getElementsByClass("nclicks(fls.list)");
		
		String[] andSplitList = eList.outerHtml().split("&");
		
		String split = null;
		for(int i = 0; i < andSplitList.length; i++){
			split = andSplitList[i];
			if(split.contains("aid")){
				break;
			}
		}
		
		return Integer.parseInt(split.split(";")[1].split("\"")[0].split("=")[1]);
	}
	
	public int getlastAidNumber(String oid, String yd){
		return getListPageInfo(oid, yd);
	}
	
	public int getFirstAidNumber(String oid, String yd){
		return getListPageInfo(oid, yd)+1;
	}
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



	 * @param args
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	
	public void newsCrawler() {
		
		long curTime = System.currentTimeMillis();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(1);
		String yd2 = yd.getYesterDay(2);
		// 1. 媛��졇�삤湲곗?�� �떆媛� 李띻�?
//		System.out.println(" Start Date : " + getCurrentData());
		
		conf cf = new conf();
		HashMap<String, String> cMap = new HashMap<String, String>();
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> fMap = dic.getDicMap("dic.txt");
		
		System.out.println("식품단어 크기: "+fMap.size());
		cMap = cf.confFileReader("news");
		
		ankusCrawlerNaverNews2 ac = new ankusCrawlerNaverNews2();

		//sid1 = ?��꾩빞, sid2 = �꽭?���遺꾩빞, oid = �돱�뒪�젣?�듭�?, aid = ?��몄꽌踰덊?��(10�옄?���?)
//		sid1= ?��꾩빞 101(寃쎌?��), 102(�궗�쉶), 103(�깮�솢/?��명솕)
//		055:SBS
//		056:KBS
//		214:MBC
		String[] oidList = cMap.get("oid").split(",");
		
		for( int o = 0; o < oidList.length; o++){
			String oid = oidList[o];
			int start = ac.getFirstAidNumber(oid, yd2)+1;
			int end = ac.getlastAidNumber(oid, yd1)+2;
			
			System.out.println(start);
			System.out.println(end);
			
			String addr = "http://news.naver.com/main/read.nhn?mode=LPOD&mid=shm&oid="+oid+"&aid=????";
			
			//KBS
//			http://news.naver.com/main/read.nhn?mode=LPOD&mid=sec&oid=056&aid=0010363277
			
			//9999999999
			//0000000098
			
			//analysing page
			
//			int start = Integer.parseInt(cMap.get("addrstart"));
//			int end = Integer.parseInt(cMap.get("addrend"));
			
			
			
			int triggerCnt = 0;
			
			for(int i = start ; i < end ; i++){
				String addrI = ac.getAddr(addr, i);
				BufferedReader br = ac.crawler(addrI);
//				System.out.println(addrI);
				HashMap<String, String> map = ac.getContents(br, addrI, oid, i);
				
//				System.out.println(map.size());
				
				if(map.size() > 0 && map.get("exist").equals("1")){
					String keywords = map.get("keyList");
					
					System.out.println("keywords"+keywords);
					String[] keyList = keywords.split("\t");
					
					int len = keyList.length;
					String[] strList = new String[len];
					
					boolean isFood = false;
					String foodName = new String();
					for(int j = 0; j < len ;j++){
						strList[j] = keyList[j].split(":")[0];
						if(fMap.containsKey(strList[j])){
							isFood = true;
							foodName = strList[j];
						}
					}
					
					if(isFood){
						String id = ac.DBInputKeyword(map);
						
						if(!id.equals("-1")){
							NewsClassifier nc = new NewsClassifier();
							DocumentDEF inputDoc = new DocumentDEF();
							
	//						map.put("date", doc.date);
	//						map.put("title", doc.title);
	//						map.put("contents", doc.contents);
	//						map.put("bagList", doc.bagList);
	//						map.put("food", doc.food);
							
							inputDoc.title = map.get("title");
							inputDoc.date = map.get("date");
							inputDoc.contents = map.get("contents");
							inputDoc.food = foodName;
							inputDoc.bagList = map.get("keyList");
							inputDoc.keywords = map.get("keyList").split("\t");
							
							if(nc.getClass(inputDoc).equals("yes")){
								//위해 클래스 뉴스
								InsertCautionData icd = new InsertCautionData();
								icd.insertCautionNews(inputDoc, id);
							}
						}
	//					System.out.println("list"+inputDoc.keywords[0]);
	//					System.out.println("위해후보정보:\t"+nc.getClass(inputDoc));  
					}
				}
				
				
			    //ac.saveHtml(br, "/Users/ankus/Documents/workspace2/ankus crawler/html_001/", sid1+"_"+oid+"_"+new String().valueOf(i));
			    //ac.removeHTML("/Users/ankus/Documents/workspace2/ankus crawler/html/",sid1+"_"+oid+"_"+ new String().valueOf(i));
			}
			ac.setConfFile(cMap);
		}
		System.out.println("Process Time:\t"+(System.currentTimeMillis()-curTime));
	}
	
	private Connection conn;
	
	public String saveFile(StringBuffer buf, String oid, String date, int idx){
		FileWriter fw;
		BufferedWriter bw;
		
		date = date.split(" ")[0];
		File f = new File("news/"+oid+"/"+date+"/");
		System.out.println(f.getAbsolutePath());
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
			
			HDFSDriver hd = new HDFSDriver();
			String[] args = new String[1];
			args[0] = path;
			try {
				hd.run(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return f.getAbsolutePath()+"/"+idx+".html";
	}
	
	public void DBInput(HashMap<String, String> map){
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
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String DBInputKeyword(HashMap<String, String> map){
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
			
//		resMap.put("title", at.text());
//		resMap.put("provider", newsProvider);
//		resMap.put("link", link);
//		resMap.put("date", t11.text());
//		resMap.put("content", abc.text());
//		resMap.put("exist", "1");
//		
		
		String bagList[] = map.get("keyList").split("\t");
		String id = null;
		int count = -1;
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from food_news where title = '"+map.get("title")+"'");
			
			rs.next();
			
			count = rs.getInt(1);
			System.out.println("중복카운트:\t"+count);
			if(count == 0){
				PreparedStatement pstmt = conn.prepareStatement("INSERT INTO food_news([ref], rdate, url, title, contents, path) VALUES (?,?,?,?,?,?)");
				
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
				pstmt.executeUpdate();
				
				pstmt.close();
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery("select last_insert_id()");
				rs.next();
				id = rs.getString(1);
				
				for(int i = 0; i < bagList.length; i++){
					pstmt = conn.prepareStatement("Insert into food_word(nid, keyword, cnt) values(?,?,?)");
					pstmt.setString(2, bagList[i].split(":")[0]);
					pstmt.setString(3, bagList[i].split(":")[1]);
					pstmt.setString(1, id);
					pstmt.executeUpdate();
					pstmt.close();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
//				System.out.println(line);
				map.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return map;
	}
	public HashMap<String, String> getContents(BufferedReader br, String link, String oid, int idx){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//			System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Document doc = Jsoup.parse(sb.toString());
		
		String newsProvider = null;
		NLP nlp = new NLP();
		if( doc.getElementsByClass("t11").size() > 0 ){
			Element t11 = doc.getElementsByClass("t11").get(0);
//					System.out.println(t11.toString());
//			t11 = t11.get(0);
			Element abc = doc.getElementById("articleBodyContents");
			
			Element at = doc.getElementById("articleTitle");
			
			ArrayList<String> list = getKeyword("keyword.txt");
//			ArrayList<String> rmList = getKeyword("removeKeyword.txt");
			
			//?��비스 ?��?��?�� 불편?�� ?��?��?�� ???��?�� 죄송?��?��?��. 
			
			if (oid.equals("056")){
				newsProvider = "KBS";
			} else if (oid.equals("055")){
				newsProvider = "SBS";
			} else if (oid.equals("032")){
				newsProvider = "경향신문";
			} else if (oid.equals("025")){
				newsProvider = "중앙일보";
			} else if (oid.equals("052")){
				newsProvider = "YTN";
			} 
			
			
			boolean trigger = false;
//			for(int j = 0; j < size ; j++){

				
				if(!trigger){
					resMap.put("title", at.text().replace("'", ""));
					resMap.put("provider", newsProvider);
					resMap.put("link", link);
					resMap.put("date", t11.text());
					resMap.put("content", abc.text().replace("'", ""));
					resMap.put("keyList", nlp.extractBag(abc.text()));
					resMap.put("exist", "1");
					resMap.put("filelink", saveFile(sb, newsProvider, t11.text(), idx));
					
////					System.out.println(link);
////				System.out.println(list.get(j));
					System.out.println("제공자: "+newsProvider);
					System.out.println("링크: "+link);
//							System.out.println("???��?��?��: "+path+fList[i].getName());
					System.out.println("기사 작성일: "+t11.text());
					System.out.println("기사 제목: "+at.text());
					System.out.println("기사 본문: "+abc.text());
					System.out.println("기사 경로: "+resMap.get("filelink"));
					
				} else {
					resMap.put("exist", "0");
				}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				System.out.println("pre: "+line);
				line = spCharRid(line);
				System.out.println("aft: "+line);
				sb.append(line);
				sb.append("\r\n");
			}
			
			 Document doc = Jsoup.parse(sb.toString());
//			 Document doc = Jsoup.parse(html);

			 System.out.println(doc.text());
//			 System.out.println(doc.textNodes());
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if(pageNum % 100 == 0)
			System.out.println(addr);
		return addr;
	}
	public String spCharRid(String strInput){
//		  System.out.println("@spCharRid original: "+ strInput);
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
//		    System.out.println("@for @proceed : "+i);
//		    System.out.println("@spCharRid @target is : " + spChars[i]);
		    strWork = strWork.replace(spChars[i], "");
//		    System.out.println("@spCharRid @replaceAll: "+ strWork);
		   }

//		  System.out.println("@spCharRid output  : "+ strWork);
		  return strWork;
		 }
	public BufferedReader crawler(String addr){
		HttpPost http = new HttpPost(addr);

	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹�? �깮�꽦
	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣?���? Response 媛앹껜��? �떞�쓬
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
			// 5. Response 諛쏆�? �뜲�씠�꽣 以�, DOM �뜲�씠�꽣?���? 媛��졇�� Entity�뿉 �떞�쓬
		    HttpEntity entity = response.getEntity();
		    
		    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 ?�⑦?��?�� ���엯�쓣 媛��졇�� �떞?�� Charset�쓣 媛��졇�샂 
		    ContentType contentType = ContentType.getOrDefault(entity);
//		    System.out.println(entity.toString());
	        Charset charset = contentType.getCharset();
//	        System.out.println(charset.name());
	        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?)
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void originalBackup(){
//		// 2. 媛��졇�삱 HTTP 二쇱?�� �꽭�똿
//	    HttpPost http = new HttpPost("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more");
//
//	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹�? �깮�꽦
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    
//	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣?���? Response 媛앹껜��? �떞�쓬
//	    HttpResponse response = httpClient.execute(http);
//	    
//	    // 5. Response 諛쏆�? �뜲�씠�꽣 以�, DOM �뜲�씠�꽣?���? 媛��졇�� Entity�뿉 �떞�쓬
//	    HttpEntity entity = response.getEntity();
//	    
//	    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 ?�⑦?��?�� ���엯�쓣 媛��졇�� �떞?�� Charset�쓣 媛��졇�샂 
//	    ContentType contentType = ContentType.getOrDefault(entity);
//        Charset charset = contentType.getCharset();
//        
//        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?) 
//	    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
//	    
//	    // 8. 媛��졇�삩 DOM �뜲�씠�꽣?���? �떞湲곗?���븳 洹몃�?
//	    StringBuffer sb = new StringBuffer();
//	    
//	    // 9. DOM �뜲�씠�꽣 媛��졇�삤湲�
//	    String line = "";
//	    while((line=br.readLine()) != null){
//	    	sb.append(line+"\n");
//	    }
//	    
//	    // 10. 媛��졇�삩 �븘?��꾨떎�슫 DOM�쓣 蹂댁?��
//	    System.out.println(sb.toString());
//	    
//	    // 11. Jsoup�쑝濡� �뙆�떛�빐蹂댁?��.
//	    Document doc = Jsoup.parse(sb.toString());
//	    
//	    // 李멸?? - Jsoup�뿉�꽌 �젣?�듯�?�뒗 Connect 泥섎?��
//	    Document doc2 = Jsoup.connect("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more").get();
////	    System.out.println(doc2.data());
	}
}