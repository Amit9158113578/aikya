package com.idep.response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SaveProposalServiceResponse {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(SaveProposalServiceResponse.class.getName());
	  
	public String sendGenericResponse(String response)
			    throws JsonProcessingException
			  {
			     return response;
			  }
}
