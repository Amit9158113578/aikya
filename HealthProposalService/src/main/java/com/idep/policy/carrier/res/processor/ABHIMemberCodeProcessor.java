package com.idep.policy.carrier.res.processor;

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
import com.idep.policy.carrier.req.processor.ABHIProposalReqStoreProcessor;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ABHIMemberCodeProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIProposalReqStoreProcessor.class.getName());
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			log.info("ABHIMemberCodeProcessor invoked");
			String input=exchange.getIn().getBody(String.class);
			JsonNode inputReq = objectMapper.readTree(input);
			
			String proposalId = inputReq.get("proposalId").asText();
			exchange.setProperty("proposalId", proposalId);
			 ArrayNode insuredMember = (ArrayNode)objectMapper.readTree(inputReq.get("insuredMembers").toString());
			String realtionCode="";
			String planCode ="";
			JsonDocument realtiondoc = serverConfig.getDocBYId(ProposalConstants.PROPOSAL_MEMBER_CODE_CONF);
			if(realtiondoc !=null){
				ArrayNode memberList = (ArrayNode)objectMapper.readTree(realtiondoc.content().get("memberList").toString());
				JsonNode memberCode = objectMapper.readTree(realtiondoc.content().get("memberCodeConfig").toString());
				
				if(memberCode.has(inputReq.get(ProposalConstants.PLAN_ID).asText()))
				{ 
					
						JsonNode planWiseCode = memberCode.get(inputReq.get(ProposalConstants.PLAN_ID).asText());
				
						for(JsonNode data : memberList){
							for(JsonNode UICode : insuredMember){
								if(UICode.get("relationship").asText().equalsIgnoreCase(data.get("relation").asText())){
									realtionCode=realtionCode+data.get("letter").asText();
						}
					}
				}
				
						planCode=planWiseCode.get(realtionCode).asText();
				}
				else
				{
						JsonNode commonFieldCode = memberCode.get("common");
						for(JsonNode data : memberList){
							for(JsonNode UICode : insuredMember){
								if(UICode.get("relationship").asText().equalsIgnoreCase(data.get("relation").asText())){
									realtionCode=realtionCode+data.get("letter").asText();
						}
					}
				}
						planCode=commonFieldCode.get(realtionCode).asText();
				}
				
			}else{
				log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|Document not found in DB | HealthRealtionCodeMappingConf-60");
			}
			JsonNode proposerInfo= inputReq.get("proposerInfo");
	((ObjectNode)proposerInfo.get("personalInfo")).put("memberCode", planCode);
		((ObjectNode)inputReq).put("proposerInfo", proposerInfo);
		log.debug("Member Code Added in proposerInfo Input Req : "+planCode);
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERREQ+"|INIT|PROPOAL REQUEST SEND TO MAPPER");
			exchange.getIn().setBody(inputReq);
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIMemberCodeProcessor|",e);
			throw new ExecutionTerminator();
		}
	}
}
