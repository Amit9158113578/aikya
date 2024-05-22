package com.idep.data.service.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

/**
 * 
 * @author sandeep.jadhav
 * read transaction bucket data
 */
public class PolicyTransDataReader {
	
	Logger log = Logger.getLogger(PolicyTransDataReader.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	
	public String readPolicyTransData(String data) throws JsonProcessingException, IOException
	{
		try 
		{
			/**
			 * read user input
			 */
			JsonNode transDocNode = this.objectMapper.readTree(data);
			JsonDocument transDocument = this.transService.getDocBYId(transDocNode.get("docId").textValue());
			
			/**
	    	  * check if requested document is available
	    	*/
			if(transDocument!=null)
			{
				JsonNode docDataNode = this.objectMapper.readTree(transDocument.content().toString());
				return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
			}
			else
			{
				String encryptedQuoteId = transDocNode.get("docId").textValue();
				JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfigService.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
				String decryptedProposalId= GenrateEncryptionKey.GetPlainText(encryptedQuoteId, keyConfigDoc.get("encryptionKey").asText());
				log.info("decryptedProposalId :"+decryptedProposalId);
				transDocument = transService.getDocBYId(decryptedProposalId);
				
				if(transDocument!=null)
				{
					JsonNode docDataNode = this.objectMapper.readTree(transDocument.content().toString());
					return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
				}
				
				return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsMessage").textValue(), this.errorNode);
			}
			
		}
		catch(Exception e)
		{
			this.log.info("Exception at PolicyTransDataReader : ",e);
			return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
		}
		
	    
	}

}
