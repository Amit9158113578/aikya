 package com.idep.soap.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import javax.jms.Message;
 import javax.jms.TextMessage;
 import org.apache.log4j.Logger;
 
 public class CalcServiceImpl {
   Logger log = Logger.getLogger(CalcServiceImpl.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   JsonNode reqInfoNode;
   
   public String calculateResponse(String quotedata) {
     return quotedata;
   }
   
   public String onMessage(Message message) throws Exception {
     try {
       if (message instanceof TextMessage) {
         TextMessage text = (TextMessage)message;
         this.reqInfoNode = this.objectMapper.readTree(text.getText());
       } 
     } catch (Exception e) {
       this.log.error("Exception at CalcServiceImpl : ", e);
     } 
     return this.reqInfoNode.toString();
   }
 }


