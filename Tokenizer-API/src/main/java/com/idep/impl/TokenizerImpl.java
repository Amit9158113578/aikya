package com.idep.impl;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.response.TokenizerResponse;
import com.idep.Tokenizer.API.Tokenizer;
import com.idep.data.searchconfig.cache.DocumentDataConfig;

public class TokenizerImpl {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(TokenizerImpl.class.getName());
	  JsonNode errorNode;
	  JsonNode responseConfigNode;
	  
	public String getToken(String data) throws JsonProcessingException, IOException
	  {
		try {
			
			JsonNode inputReq = objectMapper.readTree(data);
			
			 if(inputReq.has("id"))
			  {
					String id = inputReq.get("id").asText();
					String token = Tokenizer.createToken(id);
					JsonNode docDataNode = objectMapper.createObjectNode();
					((ObjectNode)docDataNode).put("token", token);
			        return TokenizerResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
			  }
			 else
			  {
				log.error("validation ramp id not found : "+inputReq);
				return TokenizerResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
			  }
	    }
	    catch(Exception e)
		{
	    	log.error("Exception occurred : ",e);
	    	return TokenizerResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
	    	
		}
	  }
	public String validateToken(String data) throws JsonProcessingException, IOException
	  {
		try {
			JsonNode inputReq = objectMapper.readTree(data);
			String token = inputReq.get("token").asText();
			Claims claims = Tokenizer.verifyToken(token);
	    	JsonNode docDataNode = objectMapper.createObjectNode();
	    	((ObjectNode)docDataNode).put("issuer", claims.getIssuer());
	    	((ObjectNode)docDataNode).put("tokenId", claims.getId());
	    	((ObjectNode)docDataNode).put("ttl", claims.getExpiration().toString());
	        return TokenizerResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
	    }
	    catch(Exception e)
		{
	    	log.error("Exception occurred : ",e);
	    	return TokenizerResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);	    	
		}
	  }
}
