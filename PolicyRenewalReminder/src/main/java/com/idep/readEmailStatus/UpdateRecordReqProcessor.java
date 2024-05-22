package com.idep.readEmailStatus;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateRecordReqProcessor implements Processor{
    ObjectMapper objectMapper = new ObjectMapper();

	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(UpdateRecordReqProcessor.class.getName());
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("UpdateRecordReqProcessor Req :"+reqNode);
		
		if( (!reqNode.has("messageId") && (!reqNode.has("mailId")) && (!reqNode.has("smsId")) )){
			log.info("Didn't got id to update read status ");
			throw new Exception();
		}
		if(reqNode.has("isRenewal") && reqNode.get("isRenewal").asBoolean()){
			exchange.getIn().setHeader("updateLead", "Yes");
		}
		exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
	}
}
