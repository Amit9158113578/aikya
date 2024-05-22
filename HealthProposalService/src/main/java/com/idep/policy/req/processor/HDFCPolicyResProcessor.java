package com.idep.policy.req.processor;




import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.carrier.req.processor.SoapUtils;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class HDFCPolicyResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HDFCPolicyResProcessor.class.getName());
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
			if(resNode.has("Envelope") && resNode.get("Envelope").get("Body").get("VerifyTransactionResponse").get("VerifyTransactionResult") != null ){
				String output = resNode.get("Envelope").get("Body").get("VerifyTransactionResponse").get("VerifyTransactionResult").asText();
				String arr[] = output.split("\\ |\\ ");
				if(arr[0].equalsIgnoreCase("SUCCESSFUL")){
					((ObjectNode) resNode).put("status","SUCCESSFUL");
					((ObjectNode) resNode).put("policyNo",arr[2]);
				}
				else{
					((ObjectNode) resNode).put("status","UNSUCCESSFUL");
					((ObjectNode) resNode).put("policyNo",0);
				}
			}
			JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty("inputRequest").toString());
			int carrierId = requestDocNode.findValue("carrierId").asInt();
			int productId = 0;
			if(requestDocNode.findValue("planId") != null){
				productId = requestDocNode.findValue("planId").asInt();
			}
			else{
				productId = requestDocNode.findValue("productId").asInt();
			}
			String requestType = null;

			if(!requestDocNode.has("documentType")){
				requestType = "HealthPolicyResponse";
			}
			else{
				requestType = requestDocNode.findValue("documentType").asText();
			}				

			((ObjectNode)requestDocNode).put(ProposalConstants.CARRIER_RESPONSE, resNode);
			log.info("requestDocNode :"+requestDocNode);
			JsonNode config = this.objectMapper.readTree(this.service.getDocBYId("JOLT-"+requestType+"-"+carrierId+"-"+productId).content().toString());
			ObjectNode object = SoapUtils.objectMapper.createObjectNode();
			object.put("inputRequest", requestDocNode);
			object.put("configuration", config.get("configuration"));
			log.info("HDFCQuoteResProcessor resNode: : "+object);
			exchange.getIn().setBody(object);	

		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"Carrier Quote Service Response "+exchange.getIn().getBody(String.class));
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+quoteServiceRes);
			throw new ExecutionTerminator();
		}

	}


}
