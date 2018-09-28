package com.onycom.crawler.core;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.common.HDFSDriver;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.YesterDayGenerate;

public class ankusCrawlerNaverBlog2 {
	int monthList[] = {31,28,31,30,31,30,31,31,30,31,30,31};

	public void waitDelay(int max, int min){
		try {
			int random = (int)(Math.random() * (max - min +1)+min);
			System.out.println(random);
			Thread.sleep(random);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getNextDate(String startDate){
		String startList[] = startDate.split("-");
		
		int sYear = Integer.parseInt(startList[0]);
		int sMonth = Integer.parseInt(startList[1]);
		int sDay = Integer.parseInt(startList[2]);
		
		if(sDay == monthList[sMonth-1]){
			sDay = 1;
			sMonth++;
		} else if (sDay < monthList[sMonth-1]){
			sDay++;
		}
		
		if(sMonth == 13) {
			sMonth = 1;
			sYear++;
		}
		
		String sMonthStr = new String();
		String sDayStr = new String();
		
		
		if(sMonth < 10){
			sMonthStr = "0"+sMonth;
		} else {
			sMonthStr = new String().valueOf(sMonth);
		}
		if(sDay < 10){
			sDayStr = "0"+sDay;
		} else {
			sDayStr = new String().valueOf(sDay);
		}
		
		return sYear+"-"+sMonthStr+"-"+sDayStr;
	}
	
	public String mDate;
	
	public boolean hasNext(String startDate, String endDate){
		String startList[] = startDate.split("-");
		String endList[] = endDate.split("-");
		
		int sYear = Integer.parseInt(startList[0]);
		int sMonth = Integer.parseInt(startList[1]);
		int sDay = Integer.parseInt(startList[2]);
		
		int eYear = Integer.parseInt(endList[0]);
		int eMonth = Integer.parseInt(endList[1]);
		int eDay = Integer.parseInt(endList[2]);
		
		boolean isOk = false;

		
		if(sYear < eYear){
			isOk = true;
		}	
		if(sYear == eYear && sMonth < eMonth){
			isOk = true;
		}
		if(sYear == eYear && sMonth == eMonth && sDay <= eDay){
			isOk = true;
		}
		
		return isOk;
	}
	
	public void blogCralwer(){
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(25);
		String yd2 = yd.getYesterDay(54);
		
		String startDate = yd2;
		String endDate = yd1;
		
		ankusCrawlerNaverBlog2 acn = new ankusCrawlerNaverBlog2();
		
		while(acn.hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			System.out.println(startDate);
			acn.mDate = startDate;
			acn.core(startDate, startDate);
			
			startDate = acn.getNextDate(startDate);
			
			acn.waitDelay(4000,2000);
		}		
	}
	
	public void core(String startDate, String endDate){
		//"%EB%B0%B1%EC%88%98%EC%98%A4&term=period&option.startDate=2015-04-23&option.endDate=2015-04-23&option.page.currentPage=1&option.orderBy=sim";
		
		String keywordPath = "BlogKeyword.txt";
				
		String fileMPath = "blog/";
		String blogSubString ="http://blog.naver.com/";
		
		ankusCrawlerNaverBlog2 acn = new ankusCrawlerNaverBlog2();
		
		ArrayList<String> keywordList = acn.getKeywordListUnicode(keywordPath);
		ArrayList<String> keywordListUTF = acn.getKeywordList(keywordPath);
		String searchAddr = "http://section.blog.naver.com/sub/SearchBlog.nhn?type=post&option.keyword=";
		String startAddr = "&term=period&option.startDate=";
		String endAddr = "&option.endDate=";
		String pageAddr = "&option.page.currentPage=";
		
		int page = 1;
		
		String searchTypeAddr = "&option.orderBy=sim";
		String fullPath;
		
		for(int i = 0; i < keywordList.size() ; i++){
			int totalDocSize = 0;
			
			fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+page+searchTypeAddr;
			BufferedReader br = acn.crawler(fullPath);
			
			totalDocSize = acn.getSearchTotalCount(br, fullPath);	
			
			if(totalDocSize < 10){
				page = 2;
			}
			if(totalDocSize%10 > 0){
				page = totalDocSize/10 + 2;
			} else {
				page = totalDocSize/10 + 1;
			}
			HashMap<String, String> subAddrMap = new HashMap<String, String>();
			 
			for(int j = 1; j < page; j++){
				fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+j+searchTypeAddr;
				br = acn.crawler(fullPath);
				
				acn.getContents(br, subAddrMap, fullPath, keywordListUTF.get(i));
			}
			
			Object[] subAddrList = subAddrMap.keySet().toArray();
			
			//rdirect Page crawling
			for(int j = 0; j < subAddrList.length; j++){
				acn.waitDelay(2000,1500);
				
				br = acn.crawler((String)subAddrList[j]);
				
				String postAddr = acn.getPostViewInfo(br);
				
				//original page
				HashMap<String, String> oriMap = new HashMap<String, String>();
				
				br = acn.crawler(blogSubString+postAddr);
				StringBuffer postBuf = acn.getPostOrigianalPage(br);
				oriMap = acn.getPostOrigianalPageInfo(postBuf);
				oriMap.put("searchkeyword",keywordListUTF.get(i));
				oriMap.put("date", startDate);
				
				if(!oriMap.containsKey("exist")){
				
					oriMap.put("httpLink", (String)subAddrList[j]);
					String provider = ((String)subAddrList[j]).replace("http://blog.naver.com/", "").replace("?", ",").split(",")[0];
					
					oriMap.put("provider", provider);
					
					oriMap.put("fileLink", acn.htmlFileWriter(fileMPath, "/"+keywordListUTF.get(i)+"/"+startDate+"/"+j+".html",postBuf));
					acn.DBInput(oriMap);
				}
			}
		}
	}
	private String convertEncoring(String val){
		// String 을 euc-kr 로 인코딩.
		System.out.println(val);
		byte[] euckrStringBuffer = val.getBytes(Charset.forName("euc-kr"));

		String decodedFromEucKr;
		String decodedFromUtf8 = null;
		try {
			decodedFromEucKr = new String(euckrStringBuffer, "euc-kr");

			byte[] utf8StringBuffer = decodedFromEucKr.getBytes("utf-8");
			decodedFromUtf8 = new String(utf8StringBuffer, "utf-8");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decodedFromUtf8;
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
			
			HDFSDriver hd = new HDFSDriver();
			String[] args = new String[1];
			args[0] = mPath+id;
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
		return f.getAbsolutePath();
	}
	private Connection conn;
	
	public void DBInput(HashMap<String, String> map){
		System.out.println("httpLink:\t"+map.get("httpLink"));
		System.out.println("fileLink:\t"+map.get("fileLink"));
		System.out.println("provider:\t"+map.get("provider"));
		System.out.println("date:\t"+map.get("date"));
		System.out.println("title:\t"+map.get("title"));
		System.out.println("content:\t"+map.get("content"));
		
		DBConnect db = new DBConnect();
		
		/**
		 * CREATE TABLE blogdata(
			rno bigint AUTO_INCREMENT(1,1),
			provider character varying(10),
			gendate timestamp,
			title character varying(500),
			contents character varying(4096),
			keywords character varying(4096),
			httplink character varying(500),
			filelink character varying(500) COLLATE utf8_bin 
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
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from blog_info where doc_sj = '"+map.get("title")+"'");
			
			rs.next();
			
			int count = rs.getInt(1);
			System.out.println("중복카운트:\t"+count);
			if(count == 0){
				String sql = "INSERT INTO blog_info(blog_wrter, doc_cret_dt, doc_sj, doc_cn, http_addr, file_stre_addr, kwrd, srch_kwrd) VALUES (?,?,?,?,?,?,?,?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				
	//			stmt = conn.createStatement();
	//			stmt.execute(
	//				    "INSERT INTO newsdata (provider, gendate, orilink, title, contents) " +
	//				    "VALUES ('"+map.get("provider")+"', '"+map.get("date")+"', '"+map.get("link")+"', '"+map.get("title")+"', '"+map.get("content")+"')");
				
				pstmt.setString(1, map.get("provider"));
				pstmt.setString(2, map.get("date"));
				pstmt.setString(3, map.get("title"));
				pstmt.setString(4, map.get("content"));
				pstmt.setString(5, map.get("httpLink"));
				pstmt.setString(6, map.get("fileLink"));
				pstmt.setString(7,map.get("keywords"));
				pstmt.setString(8, map.get("searchkeyword"));
							
				pstmt.executeUpdate();
				
				pstmt.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public StringBuffer getPostOrigianalPage(BufferedReader br){
		StringBuffer sb = new StringBuffer();		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb;
	}
	public HashMap<String, String> getPostOrigianalPageInfo(StringBuffer sb){
		HashMap<String, String> resMap = new HashMap<String, String>();
		
//		System.out.println(sb.toString());
		//se_publishDate pcol2 fil5
		Document doc = Jsoup.parse(sb.toString());
		Elements eList = doc.getAllElements();
		
		Elements eConTents1 = doc.getElementsByClass("se_textarea");
		Elements eDateList = doc.getElementsByClass("se_publishDate");
		
		//se_doc_title_top
		
		//�뀘�뀋 postViewArea
		Elements eTitleList = doc.getElementsByTag("title");
		
		Element eConTents2 = doc.getElementById("postViewArea");	
		
		
		
		if(eConTents1.text().length() < 1){
			resMap.put("title", eTitleList.text());
			
			if(eConTents2 != null){ 
				resMap.put("content", eConTents2.text());
			} else {
				resMap.put("exist", "");
			}
	
		} else{
			resMap.put("title", eTitleList.text());

			if(eConTents2 != null){ 
				resMap.put("content", eConTents2.text());
				NLP nlp = new NLP();
				resMap.put("Keywords", nlp.extractNoun(eConTents2.text()));
			} else {
				resMap.put("exist", "");
			}
		}
		return resMap;
	}

	public String getPostViewInfo(BufferedReader br){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//				System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Document doc = Jsoup.parse(sb.toString());		
//		Elements eTAList = 
		String subString = doc.getElementsByTag("frame").attr("src");
//		System.out.println("postViewLink:\t"+doc.getElementsByTag("frame").attr("src"));
		
		return subString;
	}
	
	public int getSearchTotalCount(BufferedReader br, String link){
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
		Elements eTAList = doc.getElementsByClass("several_post");
		Elements eDateList = doc.getElementsByTag("a");
		
		int size = eDateList.toggleClass("href").size();
		
		
		int retVal = 0;
		if(eTAList.text().contains("건")){
			retVal = Integer.parseInt(eTAList.text().split(" ")[eTAList.text().split(" ").length-1].replace("건 ", ""));
		}
		
		System.out.println("SearchResult Count:\t"+retVal);
		return retVal;
	}
	
	public HashMap<String, String> getContents(BufferedReader br, HashMap<String, String> resMap, String link, String keyword){
		StringBuffer sb = new StringBuffer();
				 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document doc = Jsoup.parse(sb.toString());		
		Elements eTAList = doc.getElementsByClass("several_post");
		Elements eDateList = doc.getElementsByTag("a");
		
		int size = eDateList.toggleClass("href").size();
		
		int cnt = 0;
		for(int i = 0; i < size ; i++){
			if(eDateList.toggleClass("href").get(i).attr("href").contains("Redirect")){
				
				String title= new String();
				
				if(cnt % 3 == 0){
					title = eDateList.toggleClass("href").get(i).text();
					
				}
				
				if(title.contains("블로그 이용 팁")){
					break;
				}
				
				cnt++;
//				if(title.contains(keyword)){
					resMap.put(eDateList.toggleClass("href").get(i).attr("href"), "1");
//				}
			}
		}	
		return resMap;
	}
	
	public BufferedReader crawler(String addr){
		HttpPost http = new HttpPost(addr);

	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹껜 �깮�꽦
	    HttpClient httpClient = HttpClientBuilder.create().build();
	    
	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣瑜� Response 媛앹껜�뿉 �떞�쓬
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
		try {
			response = httpClient.execute(http);
			// 5. Response 諛쏆� �뜲�씠�꽣 以�, DOM �뜲�씠�꽣瑜� 媛��졇�� Entity�뿉 �떞�쓬
		    HttpEntity entity = response.getEntity();
		    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 而⑦뀗�듃 ���엯�쓣 媛��졇�� �떞怨� Charset�쓣 媛��졇�샂 
		    ContentType contentType = ContentType.getOrDefault(entity);
	        Charset charset = contentType.getCharset();
	        
	        // 7. DOM �뜲�씠�꽣瑜� �븳 以꾩뵫 �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖씤痍⑦뼢) 
		    retBr = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
		   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return retBr;
	}
	
	public ArrayList<String> getKeywordListUnicode(String path){
		ArrayList<String> list = new ArrayList<String>();
		
		FileReader fr;
		BufferedReader br;
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			String line;
			
			while((line = br.readLine())!=null){
				System.out.println(line+"\t"+URLEncoder.encode(line));
				list.add(URLEncoder.encode(line));
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return list;
	}
	public ArrayList<String> getKeywordList(String path){
		ArrayList<String> list = new ArrayList<String>();
		
		FileReader fr;
		BufferedReader br;
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			String line;
			
			while((line = br.readLine())!=null){
				list.add(line);
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return list;
	}
}
