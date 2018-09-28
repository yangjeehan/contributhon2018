package com.onycom.crawler.blog;
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
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.blog.naverURL;
import com.onycom.crawler.common.*;
import com.onycom.crawler.common.Dictionary;

public class ankusCrawlerNaverBlog {
	int monthList[] = {31,28,31,30,31,30,31,31,30,31,30,31};

	private NaverBlogVal mVal;
	
	public ankusCrawlerNaverBlog(){
		mVal = new NaverBlogVal();
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
	
	public ArrayList<String> getKeywordListUnicode(ArrayList<String> flist){
		ArrayList<String> list = new ArrayList<String>();
		
		int size = flist.size();
		
		for(int i = 0; i < size ; i++){
			list.add(URLEncoder.encode(flist.get(i)));
		}
		
		return list;
	}
	
	//즉시 반영기능을 위한 수집/필터링 모듈 분리
	
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
	
	public void detectBlog(int dueDate, int limDate){
		
		long sTime = System.currentTimeMillis();
		nlp = new NLP("dic.blog");
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(limDate);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		//System.out.println("Blog Detect Only Mode Start...");
		
		Dictionary dic = new Dictionary();
		badMap = dic.getDBDic2("bad.txt");
		
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			//System.out.println(startDate);
			mDate = startDate;
//			core(startDate, startDate);
			
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
		//System.out.println(System.currentTimeMillis() - sTime);
	}
	
	public void blogCralwer(int dueDate, int limDate){
		long sTime = System.currentTimeMillis();
		
		nlp = new NLP();

		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(limDate);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		System.out.print(yd1);
		System.out.println("\tBlog Crawling Start");
		
		boolean isException = false;
		while(hasNext(startDate, endDate) || isException){
			isException = false;
			mDate = startDate;
			try {
				core(startDate, startDate);
			} catch (Exception e) {
				if(hasNext(startDate, endDate))
					isException = true;
			} finally {
				startDate = getNextDate(startDate);
				waitDelay(1,2);
			}
		}		
	}
	
	NLP nlp;
	
	public void blogCralwer(int dueDate){
		
		long sTime = System.currentTimeMillis();
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(1);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		//System.out.println("Blog Crawling Start");
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			mDate = startDate;
			core(startDate, startDate);
			
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
		//System.out.println(System.currentTimeMillis() - sTime);
	}
	
	
	
	public void core(String startDate, String endDate){
		//"%EB%B0%B1%EC%88%98%EC%98%A4&term=period&option.startDate=2015-04-23&option.endDate=2015-04-23&option.page.currentPage=1&option.orderBy=sim";
		
		String fileMPath = "blog/";
		String blogSubString ="http://blog.naver.com/";
		
		ArrayList<String> keywordListUTF = getKeywordList();
		ArrayList<String> keywordList = getKeywordListUnicode(keywordListUTF);
		
		for(int i = 0; i < keywordList.size() ; i++){
			System.out.println("수집 키워드: "+keywordListUTF.get(i));

			crawlerPage cp = new crawlerPage();
			naverURL nu = new naverURL();
			
			int preCnt = 1;
				while(true){
					int maxCnt;
					
					maxCnt = cp.crawler(nu.getURL(keywordList.get(i), startDate,endDate, preCnt));
					
					if(maxCnt < preCnt){
						break;
					}
					preCnt += 10;
				}

									
			ArrayList<String> subAddrList = cp.getSubAddr();
			//rdirect Page crawling
			
			System.out.println("검색된 페이지 수:\t"+subAddrList.size());
			for(int j = 0; j < subAddrList.size() ; j++){
				waitDelay(2000,1500);
				
				BufferedReader br = crawler(subAddrList.get(j));
				
				if(br != null){
//					System.out.println(subAddrList.get(j));
					String postAddr = getPostViewInfo(br);
					
					//original page
					HashMap<String, String> oriMap = new HashMap<String, String>();
					
					br = crawler(blogSubString+postAddr);

					StringBuffer postBuf = getPostOrigianalPage(br);
					oriMap = getPostOrigianalPageInfo(postBuf);

					oriMap.put("searchkeyword", keywordListUTF.get(i));
					
					oriMap.put("brand",keywordListUTF.get(i).split("\t")[0]);
					oriMap.put("item",keywordListUTF.get(i).split("\t")[1]);
					
					oriMap.put("date", startDate);
					
					System.out.println(oriMap.size());
					
					printBlog(oriMap);
					
					System.out.println(oriMap.containsKey("exist"));
					
					if(!oriMap.containsKey("exist")){
						System.out.println("DBInput Init");
						oriMap.put("httpLink", (String)subAddrList.get(j));
						String provider = ((String)subAddrList.get(j)).replace("http://blog.naver.com/", "").replace("?", ",").split(",")[0];
						
						oriMap.put("provider", provider);
						
						oriMap.put("fileLink", "");
						
						oriMap.put("caution", "N");
						
						DBInput(oriMap);
						printBlog(oriMap);
					}
					System.out.println("Craw Page Finish...");
				}
			}
			
//			if(isStop){
//				break;
//			}
		}
	}
	
	public void updateCaution(int dueDate){
		long sTime = System.currentTimeMillis();
		nlp = new NLP();
		YesterDayGenerate yd = new YesterDayGenerate();
		String yd1 = yd.getYesterDay(1);
		String yd2 = yd.getYesterDay(dueDate);
		
		String startDate = yd2;
		String endDate = yd1;
		
		//System.out.println("Blog Detection Process Only Start");
		Dictionary dic = new Dictionary();
		badMap = dic.getDBDic2("bad.txt");
		
		while(hasNext(startDate, endDate)){

//			acn.core(startDate, endDate);
			//System.out.println(startDate);
			mDate = startDate;
//			core(startDate, startDate);
			//System.out.println(startDate);
			updateCaution(startDate);
			
			startDate = getNextDate(startDate);
			waitDelay(1,2);
		}		
		//System.out.println(System.currentTimeMillis() - sTime);
	}
	HashMap<String, HashMap<String, Integer>> badMap;
	
	public void updateCaution(String date){
		String sql = "SELECT sn, doc_sj, doc_cn from blog_info WHERE doc_cret_dt = ? and (caution <> 'E' and caution <> 'C' and srch_kwrd <> 'static')";
		String pSql = "UPDATE blog_info SET caution = ?, kwrd = ?, kwrd_sj = ? WHERE sn = ?";
		
//		//System.out.println("Update Caution Process Running 2...");
		
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
			
			PreparedStatement pstmt = conn2.prepareStatement(pSql);
			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()){
				
//				//System.out.println(rs.getString(1));
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
				
//				//System.out.println(wordList.length+"\t"+isBad+"\t"+isSynonym);
				
				//wordList.length > 10
				if(isBad && !isSynonym && wordList.length < 15 ){
					String kwrdSJ = nlp.extractBag(title, 1);
					
//					//System.out.println("Y:\t"+rs.getString(2)+"\t"+kwrdSJ);
//					//System.out.println();
					
					pstmt.setString(1, "Y");
					pstmt.setString(4, rs.getString(1));
					pstmt.setString(3, kwrdSJ);
					
					pstmt.setString(2, nlp.extractBag(rs.getString(3),1));
					pstmt.executeUpdate();
					
				} else if(isBad && isSynonym){
					String kwrdSJ = nlp.extractBag(title,1);
					
//					//System.out.println("N:\t"+rs.getString(2)+"\t"+kwrdSJ);
//					//System.out.println();
					
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
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
	private String convertEncoring(String val){
		// String 을 euc-kr 로 인코딩.
//		//System.out.println(val);
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
//			f.delete();
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return mPath+id;
	}
	private Connection conn;
	
	public void printBlog(HashMap<String, String> map){
		System.out.println(map.get("searchkeyword"));
		System.out.println("httpLink:\t"+map.get("httpLink"));
		System.out.println("fileLink:\t"+map.get("fileLink"));
		System.out.println("provider:\t"+map.get("provider"));
		System.out.println("date:\t"+map.get("date"));
		System.out.println("title:\t"+map.get("title"));
		System.out.println("content:\t"+map.get("content"));
		System.out.println("print:\t"+map.get("Keywords"));
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
			String pSql = "select count(*) from blog_info where doc_sj = ?";
			
			stmt = conn.prepareStatement(pSql);
			
			stmt.setString(1, map.get("title"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			int count = rs.getInt(1);
			System.out.println("중복카운트:\t"+ map.get("title")+"\t"+count);
			if(count == 0){
				String sql = "INSERT INTO blog_info(blog_wrter, doc_cret_dt, doc_sj, doc_cn, http_addr, file_stre_addr, kwrd, srch_kwrd, caution, brand, item) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				
				pstmt.setString(1, map.get("provider"));
				pstmt.setString(2, map.get("date"));
				pstmt.setString(3, map.get("title"));
				pstmt.setString(4, map.get("content"));
				pstmt.setString(5, map.get("httpLink"));
				pstmt.setString(6, map.get("fileLink"));
				
				pstmt.setString(7,map.get("Keywords"));
				
//				if(nlp.simMap.containsKey(map.get("searchkeyword"))){
//					pstmt.setString(8, nlp.simMap.get(map.get("searchkeyword")));
//				} else {
					pstmt.setString(8, map.get("searchkeyword"));
//				}
				
				
				pstmt.setString(9, map.get("caution"));
				pstmt.setString(10, map.get("brand"));
				pstmt.setString(11, map.get("item"));
				//caution information input
							
				pstmt.executeUpdate();
				
				pstmt.close();
				
				System.out.println("DBInput Complete");
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("DBInput - 오류가 발생했습니다."); 
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
			e.printStackTrace();
			
		}
		
//		System.out.println(sb.toString());
		return sb;
	}
	
	public String getBRTagText(String text, int mode){
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
	
	public HashMap<String, String> getPostOrigianalPageInfo(StringBuffer sb){
		HashMap<String, String> resMap = new HashMap<String, String>();
		TagRemover tr = new TagRemover();

		Document doc = Jsoup.parse(sb.toString());

		Elements eList = null;
		Elements eConTents1 = null;
		Elements eDateList = null;
		Elements eTitleList = null;
		Element eConTents2 = null;
		Elements titleMeta = null;
		
		try {
			eList = doc.getAllElements();
			
			eList = doc.getAllElements();
			
			eConTents1 = doc.getElementsByClass(mVal.post1);
			eDateList = doc.getElementsByClass(mVal.post2);

			eTitleList = doc.getElementsByClass(mVal.post3);
			titleMeta = doc.getElementsByTag(mVal.post4);
					
			String title = new String();
			title = eTitleList.text();

			if(title.length() < 1){
				for(int i = 0; i < titleMeta.size() ; i++){
					
					String meta = titleMeta.get(i).toString();
					if(meta.contains(mVal.post5)){
						title = meta.split("\"")[3];
					}
				}
			}			
			eConTents2 = doc.getElementById(mVal.post6);	

			resMap.put("title", title);
			if(eConTents1.text().length() < 1){
				if(eConTents2 != null){ 
					resMap.put("content", tr.removeHTMLTag(getBRTagText(eConTents2.toString(), 2)));
				} else {
					resMap.put("exist", "");
					System.out.println("exist type 1");
				}
		
			} else{
				if(eConTents1.text().length() > 1){ 
					resMap.put("content", tr.removeHTMLTag(getBRTagText(eConTents1.toString(), 1)));
				} else {
					resMap.put("exist", "");
					System.out.println("exist type 2");
				}
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
//				
//			htmlFileWriter(path, fileName, buf);
			e.printStackTrace();
		}	
		
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
				System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			e.printStackTrace();
		}

		Document doc = Jsoup.parse(sb.toString());		
		Elements eTAList = null;
		Elements eDateList = null;
		try{
			
			eTAList = doc.getElementsByClass("several_post");
			eDateList = doc.getElementsByTag("a");
			
		} catch (Exception e){
			Date date = new Date();
			
			String path = "logs/";
			String fileName = "crawler_"+date.getYear()+date.getMonth()+date.getDay()+".txt";
				
			StringBuffer buf = new StringBuffer();
				
			buf.append("네이버 블로그 웹페이지 변경 또는 주소체계 변경으로 웹문서가 수집되지 않으므로 유지보수가 필요합니다.");
				
//			htmlFileWriter(path, fileName, buf);

		}
		
		
		int retVal = 0;
		if(eTAList.text().contains("건")){
			retVal = Integer.parseInt(eTAList.text().split(" ")[eTAList.text().split(" ").length-1].replace("건 ", ""));
		}
		
//		//System.out.println("SearchResult Count:\t"+retVal);
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
			System.out.println("오류가 발생했습니다."); 
			
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
				if(title.contains(keyword)){
					resMap.put(eDateList.toggleClass("href").get(i).attr("href"), "1");
				}
			}
		}	
		return resMap;
	}
	
	public BufferedReader crawler(String addr){
		HttpPost http = new HttpPost(addr);

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
			System.out.println("crawler\t오류가 발생했습니다."); 
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
			list.add(URLEncoder.encode(klist.get(i)));
		}
						
		return list;
	}
	
	public ArrayList<String> getKeywordList(){
		
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> klist = new ArrayList<String>();
		ArrayList<String> blist = new ArrayList<String>();
		
		Dictionary dic = new Dictionary();
//		klist = dic.getDBDicList("dic.txt");
		klist = dic.getDicList("dic.blog");
//		blist = dic.getDBDicList("best");
		int size = klist.size();
		boolean isStart = true;
		
//		for(int i = 0; i < blist.size() ; i++){
//			list.add(blist.get(i));
//		}
		for(int i = 0; i < size; i++){
			list.add(klist.get(i));
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
