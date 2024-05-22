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

public class CignaSumInsuredReqProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaSumInsuredReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService productService = CBInstanceProvider.getProductConfigInstance();
	static String ALL_Health_SA_Details;
	static List<Map<String, Object>> healthSADetailsList;
	
	/*static
	{
		ALL_Health_SA_Details ="select ProductData.* from ProductData where documentType='HealthSADetails' and planId=13 and carrierId=35 and rate=10000";
		healthSADetailsList = productService.executeQuery(ALL_Health_SA_Details);
	}*/
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {	  
		    	String cignaMessage = exchange.getIn().getBody().toString();
		    	JsonNode siReqNode = this.objectMapper.readTree(cignaMessage);
			    JsonNode productInfoNode = siReqNode.get(HealthQuoteConstants.PRODUCT_INFO);
			  /*  log.info("productInfoNode before findSADetails:" +productInfoNode);
			    log.info("PLANID = "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue());
			    log.info("CARRIERID = "+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue());*/
			    ALL_Health_SA_Details ="select ProductData.* from ProductData where documentType='HealthSADetails' and planId= "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()+" and carrierId="+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+" and rate=10000";
				log.debug("ALL_Health_SA_Details: " +ALL_Health_SA_Details);
			    healthSADetailsList = productService.executeQuery(ALL_Health_SA_Details);
				log.debug("Query result in healthSADetailsList:" +healthSADetailsList);
			    productInfoNode = findSADetails(productInfoNode);
			    log.debug("productInfoNode after findSADetails:" +productInfoNode);
			  
			   JsonNode healthReqConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.CARRIER_HEALTH_QUOTE_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()).content().toString());
			   // log.info("healthReqConfigNode: "+healthReqConfigNode);
			    /**
			     * set request configuration document id HealthQuoteRequest
			     */
			    exchange.setProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,healthReqConfigNode);
			    log.debug("siReqNode after findSADetails:" +siReqNode);
			    exchange.getIn().setBody(siReqNode);
				
		    }
		    catch (NullPointerException e)
		    {
		    	log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"Nullpointer CignaSumInsureReqProcessor : ",e);
		      throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		    	log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"CignaSumInsureReqProcessor : ",e);
		      throw new ExecutionTerminator();
		    }
	  }
	 
	 public  JsonNode findSADetails(JsonNode productInfoNode)
		{
		 	 double sumInsured = productInfoNode.get(HealthQuoteConstants.DROOLS_SUM_INSURED).doubleValue();
		 	 log.info("sumInsured in findSADetails: "+sumInsured);
			 try
			 {		
				 JsonNode healthSAListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSADetailsList));
				 
				 ArrayNode saDetailsArrNode = (ArrayNode)healthSAListNode;
				 for(JsonNode saDetailsNode :saDetailsArrNode)
				 {
					 if(sumInsured >= saDetailsNode.get("minSumInsured").doubleValue() && sumInsured <= saDetailsNode.get("maxSumInsured").doubleValue())
					 {
						 ((ObjectNode)productInfoNode).putAll((ObjectNode)saDetailsNode);
						 break;
					 }
				 }
			 }
			 catch(Exception e)
			 {
				 log.error("Exception at findSADetails : ",e);
			 } 
			
			 return productInfoNode;		  
		}
	 
	 

}
