package com.onycom.crawler.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;

public class CSVFileReaderToDEF {
	public HashMap<String, Integer> indexMap;
	
	public void setHeader(String header, int index){
		indexMap.put(header, index);
	}
	
	public CSVFileReaderToDEF(){
		this.indexMap = new HashMap<String, Integer>();
	}
	
	public ArrayList<DocumentDEF> getList(String path, String delimiter, String keywordDelimiter){
		ArrayList<DocumentDEF> list = new ArrayList<DocumentDEF>();
		
		FileReader fr;
		BufferedReader br;
		
		String line;
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			HashMap<String, String> distinctMap = new HashMap<String, String>();
			
			Dictionary dic = new Dictionary();
			HashMap<String, Integer> badMap = dic.getDicMap("bad.txt");
			
			while((line = br.readLine())!=null){
				String[] lineList = line.split(delimiter);
				DocumentDEF doc = new DocumentDEF();
				if(indexMap.containsKey("food"))
					doc.food = lineList[indexMap.get("food")];
				if(indexMap.containsKey("classLabel"))
					doc.classLabel = lineList[indexMap.get("classLabel")];
				if(indexMap.containsKey("rno"))
					doc.rno = lineList[indexMap.get("rno")];
				if(indexMap.containsKey("date"))
					doc.date = lineList[indexMap.get("date")];
				if(indexMap.containsKey("title"))
					doc.title = lineList[indexMap.get("title")];
				if(indexMap.containsKey("title"))
					doc.titleList = lineList[indexMap.get("title")].split(keywordDelimiter);
				if(indexMap.containsKey("contents")){
					if(lineList.length > indexMap.get("contents")){
						doc.contents = lineList[indexMap.get("contents")];
					} else {
						doc.contents = "";
					}
				}
				if(indexMap.containsKey("link"))
					doc.link = lineList[indexMap.get("link")];
				if(indexMap.containsKey("keyStr"))
					doc.keyStr = lineList[indexMap.get("keyStr")];
				if(indexMap.containsKey("keyStr"))
					doc.keyList = lineList[indexMap.get("keyStr")].split(keywordDelimiter);
				if(indexMap.containsKey("keyList")){
					StringBuffer buf = new StringBuffer();
					for(int i = indexMap.get("keyList"); i < lineList.length ; i++){
						if(lineList[i].split(":")[0].length() > 1){
							buf.append(lineList[i].split(":")[0]);
							buf.append(",");
						}
					}
					if(buf.length()>0){
						String bufStr = buf.toString().substring(0,buf.toString().length()-1);
					
						doc.keyList = bufStr.split(",");
					}
				}
				if(indexMap.containsKey("bagList")){
					StringBuffer buf = new StringBuffer();
					for(int i = indexMap.get("bagList"); i < lineList.length -1 ; i++){
						if(badMap.containsKey(lineList[i].split(":")[0]) || lineList[i].split(":")[0].length() > 1){
							buf.append(lineList[i]);
							buf.append(",");
						}
					}
					if(buf.length()>0){
						String bufStr = buf.toString().substring(0,buf.toString().length()-1);
					
						doc.bagList = bufStr;
					}
				}
				if(indexMap.containsKey("keywords")){
					StringBuffer buf = new StringBuffer();
					for(int i = indexMap.get("keywords"); i < lineList.length-1 ; i++){

						if(badMap.containsKey(lineList[i].split(":")[0]) || lineList[i].split(":")[0].length() > 1){
							
							buf.append(lineList[i].split(":")[0]);
							buf.append(",");
						}
					}
					if(buf.length()>0){
						String bufStr = buf.toString().substring(0,buf.toString().length()-1);
						
						doc.keywords = bufStr.split(",");
					}
				}
				if(indexMap.containsKey("contentsLast")){
					doc.contents = lineList[lineList.length-1];
				}
				if(!distinctMap.containsKey(doc.title)){
					distinctMap.put(doc.title, "");
//					if(doc.bagList != null || doc.keywords != null || doc.keyList != null)
						
				}
//				if(doc.keyList != null)
					list.add(doc);
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("오류가 발생했습니다."); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("오류가 발생했습니다."); 
		}
		return list;
	}
}
