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


public class LifeJobReqProcessor implements Processor {
	Logger log = Logger.getLogger(LifeJobReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{
		try
		{   log.info("Inside LifeJobReqProcessor");
			String inputReq = exchange.getIn().getBody().toString();
			String JobQuery=null;
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			JsonNode JobPlanConfig = null;
			List<Map<String, Object>> JobList = null;
			if(inputReqNode.has("carrierId") && inputReqNode.has("planId"))
			{
				try
				{
					JobPlanConfig = objectMapper.readTree(serviceConfig.getDocBYId("LifePlanJobConfig").content().toString());
					if(JobPlanConfig!=null)
					{
						if(JobPlanConfig.has(inputReqNode.get("carrierId").asInt()+"-"+inputReqNode.get("planId").asInt()))
						{
							JobQuery="select ProductData.* from ProductData where documentType='LifeJobMapping' and carrierId="+inputReqNode.get("carrierId").asInt()+" "
									+ "and planId="+inputReqNode.get("planId").asInt()+" ORDER BY preference";
							log.info(" JobQuery: "+JobQuery);
						}
						else
						{
							JobQuery="select ProductData.* from ProductData where documentType='LifeJobMapping' and carrierId="+inputReqNode.get("carrierId").asInt()+" ORDER BY preference";
							log.info(" JobQuery: "+JobQuery);
						}
						try
						{
						JobList = productService.executeQueryCouchDB(JobQuery);
						log.info(" JobQuery: "+JobList);
						}
						catch(Exception e)
						{
							log.error("failed to execute query "+JobQuery,e );
						}
						JsonNode JobFinalList = objectMapper.readTree(objectMapper.writeValueAsString(JobList));
						log.debug("JobList Result After Query Executed : "+JobList);
						((ObjectNode)inputReqNode).put("data", JobFinalList);
					}
					else
					{
						log.error("LifePlanJobConfig document not found in DB");
					}
				}
				catch(Exception e)
				{
					log.error("failed to fetch LifePlanJobConfig document from DB ",e);
				}
									
			}// checking carrierId and planId present 
			exchange.getIn().setBody(objectMapper.writeValueAsString(inputReqNode));
			
		}
		catch(Exception e)
		{
			log.error("Error AT LifeJobReqProcessor : ",e);
		}	
		
	}

}
