package com.idep.bikequote.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.bikequote.util.BikeQuoteConstants;

import java.io.IOException;
import org.apache.log4j.Logger;

public class BikeQuoteResponse {
  ObjectMapper objectMapper = new ObjectMapper();
  
  Logger log = Logger.getLogger(BikeQuoteResponse.class.getName());
  
  public String sendQuoteResponse(String response) {
    return response;
  }
  
  public String sendQMessage(String qMessage) {
    return qMessage;
  }
  
  public String sendGenericResponse(String response) throws JsonProcessingException {
    return this.objectMapper.writeValueAsString(response);
  }
  
  public String requestError(String response) {
    JsonNode responseNode = null;
    this.log.info("requestError response : " + response);
    try {
      responseNode = this.objectMapper.readTree(response);
    } catch (JsonProcessingException e) {
      this.log.error("JsonProcessingException at BikeQuoteResponse : requestError : ", (Throwable)e);
    } catch (IOException e) {
      this.log.error("IOException at BikeQuoteResponse : requestError : ", e);
    } catch (Exception e) {
      this.log.error("Exception at BikeQuoteResponse : requestError : ", e);
    } 
    return QuoteServiceResponse.createResponse(responseNode.get(BikeQuoteConstants.QUOTE_RES_CODE).intValue(), responseNode.get(BikeQuoteConstants.QUOTE_RES_MSG).textValue(), responseNode.get(BikeQuoteConstants.QUOTE_RES_DATA));
  }
}
