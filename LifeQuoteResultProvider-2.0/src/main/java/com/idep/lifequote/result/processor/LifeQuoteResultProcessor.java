package com.idep.lifequote.result.processor;

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
import com.idep.lifequote.results.util.LifeQuoteResultConstants;

public class LifeQuoteResultProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConf= CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(LifeQuoteResultProcessor.class.getName());
	JsonNode errNode;
	JsonNode timeOutConfig = null;

	public void process(Exchange exchange){
		JsonNode inputNode = null;
		String correlationID = null;
		try{
			{
				 try
				 {
					 JsonDocument timeoutDoc = serverConf.getDocBYId(LifeQuoteResultConstants.TIMEOUTCONF);
					 if(timeoutDoc!=null)
					 {
						 timeOutConfig = objectMapper.readTree(timeoutDoc.content().toString());
					 }
					 else
					 {
						 log.error("time out configuration document not found : "+LifeQuoteResultConstants.TIMEOUTCONF);
					 }
				 }
				 catch(Exception e)
				 {
					 log.error("unable to read time out configuration document : "+LifeQuoteResultConstants.TIMEOUTCONF);
				 }
			 }
			CamelContext camelContext = exchange.getContext();
			ConsumerTemplate consumerTemplate = camelContext.createConsumerTemplate();
			String inputData = exchange.getIn().getBody().toString();
			inputNode = objectMapper.readTree(inputData);
			String qname=inputNode.findValue(LifeQuoteResultConstants.QNAME).textValue();
			correlationID = inputNode.findValue(LifeQuoteResultConstants.CORRELATION_ID).textValue();

			String uri="activemq:queue:"+qname+"?selector=JMSCorrelationID %3D '"+correlationID+"'";
			/* service will wait until 10000 milliseconds to get the response msg from desired Q */
			
			 long timeout = 60000;
			 JsonNode timeOutNode = timeOutConfig.get(LifeQuoteResultConstants.QUEUE_WAIT_TIME);
			 if(timeOutNode!=null)
			 {
				 timeout = timeOutNode.asLong();
			 }
			 String mqbodydata =  consumerTemplate.receiveBody(uri,timeout).toString(); 
			JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
			((ObjectNode)responseNode).put(LifeQuoteResultConstants.QUOTE_ID, inputNode.get(LifeQuoteResultConstants.QUOTE_ID).textValue());
			if(inputNode.has(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID)){
				 ((ObjectNode)responseNode).put(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 }
			((ObjectNode)responseNode).put(LifeQuoteResultConstants.CORRELATION_ID,correlationID);
			((ObjectNode)responseNode).put(LifeQuoteResultConstants.STATUS,0);
			this.log.info("FUSE : life quote response message fetched from Q  : "+responseNode);
			exchange.getIn().setBody(responseNode);
		}catch(NullPointerException e){
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_CODE, 2000);
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_DATA, errNode);
			errorNode.put(LifeQuoteResultConstants.QUOTE_ID, inputNode.get(LifeQuoteResultConstants.QUOTE_ID).textValue());
			errorNode.put(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			errorNode.put(LifeQuoteResultConstants.CORRELATION_ID,correlationID);
			errorNode.put(LifeQuoteResultConstants.STATUS, 2);
			exchange.getIn().setBody(errorNode);
		}catch(Exception e){
			log.error("************ Exception while fetching Life quote response from Q ************ ",e);
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_CODE, 2001);
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_MSG, "Exception while fetching quote result from Q");
			errorNode.put(LifeQuoteResultConstants.QUOTE_RES_DATA, errNode);
			errorNode.put(LifeQuoteResultConstants.QUOTE_ID, inputNode.get(LifeQuoteResultConstants.QUOTE_ID).textValue());
			errorNode.put(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(LifeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			errorNode.put(LifeQuoteResultConstants.CORRELATION_ID,correlationID);
			errorNode.put(LifeQuoteResultConstants.STATUS, 2);
			exchange.getIn().setBody(errorNode);
		}
	}
}