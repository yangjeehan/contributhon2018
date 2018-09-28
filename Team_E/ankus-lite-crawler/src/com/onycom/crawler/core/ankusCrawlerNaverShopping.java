package com.onycom.crawler.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

/**
 * ?��?���? ?��?��몰에?�� �??��?�� ?�� ?��매개?�� ?��롤링
 * �??��???��?�� �?
 * ?��롤링 ?���?, ?��?��?? ?��매개?�� 
 * 
 * @author ankus
 *
 */
public class ankusCrawlerNaverShopping {
	
	Connection conn;
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
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO shoppingdata(product, amount, [date]) VALUES (?,?, now())");
			
//			stmt = conn.createStatement();
//			stmt.execute(
//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
			pstmt.setString(1, map.get("product"));
			pstmt.setInt(2, Integer.parseInt(map.get("amount").replace(",", "")));
//			pstmt.setString(3, map.get("link"));
//			pstmt.setString(4, map.get("title"));
//			pstmt.setString(5, map.get("content"));
			pstmt.executeUpdate();
			
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
	}
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
	}
	public String encode(String unicode){
	      StringBuffer str = new StringBuffer();
	      
	      for (int i = 0; i < unicode.length(); i++) {
	       if(((int) unicode.charAt(i) == 32)) {
	        str.append(" ");
	        continue;
	       }
	       str.append("\\u");
	       str.append(Integer.toHexString((int) unicode.charAt(i)));
	       
	      }
	      
	      return str.toString();

	 }

	public ArrayList<String> getSearchKeywordList(){
		FileReader fr;
		BufferedReader br;
		
		String path = "dic.txt";
		
		String line = null;
		
		ArrayList<String> list = new ArrayList<String>();
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				list.add(line);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return list;
	}
	
	public void shoppingCralwer(){
		
		// 1. �??��?��기전 ?���? 찍기
		long stime = System.currentTimeMillis();
		//System.out.println(" Start Date : " + getCurrentData());
		
		ankusCrawlerNaverShopping ac = new ankusCrawlerNaverShopping();
		
		String addr = "http://shopping.naver.com/search/all.nhn?cat_id=50000006&query=";
		
		String addr2 = "http://shopping.naver.com/search/all.nhn?cat_id=50000006&query=";
		
		ArrayList<String> list = ac.getSearchKeywordList();
		try {
			for(int i = 0; i < list.size(); i++){
				HashMap<String, String> map = new HashMap<String, String>();
//				//System.out.println(list.get(i));
//				
//					//System.out.println(ac.getAddr(addr, list.get(i)));
				
				
				BufferedReader br = ac.crawler(ac.getAddr(addr, list.get(i)));
				String productCnt = ac.getProductCount(br);
				
//				//System.out.println(productCnt);
				map.put("product", list.get(i));
				map.put("amount", productCnt);
				ac.DBInput(map);
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	
		//System.out.println(System.currentTimeMillis() - stime);
		// ???��
		
//		for(int i = 7683100 ; i < 8703421 ; i++){
//			String addrI = ac.getAddr(addr, i);
//			BufferedReader br = ac.crawler(addrI);
//		    ac.saveHtml(br, "/Users/ankus/Documents/workspace2/ankus crawler/html_001/", sid1+"_"+oid+"_"+new String().valueOf(i));
//		    //ac.removeHTML("/Users/ankus/Documents/workspace2/ankus crawler/html/",sid1+"_"+oid+"_"+ new String().valueOf(i));
//		}
  
//	    // 12. ?��마나 걸렸?�� 찍어보자
//	    //System.out.println(" End Date : " + getCurrentData());
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
				//System.out.println("pre: "+line);
				line = spCharRid(line);
				//System.out.println("aft: "+line);
				sb.append(line);
				sb.append("\r\n");
			}
			
			 Document doc = Jsoup.parse(sb.toString());
//			 Document doc = Jsoup.parse(html);

//			 //System.out.println(doc.text());
//			 //System.out.println(doc.textNodes());
			br.close();
			fr.close();
			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
	}
	
	public String getAddr(String addr, String productName) throws UnsupportedEncodingException{
		return addr + URLEncoder.encode(productName).toString();
	}
//	%EB%B0%B1%EC%88%98%EC%98%A4
//	%EB%B0%B1%EC%88%98%EC%98%A4
	//http://shopping.naver.com/search/all.nhn?query=%EB%B0%B1%EC%88%98%EC%98%A4&cat_id=&frm=NVSHATC//
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
		HttpPost http = new HttpPost(addr);

	    // 3. �??��?��기�?? ?��?��?�� ?��?��?��?��?�� 객체 ?��?��
	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    // 4. ?��?�� �? ?��?�� ?��?��?���? Response 객체?�� ?��?��
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
			// 5. Response 받�? ?��?��?�� �?, DOM ?��?��?���? �??��?? Entity?�� ?��?��
//			//System.out.println(response.);
		    HttpEntity entity = response.getEntity();
		    
		    // 6. Charset?�� ?��?��?���? ?��?�� DOM?�� 컨텐?�� ???��?�� �??��?? ?���? Charset?�� �??��?�� 
		    ContentType contentType = ContentType.getOrDefault(entity);
//		    //System.out.println(entity.toString());
	        Charset charset = contentType.getCharset();
//	        //System.out.println(charset.name());
	        // 7. DOM ?��?��?���? ?�� 줄씩 ?���? ?��?�� Reader?�� ?��?�� (InputStream / Buffered �? ?��?��?? 개인취향)
//	        //System.out.println(entity.getContent().toString());
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		    
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	    
	    return retBr;
	}
	
	public String getProductCount(BufferedReader br){
		String line = null;
		StringBuffer sb = new StringBuffer();
		
		
		try {
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		Document doc = Jsoup.parse(sb.toString());
		Elements abc = doc.getAllElements();
		
		
//		for(int i = 0; i < abc.size(); i++){
//			//System.out.println(i+"\t"+abc.get(i).toString());
//		}
//		//System.out.println(doc.tagName("em").toString());
		
		String retVal = abc.get(abc.size()-1).data().split(">")[2].split("'")[0];
		
//		//System.out.println(abc.get(abc.size()-1).data());
//		//System.out.println();
		
		return retVal;
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
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	
	public void originalBackup(){
//		// 2. �??��?�� HTTP 주소 ?��?��
//	    HttpPost http = new HttpPost("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more");
//
//	    // 3. �??��?��기�?? ?��?��?�� ?��?��?��?��?�� 객체 ?��?��
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    
//	    // 4. ?��?�� �? ?��?�� ?��?��?���? Response 객체?�� ?��?��
//	    HttpResponse response = httpClient.execute(http);
//	    
//	    // 5. Response 받�? ?��?��?�� �?, DOM ?��?��?���? �??��?? Entity?�� ?��?��
//	    HttpEntity entity = response.getEntity();
//	    
//	    // 6. Charset?�� ?��?��?���? ?��?�� DOM?�� 컨텐?�� ???��?�� �??��?? ?���? Charset?�� �??��?�� 
//	    ContentType contentType = ContentType.getOrDefault(entity);
//        Charset charset = contentType.getCharset();
//        
//        // 7. DOM ?��?��?���? ?�� 줄씩 ?���? ?��?�� Reader?�� ?��?�� (InputStream / Buffered �? ?��?��?? 개인취향) 
//	    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
//	    
//	    // 8. �??��?�� DOM ?��?��?���? ?��기위?�� 그릇
//	    StringBuffer sb = new StringBuffer();
//	    
//	    // 9. DOM ?��?��?�� �??��?���?
//	    String line = "";
//	    while((line=br.readLine()) != null){
//	    	sb.append(line+"\n");
//	    }
//	    
//	    // 10. �??��?�� ?��름다?�� DOM?�� 보자
//	    //System.out.println(sb.toString());
//	    
//	    // 11. Jsoup?���? ?��?��?��보자.
//	    Document doc = Jsoup.parse(sb.toString());
//	    
//	    // 참고 - Jsoup?��?�� ?��공하?�� Connect 처리
//	    Document doc2 = Jsoup.connect("http://finance.naver.com/item/coinfo.nhn?code=045510&target=finsum_more").get();
////	    //System.out.println(doc2.data());
	}
}