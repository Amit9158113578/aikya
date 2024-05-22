package com.idep.policy.payment.res.processor;

/***
 * 
 * @author pravin.jakhi
 * 
 * class created for check Payment is success or fail.
 * 
 * */
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


public class PaymentResponseValidator implements Processor{

	Logger log =  Logger.getLogger(PaymentResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			
			if(inputReqNode.get(ProposalConstants.TRANSSTATUSINFO).get(ProposalConstants.TRANSSTATUSCODE).asInt()==0){
				ObjectNode obj = this.objectMapper.createObjectNode();
				exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.FALSE);
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|"+"Payment response received - PaymentFail : "+inputReqNode.toString());
			    exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.FALSE);
				obj.put(ProposalConstants.PROPOSAL_RES_CODE,ProposalConstants.CARRIER_RES_CODE );
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(ProposalConstants.SUCC_CONFIG_MSG).asText());
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, "");
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
				
			}else{
				  exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.TRUE);
				  exchange.getIn().setBody(inputReqNode);
			}
			
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|",e);
			throw new ExecutionTerminator();
		}
	}
}
