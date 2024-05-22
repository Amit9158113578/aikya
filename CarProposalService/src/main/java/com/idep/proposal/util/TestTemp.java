 package com.idep.proposal.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.commons.lang.StringUtils;
 
 public class TestTemp {
   public static void main(String[] args) {
     String policyNo = "P365";
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
     Date date = new Date();
     System.out.println("date :" + date);
     String currentDate = dateFormat.format(date);
     System.out.println("current date : " + currentDate);
     long proposal_seq = 1001L;
     System.out.println(String.valueOf(String.valueOf(String.valueOf(policyNo))) + currentDate + proposal_seq);
     String name = "sandeep jadhav";
     System.out.println(StringUtils.capitalize(name));
   }
 }


