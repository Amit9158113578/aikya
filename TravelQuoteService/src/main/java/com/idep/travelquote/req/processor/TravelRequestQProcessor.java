package com.idep.travelquote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelRequestQProcessor implements Processor{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(TravelRequestQProcessor.class.getName());
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
			exchange.setProperty(TravelQuoteConstants.LOG_REQ, "Travel|"+qMsgNode.findValue("carrierId").asText()+"|"+TravelQuoteConstants.QUOTE+"|"+qMsgNode.get(TravelQuoteConstants.QUOTE_ID).textValue()+"|");
			exchange.setProperty(TravelQuoteConstants.CORRELATION_ID, qMsgNode.get(TravelQuoteConstants.UNIQUE_KEY).textValue());
			exchange.setProperty(TravelQuoteConstants.QUOTE_ID, qMsgNode.get(TravelQuoteConstants.QUOTE_ID).textValue());
			exchange.setProperty(TravelQuoteConstants.UI_QUOTEREQUEST, qMsgNode.get(TravelQuoteConstants.INPUT_MESSAGE));
			log.info("request data for travel :"+qMsgNode);
			if(qMsgNode.get(TravelQuoteConstants.ENCRYPT_QUOTE_ID) != null){
				 exchange.setProperty(TravelQuoteConstants.ENCRYPT_QUOTE_ID, qMsgNode.get(TravelQuoteConstants.ENCRYPT_QUOTE_ID).textValue()); 
			 }
			
			exchange.getIn().setBody(qMsgNode.get(TravelQuoteConstants.INPUT_MESSAGE));
		}catch(Exception e){
			this.log.error("Exception at TravelRequestQProcessor : ", e);
		}
	}
	public String init(JsonNode quoteReqNode)
    {
    	String logData = new String();
		ArrayNode logNode = (ArrayNode)docConfigNode.get("logFields");
		String seperator = docConfigNode.get("seperateBy").asText();
		try {
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