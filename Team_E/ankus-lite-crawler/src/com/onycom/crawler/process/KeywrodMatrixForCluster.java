package com.onycom.crawler.process;

import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;
import com.onycom.crawler.common.NLP;

public class KeywrodMatrixForCluster {
	public static void main(String[] args){
		KeywrodMatrixForCluster kmfc = new KeywrodMatrixForCluster();
		kmfc.core("3-2.txt");
	}
	public HashMap<String, Integer> setMatrix(){
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> map = dic.getDicMap("bad.txt");
		return map;
	}
	
	public HashMap<String, Integer> setMatrix(ArrayList<DocumentDEF> list){
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		int size = list.size();

		int cnt = 0;
		for( int i = 0; i < size ; i++){
			DocumentDEF doc = list.get(i);
			int len = doc.keyList.length;
			
			for(int j = 0; j < len ; j++){
				if(doc.keyList[j].length() > 1 && !map.containsKey(doc.keyList[j])){
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
			String keyStr = nlp.extractNoun(doc.contents,1);
			doc.keyList = keyStr.split(",");
			int len = doc.keyList.length;
//			System.out.println(len);
			boolean key = false;
			boolean bad = false;
			
//			for(int j = 0; j < len ; j++){
//				if(keyMap.containsKey(doc.keyList[j])){
//					key = true;
//				}
//				if(badMap.containsKey(doc.keyList[j])){
//					bad = true;
//				}
//			}
//			if(key && bad){
				fList.add(doc);
//			}
				if(i%100 == 0){
					System.out.println(i+"\t"+doc.classLabel+"\t"+keyStr);
				}
		}
//		HashMap<String, Integer> map = setMatrix();
		
		HashMap<String, Integer> map = setMatrix(fList);
		int mapSzie = map.size();			
		
		int size = fList.size();
		
		Object[] keyList = map.keySet().toArray();
		String matrix[] = new String[keyList.length];
		
		ArrayList<String> prtList = new ArrayList<String>();
		
		System.out.println(keyList.length+"\t"+mapSzie);
		
		StringBuffer prtBuf = new StringBuffer();
		prtBuf.append("rno\tclass\t");
		for(int j = 0; j < mapSzie; j++){
			for(int i = 0; i < keyList.length ; i++){
				if(map.get(keyList[i]) == j){
					prtBuf.append((String)keyList[i]);
					prtBuf.append("\t");
					
					System.out.println((String)keyList[i]+":"+map.get((String)keyList[i]));
					break;
				}
			}
		}
		prtList.add(prtBuf.toString().substring(0, prtBuf.toString().length()-1));
		
		for(int i = 0; i < size; i++){
			prtBuf = new StringBuffer();
			
			DocumentDEF doc = fList.get(i);
			prtBuf.append(doc.rno);
			prtBuf.append("\t");
			prtBuf.append(doc.classLabel);
			int len = doc.keyList.length;
			
			matrix = new String[mapSzie];
			
			for(int j = 0 ; j < len; j++){
				if(map.containsKey(doc.keyList[j])){
					matrix[map.get(doc.keyList[j])] = "1";
				}
			}
			
			boolean isInclude1 = false;
			for(int j = 0 ; j < mapSzie; j++){
				prtBuf.append("\t");
				if(matrix[j] == null){
					prtBuf.append("0");
				} else {
					
					prtBuf.append(matrix[j]);
					isInclude1 = true;
				}
				
			}
			if(isInclude1){
				prtList.add(prtBuf.toString());
			}
		}
		
		CSVFileWriter cfw = new CSVFileWriter();
		cfw.outputFile(prtList, "c:/conv/1399-matrix.csv");
	}
}
