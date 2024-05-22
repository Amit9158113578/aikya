package com.idep.lifequote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.lifequote.exception.processor.ExecutionTerminator;
import com.idep.lifequote.util.LifeQuoteConstants;


public class ExternalLifeServiceRespHandler implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ExternalLifeServiceRespHandler.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();;

	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String carrierResponse = exchange.getIn().getBody(String.class);
			 JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse); //read carrier response
			 
			 log.info("Inside SBIQuoteResponseHandler. Carrier Response:-"+carrierResNode);
			 
			 JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.UI_CARQUOTEREQUEST).toString());
			 JsonNode productInfo = requestDocNode.get(LifeQuoteConstants.PRODUCT_INFO);
			 ((ObjectNode)requestDocNode).put(LifeQuoteConstants.CARRIER_RESPONSE, carrierResNode);
			 ((ObjectNode)requestDocNode).put("requestType", "LifeQuoteResponse");
			 
			 log.info("Response Node is:-"+requestDocNode);
				JsonNode MaxPolicyTermConfig = this.objectMapper.readTree(this.serverConfig.getDocBYId("LifePolicyTermRecalcConfig-"+productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()).content().toString());

			 if(productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).asText().equalsIgnoreCase("43")){
				 if(requestDocNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM).get("policyTerm").asInt()>=MaxPolicyTermConfig.get("MaxPolicyTerm").asInt()){
					 ((ObjectNode)requestDocNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).put("policyTerm",MaxPolicyTermConfig.get("MaxPolicyTerm").asInt());
					 ((ObjectNode)requestDocNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).put("message",MaxPolicyTermConfig.get("message").asText());
				 }
		 }
			 exchange.getIn().setHeader("documentId", LifeQuoteConstants.CARRIER_LIFE_RES_CONF+productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()+
						  "-"+productInfo.get(LifeQuoteConstants.PRODUCTID).intValue());
			 
			 log.info("Constants are :-"+LifeQuoteConstants.CARRIER_LIFE_RES_CONF+""+productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()+
						  "-"+productInfo.get(LifeQuoteConstants.PRODUCTID).intValue());
		      
			 exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
		      
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at ExternalServiceRespHandler : ", e);
			 throw new ExecutionTerminator();
		 }
		 
	 }
	

}
