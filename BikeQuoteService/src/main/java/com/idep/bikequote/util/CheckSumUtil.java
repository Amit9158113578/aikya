package com.idep.bikequote.util;

import java.security.MessageDigest;

class CheckSumUtil
{
 
public static void main(String arg[])
{
 
	String strHash = null;
	String temp="NIAUAT|PROP000B128019|NA|1.00|NA|NA|NA|INR|NA|R|niauat|NA|NA|F|A1|1416002310015405|sandip.palodkar@infintus.com|8928704595|PROP000B128019|Sandip|PROP000B128019|https://www.billdesk.com/pgidsk/pgijsp/TESTResponse.jsp"; //Parameter string
	String commonstr="uyZx0gZKOveGod6pN5NOzXIXdRwKf2ju"; // Checksum Key
	try
	{
		strHash = checkSumSHA256(temp+"|"+commonstr);
	}
	catch(Exception error)
	{
		System.out.println(error.toString());
	}
	System.out.println("strHash==="+strHash);
}
 
public static String checkSumSHA256(String plaintext)  {
	MessageDigest md = null;
	try {
		md = MessageDigest.getInstance("SHA-256"); //step 2
		md.update(plaintext.getBytes("UTF-8")); //step 3
	} catch (Exception e) {
		md=null;
	}
 
	StringBuffer ls_sb=new StringBuffer();
	byte raw[] = md.digest(); //step 4
	for(int i=0;i<raw.length;i++)
		ls_sb.append(char2hex(raw[i]));
	return ls_sb.toString(); //step 6
	}
 
public static String char2hex(byte x)
{
	char arr[]={
 
	'0','1','2','3',
	'4','5','6','7',
	'8','9','A','B',
	'C','D','E','F'
	};
 
	char c[] = {arr[(x & 0xF0)>>4],arr[x & 0x0F]};
	return (new String(c));
}
 
}