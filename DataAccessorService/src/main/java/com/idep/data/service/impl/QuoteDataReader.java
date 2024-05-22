package com.idep.data.service.impl;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.couchbase.client.java.document.json.JsonObject;

public class QuoteDataReader {

	  Logger log = Logger.getLogger(QuoteDataReader.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode errorNode;
	  CBService transService = CBInstanceProvider.getBucketInstance(DataConstants.QUOTE_BUCKET);
	  CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	  
	  
	  public String readQuoteData(String data) throws JsonProcessingException, IOException
	  {
		  try 
			{
				/**
				 * read user input
				 */
				JsonNode quoteDocNode = this.objectMapper.readTree(data);
				JsonDocument quoteDocument = this.transService.getDocBYId(quoteDocNode.get("docId").textValue());
				
				/**
		    	  * check if requested quote document is available
		    	*/
				if(quoteDocument!=null)
				{
					JsonNode docDataNode = this.objectMapper.readTree(quoteDocument.content().toString());
					
					if(docDataNode.has("RISK_QUOTE_ID"))
					{
						JsonDocument riskQuoteDocument = this.transService.getDocBYId(docDataNode.findValue("RISK_QUOTE_ID").asText());
						JsonNode riskNode = this.objectMapper.readTree(riskQuoteDocument.content().toString());
						((ObjectNode)docDataNode).put("riskDetails", riskNode);
					}
					return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
				}
				else
				{
					String encryptedQuoteId = quoteDocNode.get("docId").textValue();
					JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfigService.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
					String decryptedQuoteId= GenrateEncryptionKey.GetPlainText(encryptedQuoteId, keyConfigDoc.get("encryptionKey").asText());
					log.info("decryptedQuoteId :"+decryptedQuoteId);
					quoteDocument = transService.getDocBYId(decryptedQuoteId);
					
					if(quoteDocument!=null)
					{
						JsonNode docDataNode = this.objectMapper.readTree(quoteDocument.content().toString());
						
						if(docDataNode.has("RISK_QUOTE_ID"))
						{
							JsonDocument riskQuoteDocument = this.transService.getDocBYId(docDataNode.findValue("RISK_QUOTE_ID").asText());
							JsonNode riskNode = this.objectMapper.readTree(riskQuoteDocument.content().toString());
							((ObjectNode)docDataNode).put("riskDetails", riskNode);
						}
						return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
					}
					return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsMessage").textValue(), this.errorNode);
				}
				
			}
			catch(Exception e)
			{
				this.log.info("Exception at QuoteDataReader : ",e);
				return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
			}
	  }
}
