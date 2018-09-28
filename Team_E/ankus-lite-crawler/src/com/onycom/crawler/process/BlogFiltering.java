package com.onycom.crawler.process;

import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;

public class BlogFiltering {
	public static void main(String[] args){
		CSVFileReaderToDEF cr = new CSVFileReaderToDEF();
		cr.setHeader("food", 0);
		cr.setHeader("date", 2);
		cr.setHeader("link", 3);
		cr.setHeader("title", 4);
		cr.setHeader("contents", 5);
		
		ArrayList<DocumentDEF> list = cr.getList("cb.txt", "\t", "");
		
		int size = list.size();
		
		ArrayList<String> prtListFood = new ArrayList<String>();
		ArrayList<String> prtList = new ArrayList<String>();
		
		
		
		NLP nlp = new NLP();
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> map = dic.getDicMap("dic.txt");
		
		System.out.println(map.size());
		for(int i = 0; i < size; i++) {
			boolean isFood = false;
			StringBuffer sb = new StringBuffer();
			DocumentDEF doc = list.get(i);
			sb.append(doc.food);
			sb.append("\t");
			sb.append(doc.date);
			sb.append("\t");
			sb.append(doc.link);
			sb.append("\t");
			sb.append(doc.title);
			sb.append("\t");
			
			String exBag = nlp.extractBag(doc.contents);
			String[] bagList = exBag.split("\t");
			
			int len = bagList.length;
			
			for(int j = 0 ; j < len ; j++){
				if(map.containsKey(bagList[j].split(":")[0])){
					isFood = true;
					break;
				}
			}
			
			
//			System.out.println(doc.contents);
//			System.out.println(nlp.extractBag(doc.contents));
			
			sb.append(exBag);
			
			if(isFood){
				prtListFood.add(sb.toString());
			} else{
				prtList.add(sb.toString());
			}
			
			if(i%10 == 0){
				System.out.println(sb.toString());
			}
//			break;
		}
		CSVFileWriter cw = new CSVFileWriter();
		cw.outputFile(prtListFood, "foodBlog.txt");
		cw.outputFile(prtList, "noFoodBlog.txt");
	}
}
