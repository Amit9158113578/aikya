package com.idep.policy.payment.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HealthPaymentProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(HealthPaymentProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
    try
    {
      String policyRequest = exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
      log.info("reqNode is: "+reqNode);
      /**
       *  retrieve payment response and attached it to policy input request node
       */
      JsonObject carrierPaymentRes =null;
      
      try{
    	  carrierPaymentRes  = service.getDocBYId(reqNode.get("transactionStausInfo").get("apPreferId").asText()).content();
      }catch(Exception e){
    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|HealthPaymentProcessor|",e);
	      throw new ExecutionTerminator();
      }
      if(carrierPaymentRes==null){
    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|Document Not found|"+reqNode.get("transactionStausInfo").get("apPreferId").asText());
	      throw new ExecutionTerminator();
      }
      
      JsonNode carrierPaymentResponse = objectMapper.readTree((carrierPaymentRes).toString());
      ((ObjectNode)reqNode).put("carrierPaymentResponse", carrierPaymentResponse);
      log.info("carrierPaymentResponse: "+carrierPaymentResponse);
      log.info("carrierPaymentResponse in reqNode: "+reqNode);
      exchange.getIn().setBody(reqNode);
      
    }
    catch (Exception e)
    {
	      log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|HealthPaymentProcessor|",e);
	      throw new ExecutionTerminator();
	      
    }
  }
}

