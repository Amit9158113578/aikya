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
import com.idep.pospservice.request.UploadFileProcessor;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class UploadDocumentResponse implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UploadFileProcessor.class.getName());
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync =  new SyncGatewayPospDataServices();
	static JsonDocument jsonDocument;
	static JsonDocument stagDocument;
	static{
		jsonDocument = PospData.getDocBYId("PospContentManagementConfig");
		stagDocument = PospData.getDocBYId("PospStages");
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class).toString());
			updateUserProfile(reqNode.findValue("mobileNumber").asText(),((ObjectNode)reqNode));
			log.info("POSP User PROFILE CREATD FOR : "+reqNode.findValue("mobileNumber").asText());
			exchange.getIn().setBody(reqNode);
		}catch(Exception e){
			log.error("unable to process request for upload document : ",e);
		}
	}
	public String updateUserProfile(String mobile, ObjectNode data)
	{
		try
		{
			String docId = "POSPUserProfile-" + mobile;
			JsonNode userProfile = pospDataSync.getPospDataDocumentBySync(docId);
			if (userProfile != null)
			{
				
			
				if(data.has("documentType")){
					if(userProfile.has(data.get("documentType").asText())){
						if(userProfile.get(data.get("documentType").asText()).has("mobileNumber")){
							((ObjectNode)userProfile.get(data.get("documentType").asText())).remove("mobileNumber");
						}
						if(userProfile.get(data.get("documentType").asText()).has("agentId")){
							((ObjectNode)userProfile.get(data.get("documentType").asText())).remove("agentId");
						}
					}
					
					if(stagDocument==null){
						stagDocument = PospData.getDocBYId("PospStages");	
					}
					
					JsonNode stagesConfig = objectMapper.readTree(stagDocument.content().toString());
					if(stagesConfig.has("stagesList")){
						JsonNode stages = stagesConfig.get("stagesList");
						log.info("stagesList : documentType Stage found : "+data.get("documentType").asText());
						if(stages.has(data.get("documentType").asText())){
							((ObjectNode)userProfile).put("stage",stages.get(data.get("documentType").asText()));
							((ObjectNode)userProfile).put("status", "completed");
						}
					}					
						data.remove("documentType");						
				}
				((ObjectNode)userProfile).putAll(data);
				String docStatus = pospDataSync.replacePospDataDocumentBySync(docId, 
						JsonObject.fromJson(objectMapper.writeValueAsString(userProfile)));
				log.info("User Profile document : " + docId + " status :" + docStatus);
				return docStatus;
			}
			return createPOSPUserProfile(mobile, ((ObjectNode)userProfile));
		}
		catch (Exception e)
		{
			log.error("failed to update POSP user profile : ", e);
		}
		return null;
	}

	public String createPOSPUserProfile(String mobile, ObjectNode data)
	{
		try
		{
			data.put("documentType", "pospUserProfile");
			data.put("mobileNumber", mobile);
			String docId = "POSPUserProfile-" + mobile;
			String docStatus = pospDataSync.createPospDataDocumentBySync(docId, 
					JsonObject.fromJson(objectMapper.writeValueAsString(data)));
			log.info("POSP User Profile document: " + docId + " status : " + docStatus);

			return docStatus;
		}
		catch (Exception e)
		{
			log.error("failed to create user profile : ", e);
		}
		return null;
	}

}
