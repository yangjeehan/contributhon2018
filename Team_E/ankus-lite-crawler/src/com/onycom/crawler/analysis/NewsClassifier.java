package com.onycom.crawler.analysis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.onycom.crawler.DB.DBConnect;
import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.Dictionary;

public class NewsClassifier {
	public static void main(String[] args){
		NewsClassifier nc = new NewsClassifier();
		nc.core();
	}
	ArrayList<DocumentDEF> trainList;
	public NewsClassifier(){
		trainList = getTrainData();
	}
	
	public ArrayList<DocumentDEF> getTrainData(){
		DBConnect dbTest = new DBConnect();
		
		dbTest.getConnection();
		String sql = "select nid, rword from food_risk_word where keyword_type = 'caution'";
		ResultSet rs = dbTest.getDBData(sql);
		
		int nid = -1;
		ArrayList<DocumentDEF> trainList = new ArrayList<DocumentDEF>();
		try {
			
			DocumentDEF doc = new DocumentDEF();
			while(rs.next()){
				if(nid != rs.getInt(1)){
					if(nid != -1 ){
						doc.keywords = doc.keyStr.split("\t");
						doc.classLabel = "yes";
						trainList.add(doc);
						doc = new DocumentDEF();
					}
					nid = rs.getInt(1);
					doc.rno = new String().valueOf(nid);
					doc.keyStr = rs.getString(2);
				} else {
					doc.keyStr += "\t"+rs.getString(2);
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trainList;
	}
	
	public String getClass(DocumentDEF docTest){
		int k = 1;
		
		Dictionary dic = new Dictionary();
		ArrayList<String> badList = dic.getDicList("bad.txt");
		
		int badSize = badList.size();
		String testStr[] = new String[badSize];
		String trainStr[] = new String[badSize];;
		for(int i = 0; i < badSize; i++){
			testStr[i] = "0";
		}
		
//		System.out.println(docTest.title);
		for(int idx = 0; idx < badSize; idx++){
			for(int l = 0; l < docTest.keywords.length; l++){
				if(badList.get(idx).equals(docTest.keywords[l].split(":")[0])){
					testStr[idx] = "1";
				}
			}
		}

//		System.out.print("test:\t");
//		for(int l = 0; l < docTest.keywords.length; l++){
//			System.out.print(docTest.keywords[l]);
//		}
//		System.out.println();
//		
//		System.out.print("test:\t");
//		for(int idx = 0; idx < badSize; idx++){
//			System.out.print(testStr[idx]);
//		}
//		System.out.println();
		
		int trainSize = trainList.size();
		double minDistance = 1;
		for(int i = 0; i < trainSize; i++){
			for(int idx = 0; idx < badSize; idx++){
				trainStr[idx] = "0";
			}
			
			DocumentDEF docTrain = trainList.get(i);
			
			for(int idx = 0; idx < badSize; idx++){
				for(int l = 0; l < docTrain.keywords.length; l++){
					if(badList.get(idx).equals(docTrain.keywords[l])){
						trainStr[idx] = "1";
					}
				}
			}
//			System.out.print("tran:\t");
//			for(int idx = 0; idx < badSize; idx++){
//				System.out.print(trainStr[idx]);
//			}
			
			docTrain.distance = getJaccardDistance(trainStr, testStr);
//			System.out.println("\t"+docTrain.distance);
			if(minDistance > docTrain.distance){
				minDistance = docTrain.distance;
			}
			
//			if(docTrain.distance == 1){
//				System.out.println(i);
//			}
			
		}
		//sort
//		boolean isFood = false;
//		HashMap<String, Integer> keyMap = dic.getDicMap("dic.txt");
//		
//		for( int l = 0; l < docTest.keywords.length; l++){
//			if(keyMap.containsKey(docTest.keywords[l])){
//				isFood = true;
//				break;
//			}
//		}
//		quickSort(trainList, 0, trainList.size()-1);
//		for(int i = 0; i < k; i++){
//			DocumentDEF docTrain = trainList.get(i);
//			if(isFood && trainList.get(i).classLabel.equals("yes")){			
//				return "yes";
//			}
//		}
		if(minDistance == 0){
			return "yes";
		} else {
			return "no";
		}
//		return "no";
	}
	
	public void core(){
		CSVFileReaderToDEF crTrain = new CSVFileReaderToDEF();
		crTrain.setHeader("classLabel", 0);
		crTrain.setHeader("title", 1);
		crTrain.setHeader("keywords", 2);
		
		String output = "3";
		
		ArrayList<DocumentDEF> trainList = crTrain.getList("train-"+output+".txt", "\t", "");
		
		int trainSize = trainList.size();
		System.out.println("training data size:\t"+trainSize);
		
		CSVFileReaderToDEF crTest = new CSVFileReaderToDEF();
		crTest.setHeader("classLabel", 0);
		crTest.setHeader("title", 1);
		crTest.setHeader("keywords", 2);
		
		ArrayList<DocumentDEF> testList = crTest.getList("test-"+output+".txt", "\t", "");
		
		int testSize = testList.size();
		System.out.println("training data size:\t"+testSize);
		
		int k = 1;
		
		Dictionary dic = new Dictionary();
		ArrayList<String> badList = dic.getDicList("bad.txt");
		
		int badSize = badList.size();
		String testStr[] = new String[badSize];
		String trainStr[] = new String[badSize];
		
		for(int i = 0; i < badSize; i++){
			testStr[i] = "0";
			trainStr[i] = "0";
		}
		
		HashMap<String, Integer> keyMap = dic.getDicMap("dic.txt");
		int yesyes = 0;
		int yesno = 0;
		int noyes = 0;
		int nono = 0;
		
		for(int j = 0; j < testSize; j++){
			DocumentDEF docTest = testList.get(j);
			String testLabel = docTest.classLabel;
			
			for(int i = 0; i < badSize; i++){
				testStr[i] = "0";
			}
			
			for(int idx = 0; idx < badSize; idx++){
				for(int l = 0; l < docTest.keywords.length; l++){
					if(badList.get(idx).equals(docTest.keywords[l])){
						testStr[idx] = "1";
					}
				}
			}
//			System.out.print("test:\t");
//			for(int idx = 0; idx < badSize; idx++){
//				System.out.print(testStr[idx]);
//			}
//			System.out.println();
			double minDistance = 1;
			for(int i = 0; i < trainSize; i++){
				for(int idx = 0; idx < badSize; idx++){
					trainStr[idx] = "0";
				}
				
				DocumentDEF docTrain = trainList.get(i);
				
				for(int idx = 0; idx < badSize; idx++){
					for(int l = 0; l < docTrain.keywords.length; l++){
						if(badList.get(idx).equals(docTrain.keywords[l])){
							trainStr[idx] = "1";
						}
					}
				}
//				System.out.print("tran:\t");
//				for(int idx = 0; idx < badSize; idx++){
//					System.out.print(trainStr[idx]);
//				}
				
				docTrain.distance = getJaccardDistance(trainStr, testStr);
//				System.out.println("\t"+docTrain.distance);
				if(minDistance > docTrain.distance){
					minDistance = docTrain.distance;
				}
				
//				if(docTrain.distance == 1){
//					System.out.println(i);
//				}
				
			}
			//sort
			boolean isFood = false;
			for( int l = 0; l < docTest.keywords.length; l++){
				if(keyMap.containsKey(docTest.keywords[l])){
					isFood = true;
					break;
				}
			}
			quickSort(trainList, 0, trainList.size()-1);
			for(int i = 0; i < k; i++){
				DocumentDEF docTrain = trainList.get(i);
				
				
				if(isFood && trainList.get(i).classLabel.equals("yes")){
					if(testLabel.equals("yes")){
						yesyes++;
						//tp
					} else {
						noyes++;
						//fp
						
					}
					docTest.classLabel = "yes";
					break;
				}
				else if(isFood) {
					if(testLabel.equals("yes")){
						yesno++;
					} else {
						nono++;
					}
				}
			}
//			if(testLabel.equals("no") || docTest.classLabel == null){
//				if(docTest.classLabel.equals("yes")){
//					yesno++;
//				} else {
//					nono++;
//				}
//				docTest.classLabel = "no";
//			}
			docTest.distance = minDistance;
			testList.set(j, docTest);
		}
		System.out.println("yes:yes"+"\t"+yesyes);
		System.out.println("yes:no"+"\t"+yesno);
		System.out.println("no:yes"+"\t"+noyes);
		System.out.println("no:no"+"\t"+nono);
		
		
		ArrayList<String> prtList = new ArrayList<String>();
		
		for(int i = 0; i < testSize; i++){
			StringBuffer buf = new StringBuffer();
			DocumentDEF docTest = testList.get(i);
			if(docTest.classLabel.equals("yes") && docTest.keywords.length > 1){
				buf.append(docTest.distance);
				buf.append("\t");
//				buf.append(docTest.date);
//				buf.append("\t");
				buf.append(docTest.title);
				for(int j = 0; j < docTest.keywords.length; j++){
					buf.append("\t");
					buf.append(docTest.keywords[j]);
				}
				prtList.add(buf.toString());
			}
		}
		CSVFileWriter cw = new CSVFileWriter();
		cw.outputFile(prtList, "knn-test-"+k+"-"+output+".txt");
	}
	
	private int partition(ArrayList<DocumentDEF> arr, int left, int right)
	{
	      int i = left, j = right;
	      DocumentDEF tmp;
	      double pivot = arr.get(((left + right) / 2)).distance;
	     
	      while (i <= j) {
	            while (arr.get(i).distance < pivot)
	                  i++;
	            while (arr.get(j).distance > pivot)
	                  j--;
	            if (i <= j) {
	                  tmp = arr.get(i);
	                  arr.set(i, arr.get(j));
	                  arr.set(j, tmp);
	                  i++;
	                  j--;
	            }
	      };
	     
	      return i;
	}
	 
	private void quickSort(ArrayList<DocumentDEF> arr, int left, int right) {
	      int index = partition(arr, left, right);
	      if (left < index - 1)
	            quickSort(arr, left, index - 1);
	      if (index < right)
	            quickSort(arr, index, right);
	}
	
	public String prtArray(String[] array){
		int len = array.length;
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < len; i++){
			buf.append(array[i]+"\t");
		}
		return buf.toString();
	}
	
	public double getJaccardDistance(String[] x, String[] y){
		boolean is1ContainX = false;
		boolean is1ContainY = false;
		
		for(int i = 0; i < x.length; i++){
			if(x[i].equals("1")){
				is1ContainX = true;
				break;
			}
		}
		
		for(int i = 0; i < x.length; i++){
			if(y[i].equals("1")){
				is1ContainY = true;
				break;
			}
		}
		
		if(!is1ContainX || !is1ContainY){
			return 1;
		}
		
		int xLen = x.length;
		int yLen = y.length;

		double xyInter = 0;
		
		for(int i = 0; i < xLen; i++){
//			for(int j = 0; j < yLen; j++){
//				if(x[i].equals(y[j])){
//					xyInter++;
//					break;
//				}
//			}
			
			if(x[i].equals(y[i])){
				xyInter++;
			}
		}
		double xySum = xLen+yLen - xyInter;
//		return (xLen+yLen) / (xLen+yLen+xyInter); // jaccard distance

		//jaccrad similiarity
		if(xyInter == 0){
			return 1;
		} else {
			return 1-(xyInter/xySum);
		}
	}
}
