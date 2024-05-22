package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CarrierDataLoader implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarrierDataLoader.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {	        
		      String quotedata = exchange.getIn().getBody().toString();
		      log.debug("quotedata in CarrierDataLoader:" +quotedata);
		      JsonNode reqNode = objectMapper.readTree(quotedata);
		      JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		      log.debug("PinCODE:= " +reqNode.get("personalInfo").get("pincode").toString());
		      log.debug("PinCODE asText:= " +reqNode.get("personalInfo").get("pincode").asText());
		      log.debug("DocID:= "+"CityDetails-"+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-4-"+reqNode.get("personalInfo").get("pincode").asText());
		      JsonNode healthZoneDetailsNode = objectMapper.readTree(serverConfig.getDocBYId("CityDetails-"+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-4-"+reqNode.get("personalInfo").get("pincode").asText()).content().toString());
		      log.debug("healthZoneDetailsNode: "+healthZoneDetailsNode);
		      log.debug("Zone=:"+healthZoneDetailsNode.get("zone").asText());
		     ((ObjectNode)reqNode.get("personalInfo")).put("zone", healthZoneDetailsNode.get("zone").asText());
		      exchange.getIn().setBody(reqNode);
		      
		      
		    }
		    catch (NullPointerException e)
		    {
		      log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"CarrierDataLoader : ",e);
		      throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		      log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"CarrierDataLoader : ",e);
		      throw new ExecutionTerminator();
		    }
	  }
	 
}

		    
