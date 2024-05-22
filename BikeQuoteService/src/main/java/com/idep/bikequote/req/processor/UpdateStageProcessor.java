package com.idep.bikequote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.exception.processor.ExecutionTerminator;
import com.idep.bikequote.util.BikeQuoteConstants;

public class UpdateStageProcessor implements Processor {
	Logger log = Logger.getLogger(UpdateStageProcessor.class);
	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		try {
			JsonNode requestNode = mapper.readTree(exchange.getIn().getBody().toString());
			JsonNode configDoc = mapper.readTree(exchange.getProperty("configDoc").toString());
			String loopIndex = exchange.getProperty("CamelLoopIndex").toString();
			String stage = null;
			if (configDoc != null) {
				stage = configDoc.get(requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText()).get("quoteStages").get(loopIndex).asText();

			} else {
				log.error("error in carrierrequestconfiguration property details not present.");
			}

			if (stage.isEmpty() || stage == null || stage.equalsIgnoreCase("NA")) {
				ObjectNode createObjectNode = this.mapper.createObjectNode();
				createObjectNode.put(BikeQuoteConstants.DROOLS_CARRIERID, requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asInt());
				createObjectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES102");
				createObjectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "configuration document not found ");
				createObjectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, "");
				exchange.getIn().setBody(createObjectNode);
				throw new ExecutionTerminator();
			}
			((ObjectNode) requestNode).put("stage", stage);
			 exchange.setProperty("stage", stage);
			 if (configDoc.get(requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText()).get("webserviceType").asText().equals("REST")) {
                 exchange.getIn().setHeader("REST", "Yes");
                 exchange.getIn().setHeader("webserviceType", "REST");
                 exchange.getIn().setHeader("requestURL", BikeQuoteConstants.REST_SERVICE);
                 exchange.getIn().setBody(requestNode);
               } 
               else if (configDoc.get(requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText()).get("webserviceType").asText().equals("SOAP")) {
                 exchange.getIn().setHeader("REST", "No");
                 exchange.getIn().setHeader("webserviceType", "SOAP");
                 exchange.getIn().setHeader("requestURL", BikeQuoteConstants.SOAP_SERVICE);
                 exchange.getIn().setBody(requestNode);
               }
             exchange.setProperty(BikeQuoteConstants.REQUEST_DATA, requestNode);

			
		} catch (Exception e) {
			log.error("error in UpdateStageProcessor class." + e.getMessage());
			throw new ExecutionTerminator();
		}
	}

}