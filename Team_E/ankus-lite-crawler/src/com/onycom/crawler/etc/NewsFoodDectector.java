package com.onycom.crawler.etc;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections.bag.HashBag;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;

public class NewsFoodDectector {
	
	public static void main(String[] args){
		NewsFoodDectector nfd = new NewsFoodDectector();
		nfd.core();
	}
	public void core(){
		CSVFileReaderToDEF crtd = new CSVFileReaderToDEF();
		crtd.setHeader("date", 0);
		crtd.setHeader("title", 1);
		crtd.setHeader("contents", 2);
		crtd.setHeader("link", 3);
		
		crtd.setHeader("keyList", 4);
		
		String path = "news-csvmerge-Caution.txt";
		
		ArrayList<DocumentDEF> list = crtd.getList(path, "\t", ",");
		
		int size = list.size();
		ArrayList<String> prtList = new ArrayList<String>();
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> map = dic.getDicMap("dic.txt");
		
		for(int i =0 ; i< size; i++){
			
			DocumentDEF doc = list.get(i);
			String[] keyList = doc.keyList;
			int len = keyList.length;
			HashBag bag = new HashBag();
			
			for(int j = 0; j < len ; j++){
				if(map.containsKey(keyList[j])){
					bag.add(keyList[j]);
				}
			}
			
			int max = Integer.MIN_VALUE;
			int maxid = -1;
			
			for(int j = 0; j < len ; j++){
				if(max < bag.getCount(keyList[j])){
					max = bag.getCount(keyList[j]);
					maxid = j;
				}
			}
			if(maxid != -1){
				StringBuffer buf = new StringBuffer();
				buf.append(keyList[maxid]);
				buf.append("\t");
				buf.append(doc.date);
				buf.append("\t");
				buf.append(doc.title);
				buf.append("\t");
				buf.append(doc.link);
				
				buf.append("\t");
				buf.append(doc.toStringBag2());
				
				buf.append("\t");
				buf.append(doc.contents);
				
				prtList.add(buf.toString());
			}
			
		}
		
		CSVFileWriter cfw = new CSVFileWriter();
		
		cfw.outputFile(prtList, "foodDectectnews-kbs.txt");
	}
}
