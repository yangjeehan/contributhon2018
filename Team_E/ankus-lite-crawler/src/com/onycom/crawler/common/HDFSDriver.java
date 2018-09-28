package com.onycom.crawler.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

public class HDFSDriver extends Configured implements Tool {
	
	public String getAddrConf(){
		conf cf = new conf();
		HashMap<String, String> cMap = new HashMap<String, String>();
		cMap = cf.confFileReader("hdfs");
		return cMap.get("addr");
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		
		conf.set("fs.defaultFS",  getAddrConf());		
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
	    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
	    
	    FileSystem fs = FileSystem.get(new java.net.URI(getAddrConf()), conf, "ankus");
		
		Path p = new Path(arg0[0]);
		fs.copyFromLocalFile(p, new Path("/"+p));
		
//		FileUtil.copy(fs, p, fs, p, false, conf);
		return 0;
	}
	
	public static void main(String[] args){
		HDFSDriver hd = new HDFSDriver();
		args = new String[1];
		args[0] = "models/test/";
		try {
			hd.run(args);
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다."); 
			
		}
	}
}
