package com.idep.proposal.carrier.req.processor;



import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class HDFCQuoteResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HDFCQuoteResProcessor.class.getName());
	CBService service = null;
	JsonNode responseConfigNode;
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {

		String quoteServiceRes=null;
		try{
			if (this.service == null)
			{
				this.service = CBInstanceProvider.getServerConfigInstance();
				this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProposalConstants.RESPONSE_CONFIG_DOC).content().toString());
				this.log.info("ResponseMessages configuration loaded");

			}
			quoteServiceRes= exchange.getIn().getBody(String.class);
			JsonNode resNode =objectMapper.readTree(quoteServiceRes);
			log.info("resNode :"+resNode);
			if(resNode.get("Envelope").has("Body") && resNode.get("Envelope").get("Body").has("ProposalCaptureResponse") || 
					resNode.get("Envelope").has("Body") && resNode.get("Envelope").get("Body").has("PaymentDetailsResponse")){
				JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty("inputRequest").toString());
				int carrierId = requestDocNode.findValue("carrierId").asInt();
				int productId = requestDocNode.findValue("planId").asInt();
				String requestType = "HealthProposalResponse";
				
				if(exchange.getProperty("documentType") != null && exchange.getProperty("documentType").toString().equalsIgnoreCase("HealthPaymentRequest")){
					requestType = "HealthPaymentResponse";
				}
				((ObjectNode)requestDocNode).put(ProposalConstants.CARRIER_RESPONSE, resNode);
				log.info("requestDocNode :"+requestDocNode);
				JsonNode config = this.objectMapper.readTree(this.service.getDocBYId("JOLT-"+requestType+"-"+carrierId+"-"+productId).content().toString());
				ObjectNode object = SoapUtils.objectMapper.createObjectNode();
				object.put("inputRequest", requestDocNode);
				object.put("configuration", config.get("configuration"));
				log.info("HDFCQuoteResProcessor resNode: : "+object);
				exchange.getIn().setBody(object);	
			}else{
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(ProposalConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(ProposalConstants.FAIL_CONFIG_CODE).intValue());
				objectNode.put(ProposalConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(ProposalConstants.FAIL_CONFIG_MSG).textValue());
				objectNode.put(ProposalConstants.QUOTE_RES_DATA, "null");
				exchange.getIn().setBody(objectNode);
				log.info("ABHIQuoteResProcessor objectNode: : "+objectNode);
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"Carrier Quote Service Response "+exchange.getIn().getBody(String.class));
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+quoteServiceRes);
			throw new ExecutionTerminator();
		}

	}

}
