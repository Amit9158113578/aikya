package com.idep.healthquote.form.req;

/**
 * @author pravin.jakhi
 *  Processor written for Add service Tax as per carrier in drool request.
 */

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;
import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HealthDroolReqServiceTaxProcessor implements Processor {

	Logger log = Logger.getLogger(HealthDroolReqServiceTaxProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService service =  CBInstanceProvider.getPolicyTransInstance();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String quotedata = exchange.getIn().getBody().toString();
		      JsonNode reqNode = this.objectMapper.readTree(quotedata);
		      log.debug("HealthDroolReqServiceTaxProcessor : reqNode : "+reqNode);
		      JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		      JsonNode quoteParamNode = reqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM);
		      JsonNode personalInfo = reqNode.get("personalInfo");
		      log.debug("HealthDroolReqServiceTaxProcessor : quoteParamNode : "+quoteParamNode);
		      JsonDocument document = serverConfig.getDocBYId(HealthQuoteConstants.SERVICETAXDOC);
		      if(document!=null)
		      {
		    	  if(personalInfo.has("state")){
		    		  JsonNode proposalConfigNode = objectMapper.readTree(document.content().toString()); 
			    	  if(personalInfo.get("state").asText().equalsIgnoreCase("JAMMU AND ASHMIR") || personalInfo.get("state").asText().contains("JAMMU")){
			    		  ((ObjectNode) quoteParamNode).put(HealthQuoteConstants.SERVICETAX,proposalConfigNode.get("JKTAX") );
			    		  log.debug("HealthDroolReqServiceTaxProcessor ServiceTax FOR JKAX : "+proposalConfigNode.get("JKTAX"));
			    	  }else{			    		  
				    	  	String carrierId=productInfoNode.get("carrierId").asText();
				    	  	log.debug("HealthDroolReqServiceTaxProcessor ServiceTax CarrierId : "+carrierId+"   "+proposalConfigNode);
				    	  	((ObjectNode) quoteParamNode).put(HealthQuoteConstants.SERVICETAX,proposalConfigNode.get(carrierId) );
			    	  }
			      }
		      } 
		    
		       log.debug("HealthDroolReqServiceTaxProcessor ServiceTax Added : "+reqNode);
		      exchange.getIn().setBody(reqNode);
		      
		}catch(Exception e){
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"Service Tax not set HealthDroolReqServiceTaxProcessor :",e);
			throw new ExecutionTerminator();
		}
	}
}


