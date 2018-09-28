package com.onycom.crawler.etc;

import java.io.*;
import java.util.ArrayList;

import com.onycom.crawler.DEF.DocumentDEF;
import com.onycom.crawler.common.CSVFileReaderToDEF;
import com.onycom.crawler.common.CSVFileWriter;
import com.onycom.crawler.common.NLP;

public class ExtractETCNoun {
	public static void main(String[] args){
		CSVFileReaderToDEF cr = new CSVFileReaderToDEF();
		
		cr.setHeader("rno", 0);
		cr.setHeader("contents", 2);
		
		ArrayList<DocumentDEF> list = cr.getList("1399.txt", "\t", "");
		int size = list.size();
		
		NLP nlp = new NLP();
		
		StringBuffer buf = new StringBuffer();
		System.out.println(size);
		ArrayList<String> prtList = new ArrayList<String>();
		for(int i = 0; i < size ; i++){
			DocumentDEF doc = list.get(i);
			prtList.add(doc.rno+"\t"+nlp.bagToStr(nlp.listToBag(nlp.extractNoun(doc.contents.replace("\"", "")).split(","))));
		}
		
		CSVFileWriter cw = new CSVFileWriter();
		cw.outputFile(prtList, "1399_CleanData_keyword-3.txt");

//		FileReader fr;
//		BufferedReader br;
//		
//		try {
//			fr = new FileReader("1399_CleanData.txt");
//			br = new BufferedReader(fr);
//			
//			String line;
//			
//			while((line = br.readLine())!=null){
//				System.out.println(line);
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
}
