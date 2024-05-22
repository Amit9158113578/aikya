/**
 * 
 */
package com.idep.professions.exception;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.professions.constant.ProfessionalConstant;


/**
 * @author pravin.jakhi
 *
 */
public class ProposalExceptionProcessor implements Processor {

	 Logger log = Logger.getLogger(ProposalExceptionProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode responseConfigNode;
	  JsonNode errorNode=null;
	 public void process(Exchange exchange) throws JsonProcessingException {
		 
	
	    try {
			 
		 if (this.service == null)
		 {
			      this.service = CBInstanceProvider.getServerConfigInstance();
			      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProfessionalConstant.RESPONSE_CONFIG_DOC).content().toString());
			        
		 }
		 ObjectNode objectNode = this.objectMapper.createObjectNode();
		 String inputReq=exchange.getIn().getBody(String.class) ;
		 
		 JsonNode inputReqNode= objectMapper.readTree(inputReq);

			  objectNode.put(ProfessionalConstant.RES_CODE, this.responseConfigNode.get(ProfessionalConstant.ERROR_CONFIG_CODE).intValue());
			     objectNode.put(ProfessionalConstant.RES_MSG, this.responseConfigNode.get(ProfessionalConstant.ERROR_CONFIG_MSG).textValue());
			     objectNode.put(ProfessionalConstant.RES_DATA, this.errorNode);
		
	     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
	     
		}
		 catch(Exception e)
		 {
			 
			 ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(ProfessionalConstant.RES_CODE, this.responseConfigNode.get(ProfessionalConstant.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(ProfessionalConstant.RES_MSG, this.responseConfigNode.get(ProfessionalConstant.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(ProfessionalConstant.RES_DATA, this.errorNode);
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
			 log.error("ProposalExceptionProcessor : ",e);
		 }
	 }
	 
	 
}	 
