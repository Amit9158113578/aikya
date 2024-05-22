package com.idep.config.impl.service;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.config.response.DataCarrierResponse;
import com.idep.config.util.ConfigConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.searchconfig.cache.SearchConfigCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

/**
 * 
 * @author sandeep.jadhav
 * This class is used to serve requested data based on configuration
 */
public class AppConfigServiceImpl
{
	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(AppConfigServiceImpl.class.getName());
  CBService service = CBInstanceProvider.getServerConfigInstance();
  CBService productservice = CBInstanceProvider.getProductConfigInstance();
  JsonNode errorNode;
  
  public String getConfigData(String reqparam) throws IOException
  {

	  List<JsonObject> list = null;
	  JsonNode docDataNode;
    
	  try
	  {
	      JsonNode inputReqdata = this.objectMapper.readTree(reqparam);
	      String documentType = inputReqdata.get("documentType").asText();
	      
	      /**
	       * check if requested data is available in cache
	       */
	      if(DocumentDataConfig.getConfigDocList().get("SearchCacheDataConfig").has(documentType))
	      {
	    	  
	    	  if(DocumentDataConfig.getConfigDocList().get("SearchCacheDataConfig").get(documentType).has("cacheConfig"))
	    	  {
	    		  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), SearchConfigCache.getConfigDataCache().get(DocumentDataConfig.getConfigDocList().get("SearchCacheDataConfig").get(documentType).get("cacheConfig").get(inputReqdata.get("searchValue").asText()).asText()));
	    	  }
	    	  else
	    	  {
	    		  String fetch = DocumentDataConfig.getConfigDocList().get("SearchCacheDataConfig").get(documentType).get("cacheNode").asText();
	    		  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), SearchConfigCache.getConfigDataCache().get(fetch)); 
	    	  }
	    	  
	      }
	
	      else
	      {
	    	  
	    	  //JsonNode inputNode = DocumentDataConfig.getConfigDocList().get("SearchConfiguration").get(documentType);
	    	  JsonNode inputNode = objectMapper.readTree(service.getDocBYId("SearchConfiguration").content().toString()).get(documentType);
	    	 
	         
	          String statement = "";
	         
	         if (inputNode.get("activeLikeCommand").textValue().equals("Y"))
	          {
	        	
	        	String condition = inputNode.get("searchField").textValue();
	            String[] conArray = condition.split(",");
	            String whereClause = "";
	            String[] arrayOfString1;
	            int j = (arrayOfString1 = conArray).length;
	            int count = inputNode.get("documentCount").intValue();
	            
	            for (int i = 0; i < j; i++)
	            {
	              String s = arrayOfString1[i];
	              whereClause = whereClause + "documentType='" + documentType + "' and " + s + " LIKE '" + inputReqdata.get("searchValue").textValue().toUpperCase() + "%' OR ";
	            }
	           
	            whereClause = whereClause.substring(0, whereClause.length() - 4);
	            
	             statement = "SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE " + whereClause + " ORDER BY " + inputNode.get("orderBy").textValue() + " LIMIT " + count;
	            list = this.service.executeConfigQuery(statement, inputNode.get("displayField").textValue());
	          }
	         else if(inputNode.get("activeLikeCommand").textValue().equals("N") &&
	        		 inputNode.get("activeANDCommand").textValue().equals("N"))
	         {
	        	 
	        	 String whereClause = "";
	        	 whereClause = whereClause + "documentType='" + documentType + "' and " ;
	        	 
	        	 String conditionParam = inputNode.get("searchField").textValue();
	        	 
	        	 if(inputReqdata.get("searchValue").isTextual())
	        	 {
	        		 whereClause = whereClause + conditionParam +" = '"+inputReqdata.get("searchValue").asText()+"'";
	        	 }
	        	 else if(inputReqdata.get("searchValue").isInt())
	        	 {
	        		 whereClause = whereClause + conditionParam +" = "+inputReqdata.get("searchValue").asInt();
	        	 }
	        	 else if(inputReqdata.get("searchValue").isBoolean())
	        	 {
	        		 whereClause = whereClause + conditionParam +" = "+inputReqdata.get("searchValue").asBoolean();
	        	 }
	        	 else 
	        	 {
	        		 whereClause = whereClause + conditionParam +" = '"+inputReqdata.get("searchValue").asText()+"'";
	        	 }
	        	 
	        	 statement = "SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE " + whereClause ;
	        	 
	        	 if(inputNode.has("orderBy"))
	        	 {
	        		 statement = statement + " ORDER BY " + inputNode.get("orderBy").asText() ;
	        	 }
	        	 if(inputNode.has("groupBy"))
	        	 {
	        		 statement = statement + " GROUP BY " + inputNode.get("groupBy").asText() ;
	        	 }
	        	 if(inputNode.has("documentCount"))
	        	 {
	        		 statement = statement + " LIMIT " + inputNode.get("documentCount").asInt() ;
	        	 }
	        	 log.debug("statement :"+statement);
	        	 log.debug("displayField :"+inputNode.get("displayField").textValue());
	        	 
		         list = this.service.executeConfigQuery(statement, inputNode.get("displayField").textValue());
		         
		       
		         
		    	 if(inputNode.has("replaceKey"))
	        	 {
		    		  for (JsonObject hospitalDetails : list) {
							
		    			  hospitalDetails= hospitalDetails.put(hospitalDetails.getString(inputNode.get("replaceKey").textValue()), hospitalDetails.get(inputNode.get("replaceFirstKey").textValue()));
		    			  hospitalDetails = hospitalDetails.removeKey(inputNode.get("replaceFirstKey").textValue());
		    			  hospitalDetails = hospitalDetails.removeKey(inputNode.get("replaceKey").textValue());
		    		  }
		    		
	        	 }
	         }
	          /*else if (inputNode.get("activeANDCommand").textValue().equals("Y"))
	          {
	            //String statement = "";
	            if(inputNode.get("textSearch").textValue().equals("N"))
	            {
	            	statement =	"SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE documentType='" + documentType + "' and " + inputNode.get("searchField").textValue() + "=" + configdata.get("searchValue").intValue() + " ORDER BY " + inputNode.get("orderBy").textValue();
	            }
	            else
	            {
	            	statement =	"SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE documentType='" + documentType + "' and " + inputNode.get("searchField").textValue() + "='" + configdata.get("searchValue").textValue() + "' ORDER BY " + inputNode.get("orderBy").textValue();
	            }
	            list = this.service.executeConfigQuery(statement, inputNode.get("displayField").textValue());
	          }
	          else
	          {
	             statement = "SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE documentType = '" + documentType + "'" + " ORDER BY " + inputNode.get("orderBy").textValue();
	             if(inputNode.get("documentCount").intValue()>0)
	             {
	            	 statement = statement+" LIMIT "+ inputNode.get("documentCount").intValue();
	             }
	             list = this.service.executeConfigQuery(statement, inputNode.get("displayField").textValue());
	             //cacheDataMap.put(documentType, list);
	          }*/
	         else if(inputNode.has("innerJoin") && inputNode.get("innerJoin").textValue().equals("Y"))
	         {
	        	 String whereClause = "";
	        	 if(inputNode.get("getByIdDocument").textValue().equals("Y"))
	        	 {
	        		 JsonNode requestNode = objectMapper.readTree(service.getDocBYId(inputReqdata.get("searchValue").textValue()).content().toString());
	        		 
	        		 if(inputNode.has("requestKey"))
	        		 {
	        			       String make = requestNode.get(inputNode.get("requestKey").get("make").textValue()).textValue();
	        			       String model = requestNode.get(inputNode.get("requestKey").get("model").textValue()).textValue();
	     	                   whereClause =" and a.isActive='Y' and b.isMapped='N' and LOWER(b."+inputNode.get("requestKey").get("uMake").textValue()+") like LOWER('%"+make+"%') and LOWER(b."+inputNode.get("requestKey").get("model").textValue()+") like LOWER('%"+model+"%')";
	        		 }
	        		 log.info("whereClause :"+whereClause);
	        		 statement = "SELECT " + inputNode.get("displayField").textValue()+ " FROM "+inputNode.get("searchBucket").asText()+" WHERE b.documentType = '" + documentType + "'"+" and a.documentType='" + inputNode.get("innerJoinDocumentType").textValue() + "'"+whereClause+" group by "+ inputNode.get("groupBy").asText();		
	        	 }
	        	  log.info("inner join query :"+statement);
	        	 list = this.service.executeConfigQuery(statement, inputNode.get("displayField").textValue());
	         }
	          this.log.info("query to fetch records : " + statement);
	          if (list.isEmpty()) {
	        	  
	        	  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("noRecordsMessage").textValue(), this.errorNode);
	          }
	          else
	          {
	        	  docDataNode = this.objectMapper.readTree(list.toString());
	        	  if(docDataNode.get(0).toString().equals("{}"))
	        	  {
	        		  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("noRecordsMessage").textValue(), this.errorNode);
	        	  }
	        	  else
	        	  {
	        	      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("successMessage").textValue(), docDataNode);
	        	  }
	          }
	      }
      
	  }
	    catch (JsonParseException e)
	    {
	      this.log.error("unable to process JSON string",e);
	      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorMessage").textValue(), this.errorNode);
	    }
	    catch (JsonMappingException e)
	    {
	      this.log.error("unable to map JSON string",e);
	      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorMessage").textValue(), this.errorNode);
	    }
	    catch (IOException e)
	    {
	      this.log.error("IOException occurred",e);
	      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorMessage").textValue(), this.errorNode);
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception occurred, please analyze logs for more details",e);
	      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("errorMessage").textValue(), this.errorNode);
	    }
    
  }
  
  /**
   * 
   * @author sandeep.jadhav
   * Get method created to search city, area or pin-code
 * @throws IOException 
 * @throws JsonProcessingException 
   */
  public String getConfigGETData(UriInfo info) throws JsonProcessingException, IOException
  {
	    String documentType = (String)info.getQueryParameters().getFirst("filter");
	    
    	JsonNode inputNode = DocumentDataConfig.getConfigDocList().get("SearchConfiguration").get(documentType);

	    log.debug("documentType : "+documentType);
	    String searchValue = (String)info.getQueryParameters().getFirst("q");
        String substring = searchValue.substring(0, 2);
	    if(inputNode.has("specialRTO") && inputNode.get("specialRTO").has(substring))
	    {
	    	    Pattern compile = Pattern.compile(ConfigConstants.PATTERN);
			    if(compile.matcher(searchValue).matches())
			    {
			    	searchValue = new StringBuffer(searchValue).insert(searchValue.length()-2, "0").toString();
			    	searchValue=searchValue.substring(0, searchValue.length()-1);
			    }
	    }
	    int carrierId = 0;
	    if(info.getQueryParameters().containsKey("id"))
	    {
	    	carrierId = Integer.parseInt(info.getQueryParameters().getFirst("id"));
	    }
	    log.debug("carrierId : "+carrierId);
	    List<JsonObject> list = null;
	    JsonNode docDataNode;
	    
	    try
	    {
			    
		    	try
		    	{
			    	if(inputNode.has("cachingApplicable"))
			    	{
			    		ArrayNode rtoList = objectMapper.createArrayNode();
			    		
			    		if(SearchConfigCache.getConfigDataCache().get("RTOList")!=null)
			    		{
				    		for(JsonNode node : SearchConfigCache.getConfigDataCache().get("RTOList"))
				    		{
				    			if(node.get("regisCode").asText().startsWith(searchValue.toUpperCase()))
				    			{
				    				rtoList.add(node);
				    			}
				    			else if(node.get("commonCityName").asText().startsWith(searchValue.toUpperCase()))
				    			{
				    				rtoList.add(node);
				    			}
				    		}
				    		if(rtoList.size()>0)
				    		{
				    			return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), rtoList);
				    		}
			    		}
			    		else
			    		{
			    			log.info("RTO List not cached. get results from query");
			    		}
			    	}
		    	}
		    	catch(Exception e)
		    	{
		    		log.error("unable to fetch results from cache, bringing data by querying");
		    	}
		    	
		    	String condition = inputNode.get("searchField").asText();
			    int count = inputNode.get("documentCount").asInt();
			      //String bucketName = inputNode.get("searchBucket").asText();
			      
			      String[] conArray = condition.split(",");
			      String whereClause = "";
			  
			      
			    // this is handled for pin code search
			    if(documentType.equals("Area"))
			    {
			        documentType="Pincode"; 
			    }
		      
			    if(inputNode.get("activeLikeCommand").asText().equals("Y"))
		    	{
			      for (int i = 0; i < conArray.length; i++)
			      {
			    	  if(inputNode.has("withAll"))
			    	  {
			    		  whereClause = whereClause + "documentType='" + documentType + "' and " + conArray[i] + " LIKE '%" + searchValue.toUpperCase() + "%' OR ";
			    	  }
			    	  else
			    	  {
			    		  whereClause = whereClause + "documentType='" + documentType + "' and " + conArray[i] + " LIKE '" + searchValue.toUpperCase() + "%' OR ";
			    	  }
			        
			      }
			      
			      whereClause = whereClause.substring(0, whereClause.length() - 4);
		    	}
			    else if(inputNode.get("activeANDCommand").textValue().equals("Y"))
		    	{
			      whereClause = whereClause + "documentType='" + documentType +"'";
			      for (int i = 0; i < conArray.length; i++)
			      {
			        whereClause = whereClause + " and " + conArray[i] + " = '" + searchValue + "'";
			      }
		    	}
		      	  
		      	  
			        String statement = "SELECT " + inputNode.get("displayField").textValue() + " FROM `"+inputNode.get("searchBucket").asText()+"`  WHERE " + whereClause + " ORDER BY " + inputNode.get("orderBy").textValue() + " LIMIT " + count;
			        this.log.info("query to fetch records : " + statement);
			        
			        if(inputNode.get("searchBucket").asText().equalsIgnoreCase("ProductData"))
			        {
			        	list = this.productservice.executeConfigQuery(statement, DocumentDataConfig.getConfigDocList().get("SearchConfiguration").get(documentType).get("displayField").textValue());
			        }
			        if(inputNode.get("searchBucket").asText().equalsIgnoreCase("ServerConfig"))
			        {
			        	list = this.service.executeConfigQuery(statement, DocumentDataConfig.getConfigDocList().get("SearchConfiguration").get(documentType).get("displayField").textValue());
			        }
		    
	      if (list.isEmpty()) {
		    	  
		    	  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsMessage").textValue(), this.errorNode);
		      }
		      else
		      {
		    	  docDataNode = this.objectMapper.readTree(list.toString());
		    	  return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
		      }
	    }
	    catch (JsonParseException e)
	    {
		      	this.log.error("unable to process JSON string : ",e);
		      	return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		}
	    catch (JsonMappingException e)
	    {
		      this.log.error("unable to map JSON string : ",e);
		      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		}
	    catch (IOException e)
	    {
		      this.log.error("IOException occurred : ",e);
		      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		}
	    catch (Exception e)
	    {
		      this.log.error("Exception occurred, please analyze logs for more details : ",e);
		      return DataCarrierResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		 }
	    
  }
}
