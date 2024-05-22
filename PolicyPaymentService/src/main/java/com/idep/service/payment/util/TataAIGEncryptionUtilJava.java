 package com.idep.service.payment.util;
 
 import java.io.UnsupportedEncodingException;
 import javax.crypto.Cipher;
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.bind.DatatypeConverter;
 
 public class TataAIGEncryptionUtilJava {
   public String encrypt(String instaReq) {
     StringBuilder nString = new StringBuilder();
     String encrInstaResp = null;
     try {
       String lKey = "#g%t8&(k$^";
       SecretKeySpec lKeySpec = new SecretKeySpec(lKey.getBytes("UTF8"), "Blowfish");
       Cipher lCipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
       lCipher.init(1, lKeySpec);
       byte[] lPassword = instaReq.getBytes("UTF8");
       byte[] lEncryptPassword = lCipher.doFinal(lPassword);
       encrInstaResp = DatatypeConverter.printBase64Binary(lEncryptPassword);
       for (int i = 0; i < encrInstaResp.length(); i++) {
         int a = encrInstaResp.charAt(i);
         if (a != 13 && a != 10 && !" ".equals(encrInstaResp.substring(i, i + 1)))
           nString.append(encrInstaResp.charAt(i)); 
       } 
     } catch (Exception lException) {
       System.out.println("PGI Exception : " + lException);
     } 
     return nString.toString();
   }
   
   public String decrypt(String instaResp) throws UnsupportedEncodingException {
     byte[] utf8 = null;
     try {
       String lKey = "#g%t8&(k$^";
       SecretKeySpec lKeySpec = new SecretKeySpec(lKey.getBytes("UTF8"), "Blowfish");
       Cipher lCipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
       lCipher.init(2, lKeySpec);
       byte[] dec = DatatypeConverter.parseBase64Binary(instaResp);
       utf8 = lCipher.doFinal(dec);
     } catch (Exception lException) {
       System.out.println("PGI Exception : " + lException);
     } 
     return new String(utf8, "UTF8");
   }
   
   public static void main(String[] args) {
     try {
       TataAIGEncryptionUtilJava test = new TataAIGEncryptionUtilJava();
       String Encrypt = "{'lob':'1','currencyCode':'INR','reqID':'dXozPpVHzzVCA8WZIRy4KnA1j','sourceReturnUrl':'https://www.integapp.com/bin/PaymentResponse','policyNumber': 'PHP/3122/0000000021','transTimeStamp': '22-05-2018 19:14:27.619', 'paymentType': '1','portalName': '1', 'businessType': '1','paymentAmount': '1', 'productCode': '1002', 'directPayment': 'N', 'consumer_app_ID': 'WEB001', 'transactionID': 'dXozPpVHzzVCA8WZIRy4KnA1j','additionalInfo1': '', 'additionalInfo2': null, 'additionalInfo3': ''}";
       String Decrypt = test.encrypt(Encrypt);
       System.out.println("Eecrypted string =" + Decrypt);
       String javade = test.decrypt(Decrypt);
       System.out.println("Decrypted string =" + javade);
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }
 }


