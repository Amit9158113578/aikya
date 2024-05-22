package com.idep.healthquote.carrier.req.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ABHIDiamondDecideRouteProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIDiamondDecideRouteProcessor.class.getName());
	CBService serverConfig = null;
	CBService productData = CBInstanceProvider.getProductConfigInstance();
	JsonNode droolSumInsuredConfig=null;
	JsonNode droolURL = null;
	JsonNode errorNode;
	
	 @Override
	public void process(Exchange exchange)throws ExecutionTerminator, JsonProcessingException, IOException{
		 log.info("ABHIDiamondDecideRouteProcessor invoked.");
		 
		    try
		    {
		     
		      String quotedata = exchange.getIn().getBody().toString();
		      JsonNode reqNode = this.objectMapper.readTree(quotedata);
		      JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		      JsonDocument planConfigDoc = productData.getDocBYId(HealthQuoteConstants.CARRIER_PLAN+productInfoNode.get("carrierId")+"-"+productInfoNode.get("planId"));
		     
		      if(planConfigDoc==null){
		    	log.error("Unable to read Configuration document"+HealthQuoteConstants.CARRIER_PLAN+productInfoNode.get("carrierId")+"-"+productInfoNode.get("planId"));  
		    	throw new ExecutionTerminator();
		      }
		      
		      JsonNode planConfigDocNode =objectMapper.readTree(planConfigDoc.content().toString());
		      if(planConfigDocNode.has(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE))
		      {
		    	  log.info("carrierServiceApplicable key is found in productInfoNode");
		    	  ((ObjectNode)reqNode).put(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE, planConfigDocNode.get(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE).asText());
		    	  exchange.getIn().setHeader(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE, planConfigDocNode.get(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE).asText());
		    	 // log.info("ABHIDiamondDecideRouteProcessor Modified productInfoNode"+planConfigDocNode);
		      }
		      else
		      {
		    	  ((ObjectNode)reqNode).put(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE, "N");
		    	  exchange.getIn().setHeader(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE, "N");
		    	  
		    	  log.info("carrierServiceApplicable key is not found in productInfoNode");
		      }
		      
		      log.info("Header in exchange: "+exchange.getIn().getHeader(HealthQuoteConstants.DROOLS_CARRIER_SERVICE_APPLICABLE));
		      
		      exchange.getIn().setBody(reqNode);
		    }
		    catch(Exception e)
		    {
		    	log.error("Request node not found.");
		    }
		 }
	
	

}
