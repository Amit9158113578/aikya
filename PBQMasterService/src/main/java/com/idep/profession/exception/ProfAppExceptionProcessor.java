package com.idep.profession.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.profession.quote.constants.ProfessionQuoteConstant;

/*
* send error message as response 
* 
* @version 1.0
* @since   25-DEC-2016
*/

public class ProfAppExceptionProcessor implements Processor {
	
	  Logger log = Logger.getLogger(ProfAppExceptionProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode responseConfigNode;
	  JsonNode errorNode=null;
	 public void process(Exchange exchange) throws JsonProcessingException {
		 
	
	    try {
	    	 if (this.service == null)
			 {
				      this.service = CBInstanceProvider.getServerConfigInstance();
				      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProfessionQuoteConstant.RESPONSE_CONFIG_DOC).content().toString());
				        
			 }
			 ObjectNode objectNode = this.objectMapper.createObjectNode();
			 String inputReq=exchange.getIn().getBody(String.class) ;
			 
			 JsonNode inputReqNode= objectMapper.readTree(inputReq);
			 log.info("Professional Journey  Exception : "+inputReqNode);
			 if(inputReqNode.has(ProfessionQuoteConstant.RES_CODE_TXT)){

				 if(inputReqNode.get(ProfessionQuoteConstant.RES_CODE_TXT).asText().equalsIgnoreCase("1010")){
				  objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, ProfessionQuoteConstant.RESECODEERROR);
				     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, ProfessionQuoteConstant.RESEMSGEERROR);
				     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, inputReqNode.get(ProfessionQuoteConstant.RES_DATA_TXT));
				 }else{
					  objectNode.put(ProfessionQuoteConstant.RES_CODE_TXT,ProfessionQuoteConstant.RESECODEFAIL);
					     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, this.responseConfigNode.get(ProfessionQuoteConstant.RESEMSGEFAIL).textValue());
					     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, this.errorNode);
				 }
			 }else if(inputReqNode.has("isError")){

				 if(inputReqNode.get("isError").asBoolean() == true  ){
				  objectNode.put(ProfessionQuoteConstant.RES_CODE_TXT, ProfessionQuoteConstant.RESECODEERROR);
				     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, ProfessionQuoteConstant.RESEMSGEERROR);
				     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, inputReqNode.get(ProfessionQuoteConstant.RES_DATA_TXT));
				 }else{
					  objectNode.put(ProfessionQuoteConstant.RES_CODE_TXT,ProfessionQuoteConstant.RESECODEFAIL);
					     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, this.responseConfigNode.get(ProfessionQuoteConstant.RESEMSGEFAIL).textValue());
					     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, this.errorNode);
				 }
			 }else{
				 objectNode.put(ProfessionQuoteConstant.RES_CODE_TXT,ProfessionQuoteConstant.RESECODEFAIL);
			     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, this.responseConfigNode.get(ProfessionQuoteConstant.RESEMSGEFAIL).textValue());
			     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, this.errorNode);
			 }
		     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
		     
	     
		}
		 catch(Exception e)
		 {
			 
			 ObjectNode objectNode = this.objectMapper.createObjectNode();
			 objectNode.put(ProfessionQuoteConstant.RES_CODE_TXT,ProfessionQuoteConstant.RESECODEFAIL);
		     objectNode.put(ProfessionQuoteConstant.RES_MSG_TXT, this.responseConfigNode.get(ProfessionQuoteConstant.RESEMSGEFAIL).textValue());
		     objectNode.put(ProfessionQuoteConstant.RES_DATA_TXT, this.errorNode);
			 
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
		 }
	 }
	 
	 
}	 
