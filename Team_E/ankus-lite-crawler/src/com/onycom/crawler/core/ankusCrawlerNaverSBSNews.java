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

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.common.conf;

///Users/ankus/Documents
public class ankusCrawlerNaverSBSNews {
	
	public static String getCurrentData(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(new Date());
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
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		
		// 1. 媛��졇�삤湲곗?�� �떆媛� 李띻�?
//		System.out.println(" Start Date : " + getCurrentData());
		
		conf cf = new conf();
		HashMap<String, String> cMap = new HashMap<String, String>();
		cMap = cf.confFileReader("news");
		
		ankusCrawlerNaverSBSNews ac = new ankusCrawlerNaverSBSNews();
				 
		//sid1 = ?��꾩빞, sid2 = �꽭?���遺꾩빞, oid = �돱�뒪�젣?�듭�?, aid = ?��몄꽌踰덊?��(10�옄?���?)
//		sid1= ?��꾩빞 101(寃쎌?��), 102(�궗�쉶), 103(�깮�솢/?��명솕)
//		055:SBS
//		056:KBS
//		214:MBC
		String oid = cMap.get("oid");
		String addr = "http://news.naver.com/main/read.nhn?mode=LPOD&mid=shm&oid="+oid+"&aid=????";
		
		String mPath = "/Users/ankus/Documents/html_055/";
		File f = new File(mPath);
		String[] fList = f.list();
		
		System.out.println(fList.length);
		int len = fList.length;
		//KBS
//		http://news.naver.com/main/read.nhn?mode=LPOD&mid=sec&oid=056&aid=0010363277
		
		//9999999999
		//0000000098
		
		//analysing page
		
		int start = Integer.parseInt(cMap.get("addrstart"));
		int end = Integer.parseInt(cMap.get("addrend"));
		
		int triggerCnt = 0;
		
		for(int i = 0 ; i < len ; i++){
//			String addrI = ac.getAddr(addr, i);
			String addrI = mPath + fList[i];
			
//			BufferedReader br = ac.crawler(addrI);
			BufferedReader br = ac.getHtmlFile(addrI);
//			System.out.println(addrI);
			HashMap<String, String> map = ac.getContents(br, addrI, oid);
			
//			System.out.println(map.size());
			
			if(map.size() > 0 && map.get("exist").equals("1")){
				ac.DBInput(map);
				
				
			} else {
				triggerCnt++;
			}
			
//			if(triggerCnt >10){
//				
//				cMap.put("addrend", i-10+"");
//				break;
//			}
		    //ac.saveHtml(br, "/Users/ankus/Documents/workspace2/ankus crawler/html_001/", sid1+"_"+oid+"_"+new String().valueOf(i));
		    //ac.removeHTML("/Users/ankus/Documents/workspace2/ankus crawler/html/",sid1+"_"+oid+"_"+ new String().valueOf(i));
		}
		ac.setConfFile(cMap);
	}
	
	private Connection conn;
	
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
			
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sbsnews(provider, gendate, orilink, title, contents) VALUES (?,?,?,?,?)");
			
//			stmt = conn.createStatement();
//			stmt.execute(
//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
			pstmt.setString(1, map.get("provider"));
			pstmt.setString(2, map.get("date"));
			pstmt.setString(3, map.get("link"));
			pstmt.setString(4, map.get("title"));
			pstmt.setString(5, map.get("content"));
			pstmt.executeUpdate();
			
			pstmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public HashMap<String, String> getContents(BufferedReader br, String link, String oid){
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
		if( doc.getElementsByClass("t11").size() > 0 ){
			Element t11 = doc.getElementsByClass("t11").get(0);
//					System.out.println(t11.toString());
//			t11 = t11.get(0);
			Element abc = doc.getElementById("articleBodyContents");
			
			Element at = doc.getElementById("articleTitle");
			
			ArrayList<String> list = getKeyword("keyword.txt");
			ArrayList<String> rmList = getKeyword("removeKeyword.txt");
			
			//?��비스 ?��?��?�� 불편?�� ?��?��?�� ???��?�� 죄송?��?��?��. 
			
//			if (oid.equals("056")){
//				newsProvider = "KBS";
//			} else if (oid.equals("055")){
//				newsProvider = "SBS";
//			}
			newsProvider = "SBS";
			boolean trigger = false;
//			for(int j = 0; j < size ; j++){

				for(int r = 0; r < rmList.size(); r++){
					if(abc == null || abc.text().contains(rmList.get(r))){
						trigger = true;
						break;
					}
				}
				if(abc != null){
					resMap.put("title", at.text());
					resMap.put("provider", newsProvider);
					resMap.put("link", link);
					resMap.put("date", t11.text());
					resMap.put("content", abc.text());
					resMap.put("exist", "1");
					
////					System.out.println(link);
////				System.out.println(list.get(j));
//					System.out.println("?��공자: "+newsProvider);
//					System.out.println("링크: "+link);
////							System.out.println("???��?��?��: "+path+fList[i].getName());
					System.out.println("기사 ?��?�� ?��?��: "+t11.text());
					System.out.println("기사 ?���?: "+at.text());
//					System.out.println("기사 본문: "+abc.text());					
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
	
	public BufferedReader getHtmlFile(String addr){
		
		
//		HttpPost http = new HttpPost(addr);
//
//	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹�? �깮�꽦
//	    HttpClient httpClient = HttpClientBuilder.create().build();
	  
	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣?���? Response 媛앹껜��? �떞�쓬
	    HttpResponse response = null;
	    
	   
	    BufferedReader retBr = null;
		try {
			FileReader fr = new FileReader(addr);
//			response = httpClient.execute(http);
//			// 5. Response 諛쏆�? �뜲�씠�꽣 以�, DOM �뜲�씠�꽣?���? 媛��졇�� Entity�뿉 �떞�쓬
//		    HttpEntity entity = response.getEntity();
//		    
//		    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 ?�⑦?��?�� ���엯�쓣 媛��졇�� �떞?�� Charset�쓣 媛��졇�샂 
//		    ContentType contentType = ContentType.getOrDefault(entity);
////		    System.out.println(entity.toString());
//	        Charset charset = contentType.getCharset();
////	        System.out.println(charset.name());
//	        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?)
//		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   retBr = new BufferedReader(fr);
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