package com.onycom.crawler.DEF;
import java.util.HashMap;

public class DOC_DEF {
	public String nid;	
	public String date;	
	public String title;
	public String contents;
	public String provider;
	public String food;
	
	public String[] keyList;
	public String[] titleList;
	public String titleCList;
	
	public String cClass;
	
	public String link;
	
	public String keyStr;
	
	public String toString(){
		StringBuffer prtBuf = new StringBuffer();
		prtBuf.append(date);
		prtBuf.append("\t");
		prtBuf.append(title);
		prtBuf.append("\t");
		prtBuf.append(contents);
		prtBuf.append("\t");
		prtBuf.append(getList());
		return prtBuf.toString();
	}
	
	public String getList(){
		StringBuffer prtBuf = new StringBuffer();
		int len = keyList.length;
		
		for(int i = 0; i < len ; i++){
			prtBuf.append(keyList[i]);
			prtBuf.append(",");
		}
		return prtBuf.toString();
	}
}
