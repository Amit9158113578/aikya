 package com.idep.policy.download;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import java.io.IOException;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.TextMessage;
 import org.apache.log4j.Logger;
 
 public class PolicyDocumentReqQListener {
   Logger log = Logger.getLogger(PolicyDocumentReqQListener.class.getName());
   
   public String onMessage(Message message) throws JsonProcessingException, IOException {
     String request = "";
     try {
       if (message instanceof TextMessage) {
         TextMessage text = (TextMessage)message;
         request = text.getText();
       } 
     } catch (JMSException e) {
       this.log.error("Exception at PolicyReqQListener : ", (Throwable)e);
     } catch (Exception e) {
       this.log.error("Exception at PolicyReqQListener : ", e);
     } 
     this.log.info("request from PolicyDoc Q : " + request);
     return request;
   }
 }


