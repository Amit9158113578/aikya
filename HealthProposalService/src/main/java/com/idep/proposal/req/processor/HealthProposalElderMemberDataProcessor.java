package com.idep.proposal.req.processor;

import java.text.SimpleDateFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HealthProposalElderMemberDataProcessor implements Processor
{
	Logger log = Logger.getLogger(HealthProposalElderMemberDataProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception {

		try {
			
			String proposalReq  = exchange.getIn().getBody(String.class);
			JsonNode proposalReqNode = objectMapper.readTree(proposalReq);
			
			String elderMemberDob = null;
			String realtionCode = null;
			int childCount = 0;
			int adultCount = 0;
			int totalCount = 0;
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			
			/**
			 * Find eldest member from insuredMembers
			 */
			for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
			{
				if (elderMemberDob == null)
				{
					elderMemberDob = insuredMember.get("dateOfBirth").asText();
				}
				if (formatter.parse(insuredMember.get("dateOfBirth").asText()).before(formatter.parse(elderMemberDob)))
				{
					elderMemberDob = insuredMember.get("dateOfBirth").asText();
				}		
			}
		((ObjectNode)proposalReqNode.get("proposerInfo")).put("elderMemberDob", elderMemberDob.toString());
		
		/**
		 * Find adult count ,child count and total count from insuredMembers
		 */
		for(JsonNode insuredMember : proposalReqNode.get("insuredMembers"))
		{
			realtionCode = insuredMember.get("relationshipCode").asText();
			float height = insuredMember.get("height").floatValue();
			float weight = insuredMember.get("weight").floatValue();
			if(realtionCode.equalsIgnoreCase("CH"))
			{
				childCount++;
				float bmi = getBMICode(height,weight);
				((ObjectNode)insuredMember).put("bmi", bmi);
		    }
			else
			{
			    adultCount++;
			    float bmi = getBMICode(height,weight);
				((ObjectNode)insuredMember).put("bmi", bmi);
			}
		}
		totalCount = childCount + adultCount;
		
		((ObjectNode)proposalReqNode).put("childCount", childCount);
		((ObjectNode)proposalReqNode).put("adultCount", adultCount);
		((ObjectNode)proposalReqNode).put("totalCount", totalCount);
			exchange.getIn().setBody(proposalReqNode);
		}
		
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"ERROR|"+"HealthProposalElderMemberDataProcessor : ",e);
			throw new ExecutionTerminator();
		}	
	}
	
	public float getBMICode(float hght,float wght)
	{
		float bmiCode;
		bmiCode = (float) (wght/Math.pow((hght/100), 2));
		log.info("bmiCode: "+bmiCode);
		return bmiCode;
	}
/*	public static void main(String[] args){
		HealthProposalElderMemberDataProcessor x = new HealthProposalElderMemberDataProcessor();
		System.out.println(x.getBMICode(172,72));
	}*/
	
}

