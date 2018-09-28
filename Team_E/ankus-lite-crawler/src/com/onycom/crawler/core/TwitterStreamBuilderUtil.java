package com.onycom.crawler.core;
import java.util.HashMap;

import com.onycom.crawler.common.ConfFileReader;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterStreamBuilderUtil {
	static String ACCESSTOKEN;
	static String ACCESSSECRET;
	static String CONSUMERTOKEN;
	static String CONSUMERSECRET;
	
	public TwitterStreamBuilderUtil(int i){
		getTwitterAccount(i);
	}
	public static TwitterStream getStream(){
                                ConfigurationBuilder cb = new ConfigurationBuilder();
                                cb.setDebugEnabled(true);
                                cb.setOAuthConsumerKey(CONSUMERTOKEN);
                                cb.setOAuthConsumerSecret(CONSUMERSECRET);
                                cb.setOAuthAccessToken(ACCESSTOKEN);
                                cb.setOAuthAccessTokenSecret(ACCESSSECRET);
                                
                                return new TwitterStreamFactory(cb.build()).getInstance();
	}
                
	public static Twitter getAccount(){
                    ConfigurationBuilder cb = new ConfigurationBuilder();
                    cb.setDebugEnabled(true);
                    cb.setOAuthConsumerKey(CONSUMERTOKEN);
                    cb.setOAuthConsumerSecret(CONSUMERSECRET);
                    cb.setOAuthAccessToken(ACCESSTOKEN);
                    cb.setOAuthAccessTokenSecret(ACCESSSECRET);
                    
                    return new TwitterFactory(cb.build()).getInstance();
    }
	private void getTwitterAccount(int i){
            		ConfFileReader conf = new ConfFileReader();
            		HashMap<String, String> confMap = conf.getConfFile("twitter"+i);
            		
            		//System.out.println(i+"\t"+confMap.get("ACCESSTOKEN"));
            		
            		ACCESSTOKEN = confMap.get("ACCESSTOKEN");
            		ACCESSSECRET = confMap.get("ACCESSSECRET");
            		CONSUMERTOKEN = confMap.get("CONSUMERTOKEN");
            		CONSUMERSECRET = confMap.get("CONSUMERSECRET");
	}             
}