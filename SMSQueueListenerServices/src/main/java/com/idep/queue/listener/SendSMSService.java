package com.idep.queue.listener;

import com.couchbase.client.java.document.json.JsonObject;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

public class SendSMSService {
  String responseKey = null;
  
  Logger log = Logger.getLogger(SendSMSService.class.getName());
  
  CBService serverConfig = null;
  
  JsonObject smsConfigNode = null;
  
  public String sendSMS(String mobileNumber, String msg,String funcType) {
    if (this.serverConfig == null) {
      this.serverConfig = CBInstanceProvider.getServerConfigInstance();
      this.smsConfigNode = (JsonObject)this.serverConfig.getDocBYId("SMSAPI-Configurations").content();
    } 
    String response = null;
    String mobiles = mobileNumber;
    URLConnection myURLConnection = null;
    URL myURL = null;
    BufferedReader reader = null;
    String encoded_message = URLEncoder.encode(msg);
    String smsServiceUrl = this.smsConfigNode.getString("APIURL");
    StringBuilder sbPostData = new StringBuilder(smsServiceUrl);
    String DLT_TE_ID =  this.smsConfigNode.getString("DLT_"+funcType);
    sbPostData.append("authkey=" + this.smsConfigNode.getString("authKey"));
    sbPostData.append("&sender=" + this.smsConfigNode.getString("senderId"));
    sbPostData.append("&DLT_TE_ID=" + DLT_TE_ID);
    sbPostData.append("&mobiles=" + "91"+mobiles);
    sbPostData.append("&message=" + encoded_message);
    log.info("sbPostData: "+sbPostData);
    smsServiceUrl = sbPostData.toString();
    log.info("smsServiceUrl: "+smsServiceUrl);
    try {
      this.log.debug("prepare connection to send SMS");
      myURL = new URL(smsServiceUrl);
      myURLConnection = myURL.openConnection();
      myURLConnection.connect();
      reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
      while ((response = reader.readLine()) != null) {
        this.log.info("RESPONSE CODE : " + response);
        this.responseKey = response;
      } 
      this.log.debug("closing connection");
      reader.close();
      return this.responseKey;
    } catch (Exception e) {
      this.log.error("SendSMSService : Error sending SMS, please check", e);
      this.responseKey = "-1";
      return this.responseKey;
    } 
  }
}
