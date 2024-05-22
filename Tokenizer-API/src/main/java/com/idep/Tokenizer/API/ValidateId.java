package com.idep.Tokenizer.API;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.response.TokenizerResponse;

public class ValidateId {

	   ObjectMapper objectMapper = new ObjectMapper();
	   Logger log = Logger.getLogger(ValidateId.class.getName());
	   static CBService cbService;
	   static String[] validateNodeArray;
	   static JsonNode validateNode;
	   JsonNode errorNode;
	  static{
			  ObjectMapper obj = new ObjectMapper();
			  cbService=CBInstanceProvider.getServerConfigInstance();
			  JsonDocument docBYId = cbService.getDocBYId("ValidateLoginDetails");
	  try {
		     validateNode = obj.readTree(docBYId.content().toString());
			 validateNodeArray = validateNode.get("id").textValue().split(",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	public String validateId(String data) throws JsonProcessingException, IOException
	  {
		try {
			
			   JsonNode inputReq = objectMapper.readTree(data);
				String id = inputReq.get("id").asText();
				for (String validateNode : validateNodeArray) {
				if(validateNode.equals(id))
				{
					return data;
				}
			}
		}
		catch(NullPointerException e)
		{
			log.error("NullPointerException in validateId method in ValidateId class :",e);
			new Exception();
		}
		return null;
	}
}
