package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class BikeResProcessor implements Processor 
{
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	JsonNode quoteNode = null;
	JsonNode proposalNode = null;
	ObjectNode responseNode = null;
	CBService quoteDataInstance = null;

	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(BikeResProcessor.class.getName());
		PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
		serverConfig = CBInstanceProvider.getServerConfigInstance();
		policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewalConstatnt.QUOTE_BUCKET);
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(request);

		log.info("Its Bike processor "+reqNode);

		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
		{
			responseNode = objectMapper.createObjectNode();
			JsonNode policyRenewalConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
			log.info("Printing policyRenewalConfig :"+policyRenewalConfig);
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			log.info("Printing proposalDOC :"+proposalDOC);
			proposalNode = objectMapper.readTree(proposalDOC);
			if(proposalNode.has("QUOTE_ID"))
			{
				//quoteNode = dataProvider.getQuoteDoc(proposalNode.findValue("QUOTE_ID").asText());
				//responseNode.put("quoteParam", quoteNode.findValue("quoteParam"));
				responseNode = dataProvider.prepareResponseNode(proposalNode,policyRenewalConfig.get("bikeResponseConfig"));

				// Update with new information

				if(responseNode.has("insuranceDetails"))
				{
					//update dates
					((ObjectNode) responseNode.get("insuranceDetails")).put("prevPolicyStartDate",responseNode.findValue("policyStartDate"));
					((ObjectNode) responseNode.get("insuranceDetails")).put("prevPolicyEndDate",responseNode.findValue("policyEndDate"));
					((ObjectNode) responseNode.get("insuranceDetails")).put("policyNumber",proposalNode.findValue("bikePolicyResponse").get("policyNo").asText());
					((ObjectNode) responseNode.get("insuranceDetails")).put("ncb",dataProvider.getNewNCB(proposalNode.findValue("ncb").asText()));
					((ObjectNode) responseNode.get("insuranceDetails")).put("insurerName",proposalNode.findValue("insuranceCompany").asText());
					((ObjectNode) responseNode.get("insuranceDetails")).put("insurerId",proposalNode.findValue("bikePolicyResponse").get("carrierId").asInt());
				}

				// update nominee details like age..
				if(responseNode.has("nominationDetails") && responseNode.get("nominationDetails").has("personAge"))
				{
					((ObjectNode) responseNode.get("nominationDetails")).put("personAge",responseNode.get("nominationDetails").get("personAge").asInt()+1);
				}

				// update proposer details like age..
				if(responseNode.has("appointeeDetails") && responseNode.findValue("appointeeDetails").has("personAge"))
				{
					((ObjectNode) responseNode.findValue("appointeeDetails")).put("personAge",responseNode.findValue("appointeeDetails").get("personAge").asDouble()+1);
				}

				// update proposer details like age..
				if(responseNode.has("proposerDetails") && responseNode.findValue("proposerDetails").has("personAge"))
				{
					((ObjectNode) responseNode.findValue("proposerDetails")).put("personAge",responseNode.findValue("proposerDetails").get("personAge").asInt()+1);
				}
			}

			log.info("FInal Bike response :"+responseNode);
		}
		exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
	}
}