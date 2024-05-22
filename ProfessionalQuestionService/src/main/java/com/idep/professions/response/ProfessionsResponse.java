package com.idep.professions.response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.professions.constant.ProfessionalConstant;


public class ProfessionsResponse implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProfessionsResponse.class);
	JsonNode errorNode=null;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			String response = exchange.getIn().getBody(String.class);
			log.info("recived body request : "+response);
			JsonNode carrierResponseNode = objectMapper.readTree(response);
		
			/* set response in exchange body */
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProfessionalConstant.RES_CODE, 1000);
			obj.put(ProfessionalConstant.RES_MSG, "success");
			obj.put(ProfessionalConstant.RES_DATA, carrierResponseNode);
			exchange.getIn().setBody(obj);

		}
		catch(Exception e)
		{
			this.log.error("ProposalResProcessor Exception : ",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProfessionalConstant.RES_CODE, 1002);
			obj.put(ProfessionalConstant.RES_MSG, "server error");
			obj.put(ProfessionalConstant.RES_DATA, errorNode);
			exchange.getIn().setBody(obj);
		}
		 
	}
}