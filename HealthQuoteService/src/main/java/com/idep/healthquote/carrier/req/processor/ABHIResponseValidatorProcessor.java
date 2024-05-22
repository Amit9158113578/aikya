package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;


/**
 * @author pravin.jakhi
 *  
 */
public class ABHIResponseValidatorProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdityaBirlaInsuredListProcessor.class.getName());
	
	
	@Override
	public void process(Exchange exchange) throws Exception,ExecutionTerminator {
	
		try{
			
			String response = exchange.getIn().getBody(String.class);
			JsonNode carrierResponse = objectMapper.readTree(response);
			JsonNode errorList = carrierResponse.get("errorList");
				if(errorList.has("errorCode") && !errorList.get("errorList").get("errorCode").asText().equalsIgnoreCase("0")){
					log.info("ABHI Quote Carrier Response in error : "+carrierResponse);
					ObjectNode obj = this.objectMapper.createObjectNode();
					obj.put(HealthQuoteConstants.QUOTE_RES_CODE, HealthQuoteConstants.QUOTE_RESECODEFAIL);
					obj.put(HealthQuoteConstants.QUOTE_RES_MSG, HealthQuoteConstants.QUOTE_RESEMSGEFAIL);
					obj.put(HealthQuoteConstants.QUOTE_RES_DATA, errorList);
					exchange.getIn().setBody(obj);
					throw new ExecutionTerminator();
				}
				exchange.getIn().setBody(carrierResponse);
		}catch(Exception e){
			log.info("Error at ABHIResponseValidatorProcessor :  "+e);
			throw new ExecutionTerminator();
		}
	}
}
