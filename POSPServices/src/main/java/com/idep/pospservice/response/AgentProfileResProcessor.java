package com.idep.pospservice.response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.Functions;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class AgentProfileResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AgentProfileResProcessor.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	//CBService PolicyTransaction = CBInstanceProvider.getBucketInstance("PolicyTransaction");
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			ObjectNode reqNode = (ObjectNode)requestNode;	
			log.info("RequestNode Of POSP Agent:"+requestNode);
			if(requestNode == null){
				throw new ExecutionTerminator();
			}
			JsonObject agentDetails = JsonObject.fromJson(requestNode.toString());
			agentDetails.put(POSPServiceConstant.DOCUMENT_TYPE,"pospUserProfile");
			agentDetails.put(POSPServiceConstant.STATUS,"pending");
			agentDetails.put(POSPServiceConstant.STAGE,"registration");	
			String docId = POSPServiceConstant.POSP_USER_PROFILE+requestNode.findValue("mobileNumber").textValue();	

			JsonNode agentDoc = pospDataSync.getPospDataDocumentBySync(docId);

			if(agentDoc == null)
			{
				long posp_seq;
				synchronized(this)
				{
					posp_seq = this.PospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_AG);
				}

				String agentId = POSPServiceConstant.POSPAG_ID+posp_seq;
				agentDetails.put("agentId",agentId);

				String doc_status = pospDataSync.createPospDataDocumentBySync(docId, agentDetails);
				log.info("POSP UserId : "+docId+" "+"Doc Content:"+agentDetails);

				log.info("POSP User Doc Status : "+doc_status);		
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

				}else
				{
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}

			}else{		
				ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(agentDoc.toString());
				jsonNode.put("updatedDate",Functions.getDateAndTime());
				((ObjectNode)jsonNode).putAll(reqNode);
				String docStatus = pospDataSync.replacePospDataDocumentBySync(docId,JsonObject.fromJson(objectMapper.writeValueAsString(jsonNode)));

				//String doc_status = PolicyTransaction.replaceDocument(agentDocId, JsonObject.fromJson(agntNode.toString()));
				log.info("POSP User Doc Status : "+docStatus);	
				if(docStatus.equalsIgnoreCase(POSPServiceConstant.DOC_UPDATED)){
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

				   }else{
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				  }
	
			}

		}
		catch(Exception e)
		{
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
