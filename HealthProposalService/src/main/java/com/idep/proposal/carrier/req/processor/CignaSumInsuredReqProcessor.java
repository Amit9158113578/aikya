package com.idep.proposal.carrier.req.processor;

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
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CignaSumInsuredReqProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaSumInsuredReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService productService = CBInstanceProvider.getProductConfigInstance();
	static String ALL_Health_SA_Details;
	static List<Map<String, Object>> healthSADetailsList;
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {	  
		    	String proposalReq  = exchange.getIn().getBody(String.class);
				JsonNode requestNode = objectMapper.readTree(proposalReq);
			    ALL_Health_SA_Details ="select ProductData.* from ProductData where documentType='HealthSADetails' and planId= "+requestNode.get(ProposalConstants.PLAN_ID).intValue()+" and carrierId="+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+" and rate=10000";
			    healthSADetailsList = productService.executeQuery(ALL_Health_SA_Details);
				requestNode = findSADetails(requestNode);
			   
			    exchange.getIn().setBody(requestNode);
				
		    }
		    catch (NullPointerException e)
		    {
		    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NullPointerException|",e);
		    	throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaSumInsuredReqProcessor|",e);
		    	throw new ExecutionTerminator();
		    }
	  }
	 /**
	  * Insert productplanoptioncode info to request node
	  * @param requestNode
	  * @return
	  */
	 public  JsonNode findSADetails(JsonNode requestNode)
		{
		 	 double sumInsured = requestNode.get("coverageDetails").get("sumAssured").doubleValue();
			 try
			 {		
				 JsonNode healthSAListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSADetailsList));
				 
				 ArrayNode saDetailsArrNode = (ArrayNode)healthSAListNode;
				 for(JsonNode saDetailsNode :saDetailsArrNode)
				 {
					 //System.out.println("node : "+saDetailsNode.get("minSumInsured").doubleValue());
					 if(sumInsured >= saDetailsNode.get("minSumInsured").doubleValue() && sumInsured <= saDetailsNode.get("maxSumInsured").doubleValue())
					 {
						 ((ObjectNode)requestNode).putAll((ObjectNode)saDetailsNode);
						 break;
					 }
				 }
			 }
			 catch(Exception e)
			 {
				 log.error("Exception at findSADetails : ",e);
				
			 }	
			 return requestNode;		  
		}
	 
	 

}

