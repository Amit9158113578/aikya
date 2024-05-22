package com.idep.healthquestion.req.processor;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class LifeDiseaseReqProcessor implements Processor {
	Logger log = Logger.getLogger(LifeDiseaseReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{
		try
		{   log.info("Inside LifeDiseaseReqProcessor");
			String inputReq = exchange.getIn().getBody().toString();
			String diseaseQuery=null;
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			JsonNode DiseasePlanConfig = null;
			List<Map<String, Object>> diseaseList = null;
			diseaseQuery="select ProductData.* from ProductData where documentType='LifeDiseaseMapping' and carrierId="+inputReqNode.get("carrierId").asText()+" "
					+ "and planId="+inputReqNode.get("planId").asText()+" ORDER BY preference";
			log.info(" diseaseQuery: "+diseaseQuery);
			
			if(inputReqNode.has("carrierId") && inputReqNode.has("planId"))
			{
				
				try
				{
					log.info("inside life disease req processsor");
					DiseasePlanConfig = objectMapper.readTree(serviceConfig.getDocBYId("LifePlanDiseaseConfig").content().toString());
					if(DiseasePlanConfig!=null)
					{
						if(DiseasePlanConfig.has(inputReqNode.get("carrierId").asInt()+"-"+inputReqNode.get("planId").asInt()))
						{
							diseaseQuery="select ProductData.* from ProductData where documentType='LifeDiseaseMapping' and carrierId="+inputReqNode.get("carrierId").asInt()+" "
									+ "and planId="+inputReqNode.get("planId").asInt()+" ORDER BY preference";
							log.info(" diseaseQuery: "+diseaseQuery);
						}
						else
						{
							diseaseQuery="select ProductData.* from ProductData where documentType='LifeDiseaseMapping' and carrierId="+inputReqNode.get("carrierId").asInt()+" ORDER BY preference";
							log.info(" diseaseQuery: "+diseaseQuery);
						}
						try
						{
						diseaseList = productService.executeQueryCouchDB(diseaseQuery);
						log.info(" diseaseQuery: "+diseaseList);
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
						log.error("LifePlanDiseaseConfig document not found in DB");
					}
				}
				catch(Exception e)
				{
					log.error("failed to fetch LifePlanDiseaseConfig document from DB ",e);
				}
									
			}// checking carrierId and planId present 
			exchange.getIn().setBody(objectMapper.writeValueAsString(inputReqNode));
			
		}
		catch(Exception e)
		{
			log.error("Error AT LifeDiseaseReqProcessor : ",e);
		}	
		
	}

}
