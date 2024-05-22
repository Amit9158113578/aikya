package com.idep.OfflinePolicyRenewal;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMGatewayImpl;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class OfflinePolicyReqProcessor implements Processor {
	static Logger log = Logger.getLogger(OfflinePolicyReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonNode policyConfig = null;
	static SugarCRMGatewayImpl sugarCRMService = SugarCRMGatewayImpl.getSugarCRMInstance();
	SugarCRMModuleServices crmServices = new SugarCRMModuleServices();
	String query = null;
	static
	{
		try {
			policyConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
		} catch (JsonProcessingException e) {
			log.info("Errors in Couchbase document");
			e.printStackTrace();
		} catch (IOException e) {
			log.info("Couchbase Document Not found");
			e.printStackTrace();
		} catch (Exception e) {
			log.info("Could not generate session Id :" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("Got Offline Renewal Request : "+reqNode);
		ObjectNode policyData = objectMapper.createObjectNode();
		String select_fields = policyConfig.findValue("selected_fields").asText();
		log.info("select_fields :" +select_fields);
		if(reqNode.get("policyId").asText() != null){
			String id  = reqNode.get("policyId").asText();
			try{
				policyData = crmServices.getModuleData("aos_contracts.id='"+id+"'" , "AOS_Contracts", select_fields);
				log.info("Fetched policy data :"+policyData);
				if(reqNode.has("policyEndDateStr")){
					policyData.put("policyEndDate", reqNode.get("policyEndDateStr"));
				}
			}
			catch (Exception e){
				log.error("Error at getting policy data");
				policyData = null;
			}
			policyData.put("intervalDay",reqNode.get("intervalDay"));
			log.info("Offline Policy create Lead :"+policyData);
			exchange.getIn().setBody(policyData);
		}
	}
}


