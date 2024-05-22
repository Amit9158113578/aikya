package com.idep.dbaccess;

import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.util.AffilationConstants;
import java.util.Iterator;
import java.util.Map;

public class AffiliateAccess  {
	
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(AffiliateAccess.class.getName());
	static CBService service = null;
	static JsonNode serviceConfigNode = null;


	public String createAffiliate(JsonNode reqNodeData) {
		String deviceId= reqNodeData.get("deviceId").asText(); 
		String response=null;
		try {
			service = CBInstanceProvider.getServerConfigInstance();
			serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId(AffilationConstants.AFFILIATE_DOC).content()).toString());
		}catch (Exception e) {
			log.error(AffilationConstants.FETCH_DOC_FAILURE);
			}
		if(serviceConfigNode!=null){
		if(serviceConfigNode.has(deviceId)){
			log.error(AffilationConstants.DEVICEID_ALREADY_PRESENT);
			response=AffilationConstants.DEVICEID_ALREADY_PRESENT;
		}else{
		((ObjectNode)serviceConfigNode).set(deviceId, reqNodeData);
		Map<String, String> mapData = objectMapper.convertValue(serviceConfigNode, Map.class) ;
		JsonObject deviceIdNode  = JsonObject.from(mapData);
	    response=service.replaceDocument(AffilationConstants.AFFILIATE_DOC, deviceIdNode);
		}
		}else{
			response=AffilationConstants.FETCH_DOC_FAILURE;
		}
	    return response;
 }  
	public String deleteAffiliate(JsonNode reqNodeData) {
		try {
			service = CBInstanceProvider.getServerConfigInstance();
			serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId(AffilationConstants.AFFILIATE_DOC).content()).toString());
		}catch (Exception e) {
			log.error(AffilationConstants.FETCH_DOC_FAILURE);
			}
		String response=null;
		String deviceId= reqNodeData.get("deviceId").asText(); 
		String isActive= reqNodeData.get("isActive").asText(); 
		
		if(serviceConfigNode!=null){
		if(serviceConfigNode.has(deviceId)){
			ObjectNode node=(ObjectNode)serviceConfigNode.get(deviceId);
			node.put("isActive",isActive);
			((ObjectNode)serviceConfigNode).set(deviceId, node);
		Map<String, String> mapData = objectMapper.convertValue(serviceConfigNode, Map.class) ;
		JsonObject deviceIdNode  = JsonObject.from(mapData);
	    response=service.replaceDocument(AffilationConstants.AFFILIATE_DOC, deviceIdNode);
        }else{
            log.error(AffilationConstants.INVALID_DEVICEID);
            response=AffilationConstants.INVALID_DEVICEID;
        }
		}else{
			response=AffilationConstants.FETCH_DOC_FAILURE;
		}
		return response;
		}
	public String updateAffiliate(ObjectNode reqNodeData) {
		try {
			service = CBInstanceProvider.getServerConfigInstance();
			serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId(AffilationConstants.AFFILIATE_DOC).content()).toString());
		}catch (Exception e) {
			log.error(AffilationConstants.FETCH_DOC_FAILURE);
			}
		String deviceId= reqNodeData.get("deviceId").asText();  
		String response=null;
		if(serviceConfigNode!=null){
		if(serviceConfigNode.has(deviceId)){
		ObjectNode node=(ObjectNode)serviceConfigNode.get(deviceId);
		Iterator<String> fieldNames=reqNodeData.fieldNames();
		
		while(fieldNames.hasNext()){
			 String field=fieldNames.next();
			 if(node.has(field)){
					String value=reqNodeData.get(field).asText();
					if(reqNodeData.get(field).asText().equals(node.get(field).asText())){
					}else{
						node.put(field,value);
					}
		}
		}
		((ObjectNode)serviceConfigNode).remove(deviceId);
		((ObjectNode)serviceConfigNode).set(deviceId, node);
		Map<String, String> mapData = objectMapper.convertValue(serviceConfigNode, Map.class) ;
		JsonObject deviceIdNode  = JsonObject.from(mapData);
	    response=service.replaceDocument(AffilationConstants.AFFILIATE_DOC, deviceIdNode);
		}else{
			log.error(AffilationConstants.INVALID_DEVICEID);
			response=AffilationConstants.INVALID_DEVICEID;
		}
		}else{
			response=AffilationConstants.FETCH_DOC_FAILURE;
		}
		return response;
   }  
	public JsonNode fetchAffiliate(String deviceId) {
		JsonNode response=objectMapper.createObjectNode();
		try {
			service = CBInstanceProvider.getServerConfigInstance();
			serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId(AffilationConstants.AFFILIATE_DOC).content()).toString());
		}catch (Exception e) {
			log.error(AffilationConstants.FETCH_DOC_FAILURE);
			}
		if(serviceConfigNode!=null){
		if(serviceConfigNode.has(deviceId)){
		response=serviceConfigNode.get(deviceId);	
		}else{
			log.error(AffilationConstants.INVALID_DEVICEID);
			((ObjectNode)response).put("response", AffilationConstants.INVALID_DEVICEID);
		}
		}else{
			((ObjectNode)response).put("response", AffilationConstants.FETCH_DOC_FAILURE);
		}
        return response;
	}
}