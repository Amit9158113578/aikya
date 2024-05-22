 package com.idep.proposal.util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class SHA12Algorithm {
   public static String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
     String generatedPassword = null;
     try {
       MessageDigest md = MessageDigest.getInstance("SHA-512");
       md.update(salt.getBytes("UTF-8"));
       byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < bytes.length; i++)
         sb.append(Integer.toString((bytes[i] & 0xFF) + 256, 16).substring(1)); 
       generatedPassword = sb.toString();
     } catch (NoSuchAlgorithmException e) {
       e.printStackTrace();
     } catch (Exception e) {
       e.printStackTrace();
     } 
     return generatedPassword;
   }
 }


