package com.onycom.crawler.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Best100 implements Runnable 
{
	int depth = 0;
	String url;
	String userid;
	String password;
	
	public Best100(int depth)
	{
		this.depth = depth;
	}
//	public HashMap<String, String> Best100_Food_Level1()
//	{
//		HashMap<String, String> items = new HashMap<String, String>();
//		try
//		{
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("rank_level1.csv"), Charset.forName("CP949"));
//			PrintWriter pw = new PrintWriter(outputStreamWriter);
//			pw.println("날짜,분류 수준1, 순위, 상품수");
//			String catetoryid = "50000006";
//			Document doc2 = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + catetoryid + "&kwdType=KWD").get();			
//			Elements elements_level2 = doc2.select("a._popular_srch_lst_rank");
//			int rank_idx = 1;
//			for(Element e2: elements_level2)
//			{
//				//System.out.println(e2.toString());
//				String itemName2 = e2.html();
//				
//				Document doc6_time = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=" + catetoryid).get();	
//				Elements elements_level6_time = doc6_time.select("p.ymd");
//				String time =  elements_level6_time.html();
//				
//				String address2 = "http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ catetoryid + "&query=" + itemName2+ "&page=1&kwdType=KWD";
//				Document doc3 = Jsoup.connect(address2).get();
//				Elements elements_level3 = doc3.select("div.tit_area p strong");
//				String EL3_Pure = elements_level3.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
//				//System.out.println(" Ranking:" + rank_idx + " Name: "+ itemName2 + " Count:" + EL3_Pure );
//				pw.println(time +","+itemName2 +","+ rank_idx +","+ EL3_Pure );
//				rank_idx++;
//			}	
//			
//			pw.close();
//		}
//		catch(Exception e)
//		{
//			
//		}
//		return items;
//	}
	
	 public void run()
	 {
		 HashMap<String, String> map = getDBInfo();
		 
//		 //System.out.println(map.get("url"));
		this.url = map.get("url");
		this.userid = map.get("id");
		this.password = map.get("password");
		Best100_Food_Level1();
		Best100_Food_Level3();
//		 while(true)
//		 {
//			 switch(depth)
//			 {
//			 case 1:
//				 
//				 try {
//					Thread.sleep(1*3600*1000);//1시간 마다 한번 수집.
//				} catch (InterruptedException e) {
//					System.out.println("오류가 발생했습니다."); 
//					
//				}
//				 break;
//			 case 3:
//				 
//				 try {
//					 Thread.sleep(1*3600*1000);//1시간 마다 한번 수집.
//				} catch (InterruptedException e) {
//					System.out.println("오류가 발생했습니다."); 
//					
//				}
//				 break;
//			 }		
//		 }
	 }
	public HashMap<String, String> Best100_Food_Level1()
	{
 	 	HashMap<String, String> items = new HashMap<String, String>();
		try
		{
			long timeFile = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
			String str_NowTime = dayTime.format(new Date(timeFile));
			
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("rank_level1_" + str_NowTime + ".csv", false), Charset.forName("CP949"));
//			PrintWriter pw = new PrintWriter(outputStreamWriter);
//			pw.println("날짜,분류 수준1, 상품수, 순위, 이동, 단계");
			String catetoryid = "50000006";
			Document doc2 = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + catetoryid + "&kwdType=KWD").get();
			Elements elements_level2 = doc2.select("ul.ranking_list");
		
			for(Element e2: elements_level2)
			{
				Document doc4 = Jsoup.parse(e2.toString());
				Elements elements_level4 = doc4.select("a._popular_srch_lst_rank");
				int itemIdx = 0;
				int rank_idx = 1;
				for(Element e5: elements_level4)
				{
					String itemName2 = e5.html();
					String  moving = doc4.select("span.vary").get(itemIdx).toString();
					String vary  ="";
					String vary_step = "";
					if(moving.indexOf("유지") > 0)
					{
						vary = "유지";
					}
					if(moving.indexOf("상승") > 0)
					{
						vary = "상승";
						String[] mv = moving.split("</span>");
						vary_step = mv[1];
					}
					if(moving.indexOf("하락") > 0)
					{
						vary = "하락";
						String[] mv = moving.split("</span>");
						vary_step = mv[1];
					}
					if(moving.indexOf("진입") > 0)
					{
						vary = "New";
						String[] mv = moving.split("</span>");
						vary_step = mv[1];
					}
					itemIdx++;
					String address2 = "http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ catetoryid + "&query=" + itemName2 + "&page=1&kwdType=KWD";
					Document doc3 = Jsoup.connect(address2).get();
					Elements elements_level3 = doc3.select("div.tit_area p strong");
					String EL3_Pure = elements_level3.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
					
					Document doc6_time = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=" + catetoryid).get();	
					Elements elements_level6_time = doc6_time.select("p.ymd");
					String time =  elements_level6_time.html();				
					if(vary_step.trim() == "")
					{
						vary_step = "-";
					}
//					//System.out.println("시간 : " + time +" 상품명 : "+itemName2 +" 순위 : "+ rank_idx  +" 이동:" + vary +" 단계: " + vary_step.trim() + " 갯수 :"+ EL3_Pure );
					//날짜,분류 수준1, 상품수, 순위, 이동, 단계
//					pw.println(time +","+itemName2 +","+ EL3_Pure +"," + rank_idx + "," + vary + ","+vary_step.trim());
					
					Connection conn = null;
		    		Statement stmt = null;
		    		String sql = "";
			    	try
			    	{
//			    		//System.out.println(url+"\t"+userid+"\t"+password);
						conn = DriverManager.getConnection(url,userid,password);
						stmt = conn.createStatement();
						//delete old Term Record
					    
					    
		            	sql = "INSERT INTO  NAVER_BEST_ITEM_DEPTH1 ( ";
		            	sql += "SCRAP_DATE, ";
		            	sql += "DEPTH1, ";
		            	sql += "ITEMCOUNT, ";
		            	sql += "RANK, ";
		            	sql += "MOVE, ";
		            	sql += "STAGE) ";
		            	sql += "values (";
		            	sql += "'" + time +"', ";
		            	sql += "'" + itemName2 +"', ";
		            	sql += "'" + EL3_Pure +"', ";
		            	sql += rank_idx +", ";
		            	sql += "'" + vary +"', ";
		            	sql += "'" + vary_step.trim() +"'";		            	
		            	sql += ")";
		            	
		            	stmt.executeUpdate(sql);
				            
					    stmt.close();
					    conn.close();
					}
			    	catch(Exception Except)
			    	{
			    		//System.out.println(sql);
			    		Except.printStackTrace();
			    		try
			    		{
			   	         if(stmt!=null)
			   	            conn.close();
			   	      	}
			    		catch(SQLException se)
			    		{
			    			 
			   	      	}// do nothing
			   	      	try
			   	      	{
			   	      		if(conn!=null)
			   	      			conn.close();
			   	      	}
			   	      	catch(SQLException se)
			   	      	{
			   	      		
			   	      	}//end finally try
			    	}
			    	
					rank_idx++;
				}
				
			}	
			
//			pw.close();
		}
		catch(Exception e)
		{
			//System.out.println(e.toString());
		}
		return items;
	}
	
	public HashMap<String, String> Best100_Food_Level2()
	{
		HashMap<String, String> items = new HashMap<String, String>();
		
		try
		{
//			PrintWriter pw = new PrintWriter("rank.csv");
//			pw.println("Item, Ranking, Name,Count");
			Document doc = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=50000006").get();
			Elements elements_level1 = doc.select("li.co_menu_food div.co_col div.co_cel strong");
			int itemCount =1;
			for(Element e: elements_level1)
			{
				String itemName = e.text().replace("전체보기", "");
				String href_url = e.html();
				String[] address = href_url.split(itemName);
				
				int catid_std = address[0].lastIndexOf("cat_id") + "cat_id".length()+1;
				int catid_etd = address[0].lastIndexOf("\">");
				String catetoryid =  address[0].substring(catid_std, catid_etd);
				//Popular Search
				Document doc2 = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + catetoryid + "&kwdType=KWD").get();			
				Elements elements_level2 = doc2.select("a._popular_srch_lst_rank");
				int rank_idx = 1;
				for(Element e2: elements_level2)
				{
					String itemName2 = e2.html();
					
				
					String address2 = "http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ catetoryid + "&query=" + itemName2+ "&page=1&kwdType=KWD";
					Document doc3 = Jsoup.connect(address2).get();
					Elements elements_level3 = doc3.select("div.tit_area p strong");
					String EL3_Pure = elements_level3.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
//					//System.out.println(" Item: " + itemName + " Ranking:" + rank_idx + " Name: "+ itemName2 + " Count:" + EL3_Pure );
//					pw.println(itemName + "," + rank_idx + ","+ itemName2 + "," + EL3_Pure );
					rank_idx++;
				}
				itemCount++;
			}
//			pw.close();
		}
		catch(Exception e)
		{
			
		}
		return items;
	}
	
	private String GetURLAddress(String taggedAddress)
	{
		String url = "";
		url = taggedAddress.replace("<a href=\"", "");
		url = url.replace("\">", "");
		url = url.replace("</a>", "");
		return url;
	}
	private String GetURLAddressWithVal(String taggedAddress, String Value)
	{
		String url = "";
		url = taggedAddress.replace(Value, "");
		url = url.replace("<a href=\"", "");
		url = url.replace("\">", "");
		url = url.replace("</a>", "");
		return url;
	}
	private String RemoveSpan(String taggedAddress)
	{
		String url = "";
		int start = taggedAddress.indexOf("<span");
		
		url = taggedAddress.substring(0, start);
		return url;
	}
//	public HashMap<String, String> Best100_Food_Level3()
//	{
//		HashMap<String, String> items = new HashMap<String, String>();
//		try
//		{
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("rank_full.csv"), Charset.forName("CP949"));
//			PrintWriter pw = new PrintWriter(outputStreamWriter);
//			pw.println("날짜 ,분류 수준1, 분류 수준2,분류 수준3, 분류 수준4, 상품수, 순위");
//			Document doc = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=50000006").get();
//			//Depth-0
//			Elements elements_level1 = doc.select("li.co_menu_food div.co_col div.co_cel strong");
//			int itemCount =1;
//			for(Element e: elements_level1)//Depth-1
//			{
//				String itemName = e.text().replace("전체보기", "");
//				String href_url = e.html();
//				String[] address = href_url.split(itemName);
//				
//				int catid_std = address[0].lastIndexOf("cat_id") + "cat_id".length()+1;
//				int catid_etd = address[0].lastIndexOf("\">");
//				String catetoryid =  address[0].substring(catid_std, catid_etd);
//				String nextUrl = GetURLAddress(address[0]);
//				Document doc2 = Jsoup.connect(nextUrl).get();//Depth-2
//				Elements elements_level2 = doc2.select("li.co_menu_food div.co_col div.co_cel");
//				for(Element e2: elements_level2)
//				{
//					String itemName2 = e2.html();
//					if(itemName2.indexOf(catetoryid) > 0) //Depth-3
//					{
//						Document doc3 = Jsoup.parse(itemName2);
//						Elements elements3 = doc3.select("ul li");				
//						for(Element e3: elements3)
//						{								
//							String e3Item = e3.select("a").text();
//							String e3URL = e3.select("a").toString();
//							e3URL = GetURLAddressWithVal(e3URL, e3Item);
//							String catid = e3URL.split("cat_id=")[1];
//							Document doc4 = Jsoup.connect(e3URL).get();//Depth-4							
//							Elements elements_level4 = doc4.select("div#_categorySummaryFilterArea");
//							Document doc5 = Jsoup.parse(elements_level4.toString());
//							Elements elements_level5 = doc5.select("li");
//							for(Element e5: elements_level5)
//							{		
//								int std = e5.html().indexOf("title")+"title=\"".length();
//								int etd = e5.html().indexOf("\"", std);
//								String title  = e5.html().substring(std, etd);	
//								std = e5.html().indexOf("data-filter-value")+"data-filter-value=\"".length();
//								etd = e5.html().indexOf("\"", std);
//								String targetID = e5.html().substring(std, etd);
//								
////								Popular Search
//								Document doc6_time = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=" + targetID).get();	
//								Elements elements_level6_time = doc6_time.select("p.ymd");
//								String time =  elements_level6_time.html();
//								String ItemCount = "";								
//								Document doc6 = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + targetID + "&kwdType=KWD").get();	
//								Elements elements_level6 = doc6.select("a._popular_srch_lst_rank");
//								
//								int rank_idx = 1;
//								for(Element e6: elements_level6)
//								{
//									String itemName6 = e6.html();
//									Document RankCountDoc = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ targetID + "&query="+ itemName6 +"&page=1&kwdType=KWD").get();	
//									Elements RankCountElement = RankCountDoc.select("div.tit_area p strong");
//									if(RankCountElement.size() > 0)
//									{
//										ItemCount = RankCountElement.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
//										pw.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName6 +","+ ItemCount + "," + rank_idx);
//										//System.out.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName6 +","+ ItemCount + "," + rank_idx);
//										pw.flush();
//									}
//									rank_idx++;
//								}
//							}
//						}
//					}					
//				}
//				itemCount++;
//			}
//			pw.close();
//		}
//		catch(Exception e)
//		{
//			//System.out.println(e.toString());
//		}
//		return items;
//	}
	public HashMap<String, String> Best100_Food_Level3()
	{
		HashMap<String, String> items = new HashMap<String, String>();
		try
		{
			long timeFile = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
			String str_NowTime = dayTime.format(new Date(timeFile));
			
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("rank_full_" + str_NowTime + ".csv", false), Charset.forName("CP949"));
//			PrintWriter pw = new PrintWriter(outputStreamWriter);
//			pw.println("날짜,분류 수준1,분류 수준2,분류 수준3,분류 수준4,상품수,순위,이동,단계");
			Document doc = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=50000006").get();
			//Depth-0
			Elements elements_level1 = doc.select("li.co_menu_food div.co_col div.co_cel strong");
			int itemCount =1;
			for(Element e: elements_level1)//Depth-1
			{
				String itemName = e.text().replace("전체보기", "");
				String href_url = e.html();
				String[] address = href_url.split(itemName);
				
				int catid_std = address[0].lastIndexOf("cat_id") + "cat_id".length()+1;
				int catid_etd = address[0].lastIndexOf("\">");
				String catetoryid =  address[0].substring(catid_std, catid_etd);
				String nextUrl = GetURLAddress(address[0]);
				Document doc2 = Jsoup.connect(nextUrl).get();//Depth-2
				Elements elements_level2 = doc2.select("li.co_menu_food div.co_col div.co_cel");
				for(Element e2: elements_level2)
				{
					String itemName2 = e2.html();
					if(itemName2.indexOf(catetoryid) > 0) //Depth-3
					{
						Document doc3 = Jsoup.parse(itemName2);
						Elements elements3 = doc3.select("ul li");				
						for(Element e3: elements3)
						{								
							String e3Item = e3.select("a").text();
							String e3URL = e3.select("a").toString();
							e3URL = GetURLAddressWithVal(e3URL, e3Item);
							String catid = e3URL.split("cat_id=")[1];
							Document doc4 = Jsoup.connect(e3URL).get();//Depth-4							
							Elements elements_level4 = doc4.select("div#_categorySummaryFilterArea");
							Document doc5 = Jsoup.parse(elements_level4.toString());
							Elements elements_level5 = doc5.select("li");
							for(Element e5: elements_level5)
							{		
								int std = e5.html().indexOf("title")+"title=\"".length();
								int etd = e5.html().indexOf("\"", std);
								String title  = e5.html().substring(std, etd);	
								std = e5.html().indexOf("data-filter-value")+"data-filter-value=\"".length();
								etd = e5.html().indexOf("\"", std);
								String targetID = e5.html().substring(std, etd);
								
//								Popular Search
								Document doc6_time = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=" + targetID).get();	
								Elements elements_level6_time = doc6_time.select("p.ymd");
								String time =  elements_level6_time.html();
								String ItemCount = "";								
								Document doc6 = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + targetID + "&kwdType=KWD").get();	
								Elements elements_level6 = doc6.select("ul.ranking_list");
								int rank_idx = 1;
								for(Element e7: elements_level6)
								{
									Document doc7 = Jsoup.parse(e7.toString());
									Elements elements_level8= doc7.select("a._popular_srch_lst_rank");
									int itemIdx = 0;
									for(Element e8: elements_level8)
									{
										String itemName3 = e8.html();
										
										if(doc7.select("span.vary").size() > itemIdx)
										{
											String  moving = doc7.select("span.vary").get(itemIdx).toString();
											String vary  ="";
											String vary_step = "";
											if(moving.indexOf("유지") > 0)
											{
												vary = "유지";
											}
											if(moving.indexOf("상승") > 0)
											{
												vary = "상승";
												String[] mv = moving.split("</span>");
												vary_step = mv[1];
											}
											if(moving.indexOf("하락") > 0)
											{
												vary = "하락";
												String[] mv = moving.split("</span>");
												vary_step = mv[1];
											}
											if(moving.indexOf("진입") > 0)
											{
												vary = "New";
												String[] mv = moving.split("</span>");
												vary_step = "-";
											}
											itemIdx++;
											String address2 = "http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ catetoryid + "&query=" + itemName3 + "&page=1&kwdType=KWD";
											Document doc8 = Jsoup.connect(address2).get();
											Elements elements_level9 = doc8.select("div.tit_area p strong");
											String EL3_Pure = "";
											if(elements_level9.size() > 0)
											{
												EL3_Pure = elements_level9.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
											}
											
	//										Document doc6_time = Jsoup.connect("http://shopping.naver.com/best100v2/detail.nhn?catId=" + catetoryid).get();	
	//										Elements elements_level6_time = doc6_time.select("p.ymd");
	//										String time =  elements_level6_time.html();				
											if(vary_step.length() == 0)
											{
												vary_step = "-";
											}
//											pw.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName3 +","+ EL3_Pure + "," + rank_idx +","+ vary + "," + vary_step);
//											//System.out.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName3 +","+ EL3_Pure + "," + rank_idx+"," + vary + "," + vary_step);

								    		Connection conn = null;
								    		Statement stmt = null;
								    		String sql = "";
									    	try
											{
//									    		HashMap<String, String> map = getDBInfo();
//									    		Class.forName(map.get("dirver"));
//									    		
//									    		this.url = map.get("url");
//									    		this.userid = map.get("id");
//									    		this.password = map.get("password");
									    		
												conn = DriverManager.getConnection(url,userid,password);
												stmt = conn.createStatement();
												//delete old Term Record
											    
								            	sql = "INSERT INTO  NAVER_BEST_ITEM_DEPTH4 ( ";
								            	sql += "SCRAP_DATE, ";
								            	sql += "DEPTH1, ";
								            	sql += "DEPTH2, ";
								            	sql += "DEPTH3, ";
								            	sql += "DEPTH4, ";
								            	sql += "ITEMCOUNT, ";
								            	sql += "RANK, ";
								            	sql += "MOVE, ";
								            	sql += "STAGE)";
								            	sql += "values (";
								            	sql += "'" + time +"', ";
								            	sql += "'" + itemName +"', ";
								            	sql += "'" + e3Item +"', ";
								            	sql += "'" + title +"', ";
								            	sql += "'" + itemName3 +"', ";
								            	sql += "'" + EL3_Pure +"', ";
								            	sql += rank_idx +", ";
								            	sql += "'" + vary +"', ";
								            	sql += "'" + vary_step +"'";
								            	sql += ")";
								            	
								            	stmt.executeUpdate(sql);
										            
											    stmt.close();
											    conn.close();
											}
									    	catch(Exception Except)
									    	{
									    		//System.out.println(sql);
									    		Except.printStackTrace();
									    		try
									    		{
									   	         if(stmt!=null)
									   	            conn.close();
									   	      	}
									    		catch(SQLException se)
									    		{
									    			 
									   	      	}// do nothing
									   	      	try
									   	      	{
									   	      		if(conn!=null)
									   	      			conn.close();
									   	      	}
									   	      	catch(SQLException se)
									   	      	{
									   	      		
									   	      	}//end finally try
									    	}
											
//											pw.flush();
											rank_idx++;
										}
									}
									
								}	
//								int rank_idx = 1;
//								for(Element e6: elements_level6)
//								{
//									String itemName6 = e6.html();
//									Document RankCountDoc = Jsoup.connect("http://shopping.naver.com/best100v2/detail/kwd/list.nhn?catId="+ targetID + "&query="+ itemName6 +"&page=1&kwdType=KWD").get();	
//									Elements RankCountElement = RankCountDoc.select("div.tit_area p strong");
//									if(RankCountElement.size() > 0)
//									{
//										ItemCount = RankCountElement.get(1).toString().replace("<strong>", "").replace("</strong>", "").replace(",", "");		
//										pw.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName6 +","+ ItemCount + "," + rank_idx);
//										//System.out.println(time + "," + itemName + ","+ e3Item  + "," +  title +"," +  itemName6 +","+ ItemCount + "," + rank_idx);
//										pw.flush();
//									}
//									rank_idx++;
//								}
							}
						}
					}					
				}
				itemCount++;
			}
//			pw.close();
		}
		catch(Exception e)
		{
			//System.out.println(e.toString());
		}
		return items;
	}
	
	public HashMap<String, String> getDBInfo(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		FileReader fr;
		BufferedReader br;
		
		String line = null;
		try {
			fr = new FileReader("CUBRIDDB.conf");
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line.split("\t")[0], line.split("\t")[1]);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return map;
	}
}

public class shop {
	public static HashMap<String, String> getDBInfo(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		FileReader fr;
		BufferedReader br;
		
		String line = null;
		try {
			fr = new FileReader("CUBRIDDB.conf");
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line.split("\t")[0], line.split("\t")[1]);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return map;
	}
	public static void crawlerShoppingBest(){
		HashMap<String, String> map = getDBInfo();
		
		try
		{
			Class.forName(map.get("driver"));
			String url = map.get("url");
			String userid =  map.get("id");
			String password =  map.get("password");
	//		connection pool setting.s
	//		NOT USE UNIQUE TERM EXTRACTION
			Connection conn = null;
			Statement stmt = null;
			try
			{
				conn = DriverManager.getConnection(url,userid,password);
				DatabaseMetaData dbm = conn.getMetaData();
				ResultSet tables = null;
				tables = dbm.getTables(null, null, "NAVER_BEST_ITEM_DEPTH4", null);
				if (tables.next()) 
				{
					//System.out.println("Table Exist");
				}
				else 
				{
					//날짜, 분류 수준1,분류 수준2,분류 수준3,분류 수준4,상품수,순위,이동,단계
					stmt = conn.createStatement();
				    String sql = "CREATE TABLE NAVER_BEST_ITEM_DEPTH4 ("; 
				    sql += "SCRAP_DATE DATE, ";
				    sql += "DEPTH1 VARCHAR(255), ";
				    sql += "DEPTH2 VARCHAR(255), ";
				    sql += "DEPTH3 VARCHAR(255), ";
				    sql += "DEPTH4 VARCHAR(255), ";
				    sql += "ITEMCOUNT BIGINT, ";
				    sql += "RANK INT, ";
				    sql += "MOVE  VARCHAR(255), ";
				    sql += "STAGE  VARCHAR(255) ";
				    sql += ")";
				    stmt.executeUpdate(sql);	
				}
				
				tables = dbm.getTables(null, null, "NAVER_BEST_ITEM_DEPTH1", null);
				if (tables.next()) 
				{
					//System.out.println("Table Exist");
				}
				else 
				{
					//날짜,분류 수준1, 상품수, 순위, 이동, 단계"
					stmt = conn.createStatement();
				    String sql = "CREATE TABLE NAVER_BEST_ITEM_DEPTH1 ("; 
				    sql += "SCRAP_DATE DATE, ";
				    sql += "DEPTH1 VARCHAR(255), ";
				    sql += "ITEMCOUNT BIGINT, ";
				    sql += "RANK INT, ";
				    sql += "MOVE  VARCHAR(255), ";
				    sql += "STAGE  VARCHAR(255) ";
				    sql += ")";
				    stmt.executeUpdate(sql);	
				}
		   }
			catch(Exception e)
			{
		      
		   }
			finally
			{
		      try{
		         if(stmt!=null)
		            conn.close();
		      }catch(SQLException se){
		      }// do nothing
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         
		      }//end finally try
		   }//end try
		}
		catch(Exception e)
		{
			
		}
		Thread thread_depth1 = new Thread(new  Best100(1));
		//System.out.println("Depth1 Start");
		thread_depth1.start();
		
		Thread thread_depth3 = new Thread(new  Best100(3));
		//System.out.println("Depth3 Start");
		thread_depth3.start();
	}
	
}
