package com.onycom.crawler.process;

import java.util.ArrayList;

import com.onycom.crawler.DEF.DocumentDEF;

/**
 * Caution DB & Association Rules
 * @author SSL
 *
 */
public class KeywordToRow {
	public void core(ArrayList<DocumentDEF> list){
		int size = list.size();
		for(int i = 0; i < size ; i++){
			DocumentDEF doc = list.get(i);
			int len = doc.keyList.length;
			
			for(int j = 0; j < len; j++){
				System.out.println(doc.rno+"\t"+doc.keyList[j]);
				
				//식품단어 데이터베이스 저장
			}
		}
	}
}
