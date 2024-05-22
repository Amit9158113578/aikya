package com.idep.healthquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;


public class ExternalServiceRespHandler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ExternalServiceRespHandler.class.getName());
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String carrierResponse = exchange.getIn().getBody(String.class);
			 JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			 JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST).toString());
			 JsonNode productInfo = requestDocNode.get(HealthQuoteConstants.PRODUCT_INFO);
			 ((ObjectNode)requestDocNode).put(HealthQuoteConstants.CARRIER_RESPONSE, carrierResNode);
			 // get sumInsured from MapperSumInsured header. we set this header while forming a request
			 ((ObjectNode)requestDocNode).put(HealthQuoteConstants.MAPPER_SUM_INSURED, objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.MAPPER_SUM_INSURED).toString()));
		      // set response configuration document id
		      exchange.setProperty(HealthQuoteConstants.CARRIER_REQ_MAP_CONF,HealthQuoteConstants.CARRIER_HEALTH_RES_CONF+productInfo.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+
						  "-"+productInfo.get(HealthQuoteConstants.DROOLS_PLANID).intValue());
			 
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
		      
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at ExternalServiceRespHandler : ", e);
			 throw new ExecutionTerminator();
		 }
		 
	 }
	

}
