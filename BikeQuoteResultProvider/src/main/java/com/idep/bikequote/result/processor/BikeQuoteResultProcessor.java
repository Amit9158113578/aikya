package com.idep.bikequote.result.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.results.util.BikeQuoteResultConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class BikeQuoteResultProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConf= CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(BikeQuoteResultProcessor.class.getName());
	JsonNode errNode;
	JsonNode timeOutConfig = null;
	
	 public void process(Exchange exchange) throws Exception {
		 CamelContext camelContext = exchange.getContext();
		 ConsumerTemplate consumerTemplate = camelContext.createConsumerTemplate();
		 JsonNode inputNode = null;
		 
		 try
		 {
			 if(timeOutConfig==null)
			 {
				 try
				 {
					 JsonDocument timeoutDoc = serverConf.getDocBYId(BikeQuoteResultConstants.TIMEOUTCONF);
					 if(timeoutDoc!=null)
					 {
						 timeOutConfig = objectMapper.readTree(timeoutDoc.content().toString());
					 }
					 else
					 {
						 log.error("time out configuration document not found : "+BikeQuoteResultConstants.TIMEOUTCONF);
					 }
				 }
				 catch(Exception e)
				 {
					 log.error("unable to read time out configuration document : "+BikeQuoteResultConstants.TIMEOUTCONF);
				 }
			 }
			 
			
			 String inputData = exchange.getIn().getBody().toString();
			 inputNode = objectMapper.readTree(inputData);
			 String qname=inputNode.get(BikeQuoteResultConstants.QNAME).textValue();
			 log.info("qname :"+qname);
			 String correlationID=inputNode.get(BikeQuoteResultConstants.CORRELATION_ID).textValue();
			 String uri="activemq:queue:"+qname+"?selector=JMSCorrelationID %3D '"+correlationID+"'";
			 log.info("uri :"+uri);
			 /**
			  *  service will wait until 60000 milliseconds to get the response msg from response Q 
			  */
			 long timeout = 60000;
			 JsonNode timeOutNode = timeOutConfig.get(BikeQuoteResultConstants.QUEUE_WAIT_TIME);
			 if(timeOutNode!=null)
			 {
				 timeout = timeOutNode.asLong();
			 }
			 consumerTemplate.start();
			 String mqbodydata =  consumerTemplate.receiveBody(uri,timeout).toString();
			 log.info("mqbodydata :"+mqbodydata);
			 JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
			 ((ObjectNode)responseNode).put(BikeQuoteResultConstants.QUOTE_ID, inputNode.get(BikeQuoteResultConstants.QUOTE_ID).textValue());
			 if(inputNode.has(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID)){
				 ((ObjectNode)responseNode).put(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue()); 
			 }
			 ((ObjectNode)responseNode).put(BikeQuoteResultConstants.CORRELATION_ID,correlationID);
			 ((ObjectNode)responseNode).put(BikeQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(responseNode);
			 consumerTemplate.stop();
			 
		 }
		 catch(NullPointerException e)
		 {

			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_CODE, 2000);
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(BikeQuoteResultConstants.CORRELATION_ID,inputNode.get(BikeQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(BikeQuoteResultConstants.STATUS,0);
			 errorNode.put(BikeQuoteResultConstants.QUOTE_ID, inputNode.get(BikeQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 exchange.getIn().setBody(errorNode);
			 consumerTemplate.stop();
		 }
		 catch(Exception e)
		 {
			 this.log.error("unable to fetch Bike Quote results from response Q : ",e);
			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_CODE, 2001);
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_MSG, "unable to fetch Message from Q");
			 errorNode.put(BikeQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(BikeQuoteResultConstants.CORRELATION_ID,inputNode.get(BikeQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(BikeQuoteResultConstants.STATUS,0);
			 errorNode.put(BikeQuoteResultConstants.QUOTE_ID, inputNode.get(BikeQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(BikeQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 exchange.getIn().setBody(errorNode);
			 consumerTemplate.stop();
		 }
		 
		 
	 }

}
