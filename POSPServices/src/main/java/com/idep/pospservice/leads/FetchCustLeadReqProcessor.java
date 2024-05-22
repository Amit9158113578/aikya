package com.idep.pospservice.leads;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class FetchCustLeadReqProcessor implements  Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FetchCustLeadReqProcessor.class.getName());
	JsonNode errorNode;
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");

	public void process(Exchange data) throws Exception {
		try{
			ArrayNode responseNode = objectMapper.createArrayNode();
			JsonNode request = objectMapper.readTree(data.getIn().getBody().toString());
			log.info("parameters to jsonNode"+request);
			FetchCustLeadReqProcessor classObject = new FetchCustLeadReqProcessor();
			String query = "select policy.proposalId,policy.policyNo from PospData as policy where documentType =  'TransactionDetails' and EventType = 'Policy'  order by PolicyCreatedDate desc";
			log.info("query" + query);
			List<Map<String, Object>> queryoutput = PospData.executeQuery(query);
			if (queryoutput.size() > 0){
				log.info("output size"+queryoutput.size());
				String output = objectMapper.writeValueAsString(queryoutput);
				log.info("Update Proposal Caar Document : " +output);
				JsonNode jsonNode = objectMapper.readTree(output);
				log.info("....reqNode"+jsonNode);
				JsonNode methodOutput = null;
				for(int i=0;i<queryoutput.size();i++){
					log.info("in forrr"+jsonNode.get(i));
					if(jsonNode.get(i).has("proposalId") && jsonNode.get(i).get("proposalId")!=null){
						log.info("proposalId type got"+jsonNode.get(i));
						methodOutput = classObject.getProposalData(jsonNode.get(i));
						log.info("integer value of FORR"+i);
						((ObjectNode)methodOutput.get(0)).put("proposalId",jsonNode.get(i).get("proposalId").asText());
						((ObjectNode)methodOutput.get(0)).put("policyId",jsonNode.get(i).get("policyNo").asText());

						log.info("method outputp...."+methodOutput);
					}

					responseNode = responseNode.add(methodOutput.get(0));
					log.info("responseNode789" + responseNode);
				}

				log.info("responseNode123" + responseNode);	
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,responseNode);
				data.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

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
	/*public static void main (String args[]) throws Exception{
		System.out.println("hello in main method...");	
		FetchCustLeadReqProcessor qc = new FetchCustLeadReqProcessor();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode data;
		try {
			data = objectMapper.readTree("{\"agentNumber\": \"\",\"agentId\": \"\",\"campaign_id\": \"59f9bb13-6a98-870c-f66d-5bffa9f88f6f\",\"policyIssueDate\": \"\",\"frequency\":\"\",\"monthStartDate\": \"\",\"monthEndDate\": \"\",\"policyDetails\": [{\"carrierId\":\"\",\"planId\":\"\",	\"policyNumber\": \"\",	\"proposalNumber\": \"\",	\"lob\": \"car\",	\"policyStartDate\": \"\",	\"policyEndDate\": \"\",	\"netPremium\": \"\",	\"TPPremium\": \"123\",	\"ODPremium\": \"123\",	\"policyTerm\": \"\",	\"vehicleAge\":\"1-3\",	\"policyType\":\"comprehensive\",	\"NoOfWheels\":\"2\"}]}");
			qc.process(data);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	 */

	private JsonNode getProposalData(JsonNode jsonNode) throws IOException {
		log.info("parameters to jsonNode"+jsonNode);
		JsonNode resNode =null;
		String query = "select proposal.businessLineId as LOB,proposal.emailId,proposal.firstName,proposal.lastName,proposal.mobile as mobileNumber from PospData as proposal where documentType =  'TransactionDetails' and EventType = 'Proposal'  and proposalId = '"+jsonNode.get("proposalId").asText()+"' order by ProposalCreatedDate desc";
		log.info("query PROPOSAL" + query);
		List<Map<String, Object>> queryoutput = PospData.executeQuery(query);
		if (queryoutput.size() > 0){
			log.info("output size"+queryoutput.size());
			String output = objectMapper.writeValueAsString(queryoutput);
			log.info("Update Proposal Caar Document6532 : " +output);
			resNode = objectMapper.readTree(output);
			log.info("....reqNode"+resNode);
			for(int i=0;i<queryoutput.size();i++){
				log.info("in forrr"+resNode.get(i));

				if (resNode.get(i).findValue("LOB").asInt() == 1) {
					((ObjectNode) resNode.get(i)).put("lob", "Life");
				} else if (resNode.get(i).findValue("LOB").asInt() == 2) {
					(  (ObjectNode) resNode.get(i)).put("lob", "Bike");
				} else if (resNode.get(i).findValue("LOB").asInt() == 3) {
					(  (ObjectNode) resNode.get(i)).put("lob", "Car");
				} else if (resNode.get(i).findValue("LOB").asInt() == 4) {
					(  (ObjectNode) resNode.get(i)).put("lob", "Health");
				} else if (resNode.get(i).findValue("LOB").asInt() == 5) {
					(  (ObjectNode) resNode.get(i)).put("lob", "Travel");
				} else if (resNode.get(i).findValue("LOB").asInt() == 6) {
					(  (ObjectNode) resNode.get(i)).put("lob", "CriticalIllness");
				} else if (resNode.get(i).findValue("LOB").asInt() == 7) {
					(  (ObjectNode) resNode.get(i)).put("lob", "Home");
				} else if (resNode.get(i).findValue("LOB").asInt() == 8) {
					(  (ObjectNode) resNode.get(i)).put("lob", "PersonalAccident");
				} else {
					log.info("businessLineId is not matched...");
				}
			}
			log.info("jsonNode" + resNode);	
		}
		return resNode;
	}	

}

