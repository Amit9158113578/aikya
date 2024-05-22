 package com.idep.proposal.util;
 
 import com.idep.user.profile.processor.UserProfileReqProcessor;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import org.apache.log4j.Logger;
 
 public class MD5Encryption {
   static Logger log = Logger.getLogger(UserProfileReqProcessor.class.getName());
   
   private static String convertToHex(byte[] data) {
     StringBuffer buf = new StringBuffer();
     int i = 0; if (i < data.length) {
       int halfbyte = data[i] >>> 4 & 0xF;
       int two_halfs = 0;
       while (true) {
         if (halfbyte >= 0 && halfbyte <= 9) {
           buf.append((char)(48 + halfbyte));
         } else {
           buf.append((char)(97 + halfbyte - 10));
         } 
         halfbyte = data[i] & 0xF;
         if (two_halfs++ >= 1)
           i++; 
       } 
     } 
     return buf.toString();
   }
   
   public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception {
     MessageDigest md = MessageDigest.getInstance("MD5");
     byte[] md5hash = new byte[32];
     md.update(text.getBytes("iso-8859-1"), 0, text.length());
     md5hash = md.digest();
     return convertToHex(md5hash);
   }
   
   public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, Exception {
     System.out.println("md5 : " + MD5("@dmin_365P"));
   }
 }


