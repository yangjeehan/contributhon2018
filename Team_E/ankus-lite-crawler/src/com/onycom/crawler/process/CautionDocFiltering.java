package com.onycom.crawler.process;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.collections.bag.HashBag;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.Dictionary;

public class CautionDocFiltering {
	public static void main(String[] args){
		CautionDocFiltering cdf = new CautionDocFiltering();
		cdf.core();
	}
	
	public void core(){
		CSVFileReaderToDEF cr = new CSVFileReaderToDEF();
		cr.setHeader("date", 0);
		cr.setHeader("title", 1);
		cr.setHeader("keyList", 2);
		
		int cnt = 0;
		ArrayList<DocumentDEF> list = cr.getList("news-sbsnews-Caution.txt", "\t", "\t");
		
		int size = list.size();
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> titleMap = new HashMap<String, Integer>();
		
		HashMap<String, Integer> map = dic.getDicMap("dic.txt");
		HashMap<String, Integer> badMap = dic.getDicMap("bad.txt");
		ArrayList<String> addList = dic.getDicList("add.txt");
		ArrayList<String> blockList = dic.getDicList("block.txt");
		HashMap<String, String> joinMap = dic.getJoinDicMap("join.txt");
		Object joinList[] = joinMap.keySet().toArray();
		HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
		
		for(int i =0; i < size ; i++){
			if(!titleMap.containsKey(list.get(i).title)){
				titleMap.put(list.get(i).title, 1);
				String[] keyList = list.get(i).keyList;
				ArrayList<String> keyFilterList = new ArrayList<String>();
				keyMap = new HashMap<String, Integer>();
				for(int j = 0 ; j < keyList.length; j++){
					if(keyList[j].length() > 1 && (map.containsKey(keyList[j]))){
						keyFilterList.add(keyList[j]);
						keyMap.put(keyList[j], 1);
					}
				}
				
				Object[] keyMapList = keyMap.keySet().toArray();
				HashBag keyBag = new HashBag();
				
				boolean isKeyInTitle = false;
				for(int j = 0; j < keyMapList.length; j++){
					if(list.get(i).title.contains((String)keyMapList[j])){
						isKeyInTitle = true;
					}
					keyBag.add(keyMapList[j]);
				}
				
				HashMap<String, String> badKeyMap = new HashMap<String, String>();
				
				for(int j = 0 ; j < keyList.length; j++){
					if(badMap.containsKey(keyList[j])){
						keyFilterList.add(keyList[j]);
						badKeyMap.put(keyList[j],"");
						keyMap.put(keyList[j], 1);
					}
				}
				
				Object[] badKeyList = badKeyMap.keySet().toArray();
				boolean isBadKeyInTitle = false;
				for(int j = 0; j < badKeyList.length ; j++){
					if(list.get(i).title.contains((String)badKeyList[j])){
						isBadKeyInTitle = true;
					}
				}
				
				
				boolean join = false;
				boolean joinReady = false;
				boolean joinResult = false;
				
				for(int j = 0 ; j < joinList.length; j++){
					if(keyMap.containsKey(joinList[j])){
						joinReady = true;
						
						if(keyMap.containsKey(joinMap.get(joinList[j]))){
							join = true;
						}
					}
				}
				boolean add = false;
				for(int j = 0; j < addList.size() ; j++){
					if(list.get(i).title.contains(addList.get(j))){
						add = true;
					}
				}
				boolean block = false;
				for(int j = 0; j < blockList.size() ; j++){
					if(list.get(i).title.contains(blockList.get(j))){
						block = true;
					}
				}
				if(joinReady){
					if(join){
						joinResult = true;
					}
				} else{
					joinResult = true;
				}
				
				int maxIdx = 0;
				int maxCnt = 0;
				if(isKeyInTitle && joinResult && !block && (keyFilterList.size() > 1 || add)){
					for(int j = 0; j < keyFilterList.size() ; j++){
						if(maxCnt < keyBag.getCount(keyFilterList.get(j))){
							maxCnt = keyBag.getCount(keyFilterList.get(j));
							maxIdx = j;
						}
					}
//					System.out.print(keyFilterList.get(maxIdx)+"\t");
					
					System.out.print(list.get(i).date+"\t");
					System.out.print(list.get(i).title+"\t");
					for(int j = 0; j < keyFilterList.size(); j++){
						System.out.print(keyFilterList.get(j)+"\t");
					}
					System.out.println();
					cnt++;
				}
				
//				if(isKeyInTitle && isBadKeyInTitle && (keyFilterList.size() > 1)){
//					for(int j = 0; j < keyFilterList.size() ; j++){
//						if(maxCnt < keyBag.getCount(keyFilterList.get(j))){
//							maxCnt = keyBag.getCount(keyFilterList.get(j));
//							maxIdx = j;
//						}
//					}
////					System.out.print(keyFilterList.get(maxIdx)+"\t");
//					
//					System.out.print(list.get(i).date+"\t");
//					System.out.print(list.get(i).title+"\t");
//					for(int j = 0; j < keyFilterList.size(); j++){
//						System.out.print(keyFilterList.get(j)+"\t");
//					}
//					System.out.println();
//					cnt++;
//				}
			}
			
		}
		System.out.println("Filtered List:\t"+cnt);
	}
}
