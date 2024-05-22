package com.idep.proposal.res.processor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.req.processor.LifeProposalReqProcessor;
import com.idep.proposal.util.ProposalConstants;


public class LifeProposalLeadHeaderSetter implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeProposalReqProcessor.class.getName());
	CBService productService = CBInstanceProvider.getServerConfigInstance();
	JsonNode configDocNode =null;
	
	public void process(Exchange exchange) throws Exception {
		try
		{
			String mapperResponse = exchange.getIn().getBody(String.class);
			JsonNode reqNode = this.objectMapper.readTree(mapperResponse);
			log.info("Input to LifeProposalLeadRequestCreation:"+reqNode);	
			
			
			//exchange.getIn().setHeader("documentId", "LifeLeadConfiguration-9-1");//add documrnt id 
			exchange.getIn().setHeader("transactionName", "paymentService");
			log.info("header transaction for LifeProposalLeadHeaderSetter:"+exchange.getIn().getHeader("transactionName"));
			
			exchange.getIn().setBody(reqNode);
		 	
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.LIFEPRORESHANDL+"|ERROR|"+"life proposal response handler failed:",e);
			throw new ExecutionTerminator();
			
		}
		
		}
}
