package com.idep.posp.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.service.POSPDataAccessorConstant;

public class updatePospDoc implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(updatePospDoc.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	SimpleDateFormat datetimeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
				JsonNode res = objectMapper.createObjectNode();
			
			if(request.has("documentCategory")){
				
				JsonDocument docData = PospData.getDocBYId(request.get("documentCategory").asText());
				if(docData==null){
					((ObjectNode)res).put(POSPDataAccessorConstant.RES_CODE,1001);
					((ObjectNode)res).put(POSPDataAccessorConstant.RES_MSG ,"failure");
				}else{
					
					JsonObject doc =JsonObject.fromJson(docData.content().toString());
					
					Date sysDate = new Date();
					((ObjectNode)request).put("lastUpdated", datetimeformat.format(sysDate));
					((ObjectNode)request).put("documentType", doc.getString("documentType"));
					if(request.has("adminId")){
					((ObjectNode)request).put("updatedBy",request.get("adminId").asText());
					}
					String doc_status = PospData.replaceDocument(request.get("documentCategory").asText(), JsonObject.fromJson(objectMapper.writeValueAsString(request)));
					log.info("Dcoument Updated : "+doc_status+" "+request.get("documentCategory").asText());
					((ObjectNode)res).put(POSPDataAccessorConstant.RES_CODE,1000);
				}
			}else{
				((ObjectNode)res).put(POSPDataAccessorConstant.RES_CODE,1001);
				((ObjectNode)res).put(POSPDataAccessorConstant.RES_MSG ,"failure");
			}
			exchange.getIn().setBody(res);
			
			
			
			
		}catch(Exception e){
			
		}
	}

}
