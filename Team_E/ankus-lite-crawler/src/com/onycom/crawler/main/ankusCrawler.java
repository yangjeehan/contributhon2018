package com.onycom.crawler.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import com.onycom.crawler.blog.ankusCrawlerNaverBlog;
import com.onycom.crawler.classifier.NewsDetector;
import com.onycom.crawler.core.*;
import com.onycom.crawler.news.ankusCrawlerNaverNews;

public class ankusCrawler {
	public static void main(String[] args){
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 

		long sTime = System.currentTimeMillis();
		int dueDate = Integer.parseInt(args[1]);
		try {
			FileWriter fw = new FileWriter("logs/"+sTime+"-time"+args[0]+".txt");
			BufferedWriter bw = new BufferedWriter(fw);
			
			if(args[0].equals("NEWS")){		
				ankusCrawlerNaverNews news = new ankusCrawlerNaverNews();
//				if(dueDate < 0){
//					NewsDetector ndc = new NewsDetector();
//					ndc.detectDuartion((-1 * dueDate), Integer.parseInt(args[2]));
//				} else {
//					
//				}
				news.newsCrawler(dueDate, Integer.parseInt(args[2]));
				
			} else if(args[0].equals("BLOG")){
				ankusCrawlerNaverBlog blog = new ankusCrawlerNaverBlog();
				
				if(dueDate < 0){
//					blog.detectBlog(-1 * dueDate, Integer.parseInt(args[2]));;
				} else {
					blog.blogCralwer(dueDate, Integer.parseInt(args[2]));
//					ankusCrawlerNaverStaticBlog cb = new ankusCrawlerNaverStaticBlog();
//					cb.blogStaticCralwer();
				}
				
			} else if(args[0].equals("BLOGCAUTION")){
				
				ankusCrawlerNaverBlogCaution blog = new ankusCrawlerNaverBlogCaution();
				if(dueDate < 0){
					blog.detectBlog(-1 * dueDate, Integer.parseInt(args[2]));;
				} else {
					blog.blogCralwer(dueDate, Integer.parseInt(args[2]));
				}
			} else if(args[0].equals("BLOGSTATIC")){ 
					ankusCrawlerNaverStaticBlog cb = new ankusCrawlerNaverStaticBlog();
					cb.blogStaticCralwer();
					
					
			} 
			else if(args[0].equals("BLOG2")){
				ankusCrawlerDaumBlog blog = new ankusCrawlerDaumBlog();
				
				if(dueDate < 0){
					blog.detectBlog(-1 * dueDate, Integer.parseInt(args[2]));;
				} else {
					blog.blogCralwer(dueDate, Integer.parseInt(args[2]));
					ankusCrawlerDaumStaticBlog cb = new ankusCrawlerDaumStaticBlog();
					cb.blogStaticCralwer();
				}
				
			} else if(args[0].equals("BLOGCAUTION2")){
				ankusCrawlerDaumBlogCaution blog = new ankusCrawlerDaumBlogCaution();
				
				
				if(dueDate < 0){
					blog.detectBlog(-1 * dueDate, Integer.parseInt(args[2]));;
				} else {
					blog.blogCralwer(dueDate, Integer.parseInt(args[2]));
				}
				
				
				
			} else if(args[0].equals("BLOGSTATIC2")){
					ankusCrawlerDaumStaticBlog cb = new ankusCrawlerDaumStaticBlog();
					cb.blogStaticCralwer();
			}  else if(args[0].equals("CIVIL")){
				ankusCrawlerCivil civil = new ankusCrawlerCivil();
				civil.civilCrawler();
			} else if(args[0].equals("SHOPPING")){
				ankusCrawlerNaverShopping shopping = new ankusCrawlerNaverShopping();
				shopping.shoppingCralwer();
			} else if(args[0].equals("SHOPPINGBEST")){
				shop shopping = new shop();
				shopping.crawlerShoppingBest();
			} else if(args[0].equals("TWITTER")){
				ankusCrawlerTwit twit = new ankusCrawlerTwit();
				twit.cralwerTwit(dueDate);
			}
			long durTime = System.currentTimeMillis() - sTime;
			bw.append(durTime+"");
			
			bw.flush();
			fw.flush();
			
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		
		
		
	}
}
