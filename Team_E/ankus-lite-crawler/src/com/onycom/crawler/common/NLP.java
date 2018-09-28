package com.onycom.crawler.common;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.bag.HashBag;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;

import kr.co.shineware.nlp.komoran.core.analyzer.*;
import kr.co.shineware.util.common.model.Pair;

public class NLP {
	private String userDic = "model/dic.user";
	private String fwDic = "model/fwdic.user";
	private String modelPath = "model/";
	
	public String extractDic(String line, int type){
		StringBuffer buf = new StringBuffer();
		Komoran komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
		komoran.setFWDic(fwDic);
		List<List<Pair<String,String>>> result = komoran.analyze(line);
		
		//
		boolean setComma = false;
		for (List<Pair<String, String>> eojeolResult : result) {
			setComma = false;
			for (Pair<String, String> wordMorph : eojeolResult) {
//				if(wordMorph.getSecond().equals("NNG") || wordMorph.getSecond().equals("NNP") || wordMorph.getSecond().equals("NNB")
//						|| wordMorph.getSecond().equals("NP")){
//					buf.append(wordMorph.getFirst());
//					buf.append(",");
//				}
				if(type == 0){
					if(dicMap.containsKey(wordMorph.getFirst())){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					}
				} else if (type == 1){
					if(badMap.containsKey(wordMorph.getFirst())){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					}
				}
				
			}
		}
		String retStr = buf.toString();
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr;
		}
	}
	
	public void listToFile(ArrayList<DocumentDEF> list, String path, int type){
		int size = list.size();
		
		FileWriter fw;
		BufferedWriter bw;
		
		try {
			fw = new FileWriter(path); 
			bw = new BufferedWriter(fw);
			
			for(int i = 0; i < size; i++){
				if(list.get(i).keyList.length > 0){
					boolean isFw = false;
					for(int j = 0; j < list.get(i).titleList.length ; j++){
						if(this.dicMap.containsKey(list.get(i).titleList[j])){
							isFw = true;
							break;
						}
					}
					
					if(isFw){
						bw.append(printNews(list.get(i),type));
						bw.append("\r\n");
						bw.flush();
						fw.flush();
					}
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			
		}
		
	}
	
	public String printNews(DocumentDEF news, int type){
		StringBuffer buf = new StringBuffer();
		
		if( type == 1){
			buf.append(news.date);
			buf.append("\t");
			buf.append(news.date);
			buf.append("\t");
			buf.append(news.title);
			buf.append("\t");
			buf.append(bagToStr(listToBag(news.keyList)));
		} else if ( type == 2){
			buf.append(news.date);
			buf.append("\t");
			buf.append(news.date);
			buf.append("\t");
			buf.append(news.title);
			buf.append("\t");
			buf.append(news.link);
			buf.append("\t");
			buf.append(bagToStr(listToBag(news.keyList)));
		}
		return buf.toString();
	}
	public String bagToStr(HashBag bag){
		Object keyList[] = bag.uniqueSet().toArray();
		StringBuffer buf = new StringBuffer();
		
		for( int i = 0; i < keyList.length ;i++){
			buf.append(keyList[i]);
			buf.append(":");
			buf.append(bag.getCount(keyList[i]));
			buf.append("\t");
		}
		return buf.toString();
	}
	
	public HashBag listToBag(String[] list){
		int len = list.length;
		HashBag bag = new HashBag();
		for(int i = 0 ; i < len; i++){
			bag.add(list[i]);
		}
		return bag;
	}
	
	public ArrayList<DocumentDEF> convRStoList(ResultSet rs, int i){
		ArrayList<DocumentDEF> list = new ArrayList<DocumentDEF>();
		
		int cnt = 0;
		try {
			long stime = System.currentTimeMillis();
			
			HashMap<String, Integer> dicMap = getDic("dic.txt");
			HashMap<String, Integer> badMap = getDic("bad.txt");
			
			while(rs.next()){
				
				DocumentDEF news = new DocumentDEF();
				if( i == 1) {
					news.date = rs.getString(1);
					news.title = rs.getString(2);
					news.contents = rs.getString(3);
				} else if( i == 2){
					news.date = rs.getString(1);
					news.title = rs.getString(2);
					news.contents = rs.getString(4);
					news.link = rs.getString(3);
				}
				
				
				String extranNoun = extractNoun(news.contents);
				String extractTitle = extractNoun(news.title);
				news.titleList = extractTitle.split(",");
				news.keyList = extranNoun.split(",");
				news.keyStr =extranNoun;
				
				list.add(news);
				if(cnt%100 == 0){
					stime = System.currentTimeMillis();
				}
				cnt++;
			}
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		return list;
	}
	
	private Connection conn;
	public ResultSet getDB(String q){
		DBConnect db = new DBConnect();

		
		Statement stmt;
		ResultSet rs = null;

		try {
			if(conn == null || conn.isClosed()){
				conn = db.getConnection();
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery(q);
			
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
		return rs;
	}
	
	public HashMap<String, Integer> getDic(String path){
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		FileReader fr;
		BufferedReader br;
		
		String line;
		
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			while((line = br.readLine())!=null){
				map.put(line, 1);
			}
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.out.println("오류가 발생했습니다."); 
			
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		return map;
	}
	public NLP(){
		Dictionary dic = new Dictionary();
		
		this.dicMap = dic.getDicMap("dic.news");
//		this.badMap = dic.getDicMap("bad.txt");
//		this.simMap = dic.getDicMap("sim");
		
		ArrayList<String> dicList = dic.getDicList("dic.news");
//		ArrayList<String> badList = dic.getDicList("bad.txt");
//		ArrayList<String> bestList = dic.getDicList("best");
//		ArrayList<String> etcList = dic.getDicList("etc.txt");
		
		try {
			FileWriter fw = new FileWriter(userDic);
			BufferedWriter bw = new BufferedWriter(fw);

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for(int i = 0; i < dicList.size(); i++){
				if(dicList.get(i) != null && dicList.get(i).length() > 0 && !map.containsKey(dicList.get(i))){
					bw.append(dicList.get(i));
					bw.append("\t");
					bw.append("NNP");
					bw.append("\r\n");
					
					map.put(dicList.get(i), 1);
				}
				
			}
			bw.flush();
//			for(int i = 0; i < badList.size(); i++){
//				bw.append(badList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			bw.flush();
//			for(int i = 0; i < etcList.size(); i++){
//				bw.append(etcList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			bw.flush();
//			for(int i = 0; i < bestList.size(); i++){
//				bw.append(bestList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			
//			bw.flush();
			fw.flush();
			
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("NLP-오류가 발생했습니다."); 
			
		}
		komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
	}
	
	private Komoran komoran; 
	
	public NLP(String path){
		Dictionary dic = new Dictionary();
		
		this.dicMap = dic.getDicMap(path);
//		this.badMap = dic.getDicMap("bad.txt");
//		this.simMap = dic.getDicMap("sim");
		
		ArrayList<String> dicList = dic.getDicList(path);
//		ArrayList<String> badList = dic.getDicList("bad.txt");
//		ArrayList<String> bestList = dic.getDicList("best");
//		ArrayList<String> etcList = dic.getDicList("etc.txt");
		
		try {
			FileWriter fw = new FileWriter(userDic);
			BufferedWriter bw = new BufferedWriter(fw);

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for(int i = 0; i < dicList.size(); i++){
				if(dicList.get(i) != null && dicList.get(i).length() > 0 && !map.containsKey(dicList.get(i))){
					bw.append(dicList.get(i));
					bw.append("\t");
					bw.append("NNP");
					bw.append("\r\n");
					
					map.put(dicList.get(i), 1);
				}
				
			}
			bw.flush();
//			for(int i = 0; i < badList.size(); i++){
//				bw.append(badList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			bw.flush();
//			for(int i = 0; i < etcList.size(); i++){
//				bw.append(etcList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			bw.flush();
//			for(int i = 0; i < bestList.size(); i++){
//				bw.append(bestList.get(i));
//				bw.append("\t");
//				bw.append("NNP");
//				bw.append("\r\n");
//			}
//			
//			bw.flush();
			fw.flush();
			
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("NLP-오류가 발생했습니다."); 
			
		}
		
	}
	

	public HashMap<String, String> simMap;
	public HashMap<String, Integer> dicMap;
	public HashMap<String, Integer> badMap;
	
	public String extractDic(String line){
		StringBuffer buf = new StringBuffer();
		Komoran komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
//		komoran.setFWDic(fwDic);
		List<List<Pair<String,String>>> result = komoran.analyze(line);
		
		//
		for (List<Pair<String, String>> eojeolResult : result) {
			for (Pair<String, String> wordMorph : eojeolResult) {
				if(wordMorph.getSecond().equals("NNG") || wordMorph.getSecond().equals("NNP") || wordMorph.getSecond().equals("NNB")
						|| wordMorph.getSecond().equals("NP")){
					if(dicMap.containsKey(wordMorph.getFirst()) || badMap.containsKey(wordMorph.getFirst())){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					}
				}
			}		
		}
		String retStr = buf.toString();
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr.substring(0, retStr.length()-1);
		}
	}
	
	public String extractNoun(String line){
		StringBuffer buf = new StringBuffer();
		
//		komoran.setFWDic(fwDic);
		String retStr = new String();
		if(line.length() > 0 ){
			List<List<Pair<String,String>>> result = komoran.analyze(line);
			
			//
			boolean setComma = false;
			for (List<Pair<String, String>> eojeolResult : result) {
				setComma = false;
				for (Pair<String, String> wordMorph : eojeolResult) {
					if(wordMorph.getSecond().equals("NNP") ||wordMorph.getSecond().equals("NNG") ||  wordMorph.getSecond().equals("NNB")
							|| wordMorph.getSecond().equals("NP")){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					}
				}
			}
			retStr = buf.toString();
		}
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr.substring(0, retStr.length()-1);
		}
	}
	public String extractNoun(String line, int type){
		StringBuffer buf = new StringBuffer();
		Komoran komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
		komoran.setFWDic(fwDic);
		List<List<Pair<String,String>>> result = komoran.analyze(line);
		
		//
		boolean setComma = false;
		HashMap<String, Integer> tmpMap = new HashMap<String, Integer>();
		for (List<Pair<String, String>> eojeolResult : result) {
			setComma = false;
			for (Pair<String, String> wordMorph : eojeolResult) {
				if(wordMorph.getSecond().equals("NNP") ||wordMorph.getSecond().equals("NNG") ||  wordMorph.getSecond().equals("NNB")
						|| wordMorph.getSecond().equals("NP")){
					if(!tmpMap.containsKey(wordMorph.getFirst()) && type == 1 &&  badMap.containsKey(wordMorph.getFirst())){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					} else if(!tmpMap.containsKey(wordMorph.getFirst()) && type == 0 &&  dicMap.containsKey(wordMorph.getFirst())){
						buf.append(wordMorph.getFirst());
						buf.append(",");
					}
					
				}
//				System.out.println(wordMorph.toString());
//				if(dicMap.containsKey(wordMorph.getFirst()) || badMap.containsKey(wordMorph.getFirst())){
//					buf.append(wordMorph.getFirst());
//					buf.append(",");
//				}
			}
		}
		String retStr = buf.toString();
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr.substring(0, retStr.length()-1);
		}
	}
	
	public String extractBag(String line, int type){
		StringBuffer buf = new StringBuffer();
		Komoran komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
//		komoran.setFWDic(fwDic);
//		System.out.println(line);
		List<List<Pair<String,String>>> result = komoran.analyze(line);
		//
		boolean setComma = false;
		
		HashBag bag = new HashBag();
		for (List<Pair<String, String>> eojeolResult : result) {
			setComma = false;
			for (Pair<String, String> wordMorph : eojeolResult) {
				if(wordMorph.getSecond().equals("NNG") || wordMorph.getSecond().equals("NNP") || wordMorph.getSecond().equals("NNB")
						|| wordMorph.getSecond().equals("NP")){
					if(wordMorph.getFirst().length() > 1){
						if(type == 1 && badMap.containsKey(wordMorph.getFirst())){
							bag.add(wordMorph.getFirst());
						} else if ( type == 0 && dicMap.containsKey(wordMorph.getFirst())) {
							bag.add(wordMorph.getFirst());
						}
//						if(dicMap.containsKey(wordMorph.getFirst()) && badMap.containsKey(wordMorph.getFirst())){
							
//						}
					}
				}
			}
		}
		Object[] keyList = bag.uniqueSet().toArray();
		
		int len = keyList.length;
		
		for(int i = 0; i < len ; i++){
			buf.append(keyList[i]);
			buf.append(":");
			buf.append(bag.getCount(keyList[i]));
			buf.append("\t");
		}
		
		String retStr = buf.toString();
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr.substring(0, retStr.length()-1);
		}
	}
	
	public String extractBag(String line){
		StringBuffer buf = new StringBuffer();
		Komoran komoran = new Komoran(modelPath);
		komoran.setUserDic(userDic);
		komoran.setFWDic(fwDic);
		List<List<Pair<String,String>>> result = komoran.analyze(line);
		
		//
		boolean setComma = false;
		
		HashBag bag = new HashBag();
		for (List<Pair<String, String>> eojeolResult : result) {
			setComma = false;
			for (Pair<String, String> wordMorph : eojeolResult) {
				if(wordMorph.getSecond().equals("NNG") || wordMorph.getSecond().equals("NNP") || wordMorph.getSecond().equals("NNB")
						|| wordMorph.getSecond().equals("NP")){
					if(wordMorph.getFirst().length() > 1){
//						if(dicMap.containsKey(wordMorph.getFirst()) && badMap.containsKey(wordMorph.getFirst())){
							bag.add(wordMorph.getFirst());
//						}
					}
				}
			}
		}
		Object[] keyList = bag.uniqueSet().toArray();
		
		int len = keyList.length;
		
		for(int i = 0; i < len ; i++){
			buf.append(keyList[i]);
			buf.append(":");
			buf.append(bag.getCount(keyList[i]));
			buf.append("\t");
		}
		
		String retStr = buf.toString();
		
		if(retStr.length() == 0){
			return "";
		} else {
			return retStr.substring(0, retStr.length()-1);
		}
	}
	
	public void dicFileConvert(String path){
		FileReader fr;
		BufferedReader br;
		
		try {
			FileWriter fw = new FileWriter("dic.user");
			BufferedWriter bw = new BufferedWriter(fw);
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			
			String line;
			
			while((line = br.readLine())!=null){
				bw.append(line);
				bw.append("\t");
				bw.append("NNP");
				bw.append("\r\n");
				bw.flush();
			}
			fw.flush();
			
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
		
	}
}
