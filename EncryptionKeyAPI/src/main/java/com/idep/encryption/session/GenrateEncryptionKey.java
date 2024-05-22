package com.idep.encryption.session;

import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;

public class GenrateEncryptionKey {
  private static final String UNICODE_FORMAT = "UTF8";
  
  public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
  
  private KeySpec ks;
  
  private SecretKeyFactory skf;
  
  private Cipher cipher;
  
  byte[] arrayBytes;
  
  private String myEncryptionScheme;
  
  SecretKey myencryptedkey;
  
  public void EncryptionKey(String myEncryptionKey) throws Exception {
    this.myEncryptionScheme = "DESede";
    this.arrayBytes = myEncryptionKey.getBytes("UTF8");
    this.ks = new DESedeKeySpec(this.arrayBytes);
    this.skf = SecretKeyFactory.getInstance(this.myEncryptionScheme);
    this.cipher = Cipher.getInstance(this.myEncryptionScheme);
    this.myencryptedkey = this.skf.generateSecret(this.ks);
  }
  
  public String encrypt(String unencryptedString) {
    String encryptedString = null;
    try {
      this.cipher.init(1, this.myencryptedkey);
      byte[] plainText = unencryptedString.getBytes("UTF8");
      byte[] encryptedText = this.cipher.doFinal(plainText);
      encryptedString = new String(Base64.encodeBase64(encryptedText));
      encryptedString = encryptedString.replaceAll("/", "__").replaceAll("\\+", "qwqw").replace("\\&", "^^");
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return encryptedString;
  }
  
  public String decrypt(String encryptedString) {
    String decryptedText = null;
    try {
      this.cipher.init(2, this.myencryptedkey);
      encryptedString = encryptedString.replaceAll("__", "/").replaceAll("qwqw", "\\+").replace("^^", "\\&");
      byte[] encryptedText = Base64.decodeBase64(encryptedString);
      byte[] plainText = this.cipher.doFinal(encryptedText);
      decryptedText = new String(plainText);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    return decryptedText;
  }
  
  public static String GetEncryptedKey(String data, String userKey) {
    try {
      GenrateEncryptionKey td = new GenrateEncryptionKey();
      td.EncryptionKey(userKey);
      String encrypted = td.encrypt(data);
      return encrypted;
    } catch (Exception e) {
      System.out.println("Encyption failed");
      e.printStackTrace();
      return null;
    } 
  }
  
  public static String GetPlainText(String data, String userKey) {
    try {
      GenrateEncryptionKey td = new GenrateEncryptionKey();
      td.EncryptionKey(userKey);
      String plainText = td.decrypt(data);
      return plainText;
    } catch (Exception e) {
      System.out.println("Encyption failed");
      e.printStackTrace();
      return null;
    } 
  }
}
