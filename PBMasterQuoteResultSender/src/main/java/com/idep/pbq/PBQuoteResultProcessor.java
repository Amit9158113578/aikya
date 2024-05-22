/**
 * 
 */
package com.idep.pbq;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import  com.idep.pbq.util.PBQuoteResultConstants;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/**
 * @author pravin.jakhi
 *
 */
public class PBQuoteResultProcessor implements Processor {

	 ObjectMapper objectMapper = new ObjectMapper();
	  CBService serverConf = CBInstanceProvider.getServerConfigInstance();
	  Logger log = Logger.getLogger(PBQuoteResultProcessor.class.getName());
	  JsonNode errNode;
	  JsonNode timeOutConfig = null;
	  
	  public void process(Exchange exchange)
	  {
	    JsonNode inputNode = null;
	    try
	    {
	      if (this.timeOutConfig == null) {
	        try
	        {
	          JsonDocument timeoutDoc = this.serverConf.getDocBYId(PBQuoteResultConstants.TIMEOUTCONF);
	          if (timeoutDoc != null) {
	            this.timeOutConfig = this.objectMapper.readTree(((JsonObject)timeoutDoc.content()).toString());
	          } else {
	            this.log.error("time out configuration document not found : TimeOutConfig");
	          }
	        }
	        catch (Exception e)
	        {
	          this.log.error("unable to read time out configuration document : TimeOutConfig");
	        }
	      }
	      CamelContext camelContext = exchange.getContext();
	      ConsumerTemplate consumerTemplate = camelContext.createConsumerTemplate();
	      String inputData = exchange.getIn().getBody().toString();
	      inputNode = this.objectMapper.readTree(inputData);
	      String qname = inputNode.get(PBQuoteResultConstants.QNAME).textValue();
	      String correlationID = inputNode.get(PBQuoteResultConstants.CORRELATION_ID).textValue();
	      String uri = "activemq:queue:" + qname + "?selector=JMSCorrelationID %3D '" + correlationID + "'";
	      
	      long timeout = 60000L;
	      JsonNode timeOutNode=null;
	      if(timeOutConfig.has(PBQuoteResultConstants.PBQUEUE_WAIT_TIME)){
	    	 timeOutNode = this.timeOutConfig.get(PBQuoteResultConstants.PBQUEUE_WAIT_TIME);  
	      }
	       
	      if (timeOutNode != null) {
	        timeout = timeOutNode.asLong();
	      }
	      String mqbodydata = consumerTemplate.receiveBody(uri, timeout).toString();
	      JsonNode responseNode = this.objectMapper.readTree(mqbodydata);
	    /*  ((ObjectNode)responseNode).put("QUOTE_ID", inputNode.get("QUOTE_ID").textValue());*/
	      ((ObjectNode)responseNode).put(PBQuoteResultConstants.CORRELATION_ID, correlationID);
	      ((ObjectNode)responseNode).put(PBQuoteResultConstants.STATUS, 0);
	      exchange.getIn().setBody(responseNode);
	    }
	    catch (NullPointerException e)
	    {
	      ObjectNode errorNode = this.objectMapper.createObjectNode();
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_CODE, 2000);
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_MSG, "Message not found in Q");
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_DATA, this.errNode);
	      errorNode.put("queueName", inputNode.get("qname").textValue());
	      errorNode.put(PBQuoteResultConstants.CORRELATION_ID, inputNode.get("messageId").textValue());
	      errorNode.put(PBQuoteResultConstants.STATUS, 0);
	      exchange.getIn().setBody(errorNode);
	    }
	    catch (Exception e)
	    {
	      log.error("************ Exception while fetching Professional base journey quote response from Q ************ ", e);
	      ObjectNode errorNode = objectMapper.createObjectNode();
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_CODE, 2000);
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_MSG, "unable to fetch quote response from Q");
	      errorNode.put(PBQuoteResultConstants.QUOTE_RES_DATA, this.errNode);
	      errorNode.put("queueName", inputNode.get("qname").textValue());
	      errorNode.put(PBQuoteResultConstants.CORRELATION_ID, inputNode.get("messageId").textValue());
	      errorNode.put(PBQuoteResultConstants.STATUS, 0);
	      exchange.getIn().setBody(errorNode);
	    }
	  }
	}
