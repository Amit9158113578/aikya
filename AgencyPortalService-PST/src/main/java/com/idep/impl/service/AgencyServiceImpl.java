package com.idep.impl.service;


import io.jsonwebtoken.Claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.Tokenizer.API.Tokenizer;


public class AgencyServiceImpl {
	
	
	 public String validateUser(String credential)
	  {
	    return credential;
	  } 
	  public String getUserDetails(String UserId)
	  {
	    return UserId;
	  }
	  public String fetchDetails(String roleId)
	  {
	    return roleId;
	  }
	  public String agencyMaint(String input)
	  {
	    return input;
	  }
	  public String agentMaint(String input)
	  {
	    return input;
	  }
	  public String activityMaint(String input)
	  {
		return input;
	  }
	  public String forgotPass(String input)
	  {
		return input;
	  }
	  public String createToken(String input)
	  { 
		ObjectMapper objectMapper = new ObjectMapper();
		String token=null;
		try{  
		JsonNode reqNode = objectMapper.readTree(input.toString());
		token=Tokenizer.createToken(reqNode.get("Username").asText());
        }catch(Exception e){}
      return token;
	  }
	  public String verifyToken(String input)
	  { ObjectMapper objectMapper = new ObjectMapper();
		Claims response=null;
		try{  
		JsonNode reqNode = objectMapper.readTree(input);
		response=Tokenizer.verifyToken(reqNode.get("token").asText());
      }catch(Exception e){}
    return response.toString();
	  }
}
