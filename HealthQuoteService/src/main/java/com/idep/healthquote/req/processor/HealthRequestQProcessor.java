package com.idep.healthquote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class HealthRequestQProcessor implements Processor {
	
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(HealthRequestQProcessor.class.getName());
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
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String message = exchange.getIn().getBody().toString();
			 JsonNode qMsgNode = objectMapper.readTree(message);
			
			 /**
			  *  set all required properties in exchange to retrieve it further
			  */
			 exchange.setProperty(HealthQuoteConstants.CORRELATION_ID, qMsgNode.get(HealthQuoteConstants.UNIQUE_KEY).textValue());
			 exchange.setProperty(HealthQuoteConstants.RATINGS, qMsgNode.get(HealthQuoteConstants.HEALTH_RATINGS));
			 exchange.setProperty(HealthQuoteConstants.UI_QUOTEREQUEST, this.objectMapper.writeValueAsString(qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE)));
			 exchange.setProperty(HealthQuoteConstants.QUOTE_ID, qMsgNode.get(HealthQuoteConstants.QUOTE_ID).asText());
			 exchange.setProperty(HealthQuoteConstants.ENCRYPT_QUOTE_ID, qMsgNode.get(HealthQuoteConstants.ENCRYPT_QUOTE_ID).asText());
			 exchange.setProperty("HealthProductUIReq", objectMapper.writeValueAsString(qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE)));

			 /**
			  * Add for Exception and accessible from other component 
			  */
			 exchange.setProperty(HealthQuoteConstants.PRODUCT_CARRIERID, qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE).get(HealthQuoteConstants.PRODUCT_INFO).get(HealthQuoteConstants.DROOLS_CARRIERID).asText());
			 exchange.setProperty(HealthQuoteConstants.PRODUCT_PLANID, qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE).get(HealthQuoteConstants.PRODUCT_INFO).get(HealthQuoteConstants.DROOLS_PLANID).asText());
			 exchange.setProperty(HealthQuoteConstants.PRODUCT_CHILDPLANID, qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE).get(HealthQuoteConstants.PRODUCT_INFO).get(HealthQuoteConstants.DROOL_CHILDPLANID).asText());
			 
			 /**
			  * set input message in exchange body
			  */
			 exchange.getIn().setBody(qMsgNode.get(HealthQuoteConstants.INPUT_MESSAGE));
			 
			 /**
			  * set default log data in property
			  */
			 exchange.setProperty(HealthQuoteConstants.DEFAULT_LOG,init(qMsgNode));
			 exchange.setProperty(HealthQuoteConstants.LOG_REQ, "Health|"+qMsgNode.findValue("carrierId").asText()+"|QUOTE"+"|"+exchange.getProperty(HealthQuoteConstants.QUOTE_ID).toString()+"|");
			// log.info("sending modified message to Health Response Q Message :  "+qMsgNode);
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at HealthRequestQProcessor : \tCarrierId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CARRIERID)+"\tPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_PLANID)+"\tChildPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CHILDPLANID),e);
			 throw new ExecutionTerminator();
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
