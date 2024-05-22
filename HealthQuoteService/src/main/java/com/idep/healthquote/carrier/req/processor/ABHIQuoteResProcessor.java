
package com.idep.healthquote.carrier.req.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * @author pravin.jakhi
 *
 */
public class ABHIQuoteResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIMemberCodeProcessor.class.getName());
	CBService service = null;
	  JsonNode responseConfigNode;
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator, JsonProcessingException, IOException {
	
		String quoteServiceRes=null;
	try{
		 if (this.service == null)
	      {
	        this.service = CBInstanceProvider.getServerConfigInstance();
	        this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
	        this.log.info("ResponseMessages configuration loaded");
	        
	      }
		 quoteServiceRes= exchange.getIn().getBody(String.class);
		JsonNode resNode =objectMapper.readTree(quoteServiceRes);
		log.debug("ABHIQuoteResProcessor : "+resNode.get("carrierRequestForm").get("annualPremium").asDouble());
		if(resNode.get("carrierRequestForm").has("annualPremium") && resNode.get("carrierRequestForm").get("annualPremium").asDouble() > 0.0){
			exchange.getIn().setBody(resNode);	
			  log.info("ABHIQuoteResProcessor resNode: : "+resNode);
		}else{
			 ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.FAIL_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.FAIL_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, "null");
		      exchange.getIn().setBody(objectNode);
		      log.info("ABHIQuoteResProcessor objectNode: : "+objectNode);
		}
	}catch(Exception e){
		log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"Carrier Quote Service Response "+exchange.getIn().getBody(String.class));
		log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+quoteServiceRes);
		throw new ExecutionTerminator();
	}
	
	}

}
