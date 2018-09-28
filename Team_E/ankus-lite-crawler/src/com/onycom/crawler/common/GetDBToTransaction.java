package com.onycom.crawler.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.onycom.crawler.DB.DBConnect;

public class GetDBToTransaction {
	public static void main(String[] args){
		GetDBToTransaction gt = new GetDBToTransaction();
		gt.core();
	}
	public void core(){
		String sql = "select * from food_word";
		
		DBConnect dbConn = new DBConnect();
		ResultSet rs =dbConn.getDBData(sql);
		
		ArrayList<String> list = new ArrayList<String>();
		try {
			while(rs.next()){
				list.add(rs.getString(1)+"\t"+rs.getString(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CSVFileWriter cw = new CSVFileWriter();
		cw.outputFile(list, "transaction.txt");
	}
}
