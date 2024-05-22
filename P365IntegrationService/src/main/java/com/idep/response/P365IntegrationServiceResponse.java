package com.idep.response;

import org.apache.log4j.Logger;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P365IntegrationServiceResponse {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(P365IntegrationServiceResponse.class.getName());
	  
	public String sendGenericResponse(String response)
			    throws JsonProcessingException
			  {
			    return this.objectMapper.writeValueAsString(response);
			  }
}
