package com.idep.config.impl.service;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.config.response.DataCarrierResponse;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;

public class AddressSearchImpl {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(AddressSearchImpl.class.getName());
	  CBService service = CBInstanceProvider.getServerConfigInstance();
	  CBService productservice = CBInstanceProvider.getProductConfigInstance();
	  JsonNode errorNode;
	
	public String getAddressDetails(UriInfo info) throws JsonProcessingException, IOException
	  {
		try {
			
		    List<JsonObject> list = null;
		    JsonNode docDataNode;
			String documentType = (String)info.getQueryParameters().getFirst("filter");
		    log.debug("documentType : "+documentType);
		    String searchValue = (String)info.getQueryParameters().getFirst("q");
		    int carrierId = 0;
		    
		    if(info.getQueryParameters().containsKey("id"))
		    {
		    	carrierId = Integer.parseInt(info.getQueryParameters().getFirst("id"));
		    }
	
		    JsonNode configNode = this.objectMapper.readTree(this.service.getDocBYId("AddressSearchConfiguration").content().toString()).get(documentType);
		    String[] fullSearchArray = configNode.get("fullSearch").asText().split(",");
	    	String fullSearchwhereClause = "";
	    	String displayField = "";
	    	
	    	if(configNode.get("fullSearchConfig").get("activate").has(String.valueOf(carrierId)))
	    	{
	    		/**
			     * iterate and form where clause on full search
			     */
			    for(int i=0;i<fullSearchArray.length;i++)
			    {
			    	fullSearchwhereClause = fullSearchwhereClause +  fullSearchArray[i]+" = "+carrierId + "and ";
			    }
			    
			    fullSearchwhereClause = fullSearchwhereClause.substring(0, fullSearchwhereClause.length() - 4);
			    displayField = configNode.get("displayField").asText();
	    	}
	    	else
	    	{
	    		/**
	    		 * changing document type to use traditional pin-code search 
	    		 * also get display field
	    		 */
	    		documentType = configNode.get("fullSearchConfig").get("deactivate").get("documentType").asText();
	    		displayField = configNode.get("fullSearchConfig").get("deactivate").get("displayField").asText();
	    	}
	    	
		    
	    	String statement = "select "+displayField+" from "+configNode.get("searchBucket").asText()+" where "
	    					 + "documentType='"+documentType+"'";

	    	
	    	
	    	
	    	String[] partialSearchArray = configNode.get("partialSearch").asText().split(",");
		    String partialSearchwhereClause = "";
		    
		    /**
		     * iterate and form where clause on partial search
		     */
	    	for(int i=0;i<partialSearchArray.length;i++)
	    	{
	    		if(configNode.has("withAll"))
	    		{
	    			partialSearchwhereClause = partialSearchwhereClause +  partialSearchArray[i]+" LIKE '%"+searchValue.toUpperCase() + "%' OR ";
	    		}
	    		else
	    		{
	    			partialSearchwhereClause = partialSearchwhereClause +  partialSearchArray[i]+" LIKE '"+searchValue.toUpperCase() + "%' OR ";
	    		}
	    		
	    	}
	    	
	    	partialSearchwhereClause = partialSearchwhereClause.substring(0, partialSearchwhereClause.length() - 4);
	    	
	    	if(fullSearchwhereClause.length()>0)
	    	{
	    		statement = statement.concat(" and ").concat(fullSearchwhereClause);
	    	}
	    	
	    	if(partialSearchwhereClause.length()>0)
	    	{
	    		statement = statement.concat(" and ").concat(partialSearchwhereClause);
	    	}
	    	
	    	log.info("query to fetch records : "+statement);
	    	
	    	list = this.service.executeConfigQuery(statement, displayField);
	    	
	    	if (list.isEmpty()) {
		    	  
		    	  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsMessage").textValue(), this.errorNode);
		    }
		    else
		    {
		    	  docDataNode = this.objectMapper.readTree(list.toString());
		    	  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
		    }
	    	
	    }
	    catch(Exception e)
		{
	    	log.error("Exception occurred : ",e);
	    	return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
	    	
		}
	    
	    
	  }

}
