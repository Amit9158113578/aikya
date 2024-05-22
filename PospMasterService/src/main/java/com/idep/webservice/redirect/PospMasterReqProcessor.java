package com.idep.webservice.redirect;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.master.util.MasterConstants;
public class PospMasterReqProcessor implements Processor
{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(PospMasterReqProcessor.class.getName());
	static CBService PospData = null;
	static CBService ServerConfig = null;
	static JsonNode serviceConfigNode = null;
	static JsonNode sourceConfigNode = null;
	static JsonNode hostConfigNode = null;

	static
	{
		try
		{
			PospData = CBInstanceProvider.getBucketInstance("PospData");
			ServerConfig = CBInstanceProvider.getBucketInstance("ServerConfig");
			serviceConfigNode = objectMapper.readTree(PospData.getDocBYId(MasterConstants.SERVICE_URL_CONFIG_DOC).content().toString());
			hostConfigNode = objectMapper.readTree(PospData.getDocBYId(MasterConstants.HOST_CONFIG_DOC).content().toString());
			sourceConfigNode =objectMapper.readTree(ServerConfig.getDocBYId(MasterConstants.SOURCE_CONFIG_DOC).content().toString());
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
			log.info("Posp Master Service Request :"+inputmsg);
			JsonNode headerNode = masterReqNode.get(MasterConstants.REQUEST_HEADER);
			JsonNode inputBodyContent = masterReqNode.get(MasterConstants.REQUEST_BODY);

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

							if(isReqValid)
							{				
								setServiceRequest(inExchange,headerNode,inputBodyContent);
								log.info("POSP Invoke Service : "+inExchange.getIn().getHeader(MasterConstants.SERVICE_URL).toString());//header.MServiceURL
							}
							else
							{
								log.info("Posp Service Request Headers : "+inExchange.getIn().getHeaders());
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
				throw new Exception("Posp Request does not have valid DeviceId");
			}
		}
		catch (JsonProcessingException e)
		{
			log.error("Posp input request doesn't seems to be a valid JSON object.");
		}
		catch (Exception e) {
			log.error("Exception at posp MasterReqProcessor : ",e);
			log.info(e.getMessage());
		}   
	}

	public void setServiceRequest(Exchange inExchange,JsonNode headerNode,JsonNode inputBodyContent) throws JsonProcessingException
	{    

		inExchange.getIn().setHeader(MasterConstants.SERVICE_URL, serviceConfigNode.get(headerNode.get(MasterConstants.TRANS_NAME).textValue()).textValue());
		inExchange.getIn().setHeader(MasterConstants.METHOD_HEADER, headerNode.get(MasterConstants.TRANS_NAME).textValue());
		inExchange.getIn().setHeader(MasterConstants.DEVICE_ID, headerNode.get(MasterConstants.DEVICE_ID).textValue());
		inExchange.getIn().setBody(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputBodyContent));

	}
}