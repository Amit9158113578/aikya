 package com.idep.proposal.service;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class DecryptionSha512 {
   public boolean empty(String s) {
     if (s == null || s.trim().equals(""))
       return true; 
     return false;
   }
   
   public String hashCal(String type, String str) {
     byte[] hashseq = str.getBytes();
     StringBuffer hexString = new StringBuffer();
     try {
       MessageDigest algorithm = MessageDigest.getInstance(type);
       algorithm.reset();
       algorithm.update(hashseq);
       byte[] messageDigest = algorithm.digest();
       for (int i = 0; i < messageDigest.length; i++) {
         String hex = Integer.toHexString(0xFF & messageDigest[i]);
         if (hex.length() == 1)
           hexString.append("0"); 
         hexString.append(hex);
       } 
     } catch (NoSuchAlgorithmException nsae) {
       nsae.printStackTrace();
     } 
     return hexString.toString();
   }
   
   public String findOutHash(Map<String, String> params, String salt) {
     String hashSequence = "key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5|udf6|udf7|udf8|udf9|udf10";
     String[] hashVarSeq = hashSequence.split("\\|");
     String hashString = "";
     String hash = "";
     String txnid = params.get("txnid");
     byte b;
     int i;
     String[] arrayOfString1;
     for (i = (arrayOfString1 = hashVarSeq).length, b = 0; b < i; ) {
       String part = arrayOfString1[b];
       if (part.equals("txnid")) {
         hashString = String.valueOf(String.valueOf(String.valueOf(String.valueOf(hashString)))) + txnid;
       } else {
         hashString = empty(params.get(part)) ? hashString.concat("") : hashString.concat(((String)params.get(part)).trim());
       } 
       hashString = hashString.concat("|");
       b = (byte)(b + 1);
     } 
     hashString = hashString.concat(salt);
     hash = hashCal("SHA-512", hashString);
     System.out.println("key used : " + hashString);
     System.out.println("hash value : " + hash);
     return hash;
   }
   
   public static void main(String[] args) {
     String s = "";
     System.out.println("length : " + s.length());
     HashMap<String, String> hm = new HashMap<>();
     hm.put("key", "");
     hm.put("txnid", "");
     hm.put("amount", "");
     hm.put("productinfo", "");
     hm.put("key", "");
     hm.put("key", "");
     hm.put("key", "");
   }
 }


