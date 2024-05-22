package com.idep.api.function.library;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * Data Function Library
 * @author sandeep.jadhav
 */
public class DataFunctions {
	
	Logger log = Logger.getLogger(DataFunctions.class);
	
	public String getIntASString(int i)
	{
		return ((Integer)i).toString();
	}
	
	public int getStringAsInt(String strNum)
	{
		return Integer.parseInt(strNum);
	}
	
	public String getLongASString(long i)
	{
		return ((Long)i).toString();
	}
	
	public long getStringAsLong(String strNum)
	{
		return Long.parseLong(strNum);
	}
	
	public String getDoubleASString(double i)
	{
		return ((Double)i).toString();
	}
	
	public double getStringAsDouble(String strNum)
	{
		return Double.parseDouble(strNum);
	}
	
	
	public String getSYSDate(String dateFormat) throws ParseException
	{
		if(dateFormat==null || dateFormat.equals("")){
			dateFormat="dd/MM/yyyy";
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.ENGLISH);
		Date date = new Date();
		String currentDate = formatter.format(date);
		Date newDate = formatter.parse(currentDate);
		return formatter.format(newDate);
	}
	
	public String getFormattedDate(String date,String dateFormat) throws ParseException
	{

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.ENGLISH);
		Date newDate = format.parse(date);
		return formatter.format(newDate);
	}
	
	public String getBackDate(String date,String dateFormat) throws ParseException
	{
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		cal.setTime(format.parse(date));
		cal.add(Calendar.YEAR, -1);
		Date prevYearDate = cal.getTime();
		return formatter.format(prevYearDate);
	}
	
	public String getPolicyStartDate(String date,String dateFormat) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		cal.setTime(formatter.parse(date));
		cal.add(Calendar.DATE, +1);
		Date policyStartDate = cal.getTime();
		return formatter.format(policyStartDate);
	}
	
	public String getPolicyEndDate(String date,String dateFormat) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		cal.setTime(formatter.parse(date));
		cal.add(Calendar.YEAR, +1);
		Date policyEndDate = cal.getTime();
		return formatter.format(policyEndDate);
	}
	
	public long roundOffDouble(double num)
	{
		return Math.round(num);
	}
	
	public String removeChar(String str, String ch)
	{
		return str.replace(ch, "");
	}
	
	public String concatString(String strArray[],String concatChar)
	{
		String str="";
		
		for(int i=0;i<strArray.length;i++)
		{
			str = str.concat(strArray[i]).concat(concatChar);
		}
		
	return str;
		
	}
	
	public double concatNumber(Double numArray[])
	{
		double num=0;
		
		for(int i=0;i<numArray.length;i++)
		{
			num = num+numArray[i];
		}
		
	return num;
		
	}
	
	public long getAge(String date) throws ParseException
	{
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		  Date dob = null;
			Date sysDate =new Date();
			long age=0;
			try {
				dob = format.parse(date);
				sysDate = format.parse(format.format(sysDate));

				//in milliseconds
				long diff = sysDate.getTime() - dob.getTime();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				//System.out.print((diffDays/365) + " Year, ");
				 age= (diffDays/365);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		return age;	
	}
		
	
	public static void main(String[] args) throws ParseException {
		DataFunctions fn = new DataFunctions();
		String s="MM/dd/yyyy";
		
		System.out.println(fn.getPolicyStartDate(fn.getSYSDate(s),s));
		System.out.println(fn.getPolicyEndDate(fn.getSYSDate(s),s));
		System.out.println(fn.getSYSDate(s));
	}
	
	
	
	

}
