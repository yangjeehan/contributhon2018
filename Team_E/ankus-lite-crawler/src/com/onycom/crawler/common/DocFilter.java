package com.onycom.crawler.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;

public class DocFilter {
	private ArrayList<DocumentDEF> mList;
	
	public DocFilter(ArrayList<DocumentDEF> list){
		this.mList = list;
	}
	
	public ArrayList<DocumentDEF> containsTitle(String str){
		return containsString(str, 0);
	}
	
	public ArrayList<DocumentDEF> containsContetns(String str){
		return containsString(str, 1);
	}
	
	private ArrayList<DocumentDEF> containsString(String str, int index){
		DocumentDEF doc = new DocumentDEF();
		
		ArrayList<DocumentDEF> retList = new ArrayList<DocumentDEF>();
		
		int size = mList.size();
		
		for(int i = 0; i < size ; i++){
			doc = mList.get(i);
			if(index == 0 && doc.title.contains(str)){
				retList.add(doc);
			} else if(index == 1 && doc.contents.contains(str)){
				
			}
		}
		return retList;
	}
	
	public ArrayList<DocumentDEF> containsKeyList(String str, HashMap<String, Integer> map, boolean isEtcInclude){
		DocumentDEF doc = new DocumentDEF();
		
		ArrayList<DocumentDEF> retList = new ArrayList<DocumentDEF>();
		
		int size = mList.size();
		
		for(int i = 0; i < size ; i++){
			doc = mList.get(i);
			int len = doc.keyList.length;
			
			StringBuffer buf = new StringBuffer();
			if(isEtcInclude){
				boolean isContains = false;
				for(int j = 0; j < len ; j++){
					if(map.containsKey(doc.keyList[j])){
						isContains = true;
						break;
					}
				}
				if(isContains){
					retList.add(doc);
				}
			} else {
				for(int j = 0; j < len ; j++){
					if(map.containsKey(doc.keyList[j])){
						buf.append(doc.keyList[j]);
						buf.append(",");
					}
				}
				buf.toString().substring(0,buf.toString().length()-1);
				doc.keyList = buf.toString().split(",");
				if(doc.keyList.length > 0){
					retList.add(doc);
				}
			}
		}
		return retList;
	}
}
