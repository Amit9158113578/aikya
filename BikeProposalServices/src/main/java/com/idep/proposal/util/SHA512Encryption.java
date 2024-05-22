 package com.idep.proposal.util;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 public class SHA512Encryption
 {
   public static String encryptThisStringSHA512(String input) {
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-512");
       byte[] mdbytes = md.digest(input.getBytes());
       StringBuffer sb = new StringBuffer();
       for (int i = 0; i < mdbytes.length; i++)
         sb.append(Integer.toString((mdbytes[i] & 0xFF) + 256, 16).substring(1)); 
       System.out.println("Hex format : " + sb.toString());
       StringBuffer hexString = new StringBuffer();
       for (int j = 0; j < mdbytes.length; j++)
         hexString.append(Integer.toHexString(0xFF & mdbytes[j])); 
       System.out.println("Hex format : " + hexString.toString());
       return hexString.toString().toLowerCase();
     } catch (NoSuchAlgorithmException e) {
       throw new RuntimeException(e);
     } 
   }
   
   public static String encryptThisString(String input) {
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-512");
       byte[] messageDigest = md.digest(input.getBytes());
       BigInteger no = new BigInteger(1, messageDigest);
       String hashtext = no.toString(16);
       while (hashtext.length() < 128)
         hashtext = "0" + hashtext; 
       return hashtext.toLowerCase();
     } catch (NoSuchAlgorithmException e) {
       throw new RuntimeException(e);
     } 
   }
   
   public static void main(String[] args) throws Exception {
     String constant = "~";
     SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
     SimpleDateFormat optSdf = new SimpleDateFormat("dd-MMM-YYYY");
     SimpleDateFormat tpFormate = new SimpleDateFormat("dd-MM-yyyy");
     SimpleDateFormat expiryDateFormate = new SimpleDateFormat("dd/MM/yyyy");
     Date startDate = tpFormate.parse("22-02-2023");
     Date EndDate = tpFormate.parse("21-02-2028");
     Date prePoliyEND = expiryDateFormate.parse("22/03/2024");
     int insurerId = 12;
     String insurerName = "HDFC ERGO General Insurance Company Limited.";
     String policyNumber = "assdds24334sadds";
     int ncb = 20;
     String bajajExtcol36 = String.valueOf(optSdf.format(prePoliyEND)) + constant + insurerId + constant + insurerName + constant + policyNumber + constant + optSdf.format(EndDate) + constant + ncb + constant + '\005' + constant + optSdf.format(startDate);
     System.out.println(bajajExtcol36);
   }
 }


