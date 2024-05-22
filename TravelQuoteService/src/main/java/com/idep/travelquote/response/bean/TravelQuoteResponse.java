package com.idep.travelquote.response.bean;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelQuoteResponse{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelQuoteResponse.class.getName());

	public String sendQuoteResponse(String response){
		return response;
	}

	public String sendQMessage(String qMessage){
		return qMessage;
	}

	public String sendGenericResponse(String response) throws JsonProcessingException{
		return objectMapper.writeValueAsString(response);
	}

	public String requestError(String response){
		JsonNode responseNode = null;
		this.log.info("requestError response : " + response);
		try{
			responseNode = this.objectMapper.readTree(response);
		}catch(JsonProcessingException e){
			this.log.error("JsonProcessingException at BikeRequestProcessor : requestError : ", e);
		}catch(IOException e){
			this.log.error("IOException at BikeRequestProcessor : requestError : ", e);
		}catch(Exception e){
			this.log.error("Exception at BikeRequestProcessor : requestError : ", e);
		}

		return QuoteServiceResponse.createResponse(responseNode.get(TravelQuoteConstants.QUOTE_RES_CODE).intValue(), responseNode.get(TravelQuoteConstants.QUOTE_RES_MSG).textValue(), responseNode.get(TravelQuoteConstants.QUOTE_RES_DATA));
	}
}