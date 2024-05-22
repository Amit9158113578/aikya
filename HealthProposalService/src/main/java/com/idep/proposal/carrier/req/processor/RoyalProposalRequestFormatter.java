package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.util.ProposalConstants;

public class RoyalProposalRequestFormatter implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(RoyalProposalRequestFormatter.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			String input = exchange.getIn().getBody(String.class);
			ArrayNode questionNode = null;
			ArrayNode finalQuestionList = null;
			JsonNode inputReq=objectMapper.readTree(input);
			
			ArrayNode insuredmemberList =(ArrayNode)inputReq.get("CalculatePremiumRequest").get("insuredDetailsData").get("insuredDetails"); 
			log.info("insuredmemberList "+insuredmemberList);
			for(JsonNode memberList : insuredmemberList)
			{	
				/**
				 * Removing empty additionMedicalQuestionInfo node 
				 * */
				questionNode = (ArrayNode)memberList.get("additionMedicalDetails").get("additionMedicalQuestionInfo");
				finalQuestionList = objectMapper.createArrayNode();
				for(JsonNode list : questionNode )
				{
					if(list.get("medicationDetail").asText().equalsIgnoreCase("Received") || list.get("medicationDetail").asText().equalsIgnoreCase("Receiving"))
						{
						finalQuestionList.add(list);
						}
				}
				log.info("Final finalQuestionList LIST Created : "+finalQuestionList);
				
				if (finalQuestionList.size()== 0)
				{
					((ObjectNode)memberList).remove("additionMedicalDetails");
				}
				else
				{				
				((ObjectNode)memberList.get("additionMedicalDetails")).put("additionMedicalQuestionInfo", finalQuestionList);
				}
	
			}
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERRES+"|SUCCESS|PROPOAL REQUEST MAPPING COMPLETED");
			exchange.getIn().setBody(objectMapper.writeValueAsString(inputReq));
			
		}
		catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"|ERROR|RoyalProposalRequestFormatter|",e);
		}
		
	}
}
