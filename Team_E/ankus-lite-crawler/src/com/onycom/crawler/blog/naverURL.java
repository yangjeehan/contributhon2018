package com.onycom.crawler.blog;

import java.net.URLEncoder;

public class naverURL {
	public String getURL(String keyword, String start, String end, int cnt){
		
		
		String mainURL = "https://search.naver.com/search.naver?";
		String etcURL = "date_option=8&dup_remove=1&ie=utf8&post_blogurl=blog.naver.com&srchby=all&st=date&where=post&";
		String startDate = "date_from="+start.replace("-", "")+"&";
		String endDate = "date_to="+end.replace("-", "")+"&";
		String query = "query="+keyword+"&";
		String maxPost = "start="+cnt;
		//
		String url = mainURL+startDate+endDate+query+etcURL+maxPost;
		
//		System.out.println(url);
		return url;
	}
}
