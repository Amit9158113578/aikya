package com.idep.PolicyRenewal.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyRenewalRepsonse implements Processor {

	Logger log = Logger.getLogger(PolicyRenewalRepsonse.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	JsonNode proposalNode = null;
	ObjectNode responseNode = null;

	@SuppressWarnings("unchecked")
	public void process(Exchange exchange)
			throws Exception
	{
		try
		{
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			policyTransaction = CBInstanceProvider.getPolicyTransInstance();
			String request = exchange.getIn().getBody().toString();
			log.info("Renewal Response Req :"+request);
			JsonNode reqNode = objectMapper.readTree(request);
			log.info("Renewal Response ReqNode :"+reqNode);
			if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
			{
				responseNode = objectMapper.createObjectNode();
				JsonNode policyRenewalConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
				log.info("Printing policyRenewalConfig :"+policyRenewalConfig);
				proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
				log.info("Printing proposalDOC :"+proposalDOC);
				proposalNode = objectMapper.readTree(proposalDOC);
				
				log.info("proposalNode :"+proposalNode);
				Map<String, String> policyRenewalConfigMapNode = null;
				if(proposalNode.findValue("QUOTE_ID").asText().contains("LIFE"))
				{
					policyRenewalConfigMapNode = objectMapper.readValue(policyRenewalConfig.get("lifeResponseConfig").toString(), Map.class);
				}
				else if(proposalNode.findValue("QUOTE_ID").asText().contains("BIKE"))
				{
					policyRenewalConfigMapNode = objectMapper.readValue(policyRenewalConfig.get("bikeResponseConfig").toString(), Map.class);
				}
				else if(proposalNode.findValue("QUOTE_ID").asText().contains("CAR"))
				{
					policyRenewalConfigMapNode = objectMapper.readValue(policyRenewalConfig.get("carResponseConfig").toString(), Map.class);
				}
				else
				{
					policyRenewalConfigMapNode = objectMapper.readValue(policyRenewalConfig.get("healthResponseConfig").toString(), Map.class);
				}
				responseNode = dataProvider.filterMapData(responseNode, proposalNode, policyRenewalConfigMapNode);
				log.info("responseNode :"+responseNode);
			}
		}
		catch(Exception e)
		{
			log.info("Exception at  policy renewal response");
		}
		exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
	}

}
