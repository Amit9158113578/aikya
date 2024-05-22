package com.idep.healthquote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.healthquote.exception.processor.RequestValidationTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * @author pravin.jakhi
 * this class validate validate Quote Request, at the time of validation process. 
 * if quote input request in requestFailure flag if Y then it will terminate process. 
 */
public class HealthReqQValidatorProcessor implements Processor {

	Logger log = Logger.getLogger(HealthReqQValidatorProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws RequestValidationTerminator,Exception {

		
		try{
			
			String input = exchange.getIn().getBody(String.class);
			
			JsonNode inputReq=objectMapper.readTree(input);
			log.debug("HealthReqQValidatorProcessor : "+inputReq+" \t inputReq.has(requestFailure) : "+inputReq.has("requestFailure"));
			
			if(inputReq.has("requestFailure")){
				
				if(inputReq.get("requestFailure").asText().equalsIgnoreCase("Y")){
					log.debug("Quote Request Failure "+input);
						throw new RequestValidationTerminator();
				}
				
			}else{
				exchange.getIn().setBody(inputReq);	
			}
		}catch(Exception e){
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthReqQValidatorProcessor ",e);
			throw new RequestValidationTerminator();
		}
	}
}
