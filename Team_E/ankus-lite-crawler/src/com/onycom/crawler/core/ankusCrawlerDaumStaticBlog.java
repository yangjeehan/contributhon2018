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
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.common.*;
import com.onycom.crawler.common.Dictionary;

public class ankusCrawlerDaumStaticBlog {
	int monthList[] = {31,28,31,30,31,30,31,31,30,31,30,31};
	public String getBRTagText(String text, int mode){
		String retVal = new String();
		TagRemover tr = new TagRemover();
		text = text.replace("<br>", "\r\n");
		text = text.replace("&nbsp;", " ");
		
		retVal = tr.removeHTMLTag(text);
//		//System.out.println(text);
//		Document doc = Jsoup.parse(text);
//		Element element;
//		
//		retVal = doc.text();
//		if(mode == 1){
//			retVal = doc.getElementById("se_textarea").text();
//		} else {
//			retVal = doc.text();
//		}
		
		
		return retVal;
	}

	public void printBlog(HashMap<String, String> map){
		//System.out.println(map.get("searchkeyword"));
		//System.out.println("httpLink:\t"+map.get("httpLink"));
		//System.out.println("fileLink:\t"+map.get("fileLink"));
		//System.out.println("provider:\t"+map.get("provider"));
		//System.out.println("date:\t"+map.get("date"));
		//System.out.println("title:\t"+map.get("title"));
		//System.out.println("content:\t"+map.get("content"));
		//System.out.println("print:\t"+map.get("Keywords"));
	}
	public ArrayList<String> getBlogIdList(){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		ArrayList<String> idList = new ArrayList<String>();
		try {
 			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT blogid FROM BLOG_LIST WHERE blogtype = 'daum' and crawling = 'Y'");
			
			while(rs.next()){
				idList.add(rs.getString(1));
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		return idList;
	}
	
	public void waitDelay(int max, int min){
		try {
			 Random r = new Random(); 
			 r.setSeed(System.currentTimeMillis()); 

			int random = (int)(r.nextDouble() * (max - min +1)+min);
//			//System.out.println(random);
			Thread.sleep(random);
		} catch (InterruptedException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	public String getNextDate(String startDate){
		int sYear = Integer.parseInt(startDate.substring(0,4));
		int sMonth = Integer.parseInt(startDate.substring(5,6));
		int sDay = Integer.parseInt(startDate.substring(7,8));
		
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
		
		return sYear+sMonthStr+sDayStr;
	}
	
	public String mDate;
	
	public boolean hasNext(String startDate, String endDate){
		
		int sYear = Integer.parseInt(startDate.substring(0,4));
		int sMonth = Integer.parseInt(startDate.substring(5,6));
		int sDay = Integer.parseInt(startDate.substring(7,8));
		
		int eYear = Integer.parseInt(endDate.substring(0,4));
		int eMonth = Integer.parseInt(endDate.substring(5,6));
		int eDay = Integer.parseInt(endDate.substring(7,8));
		
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
	
	NLP nlp;
	public void blogStaticCralwer(){
		//System.out.println("Static Blog Crawler Start...");
		nlp = new NLP();
		boolean isNext = true;
		
		ArrayList<String> idList = getBlogIdList();
		
//		idList.add("khj3316");
		//System.out.println(idList.size());
		for(int i = 0; i < idList.size(); i++){
			String id = idList.get(i);
			
//			id = "khj3316";
			int cnt = 1;
			//System.out.println(id +"blog crawling");
			
			String tempPostAddr = "http://blog.daum.net/"+id+"/";
			//postView call / id get
			BufferedReader br = crawler(tempPostAddr);
			
//			//System.out.println(subAddrList[j]);
			
			String postAddr = new String();
			postAddr = getPostViewInfo(br);
			//System.out.println(postAddr);
			
			br = crawler("http://blog.daum.net/"+postAddr);
			int maxCnt = getMaxPage(br);
			
			String readId = postAddr.replace("/_blog/BlogTypeMain.do?blogid=", "").split("&")[0];
//	
			for(int j = maxCnt; j > 0; j--){
	//			//System.out.println(url);

				isNext = core(readId, id, postAddr, j);
				cnt++;
				waitDelay(2000,1500);
				
				if(!isNext){
					break;
				}
//				break;
			}		
//			isNext = true;
		}
	}
	public int getMaxPage(BufferedReader br){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		Document doc = Jsoup.parse(sb.toString());		
//		Elements eTAList = 
		String subString = doc.getElementsByClass("link_reMov").attr("href");
		return Integer.parseInt(subString.split("&")[1].replace("articleno=", ""));
		
//		//System.out.println("postViewLink:\t"+doc.getElementsByTag("frame").attr("src"));
	}
	public HashMap<String, String> getPostOrigianalPageInfo(String addr, String conId, StringBuffer sb){
		HashMap<String, String> resMap = new HashMap<String, String>();
		TagRemover tr = new TagRemover();
//		//System.out.println(sb.toString());
		//se_publishDate pcol2 fil5
		Document doc = Jsoup.parse(sb.toString());
		
		Elements meta = doc.getElementsByTag("meta");
		Elements date = doc.getElementsByClass("cB_Tdate");
		
		resMap.put("date",date.text());
		for(int i = 0 ; i < meta.size(); i++){
			String[] metaList = meta.get(i).toString().split("\"");
			
			
			if(metaList[1].equals("title")){
//				//System.out.println(metaList[1]+"\t"+metaList[3]);
				resMap.put("title",metaList[3]);
			}
		}//funcMenu_R
		
		Elements eList = doc.getAllElements();
		Elements conIfId = doc.getElementsByClass("funcMenu_R");
		conId= conIfId.text().split("/")[conIfId.text().split("/").length-1];
		if(m_Conid == 0){
			m_Conid = Integer.parseInt(conId);
		}
		Element eConTents1 = doc.getElementById("if_b_"+m_Conid);
//		//System.out.println("if_b_"+m_Conid);
		try {
			FileWriter fw = new FileWriter("tempHtml.html");
			BufferedWriter bw = new BufferedWriter(fw);
			
			
			bw.append(sb.toString());
			bw.flush();
			fw.flush();
	
			BufferedReader br = crawler(addr+eConTents1.attr("src").toString());
			String line;
			
			StringBuffer cBuf = new StringBuffer();
			while((line = br.readLine())!=null){
				
				cBuf.append(line+"\r\n");

			}
			
			Document cDoc = Jsoup.parse(cBuf.toString());
			Elements content = cDoc.getElementsByClass("cContentBody");
			m_Conid--;
			resMap.put("content",getBRTagText(content.toString()));
			resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		return resMap;
	}
	public int m_Conid = 0;
	public String getBRTagText(String text){
		String retVal = new String();
		TagRemover tr = new TagRemover();
		
		
		
		text = text.replace("&lt;","");
		text = text.replace("&gt;","");
		
		text = text.replace("\n","");
		text = text.replace("\r\n","");
		text = text.replace("<br>", ":BR:");
		text = text.replace("</p>", ":BR:");
		text = text.replace("\r\n", ":BR:");
		text = text.replace("&nbsp;", " ");
		retVal = tr.removeHTMLTag(text);
		
		String[] lineText = retVal.split(":BR:");
		
		int len = lineText.length;
//		
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < len ; i++){
			if(lineText[i].replace(" ", "").length() > 0){
				buf.append(lineText[i]);
				buf.append("\r\n");
			}
		}
		
		
		return buf.toString();
	}
	// 여기 수정
	public boolean core(String id, String provider, String postAddr, int cnt){
		///
		
		
		//http://blog.daum.net/_blog/BlogTypeView.do?blogid=0UmPM&articleno=2441&admin=
		
		//"%EB%B0%B1%EC%88%98%EC%98%A4&term=period&option.startDate=2015-04-23&option.endDate=2015-04-23&option.page.currentPage=1&option.orderBy=sim";
		boolean retVal = false;
		String url = "http://blog.daum.net/_blog/BlogTypeView.do?blogid="+id+"&articleno="+cnt;

		
//		Dictionary dic = new Dictionary();
//		HashMap<String, Integer> badMap = dic.getDBDic("bad.txt");
		
//		//System.out.println(url);
		BufferedReader br = crawler(url);
		HashMap<String, String> oriMap = new HashMap<String, String>();
		
		StringBuffer postBuf = getPostOrigianalPage(br);
		oriMap = getPostOrigianalPageInfo("http://blog.daum.net/", postAddr, postBuf);
		
//		//System.out.println(postBuf.toString());
		
		
		oriMap = getPostOrigianalPageInfo(postBuf);
		oriMap.put("provider", provider);
		oriMap.put("searchkeyword", "static");
		oriMap.put("httpLink", url);
//		oriMap.put("date", startDate);
		
		if(!oriMap.containsKey("exist")){
			oriMap.put("caution", "E");
			
			retVal = DBInput(oriMap);
			
			if(retVal){
				retVal = false;
			} else {
				retVal = true;
			}
			//System.out.println(retVal);
		}
		if(m_Conid == 1){
			retVal = false;
		}
		//System.out.println(retVal);
		return retVal;
	}	
	
	private String convertEncoring(String val){
		// String 을 euc-kr 로 인코딩.
//		//System.out.println(val);
		byte[] euckrStringBuffer = val.getBytes(Charset.forName("euc-kr"));

		String decodedFromEucKr;
		String decodedFromUtf8 = new String();
		try {
			decodedFromEucKr = new String(euckrStringBuffer, "euc-kr");

			byte[] utf8StringBuffer = decodedFromEucKr.getBytes("utf-8");
			decodedFromUtf8 = new String(utf8StringBuffer, "utf-8");
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("오류가 발생했습니다."); 
			
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
				System.out.println("오류가 발생했습니다."); 
				
			}
			
			f.delete();
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return f.getAbsolutePath();
	}
	private Connection conn;
	
	public boolean DBInput(HashMap<String, String> map){
		boolean isExist = false;
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
		PreparedStatement stmt;
		ResultSet rs;

		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			String sqlp = "select count(*) from blog_info where http_addr = ?";
			stmt = conn.prepareStatement(sqlp);
			
			stmt.setString(1, map.get("httpLink"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			int count = rs.getInt(1);
			//System.out.println("중복카운트:\t"+count);
			if(count == 0){
				String sql = "INSERT INTO blog_info(blog_wrter, doc_cret_dt, doc_sj, doc_cn, http_addr, file_stre_addr, kwrd, srch_kwrd, caution) VALUES (?,?,?,?,?,?,?,?,?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
		
				pstmt.setString(1, map.get("provider"));
				pstmt.setString(2, map.get("date"));
				pstmt.setString(3, map.get("title"));
				pstmt.setString(4, map.get("content"));
				pstmt.setString(5, map.get("httpLink"));
				pstmt.setString(6, map.get("fileLink"));
				
				pstmt.setString(7,map.get("Keywords"));
				
				if(nlp.simMap.containsKey(map.get("searchkeyword"))){
					pstmt.setString(8, nlp.simMap.get(map.get("searchkeyword")));
				} else {
					pstmt.setString(8, map.get("searchkeyword"));
				}
				
				pstmt.setString(9, map.get("caution"));
							
				if(map.get("title").length() > 0){
					pstmt.executeUpdate();
				}
				
				pstmt.close();
				isExist = false;
			} else {
				isExist = true;
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		return isExist;
	}
	public StringBuffer getPostOrigianalPage(BufferedReader br){
		StringBuffer sb = new StringBuffer();		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return sb;
	}

	public HashMap<String, String> getPostOrigianalPageInfo(StringBuffer sb){
		HashMap<String, String> resMap = new HashMap<String, String>();
		TagRemover tr = new TagRemover();
//		//System.out.println(sb.toString());
		//se_publishDate pcol2 fil5
		Document doc = Jsoup.parse(sb.toString());
		
		Elements meta = doc.getElementsByTag("meta");
		Elements date = doc.getElementsByClass("cB_Tdate");
		
		resMap.put("date",date.text());
		for(int i = 0 ; i < meta.size(); i++){
			String[] metaList = meta.get(i).toString().split("\"");
			
			
			if(metaList[1].equals("title")){
//				//System.out.println(metaList[1]+"\t"+metaList[3]);
				resMap.put("title",metaList[3]);
			} else if(metaList[1].equals("og:description")){
//				//System.out.println(metaList[1]+"\t"+metaList[3]);
				resMap.put("content",metaList[3]);
				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
			} 
		}
		
//		
//		Elements eList = doc.getAllElements();
//		
//		Elements eConTents1 = doc.getElementsByClass("se_textarea");
//		Elements eDateList = doc.getElementsByClass("se_publishDate");
//		
//		//se_doc_title_top
//		
//		//�뀘�뀋 postViewArea
//		Elements eTitleList = doc.getElementsByTag("title");
//		Element eConTents2 = doc.getElementById("postViewArea");	
//
//		resMap.put("title", tr.removeHTMLTag(eTitleList.text().replace(": 네이버 블로그", "")));
//		if(eConTents1.text().length() < 1){
//			if(eConTents2 != null){ 
//				resMap.put("content", tr.removeHTMLTag(eConTents2.text()));
//				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
////				resMap.put("tKeywords", nlp.extractBag(resMap.get("title"),1));
//			} else {
//				resMap.put("exist", "");
//			}
//	
//		} else{
//			if(eConTents1.text().length() > 1){ 
//				resMap.put("content", tr.removeHTMLTag(eConTents1.text()));
//				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
////				resMap.put("tKeywords", nlp.extractBag(resMap.get("title"),1));
//			} else {
//				resMap.put("exist", "");
//			}
//		}
		return resMap;
	}



	public String getPostViewInfo(BufferedReader br){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		Document doc = Jsoup.parse(sb.toString());		
//		Elements eTAList = 
		String subString = doc.getElementsByTag("frame").attr("src");
//		//System.out.println("postViewLink:\t"+doc.getElementsByTag("frame").attr("src"));
		
		return subString;
	}
	
	public int getSearchTotalCount(BufferedReader br, String link){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Element eTAList = doc.getElementById("resultCntArea");
		
		int retVal = 0;
		
		if(eTAList != null){
			String[] cntStrList = eTAList.text().split("/");
					
			
			if(eTAList.text().contains("건")){
				retVal = Integer.parseInt(cntStrList[1].replace(",", "").replace("약", "").replace(" ", "").replace("건", ""));
			}
		}
		
//		//System.out.println("SearchResult Count:\t"+retVal);
		return retVal;
	}


	
	public String getBlogMoreAddr(BufferedReader br){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Element eTAList = doc.getElementById("blogExtMore");
		
		String[] strList = eTAList.toString().split("\"");
		
//		//System.out.println(strList.length+"\t"+strList[1]);
//		Elements list =  eTAList.getAllElements();
//		
//		for(int i = 0; i < list.size() ; i++){
//			//System.out.println(i+"\t"+list.get(i).toString());
//		}
//		String moreAddr = eTAList.getAllElements();
//		//System.out.println(moreAddr);
//		//System.out.println("SearchResult Count:\t"+retVal);
		return strList[1];
	}
	
	public HashMap<String, String> getContents(BufferedReader br, HashMap<String, String> resMap, String link, String keyword){
		StringBuffer sb = new StringBuffer();
				 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
//				//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Elements eTAList = doc.getElementsByClass("f_url");
		for(int i = 0 ; i < eTAList.size() ; i++){
			String href = eTAList.get(i).toString();
			if(!href.contains("keyword.daumdn.com") && href.contains("blog.daum.net")){
//				//System.out.println(eTAList.get(i).toString().split("\"")[1]);
//				//System.out.println(eTAList.get(i).toString());
				resMap.put(eTAList.get(i).toString().split("\"")[1], "1");
			}
		}
		
		
//		f_url
		
//		Elements eTAList = doc.getElementsByClass("several_post");
//		Elements eDateList = doc.getElementsByTag("a");
//		
//		int size = eDateList.toggleClass("href").size();
//		
//		int cnt = 0;
//		for(int i = 0; i < size ; i++){
//			if(eDateList.toggleClass("href").get(i).attr("href").contains("Redirect")){
//				
//				String title= new String();
//				
//				if(cnt % 3 == 0){
//					title = eDateList.toggleClass("href").get(i).text();
//				}
//				
//				if(title.contains("블로그 이용 팁")){
//					break;
//				}
//				
//				cnt++;
//				if(title.contains(keyword)){
//					resMap.put(eDateList.toggleClass("href").get(i).attr("href"), "1");
//				}
//			}
//		}	
		return resMap;
	}
	private Logger logger = Logger.getLogger(getClass());
	public BufferedReader crawler(String addr){
		
		HttpGet http = new HttpGet(addr);
		
		RequestConfig customizedRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
		HttpClientBuilder customizedClientBuilder = HttpClients.custom().setDefaultRequestConfig(customizedRequestConfig);
		CloseableHttpClient client = customizedClientBuilder.build();
	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹껜 �깮�꽦
	    HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        // Waiting for a connection from connection manager
                        .setConnectionRequestTimeout(10000)
                        // Waiting for connection to establish
                        .setConnectTimeout(5000)
                        .setExpectContinueEnabled(false)
                        // Waiting for data
                        .setSocketTimeout(5000)
                        .build())
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(100)
                .build();
	    
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
			System.out.println("오류가 발생했습니다."); 
			
		}
		
//		HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
//
////		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
//
//		HttpGet http = new HttpGet(addr);
//		
////		HttpClient httpClient =  HttpClientBuilder.create().build();
//		
////		getRequest.addHeader("accept", "application/json");
//		 
//		
//	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹껜 �깮�꽦
////	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣瑜� Response 媛앹껜�뿉 �떞�쓬
//	    HttpResponse response = null;
//	    
//	    BufferedReader retBr = null;
//		try {
//			
////			StringEntity input = new StringEntity("{\"qty\":100,\"name\":\"iPad 4\"}");
////			input.setContentType("application/json");
////			HttpPost postRequest = new HttpPost(addr);
//			
//			
////			postRequest.setEntity(input);
////			postRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;"); 
////
////			HttpResponse response = httpClient.execute(postRequest);
//			
//			response = httpClient.execute(http);
//			// 5. Response 諛쏆� �뜲�씠�꽣 以�, DOM �뜲�씠�꽣瑜� 媛��졇�� Entity�뿉 �떞�쓬
//		    HttpEntity entity = response.getEntity();
//		    // 6. Charset�쓣 �븣�븘�궡湲� �쐞�빐 DOM�쓽 而⑦뀗�듃 ���엯�쓣 媛��졇�� �떞怨� Charset�쓣 媛��졇�샂 
//		    ContentType contentType = ContentType.getOrDefault(entity);
////	        Charset charset = contentType.getCharset();
//	        
//	        // 7. DOM �뜲�씠�꽣瑜� �븳 以꾩뵫 �씫湲� �쐞�빐 Reader�뿉 �떞�쓬 (InputStream / Buffered 以� �꽑�깮�� 媛쒖씤痍⑦뼢) 
//		    retBr = new BufferedReader(new InputStreamReader(entity.getContent()));
//		   
//		} catch (IOException e) {
//			System.out.println("오류가 발생했습니다."); 
////			
//		}
	    
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
//				//System.out.println(line+"\t"+URLEncoder.encode(line));
				list.add(URLEncoder.encode(line));
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
	
	public ArrayList<String> getKeywordListUnicode(){
		
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> klist = new ArrayList<String>();
		ArrayList<String> blist = new ArrayList<String>();
		
		Dictionary dic = new Dictionary();
		klist = dic.getDBDicList("dic.txt");
		blist = dic.getDBDicList("best");
		int size = klist.size();
		boolean isStart = true;
		
		for(int i = 0; i < blist.size() ; i++){
			list.add(URLEncoder.encode(blist.get(i)));
		}
		for(int i = 0; i < size; i++){
			
			if(isStart)
				list.add(URLEncoder.encode(klist.get(i)+" 식품"));
			if(klist.get(i).equals("맛")){
				isStart = true;
			}
		}
						
		return list;
	}
	
	public ArrayList<String> getKeywordList(){
		
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> klist = new ArrayList<String>();
		ArrayList<String> blist = new ArrayList<String>();
		
		Dictionary dic = new Dictionary();
		klist = dic.getDBDicList("dic.txt");
		blist = dic.getDBDicList("best");
		int size = klist.size();
		boolean isStart = true;
		
		for(int i = 0; i < blist.size() ; i++){
			list.add(blist.get(i));
		}
		for(int i = 0; i < size; i++){
			
			if(isStart)
				list.add(klist.get(i));
			if(klist.get(i).equals("맛")){
				isStart = true;
			}
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
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
				
		return list;
	}
}
