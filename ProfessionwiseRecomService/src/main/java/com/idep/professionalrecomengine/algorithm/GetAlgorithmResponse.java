package com.idep.professionalrecomengine.algorithm;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.insassessment.invoke.invoker;



public class GetAlgorithmResponse implements Processor{
	Logger log = Logger.getLogger(GetAlgorithmResponse.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	ObjectNode response = objectMapper.createObjectNode();
	@Override
	public void process(Exchange exchange) throws Exception {
		String inputRequest = exchange.getIn().getBody().toString();
		JsonNode inputRequestNode = objectMapper.readTree(inputRequest);
		log.info("P365 Input Request : "+inputRequestNode );
		invoker invokeMethod = new invoker();
		response = invokeMethod.invoke(inputRequestNode);
		log.info("RESPONSE::: "+response);

		exchange.getIn().setBody(objectMapper.writeValueAsString(response));
	}
	
}
