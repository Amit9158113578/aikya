package com.idep.webservice.redirect;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.master.util.MasterConstants;
public class MasterReqProcessor implements Processor
{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(MasterReqProcessor.class.getName());
	static CBService service = null;
	static JsonNode serviceConfigNode = null;
	static JsonNode sourceConfigNode = null;
	static JsonNode hostConfigNode = null;
	
	static
	{
		try
		{
			service = CBInstanceProvider.getServerConfigInstance(); 
			serviceConfigNode = objectMapper.readTree(service.getDocBYId(MasterConstants.SERVICE_URL_CONFIG_DOC).content().toString());
			hostConfigNode = objectMapper.readTree(service.getDocBYId(MasterConstants.HOST_CONFIG_DOC).content().toString());
			sourceConfigNode =objectMapper.readTree(service.getDocBYId(MasterConstants.SOURCE_CONFIG_DOC).content().toString());
		} 
		catch(Exception e)
		{
			log.error("Master Service not able to load ServiceURLConfig,WEBHOSTConfig document ",e);
		}
	}
		  
	public void process(Exchange inExchange) {
				    
	   try {
		   		String inputmsg = inExchange.getIn().getBody(String.class);
		   		JsonNode masterReqNode =  objectMapper.readTree(inputmsg);
		   		log.info("Master Service Request :"+inputmsg);
		   		JsonNode headerNode = masterReqNode.get(MasterConstants.REQUEST_HEADER);
		   		JsonNode inputBodyContent = masterReqNode.get(MasterConstants.REQUEST_BODY);
		   		
		   		inExchange.setProperty("setMasterProperty", "marathi");
	   		    if(sourceConfigNode.has(headerNode.get("deviceId").asText())){
					
		   			if(headerNode.has("source")&&headerNode.get("source").asText().equalsIgnoreCase("mobile"))
					{
						// skip origin validation for mobile application
						
						setServiceRequest(inExchange,headerNode,inputBodyContent);
					}
					else
					{
						if(hostConfigNode.has("validateOrigin")&& hostConfigNode.get("validateOrigin").asText().equalsIgnoreCase("Y"))
						{
					   		Object originHeader = inExchange.getIn().getHeader("Origin");
					   		String origin = originHeader.toString();
					   		boolean isReqValid = false;
					   		
							if(originHeader!=null)
							 {
								for(String originConfigHeader : hostConfigNode.get("origin").asText().split(","))
								{
									if(originConfigHeader.equalsIgnoreCase(origin))
									 {				
										isReqValid = true;
										break;
									 }
								}
								//applicable sub origin introduce for validate ramp sub origin 
								
									if(hostConfigNode.has("applicableSubOrigin"))
									{
											for(String subOrgine : hostConfigNode.get("applicableSubOrigin").asText().split(","))
											{
												if(origin.contains(subOrgine))
												 {				
													isReqValid = true;
													break;
												 }
											}
									}		
								 if(isReqValid)
								 {				
									 setServiceRequest(inExchange,headerNode,inputBodyContent);
									 log.info("Invoke Service : "+inExchange.getIn().getHeader(MasterConstants.SERVICE_URL).toString());//header.MServiceURL
								 }
								 else
								 {
									 log.info("Service Request Headers : "+inExchange.getIn().getHeaders());
									 log.error(origin+" HOST access is restricted");
								 }
							 }
						}
						else
						{
							// skip origin validation if marked as N in configuration
							setServiceRequest(inExchange,headerNode,inputBodyContent);
						}
						
					}
	   				
	   			}
	   			else{
	   				throw new Exception("Request does not have valid DeviceId");
	   			}
			}
		 catch (JsonProcessingException e)
		 {
			  	log.error("input request doesn't seems to be a valid JSON object.");
		 }
		 catch (IOException e)
		 {
			 	log.error("IOException at MasterReqProcessor",e);
		 }
		 catch (Exception e)
		 {
			 	log.error("Exception at MasterReqProcessor",e);
			 	log.info(e.getMessage());
		 }   
	}
	
	public void setServiceRequest(Exchange inExchange,JsonNode headerNode,JsonNode inputBodyContent) throws JsonProcessingException
	{    
			/**
			 * add messageId to request while submitting proposals. this will be used while creating policy after successful payment.
			 */
			try {
				if(headerNode.has("messageId")&& headerNode.has(MasterConstants.TRANS_NAME))
				{  
					if(headerNode.get(MasterConstants.TRANS_NAME).asText().equalsIgnoreCase("submitCarProposal")
					   ||headerNode.get(MasterConstants.TRANS_NAME).asText().equalsIgnoreCase("submitBikeProposal")
					   ||headerNode.get(MasterConstants.TRANS_NAME).asText().equalsIgnoreCase("submitHealthProposal")
					   ||headerNode.get(MasterConstants.TRANS_NAME).asText().equalsIgnoreCase("saveProposalService"))
					{ 
						((ObjectNode)inputBodyContent).put("leadMessageId",headerNode.get("messageId").asText());
					}
				}
			}
			catch(Exception e)
			{
				log.error("master service failed to attach messageId to input request still service will continue to server",e);
			}
			
			inExchange.getIn().setHeader(MasterConstants.SERVICE_URL, serviceConfigNode.get(headerNode.get(MasterConstants.TRANS_NAME).textValue()).textValue());
			inExchange.getIn().setHeader(MasterConstants.METHOD_HEADER, headerNode.get(MasterConstants.TRANS_NAME).textValue());
			inExchange.getIn().setHeader(MasterConstants.DEVICE_ID, headerNode.get(MasterConstants.DEVICE_ID).textValue());
			inExchange.getIn().setBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputBodyContent));
			if(headerNode.has(MasterConstants.BROWSER))
			{
				inExchange.getIn().setHeader(MasterConstants.BROWSER, headerNode.get(MasterConstants.BROWSER).textValue());
			}
	}
}