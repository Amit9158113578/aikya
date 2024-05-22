package com.idep.pospservice.request;

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

public class DataAccessorProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	Logger log = Logger.getLogger(DataAccessorProcessor.class.getName()); 
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub

		String query = "";
		long lStartTime = System.currentTimeMillis();
		try
		{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			List<Map<String, Object>> list  = null;
			log.info("RequestNode Of Doc:"+requestNode);

			if(requestNode == null){
				throw new ExecutionTerminator();
			}
			if(requestNode.has("documentType")){
				if(requestNode.findValue("documentType").textValue().equalsIgnoreCase("AgentRequest")){

					query =  "select PospData.* from PospData  where documentType = 'pospUserProfile';";
				}
				if(requestNode.findValue("documentType").textValue().equalsIgnoreCase("AgentConfiguration")){

					query =  "select firstName,lastName,mobileNumber,isActive,city,state,agentId from PospData  where documentType = 'pospUserProfile';";
				}
			}
			if(requestNode.has("agentId")){

				query =  "select PospData.* from PospData  where documentType = 'pospUserProfile'and agentId="+requestNode.findValue("agentId").toString()+";";
			}

			this.log.info("query formed : " + query);

			list = PospData.executeQueryCouchDB(query);

			long lEndTime = System.currentTimeMillis();
			this.log.info("Service Query Time Elapsed in milliseconds : " + (lEndTime - lStartTime));

			String agentList = this.objectMapper.writeValueAsString(list);
			JsonNode agentDetails = this.objectMapper.readTree(agentList);

			this.log.info("list : " + agentDetails);
			if (list.isEmpty()) {
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NO_RECORDS_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NORECORD_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}
			else
			{
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,agentDetails);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}
		}
		catch(Exception e){
			log.error("Unable to process : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();

		}
	}

}

