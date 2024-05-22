package com.idep.healthquote.form.req;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;


import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.req.transformer.SOAPRequestFormatter;
import com.idep.healthquote.util.HealthQuoteConstants;

public class FutureGeneraliSOAPReqProcessor implements Processor{
	Logger log = Logger.getLogger(FutureGeneraliSOAPReqProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  SoapConnector  soapService = new SoapConnector();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			String input = exchange.getIn().getBody().toString();
	    	JsonNode inputReqNode = this.objectMapper.readTree(input);
		    JsonNode productInfoNode = inputReqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		    ((ObjectNode)productInfoNode).put("carrierQuoteId",getCarrierQuoteId());
		JsonNode healthReqConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.CARRIER_HEALTH_QUOTE_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()).content().toString());
		   // log.info("healthReqConfigNode: "+healthReqConfigNode);
		    /**
		     * set request configuration document id HealthQuoteRequest
		     */
		    exchange.setProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,healthReqConfigNode);
		    log.info("inputReqNode.get(personalInfo).get(selectedFamilyMembers).toString() : "+inputReqNode.get("personalInfo").get("selectedFamilyMembers").toString());
		    ArrayNode selectedMember = (ArrayNode)objectMapper.readTree(inputReqNode.get("personalInfo").get("selectedFamilyMembers").toString());
		    log.info("selectedMember for FUTU "+selectedMember);
		    int memberCount=0;
		    for(JsonNode member : selectedMember ){
		    	++memberCount;
		    	((ObjectNode)member).put("memberNo", memberCount);
		    }
		    ((ObjectNode)inputReqNode.get("personalInfo")).put("selectedFamilyMembers",selectedMember);
		    log.info("Configuration Doc Added in  FutureGeneraliSOAPReqProcessor "+inputReqNode);
		    exchange.getIn().setBody(inputReqNode);
		}catch(Exception e){
			log.error("Error AT FutureGeneraliSOAPReqProcessor : ",e);
		}
	}
	
	public String getCarrierQuoteId()
	{
		String guiCode = "";
		UUID uniqueKey = UUID.randomUUID();
		guiCode = uniqueKey.toString();
		log.info("FutureGene UUID: "+guiCode);
		return guiCode;
	}

}
