package com.onycom.crawler.common;

public class TagRemover {
	public String removeHTMLTag(String line) {
		line = line.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
		line = line.replaceAll("<[^>]*>", "");
		line = spCharRid(line);
		
		return line;
	}
	public String spCharRid(String strInput){
		  String strWork = strInput;
		  String[] spChars = {
				    "`", "'", "/", "\""
				  };
		  
		  int spCharLen = spChars.length; 
		  
		   for(int i = 0; i < spCharLen; i++){
		    strWork = strWork.replace(spChars[i], "");
		   }

		  return strWork;
		 }
}
