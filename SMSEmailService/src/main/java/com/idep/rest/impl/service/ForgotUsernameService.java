package com.idep.rest.impl.service;

/*import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;*/
import com.fasterxml.jackson.databind.ObjectMapper;
/*import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.idep.smsemail.request.bean.ForgotUserInput;
import com.idep.smsemail.response.bean.ForgotUserResponse;*/
//import com.idep.sync.rest.impl.SyncGatewayRestClient;
//import com.idep.sync.rest.impl.SyncGatewayTransInstance;

/*import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;*/

import org.apache.log4j.Logger;

public class ForgotUsernameService
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(ForgotUsernameService.class.getName());
  //SyncGatewayRestClient restClient = null;
  
//@SuppressWarnings("unchecked")
public String forgotUsername(String data)
   
  {
	 /* this.log.info("forgotUsername method invoked");
    ForgotUserResponse response = new ForgotUserResponse();
    
    try
    {
      ForgotUserInput userinput = (ForgotUserInput)this.objectMapper.readValue(data, ForgotUserInput.class);
      String username = null;
      this.restClient = SyncGatewayTransInstance.getSyncGatewayInstance();
      String content = this.restClient.executeView("", userinput.getMobileNumber());
      Map<String, Object> viewMap = objectMapper.readValue(content, Map.class);
      this.log.info("viewMap from db : " + viewMap.toString());
      
      List<Map<String, Object>> userdata = (List<Map<String, Object>>)viewMap.get("rows");
      this.log.debug("userdata : " + userdata.toString());
      
	      if (!userdata.isEmpty())
	      {
		    	username = userdata.get(0).get("value").toString();
		        this.log.info("username is : " + username);
		        response.setFuncType(userinput.getFuncType());
		        response.setMobileNumber(userinput.getMobileNumber());
		        response.setUsername(username);
		        HashMap<String, String> paramMap = new HashMap<String, String>();
		        paramMap.put("USERNAME", username);
		        response.setParamMap(paramMap);
		        this.log.info(this.objectMapper.writeValueAsString(response));
		        return this.objectMapper.writeValueAsString(response);
	      }
	      else
	      {
		      this.log.info("Please Enter Registered Mobile Number");
		      response.setUsername("Invalid");
		      response.setMobileNumber("0");
		      return this.objectMapper.writeValueAsString(response);
	      }
    }
    catch (JsonParseException e)
    {
      e.printStackTrace();
      this.log.error("unable to parse JSON input provided, unexpected character occurred");
      response.setMobileNumber("0");
      response.setUsername("ERROR");
      return this.objectMapper.writeValueAsString(response);
    }
    catch (InvalidFormatException e)
    {
      e.printStackTrace();
      this.log.error("Please check input values, It seems data type is wrong in provided value");
      response.setMobileNumber("0");
      response.setUsername("ERROR");
      return this.objectMapper.writeValueAsString(response);
    }
    catch (JsonMappingException e)
    {
      e.printStackTrace();
      this.log.error("Please check input values. Unrecognized field occurred, unable to map input values to bean");
      response.setMobileNumber("0");
      response.setUsername("ERROR");
      return this.objectMapper.writeValueAsString(response);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      this.log.error("IOException occurred, unable to write response");
      response.setMobileNumber("0");
      response.setUsername("ERROR");
      return this.objectMapper.writeValueAsString(response);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      this.log.error("Exception occurred, please analyze logs");
      response.setMobileNumber("0");
      response.setUsername("ERROR");
      return this.objectMapper.writeValueAsString(response);
    }*/
    
	 return null;
  }
}
