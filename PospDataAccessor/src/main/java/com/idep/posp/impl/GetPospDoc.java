package com.idep.posp.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.service.POSPDataAccessorConstant;


public class GetPospDoc implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetPospDoc.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
			
			if(request.has("documentType")){
				JsonNode doc = null;
				JsonDocument documentData  = PospData.getDocBYId(request.get("documentType").asText());
				if(documentData!=null){
					doc=objectMapper.readTree(documentData.content().toString());
					((ObjectNode)doc).put(POSPDataAccessorConstant.RES_CODE,1000);
				}else{
					doc =  objectMapper.createObjectNode();
					((ObjectNode)doc).put(POSPDataAccessorConstant.RES_CODE,1001);
					((ObjectNode)doc).put(POSPDataAccessorConstant.RES_MSG ,"failure");
				}
				exchange.getIn().setBody(doc);
			}
		}catch(Exception e){
			log.error("Unable to get document : ",e);
		}
	}

}
