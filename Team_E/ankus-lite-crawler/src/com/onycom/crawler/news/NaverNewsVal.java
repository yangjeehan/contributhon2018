package com.onycom.crawler.news;

public class NaverNewsVal {
	public String list1 = "nclicks(cnt_flashart)";
	
	public String contents1 = "t11";
	public String contents2 = "articleBodyContents";
	public String contents3 = "articleTitle";
	
	public String insertQeuery = "INSERT INTO news(nsite, rdate, url, title, content, path) VALUES (?,?,?,?,?,?)";
	public String countSql = "select count(*) from news where title = ?";
	public String kwrdInput = "Insert into rel_word(nid, word, cnt) values(?,?,?)";

	public String oidSql = "SELECT [nsite], [id] FROM newssite WHERE [ENABLE] = 'Y';";
}
