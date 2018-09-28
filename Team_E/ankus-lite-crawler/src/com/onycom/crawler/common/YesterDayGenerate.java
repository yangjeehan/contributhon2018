package com.onycom.crawler.common;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class YesterDayGenerate {

	public String getYesterDay(int gab){
		Calendar cal = new GregorianCalendar();
	    cal.add(Calendar.DATE, -gab);

	    StringBuffer buf = new StringBuffer();
	    
	    buf.append(cal.get(Calendar.YEAR));
	    buf.append("-");
	    if((cal.get(Calendar.MONTH)) < 9){
	    	 buf.append("0"+(cal.get(Calendar.MONTH) + 1));
	    }else {
	    	 buf.append((cal.get(Calendar.MONTH) + 1));
	    }
	   
	    buf.append("-");
	    if(cal.get(Calendar.DAY_OF_MONTH )< 10){
	    	buf.append("0"+cal.get(Calendar.DAY_OF_MONTH));
	    } else {
	    	buf.append(cal.get(Calendar.DAY_OF_MONTH));
	    }
		return buf.toString();
	}
	
	public String getYesterDayD(int gab){
//		20130101000000
		
		Calendar cal = new GregorianCalendar();
	    cal.add(Calendar.DATE, -gab);

	    StringBuffer buf = new StringBuffer();
	    
	    buf.append(cal.get(Calendar.YEAR));
//	    buf.append("-");
	    if((cal.get(Calendar.MONTH)) < 9){
	    	 buf.append("0"+(cal.get(Calendar.MONTH) + 1));
	    }else {
	    	 buf.append((cal.get(Calendar.MONTH) + 1));
	    }
	   
//	    buf.append("-");
	    if(cal.get(Calendar.DAY_OF_MONTH )< 10){
	    	buf.append("0"+cal.get(Calendar.DAY_OF_MONTH));
	    } else {
	    	buf.append(cal.get(Calendar.DAY_OF_MONTH));
	    }
//	    buf.append("000000");
		return buf.toString();
	}
	
	public String getYesterDayDE(int gab){
//		20130101000000
		
		Calendar cal = new GregorianCalendar();
	    cal.add(Calendar.DATE, -gab);

	    StringBuffer buf = new StringBuffer();
	    
	    buf.append(cal.get(Calendar.YEAR));
//	    buf.append("-");
	    if((cal.get(Calendar.MONTH)) < 9){
	    	 buf.append("0"+(cal.get(Calendar.MONTH) + 1));
	    }else {
	    	 buf.append((cal.get(Calendar.MONTH) + 1));
	    }
	   
//	    buf.append("-");
	    if(cal.get(Calendar.DAY_OF_MONTH )< 10){
	    	buf.append("0"+cal.get(Calendar.DAY_OF_MONTH));
	    } else {
	    	buf.append(cal.get(Calendar.DAY_OF_MONTH));
	    }
	    buf.append("235959");
		return buf.toString();
	}
}
