package com.idep.leadutility.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lead.upload.format.ImportLeadFormat;

public class LeadFormatProcessor implements Processor {

	Logger log = Logger.getLogger(LeadFormatProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	ImportLeadFormat importLeadFormat = new ImportLeadFormat();
	String message = null;

	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		try {
			importLeadFormat.jsonToCSVFormat(request);
			message = "Processing completed successfully";
		} catch (NumberFormatException e) {
			message = "Please check phone number";
		}catch(Exception e){
			message = "Processing Failed";
		}
		exchange.getIn().setBody(message);
	}

}
