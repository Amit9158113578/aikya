package com.idep.proposal.carrier.res.processor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.req.processor.LifeProposalReqProcessor;
import com.idep.proposal.util.ProposalConstants;

public class ProposalRedirectionProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeProposalReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode configDocNode =null;
	
	public void process(Exchange exchange) throws Exception {
		try{
			String mapperResponse = exchange.getIn().getBody(String.class);
			JsonNode reqNode = this.objectMapper.readTree(mapperResponse);
			
			//fetching proposal request from property
			String proposalRequest=exchange.getProperty(ProposalConstants.LIFE_PROPOSAL_REQUEST).toString();
			JsonNode propReq = this.objectMapper.readTree(proposalRequest);			
			String KotakQuoteNumber=propReq.get("premiumDetails").get("KotakQuoteNumber").asText();
			String carrierId=propReq.get("carrierId").asText();
			String productId=propReq.get("productId").asText();
			
			//fetching document for redirectionURL creation eg. LifeProposalRedirectionConfig-53-1
			JsonDocument configDocument = serverConfig.getDocBYId(ProposalConstants.LIFE_PROPOSAL_REDIRECTION_CONFIG+"-"+carrierId+"-"+productId);
			configDocNode = objectMapper.readTree(configDocument.content().toString());
			String downloadURL = configDocNode.get("redirectionURL").asText();
					
			//String KotakQuoteNumber=exchange.getProperty("KotakQuoteNumber").toString();	
		    if(configDocNode.has("fieldNameReplacement"))
						{
							for(JsonNode fieldName : configDocNode.get("fieldNameReplacement"))
							{
							downloadURL = downloadURL.replace(fieldName.get("destFieldName").asText(), KotakQuoteNumber);
							}
							
						}
		    
		    ((ObjectNode)reqNode).put("redirectionUrl",downloadURL);
		    
		    exchange.getIn().setBody(reqNode);
		
		}
	catch(Exception e)
	{
		log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.LIFEPRORESHANDL+"|ERROR|"+"life proposal response handler failed:",e);
		throw new ExecutionTerminator();
	}	
}
}
