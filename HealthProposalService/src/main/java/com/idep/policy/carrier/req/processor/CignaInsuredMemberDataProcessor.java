package com.idep.policy.carrier.req.processor;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CignaInsuredMemberDataProcessor implements Processor {
	
	  Logger log = Logger.getLogger(CignaInsuredMemberDataProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {

		try {
			
			String proposalReq  = exchange.getIn().getBody(String.class);
			JsonNode proposalReqNode = objectMapper.readTree(proposalReq);
			
			/**
			 * Add bmi,roleCd,partyId fields for insuredMember
			 */
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				((ObjectNode)insuredMember).put("roleCd", "PRIMARY");
				((ObjectNode)insuredMember).put("partyId", getGUIDCode(insuredMember.get("relationship").textValue()));
				
				float height = insuredMember.get("height").floatValue();
				float weight = insuredMember.get("weight").floatValue();
				float bmi = getBMICode(height,weight);
				((ObjectNode)insuredMember).put("bmi", bmi);
			}
			

			JsonNode proposerInfo = proposalReqNode.get("proposerInfo");
			
			/**
			 * separate out last name and middle name
			 */
			String lastMidName = proposerInfo.get("personalInfo").get("lastName").asText();
			String nameArr [] = lastMidName.split(" ");
			String middleName = nameArr[0];
			String lastName = "";
			
			for(int i=1;i<nameArr.length;i++)
			{
				lastName = lastName.concat(nameArr[i]);
			}
			
			
			/**
			 * Create Proposer info node
			 */
			ObjectNode proposerDetailsNode = objectMapper.createObjectNode();
			proposerDetailsNode.put("firstName", proposerInfo.get("personalInfo").get("firstName").asText());
			if(lastName.equalsIgnoreCase(""))
			{
				lastName=middleName;
				middleName="";
			}	
			proposerDetailsNode.put("lastName", lastName);
			proposerDetailsNode.put("middleName", middleName);
			proposerDetailsNode.put("dateOfBirth", proposerInfo.get("personalInfo").get("dateOfBirth").asText());
			proposerDetailsNode.put("relationship", "Self");
			proposerDetailsNode.put("salutation", proposerInfo.get("personalInfo").get("salutation").asText());
			proposerDetailsNode.put("gender", proposerInfo.get("personalInfo").get("gender").asText());
			proposerDetailsNode.put("occupation", proposerInfo.get("personalInfo").get("occupation").asText());
			proposerDetailsNode.put("martialStatus", proposerInfo.get("personalInfo").get("martialStatus").asText());
			proposerDetailsNode.put("roleCd", "PROPOSER");
			
			
			/**
			 * Add partyId field value for Proposer 
			 */
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				if(proposalReqNode.get("insuredMembers").get(0).get("relationship").textValue().equalsIgnoreCase("Self"))
				{
					proposerDetailsNode.put("partyId", proposalReqNode.get("insuredMembers").get(0).get("partyId").textValue());
				}
				else
				{
					proposerDetailsNode.put("partyId", getGUIDCode("Self"));
				}
			}
			
			//pancard field is missing in request
			/**
			 * Add pancard field value for Proposer 
			 */
			if(proposerInfo.get("personalInfo").has("pancard"))
			{
				proposerDetailsNode.put("pancard", proposerInfo.get("personalInfo").get("pancard").asText());
			}
			else
			{
				proposerDetailsNode.put("pancard", "");
			}		
			
			ArrayNode proposerRoleCodeArrayNode = objectMapper.createArrayNode();
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				ObjectNode proposerRoleCodeNode = objectMapper.createObjectNode();
				proposerRoleCodeNode.put("roleCd", insuredMember.get("roleCd").textValue());
				proposerRoleCodeNode.put("partyId", insuredMember.get("partyId").textValue());
				proposerRoleCodeArrayNode.add(proposerRoleCodeNode);
			}
			/**
			 * Attach PROPOSER info node to proposerRoleCodeArrayNode
			 */
			proposerRoleCodeArrayNode.add(proposerDetailsNode);
			((ObjectNode)proposalReqNode).put("proposerRoleCode",proposerRoleCodeArrayNode);
			
			/**
			 * partyDoList
			 */
			ArrayNode partyDoListArrayNode = objectMapper.createArrayNode();
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				if(insuredMember.get("relationship").textValue().equalsIgnoreCase("Self"))
				{
					continue;
				}
				else
				{
					partyDoListArrayNode.add(insuredMember);
				}
			}
			/**
			 * Attach PROPOSER info node to partyDoListArrayNode
			 */
			partyDoListArrayNode.add(proposerDetailsNode);
			((ObjectNode)proposalReqNode).put("proposerInsuredMembers",partyDoListArrayNode);
			((ObjectNode)proposalReqNode.get("proposerInfo")).put("guidCode", proposalReqNode.get("insuredMembers").get(0).get("partyId").textValue());
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERREQ+"|INIT|CignaInsuredMemberDataProcessor|MAPPER REQUEST Started");
			exchange.getIn().setBody(proposalReqNode);
		}
		
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaInsuredMemberDataProcessor|",e);
			throw new ExecutionTerminator();
		}	
	}
	
	public String getGUIDCode(String relation)
	{
		String guiCode="";
		UUID uniqueKey = UUID.randomUUID();
		guiCode = uniqueKey.toString() ;
		log.info("getGUIDCode");
		return guiCode;
	}
	
	public float getBMICode(float hght,float wght)
	{
		float bmiCode;
		bmiCode = (float) (wght/Math.pow((hght/100), 2));
		log.info("bmiCode: "+bmiCode);
		return bmiCode;
	}
	
}
