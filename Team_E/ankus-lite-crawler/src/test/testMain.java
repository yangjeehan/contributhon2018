package test;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.host.URL;

public class testMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testMain tm = new testMain();
		tm.homePage();
	}

	public void homePage() throws Exception {
		WebClient webClient = new WebClient();
		try {
			webClient.getOptions().setCssEnabled(true);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setRedirectEnabled(true);
			
			webClient.getOptions().setActiveXNative(true);
			
			webClient.getOptions().setThrowExceptionOnScriptError(true);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
//			webClient.getOptions().setTimeout(100000);
			
			HtmlPage page = webClient.getPage("https://section.blog.naver.com/");
			Thread.sleep(1000);
			
			
//			System.out.println(page.asXml());
			
			HtmlInput queryInput = page.getElementByName("sectionBlogQuery");
			
			Thread.sleep(1000);
			
			System.out.println(queryInput.getValueAttribute());
			queryInput.setValueAttribute("요쿠르트아몬드");
//			DomElement queryInput2;
			Thread.sleep(500);
			 System.out.println(page.asText());
			 
			for(int i = 0; i < 7; i++){
				page.tabToNextElement();
//				
				page.pressAccessKey('\n');
				Thread.sleep(500);
				System.out.println(i+"\t"+page.getFocusedElement().asXml());
			}
			
//			System.out.println(queryInput.getValueAttribute());
//			
			Thread.sleep(1000);
			 System.out.println(page.asText());
//			HtmlAnchor button = page.getFirstByXPath("//*[@id='header']/div[1]/div/div[2]/form/fieldset/a[1]");
//
//			System.out.println(button.getHrefAttribute());
//			
//			HtmlPage page3 = queryInput.click();
//			
////			System.out.println(button.getLocalName());
//			
//			page3 = button.click();
//			Thread.sleep(3000);
//			
		   
//			
//			System.out.println(page3.asText());
//			System.out.println(page.getBody().asText());
//						
//	        NodeList inputs = page.getElementsByTagName("a");
//
//	        System.out.println(inputs.getLength());
//	        
//	        for(int i = 0 ; i < inputs.getLength(); i++){
//	        	System.out.println(inputs.item(i));
//	        }
//	        
//	        final List<?> divs = page.getByXPath("//*[@id=\"content\"]/section/div[2]/div[1]");
//	      
//	        
//	        
//	        System.out.println(divs.size());
	    } catch(Exception e){
	    	e.printStackTrace();
	    }
	}
}

