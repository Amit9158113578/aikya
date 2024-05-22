package com.idep.travelquote.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;


public class DateFormatter {

	Logger log = Logger.getLogger(DateFormatter.class);
	
	public String calculateDOB(int age)
	{				
			/* LocalDate now = LocalDate.now();
			 LocalDate dob = now.minusYears(age);		 
			 DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
			 DateTimeFormatter month = DateTimeFormatter.ofPattern("MM");
			 System.out.println("Year:"+dob.format(year));
			 System.out.println("Month:"+dob.format(month));
			 String birthYear = dob.format(year);
			 String birthMonth = dob.format(month);
				if(Integer.parseInt(birthMonth) <= 6)
					return("01/01/" + birthYear);
				else
					return("01/07/" + birthYear);
				*/
			int birthYear = 0;
			Calendar now = Calendar.getInstance();
			int currentYear = now.get(Calendar.YEAR);
			int currentMonth = now.get(Calendar.MONTH);
			birthYear = currentYear - age;
			if(currentMonth <= 6)
				return("01/01/" + birthYear);
			else
				return("01/07/" + birthYear);					
	}
	
	public long getDaysDifference(String startDate ,String endDate) throws ParseException
	{		
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date startingDate = formatter.parse(startDate);
			Date endingDate = formatter.parse(endDate);
			long diff = endingDate.getTime() - startingDate.getTime();
			long numberOfDays =  TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			numberOfDays = numberOfDays + 1;
			return numberOfDays; 
	}
}