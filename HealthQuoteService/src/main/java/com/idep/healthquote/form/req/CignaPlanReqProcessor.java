
package com.idep.healthquote.form.req;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CignaPlanReqProcessor implements Processor 
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaPlanReqProcessor.class.getName());
	CBService productConfig = CBInstanceProvider.getProductConfigInstance();
	static String ALL_Health_SA_ADDON_Details;
	static List<Map<String, Object>> healthSAAddOnDetailsList;
	
	public void process(Exchange exchange) throws ExecutionTerminator
	{
		try
	    {	  
	    	String cignaReqMessage = exchange.getIn().getBody().toString();
	    	JsonNode cignareqNode = this.objectMapper.readTree(cignaReqMessage);
	    	JsonNode productInfoNode = cignareqNode.get(HealthQuoteConstants.PRODUCT_INFO);		
	    	/**
	    	 * Read all HealthSAAddOnDetails info from Product Data
	    	 */
		    ALL_Health_SA_ADDON_Details ="select ARRAY_AGG(ProductData) as riderData,riderId from ProductData where documentType='HealthSAAddOnDetails' and planId= "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()+" and carrierId="+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+" group by riderId";
			log.debug("ALL_Health_SA_ADDON_Details: " +ALL_Health_SA_ADDON_Details);
			healthSAAddOnDetailsList = productConfig.executeQueryCouchDB(ALL_Health_SA_ADDON_Details);			
			log.debug("Query result in healthSAAddOnDetailsList:" +healthSAAddOnDetailsList);
			/**
		     * Read Product ,Rider details from Product Data and append it into requestNode
		     */
	    	log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"RIDERREQPROCESS|SUCCESS|"+"riders processing initiated");
	    	exchange.setProperty("headerChangeReq","Y");
		    addproductDetails(cignareqNode);
		    log.debug("Modified cignareqNode:" +cignareqNode);
		    exchange.setProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(cignareqNode));
		    exchange.getIn().setBody(cignareqNode);
		    
		    
	    }
		 catch (NullPointerException e)
	    {
			 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"NullPointerException at CignaPlanReqProcessor  : ",e);
			 throw new ExecutionTerminator();
	    }
	    catch (Exception e)
	    {
	    	 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"CignaPlanReqProcessor  : ",e);
	    	throw new ExecutionTerminator();
	    }
	}

	public  JsonNode addproductDetails(JsonNode cignareqNode) throws Exception
	{
		JsonNode productInfoNode = cignareqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		JsonNode healthProductDetailsNode = this.objectMapper.readTree(this.productConfig.getDocBYId(HealthQuoteConstants.CARRIER_PLAN+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()).content().toString());
		log.debug("healthProductDetailsNode in cignaPlanreqProcessor: "+healthProductDetailsNode);
				
		/**
		 * Add basePlanOptionCd and sumInsured value of product into request node
		 */
		 ObjectNode insuredProductConfigNode = objectMapper.createObjectNode();
		 insuredProductConfigNode.put("productPlanOptionCd",productInfoNode.get("productPlanOptionCd"));
		 insuredProductConfigNode.put("sumInsured",productInfoNode.get("sumInsured"));
		((ObjectNode)cignareqNode).put("insuredProductConfig", insuredProductConfigNode);
		
    	/**
    	 * Add rider and product details into planRidersDetailsArray
    	 */
		ArrayNode planRidersDetailsArray = objectMapper.createArrayNode();
		/**
    	 * Add ADDONS details into planAddonDetailsArray
    	 */
		ArrayNode planAddonDetailsArray = objectMapper.createArrayNode();
		try
		{
			ObjectNode objNode = objectMapper.createObjectNode();
	    	/**
	    	 * Insert Product details into node
	    	 */
			objNode.put("productId",healthProductDetailsNode.get("clientProductCode").asText());
			objNode.put("productTypeCd","SUBPLAN");
			objNode.put("productFamilyCd",healthProductDetailsNode.get("productrFamilyCd"));
			objNode.put("sumInsured","");
			objNode.put("productPlanOptionCd",productInfoNode.get("productPlanOptionCd"));
			planRidersDetailsArray.add(objNode);
						
			/**
			 * Group Carrier riders details into object node carrierRiderObjNode
			 */
			ArrayNode carrierRiderList = (ArrayNode)healthProductDetailsNode.get("plans").get(0).get("riderList");
			ObjectNode carrierRiderObjNode = objectMapper.createObjectNode();
			for(JsonNode carrierRider : carrierRiderList)
			{
				carrierRiderObjNode.put(carrierRider.get("riderId").asText(),carrierRider);
				/*log.info("carrierRiderObjNode"+carrierRiderObjNode);
				log.info("carrierRider"+carrierRider);*/	
			}
			
			/**
			 * Group all healthSAAddOnDetails into object node addonObjNode
			 */
			JsonNode healthSAAddOnListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSAAddOnDetailsList));
			log.debug("healthSAAddOnList :"+healthSAAddOnListNode);
			ObjectNode addonObjNode = objectMapper.createObjectNode();
			for(JsonNode addons : healthSAAddOnListNode)
			{
				addonObjNode.put(addons.get("riderId").asText(),addons.get("riderData"));
			}
			/**
			 * Read input riders list			
			 */
			if(cignareqNode.get("quoteParam").has("riders") && cignareqNode.get("quoteParam").get("riders").size()>0)
			{
				try{
				 JsonNode riderList = cignareqNode.get("quoteParam").get("riders");
				 for(JsonNode rider : riderList)
					{
					 	log.debug("rider procesing started : "+rider.get("riderId").asText());
						if(carrierRiderObjNode.has(rider.get("riderId").asText()))
						{
						/**
						 * If  riders present in input request then add riders details into request node
						 */
						log.debug("Details of RIDER Node :"+carrierRiderObjNode.get(rider.get("riderId").asText()));
						JsonNode riderDetailsNode = carrierRiderObjNode.get(rider.get("riderId").asText());
							if(riderDetailsNode.get("productTypeCd").asText().equalsIgnoreCase("RIDER"))
							{
								ObjectNode riderObjNode = objectMapper.createObjectNode();
								riderObjNode.put("productId",riderDetailsNode.get("clientRiderCode").asText());
								riderObjNode.put("productTypeCd",riderDetailsNode.get("productTypeCd").asText());
								riderObjNode.put("productFamilyCd",riderDetailsNode.get("riderFamilyCd").asText());
								riderObjNode.put("sumInsured",productInfoNode.get("sumInsured"));
								riderObjNode.put("productPlanOptionCd",productInfoNode.get("productPlanOptionCd"));
								planRidersDetailsArray.add(riderObjNode);
								//break;
							}
							/**
							 * If ADDONS present in input request then add addons details into request node
							 */
							else if (riderDetailsNode.get("productTypeCd").asText().equalsIgnoreCase("ADDON"))
							{
							 	double sumInsured = productInfoNode.get(HealthQuoteConstants.DROOLS_SUM_INSURED).doubleValue();
								if(addonObjNode.has(rider.get("riderId").asText()))
								{
								JsonNode addonDetailsNode = addonObjNode.get(rider.get("riderId").asText());
								 //ArrayNode addonDetailsArrNode = (ArrayNode)addonDetailsNode;
								 for(JsonNode addonNode :addonDetailsNode)
								 {
									 
									if(sumInsured >= addonNode.get("minSumInsured").doubleValue() && sumInsured <= addonNode.get("maxSumInsured").doubleValue())
									{
										/*log.info("Add riders details ");
										log.info(addonNode.get("clientRiderCode").asText());
										log.info(productInfoNode.get("sumInsured"));
										log.info(addonNode.get("riderOptionCD").asText());*/
									    ObjectNode riderObjNode = objectMapper.createObjectNode();
										 riderObjNode.put("productId",addonNode.get("clientRiderCode").asText());
										 riderObjNode.put("sumInsured",productInfoNode.get("sumInsured"));
										 riderObjNode.put("productPlanOptionCd",addonNode.get("riderOptionCD").asText());
										 planAddonDetailsArray.add(riderObjNode);
										 
									 break;
									 
									}	
								 }
								 }
							}
							else
							{
								log.info("productTypeCd is other than RIDER and ADDON which Cigna doesn't support hence skipped");
							}
						}
						
					} 	
				}
				
				catch(Exception e)
				{
					 log.error("Exception at CignaPlanReqProcessor : ", e);
				}
			}
					
			log.debug("planRidersDetailsArray"+planRidersDetailsArray);
			((ObjectNode)cignareqNode).put("planRidersDetails", planRidersDetailsArray);
			log.debug("planAddonDetailsArray"+planAddonDetailsArray);
			((ObjectNode)cignareqNode).put("planAddonDetails", planAddonDetailsArray);
			log.debug("after adding planRidersDetails and planAddonDetails: "+cignareqNode);
		
		}
		catch(NullPointerException e)
		 {
			 log.error("NullPointerException : "+e);
			 
		 } 
		catch(Exception e)
		 {
			 log.error(e);
		 } 

		return cignareqNode;
	}

}
