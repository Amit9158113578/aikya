package com.idep.createPolicyDoc;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class CreatePolicyRequest implements Processor {
	static Logger log = Logger.getLogger(CreatePolicyRequest.class);
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	public void process(Exchange exchange)throws Exception{
		try{
		   JsonNode proposalDocNode = objectMapper.readTree(exchange.getProperty("proposalRequest").toString());
			JsonNode serviceURLConfig = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CreatePolicyURL").content()).toString());
		    String req = (String)exchange.getIn().getBody(String.class);
		    JsonNode reqNode = objectMapper.readTree(req);
			JsonObject ReqResult = JsonObject.create();
			ReqResult = JsonObject.fromJson(reqNode.toString());
			 String doc_name=null;
				if(proposalDocNode.has("paymentResponse") && proposalDocNode.get("paymentResponse").get("apPreferId").textValue()!=null)
				{
					 doc_name=proposalDocNode.get("paymentResponse").get("apPreferId").textValue();
				}
				else
				{
					 doc_name = CreatePolicyDoc.createPolicyDoc(ReqResult);
				}
			JsonNode methodOutput = CreatePolicyServiceCall.CreatePolicy(proposalDocNode,doc_name);
			((ObjectNode) methodOutput).remove("transactionStatusCode");
			((ObjectNode) methodOutput).remove("proposalId");
			 exchange.getIn().setHeader("policyStatus", "success");
			 exchange.getIn().setHeader("CamelHttpMethod", "POST");
			 exchange.getIn().setHeader("Content-Type", "application/json");
			 exchange.getIn().setHeader("webserviceType", "REST");
			 exchange.getIn().setHeader("CamelAcceptContentType", "application/json");
			 exchange.getIn().setHeader("requestURL", serviceURLConfig.get(methodOutput.get("carrierId").asText()+"-"+methodOutput.get("productId").asText()).asText());
			 exchange.getIn().setBody(objectMapper.writeValueAsString(methodOutput));
         }catch(Exception e)
		 { 
        	 log.error("error in CreatePolicyRequest class :"+e);
         } 
		}
	}
