package com.idep.healthquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class MapperReqProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(MapperReqProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
	
		 try {
			 
			 	String mapperReq = exchange.getIn().getBody(String.class);
			 	JsonNode mapperReqNode = this.objectMapper.readTree(mapperReq);
			 	exchange.setProperty(HealthQuoteConstants.MAPPER_SUM_INSURED,mapperReqNode.get(HealthQuoteConstants.CARRIER_SUM_INSURED));
			 	exchange.getIn().setBody(this.objectMapper.writeValueAsString(mapperReqNode.get(HealthQuoteConstants.CARRIER_REQUEST_FORM)));
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at MapperReqProcessor : ",e);
			 throw new ExecutionTerminator();
		 }
		
	
	}

}
