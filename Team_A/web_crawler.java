package Ankus_crawler;

import java.io.BufferedReader; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.InputStreamReader; 
import java.net.MalformedURLException; 
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList; 
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 

// java web_crawler 자동차 ./down_image
//   args[0] : 검색할 해시태그
// 	 args[1] : 이미지를 저장할 디렉토리   (default : .//down_image/ )
public class web_crawler 
{ 	
	public static void main( String[] args ) throws IOException {
		String save_Dir = ".//down_image/";
		
		if( args[1] != "" )
			save_Dir = args[1];  		// args[1] : directory of stored image
		
		String encode_result = null;	
		encode_result = URLEncoder.encode(args[0], "utf-8");	// args[0] : tag name

		String down_Url = "https://www.instagram.com/explore/tags/" + encode_result;		// target url
		String typeRgx = "jpg";				
		
		System.out.println( "Directory : " + save_Dir );
		System.out.println( "Tag name  : " + args[0] );
		
		web_crawler.getTypedFileDown(down_Url, save_Dir, typeRgx);	// crawling procedure
	}
	
	// function : check a length of url
	// input  : url
	// output : length
	public static String getSource(String url) throws MalformedURLException, IOException { 
		String output = ""; 
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream())); 
		String line; 
		
		while ((line = br.readLine()) != null) { 
			output += line; 
		} 
		return output; 
	} 

	// function : check a length of url
	// input  : url
	// output : length
	public static ArrayList<String> getTypedFile_case1(String text, String typeRegex) { 
		String regex = "\"(http|https)[:][/][/][^<>\"]+[.]" + typeRegex + "\""; 
		Matcher m = Pattern.compile(regex).matcher(text); 
		//System.out.println("MATCH :" + m ); 

		ArrayList<String> output = new ArrayList<>(); 
		
		while (m.find()) { 
			output.add(m.group().replace("\"", "")); 
		} 
		
		//System.out.println("LIST" + output ); 
		return output; 
	} 

	public static void FileDownload(String address, String saveDir) { 
		try { 
			URL u = new URL(address); 
			FileOutputStream fos = new FileOutputStream(saveDir); 
			InputStream is = u.openStream(); 
			byte[] buf = new byte[1024]; 
			int len = 0; 
			
			while ((len = is.read(buf)) > 0) { 
				fos.write(buf, 0, len); 
			} 
			fos.close(); 
			is.close(); 
			
		} catch (IOException e) { 
			e.printStackTrace(); 
		} 
	} 

	public static String getWebFileName(String filePath) { 
		String[] parts = filePath.split("[/]"); 
		return parts[parts.length - 1]; 
	} 

	public static void getTypedFileDown(String url, String saveDir, String typeRegex) throws IOException { 
		String source = getSource(url); 
		ArrayList<String> urls = getTypedFile_case1(source, typeRegex); 
		
		int size = urls.size(); 		
		int k = 1; 

		System.out.println("//--------------------------------------"); 
		System.out.println("//   Now Downloading...  "); 
		System.out.print("// "); 
		int k2 = 0;
		int download_count=0;
		
		for (String i : urls) { 
			String save = saveDir + "\\" + getWebFileName(i); 
			FileDownload(i, save);
			download_count ++;
			
			System.out.print('+');
			
			if( download_count%20 == 0 )
			{
				System.out.println(" "); 
				System.out.print("// "); 
			}
			k++; 

		} 
		System.out.println(' ');
		System.out.println("//   Completed..." + download_count + " ea"); 
		System.out.println("//--------------------------------------"); 
	}	 
}
