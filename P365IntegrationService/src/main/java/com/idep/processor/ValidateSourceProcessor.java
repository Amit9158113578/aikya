package com.idep.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ValidateSourceProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ValidateSourceProcessor.class.getName());
	static CBService service = null;
	static JsonNode serviceConfigNode = null;
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	    	service = CBInstanceProvider.getServerConfigInstance();
	    	serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId("P365IntegrationList").content()).toString());
	        String input = exchange.getIn().getBody().toString();
	        JsonNode reqNode = this.objectMapper.readTree(input);
	        log.info("Input Body for P365Integration Service :"+reqNode);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	        JsonNode responseDataByDeviceId = serviceConfigNode.get(reqNode.get("deviceId").asText());

	        	if(responseDataByDeviceId==null)
		        {   log.info("Response in validateSourceProcessor");
			        finalresultNode.put("responseCode",1001);
			        finalresultNode.put("message","Invalid DeviceId");
			        finalresultNode.put("data",responseDataByDeviceId);
		        	exchange.getIn().setBody(finalresultNode);
		        }
		        else
		        {   log.info("Response in validateSourceProcessor");
			        finalresultNode.put("responseCode", 1000);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data",responseDataByDeviceId);
		        	exchange.getIn().setBody(finalresultNode);
		        }
	    }
	    catch (Exception e)
	    {
		      this.log.error("Exception at ValidateSourceProcessor ", e);
		      ObjectNode finalresultNode = this.objectMapper.createObjectNode();  
		      finalresultNode.put("responseCode",1001);
		      finalresultNode.put("message", "Failure");
		      finalresultNode.put("data", e.getMessage());
		      exchange.getIn().setBody(finalresultNode);
	    }
	  }
}
