package com.idep.healthquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.util.HealthQuoteConstants;

public class GenericResponseProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GenericResponseProcessor.class.getName());
	CBService service = null;
	JsonNode responseConfigNode;
	JsonNode errorNode;
	JsonNode HealthCarrierQNode=null;
	
	 @Override
	public void process(Exchange exchange) {
		 
		 try {
			 
				if (this.service == null)
			     {
			        this.service = CBInstanceProvider.getServerConfigInstance();
			        this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
			        this.log.info("ResponseMessages configuration loaded");
			        this.HealthCarrierQNode = this.objectMapper.readTree(this.service.getDocBYId(HealthQuoteConstants.HEALTH_CARRIERS_Q).content().toString());
			        this.log.info("HealthCarrierQNode configuration Loaded");
			     }
							 
				 ArrayNode qList = objectMapper.createArrayNode();
				 ArrayNode resQList = (ArrayNode)HealthCarrierQNode.get(HealthQuoteConstants.RES_Q_LIST);
				 
				 for(int i=0;i<resQList.size();i++)
				 {
					 ObjectNode resultNode = objectMapper.createObjectNode();
					 resultNode.put(HealthQuoteConstants.QNAME, resQList.get(i).textValue());
					 qList.add(resultNode);
				 }
				 
				 
				 ObjectNode finalresultNode = objectMapper.createObjectNode();
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_DATA, qList);
				 exchange.getIn().setBody(finalresultNode);
				 
			 }
			 catch(Exception e)
			 {
				 this.log.error(" Exception at GenericResponseProcessor : ", e);
			      ObjectNode objectNode = this.objectMapper.createObjectNode();
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			      exchange.getIn().setBody(objectNode);
			 }
	 }

}
