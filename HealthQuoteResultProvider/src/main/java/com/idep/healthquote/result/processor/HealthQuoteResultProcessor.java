package com.idep.healthquote.result.processor;

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
import com.idep.healthquote.results.util.HealthQuoteResultConstants;

public class HealthQuoteResultProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConf= CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(HealthQuoteResultProcessor.class.getName());
	JsonNode errNode;
	JsonNode timeOutConfig = null;
	
	 public void process(Exchange exchange) {
		 
		 JsonNode inputNode = null;
		 
		 try
		 {
			 if(timeOutConfig==null)
			 {
				 try
				 {
					 JsonDocument timeoutDoc = serverConf.getDocBYId(HealthQuoteResultConstants.TIMEOUTCONF);
					 if(timeoutDoc!=null)
					 {
						 timeOutConfig = objectMapper.readTree(timeoutDoc.content().toString());
					 }
					 else
					 {
						 log.error("time out configuration document not found : "+HealthQuoteResultConstants.TIMEOUTCONF);
					 }
				 }
				 catch(Exception e)
				 {
					 log.error("unable to read time out configuration document : "+HealthQuoteResultConstants.TIMEOUTCONF);
				 }
			 } 
			 CamelContext camelContext = exchange.getContext();
			 ConsumerTemplate consumerTemplate = camelContext.createConsumerTemplate();
			 String inputData = exchange.getIn().getBody().toString();
			 inputNode = objectMapper.readTree(inputData);
			 String qname=inputNode.get(HealthQuoteResultConstants.QNAME).textValue();
			 String correlationID=inputNode.get(HealthQuoteResultConstants.CORRELATION_ID).textValue();
			 String uri="activemq:queue:"+qname+"?selector=JMSCorrelationID %3D '"+correlationID+"'";
			 /**
			  *  service will wait until 60000 milliseconds to get the response msg from response Q 
			  */
			 long timeout = 60000;
			 JsonNode timeOutNode = timeOutConfig.get(HealthQuoteResultConstants.QUEUE_WAIT_TIME);
			 if(timeOutNode!=null)
			 {
				 timeout = timeOutNode.asLong();
			 }
			 String mqbodydata =  consumerTemplate.receiveBody(uri,timeout).toString();
			 JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
			 ((ObjectNode)responseNode).put(HealthQuoteResultConstants.QUOTE_ID, inputNode.get(HealthQuoteResultConstants.QUOTE_ID).textValue());
			 if(inputNode.has(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID)){
				 ((ObjectNode)responseNode).put(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 }
			 ((ObjectNode)responseNode).put(HealthQuoteResultConstants.CORRELATION_ID,correlationID);
			 ((ObjectNode)responseNode).put(HealthQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(responseNode);
			 
		 }
		 catch(NullPointerException e)
		 {
			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_CODE, 2000);
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(HealthQuoteResultConstants.QUOTE_ID, inputNode.get(HealthQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.CORRELATION_ID,inputNode.get(HealthQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(errorNode);
		 }
		 catch(Exception e)
		 {
			 log.error("************ Exception while fetching Health quote response from Q ************ ",e);
			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_CODE, 2001);
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_MSG, "unable to fetch quote response from Q");
			 errorNode.put(HealthQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(HealthQuoteResultConstants.QUOTE_ID, inputNode.get(HealthQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(HealthQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.CORRELATION_ID,inputNode.get(HealthQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(HealthQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(errorNode);
		 }
		 
		 
	 }

}
