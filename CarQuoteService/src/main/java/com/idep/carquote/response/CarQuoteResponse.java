 package com.idep.carquote.response;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import org.apache.log4j.Logger;
 
 public class CarQuoteResponse {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarQuoteResponse.class.getName());
   public String sendQuoteResponse(String response) {
     return response;
   } public String sendQMessage(String qMessage) {
     return qMessage;
   } public String sendGenericResponse(String response) throws JsonProcessingException {
     return this.objectMapper.writeValueAsString(response);
   }
   public String requestError(String response) {
     JsonNode responseNode = null;
     this.log.info("requestError response : " + response);
     try {
       responseNode = this.objectMapper.readTree(response);
     } catch (JsonProcessingException e) {
       this.log.error("JsonProcessingException at CarQuoteResponse : requestError : ", (Throwable)e);
     } catch (IOException e) {
       this.log.error("IOException at CarQuoteResponse : requestError : ", e);
     } catch (Exception e) {
       this.log.error("Exception at CarQuoteResponse : requestError : ", e);
     } 
     return QuoteServiceResponse.createResponse(responseNode.get("responseCode").intValue(), responseNode.get("message").textValue(), responseNode.get("data"));
   }
 }


