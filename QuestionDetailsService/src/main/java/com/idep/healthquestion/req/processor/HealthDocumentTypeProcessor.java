/**
 * @author Pravin.Jakhi
 */
package com.idep.healthquestion.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
/**
 * @author pravin.jakhi
 *This Processor Added for setHeader in document Type 
 *if Document type = DiseaseMapping then this service return Disease as per planId or without PlanId
 *if Document type = DiseaseQuestion then this service return Disease as per planId or without PlanId
 *
 */
public class HealthDocumentTypeProcessor implements Processor 
{	
	Logger log = Logger.getLogger(HealthDocumentTypeProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try
		{
			String inputReq = exchange.getIn().getBody().toString();
			JsonNode inputReqNode = objectMapper.readTree(inputReq) ;
			
			if(inputReqNode.has("documentType"))
			{
				exchange.setProperty("documentType", inputReqNode.get("documentType").asText());
				log.info("Document Type set in header : "+inputReqNode);
			}
			exchange.getIn().setBody(inputReqNode);
		}
		catch(Exception e)
		{
			log.error("Error At HealthDocumentTypeProcessor : ",e);
		}
			
	}

}
