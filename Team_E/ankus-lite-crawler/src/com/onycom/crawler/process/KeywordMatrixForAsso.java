package com.onycom.crawler.process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;

public class KeywordMatrixForAsso {
	public void generateMatrix(){
		core("2.txt");
	}
	
	public static void main(String[] args){
		KeywordMatrixForAsso kmfc = new KeywordMatrixForAsso();
		kmfc.core("3-1.txt");
	}
	public HashMap<String, Integer> setMatrix(){
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> map = dic.getDicMap("bad.txt");
		return map;
	}
	
	public HashMap<String, Integer> setMatrix(ArrayList<DocumentDEF> list){
		FileReader fr;
		BufferedReader br;
		
		String[] blindKeyword = null;
		try {
			fr= new FileReader("blindAsso.txt");
			br = new BufferedReader(fr);
			
			blindKeyword = br.readLine().split(",");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		String[] blindKeyword = {"수사","재판","확인","징역","형사","선고","조사","무단","복제","배포","금지","기자","영상","뉴스","제보","기사","기소","경찰","혐의"};
		HashMap<String,String> blindMap = new HashMap<String,String>();
		
		for(int i = 0; i < blindKeyword.length; i++){
			blindMap.put(blindKeyword[i], "");
		}
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> map = dic.getDicMap("bad.txt");
		
		int size = list.size();

		int cnt = map.size();
		for( int i = 0; i < size ; i++){
			DocumentDEF doc = list.get(i);
			int len = doc.keyList.length;
			
			for(int j = 0; j < len ; j++){
				if(doc.keyList[j].length() > 1 && !blindMap.containsKey(doc.keyList[j]) &&!map.containsKey(doc.keyList[j])){
					map.put(doc.keyList[j], cnt);
					cnt++;
				}
			}
		}
		return map;
	}
	
	public void core(String path){
		CSVFileReaderToDEF cftd = new CSVFileReaderToDEF();
		cftd.setHeader("contents", 2);
		cftd.setHeader("classLabel", 1);
		cftd.setHeader("rno", 0);
		
		ArrayList<DocumentDEF> list = cftd.getList(path, "\t", "");
		ArrayList<DocumentDEF> fList = new ArrayList<DocumentDEF>();
		
		Dictionary dic =  new Dictionary();
		
		HashMap<String, Integer> badMap = dic.getDicMap("bad.txt");
		HashMap<String, Integer> keyMap = dic.getDicMap("dic.txt");
		
		NLP nlp = new NLP();
		for(int i = 0; i < list.size(); i++){
			DocumentDEF doc = list.get(i);
//			System.out.println(doc.classLabel);
			String[] keyList = nlp.extractNoun(doc.contents).split(",");
			doc.keyList = keyList;
			int len = doc.keyList.length;
//			System.out.println(len);
			boolean key = false;
			boolean bad = false;
			
			for(int j = 0; j < len ; j++){
				if(keyMap.containsKey(doc.keyList[j])){
					key = true;
				}
				if(badMap.containsKey(doc.keyList[j])){
					bad = true;
				}
			}
			if(key && bad){
				fList.add(doc);
			}
		}
//		HashMap<String, Integer> map = setMatrix();
		
		HashMap<String, Integer> map = setMatrix(fList);
		int mapSize = map.size();			
		
		int size = fList.size();
		
		Object[] keyList = map.keySet().toArray();
		String matrix[] = new String[keyList.length];
		
		ArrayList<String> prtList = new ArrayList<String>();
		
//		System.out.println(keyList.length+"\t"+mapSzie);
		
		StringBuffer prtBuf = new StringBuffer();
//		prtBuf.append("rno\tclass\t");
//		for(int j = 0; j < mapSize; j++){
//			for(int i = 0; i < keyList.length ; i++){
//				if(map.get(keyList[i]) == j){
//					prtBuf.append((String)keyList[i]);
//					prtBuf.append(",");
//					
//					System.out.println((String)keyList[i]+":"+map.get((String)keyList[i]));
//					break;
//				}
//			}
//		}
//		prtList.add(prtBuf.toString().substring(0, prtBuf.toString().length()-1));
		
		boolean rmList[] = new boolean[matrix.length];
		boolean rmSize[] = new boolean[size];
		
		String[][] table = new String[matrix.length][size];
		
		for(int i = 0; i < size; i++){
			prtBuf = new StringBuffer();
			
			DocumentDEF doc = fList.get(i);
//			prtBuf.append(doc.rno);
//			prtBuf.append("\t");
//			prtBuf.append(doc.classLabel);
			int len = doc.keyList.length;
			
			matrix = new String[mapSize];
			for(int j = 0; j < matrix.length; j++){
				matrix[j] = "?";
			}
			for(int j = 0 ; j < len; j++){
				if(map.containsKey(doc.keyList[j])){
					matrix[map.get(doc.keyList[j])] = "Y";
					
				}
			}
			
			boolean isInclude1 = false;
			for(int j = 0 ; j < mapSize; j++){
				
//				if(matrix[j] == null){
//					prtBuf.append("?");
//				} else {
//					
//				prtBuf.append(matrix[j]);
				table[j][i] = matrix[j];
				if(matrix[j].equals("Y")){
					isInclude1 = true;
					rmList[j] = true;
				}
//					isInclude1 = true;
//				}
//				prtBuf.append(",");
			}
			if(!isInclude1){
//				prtList.add(prtBuf.toString().substring(0, prtBuf.toString().length()-1));
				rmSize[i] = true;
			}
		}
		for(int i = 0; i < mapSize; i++){
			System.out.print(rmList[i]+"\t");
		}
		System.out.println(rmSize.length+":"+mapSize);
		
//		for(int i = 0; i < keyList.length ; i++){
//			if(rmList[i]){
//				prtBuf.append((String)keyList[i]);
//				prtBuf.append(",");
//				
//				System.out.println((String)keyList[i]+":"+map.get((String)keyList[i]));
//			}
//		}
		
		for(int j = 0; j < mapSize; j++){
			for(int i = 0; i < keyList.length ; i++){
				if(map.get(keyList[i]) == j){
					if(rmList[map.get((String)keyList[i])]){
						prtBuf.append((String)keyList[i]);
						prtBuf.append(",");
						
						System.out.println((String)keyList[i]+":"+map.get((String)keyList[i]));
						break;
					}
				}
			}
		}
		prtList.add(prtBuf.toString().substring(0, prtBuf.toString().length()-1));
		
		
		
		
		for(int i = 0 ; i < size; i++){
			StringBuffer buf = new StringBuffer();
			if(!rmSize[i]){
				for(int j = 0; j < mapSize; j++){
					if(rmList[j]){
						buf.append(table[j][i]);
						buf.append(",");
					} else {
//						System.out.println(j);
					}
				}
				prtList.add(buf.toString().substring(0, buf.toString().length()-1));
			}
		}
		
		for(int i = 0 ; i < size; i++){
			StringBuffer buf = new StringBuffer();
			if(!rmSize[i]){
				for(int j = 0; j < mapSize; j++){
					if(rmList[j]){
						buf.append(table[j][i]);
						buf.append(",");
					} else {
//						System.out.println(j);
					}
				}
				prtList.add(buf.toString().substring(0, buf.toString().length()-1));
			}
		}
		
				
		
		CSVFileWriter cfw = new CSVFileWriter();
		cfw.outputFile(prtList, "c:/conv/sbs-matrix-20161216.csv");
	}
}
