package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HDFCTravelPlanCodeProcessor implements Processor
{
	Logger log = Logger.getLogger(HDFCTravelPlanCodeProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{
	try
		{
			String inputReq=exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			int dependentParentCount = 0;
			int noOfAdditionalKids = 0;
			log.debug("inputReqNode ::"+inputReqNode);
			JsonNode travellers = inputReqNode.get("travellerDetails");
		if(inputReqNode.get("premiumDetails").has("adultCount") && inputReqNode.get("premiumDetails").has("childCount"))
		{
			String adultCount = inputReqNode.get("premiumDetails").get("adultCount").asText();
			String childCount = inputReqNode.get("premiumDetails").get("childCount").asText();
			log.debug("adultCount: "+adultCount+"childCount "+childCount);
			String floaterPlanCode = adultCount+childCount;
			((ObjectNode)inputReqNode).put("FloaterPlanCode",floaterPlanCode);
		}
		else
		{
			log.info("adultCount and childCount not present in the request: ");
		}
		
		if(inputReqNode.get("premiumDetails").has("adultCount") && inputReqNode.get("premiumDetails").get("adultCount").intValue()>2)
		{
			dependentParentCount = inputReqNode.get("premiumDetails").get("adultCount").intValue()-2;
			
			if(dependentParentCount==2)
			{
				//for both Mother and Father DependentParent is both
				((ObjectNode)inputReqNode).put("dependentParentRelation","Both");
			}
			else if (dependentParentCount==1)
			{
				//if Adult is more than 2 and and dependentParentCount is 1 then it should be Mother or Father
				for(JsonNode member : travellers)
				{
					String memberRelation = member.get("relation").asText();
					if(memberRelation.equalsIgnoreCase("Mother")||memberRelation.equalsIgnoreCase("Father") )
					{
					((ObjectNode)inputReqNode).put("dependentParentRelation",memberRelation);
					log.debug("Caculated memberRelation is : "+memberRelation);
					break;
					}
				}
			}
		}
		else if(inputReqNode.get("premiumDetails").has("childCount") && inputReqNode.get("premiumDetails").get("childCount").intValue()>2)
		{
			noOfAdditionalKids = inputReqNode.get("premiumDetails").get("childCount").intValue()-2;
			((ObjectNode)inputReqNode).put("noOfAdditionalKids",noOfAdditionalKids);
		}
		else
		{
			log.info("No additional member is present");
		}
			exchange.getIn().setBody(inputReqNode);
		}
	catch(Exception e)
		{
		log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|HDFCTravelPlanCodeProcessor|",e);
		throw new ExecutionTerminator();
		}
	}
}
