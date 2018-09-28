package com.onycom.crawler.common;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class conf {
	public HashMap<String, String> confFileReader(String cType){
		FileReader fr;
		BufferedReader br;
		
		String path = getPath(cType);
		
		String line = null;
		
		HashMap<String, String> cMap = new HashMap<String, String>();
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				cMap.put(line.split("\t")[0], line.split("\t")[1]);
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
		
		return cMap;
	}
	
	public String getPath(String cType){
		if(cType.equals("news")){
			return "news.conf";
		} else if(cType.equals("twitter")){
			return "twi.conf";
		} else if(cType.equals("shopping")){
			return "shop.conf";
		} else if(cType.equals("civil")){
			return "civil.conf";
		} else if(cType.equals("hdfs")){
			return "hdfs.conf";
		}
		
		return null;
	}
}
