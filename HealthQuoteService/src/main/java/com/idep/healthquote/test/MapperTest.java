package com.idep.healthquote.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class MapperTest {
	
	

	public static void main(String[] args) throws ParseException {
	
		
		   SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
		   SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy",Locale.ENGLISH);
		   String date = "25/10/2016";
		   Date newDate = format.parse(date);
		   System.out.println(newDate);
		 System.out.println(formatter.format(newDate));
		

	}

}
