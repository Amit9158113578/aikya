package com.idep.healthquote.carrier.req.processor;

import java.util.UUID;

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

public class IffcoTokioInputReqProcessor implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(IffcoTokioInputReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService productService = CBInstanceProvider.getProductConfigInstance();
	
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {	  
		    	String inputMessage = exchange.getIn().getBody().toString();
		    	log.info("message in IffcoTokioInputReqProcessor:" +inputMessage);
		    	JsonNode requestNode = this.objectMapper.readTree(inputMessage);
		    	log.info("requestNode:" +requestNode);
			    JsonNode productInfoNode = requestNode.get(HealthQuoteConstants.PRODUCT_INFO);
			    log.info("productInfoNode before findSADetails:" +productInfoNode);
			    log.info("PLANID = "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue());
			    log.info("CARRIERID = "+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue());
			    ((ObjectNode)productInfoNode).put("carrierQuoteId", getCarrierQuoteId(requestNode.get("personalInfo").get("selectedFamilyMembers").get(0).get("relation").textValue()));
			   JsonNode healthReqConfigNode = objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.CARRIER_HEALTH_QUOTE_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()).content().toString());
			    /**
			     * set request configuration document id HealthQuoteRequest
			     */
			    exchange.setProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,healthReqConfigNode);
			    exchange.setProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(requestNode));
			    exchange.getIn().setBody(requestNode);
				
		    }
		    catch (NullPointerException e)
		    {
		      this.log.error("NullPointerException at IffcoTokioInputReqProcessor : ", e);
		      throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		      this.log.error("Exception at IffcoTokioInputReqProcessor : ", e);
		      throw new ExecutionTerminator();
		    }
	  }
	
	public String getCarrierQuoteId(String relation)
	{
		String guiCode="";
		UUID uniqueKey = UUID.randomUUID();
		guiCode = uniqueKey.toString() ;
		log.info("IffcoTokiogetGUIDCode");
		return guiCode;
	}
}

