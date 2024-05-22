package com.idep.travelquote.result.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.results.util.TravelQuoteResultConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelQuoteResultProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConf= CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(TravelQuoteResultProcessor.class.getName());
	JsonNode errNode;
	JsonNode timeOutConfig = null;

	public void process(Exchange exchange){
		JsonNode inputNode = null;
		String correlationID = null;
		try{
			try{
				JsonDocument timeoutDoc = serverConf.getDocBYId(TravelQuoteResultConstants.TIMEOUTCONF);
				if(timeoutDoc != null){
					timeOutConfig = objectMapper.readTree(timeoutDoc.content().toString());
				}else{
					log.error("time out configuration document not found : "+TravelQuoteResultConstants.TIMEOUTCONF);
				}
			}catch(Exception e){
				log.error("unable to read time out configuration document : "+TravelQuoteResultConstants.TIMEOUTCONF);
			}
			
			CamelContext camelContext = exchange.getContext();
			ConsumerTemplate consumerTemplate = camelContext.createConsumerTemplate();
			String inputData = exchange.getIn().getBody().toString();
			inputNode = objectMapper.readTree(inputData);
			String qname=inputNode.findValue(TravelQuoteResultConstants.QNAME).textValue();
			correlationID = inputNode.findValue(TravelQuoteResultConstants.CORRELATION_ID).textValue();

			String uri="activemq:queue:"+qname+"?selector=JMSCorrelationID %3D '"+correlationID+"'";
			/* service will wait until 10000 milliseconds to get the response msg from desired Q */

			long timeout = 60000;
			JsonNode timeOutNode = timeOutConfig.get(TravelQuoteResultConstants.QUEUE_WAIT_TIME);
			if(timeOutNode!= null){
				timeout = timeOutNode.asLong();
			}
			String mqbodydata =  consumerTemplate.receiveBody(uri,timeout).toString(); 
			JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
			((ObjectNode)responseNode).put(TravelQuoteResultConstants.QUOTE_ID, inputNode.get(TravelQuoteResultConstants.QUOTE_ID).textValue());
			 if (inputNode.has("encryptedQuoteId")) {
			        ((ObjectNode)responseNode).put("encryptedQuoteId", inputNode.get("encryptedQuoteId").textValue());
			      }
			((ObjectNode)responseNode).put(TravelQuoteResultConstants.CORRELATION_ID,correlationID);
			((ObjectNode)responseNode).put(TravelQuoteResultConstants.STATUS,0);
			this.log.info("FUSE : travel quote response message fetched from Q  : "+responseNode);
			exchange.getIn().setBody(responseNode);
		}catch(NullPointerException e){
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_CODE, 2000);
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_DATA, errNode);
			errorNode.put("encryptedQuoteId", inputNode.get("encryptedQuoteId").textValue());
			errorNode.put(TravelQuoteResultConstants.QUOTE_ID, inputNode.get(TravelQuoteResultConstants.QUOTE_ID).textValue());
			errorNode.put(TravelQuoteResultConstants.CORRELATION_ID,correlationID);
			errorNode.put(TravelQuoteResultConstants.STATUS, 2);
			exchange.getIn().setBody(errorNode);
		}catch(Exception e){
			log.error("************ Exception while fetching Travel quote response from Q ************ ",e);
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_CODE, 2001);
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_MSG, "Exception while fetching quote result from Q");
			errorNode.put(TravelQuoteResultConstants.QUOTE_RES_DATA, errNode);
			errorNode.put(TravelQuoteResultConstants.QUOTE_ID, inputNode.get(TravelQuoteResultConstants.QUOTE_ID).textValue());
			errorNode.put(TravelQuoteResultConstants.CORRELATION_ID,correlationID);
			errorNode.put(TravelQuoteResultConstants.STATUS, 2);
			exchange.getIn().setBody(errorNode);
		}
	}
}