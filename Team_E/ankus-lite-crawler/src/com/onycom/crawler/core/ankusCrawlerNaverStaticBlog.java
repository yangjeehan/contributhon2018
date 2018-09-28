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
import com.onycom.crawler.common.*;
import com.onycom.crawler.common.Dictionary;

public class ankusCrawlerNaverStaticBlog {
	int monthList[] = {31,28,31,30,31,30,31,31,30,31,30,31};

	
	public ArrayList<String> getBlogIdList(){
		DBConnect dbconn = new DBConnect();
		Connection conn = dbconn.getConnection();
		
		ArrayList<String> idList = new ArrayList<String>();
		try {
 			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT blogid FROM BLOG_LIST WHERE blogtype = 'naver' and crawling = 'Y'");
			
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
	
	NLP nlp;
	public void blogStaticCralwer(){
		//System.out.println("Static Blog Crawler Start...");
		nlp = new NLP();
		boolean isNext = true;
		
		ArrayList<String> idList = getBlogIdList();
		
		//System.out.println(idList.size());
		for(int i = 0; i < idList.size(); i++){
			String id = idList.get(i);
			int cnt = 1;
			//System.out.println(id +"blog crawling");
			
			while(isNext){
	//			//System.out.println(url);
				isNext = core(id, cnt);
				cnt++;
				waitDelay(2000,1500);
//				break;
			}		
			isNext = true;
		}
	}
	
	public boolean core(String id, int cnt){
		//"%EB%B0%B1%EC%88%98%EC%98%A4&term=period&option.startDate=2015-04-23&option.endDate=2015-04-23&option.page.currentPage=1&option.orderBy=sim";
		boolean retVal = false;
		String url = "http://blog.naver.com/PostList.nhn?from=menu&blogId="+id+"&currentPage="+cnt;

		//System.out.println(url);
//		Dictionary dic = new Dictionary();
//		HashMap<String, Integer> badMap = dic.getDBDic("bad.txt");
		BufferedReader br = crawler(url);
		
		StringBuffer postBuf = getPostOrigianalPage(br);
		
//		//System.out.println(postBuf.toString());
		
		HashMap<String, String> oriMap = new HashMap<String, String>();
		oriMap = getPostOrigianalPageInfo(postBuf);
		oriMap.put("provider", id);
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
			
		}
		return retVal;
	}
//		
//		for(int i = 0; i < keywordList.size() ; i++){
//			int totalDocSize = 0;
//			
//			fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+page+searchTypeAddr;
//			BufferedReader br = crawler(fullPath);
//			
//			totalDocSize = getSearchTotalCount(br, fullPath);	
//			
//			if(totalDocSize < 10){
//				page = 2;
//			}
//			if(totalDocSize%10 > 0){
//				page = totalDocSize/10 + 2;
//			} else {
//				page = totalDocSize/10 + 1;
//			}
//			HashMap<String, String> subAddrMap = new HashMap<String, String>();
//			 
			
			
//			for(int j = 1; j < page; j++){
//				fullPath = searchAddr + keywordList.get(i)+startAddr+startDate+endAddr+endDate+pageAddr+j+searchTypeAddr;
				
//				br = crawler(fullPath);
//				getContents(br, subAddrMap, fullPath, keywordListUTF.get(i));
//			}
//			
//			Object[] subAddrList = subAddrMap.keySet().toArray();
//			
//			//rdirect Page crawling
//			for(int j = 0; j < subAddrList.length; j++){
//				waitDelay(2000,1500);
//				
//				br = crawler((String)subAddrList[j]);
//				
//				String postAddr = getPostViewInfo(br);
//				
//				//original page
//				HashMap<String, String> oriMap = new HashMap<String, String>();
//				
//				br = crawler(blogSubString+postAddr);
//				StringBuffer postBuf = getPostOrigianalPage(br);
//				oriMap = getPostOrigianalPageInfo(postBuf);
//				oriMap.put("searchkeyword", keywordListUTF.get(i));
//				oriMap.put("date", startDate);
//				
//				if(!oriMap.containsKey("exist")){
//				
//					oriMap.put("httpLink", (String)subAddrList[j]);
//					String provider = ((String)subAddrList[j]).replace("http://blog.naver.com/", "").replace("?", ",").split(",")[0];
//					
//					oriMap.put("provider", provider);
//					
//					oriMap.put("fileLink", htmlFileWriter(fileMPath, "/"+keywordListUTF.get(i)+"/"+startDate+"/"+j+".html",postBuf));
//					
//					String[] keyList =nlp.extractBag(oriMap.get("title"), 1).split("\t"); 
//					int len = keyList.length;
//					boolean isCautions = false;
//					for(int k = 0; k < len ; k++){
//						//제목 
//						if(badMap.containsKey(keyList[k].split(":")[0])){
//							isCautions = true;
//							break;
//						}
//					}
//					if(isCautions){
//						oriMap.put("caution", "Y");
//					} else {
//						oriMap.put("caution", "N");
//					}
//					
//					//DBInput(oriMap);
//				}
//			}
//		}
		
		
	
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
			
			String pSql = "select count(*) from blog_info where caution ='E' and doc_sj = ? and blog_wrter = ?";
			stmt = conn.prepareStatement(pSql);
			
			stmt.setString(1, map.get("title"));
			stmt.setString(2, map.get("provider"));
			rs = stmt.executeQuery();
			
			rs.next();
			
			int count = rs.getInt(1);
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
				pstmt.setString(8, map.get("searchkeyword"));
				
				pstmt.setString(9, map.get("caution"));
				//caution information input
							
				pstmt.executeUpdate();
				
				pstmt.close();
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
			FileWriter fw = new FileWriter("static.html");
			BufferedWriter bw = new BufferedWriter(fw);
			
			while((line = br.readLine())!=null){
				sb.append(line+"\r\n");
			}
			bw.append(sb.toString());
			bw.flush();
			fw.flush();
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
		
		
		
		Elements eConTents1 = doc.getElementsByClass("se_textarea");
		Elements eDateList = doc.getElementsByClass("se_publishDate");
		
		resMap.put("date", eDateList.text().split(" ")[0]);
		
		if(eDateList.text().split(" ").length < 2){
			resMap.put("date", "0000-00-00");
		}
		
		Element eConTents2 = doc.getElementById("postViewArea");	
		
		
		Elements eTitleList = doc.getElementsByClass("se_title");

		resMap.put("title", tr.removeHTMLTag(eTitleList.text().replace(": 네이버 블로그", "")));
		if(eConTents1.text().length() < 1){
			if(eConTents2 != null){ 
				resMap.put("content", tr.removeHTMLTag(getBRTagText(eConTents2.toString(), 2)));
				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
			} else {
				resMap.put("exist", "");
			}
	
		} else{
			if(eConTents1.text().length() > 1){ 
				resMap.put("content", tr.removeHTMLTag(getBRTagText(eConTents1.toString(), 1)));
				resMap.put("Keywords", nlp.extractBag(resMap.get("content"),1));
			} else {
				resMap.put("exist", "");
			}
		}
		return resMap;
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
//			//System.out.println(line);
				sb.append(line+"\r\n");
			}
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}

		Document doc = Jsoup.parse(sb.toString());		
		Elements eTAList = doc.getElementsByClass("several_post");
		Elements eDateList = doc.getElementsByTag("a");
		
		int size = eDateList.toggleClass("href").size();
		
		
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
			System.out.println("오류가 발생했습니다."); 
			
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
