package com.garage.processor;

import java.util.List;
import java.util.Map;

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
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.garage.util.ExecutionTerminator;
import com.idep.garage.util.GarageConstant;



public class GarageDetailsProcessor implements Processor {

	ObjectMapper objectMapper=new ObjectMapper();
	 Logger log = Logger.getLogger(GarageDetailsProcessor.class.getName());
	 CBService service= CBInstanceProvider.getServerConfigInstance();
	 JsonNode errorNode;
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		try{
		 /*    for Request City Name and RegisCode Required
		       RegisCode partition add '-' tag
		       GET RTODETAILS Document details pick commonCity name and set name 
		       
		 */
			
		JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
		String display = requestNode.get("regisCode").textValue();
		String rto = display.substring(0, 2);
		String code = display.substring(2, 4);
		
		// create  RTODetails document Id get commonCity use it
		String documentId= GarageConstant.RTODETAILS+ rto+"-"+code;
		JsonDocument RTODetails = service.getDocBYId(documentId);
		if(RTODetails==null)
		{
			log.info("RTO Details Document Not Found :"+RTODetails);
			throw new ExecutionTerminator();
		}
		else
		{
			JsonNode rtoDetails = objectMapper.readTree(RTODetails.content().toString());
			String city = rtoDetails.get(GarageConstant.COMMON_CITYNAME).textValue();
			String state = rtoDetails.get(GarageConstant.STATE).textValue();
			List<Map<String, Object>> garageDetailsMapList = null;
			String query=null;
			try
			{
				if(requestNode.has("carrierId"))
				{
					int carrierId = requestNode.get("carrierId").asInt();
					
					String varientId = requestNode.get("varientId").textValue();
				    String CarVarientId=varientId+"-"+carrierId;
				  
				    try{
				           JsonDocument carVarientDocument = service.getDocBYId(CarVarientId);
						    if(carVarientDocument==null)
						    {
						    	log.info("Car Varient Id Details Document Not Found :"+carVarientDocument);
								throw new ExecutionTerminator();
						    }
						    else
						    {
						    	JsonNode CarVarientNode = objectMapper.readTree(carVarientDocument.content().toString());
						    	String uMake = CarVarientNode.get("uMake").textValue();
						    	log.info("uMake:"+uMake);
						    	if(uMake==null)
								{
									log.info("uMake Details Document Not Found in carVarientId Document :"+uMake);
									throw new ExecutionTerminator();
								}
						    	query="select ARRAY_AGG(ServerConfig) as garageData,carrierId from ServerConfig where documentType = 'garageDetails' and uMake ='" + uMake + "' and uCity ='" + city + "' and state ='" + state + "' OR  documentType = 'garageDetails' and uMake='Multibrand' and uCity ='" + city + "' and state ='" + state + "' group by carrierId";
						    }
				      }
				     catch(Exception e)
				     {
				    	 log.error("failed to get carVarientId Document :",e);
							throw new ExecutionTerminator();
				     }
				}
				else
				{
					String make = null;
					 if (requestNode.has("make"))
			          {
			            make = requestNode.get("make").asText();
			          }
			          else if (requestNode.has("variantId"))
			          {
			            JsonDocument varientDoc = this.service.getDocBYId(requestNode.get("variantId").asText());
			            if (varientDoc != null)
			            {
			              JsonNode variant = this.objectMapper.readTree(varientDoc.content().toString());
			              if (variant.has("make")) {
			                make = variant.get("make").asText();
			              }
			            }
			          }
					
					log.info("make:"+make);
					if(make==null)
					{
						log.error("make Details Document Not Found :"+make);
						throw new ExecutionTerminator();
					}
					
					query="select ARRAY_AGG(ServerConfig) as garageData,carrierId from ServerConfig where documentType = 'garageDetails' and uCity ='"+city+"'and state ='"+state+"' and (uMake ='"+make+"' or uMake='Multibrand') group by carrierId;";
				}
				
				garageDetailsMapList = service.executeQueryCouchDB(query);
			}
			catch(Exception e)
			{
				log.error("failed to get results by query : "+query ,e);
			}
		
			if(garageDetailsMapList==null)
			{
				ObjectNode objectNode = this.objectMapper.createObjectNode();
			    objectNode.put(GarageConstant.GARAGE_RES_CODE,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.NORECORD_CODE).asInt() );
			    objectNode.put(GarageConstant.GARAGE_RES_MSG ,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.NORECORD_MESSAGES).asText());
			    objectNode.put(GarageConstant.GARAGE_RES_DATA,errorNode);
			    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			    throw new ExecutionTerminator();
			}
		    String garageDetailsInString = objectMapper.writeValueAsString(garageDetailsMapList);
		    
			JsonNode garageDetails = objectMapper.readTree(garageDetailsInString);
			
			// concate repairerName and address 
			
			log.info("garage Details Size "+garageDetails.get(0).get("garageData").size());
			
				if(garageDetails.get(0).get("garageData").size()>0)
				{
			    	for (JsonNode data : garageDetails) 
			    	{
					   JsonNode garageData = data.get("garageData");
					   
					      for (JsonNode garage : garageData)
					      {
						    String address = garage.get("address").textValue();
						    
						    String[] split = address.split(",");
					    	if(split.length>3)
							{
								 String newaddress = address.substring(address.indexOf(","));
								 String concateAddress = garage.get("repairerName").textValue()+" "+newaddress;
								 ((ObjectNode)garage).put("place", concateAddress);
							}
							else
							{
								 String concateAddress = garage.get("repairerName").textValue()+" "+address;
								 ((ObjectNode)garage).put("place", concateAddress);
							}
				       	  }
					      
					      
				       }
				
						ObjectNode objectNode = this.objectMapper.createObjectNode();
					    objectNode.put(GarageConstant.GARAGE_RES_CODE,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.SUCC_CONFIG_CODE).asInt());
					    objectNode.put(GarageConstant.GARAGE_RES_MSG ,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.SUCC_CONFIG_MSG).asText());
					    objectNode.put(GarageConstant.GARAGE_RES_DATA,this.objectMapper.readTree(garageDetails.toString()));
					    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					}
				
			    else
				   {
					ObjectNode objectNode = this.objectMapper.createObjectNode();
				    objectNode.put(GarageConstant.GARAGE_RES_CODE,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.NORECORD_CODE).asInt() );
				    objectNode.put(GarageConstant.GARAGE_RES_MSG ,DocumentDataConfig.getConfigDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.NORECORD_MESSAGES).asText());
				    objectNode.put(GarageConstant.GARAGE_RES_DATA,errorNode);
				    log.info("Record Not Found : "+garageDetails);
				    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				   }
		     }
		  }catch(Exception e)
		    {
			
			ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(GarageConstant.GARAGE_RES_CODE,DocumentDataConfig.getcacheDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.FAILURE_CODE).asInt());
		    objectNode.put(GarageConstant.GARAGE_RES_CODE,DocumentDataConfig.getcacheDocList().get(GarageConstant.RESPONSE_MESSAGES).get(GarageConstant.FAILURE_MESSAGES).asText());
		    objectNode.put(GarageConstant.GARAGE_RES_DATA,errorNode);
		    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		    
		   }
		
		
		
		
		
	}

}
