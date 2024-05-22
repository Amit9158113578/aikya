package com.idep.proposal.res.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.req.processor.LifeProposalReqProcessor;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class LifeProposalLeadRequestCreation
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(LifeProposalReqProcessor.class.getName());
  CBService productService = CBInstanceProvider.getServerConfigInstance();
  JsonNode configDocNode = null;
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
      String mapperResponse = (String)exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(mapperResponse);
      this.log.info("Input to LifeProposalLeadRequestCreation:" + reqNode);
      
      String inputRequest = exchange.getProperty("LifeProposalRequest").toString();
      this.log.info("The carrier input request:" + inputRequest);
      
      JsonNode inputReq = this.objectMapper.readTree(inputRequest);
      
      ((ObjectNode)reqNode).put("CarrierInputRequest", inputReq);
      ((ObjectNode)reqNode).put("documentType", "LifeLeadConfigurationRequest");
     // ((ObjectNode)reqNode).put("carrierId", exchange.getProperty("carrierId").toString());
      ((ObjectNode)reqNode).put("carrierId",exchange.getProperty(ProposalConstants.CARRIER_ID).toString());
      ((ObjectNode)reqNode).put("planId", exchange.getProperty(ProposalConstants.PRODUCT_ID).toString());
      /*
	  
	  ((ObjectNode)reqNode).put("carrierId",exchange.getProperty(ProposalConstants.CARRIER_ID).toString());
		//((ObjectNode)reqNode).put("planId",exchange.getProperty(ProposalConstants.PRODUCT_ID).toString());
		((ObjectNode)reqNode).put("planId",1);
	  */
      log.info("Doc Fetched:"+ "LifeLeadConfiguration"+"-"+exchange.getProperty(ProposalConstants.CARRIER_ID).toString()+"-"+exchange.getProperty(ProposalConstants.PRODUCT_ID).toString());
      exchange.getIn().setHeader("documentId", "LifeLeadConfiguration"+"-"+exchange.getProperty(ProposalConstants.CARRIER_ID).toString()+"-"+exchange.getProperty(ProposalConstants.PRODUCT_ID).toString());
      exchange.getIn().setHeader("transactionName", "paymentService");
      this.log.info("header transaction:" + exchange.getIn().getHeader("transactionName"));
      
      exchange.getIn().setBody(reqNode);
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "LIFEPRORESHANDL" + "|ERROR|" + "life proposal response handler failed:", e);
      throw new ExecutionTerminator();
    }
  }
}
