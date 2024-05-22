package com.idep.lifequote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;



import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeRequestQProcessor implements Processor{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(LifeRequestQProcessor.class.getName());
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static
	{
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		try {
			docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("LogConfiguration").content().toString());
		}
		catch(Exception e)
		{
			log.info("Failed to load Log Config Document"+e);
		}
	}

	public void process(Exchange exchange){
		try{
			String message = exchange.getIn().getBody().toString();
			JsonNode qMsgNode = objectMapper.readTree(message);
			exchange.setProperty(LifeQuoteConstants.CORRELATION_ID, qMsgNode.get(LifeQuoteConstants.UNIQUE_KEY).textValue());
			exchange.setProperty(LifeQuoteConstants.QUOTE_ID, qMsgNode.get(LifeQuoteConstants.QUOTE_ID).textValue());
			exchange.setProperty(LifeQuoteConstants.ENCRYPT_QUOTE_ID, qMsgNode.get(LifeQuoteConstants.ENCRYPT_QUOTE_ID).textValue());
			exchange.setProperty(LifeQuoteConstants.UI_CARQUOTEREQUEST, qMsgNode.get(LifeQuoteConstants.INPUT_MESSAGE));
			
			/**
			 * removed for professional journey , Response in sending Profession quote Id which is not required for Drool request
			 * **/
			if(qMsgNode.has("quoteParam")){
				if(qMsgNode.get("quoteParam").has("PROF_QUOTE_ID")){
					exchange.setProperty("PROF_QUOTE_ID", qMsgNode.get("quoteParam").get("PROF_QUOTE_ID").asText());
					 ((ObjectNode)qMsgNode.get("quoteParam")).remove("PROF_QUOTE_ID");
				}
			}
			
			exchange.getIn().setBody(qMsgNode.get(LifeQuoteConstants.INPUT_MESSAGE));
			 
			 /**
			  * set default log data in property
			  */
			 exchange.setProperty(LifeQuoteConstants.DEFAULT_LOG_DATA,init(qMsgNode));
			 
			 log.info("sending modified message to Life Response Q Message :  "+qMsgNode);
		}catch(Exception e){
			this.log.error("Exception at LifeRequestQProcessor : ", e);
		}
	}
	 public String init(JsonNode quoteReqNode)
	    {
	    	String logData = new String();
			ArrayNode logNode = (ArrayNode)docConfigNode.get("logFields");
			String seperator = docConfigNode.get("seperateBy").asText();
			try {
			if(docConfigNode.has("defaultValue"))	
			{
				logData = logData.concat(docConfigNode.get("defaultValue").asText());
				logData = logData.concat(seperator);
			}
				
			for(JsonNode node : logNode)
				{
					if(quoteReqNode.findPath(node.asText()) == null)
					{
						logData = logData.concat(seperator);
					}
					else
					{
						if(node.asText().equalsIgnoreCase("quoteType"))
						{
							
							if(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText())!=null)
							{
								logData = logData.concat(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()).asText());
								logData = logData.concat(seperator);
							}
						}
					
						else
						{
							logData = logData.concat(quoteReqNode.findPath(node.asText()).asText());	
							logData = logData.concat(seperator);
						}
					}
				}
			}
			catch(Exception e)
			{
				log.error("Error occurred while processing logging details ",e);
			}
			return logData;
	    }


}
