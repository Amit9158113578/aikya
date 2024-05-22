package com.idep.healthquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ExternalServiceReqProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ExternalServiceReqProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	JsonNode responseConfigNode = null;
	JsonNode errorNode;
	JsonNode healthSumCalc = null;
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {
		    	
		      if (this.serverConfig == null) {
		        	
		          this.serverConfig = CBInstanceProvider.getServerConfigInstance();
		          this.responseConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
		          this.productService = CBInstanceProvider.getProductConfigInstance();
		      }	
		        
		      String quotedata = exchange.getIn().getBody().toString();
		      log.debug("quotedata in ExternalServiceReqProcessor:" +quotedata);
		      JsonNode reqNode = this.objectMapper.readTree(quotedata);
		      
		      log.debug("reqNode:" +reqNode);
		      JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		      log.debug("productInfoNode:" +productInfoNode);
			  // set request configuration document id for sutrrMapper
			  exchange.setProperty(HealthQuoteConstants.CARRIER_REQ_MAP_CONF,HealthQuoteConstants.CARRIER_HEALTH_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+
					    		  							  "-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue());
			  
			  // set input request as property for sutrrMapper to prepare quote response
			  exchange.setProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(reqNode));
			  exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.TRUE);
		     //  write code to gather all attributes to send to sutrrMapper for transforming request 
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(reqNode));
		      this.log.debug("External : HealthQuote Service Req is sent for client specific transformation");
		      
		    }
		    catch (NullPointerException e)
		    {
		    	log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"NullPointerException at ExternalServiceReqProcessor : ",e);
		     // this.log.error("NullPointerException at ExternalServiceReqProcessor : ", e);
		      /*exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);*/
		      throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		     // this.log.error("Exception at ExternalServiceReqProcessor : ", e);
		      log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"Exception at ExternalServiceReqProcessor  : ",e);
		      /*exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);*/
		      throw new ExecutionTerminator();
		    }
	  }
	 
}

		    