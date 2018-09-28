package com.onycom.crawler.common;

import java.io.*;
import java.util.ArrayList;

public class CSVFileWriter {
	public void outputFile(ArrayList<String> list, String path){
		FileWriter fw;
		BufferedWriter bw;
		
		File f = new File("/"+path.split("/")[0]+"/");
		f.mkdir();
		
		try {
			fw = new FileWriter(path);
			bw = new BufferedWriter(fw);
			
			int size = list.size();
			
			for(int i = 0; i < size; i++){
				bw.append(list.get(i));
				bw.append("\r\n");
				bw.flush();
				fw.flush();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("오류가 발생했습니다."); 
		}
		
	}
}
