package com.idep.healthquote.req.processor;

import java.text.SimpleDateFormat;
import org.apache.camel.Exchange;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

import org.apache.camel.Processor;

public class HealthQuoteElderMemberDataProcessor implements Processor
{
	Logger log = Logger.getLogger(HealthQuoteElderMemberDataProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception {

		try {
			
			String quoteReq  = exchange.getIn().getBody(String.class);
			JsonNode quoteReqNode = objectMapper.readTree(quoteReq);
			
			String elderMemberDob = null;
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			for(JsonNode insuredMember : quoteReqNode.get("personalInfo").get("selectedFamilyMembers"))
			{
				if (elderMemberDob == null)
				{
					elderMemberDob = insuredMember.get("dob").asText();
				}
				if (formatter.parse(insuredMember.get("dob").asText()).before(formatter.parse(elderMemberDob)))
				{
					elderMemberDob = insuredMember.get("dob").asText();
				}		
			}
			((ObjectNode)quoteReqNode.get("personalInfo")).put("elderMemberDob", elderMemberDob.toString());
			exchange.getIn().setBody(quoteReqNode);
		}
		
		catch(Exception e)
		{
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthQuoteElderMemberDataProcessor : ",e);
			throw new ExecutionTerminator();
		}	
	}	
}
