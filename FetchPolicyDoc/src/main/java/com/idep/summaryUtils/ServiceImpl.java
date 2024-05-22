package com.idep.summaryUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceImpl
{
  public String getRequest(String request)
  {
    return request;
  }
  
  public String sendResposne(String response)
  {
	  try{
			ObjectMapper mapper=new ObjectMapper();
			ObjectNode responseData = mapper.createObjectNode();
			responseData.put("responseCode", 1002);
			responseData.put("responseMsg", "Successfull create policy request :");
        return responseData.toString();
	}catch(Exception e)
	{
		e.printStackTrace();
		return response;
	}
  }
}
