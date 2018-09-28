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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.common.*;
import com.onycom.crawler.common.Dictionary;

public class ankusCrawlerDaumBlog {
	int monthList[] = {31,28,31,30,31,30,31,31,30,31,30,31};


	public void waitDelay(int max, int min){
		try {
			 Random r = new Random(); 
			 r.setSeed(System.currentTimeMillis()); 

			int random = (int)(r.nextDouble() * (max - min +1)+min);
			Thread.sleep(random);
		} catch (InterruptedException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
	}
	
	//즉시 반영기능을 위한 수집/필터링 모듈 분리
	
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
	
	public void detectBlog(int dueDate, int limDate){
		
		long sTime = System.currentTimeMillis();
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(limDate);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		
		Dictionary dic = new Dictionary();
		badMap = dic.getDBDic2("bad.txt");
		
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			mDate = startDate;
//			core(startDate, startDate);
			
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
	}
	
	public void blogCralwer(int dueDate, int limDate){
		
		long sTime = System.currentTimeMillis();
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDayD(limDate);
		String yd2 = yd.getYesterDayD(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			mDate = startDate;
//			try {
			core(startDate+"000000", startDate+"235959");
//			} catch(Exception e){
				
//			}
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
	}
	
	NLP nlp;
	
	//
	
	public void blogCralwer(int dueDate){
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDayD(1);
		String yd2 = yd.getYesterDayD(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		while(hasNext(startDate, endDate)){

			mDate = startDate;
			core(startDate+"000000", startDate+"235959");
			
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
	}
	
	public void core(String startDate, String endDate){
		
		String fileMPath = "blog/";
		String blogSubString ="http://blog.daum.net";
		
		
		ArrayList<String> keywordListUTF = getKeywordList();
		ArrayList<String> keywordList = getKeywordListUnicode(keywordListUTF);
		String searchAddr = "http://search.daum.net/search?w=blog&q=";

		String startAddr = "&sd=";
		String endAddr = "&ed="; 
		String pageAddr = "&page=";
		
		int page = 1;
		
		String searchTypeAddr = "&sort=accu&DA=NTB";
		String fullPath;
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> badMap = dic.getDBDic("bad.txt");
		
		boolean isStop = false;
		
		for(int i = 0; i < keywordList.size() ; i++){
			int totalDocSize = 0;
			fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+page+searchTypeAddr;
			
//			fullPath = searchAddr + keywordList.get(i);
			BufferedReader br = crawler(fullPath);
			totalDocSize = getSearchTotalCount(br, fullPath);	
			
//			String moreAddr = getBlogMoreAddr(br);
//			
//			BufferedReader br2 = crawler(searchAddrSub+moreAddr);
//			
			
			if(totalDocSize < 10){
				page = 2;
			} else if(totalDocSize%10 > 0){
				page = totalDocSize/10 + 2;
			} else {
				page = totalDocSize/10 + 1;
			}
			
			HashMap<String, String> subAddrMap = new HashMap<String, String>();
			 
			for(int j = 1; j < page; j++){
				fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+j+searchTypeAddr;
				br = crawler(fullPath);
				
				getContents(br, subAddrMap, fullPath, keywordListUTF.get(i));
				
			}
			
			
			Object[] subAddrList = subAddrMap.keySet().toArray();
			
			//rdirect Page crawling
			for(int j = 0; j < subAddrList.length; j++){
				waitDelay(2000,1500);
				
				br = crawler((String)subAddrList[j]);
				
				
				String postAddr = new String();
				try {
					postAddr = getPostViewInfo(br);
				} catch(Exception e){
					i++;
					break;
				}
				
				//original page
				HashMap<String, String> oriMap = new HashMap<String, String>();
				
				
				br = crawler(blogSubString+postAddr);
				
				String urlList[] = ((String)subAddrList[j]).split("/");
				String conid = urlList[urlList.length-1];
				
				StringBuffer postBuf = getPostOrigianalPage(br);
				oriMap = getPostOrigianalPageInfo(blogSubString,conid, postBuf);
				
				
				oriMap.put("searchkeyword", keywordListUTF.get(i));
				oriMap.put("date", startDate.substring(0,4)+"-"+startDate.substring(4,6)+"-"+startDate.substring(6,8));
				
				if(!oriMap.containsKey("exist")){
				
					oriMap.put("httpLink", (String)subAddrList[j]);
					String provider = ((String)subAddrList[j]).replace("http://blog.daum.net/", "").replace("?", ",").split("/")[0];
					
					oriMap.put("provider", provider);
					
					oriMap.put("fileLink", htmlFileWriter(fileMPath, "/"+keywordListUTF.get(i)+"/"+startDate+"/"+"daum_search_"+j+".html",postBuf));
					
					oriMap.put("caution", "N");
					
					DBInput(oriMap);
					
					
					//printBlog(oriMap);
					isStop = true;
				}
//				break;
			}
			
//			break;
//			if(isStop){
//				break;
//			}
		}
	}
	public ArrayList<String> getKeywordListUnicode(ArrayList<String> flist){
		ArrayList<String> list = new ArrayList<String>();
		
		int size = flist.size();
		
		for(int i = 0; i < size ; i++){
			list.add(URLEncoder.encode(flist.get(i) +" 식품"));
		}
		
		return list;
	}
	public void updateCaution(int dueDate){
		long sTime = System.currentTimeMillis();
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(1);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		Dictionary dic = new Dictionary();
		badMap = dic.getDBDic2("bad.txt");
		
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			mDate = startDate;
//			core(startDate, startDate);
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
	}
	HashMap<String, HashMap<String, Integer>> badMap;
	
	public void updateCaution(String date){
		String sql = "SELECT sn, doc_sj, doc_cn from blog_info WHERE doc_cret_dt = ? and (caution <> 'E' and caution <> 'C' and srch_kwrd <> 'static')";
		String pSql = "UPDATE blog_info SET caution = ?, kwrd = ?, kwrd_sj = ? WHERE sn = ?";
		
		
		DBConnect dbconn = new DBConnect();
		conn = dbconn.getConnection();
		
		DBConnect dbconn2 = new DBConnect();
		Connection conn2 = dbconn2.getConnection();
		
		if(nlp == null){
			nlp = new NLP();
		}
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, date);
			ResultSet rs = stmt.executeQuery();
			
			PreparedStatement pstmt = conn2.prepareStatement(pSql);
	
			while(rs.next()){
				
				String title = rs.getString(2);
				String[] wordList = nlp.extractBag(title).split("\t");
				
				boolean isBad = false;
				boolean isSynonym = false;
				
				if(badMap == null){
					Dictionary dic = new Dictionary();
					badMap = dic.getDBDic2("bad.txt");
				}
				
				ArrayList<String> jcList = new ArrayList<String>();
				for(int i = 0; i < wordList.length; i++){
					if(wordList != null && badMap.containsKey(wordList[i].split(":")[0])){
						isBad = true;
						
						HashMap<String, Integer> subMap = badMap.get(wordList[i].split(":")[0]);
						if(subMap.size() > 0){
							Object keyList[] = subMap.keySet().toArray();
							for(int j = 0; j < keyList.length; j++){
								if(title.contains((String)keyList[j])){
									isSynonym = true;
									jcList.add((String)keyList[j]);
								}
							}
							
//							for(int j = 0; j < wordList.length; j++){
//								if(subMap.containsKey(wordList[j].split(":")[0])){
//									isSynonym = true;
//								}
//							}
						}
					}
				}
				
				
				//wordList.length > 10
				if(isBad && !isSynonym && wordList.length < 15 ){
					String kwrdSJ = nlp.extractBag(title, 1);
					
					
					pstmt.setString(1, "Y");
					pstmt.setString(4, rs.getString(1));
					pstmt.setString(3, kwrdSJ);
					
					pstmt.setString(2, nlp.extractBag(rs.getString(3),1));
					pstmt.executeUpdate();
					
				} else if(isBad && isSynonym){
					String kwrdSJ = nlp.extractBag(title,1);
					
					
					StringBuffer jcBuf = new StringBuffer();
					jcBuf.append(" (제척단어: ");
					
					for(int i = 0 ; i < jcList.size() ; i++){
						jcBuf.append(jcList.get(i));
						
						if( i < jcList.size()-1){
							jcBuf.append(", ");
						}
					}
					jcBuf.append(")");
					
					pstmt.setString(1, "N");
					pstmt.setString(4, rs.getString(1));
					
					pstmt.setString(3, kwrdSJ+jcBuf.toString());
					
					pstmt.setString(2, nlp.extractBag(rs.getString(3),1));
					
					pstmt.executeUpdate();
				}	else {
					String kwrdSJ = nlp.extractBag(title, 1);
					
					pstmt.setString(1, "N");
					pstmt.setString(4, rs.getString(1));
					
					pstmt.setString(3, kwrdSJ);
					pstmt.setString(2, nlp.extractBag(rs.getString(3),1));
					
					pstmt.executeUpdate();
				}
			}
			rs.close();
			stmt.close();
			
			conn.close();
			conn2.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	private String convertEncoring(String val){
		// String 을 euc-kr 로 인코딩.
		byte[] euckrStringBuffer = val.getBytes(Charset.forName("euc-kr"));

		String decodedFromEucKr;
		String decodedFromUtf8 = null;
		try {
			decodedFromEucKr = new String(euckrStringBuffer, "euc-kr");

			byte[] utf8StringBuffer = decodedFromEucKr.getBytes("utf-8");
			decodedFromUtf8 = new String(utf8StringBuffer, "utf-8");
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return decodedFromUtf8;
	}
	
	//여기 수정
	public String htmlFileWriter(String mPath, String id, StringBuffer buf){
		FileWriter fw;
		BufferedWriter bw;
		File f = new File(mPath+id);
		f.getParentFile().mkdirs();
		
		String path = mPath+id;
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
//				System.out.println("오류가 발생했습니다."); 
//				
//			}
//			
//			f.delete();
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return mPath+id;
	}
	private Connection conn;
	
	public void printBlog(HashMap<String, String> map){
	}
	
	public void DBInput(HashMap<String, String> map){

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
		 */
		PreparedStatement stmt;
		ResultSet rs;
		
		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			
			String sqlp = "select count(*) from blog_info where doc_sj = ?";
			stmt = conn.prepareStatement(sqlp);
			
			stmt.setString(1, map.get("title"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			int count = rs.getInt(1);
			if(count == 0){
				String sql = "INSERT INTO blog_info(blog_wrter, doc_cret_dt, doc_sj, doc_cn, http_addr, file_stre_addr, kwrd, srch_kwrd, caution) VALUES (?,?,?,?,?,?,?,?,?)";
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
				
				pstmt.setString(7,map.get("Keywords"));
				
				if(nlp.simMap.containsKey(map.get("searchkeyword"))){
					pstmt.setString(8, nlp.simMap.get(map.get("searchkeyword")));
				} else {
					pstmt.setString(8, map.get("searchkeyword"));
				}
				
				
				pstmt.setString(9, map.get("caution"));
				//caution information input
							
				pstmt.executeUpdate();
				
				pstmt.close();
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
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
			System.out.println("오류가 발생했습니다."); 
			
		}
		return sb;
	}
	public HashMap<String, String> getPostOrigianalPageInfo(String addr, String conId, StringBuffer sb){
		HashMap<String, String> resMap = new HashMap<String, String>();
		TagRemover tr = new TagRemover();

		Document doc = Jsoup.parse(sb.toString());
		
		
		try {
			Elements meta = doc.getElementsByTag("meta");
			Elements date = doc.getElementsByClass("cB_Tdate");
			
			resMap.put("date",date.text());
			for(int i = 0 ; i < meta.size(); i++){
				String[] metaList = meta.get(i).toString().split("\"");
				
				
				if(metaList[1].equals("title")){
					resMap.put("title",metaList[3]);
				}
			}
			//summaryArea
			Element eConTents1 = doc.getElementById("if_b_"+conId);

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

				resMap.put("content",getBRTagText(content.toString()));
				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
			} catch (IOException e) {
				System.out.println("오류가 발생했습니다."); 
				
			}
		} catch (Exception e){
			Date date = new Date();
			
			String path = "logs/";
			String fileName = "crawler_"+date.getYear()+date.getMonth()+date.getDay()+".txt";
				
			StringBuffer buf = new StringBuffer();
				
			buf.append("네이버 블로그 웹페이지 변경 또는 주소체계 변경으로 웹문서가 수집되지 않으므로 유지보수가 필요합니다.");			
			htmlFileWriter(path, fileName, buf);

		}
		
		return resMap;
	}

	public String getPostViewInfo(BufferedReader br){
		StringBuffer sb = new StringBuffer();
				 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		Document doc = Jsoup.parse(sb.toString());		
		String subString = doc.getElementsByTag("frame").attr("src");
		return subString;
	}
	
	public int getSearchTotalCount(BufferedReader br, String link){
		StringBuffer sb = new StringBuffer();
				 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
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
		
		return retVal;
	}
	
	public String getBlogMoreAddr(BufferedReader br){
		StringBuffer sb = new StringBuffer();
		
		HashMap<String, String> resMap = new HashMap<String, String>();
		 
		String line = null;
		
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Element eTAList = doc.getElementById("blogExtMore");
		
		String[] strList = eTAList.toString().split("\"");
		
		return strList[1];
	}
	
	public HashMap<String, String> getContents(BufferedReader br, HashMap<String, String> resMap, String link, String keyword){
		StringBuffer sb = new StringBuffer();
				 
		String line = null;
		try {
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Elements eTAList = doc.getElementsByClass("f_url");
		Elements eTitle = doc.getElementsByClass("wrap_cont");
		
		for(int i = 0; i < eTitle.size() ; i++){
			String href = eTitle.get(i).getElementsByClass("f_url").toString().split("\"")[1];
			
			String title = eTitle.get(i).getElementsByClass("f_link_bu").text();
			if(title.contains(keyword)){
				if(!href.contains("keyword.daumdn.com") && href.contains("blog.daum.net")){
					resMap.put(eTAList.get(i).toString().split("\"")[1], "1");
					
				}
			}
		}

		return resMap;
	}
	private Logger logger = Logger.getLogger(getClass());
	public BufferedReader crawler(String addr){
		
		HttpGet http = new HttpGet(addr);
		
		RequestConfig customizedRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
		HttpClientBuilder customizedClientBuilder = HttpClients.custom().setDefaultRequestConfig(customizedRequestConfig);
		CloseableHttpClient client = customizedClientBuilder.build();
	    // 3. 媛��졇�삤湲곕�� �떎�뻾�븷 �겢�씪�씠�뼵�듃 媛앹껜 �깮�꽦
		Date expdate = new Date();
		expdate.setTime(expdate.getTime() + (3600 * 1000));
		DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String cookieExpire = "expires="+df.format(expdate);
		 
	    HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        // Waiting for a connection from connection manager
                        .setConnectionRequestTimeout(10000)
                        // Waiting for connection to establish
                        .setConnectTimeout(5000)
                        .setExpectContinueEnabled(false)
                        // Waiting for data
                        .setSocketTimeout(5000)
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .build())
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(100)
                .build();
	    
	    
//		HttpClient httpClient = new DefaultHttpClient();
//		httpClient.getParams().setParameter("http.protocol.single-cookie-header", true);
//		httpClient.getParams().
		http.setHeader("Set-Cookie", "expires="+DateUtil.formatDate(expdate)+";");
	    // 4. �떎�뻾 諛� �떎�뻾 �뜲�씠�꽣瑜� Response 媛앹껜�뿉 �떞�쓬
	    HttpResponse response = null;
	    
	    BufferedReader retBr = null;
	    
		try {
			response = httpClient.execute(http);
			response.setHeader("Set-Cookie", "expires="+DateUtil.formatDate(expdate)+";");
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
//		retVal.replace("\n","");
//		retVal.replace("\r\n","");
//		
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
	public ArrayList<String> getKeywordListUnicode(String path){
		ArrayList<String> list = new ArrayList<String>();
		
		FileReader fr;
		BufferedReader br;
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			String line;
			
			while((line = br.readLine())!=null){
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
			list.add(URLEncoder.encode(klist.get(i)+" 식품"));
		}
		
//		list.add(URLEncoder.encode("홍삼"));
						
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
			list.add(klist.get(i));
		}
//		list.add("홍삼");
						
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
