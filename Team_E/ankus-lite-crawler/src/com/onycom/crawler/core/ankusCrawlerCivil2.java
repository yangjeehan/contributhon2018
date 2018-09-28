package com.onycom.crawler.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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
import java.util.Date;
import java.util.HashMap;

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
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.analysis.NewsClassifier;
import com.onycom.crawler.common.NLP;

public class ankusCrawlerCivil2 {
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
	}
	
	Connection conn;
	public void DBInput(HashMap<String, String> map){
		DBConnect db = new DBConnect();
		/**
		 * CREATE TABLE civildata(
rno bigint AUTO_INCREMENT(1,1),
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
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO civildata2(gendate, orilink, title, contents, keywords) VALUES (?,?,?,?,?)");
			
//			stmt = conn.createStatement();
//			stmt.execute(
//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
			pstmt.setString(1, map.get("date"));
			pstmt.setString(2, map.get("link"));
			pstmt.setString(3, map.get("title"));
			pstmt.setString(4, map.get("content"));
			pstmt.setString(5, map.get("keyList"));
			pstmt.executeUpdate();
			
			pstmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws ClientProtocolException, IOException {
		
		// 1. åª›ï¿½ï¿½ì¡‡ï¿½ì‚¤æ¹²ê³—?Ÿ¾ ï¿½ë–†åª›ï¿½ ï§¡ë»ë¦?
		System.out.println(" Start Date : " + getCurrentData());
		
		ankusCrawlerCivil2 ac = new ankusCrawlerCivil2();
		
		
		//sid1 = ?ºê¾©ë¹, sid2 = ï¿½ê½­?ºï¿½éºê¾©ë¹, oid = ï¿½ë±ï¿½ë’ªï¿½ì £?¨ë“­ê¶?, aid = ?‡¾ëª„ê½Œè¸°ëŠ?ƒ‡(10ï¿½ì˜„?”±ï¿?)
//		sid1= ?ºê¾©ë¹ 101(å¯ƒìŒ? £), 102(ï¿½ê¶—ï¿½ì‰¶), 103(ï¿½ê¹®ï¿½ì†¢/?‡¾ëª…ì†•)
//		055:SBS
//		056:KBS
//		214:MBC
		String sid1 = "101";
		String oid = "001";
		String addr = "http://www.ciss.go.kr/www/selectBbsNttView.do?key=70&bbsNo=81&nttNo=????&searchCtgry=&searchCnd=all&searchKrwd=&pageIndex=63&pageUnit=10&integrDeptCode=";
		
		//KBS
//		http://news.naver.com/main/read.nhn?mode=LPOD&mid=sec&oid=056&aid=0010363277
		
		//9999999999
		//0000000098
		int start = 2328;
		int end = 27405;
				
		
		for(int i = start ; i < end ; i++){
			String addrI = ac.getAddr(addr, i);
			System.out.println(addrI);
			BufferedReader br = ac.crawler(addrI);
			
			StringBuffer sb = new StringBuffer();
			 
			String line = null;
			
			while((line = br.readLine())!=null){
//				System.out.println(line);
				sb.append(line+"\r\n");
			}
			
			Document doc = Jsoup.parse(sb.toString());
			Element eTitle = doc.getElementsByTag("td").addClass("subject").first();
			Element eDate = doc.getElementsByTag("td").get(4);
			Element eContents = doc.getElementsByTag("td").get(5);
			
			
			Elements eList = doc.getAllElements();
			
			System.out.println("title:\t"+eTitle.text());
			System.out.println("Date:\t"+eDate.text());
			System.out.println("Contents:\t"+eContents.text());
			
			HashMap<String, String> resMap = new HashMap<String, String>();
			
			resMap.put("title", eTitle.text());
			resMap.put("date", eDate.text());
			resMap.put("content", eContents.text());
			
			NLP nlp = new NLP();
			
			resMap.put("keyList", nlp.extractNoun(eContents.text()));
			resMap.put("link", addrI);
			
			ac.DBInput(resMap);
			
//			for(int j = 0; j < eList.size(); j++){
//				System.out.println("className:\t"+eList.get(j).className());
//				System.out.println(eList.get(j).text());
//			}
			
//			HashMap<String, String> resMap = new HashMap<String, String>();
//			System.out.println(e.text());
//			
//			if(!e.text().contains("?™ˆ?˜?´ì§? ê°œí¸ ?•ˆ?‚´")){
//			
//				String title = e.text().split("? œëª?")[1].split("?‘?„±?¼")[0];
//				
//				
//				
//				System.out.println(title);
//				
//				String date = e.text().split("?‘?„±?¼")[1].split("ì¡°íšŒ?ˆ˜")[0].replaceAll(" ", "");
//				System.out.println(date);
//				
//				
//				
//				if(e.text().split(title).length > 2){
//					
//					String content = e.text().split(title)[2];
//					System.out.println(content);
//					resMap.put("title", title);
//					resMap.put("date", date);
//					resMap.put("content", content);
//					resMap.put("link", addrI);
//					
//					ac.DBInput(resMap);
//					System.out.println("DBINput");
//				}
//			}
//			System.out.println(e.text().split("?‘?„±?¼")[1].split("ì¡°íšŒ?ˆ˜")[1]);
		    //ac.removeHTML("/Users/ankus/Documents/workspace2/ankus crawler/html/",sid1+"_"+oid+"_"+ new String().valueOf(i));
		}
		

//	    
//	    // 12. ï¿½ë¼¹ï§ëˆêµ? å«„ëªƒì¡‡ï¿½êµ? ï§¡ë¿ë¼±è¹‚?Œ?˜„
//	    System.out.println(" End Date : " + getCurrentData());
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

	    // 3. åª›ï¿½ï¿½ì¡‡ï¿½ì‚¤æ¹²ê³•ï¿½ï¿½ ï¿½ë–ï¿½ë»¾ï¿½ë¸· ï¿½ê²¢ï¿½ì”ªï¿½ì” ï¿½ë¼µï¿½ë“ƒ åª›ì•¹ê»? ï¿½ê¹®ï¿½ê½¦
	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    // 4. ï¿½ë–ï¿½ë»¾ è«›ï¿½ ï¿½ë–ï¿½ë»¾ ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? Response åª›ì•¹ê»œï¿½ë¿? ï¿½ë–ï¿½ì“¬
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
			// 5. Response è«›ì†ï¿? ï¿½ëœ²ï¿½ì” ï¿½ê½£ ä»¥ï¿½, DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? åª›ï¿½ï¿½ì¡‡ï¿½ï¿½ Entityï¿½ë¿‰ ï¿½ë–ï¿½ì“¬
		    HttpEntity entity = response.getEntity();
		    
		    // 6. Charsetï¿½ì“£ ï¿½ë¸£ï¿½ë¸˜ï¿½ê¶¡æ¹²ï¿½ ï¿½ìï¿½ë¹ DOMï¿½ì“½ ?Œâ‘¦?—ï¿½?“ƒ ï¿½ï¿½ï¿½ì—¯ï¿½ì“£ åª›ï¿½ï¿½ì¡‡ï¿½ï¿½ ï¿½ë–?¨ï¿½ Charsetï¿½ì“£ åª›ï¿½ï¿½ì¡‡ï¿½ìƒ‚ 
		    ContentType contentType = ContentType.getOrDefault(entity);
//		    System.out.println(entity.toString());
	        Charset charset = contentType.getCharset();
//	        System.out.println(charset.name());
	        // 7. DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? ï¿½ë¸³ ä»¥ê¾©ëµ? ï¿½ì”«æ¹²ï¿½ ï¿½ìï¿½ë¹ Readerï¿½ë¿‰ ï¿½ë–ï¿½ì“¬ (InputStream / Buffered ä»¥ï¿½ ï¿½ê½‘ï¿½ê¹®ï¿½ï¿½ åª›ì’–?”¤?—?‘¦ë¼?)
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
//		// 2. åª›ï¿½ï¿½ì¡‡ï¿½ì‚± HTTP äºŒì‡±?ƒ¼ ï¿½ê½­ï¿½ë˜¿
//	    HttpPost http = new HttpPost("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more");
//
//	    // 3. åª›ï¿½ï¿½ì¡‡ï¿½ì‚¤æ¹²ê³•ï¿½ï¿½ ï¿½ë–ï¿½ë»¾ï¿½ë¸· ï¿½ê²¢ï¿½ì”ªï¿½ì” ï¿½ë¼µï¿½ë“ƒ åª›ì•¹ê»? ï¿½ê¹®ï¿½ê½¦
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    
//	    // 4. ï¿½ë–ï¿½ë»¾ è«›ï¿½ ï¿½ë–ï¿½ë»¾ ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? Response åª›ì•¹ê»œï¿½ë¿? ï¿½ë–ï¿½ì“¬
//	    HttpResponse response = httpClient.execute(http);
//	    
//	    // 5. Response è«›ì†ï¿? ï¿½ëœ²ï¿½ì” ï¿½ê½£ ä»¥ï¿½, DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? åª›ï¿½ï¿½ì¡‡ï¿½ï¿½ Entityï¿½ë¿‰ ï¿½ë–ï¿½ì“¬
//	    HttpEntity entity = response.getEntity();
//	    
//	    // 6. Charsetï¿½ì“£ ï¿½ë¸£ï¿½ë¸˜ï¿½ê¶¡æ¹²ï¿½ ï¿½ìï¿½ë¹ DOMï¿½ì“½ ?Œâ‘¦?—ï¿½?“ƒ ï¿½ï¿½ï¿½ì—¯ï¿½ì“£ åª›ï¿½ï¿½ì¡‡ï¿½ï¿½ ï¿½ë–?¨ï¿½ Charsetï¿½ì“£ åª›ï¿½ï¿½ì¡‡ï¿½ìƒ‚ 
//	    ContentType contentType = ContentType.getOrDefault(entity);
//        Charset charset = contentType.getCharset();
//        
//        // 7. DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? ï¿½ë¸³ ä»¥ê¾©ëµ? ï¿½ì”«æ¹²ï¿½ ï¿½ìï¿½ë¹ Readerï¿½ë¿‰ ï¿½ë–ï¿½ì“¬ (InputStream / Buffered ä»¥ï¿½ ï¿½ê½‘ï¿½ê¹®ï¿½ï¿½ åª›ì’–?”¤?—?‘¦ë¼?) 
//	    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
//	    
//	    // 8. åª›ï¿½ï¿½ì¡‡ï¿½ì‚© DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£?‘œï¿? ï¿½ë–æ¹²ê³—?ï¿½ë¸³ æ´¹ëªƒì«?
//	    StringBuffer sb = new StringBuffer();
//	    
//	    // 9. DOM ï¿½ëœ²ï¿½ì” ï¿½ê½£ åª›ï¿½ï¿½ì¡‡ï¿½ì‚¤æ¹²ï¿½
//	    String line = "";
//	    while((line=br.readLine()) != null){
//	    	sb.append(line+"\n");
//	    }
//	    
//	    // 10. åª›ï¿½ï¿½ì¡‡ï¿½ì‚© ï¿½ë¸˜?”±ê¾¨ë–ï¿½ìŠ« DOMï¿½ì“£ è¹‚ëŒ?˜„
//	    System.out.println(sb.toString());
//	    
//	    // 11. Jsoupï¿½ì‘æ¿¡ï¿½ ï¿½ë™†ï¿½ë–›ï¿½ë¹è¹‚ëŒ?˜„.
//	    Document doc = Jsoup.parse(sb.toString());
//	    
//	    // ï§¡ë©¸?? - Jsoupï¿½ë¿‰ï¿½ê½Œ ï¿½ì £?¨ë“¯ë¸?ï¿½ë’— Connect ï§£ì„?”
//	    Document doc2 = Jsoup.connect("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more").get();
////	    System.out.println(doc2.data());
	}
}
