package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class NIAHealthProposalRequestFormatter  implements Processor {
	
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(NIAHealthProposalRequestFormatter.class.getName());
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();

	 public void process(Exchange exchange) throws Exception 
	 {
		 try
		 {
			 String carrierResponse = (String)exchange.getIn().getBody(String.class);
		     JsonNode carrierResponseNode = objectMapper.readTree(carrierResponse);
		     
		     JsonNode requestNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
		      /**
		       * put generated ProposalNum into proposalRequest  
		       */	    
		    
		      ((ObjectNode)requestNode).put("carrierPartyCode",carrierResponseNode.get("partyCode").textValue());
		     log.debug("carrierPartyCode: "+carrierResponseNode.get("partyCode").textValue());
	 	    
	          exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, objectMapper.writeValueAsString(requestNode));
	          /**
		       * Read Product ,Rider details from Product Data and append it into requestNode
		       * 
		       */
		      log.debug("HealthPlanDocId"+"HealthPlan-"+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+"-"+requestNode.get(ProposalConstants.PLAN_ID).intValue());
			  JsonNode HealthCarrierReqNode = objectMapper.readTree(productService.getDocBYId("HealthPlan-"+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+"-"+requestNode.get(ProposalConstants.PLAN_ID).intValue()).content().toString());
			  //JsonNode HealthCarrierReqNode = objectMapper.readTree(productService.getDocBYId("HealthPlan-35-21").content().toString());
	          ((ObjectNode)requestNode).put("HealthCarrierDetails",HealthCarrierReqNode);
	         
     		 
	            //String input=exchange.getIn().getBody(String.class);
				//JsonNode requestNode = objectMapper.readTree(input);
				
				if(requestNode.has("planId")){
					JsonDocument configDoc = serverConfig.getDocBYId("HealthProposalRequest-"+requestNode.get("carrierId")+"-"+requestNode.get("planId"));
					JsonNode configDocNode =objectMapper.readTree(configDoc.content().toString());

					if(configDocNode.has("sumAssuredConfig")){
                  	String threshold =null;
						if(configDocNode.has("sumAssuredConfig")){
                        ArrayNode planId = (ArrayNode)configDocNode.get("sumAssuredConfig");
							for(JsonNode ranges : planId){
								
								if(ranges.get("endValue").asDouble() >= requestNode.get("coverageDetails").get("sumAssured").asDouble() &&
										ranges.get("startValue").asDouble() <=	requestNode.get("coverageDetails").get("sumAssured").asDouble()){
									
									threshold = ranges.get("absoluteValue").asText();
								}
							}
							ArrayNode insuredMemberList = (ArrayNode)requestNode.get("insuredMembers");
                            for(JsonNode member : insuredMemberList ){

								((ObjectNode)member).put("threshold", threshold);
								log.info("threshold put found : "+threshold);

							}
							log.info("After threshold Updated INsured memebr List :  "+insuredMemberList);
							((ObjectNode)requestNode).put("insuredMembers", insuredMemberList);
							 exchange.getIn().setBody(requestNode);
							 
						}
					}
				}     
	        		  
			  exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestNode));
				log.info("After threshold add requestNode: "+requestNode);
				
			  
		 }
		 catch(Exception e)
		 {
			 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaHealthProposalRequestFormatter|",e);
				throw new ExecutionTerminator();
		 }
	 }
	
	

}
