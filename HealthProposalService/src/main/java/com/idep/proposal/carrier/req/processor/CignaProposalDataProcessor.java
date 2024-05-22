package com.idep.proposal.carrier.req.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CignaProposalDataProcessor implements Processor
{ 
	Logger log = Logger.getLogger(CignaProposalDataProcessor.class.getName());
	CBService productConfig = CBInstanceProvider.getProductConfigInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	static String ALL_Health_SA_ADDON_Details;
	static List<Map<String, Object>> healthSAAddOnDetailsList;
	
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
	
	ArrayNode planRidersDetailsArray = objectMapper.createArrayNode();
	ArrayNode planAddonDetailsArray = objectMapper.createArrayNode();
	
	try
    	{	  
		String proposalReq  = exchange.getIn().getBody(String.class);
		JsonNode proposalReqNode = objectMapper.readTree(proposalReq);
		/**
    	 * Read all HealthSAAddOnDetails info from Product Data
    	 */
	    ALL_Health_SA_ADDON_Details ="select ARRAY_AGG(ProductData) as riderData,riderId from ProductData where documentType='HealthSAAddOnDetails' and planId= "+proposalReqNode.get(ProposalConstants.PLAN_ID).intValue()+" and carrierId="+proposalReqNode.get(ProposalConstants.CARRIER_ID).intValue()+" group by riderId";
		healthSAAddOnDetailsList = productConfig.executeQueryCouchDB(ALL_Health_SA_ADDON_Details);			
		log.debug("Query result in healthSAAddOnDetailsList:" +healthSAAddOnDetailsList);
		JsonNode HealthProductDetailsNode = proposalReqNode.get("HealthCarrierDetails");
		ArrayNode ridersReqNode = (ArrayNode)proposalReqNode.get("coverageDetails").get("riders");
    	
    	/**
    	 * Temporary added rider info into riders.
    	 * Remove this info
    	 */
   
    	ObjectNode objNode = objectMapper.createObjectNode();
    	
		objNode.put("productId",HealthProductDetailsNode.get("clientProductCode").asText());
		objNode.put("productTypeCd","SUBPLAN");
		objNode.put("productFamilyCd","HEALTHREVISED");
		objNode.put("sumInsured",proposalReqNode.get("sumInsured"));
		objNode.put("productPlanOptionCd",proposalReqNode.get("productPlanOptionCd"));
		planRidersDetailsArray.add(objNode);
	
		/**
		 * Read riders details from Product Data
		 */
		ArrayNode carrierRiderList = (ArrayNode)HealthProductDetailsNode.get("plans").get(0).get("riderList");
		ObjectNode carrierRiderObjNode = objectMapper.createObjectNode();
		for(JsonNode carrierRider : carrierRiderList)
		{
			carrierRiderObjNode.put(carrierRider.get("riderId").asText(),carrierRider);
		}
		
		/**
		 * Group all healthSAAddOnDetails into object node addonObjNode
		 */
		JsonNode healthSAAddOnListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSAAddOnDetailsList));
		ObjectNode addonObjNode = objectMapper.createObjectNode();
		for(JsonNode addons : healthSAAddOnListNode)
		{
			addonObjNode.put(addons.get("riderId").asText(),addons.get("riderData"));
		}
		
		/**
		 * Read input riders list			
		 */
		
	if(proposalReqNode.get("coverageDetails").has("riders") && proposalReqNode.get("coverageDetails").get("riders").size()>0)
	{
		try{
			 JsonNode riderList = proposalReqNode.get("coverageDetails").get("riders");
			 for(JsonNode rider : riderList)
				{
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
							riderObjNode.put("sumInsured",proposalReqNode.get("sumInsured"));
							riderObjNode.put("productPlanOptionCd",proposalReqNode.get("productPlanOptionCd"));
							planRidersDetailsArray.add(riderObjNode);
							
						}
						/**
						 * If ADDONS present in input request then add addons details into request node
						 */
						else if (riderDetailsNode.get("productTypeCd").asText().equalsIgnoreCase("ADDON"))
						{
						 	double sumInsured = proposalReqNode.get("sumInsured").doubleValue();
							if(addonObjNode.has(rider.get("riderId").asText()))
							{
							JsonNode addonDetailsNode = addonObjNode.get(rider.get("riderId").asText());
							 //ArrayNode addonDetailsArrNode = (ArrayNode)addonDetailsNode;
							 for(JsonNode addonNode :addonDetailsNode)
							 {
								if(sumInsured >= addonNode.get("minSumInsured").doubleValue() && sumInsured <= addonNode.get("maxSumInsured").doubleValue())
								{
								    ObjectNode riderObjNode = objectMapper.createObjectNode();
									riderObjNode.put("productId",addonNode.get("clientRiderCode").asText());
									riderObjNode.put("sumInsured",proposalReqNode.get("sumInsured"));
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
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaProposalDadaProcessor|",e);
				throw new ExecutionTerminator();
			}
    	}
	
		((ObjectNode)proposalReqNode).put("planRidersDetails", planRidersDetailsArray);
		((ObjectNode)proposalReqNode).put("planAddonDetails", planAddonDetailsArray);
		exchange.getIn().setBody(proposalReqNode);
    }
	 catch (NullPointerException e)
		{
		 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaProposalDadaProcessor:NullPointerException|",e);
			throw new ExecutionTerminator();
         }
     catch (Exception e)
    	{
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaProposalDadaProcessor|",e);
		throw new ExecutionTerminator();
         }
	}

}
