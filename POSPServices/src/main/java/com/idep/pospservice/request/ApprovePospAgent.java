package com.idep.pospservice.request;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.dsl.path.index.CreateIndexPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
//import com.idep.posp.request.CreateMoodleUser;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class ApprovePospAgent implements Processor {


	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ApprovePospAgent.class.getName());
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	//CreateMoodleUser createmoodleUser =  new CreateMoodleUser();
	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	JsonNode errorNode;
	static JsonDocument stagDocument;
	static{
		stagDocument = PospData.getDocBYId("PospStages");
	}
	@Override
	public void process(Exchange exchange) throws Exception {

		JsonNode agentProfileDoc=null; 
		String docId="";
		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			log.info("request : "+reqNode);
			if(reqNode.findValue(POSPServiceConstant.MOBILE_NO)!=null || reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText().equalsIgnoreCase("")){
				docId = "POSPUserProfile-"+reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText();
				agentProfileDoc= pospDataSync.getPospDataDocumentBySync(docId);
			} 
			((ObjectNode)reqNode).remove(POSPServiceConstant.MOBILE_NO);
			Date date = new Date();
			if(reqNode.has("educationDetails")){
				((ObjectNode)reqNode.get("educationDetails")).put("verificationDate",dateFormat.format(date).toString());
				((ObjectNode)reqNode.get("educationDetails")).put("verifiedBy",reqNode.get("educationDetails").get("adminId"));
			}else if(reqNode.has("bankAccountDetails")){
				((ObjectNode)reqNode.get("bankAccountDetails")).put("verificationDate",dateFormat.format(date).toString());
				((ObjectNode)reqNode.get("bankAccountDetails")).put("verifiedBy",reqNode.get("bankAccountDetails").get("adminId"));
			}else if(reqNode.has("panDetails")){
				((ObjectNode)reqNode.get("panDetails")).put("verificationDate",dateFormat.format(date).toString());
				((ObjectNode)reqNode.get("panDetails")).put("verifiedBy",reqNode.get("panDetails").get("adminId"));
			}else if(reqNode.has("aadharDetails")){
				((ObjectNode)reqNode.get("aadharDetails")).put("verificationDate",dateFormat.format(date).toString());
				((ObjectNode)reqNode.get("aadharDetails")).put("verifiedBy",reqNode.get("aadharDetails").get("adminId"));
			}else{
				((ObjectNode)reqNode).put("verificationDate",dateFormat.format(date).toString());
				((ObjectNode)reqNode).put("verifiedBy",reqNode.get("adminId"));
				/*((ObjectNode)reqNode).put("isVerified","Y");  It will receive from UI request
				((ObjectNode)reqNode).put("isActive","Y");*/
				if(reqNode.has("isVerified")){
					JsonNode config=null ;
					if(stagDocument!=null){
						config = objectMapper.readTree(stagDocument.content().toString());
					}
					if(reqNode.get("isVerified").asText().equalsIgnoreCase("Y")){
						((ObjectNode)agentProfileDoc).put("isProfileVerified", "true");	
						((ObjectNode)agentProfileDoc).put("stage",config.get("stagesList").get("profileVerify").asText() );
							((ObjectNode)agentProfileDoc).put("status","completed" );	
					}else{
						((ObjectNode)agentProfileDoc).put("stage",config.get("stagesList").get("profileVerify").asText() );
						((ObjectNode)agentProfileDoc).put("status","pending" );
					}
				}
			}
			((ObjectNode)agentProfileDoc).putAll(((ObjectNode)reqNode));
			String docStatus = pospDataSync.replacePospDataDocumentBySync(docId, 
					JsonObject.fromJson(objectMapper.writeValueAsString(agentProfileDoc)));
			log.info("User Profile document : " + docId + " status :" + docStatus);
			if(reqNode.has("isVerified") && reqNode.get("isVerified").asText().equalsIgnoreCase("Y")){
				/*log.info("creating moodle user for : "+docId);
				
				createmoodleUser.CreateUser(GenerateMoodleUserRequst(agentProfileDoc));
				log.info("Moodle User created for : "+docId);
				*/
				
			}
			if(agentProfileDoc.isNull()){
				log.error("unable to read Agent profile Docuument :"+"POPUserProfie-"+reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText());
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText());
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				throw new ExecutionTerminator();
			}		
			exchange.getIn().setBody(reqNode);
		}catch(Exception e){
			log.error("unable to procss rquuest : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}


	}
	
	
	public String GenerateMoodleUserRequst(JsonNode reqNode){
		
		ObjectNode moodleReq = objectMapper.createObjectNode();
		
		JsonNode req = objectMapper.createObjectNode();
		if(reqNode.findValue("agentId")!=null){
		((ObjectNode)req).put("agentId", reqNode.findValue("agentId").asText());
		}
		if(reqNode.findValue("firstName")!=null){
			((ObjectNode)req).put("firstName", reqNode.findValue("firstName").asText());
			}
		if(reqNode.findValue("lastName")!=null){
			((ObjectNode)req).put("lastName", reqNode.findValue("lastName").asText());
			}
		if(reqNode.findValue("email")!=null){
			((ObjectNode)req).put("email", reqNode.findValue("email").asText());
			}
		if(reqNode.findValue("mobileNumber")!=null){
			((ObjectNode)req).put("mobileNumber", reqNode.findValue("mobileNumber").asText());
			}
		
		moodleReq.put("documentType", "POSPUserProfile");
		moodleReq.put("dataParam", req);
		return moodleReq.toString();
	}

}
