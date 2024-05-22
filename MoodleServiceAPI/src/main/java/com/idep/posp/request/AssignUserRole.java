package com.idep.posp.request;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.connection.RestServiceClient;
import com.idep.posp.connection.SaveDBServiceResponse;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class AssignUserRole{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AssignUserRole.class.getName());
	RestServiceClient rsc = new RestServiceClient();
	SaveDBServiceResponse db  =  new SaveDBServiceResponse();
	SyncGatewayPospDataServices syncPospData = new SyncGatewayPospDataServices();
	public JsonNode assignUserRole(String request) throws Exception {
try{
			
			JsonNode reqNode = objectMapper.readTree(request);
			((ObjectNode)reqNode).put("urlConfigDocId", "POSPExternalServiceUrl");
			((ObjectNode)reqNode).put("ConfigDocId", "AssignUserRole");
			((ObjectNode)reqNode).put("functionName", "AssignUserRole");
			((ObjectNode)reqNode).put("methodType","POST");
			if(reqNode.findValue("id")!=null){
				String res= rsc.CallRestService(reqNode);
				JsonNode resNode = objectMapper.readTree(res);
				if(resNode.has("exception") || resNode.has("errorcode")){
					log.error("unable to create user in Moodle : "+resNode);
					return null;
				}else{
					return resNode;
				}
			}else{
				String docId = null;
				if(reqNode.has("documentType")){
					docId = reqNode.get("documentType").asText()+"-"+reqNode.findValue("mobileNumber");
				}else{
				((ObjectNode)reqNode).put("documentType", "PospUserProfile");	
				}
				JsonNode userProfile= syncPospData.getPospDataDocumentBySync(docId);
				if(userProfile!=null && !userProfile.has("error")){
					((ObjectNode)reqNode).put("id",userProfile.get("moodleDetails").get("id").asText());
				}
				String req= rsc.CallRestService(reqNode);
				JsonNode resNode = objectMapper.readTree(req);
				if(resNode.has("exception") || resNode.has("errorcode")){
					log.error("unable to create user in Moodle : "+resNode);
					return null;
				}else{
					return resNode;
				}
			}
		}catch(Exception e){
			log.error("unable to process for Create moodle user : ",e);
		}
return null;
	}

}
