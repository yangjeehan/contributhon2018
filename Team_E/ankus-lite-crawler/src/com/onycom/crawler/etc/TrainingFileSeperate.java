package com.onycom.crawler.etc;

import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;

public class TrainingFileSeperate {
	public static void main(String[] args){
		TrainingFileSeperate tfs = new TrainingFileSeperate();
		tfs.core();
	}
	public void core(){
		CSVFileReaderToDEF crTrain = new CSVFileReaderToDEF();
		crTrain.setHeader("classLabel", 0);
		crTrain.setHeader("title", 3);
		crTrain.setHeader("keywords", 4);
		
		ArrayList<DocumentDEF> trainList = crTrain.getList("SBSFoodNews_label_sort-3.txt", "\t", "");
		
		int size = trainList.size();
		
		ArrayList<DocumentDEF> yesList = new ArrayList<DocumentDEF>();
		ArrayList<DocumentDEF> noList = new ArrayList<DocumentDEF>();
		
		Dictionary dic = new Dictionary();
		HashMap<String, Integer> keyMap= new HashMap<String, Integer>();
		keyMap = dic.getDicMap("dic.txt");
		
		for(int i = 0 ; i< size ; i++ ){
			DocumentDEF doc = trainList.get(i);
			int len = doc.keywords.length;
			boolean isKey = false;
			for(int j = 0; j < len; j++){
				if(keyMap.containsKey(doc.keywords[j])){
					isKey = true;
					break;
				}
			}
			
			if(isKey){
				if(doc.classLabel.equals("yes")){
					yesList.add(doc);
				} else {
					noList.add(doc);
				}
			}
		}
		
		for(int i = 0; i < 4; i++){
			ArrayList<DocumentDEF> tmpTestList = new ArrayList<DocumentDEF>();
			ArrayList<DocumentDEF> tmpTrainList = new ArrayList<DocumentDEF>();
			
			for(int j = 0; j < yesList.size(); j++){
				if(j % 4 == i){
					tmpTestList.add(yesList.get(j));
				} else {
					tmpTrainList.add(yesList.get(j));
				}
			}
			
			for(int j = 0; j < noList.size(); j++){
				if(j % 4 == i){
					tmpTestList.add(noList.get(j));
				}else {
					tmpTrainList.add(noList.get(j));
				}
			}
			
			ArrayList<String> prtList = new ArrayList<String>();
			CSVFileWriter cw = new CSVFileWriter();
			
			for(int j = 0; j < tmpTestList.size(); j++){
				StringBuffer buf = new StringBuffer();
				DocumentDEF doc = tmpTestList.get(j);
				buf.append(doc.classLabel);
				buf.append("\t");
				buf.append(doc.title);
				
				int len = doc.keywords.length;
				for(int l = 0; l < len ; l++){
					buf.append("\t");
					buf.append(doc.keywords[l]);
				}
				buf.append("\t");
				buf.append("dummy");
				
				prtList.add(buf.toString());
			}
			
			cw.outputFile(prtList, "test-"+i+".txt");
			
			prtList = new ArrayList<String>();
			cw = new CSVFileWriter();
			
			for(int j = 0; j < tmpTrainList.size(); j++){
				StringBuffer buf = new StringBuffer();
				DocumentDEF doc = tmpTrainList.get(j);
				buf.append(doc.classLabel);
				buf.append("\t");
				buf.append(doc.title);
				
				int len = doc.keywords.length;
				for(int l = 0; l < len ; l++){
					buf.append("\t");
					buf.append(doc.keywords[l]);
				}
				buf.append("\t");
				buf.append("dummy");
				
				prtList.add(buf.toString());
			}
			cw.outputFile(prtList, "train-"+i+".txt");
		}
		System.out.println(trainList.size()+"\t"+yesList.size()+"\t"+noList.size());
	}
}
