package com.idep.healthquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CalculateDeductibleProcessor implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CalculateDeductibleProcessor.class.getName());
	CBService productConfig = CBInstanceProvider.getProductConfigInstance();
	public void process(Exchange exchange) throws Exception 
	{
		try{
			String response = exchange.getIn().getBody(String.class);
			JsonNode responseNode = objectMapper.readTree(response);
			ObjectNode deductibleObjNode = null;
			JsonNode inputReqNode= objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.UI_QUOTEREQUEST).toString());
			
			log.debug("inputReqNode in CalculateDeductibleProcessor: "+inputReqNode);
			
			JsonNode healthPlanConfigNode = this.objectMapper.readTree(this.productConfig.getDocBYId(HealthQuoteConstants.CARRIER_PLAN+inputReqNode.findValue(HealthQuoteConstants.DROOLS_CARRIERID)+"-"+inputReqNode.findValue(HealthQuoteConstants.DROOLS_PLANID)).content().toString());
			
			if(healthPlanConfigNode.get("plans").get(0).has("deductibleList") && healthPlanConfigNode.get("plans").get(0).get("deductibleList").size()>0)
				{
				JsonNode deductibleList = healthPlanConfigNode.get("plans").get(0).get("deductibleList");
				
				deductibleObjNode = objectMapper.createObjectNode();				
				for(JsonNode deductibleNode : deductibleList)
				{
					deductibleObjNode.put(deductibleNode.get("sumInsured").asText(),deductibleNode.get("deductibleAmount"));
					
				}
				
				log.debug("deductibleObjNode CalculateDeductibleProcessor:" +deductibleObjNode);
				if(deductibleObjNode.has(responseNode.get("sumInsured").asText()))
					{
						((ObjectNode)responseNode).put("deductibleAmount",deductibleObjNode.get(responseNode.get("sumInsured").asText()));				
					}
				log.debug("Health Quote Deductible Processor response : "+responseNode);
				}
			
			exchange.getIn().setBody(responseNode);	
		}catch(Exception e){
			log.error("error at CalculateDeductibleProcessor :",e);
		}
		 
		
	}

}
