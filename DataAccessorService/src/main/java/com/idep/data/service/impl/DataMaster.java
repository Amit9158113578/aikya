package com.idep.data.service.impl;

import java.util.Date;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.service.response.DataResponse;

public class DataMaster {
	Logger log = Logger.getLogger(DataWriter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode docDataNode;
	JsonNode errorNode;
	String queryStatus = "";

	public String couchDBOperation(String inputRequest) throws JsonProcessingException{
		try{
			this.log.info("DataWriterImpl : writeCouchDBImpl invoked : " + inputRequest);
			boolean status = false;

			JsonNode data = this.objectMapper.readTree(inputRequest);
			String documentId = data.get("documentId").asText();
			String bucketName = data.get("bucketName").asText();
			String action = data.get("action").asText();

			CBService restClient = getDatabaseInstance(bucketName);

			if(action.equalsIgnoreCase("create")){
				status = createDocument(restClient, documentId, inputRequest);
			}else if(action.equalsIgnoreCase("update")){
				status = updateDocument(restClient, documentId, inputRequest);
			}else if(action.equalsIgnoreCase("disable")){
				status = disableDocument(restClient, documentId);
			}else if(action.equalsIgnoreCase("enable")){
				status = enableDocument(restClient, documentId);
			}

			if(status){
				this.docDataNode = this.objectMapper.createObjectNode();
				((ObjectNode)this.docDataNode).put("docId", documentId);
				return DataResponse.createResponse(1000, queryStatus, this.docDataNode);
			}else{
				this.log.info("Exception while creating document");
				return DataResponse.createResponse(1002, queryStatus, this.errorNode);
			}
		}catch(Exception e){
			this.log.info("Exception : " , e);
			return DataResponse.createResponse(1002, queryStatus, this.errorNode);
		}
	}

	public JsonNode readDocument(CBService restClient, String documentId){
		JsonNode document = null;
		try{
			document = this.objectMapper.readTree(restClient.getDocBYId(documentId).content().toString());	
		}catch(Exception ex){
			log.info("Error while fetching document : " , ex);
		}
		return document;
	}

	public boolean createDocument(CBService restClient, String documentId, String inputRequest){
		String status = "";
		try{
			JsonNode document = this.objectMapper.readTree(inputRequest);
			if(document != null){
				Date currentDate = new Date();
				((ObjectNode) document).put("updatedDate", currentDate.toString());
				status = restClient.createAsyncDocument(documentId, JsonObject.fromJson(document.toString()));
				queryStatus = status;
				if(status.equalsIgnoreCase("doc_created")){
					return true;				
				}else{
					return false;
				}
			}else{
				return false;
			}
		}catch(Exception ex){
			log.info("Error while creating document : " , ex);
		}
		return false;
	}

	public boolean updateDocument(CBService restClient, String documentId, String inputRequest){
		String status = "";
		try{
			JsonNode document = this.objectMapper.readTree(inputRequest);
			if(document != null){
				Date currentDate = new Date();
				((ObjectNode) document).put("updatedDate", currentDate.toString());
				status = restClient.replaceAsyncDocument(documentId, JsonObject.fromJson(document.toString()));
				queryStatus = status;
				if(status.equalsIgnoreCase("doc_replaced")){
					return true;				
				}else{
					return false;
				}
			}else{
				return false;
			}
		}catch(Exception ex){
			log.info("Error while creating document : " , ex);
		}
		return false;
	}

	public boolean disableDocument(CBService restClient, String documentId){
		String status = "";
		try{
			JsonNode document = readDocument(restClient, documentId);
			if(document != null){
				Date currentDate = new Date();
				((ObjectNode) document).put("updatedDate", currentDate.toString());
				((ObjectNode) document).put("isVisible", false);
				status = restClient.replaceAsyncDocument(documentId, JsonObject.fromJson(document.toString()));
				queryStatus = status;
				if(status.equalsIgnoreCase("doc_replaced")){
					return true;				
				}else{
					return false;
				}	
			}else{
				return false;
			}
		}catch(Exception ex){
			log.info("Error while creating document : " , ex);
		}
		return false;
	}

	public boolean enableDocument(CBService restClient, String documentId){
		String status = "";
		try{
			JsonNode document = readDocument(restClient, documentId);
			if(document != null){
				Date currentDate = new Date();
				((ObjectNode) document).put("updatedDate", currentDate.toString());
				((ObjectNode) document).put("isVisible", true);
				status = restClient.replaceAsyncDocument(documentId, JsonObject.fromJson(document.toString()));
				queryStatus = status;
				if(status.equalsIgnoreCase("doc_replaced")){
					return true;				
				}else{
					return false;
				}	
			}else{
				return false;
			}
		}catch(Exception ex){
			log.info("Error while creating document : " , ex);
		}
		return false;
	}

	public CBService getDatabaseInstance(String bucketName){
		CBService cbservice = CBInstanceProvider.getBucketInstance(bucketName);
		return cbservice;
	}
}