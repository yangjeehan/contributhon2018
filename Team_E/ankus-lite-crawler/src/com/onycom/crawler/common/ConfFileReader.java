package com.onycom.crawler.common;

import java.io.*;
import java.util.HashMap;

public class ConfFileReader {
	public HashMap<String, String> getConfFile(String name){
		FileReader fr;
		BufferedReader br;
		
		String line;
		try {
			fr = new FileReader(confMap.get(name));
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				confMap.put(line.split("\t")[0], line.split("\t")[1]);
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
		return confMap;
	}
	
	private HashMap<String, String> confMap;
	
	public ConfFileReader(){
		FileReader fr;
		BufferedReader br;
		
		String line;
		confMap = new HashMap<String, String>();
		try {
			fr = new FileReader("conf.conf");
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				confMap.put(line.split("\t")[0], line.split("\t")[1]);
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
	}
	
	public String getFileName(String name){
		String retVal = new String();
		if(name.equals("twitter")){
			retVal = "conf/twitter.conf";
		}
		return retVal;
	}
}
