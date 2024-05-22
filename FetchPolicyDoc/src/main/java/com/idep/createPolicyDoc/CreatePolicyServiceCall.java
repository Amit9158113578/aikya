package com.idep.createPolicyDoc;

import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class CreatePolicyServiceCall {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(PolicyRequest.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService proposalService = CBInstanceProvider.getPolicyTransInstance();
	PolicyRequest qc = new PolicyRequest();

	public static JsonNode CreatePolicy(JsonNode proposalDoc,String doc_name) {
		JsonNode createPolicyReq = null;
		try {

			JsonNode createPolicyReqMap = null;
			createPolicyReq = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CreatePolicyRequest-"+proposalDoc.findValue("businessLineId").asText()+"-"+proposalDoc.findValue("carrierId").asText()+"-sample").content()).toString());
			createPolicyReqMap = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CreatePolicyRequest-"+proposalDoc.findValue("carrierId").asText()+"-mapping").content()).toString());
			Map<String, String> quoteTypeMap = (Map)objectMapper.readValue(createPolicyReqMap.toString(), Map.class);
			for (Map.Entry<String, String> field : quoteTypeMap.entrySet()) {
				if (createPolicyReqMap.findValue((String)field.getKey()).isTextual()) {		

					((ObjectNode) createPolicyReq).put((String)field.getValue(), proposalDoc.findValue(field.getKey()));
				} 
				else if (createPolicyReqMap.findValue((String)field.getKey()).isInt()) {
					((ObjectNode) createPolicyReq).put((String)field.getValue().toLowerCase(), proposalDoc.findValue((String)field.getKey().toLowerCase()).intValue());
				}
				else if (createPolicyReqMap.findValue((String)field.getKey()).isLong()) {
					((ObjectNode) createPolicyReq).put((String)field.getValue().toLowerCase(), proposalDoc.findValue((String)field.getKey().toLowerCase()).longValue());
				}
				else if (createPolicyReqMap.findValue((String)field.getKey()).isDouble()) {
					((ObjectNode) createPolicyReq).put((String)field.getValue().toLowerCase(), proposalDoc.findValue((String)field.getKey().toLowerCase()).doubleValue());
				}
			}
			((ObjectNode) createPolicyReq.with("transactionStausInfo")).put("proposalId",proposalDoc.findValue("proposalId").asText());
			((ObjectNode) createPolicyReq.with("transactionStausInfo")).put("apPreferId",doc_name);
			((ObjectNode) createPolicyReq.with("transactionStausInfo")).put("transactionStatusCode","1");

			//log.info("updated mapped Request...."+createPolicyReq);






		}
		catch(Exception e){
			log.error("Error in CreatePolicyServiceCall",e);
		}
		return createPolicyReq; 
	}
}
