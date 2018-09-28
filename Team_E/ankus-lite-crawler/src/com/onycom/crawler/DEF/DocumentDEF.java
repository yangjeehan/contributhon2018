package com.onycom.crawler.DEF;
import java.util.HashMap;

import com.onycom.crawler.common.NLP;

public class DocumentDEF {
	public String date;
	public String rno;
	
	public String title;
	public String contents;
	
	public String[] keyList;
	public String[] titleList;
	public String[] badList;
	public String titleCList;
	
	public String link;
	
	public String keyStr;
	public String badStr;
	
	public String classLabel;
	public String keywords[];
	
	public String food;
	
	public double distance;
	public String bagList;
	
	public String toString(){
		StringBuffer prtBuf = new StringBuffer();
		
		if(date != null){
			prtBuf.append(date);
			prtBuf.append("\t");
		}
		if(title !=null){
			prtBuf.append(title);
			prtBuf.append("\t");
		}
		if(link != null){
			prtBuf.append(link);
			prtBuf.append("\t");
		}
//		if(contents != null){
//			prtBuf.append(contents);
//			prtBuf.append("\t");
//		}
		if(keyStr !=null){
			prtBuf.append(keyStr);
		}
		if(badStr !=null){
			prtBuf.append(badStr);
		}
	
		prtBuf.append(getList());
		return prtBuf.toString();
	}
	public String toStringBag2(){
		StringBuffer prtBuf = new StringBuffer();
		
		if(date != null){
			prtBuf.append(date);
			prtBuf.append("\t");
		}
		if(title !=null){
			prtBuf.append(title);
			prtBuf.append("\t");
		}
		if(link != null){
			prtBuf.append(link);
			prtBuf.append("\t");
		}
//		if(contents != null){
//			prtBuf.append(contents);
//			prtBuf.append("\t");
//		}
		if(keyList !=null){
			NLP nlp = new NLP();
			prtBuf.append(nlp.bagToStr(nlp.listToBag(keyList)));
		}
		if(badList !=null){
			NLP nlp = new NLP();
			prtBuf.append(nlp.bagToStr(nlp.listToBag(badList)));
		}
	
//		prtBuf.append(getList());
		return prtBuf.toString();
	}
	public String toStringBag(){
		StringBuffer prtBuf = new StringBuffer();
		
		if(date != null){
			prtBuf.append(date);
			prtBuf.append("\t");
		}
		if(title !=null){
			prtBuf.append(title);
			prtBuf.append("\t");
		}
		if(link != null){
			prtBuf.append(link);
			prtBuf.append("\t");
		}
//		if(contents != null){
//			prtBuf.append(contents);
//			prtBuf.append("\t");
//		}
		if(badList !=null){
			NLP nlp = new NLP();
			prtBuf.append(nlp.bagToStr(nlp.listToBag(badList)));
		}
	
//		prtBuf.append(getList());
		return prtBuf.toString();
	}
	
	public String toStringExceptKeyword(){
		StringBuffer prtBuf = new StringBuffer();
		
		if(date != null){
			prtBuf.append(date);
			prtBuf.append("\t");
		}
		if(title !=null){
			prtBuf.append(title);
			prtBuf.append("\t");
		}
		if(link != null){
			prtBuf.append(link);
			prtBuf.append("\t");
		}
//		if(contents != null){
//			prtBuf.append(contents);
//			prtBuf.append("\t");
//		}
		
	
//		prtBuf.append(getList());
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
