package com.idep.carquote.result.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.carquote.results.util.CarQuoteResultConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
/*
* @author  Sandeep Jadhav
* @version 3.0
* @since   2-AUG-2016
*/
public class CarQuoteResultProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConf= CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(CarQuoteResultProcessor.class.getName());
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
					 JsonDocument timeoutDoc = serverConf.getDocBYId(CarQuoteResultConstants.TIMEOUTCONF);
					 if(timeoutDoc!=null)
					 {
						 timeOutConfig = objectMapper.readTree(timeoutDoc.content().toString());
					 }
					 else
					 {
						 log.error("time out configuration document not found : "+CarQuoteResultConstants.TIMEOUTCONF);
					 }
				 }
				 catch(Exception e)
				 {
					 log.error("unable to read time out configuration document : "+CarQuoteResultConstants.TIMEOUTCONF);
				 }
			 }
			
			 
			 String inputData = exchange.getIn().getBody().toString();
			 inputNode = objectMapper.readTree(inputData);
			 String qname=inputNode.get(CarQuoteResultConstants.QNAME).textValue();
			 String correlationID=inputNode.get(CarQuoteResultConstants.CORRELATION_ID).textValue();
			 
			 String uri="activemq:queue:"+qname+"?selector=JMSCorrelationID %3D '"+correlationID+"'";
			
			 log.info("activemq queue request url :"+uri);
			 /**
			  *  service will wait until specified milliseconds to get the response msg from response Q 
			  */
			 long timeout = 60000;
			 JsonNode timeOutNode = timeOutConfig.get(CarQuoteResultConstants.QUEUE_WAIT_TIME);
			 if(timeOutNode!=null)
			 {
				 timeout = timeOutNode.asLong();
			 }
			 consumerTemplate.start();
			 String mqbodydata =  consumerTemplate.receiveBody(uri,timeout).toString();
			 JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
			 ((ObjectNode)responseNode).put(CarQuoteResultConstants.QUOTE_ID, inputNode.get(CarQuoteResultConstants.QUOTE_ID).textValue());
			 if(inputNode.has(CarQuoteResultConstants.ENCRYPT_QUOTE_ID)){
				 ((ObjectNode)responseNode).put(CarQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(CarQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 }
			 ((ObjectNode)responseNode).put(CarQuoteResultConstants.CORRELATION_ID,correlationID);
			 ((ObjectNode)responseNode).put(CarQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(responseNode);
			 consumerTemplate.stop();
			 
		 }
		 catch(NullPointerException e)
		 {

			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_CODE, 2000);
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(CarQuoteResultConstants.QUOTE_ID, inputNode.get(CarQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(CarQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.CORRELATION_ID,inputNode.get(CarQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(errorNode);
			 consumerTemplate.stop();
		 }
		 catch(Exception e)
		 {
			 this.log.error("unable to fetch Car Quote results from response Q : ",e);
			 ObjectNode errorNode = objectMapper.createObjectNode();
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_CODE, 2001);
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_MSG, "unable to fetch Message from Q");
			 errorNode.put(CarQuoteResultConstants.QUOTE_RES_DATA, errNode);
			 errorNode.put(CarQuoteResultConstants.QUOTE_ID, inputNode.get(CarQuoteResultConstants.QUOTE_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.ENCRYPT_QUOTE_ID, inputNode.get(CarQuoteResultConstants.ENCRYPT_QUOTE_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.CORRELATION_ID,inputNode.get(CarQuoteResultConstants.CORRELATION_ID).textValue());
			 errorNode.put(CarQuoteResultConstants.STATUS,0);
			 exchange.getIn().setBody(errorNode);
			 consumerTemplate.stop();
		 }
		 
		 
	 }

}
