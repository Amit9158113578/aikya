package com.idep.pospservice.leads;

import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class FetchCustProposalReqProcessor implements  Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FetchCustProposalReqProcessor.class.getName());
	JsonNode errorNode;
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");

	public void process(Exchange data) throws Exception {
		try{
			JsonNode request = objectMapper.readTree(data.getIn().getBody().toString());
			log.info("parameters to jsonNode"+request);
			String query = "select proposal.proposalId,proposal.requestSource,proposal.businessLineId as LOB,proposal.emailId,proposal.firstName,proposal.lastName,proposal.mobile as mobileNumber from PospData as proposal where documentType =  'TransactionDetails' and EventType = 'Proposal'  order by ProposalCreatedDate desc";
			log.info("query" + query);
			List<Map<String, Object>> queryoutput = PospData.executeQuery(query);
			if (queryoutput.size() > 0){
				log.info("output size"+queryoutput.size());
				String output = objectMapper.writeValueAsString(queryoutput);
				log.info("Update Proposalnbvn Document6532 : " +output);
				JsonNode jsonNode = objectMapper.readTree(output);
				log.info("....reqNode"+jsonNode);
				for(int i=0;i<queryoutput.size();i++){
					log.info("in forrr"+jsonNode.get(i));
					if(jsonNode.get(i).has("LOB") && jsonNode.get(i).get("LOB")!=null){
						log.info("quote type got"+jsonNode.get(i).get(i));
						if (jsonNode.get(i).findValue("LOB").asInt() == 1) {
							((ObjectNode) jsonNode.get(i)).put("lob", "Life");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 2) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "Bike");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 3) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "Car");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 4) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "Health");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 5) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "Travel");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 6) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "CriticalIllness");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 7) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "Home");
						} else if (jsonNode.get(i).findValue("LOB").asInt() == 8) {
							(  (ObjectNode) jsonNode.get(i)).put("lob", "PersonalAccident");
						} else {
							log.info("businessLineId is not matched...");
						}
					}
					log.info("jsonNode" + jsonNode);	
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,jsonNode);
					data.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}
			}
			else{
				log.info("no documnets with this type");
			}
		}catch(Exception e){
			log.error("Unable to process : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			data.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
}

