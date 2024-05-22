package com.idep.healthquote.carrier.req.processor;

/**
 * @author pravin.jakhi
 * 
 * below class is wrote for Get Relationship code send at the time for proposal request.
 * eg.
 * Self = M041
 * Self,Spouse,Son = M013
 * Self,Spoouse,Father=M017
 * 
 * (HealthRealtionCodeMappingConf-60) document used for get realtion.
 *  
 *  
 *  
 * */

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
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ABHIMemberCodeProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIMemberCodeProcessor.class.getName());
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String input=exchange.getIn().getBody(String.class);
			JsonNode inputReq = objectMapper.readTree(input);
			log.debug("inputReq ABHI MEMBER CODE : "+inputReq);
			
			
			JsonNode personalInfo = inputReq.get("personalInfo");
			
			ArrayNode insuredMember = (ArrayNode)personalInfo.get("selectedFamilyMembers");
			String realtionCode="";
			String planCode ="";
			JsonDocument realtiondoc = serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_RELATIONCODE_MAPPING_CONF+"60");
			if(realtiondoc !=null){
				ArrayNode memberList = (ArrayNode)objectMapper.readTree(realtiondoc.content().get(HealthQuoteConstants.HEALTH_RELATIONCODE_MEMBERLIST).toString());
				JsonNode memberCode = objectMapper.readTree(realtiondoc.content().get(HealthQuoteConstants.HEALTH_RELATIONCODE_MEMBERCODED_CONFIG).toString());
				String size=String.valueOf(insuredMember.size());
				

				if(memberCode.has(inputReq.get(HealthQuoteConstants.PRODUCT_INFO).get("planId").asText()))
				{
					if(inputReq.get("productInfo").get("planId").asText() == "67")
					{
						
						if(insuredMember.size()==1 && insuredMember.get(0).get("relation").asText().equalsIgnoreCase("Self")){
							realtionCode = memberCode.get("67").get("A").asText();
							planCode=realtionCode;
						}else{
							for(JsonNode data : memberList){
								for(JsonNode UICode : insuredMember){
								if(UICode.get("relation").asText().equalsIgnoreCase(data.get("relation").asText())){
									realtionCode=realtionCode+data.get("letter").asText();
									}
								}
							}
							planCode=memberCode.get("67").get(realtionCode).asText();							
						}
					}
					else
					{
						
						if(insuredMember.size()==1 && insuredMember.get(0).get("relation").asText().equalsIgnoreCase("Self")){
							realtionCode = memberCode.get("68").get("A").asText();
							planCode=realtionCode;
						}else{
							for(JsonNode data : memberList){
								for(JsonNode UICode : insuredMember){
								if(UICode.get("relation").asText().equalsIgnoreCase(data.get("relation").asText())){
									realtionCode=realtionCode+data.get("letter").asText();
									
									}
								}
							}
							planCode=memberCode.get("68").get(realtionCode).asText();
							
						}
					}
				}
				else
				{
					
				if(insuredMember.size()==1 && insuredMember.get(0).get("relation").asText().equalsIgnoreCase("Self")){
					realtionCode = memberCode.get("common").get("A").asText();
					planCode=realtionCode;
					
				}else{
					for(JsonNode data : memberList){
						for(JsonNode UICode : insuredMember){
						if(UICode.get("relation").asText().equalsIgnoreCase(data.get("relation").asText())){
							realtionCode=realtionCode+data.get("letter").asText();
							
							}
						}
					}
					planCode=memberCode.get("common").get(realtionCode).asText();
				}
				}
				
				log.debug("realtionCode IN ABHI QUOTE : "+realtionCode);
			}
			if(inputReq.get("productInfo").has("childPlanId")){


				log.debug("config Doc For CHILD PLAN ID  : "+inputReq.get("productInfo").get("childPlanId"));
				JsonDocument configDoc = serverConfig.getDocBYId("HealthQuoteRequest-"+inputReq.get("productInfo").get("carrierId")+"-"+inputReq.get("productInfo").get("planId"));
				JsonNode configDocNode =objectMapper.readTree(configDoc.content().toString());				
				log.debug("config Doc For ABHi : "+configDocNode);
				
				if(configDocNode.has("deductibleConfig")){
					
					if(configDocNode.get("deductibleConfig").has(inputReq.get("productInfo").get("childPlanId").asText())){
						
						ArrayNode childPlan = (ArrayNode)configDocNode.get("deductibleConfig").get(inputReq.get("productInfo").get("childPlanId").asText());
						
						for(JsonNode ranges : childPlan){
							log.debug("ranges for ABHI : "+ranges);
							if(ranges.get("endValue").asDouble() >= inputReq.get("productInfo").get("sumInsured").asDouble() &&
							   ranges.get("startValue").asDouble() <=	inputReq.get("productInfo").get("sumInsured").asDouble()){
								log.debug("Values set as Deductible fro ABHi Plan   : "+ranges.get("absoluteValue").asText());	
								((ObjectNode)inputReq.get("productInfo")).put("deductible",ranges.get("absoluteValue").asText());
							}
						}
					}

				}
				
				
			}
			
			/**
			 * changes for Deducible amount recived from UI 
			 * Deducible present in quote param, if user change value on but screen
			 * */
			JsonNode quoteParam = inputReq.get("quoteParam");
			if(quoteParam.has("deductible")){
				
				if(inputReq.get("productInfo").has("deductible")){
					log.debug("quoteParam.get(deductible) : "+quoteParam.get("deductible"));
						((ObjectNode)inputReq.get("productInfo")).put("deductible",quoteParam.get("deductible"));
					}

				}
			log.debug("inputReq ABHI Chnaged after deductible : "+inputReq);
			
			
			((ObjectNode)personalInfo).put("memberCode", planCode);
			((ObjectNode)inputReq).put("personalInfo", personalInfo);
		log.debug("Member Code Added in Quote Request proposerInfo Input Req : "+planCode);
			exchange.getIn().setBody(inputReq);
		}catch(Exception e){
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"ABHIMemberCodeProcessor : ",e);
			throw new ExecutionTerminator();
		}
	}
}
