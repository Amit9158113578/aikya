package com.idep.proposal.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HealthGenericFunction {

	public HealthGenericFunction() {
		// TODO Auto-generated constructor stub
	}
public static void main(String[] args) {
	
	
	String s="";
	System.out.println("length : "+s.length());
	
	/*DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	String strDate="13/06/1998";
	Date frmDate=null;
	try {
		frmDate = sdf.parse(strDate);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	System.out.println("Date : "+frmDate);
	
	int age=0;
	try {
		HealthGenericFunction hf = new HealthGenericFunction();
		age = hf.getAge(sdf.parse(strDate));
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Age : "+age);
*/
}


public int getAge(Date birthday)
{
    GregorianCalendar today = new GregorianCalendar();
    GregorianCalendar bday = new GregorianCalendar();
    GregorianCalendar bdayThisYear = new GregorianCalendar();

    bday.setTime(birthday);
    bdayThisYear.setTime(birthday);
    bdayThisYear.set(Calendar.YEAR, today.get(Calendar.YEAR));

    int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

    if(today.getTimeInMillis() < bdayThisYear.getTimeInMillis())
        age--;

    return age;
}
	
}
