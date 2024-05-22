package com.idep.proposal.res.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CarrierProposalResponseHandler implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(CarrierProposalResponseHandler.class.getName());
  
  public void process(Exchange exchange)  throws Exception {
	  
    try
    {
    	
    	String proposalResponse = (String)exchange.getIn().getBody(String.class);
        JsonNode carrierPropResNode = this.objectMapper.readTree(proposalResponse);
        
        JsonNode proposalReqNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
        ((ObjectNode)proposalReqNode).put(ProposalConstants.CARRIER_RESPONSE,carrierPropResNode);
      
        // set carrier proposal response
        exchange.setProperty(ProposalConstants.CARRIER_PROP_RES, this.objectMapper.writeValueAsString(carrierPropResNode));
        
        
        /**
         * set header for mapper to pick configuration document post proposal service
         */
        exchange.getIn().setHeader("documentId", "PostHealthProposalRequest-"+proposalReqNode.get(ProposalConstants.CARRIER_ID).asInt());
        
        /**
         * set carrier response in exchange body
         */
        exchange.getIn().setBody(proposalReqNode);
    	
    }
    
    catch(NullPointerException e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CarrierProposalResponseHandler:NullPointerException|",e);
		throw new ExecutionTerminator();
    }
    catch(JsonProcessingException e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CarrierProposalResponseHandler:JsonProcessingException|",e);
		throw new ExecutionTerminator();
    }
    catch(IOException e){
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CarrierProposalResponseHandler:IOException|",e);
		throw new ExecutionTerminator();
    }
    catch(Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CarrierProposalResponseHandler|",e);
		throw new ExecutionTerminator();
    }
    
  }

}
