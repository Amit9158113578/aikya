package com.idep.healthquote.response;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HealthQuoteResponse {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthQuoteResponse.class.getName());
	
	  public String sendQuoteResponse(String response) 
	  {
		  
		   return response;
	  }
	  
	  public String sendGenericResponse(String response) throws JsonProcessingException
	  {
		  
		   return objectMapper.writeValueAsString(response);
	  }
	  
	  public String sendQMessage(String qMessage)
	  {
	    return qMessage;
	  }
	  
	  public String requestError(String response)
	  {
	    JsonNode responseNode = null;
	    this.log.info("requestError response : " + response);
	    
	    try
	    {
	      responseNode = this.objectMapper.readTree(response);
	    }
	    catch (JsonProcessingException e)
	    {
	      this.log.error("JsonProcessingException at HealthQuoteResponse : requestError : ", e);
	    }
	    catch (IOException e)
	    {
		  this.log.error("IOException at HealthQuoteResponse : requestError : ", e);
	    }
	    catch (Exception e)
	    {
		  this.log.error("Exception at HealthQuoteResponse : requestError : ", e);
	    }
	    return QuoteServiceResponse.createResponse(responseNode.get("responseCode").intValue(), responseNode.get("message").textValue(), responseNode.get("data"));
	  }


}
