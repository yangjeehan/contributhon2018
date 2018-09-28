package com.onycom.crawler.blog;

public class NaverBlogVal {
	public String listPage1 = "//*[@id='main_pack']/div[1]/div/span";
	public String listPage2 = "//*[@id='main_pack']/div[1]/div/span";
	public String listPage4 = "//*[@id='main_pack']/div[3]/div/span";
	
	public String getListPage3(int i){
		return "//*[@id='sp_blog_"+i+"']/dl";
	}
	
	public String getListPage5(int i){
		return "//*[@id='sp_blog_"+i+"']/dl";
	}
	
	
	public String post1 = "se_textarea";
	public String post2 = "se_publishDate";
	public String post3 = "htitle";
	public String post4 = "meta";	
	public String post5 = "og:title";
	public String post6 = "postViewArea";

	
}
