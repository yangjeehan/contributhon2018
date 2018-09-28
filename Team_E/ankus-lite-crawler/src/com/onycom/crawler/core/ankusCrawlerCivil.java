package com.onycom.crawler.core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.ArrayList;
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
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.HDFSDriver;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.TagRemover;

public class ankusCrawlerCivil {
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
		PreparedStatement stmt;
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
			String sql = "select count(*) from civildata where title = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, map.get("title"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			int count = rs.getInt(1);
			if(count == 0){
				
			
				PreparedStatement pstmt = conn.prepareStatement("INSERT INTO civildata(gendate, orilink, title, contents, keywords, srch_kwrd, rtype) VALUES (?,?,?,?,?,?,?)");
				
				String[] tList = nlp.extractNoun(map.get("title")).split(",");
				boolean isFood = false;
				String food = new String();
				for(int i = 0; i < tList.length ; i++){
					if(nlp.dicMap.containsKey(tList[i])){
						isFood = true;
						food = tList[i];
					}
				}
				if(isFood){
					pstmt.setString(1, map.get("date"));
					pstmt.setString(2, map.get("link"));
					pstmt.setString(3, map.get("title"));
					pstmt.setString(4, map.get("content"));
					pstmt.setString(5, nlp.extractNoun(map.get("content"),1));
					pstmt.setString(6, food);
					pstmt.setString(7, "관심");
					pstmt.executeUpdate();
					
					pstmt.close();
				}
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}	
	}
	NLP nlp;
	public void civilCrawler(){
		
		
		nlp = new NLP();
		
		//sid1 = ?��꾩빞, sid2 = �꽭?���遺꾩빞, oid = �돱�뒪�젣?�듭�?, aid = ?��몄꽌踰덊?��(10�옄?���?)
//		sid1= ?��꾩빞 101(寃쎌?��), 102(�궗�쉶), 103(�깮�솢/?��명솕)
//		055:SBS
//		056:KBS
//		214:MBC
		String sid1 = "101";
		String oid = "001";
		String addr = "http://www.kca.go.kr/brd/m_4/view.do?seq=????&srchFr=&srchTo=&srchWord=&srchTp=&itm_seq_1=0&itm_seq_2=0&multi_itm_seq=0&company_cd=&company_nm=&page=1";
		
		//KBS
//		http://news.naver.com/main/read.nhn?mode=LPOD&mid=sec&oid=056&aid=0010363277
		
		//9999999999
		//0000000098
		int si = 1;
		FileReader fr;
		
		try {
			fr = new FileReader("civil.con");
			BufferedReader conBr = new BufferedReader(fr);
			
			conBr.close();
			fr.close();
			si = Integer.parseInt(conBr.readLine());
		} catch (FileNotFoundException e2) {
			System.out.println("오류가 발생했습니다."); 
		} catch (NumberFormatException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		
		int noneCnt = 0;
		for(int i = si ; ; i++){
			String addrI = getAddr(addr, i);
			BufferedReader br = crawler(addrI);
			
			StringBuffer sb = new StringBuffer();
			 
			String line = null;
			ArrayList<String> prtList = new ArrayList<String>();
			try {
				while((line = br.readLine())!=null){
					sb.append(line+"\r\n");
					prtList.add(line);
				}
			} catch (IOException e1) {
				System.out.println("오류가 발생했습니다."); 
				
			}
			
			CSVFileWriter fw = new CSVFileWriter();
			fw.outputFile(prtList, "civil/"+i+".html");

		    HDFSDriver hd = new HDFSDriver();
			String[] args = new String[1];
			
			args[0] = "civil/"+i+".html";
			try {
				hd.run(args);
			} catch (Exception e) {
				System.out.println("오류가 발생했습니다."); 
				
			}
			new File("civil/"+i+".html").delete();
			
			Document doc = Jsoup.parse(sb.toString());
			Element e = doc.getElementById("contents");
			
			
			Elements eList = doc.getAllElements();
			
			HashMap<String, String> resMap = new HashMap<String, String>();
			
			if(!e.text().contains("홈페이지 개편 안내")){
				
				String title = e.text().split("제목")[1].split("작성일")[0];
				
				
				String date = e.text().split("작성일")[1].split("조회수")[0].replaceAll(" ", "");
				
				
				TagRemover tr = new TagRemover();
				if(e.text().split(title).length > 2){
					
					String content = e.text().split(title)[2];
					resMap.put("title", tr.removeHTMLTag(title));
					resMap.put("date", date);
					resMap.put("content", tr.removeHTMLTag(content));
					resMap.put("link", addrI);
					
					DBInput(resMap);
				}
				noneCnt = 0;
			} else {
				noneCnt++;
			}
			
			if(noneCnt > 20){
				try {
					FileWriter conFw = new FileWriter("civil.con");
					BufferedWriter conBw = new BufferedWriter(conFw);
					
					conBw.append(new String().valueOf((i-noneCnt)));
					conBw.flush();
					conFw.flush();
					conBw.close();
					conFw.close();
				} catch (IOException e1) {
					System.out.println("오류가 발생했습니다."); 
					
				}
				
				break;
			}
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
				line = spCharRid(line);
				sb.append(line);
				sb.append("\r\n");
			}
			
			 Document doc = Jsoup.parse(sb.toString());
//			 Document doc = Jsoup.parse(html);

			bw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
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
		    strWork = strWork.replace(spChars[i], "");
		   }

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
	        Charset charset = contentType.getCharset();
	        // 7. DOM �뜲�씠�꽣?���? �븳 以꾩�? �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖?��?��?���?)
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
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
		    
		    HDFSDriver hd = new HDFSDriver();
			String[] args = new String[1];
			args[0] = targetPath +"/" + fileName+".html";
			try {
				hd.run(args);
			} catch (Exception e) {
				System.out.println("오류가 발생했습니다."); 
				
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	

}
