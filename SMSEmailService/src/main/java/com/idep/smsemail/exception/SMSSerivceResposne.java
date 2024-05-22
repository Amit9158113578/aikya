package com.idep.smsemail.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idep.smsemail.util.SMSConstants;


public class SMSSerivceResposne implements Processor {
	 Logger log = Logger.getLogger(SMSSerivceResposne.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
		ObjectNode objectNode = this.objectMapper.createObjectNode();
		try{
			
			
			
			 String inputReq=exchange.getIn().getBody(String.class) ;
			 
			 objectNode.put(SMSConstants.RESPONSECODE, 1002);
		     objectNode.put(SMSConstants.RESPONSE_MSG, "failure");
		     objectNode.put(SMSConstants.RESPONSEDATA, "user reached to max OTP limit, Please try after one hour");
		     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
			
			
			
			
		}catch(Exception e){
			log.error("Exception occure at the SMS respoosne sednding",e);
			 objectNode.put(SMSConstants.RESPONSECODE, 1002);
		     objectNode.put(SMSConstants.RESPONSE_MSG, "failure");
		     objectNode.put(SMSConstants.RESPONSEDATA, "");
		     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
		}
	}

}
