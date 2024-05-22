package com.posp.query;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.ArrayNode;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posp.response.PospQueryResponse;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.util.List;

import org.apache.log4j.Logger;


public class QueryServiceImpl {
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(QueryServiceImpl.class.getName());
	  CBService PospData = null;
	  CBService service = null;
	  JsonNode responseConfigNode;
	  CBService productservice = null;
	  JsonNode searchConfigNode;
	  JsonNode errorNode;
	  
	  public String getApplicationData(String appData)
	  {
	    try
	    {
	      long lStartTime = System.currentTimeMillis();
	      List<JsonObject> list = null;
	      if (this.service == null || this.PospData == null)
	      {
	        this.log.info("ServerConfig bucket instance created");
	        this.service = CBInstanceProvider.getServerConfigInstance();
	        this.PospData = CBInstanceProvider.getBucketInstance("PospData");  
	        this.responseConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("ResponseMessages").content()).toString());
	        this.log.info("ResponseMessages configuration loaded");
	        this.searchConfigNode = this.objectMapper.readTree(((JsonObject)this.PospData.getDocBYId("POSPSearchQueryConfig").content()).toString());
	      }
	      JsonNode inputdataNode = this.objectMapper.readTree(appData);
	      String operation = inputdataNode.get("operation").textValue();
	      JsonNode currentConfigNode = this.searchConfigNode.get(operation);
	      String whereClause = "";
	      String statement = "";
	      if(currentConfigNode.get("activeLikeCommand").textValue().equals("N"))
	      {
		      whereClause = whereClause + currentConfigNode.get("documentTypeFormat").textValue()+"='" + currentConfigNode.get("documentType").textValue()+"'";
		     	    	  	  
		  	  String inputArr[]=inputdataNode.get("searchValues").asText().split(",");
		  	  String configArr[]=currentConfigNode.get("searchKeys").asText().split(",");
		  	  for(int i=0;i<configArr.length;i++)
		  	  {
		  		whereClause = whereClause +  " and " + configArr[i] + "='" + inputArr[i]+ "'";
		  	  }
	     }
	      if(currentConfigNode.get("activeLikeCommand").textValue().equals("Y"))
	      {
		      whereClause = whereClause + currentConfigNode.get("documentTypeFormat").textValue()+"='" + currentConfigNode.get("documentType").textValue()+"'";
		     	    	  	  
		  	  
		  	  String configArr[]=currentConfigNode.get("searchKeys").asText().split(",");
		  	  for(int i=0;i<configArr.length;i++)
		  	  {
		  		  if(inputdataNode.get("searchParam").get(configArr[i]).textValue()!="")
		  		  {
		  			   if(currentConfigNode.has("requestNode"))
		  			   {
		  				   if(currentConfigNode.get("requestNode").get(0).get("activeLikeCommandYes").has(configArr[i]))
		  				   {
		  					  whereClause = whereClause +  " and " + currentConfigNode.get("requestNode").get(0).get("activeLikeCommandYes").get("requestPath").textValue()+"."+configArr[i] + " LIKE '" + inputdataNode.get("searchParam").get(configArr[i]).textValue()+ "%'";
		  				   }
		  				   else if(currentConfigNode.get("requestNode").get(0).has("activeLikeCommandNo"))
		  				   {
			  				   if(currentConfigNode.get("requestNode").get(0).get("activeLikeCommandNo").has(configArr[i]))
			  				   {
			  					  whereClause = whereClause +  " and " + currentConfigNode.get("requestNode").get(0).get("activeLikeCommandNo").get("requestPath").textValue()+"."+configArr[i] + " = " + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "";
			  				   }
			  				 else
			  				   {
			  					  if(currentConfigNode.has("requestNodePath") )
			  					  {
			  						 whereClause = whereClause +  " and " + currentConfigNode.get("requestNodePath").textValue()+configArr[i] + "= '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "'";
			  					  }
			  					  else
			  					  {
			  						  
			  						  if(currentConfigNode.has("existDateParam"))
			  						  {
			  							  if(!currentConfigNode.get("existDateParam").textValue().contains(configArr[i]))
			  							  {
			  								whereClause = whereClause +  " and " + configArr[i] + "= '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "'";
			  							  }
			  						  }
			  						  else
			  						  {
				  					      whereClause = whereClause +  " and " + configArr[i] + "= '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "'";
			  						  }
			  					  }
			  			        }
		  				   }
		  				 else
		  				   {
		  					 whereClause = whereClause +  " and " + configArr[i] + "= '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "'";
		  				   }
		  				 
		  				   if(currentConfigNode.has("date"))
			  			   {
			  				    log.info("inside date function :");
			  				    if(inputdataNode.get("searchParam").get(currentConfigNode.get("date").get("from").textValue()).textValue()!="")
			  				    {
			  				    	if(!whereClause.contains(currentConfigNode.get("date").get("betweenDateKey").textValue()))
			  				    	{
				  				    	log.info("inside from date function :"+whereClause);
				  				    	whereClause=whereClause+" and "+currentConfigNode.get("date").get("betweenDateKey").textValue()+" '"+inputdataNode.get("searchParam").get(currentConfigNode.get("date").get("from").textValue()).textValue()+"%' AND '"+inputdataNode.get("searchParam").get(currentConfigNode.get("date").get("to").textValue()).textValue()+"%'";
			  				    	}
			  				     }
			  				   
			  			   }
		  				   else
		  				   {
		  					 whereClause = whereClause +  " and " + configArr[i] + "= '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "'";
		  				   }
		  					   
		  			   }
		  			  else
		  			  {
		  			    whereClause = whereClause +  " and " + configArr[i] + " LIKE '" + inputdataNode.get("searchParam").get(configArr[i]).asText()+ "%'";
		  			  }
		  	        }
		  	  }
	     }
	    log.info("created where clause :"+whereClause);
        statement = "SELECT " + currentConfigNode.get("displayFields").textValue() + " FROM " + currentConfigNode.get("searchBucket").asText() + "  WHERE " + whereClause;
        if (currentConfigNode.has("orderBy")) {
          statement = statement + " ORDER BY " + currentConfigNode.get("orderBy").asText();
        }
        if (currentConfigNode.has("groupBy")) {
          statement = statement + " GROUP BY " + currentConfigNode.get("groupBy").asText();
        }
        if (currentConfigNode.has("documentCount")) {
            statement = statement + " LIMIT " + currentConfigNode.get("documentCount").asInt();
        }
        this.log.info("statement :" + statement);
        this.log.info("displayFields :" + currentConfigNode.get("displayFields").textValue());
        
        list = this.service.executeConfigQuery(statement, currentConfigNode.get("displayFields").textValue());
        long lEndTime = System.currentTimeMillis();
      
        this.log.info("SearchService Query Time Elapsed in milliseconds : " + (lEndTime - lStartTime));
      
        if (list.isEmpty()) {
        return PospQueryResponse.createResponse(this.responseConfigNode.findValue("noRecordsCode").intValue(), this.responseConfigNode.findValue("noRecordsMessage").textValue(), this.errorNode);
      }
      JsonNode docDataNode = this.objectMapper.readTree(list.toString());
      return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(), docDataNode);
    }
    catch (Exception e)
    {
      this.log.error("Exception at AppDataServiceImpl : ", e);
    }
    return PospQueryResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), this.responseConfigNode.findValue("errorMessage").textValue(), this.errorNode);
  }
}
