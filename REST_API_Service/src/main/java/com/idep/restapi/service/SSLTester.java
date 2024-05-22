package com.idep.restapi.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class SSLTester implements Processor {
  static Logger log = Logger.getLogger(SSLTester.class.getName());
  
  public static void main(String[] args) throws Exception {
    SSLBypass();
  }
  
  private static class DefaultTrustManager implements X509TrustManager {
    private DefaultTrustManager() {}
    
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }
  
  static void SSLBypass() {
    log.info("1");
    try {
      log.info("0");
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(new javax.net.ssl.KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
      SSLContext.setDefault(ctx);
      URL url = new URL("https://kgibridgeuat.kotakmahindrageneralinsurance.com/BPOS_USER_SERVICE/wsUserManagementServices.svc/Fn_Get_Service_Access_Token_For_User");
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      log.info("1");
      conn.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
              return true;
            }
          });
      log.info("2");
      conn.setRequestMethod("POST");
      conn.setRequestProperty("vRanKey", "6517636495586876");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);
      log.info("3");
      String jsonInputString = "{\"vLoginEmailId\": \"RVc0VHUxNGg4bm1waS9NOWlPeHZyQT09\", \"vPassword\": \"WHo5QkQxdm1Tb1NaRWE3YzRCbWV0dz09\"}";
      byte[] compressedData = jsonInputString.toString().getBytes();
      DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
      outputStream.write(compressedData);
      outputStream.flush();
      log.info("4");
      BufferedReader in = new BufferedReader(
          new InputStreamReader(conn.getInputStream()));
      StringBuffer response = new StringBuffer();
      String inputLine;
      while ((inputLine = in.readLine()) != null)
        response.append(inputLine); 
      in.close();
      log.info("SSL TESTER RESPONSE :" + response.toString());
      conn.disconnect();
      outputStream.close();
    } catch (Exception e) {
      log.info("Error SSLTESTER: " + e.getMessage());
      log.error("Ops!", e);
    } 
  }
  
  public void process(Exchange arg0) throws Exception {}
}
