package com.idep.healthquestion.req.processor;

/**
 * @author Shweta.Joshi
 */

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TravelDiseaseReqProcessor implements Processor {
	Logger log = Logger.getLogger(TravelDiseaseReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{
		try
		{			
			String inputReq = exchange.getIn().getBody().toString();
			String diseaseQuery=null;
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			JsonNode DiseasePlanConfig = null;
			List<Map<String, Object>> diseaseList = null;
			if(inputReqNode.has("carrierId") && inputReqNode.has("planId"))
			{
				try
				{
					DiseasePlanConfig = objectMapper.readTree(serviceConfig.getDocBYId("TravelPlanDiseaseConfig").content().toString());
					if(DiseasePlanConfig!=null)
					{
						if(DiseasePlanConfig.has(inputReqNode.get("carrierId").asText()+"-"+inputReqNode.get("planId").asText()))
						{
							diseaseQuery="select ProductData.* from ProductData where documentType='TravelDiseaseMapping' and carrierId="+inputReqNode.get("carrierId").asText()+" "
									+ "and planId="+inputReqNode.get("planId").asText()+" ORDER BY preference";
						}
						else
						{
							diseaseQuery="select ProductData.* from ProductData where documentType='TravelDiseaseMapping' and carrierId="+inputReqNode.get("carrierId").asText()+" ORDER BY preference";
						}
						try
						{
						diseaseList = productService.executeQueryCouchDB(diseaseQuery);
						}
						catch(Exception e)
						{
							log.error("failed to execute query "+diseaseQuery,e );
						}
						JsonNode diseaseFinalList = objectMapper.readTree(objectMapper.writeValueAsString(diseaseList));
						log.debug("diseaseList Result After Query Executed : "+diseaseList);
						((ObjectNode)inputReqNode).put("data", diseaseFinalList);
					}
					else
					{
						log.error("TravelPlanDiseaseConfig document not found in DB");
					}
				}
				catch(Exception e)
				{
					log.error("failed to fetch TravelPlanDiseaseConfig document from DB ",e);
				}
									
			}// checking carrierId and planId present 
			exchange.getIn().setBody(objectMapper.writeValueAsString(inputReqNode));
			
		}
		catch(Exception e)
		{
			log.error("Error AT TravelDiseaseReqProcessor : ",e);
		}	
		
	}

}
