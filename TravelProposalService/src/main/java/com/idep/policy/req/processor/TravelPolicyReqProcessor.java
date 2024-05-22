package com.idep.policy.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
/**
 * @author sandeep jadhav
 *
 */
public class TravelPolicyReqProcessor implements Processor

{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelPolicyReqProcessor.class.getName());
	CBService service =  CBInstanceProvider.getPolicyTransInstance();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static String ExceptionHandlerQ = "ExceptionHandlerQ";

	public void process(Exchange exchange) throws ExecutionTerminator {
		CamelContext camelContext = exchange.getContext();
		ProducerTemplate template = camelContext.createProducerTemplate();
		JsonNode reqNode = null;
		try
		{
			String policyRequest = exchange.getIn().getBody(String.class);
			reqNode = this.objectMapper.readTree(policyRequest);
			log.debug("TravelPolicyReqProcessor reqNode: "+reqNode);
			/**
			 *  retrieve proposal response and attached it to policy input request node
			 */

			JsonObject carrierPropRes=null;
			JsonNode carrierProposalResponse =null;
			carrierPropRes= service.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText()).content();

			if(carrierPropRes!=null)
			{
				carrierProposalResponse = objectMapper.readTree(carrierPropRes.get("travelProposalResponse").toString());
				log.debug("TravelPolicyReqProcessor carrierProposalResponse: "+carrierProposalResponse);
				exchange.setProperty(ProposalConstants.TravelProposalID, carrierPropRes);


			}else
			{
				log.debug(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|unable to load Proposal Document :  "+reqNode.get("transactionStausInfo").get("proposalId").asText());
				throw new ExecutionTerminator();
			}

			((ObjectNode)reqNode).put("carrierProposalResponse", carrierProposalResponse);
			((ObjectNode)reqNode).put("documentType", "carrierProposalResponse");
			((ObjectNode)reqNode).put("requestType", "TravelPolicyRequest");
			((ObjectNode)reqNode).put("proposalId", reqNode.get("transactionStausInfo").get("proposalId").asText());
			((ObjectNode)reqNode).put("planId", reqNode.get("planId").intValue());
			// exchange.getIn().setBody(reqNode);   

			/**
			 * add policy no if carrier provides it in payment response
			 */

			if(!(reqNode.has(ProposalConstants.CARRIER_ID)))
			{

				log.debug(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|carrier id is missing in request , Added from Propoosal "+reqNode.get("transactionStausInfo").get("proposalId").asText());

				((ObjectNode)reqNode).put(ProposalConstants.CARRIER_ID, carrierPropRes.getInt(ProposalConstants.CARRIER_ID));
				//throw new ExecutionTerminator();
			}
			if(!(reqNode.has(ProposalConstants.PLAN_ID)))
			{
				log.debug(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|plan id is missing in request , Added from Propoosal|"+reqNode.get("transactionStausInfo").get("proposalId").asText());
				//log.info("plan id is missing in request , Added from Propoosal "+reqNode.get("transactionStausInfo").get("proposalId").asText());
				((ObjectNode)reqNode).put(ProposalConstants.PLAN_ID , carrierPropRes.getInt(ProposalConstants.PRODUCTID));
			}

			/**
			 *  set request configuration document id for mapper
			 */
			exchange.setProperty(ProposalConstants.TRVLPOLICY_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));

			exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
					"-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());

			JsonDocument document = serverConfig.getDocBYId(ProposalConstants.TravelPolicyREQDOC+reqNode.get(ProposalConstants.CARRIER_ID).intValue()+ 
					"-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
			if(document!=null)
			{
				JsonNode proposalConfigNode = objectMapper.readTree(document.content().toString());
				log.debug("TravelPolicyNumberConfigNode: "+proposalConfigNode);
				exchange.setProperty(ProposalConstants.PROPOSALREQ_CONFIG,proposalConfigNode);
			} 
			exchange.getIn().setBody(reqNode);     
		}
		catch (Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|HealthPolicyReqProcessor|",e);

			String trace = "Error in Class :"+TravelPolicyReqProcessor.class+"   Line Number :"+Thread.currentThread().getStackTrace()[0].getLineNumber();
			log.info("Erroror messgaes TravelPolicyReqProcessor"+TravelPolicyReqProcessor.class+"    "+Thread.currentThread().getStackTrace()[0].getLineNumber());
			String uri = "activemq:queue:" + ExceptionHandlerQ;
			((ObjectNode) reqNode).put("transactionName","TravelPolicyReqProcessor");
			((ObjectNode) reqNode).put("Exception",e.toString());
			((ObjectNode) reqNode).put("ExceptionMessage",trace);
			exchange.getIn().setBody(reqNode.toString());
			log.info("sending to exception handler queue"+reqNode);
			exchange.setPattern(ExchangePattern.InOnly);
			template.send(uri, exchange);

			throw new ExecutionTerminator();
		}
	}
}
