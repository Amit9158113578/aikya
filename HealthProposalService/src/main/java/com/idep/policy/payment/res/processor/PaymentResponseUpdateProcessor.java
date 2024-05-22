package com.idep.policy.payment.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *  this class created for Store Payment Service Response in DB as per format added new node PaymentResponse  
 */
public class PaymentResponseUpdateProcessor implements Processor {

	Logger log =  Logger.getLogger(PaymentResponseUpdateProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
	
				String inputReq=exchange.getIn().getBody(String.class);
				JsonNode inputReqNode = objectMapper.readTree(inputReq);
				log.debug("PaymentResponseUpdateProcessor input Node : "+inputReqNode);
				
				ObjectNode paymentResNode = objectMapper.createObjectNode();
				paymentResNode.putAll((ObjectNode)inputReqNode.get(ProposalConstants.TRANSSTATUSINFO));
				paymentResNode.put(ProposalConstants.DOCUMENT_TYPE, "paymentResponse");
				paymentResNode.put(ProposalConstants.PROPOSAL_ID,inputReqNode.get(ProposalConstants.TRANSSTATUSINFO).get(ProposalConstants.PROPOSAL_ID).asText());
				
				if(inputReqNode.get(ProposalConstants.TRANSSTATUSINFO).get(ProposalConstants.TRANSSTATUSCODE).asInt()==0){
					
					paymentResNode.put("proposalStatus", "paymentFail");
					log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|"+"Payment response received - PaymentFail : "+inputReqNode.toString());
					throw new ExecutionTerminator();
				}else{
					paymentResNode.put("proposalStatus", "paymentSuccess");
				}
				
				exchange.getIn().setBody(paymentResNode);
	
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|PaymentResponseUpdateProcessor|",e);		
		}
	}
}
