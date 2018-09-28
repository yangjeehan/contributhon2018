package com.onycom.crawler.core;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import com.onycom.crawler.DB.*;
import com.onycom.crawler.common.ConfFileReader;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;
import com.onycom.crawler.common.YesterDayGenerate;

public class ankusCrawlerTwit {
	public String ACCESSTOKEN;
	public String ACCESSSECRET;
	public String CONSUMERTOKEN;
	public String CONSUMERSECRET;
		
	private DBConnect dbConn;
	private int day;
	public void cralwerTwit(int dueDate){
		long stime = System.currentTimeMillis();
		DBConnect dbConn = new DBConnect();
		this.day = dueDate;
		ankusCrawlerTwit act = new ankusCrawlerTwit();
		
		Dictionary dic = new Dictionary();
		ArrayList<String> list = dic.getDicList("dic.txt");
		
		int size = list.size();
		
		for(int i = 0; i < size ; i++){
			act.getTwit(list.get(i), dbConn);
			if(act.minus == -1){
				i--;
				act.minus = 0;
			}
			if(act.accountNo > 2){
				break;
			}
		}
		
		//System.out.println("running Time:\t"+(System.currentTimeMillis()-stime));
	}
	
	
	private int minus = 0;
	
	int accountNo = 1;
	public void getTwit(String Query, DBConnect dbConn){
		 try {
			 TwitterStreamBuilderUtil tsb = new TwitterStreamBuilderUtil(accountNo);
			 Twitter twitter = tsb.getAccount();
		        Query q = new Query();
		        
		        
		        q.setQuery("\""+Query+"\"");
		        YesterDayGenerate ydg = new YesterDayGenerate();
		        q.setSince(ydg.getYesterDay(day));
//		        //System.out.println(q.RECENT);
//		        q.until("2011-09-25");
		        q.setCount(100);
		        QueryResult qr = twitter.search(q);
//		        
//		        Paging page = new Paging (1, 50);
		        //System.out.println(qr.getQuery());
		        
		        List<Status> list = qr.getTweets();
		        
//		        List<Status> list = twitter.getUserTimeline(page);
		        HashMap<Integer, String> map = new HashMap<Integer, String>();
		        
		        for(Status status : list) {
//		        	//System.out.println("Link:"+"https://www.twitter.com/"+status.getUser().getScreenName()+"/status/"+status.getId());
//		            //System.out.println("ID:"+status.getUser().getScreenName());
//		            //System.out.println("Date:"+status.getCreatedAt().toString());
//		            //System.out.println("Twitter:"+status.getText());
		            
		            NLP nlp = new NLP();
		            
		            Date date = status.getCreatedAt();
		            
		            Timestamp ts = new Timestamp(date.getTime());

		            String pSql = "select count(*) from twitter where contents = ?";
		            PreparedStatement stmt = dbConn.conn.prepareStatement(pSql);
		            stmt.setString(1, status.getText());
					ResultSet rs = stmt.executeQuery();
					
					rs.next();
					
					int count = rs.getInt(1);
					//System.out.println("중복카운트:\t"+count);
					if(count == 0){
			            String sql = "INSERT INTO twitter (gendate, provider, orilink, contents, keywords, searchkeyword) VALUES (?,?,?,?,?,?);";
			            PreparedStatement pstmt = dbConn.conn.prepareStatement(sql);
			            
			            pstmt.setTimestamp(1, ts);
			            pstmt.setString(2, status.getUser().getScreenName());
			            pstmt.setString(3, "https://www.twitter.com/"+status.getUser().getScreenName()+"/status/"+status.getId());
			            pstmt.setString(4, status.getText());
			            pstmt.setString(5, nlp.extractNoun(status.getText()));
			            pstmt.setString(6, Query);
			            
			            dbConn.DBInput(pstmt);
					}
					rs.close();
					stmt.close();
					
		        }
		        dbConn.conn.close();
		    } catch (Exception e) {
		    	accountNo++;
		        
		        minus = -1;
//		        try {
//					Thread.sleep(600000);
//				} catch (InterruptedException e1) {
//					System.out.println("오류가 발생했습니다."); 
//					e1.printStackTrace();
//				}
		    }
	}
}
