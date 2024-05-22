package com.idep.smsemail.request.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginServiceImpl {
ObjectMapper objectMapper = new ObjectMapper();
	
	public String loginDetails(String login)
	{
		JsonNode reqNode =null;
		
		try {
			
		reqNode = this.objectMapper.readTree(login);
		return reqNode.toString();
		
		}
		catch(Exception e)
		{
			return reqNode.toString();
		}
	}
	
	public String sendMessage(String login)
	{
		return login;
	}
}
