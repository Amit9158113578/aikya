package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

public class CarrierDataLoader implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarrierDataLoader.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	 @Override
	public void process(Exchange exchange)  {
		 
		    try
		    {	        
		    	String proposalReq  = exchange.getIn().getBody(String.class);
				JsonNode requestNode = objectMapper.readTree(proposalReq);
		    	/**
		    	 * Get zone information from database
		    	 */
		        JsonNode healthZoneDetailsNode = objectMapper.readTree(serverConfig.getDocBYId("CityDetails-"+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+"-4-"+requestNode.get("proposerInfo").get("contactInfo").get("pincode").asText()).content().toString());
		        log.debug("Zone=:"+healthZoneDetailsNode.get("zone").asText());
		        /**
		         * Attach zone info to request node
		         */
		        ((ObjectNode)requestNode).put("zone", healthZoneDetailsNode.get("zone").asText());
		        exchange.getIn().setBody(requestNode);
		      
		    }
		    catch (NullPointerException e)
		    {
		      log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NullPointerException at CarrierDataLoader|",e);
		    }
		    catch (Exception e)
		    {
		      log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CarrierDataLoader|",e);
		    }
	  }
	 
}

