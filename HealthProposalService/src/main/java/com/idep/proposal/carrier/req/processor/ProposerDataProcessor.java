package com.idep.proposal.carrier.req.processor;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


public class ProposerDataProcessor implements Processor {
	
	  Logger log = Logger.getLogger(ProposerDataProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			String proposalReq  = exchange.getIn().getBody(String.class);
			JsonNode proposalReqNode = objectMapper.readTree(proposalReq);
			JsonNode proposerInfo = proposalReqNode.get("proposerInfo");
			ArrayNode proposerArrayNode = objectMapper.createArrayNode();
			
			ObjectNode proposerDetailsNode = objectMapper.createObjectNode();
			
			proposerDetailsNode.put("firstName", proposerInfo.get("personalInfo").get("firstName").asText());
			//middleName field is missing in request
			if(proposerInfo.get("personalInfo").has("middleName"))
			{
				proposerDetailsNode.put("middleName", proposerInfo.get("personalInfo").get("middleName").asText());
			}
			else
			{
				proposerDetailsNode.put("middleName", "");
			}
			proposerDetailsNode.put("lastName", proposerInfo.get("personalInfo").get("lastName").asText());
			proposerDetailsNode.put("dateOfBirth", proposerInfo.get("personalInfo").get("dateOfBirth").asText());
			proposerDetailsNode.put("relationship", "Self");
			proposerDetailsNode.put("salutation", proposerInfo.get("personalInfo").get("salutation").asText());
			proposerDetailsNode.put("gender", proposerInfo.get("personalInfo").get("gender").asText());
			if(proposerInfo.get("personalInfo").has("pancard")){
				proposerDetailsNode.put("pancard", proposerInfo.get("personalInfo").get("pancard").asText());	
			}else{
				proposerDetailsNode.put("pancard", "");
			}
			
			proposerDetailsNode.put("roleCd", "PROPOSER");
			proposerDetailsNode.put("guidCd", getGUIDCode("Self"));
			
			
			
			/**
			 * add disease list to proposer node
			 */
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				if(insuredMember.get("relationship").asText().equalsIgnoreCase("Self"))
				{
					proposerDetailsNode.put("dieaseDetails", insuredMember.get("dieaseDetails"));
				}
			}
			/**
			 * add proposer node
			 */
			proposerArrayNode.add(proposerDetailsNode);
			
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				((ObjectNode)insuredMember).put("roleCd", "PRIMARY");
				
				((ObjectNode)insuredMember).put("guidCd", getGUIDCode(insuredMember.get("relationship").textValue()));
				if(proposerInfo.get("personalInfo").has("pancard")){
				((ObjectNode)insuredMember).put("pancard", proposerInfo.get("personalInfo").get("pancard").asText());
				}else{
						((ObjectNode)insuredMember).put("pancard", "");	
				}
			}
			
			proposerArrayNode.addAll((ArrayNode)proposalReqNode.get("insuredMembers"));
			/**
			 * override insured members array
			 */
			((ObjectNode)proposalReqNode).put("insuredMembers",proposerArrayNode);
			
			exchange.getIn().setBody(proposalReqNode);
		}
		
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ProposerDataProcessor|",e);
			throw new ExecutionTerminator();
		}
	
	
	}
	
	
	public String getGUIDCode(String relation)
	{
		String guiCode="";
		if(relation.equalsIgnoreCase("Self"))
		{
			guiCode = "D3714FF2-B24F-4463-905D-8E6B97145113";
		}
		else
		{
			UUID uniqueKey = UUID.randomUUID();
			guiCode = uniqueKey.toString() ;
		}
		
		return guiCode;
	}
	
public static void main(String[] args) {
	
	ProposerDataProcessor t = new ProposerDataProcessor();
	//System.out.println(t.getGUIDCode("Self"));
	
}
}
