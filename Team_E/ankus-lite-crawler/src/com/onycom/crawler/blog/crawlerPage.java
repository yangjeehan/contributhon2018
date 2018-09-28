package com.onycom.crawler.blog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionList;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

public class crawlerPage {
	
	private NaverBlogVal mVal;
	
	public crawlerPage(){
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 

		this.subAddrList = new ArrayList<String>();
		
		mVal = new NaverBlogVal();
	}
	
	private ArrayList<String> subAddrList;
	
	public int crawler(String addr) {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		
		WebClient webClient = new WebClient();
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setRedirectEnabled(true);
		
		webClient.getOptions().setActiveXNative(true);
		
		webClient.getOptions().setThrowExceptionOnScriptError(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);

		HtmlPage page;
		int maxCnt = 0;
		try {
			page = webClient.getPage(addr);
	
			List<?> tmpDivs = page.getByXPath(mVal.listPage1);
			
			List<?> cntDivs;
			HtmlSpan cntDiv;
					
			
			if(tmpDivs.size() > 0){
				cntDivs = page.getByXPath(mVal.listPage2);
				cntDiv = (HtmlSpan)cntDivs.get(0);
				
				String countStr = cntDiv.asText();
				maxCnt = Integer.parseInt(countStr.split("/")[1].replace(" ", "").replace(",", "").replace("건", ""));
				
	//			System.out.println(page.asXml().replace("amp;",""));
				for(int i = 1; i < 11; i++){
					List<?> divs = page.getByXPath(mVal.getListPage3(i));	
					if(divs.size() > 0){
						HtmlDefinitionList div = (HtmlDefinitionList)divs.get(0);
	//					System.out.println(div.getElementsByTagName("a").get(0).getAttribute("href"));
						
						this.subAddrList.add(div.getElementsByTagName("a").get(0).getAttribute("href"));
					}
				}
			} else {
				cntDivs = page.getByXPath(mVal.listPage4);
				
				if(cntDivs.size() > 0){
					cntDiv = (HtmlSpan)cntDivs.get(0);
					
					String countStr = cntDiv.asText();
					maxCnt = Integer.parseInt(countStr.split("/")[1].replace(" ", "").replace(",", "").replace("건", ""));
					
	//				System.out.println(page.asXml().replace("amp;",""));
					for(int i = 1; i < 11; i++){
						List<?> divs = page.getByXPath(mVal.getListPage5(i));	
						if(divs.size() > 0){
							HtmlDefinitionList div = (HtmlDefinitionList)divs.get(0);
	//						System.out.println(div.getElementsByTagName("a").get(0).getAttribute("href"));
							
							this.subAddrList.add(div.getElementsByTagName("a").get(0).getAttribute("href"));
						}
					}
				} 
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	    
	    return maxCnt;
	}
	
	public ArrayList<String> getSubAddr(){
		return this.subAddrList;
	}
}
