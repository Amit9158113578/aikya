package com.idep.healthquote.form.req;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CignaADDONReqProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaADDONReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService productConfig = CBInstanceProvider.getProductConfigInstance();
	static String ALL_Health_SA_ADDON_Details;
	static List<Map<String, Object>> healthSAAddOnDetailsList;

	public void process(Exchange exchange) throws ExecutionTerminator 
	{
		try
			{	  
		    	String cignaMessage = exchange.getIn().getBody().toString();
		    	JsonNode reqNode = this.objectMapper.readTree(cignaMessage);
		    	log.info("siReqNode:" +reqNode);
			    JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
			    log.info("productInfoNode before addproductAddonDetails:" +productInfoNode);
			    
			    ALL_Health_SA_ADDON_Details ="select ARRAY_AGG(ProductData) as riderData,riderId from ProductData where documentType='HealthSAAddOnDetails' and planId= "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()+" and carrierId="+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+" group by riderId";
				log.info("ALL_Health_SA_ADDON_Details: " +ALL_Health_SA_ADDON_Details);
				//log.info("Query result in new healthSAAddOnDetailsList:"+productConfig.executeQueryCouchDB(ALL_Health_SA_ADDON_Details));
				healthSAAddOnDetailsList = productConfig.executeQueryCouchDB(ALL_Health_SA_ADDON_Details);
				log.info("Query result in healthSAAddOnDetailsList:" +healthSAAddOnDetailsList);
			    addproductAddonDetails(reqNode);
			    log.info("productInfoNode after addproductAddonDetails:" +productInfoNode);
			  
		        log.info("siReqNode after addproductAddonDetails:" +reqNode);
			    exchange.getIn().setBody(reqNode);
			 }
		catch (NullPointerException e)
		    {
		      log.error("NullPointerException at CignaADDONReqProcessor : ", e);
		      throw new ExecutionTerminator();
		    }
		catch (Exception e)
		    {
		       log.error("Exception at CignaADDONReqProcessor : ", e);
		       throw new ExecutionTerminator();
		    }
	  }
	 
	 public  JsonNode addproductAddonDetails(JsonNode requestNode) throws JsonProcessingException, IOException
		{
		 	JsonNode productInfoNode = requestNode.get(HealthQuoteConstants.PRODUCT_INFO);
		 	double sumInsured = productInfoNode.get(HealthQuoteConstants.DROOLS_SUM_INSURED).doubleValue();
			ArrayNode planAddonDetailsArray = objectMapper.createArrayNode();
			JsonNode healthSAAddOnListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSAAddOnDetailsList));
			log.info("healthSAAddOnList :"+healthSAAddOnListNode);
			//ArrayNode saAddonDetailsArrNode = (ArrayNode)healthSAAddOnListNode;
			//log.info("saAddonDetailsArrNode: "+saAddonDetailsArrNode);
			ObjectNode addonObjNode = objectMapper.createObjectNode();
			for(JsonNode addons : healthSAAddOnListNode)
			{
				addonObjNode.put(addons.get("riderId").asText(),addons.get("riderData"));
				log.info("addonObjNode"+addonObjNode);
				log.info("addons: "+addons.get("riderId").asText());	
				log.info("addon data: "+addons.get("riderData"));
			}
		 	
			 try
			 {			
				 log.info("riders size: "+requestNode.get("riders").size());
				 if(requestNode.has("riders") && requestNode.get("riders").size()>0)
				 	{
					 JsonNode riderList = requestNode.get("riders");
						for(JsonNode rider : riderList)
						{
							log.info("RiderInfo:"+rider);
							log.info("RIDERID:"+rider.get("riderId").asText());
							if(addonObjNode.has(rider.get("riderId").asText()))
							{
								log.info("Details of addon Node :"+addonObjNode.get(rider.get("riderId").asText()));
							JsonNode addonDetailsNode = addonObjNode.get(rider.get("riderId").asText());
							log.info("addonDetailsNode :"+addonDetailsNode);
							log.info("SumInsured in addonDetailsNode"+sumInsured);
							 ArrayNode addonDetailsArrNode = (ArrayNode)addonDetailsNode;
							 for(JsonNode addonNode :addonDetailsArrNode)
							 {
							if(sumInsured >= addonNode.get("minSumInsured").doubleValue() && sumInsured <= addonNode.get("maxSumInsured").doubleValue())
								{
								log.info("Add riders details ");
								log.info("addonNode :"+addonNode);
								//append Rider info 
								ObjectNode riderObjNode = objectMapper.createObjectNode();
								 riderObjNode.put("productId",addonNode.get("clientRiderCode").asText());
								 riderObjNode.put("sumInsured",productInfoNode.get("sumInsured"));
								 riderObjNode.put("productPlanOptionCd",addonNode.get("riderOptionCD"));
								 planAddonDetailsArray.add(riderObjNode);
								 break;	
								}	
							 }
							}
						} 
						log.info("planAddonDetailsArray"+planAddonDetailsArray);
						((ObjectNode)requestNode).put("planAddonDetails", planAddonDetailsArray);
						
						log.info("after adding planAddonDetailsArray : "+requestNode);
				 	}
				}
			 catch(Exception e)
			 {
				 log.error("Exception at findSADetails : ",e);
			 } 
			
			 return requestNode;		  
		}
	 
	 

}

