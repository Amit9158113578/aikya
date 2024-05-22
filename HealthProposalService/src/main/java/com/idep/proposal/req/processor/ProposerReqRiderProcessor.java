package com.idep.proposal.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ProposerReqRiderProcessor implements Processor {
	
	Logger log = Logger.getLogger(ProposerReqRiderProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			String proposalReq  = exchange.getIn().getBody(String.class);
			JsonNode proposalReqNode = objectMapper.readTree(proposalReq);
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.RIDERPROCESS+"|INIT|Rider process Started");
			if(proposalReqNode.get("coverageDetails").has("riders"))
			{
				JsonNode riderList = proposalReqNode.get("coverageDetails").get("riders");
				if(riderList.size()>0){
				
				StringBuffer addon = new StringBuffer();
				for(JsonNode rider : riderList)
				{
					if(rider.get("riderId").asInt()==32)
					{
						addon.append("CAREWITHNCB").append(",");
					}
					else if(rider.get("riderId").asInt()==33)
					{
						addon.append("UAR").append(",");
					}
					else if(rider.get("riderId").asInt()==17)
					{
						addon.append("PA").append(",");
					}
					
				}
				
				((ObjectNode)proposalReqNode).put("addOns",addon.toString().substring(0, addon.toString().length()-1));
				}
			}
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.RIDERPROCESS+"|SUCCESS|Rider process completed");
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERREQ+"|INIT|Mapper request transform started");
			exchange.getIn().setBody(proposalReqNode);
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|Exception at ProposerReqRiderProcessor|",e);
			throw new ExecutionTerminator();
		}
		
}	

}
