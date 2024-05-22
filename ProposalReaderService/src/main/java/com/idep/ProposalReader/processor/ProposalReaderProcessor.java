package com.idep.ProposalReader.processor;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class ProposalReaderProcessor implements Processor {

	Logger log = Logger.getLogger(ProposalReaderProcessor.class);
	ObjectMapper objectMapper =new ObjectMapper();
	
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	JsonNode requestNode = null;
	List<JsonObject> quoteDocList=null;
	List<JsonObject> ProposalDocList=null;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			requestNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode queryConfig =objectMapper.readTree(serverConfig.getDocBYId("ProposalReaderConfig").content().toString());
			JsonArray paramobj = JsonArray.create();
			if(requestNode.has("messageId") && requestNode.has("businessLineId")){
				
				paramobj.add(requestNode.get("messageId").asText());
				log.debug("Message Lead Id : "+requestNode.get("messageId").asText()+" Quote Query : "+queryConfig.get("quoteQuery").get(requestNode.get("businessLineId").asText()).asText());
				log.debug("Quote Query PARAM : "+paramobj);
				quoteDocList= policyTransaction.executeConfigParamArrQuery(queryConfig.get("quoteQuery").get(requestNode.get("businessLineId").asText()).asText(),paramobj);
				log.info("Quote ID Found for : "+quoteDocList.toString());
				paramobj = JsonArray.create();
				paramobj.add(requestNode.get("messageId").asText());
				log.debug("Proposal Query : "+queryConfig.get("proposalQuery").get(requestNode.get("businessLineId").asText()).asText());
				log.debug("Proposal Query PARAM : "+paramobj);
				ProposalDocList= policyTransaction.executeConfigParamArrQuery(queryConfig.get("proposalQuery").get(requestNode.get("businessLineId").asText()).asText(),paramobj);
				log.info("Latest Proposal ID : "+ProposalDocList.toString());
			}
			
			
			if(quoteDocList.size() > 0 ){
				log.info(" Quote ID Found : "+quoteDocList.get(0).getString("id").toString());
				JsonNode quoteDoc = objectMapper.readTree(quoteData.getDocBYId(quoteDocList.get(0).getString("id").toString()).content().toString());
			((ObjectNode)requestNode).put("quoteDetails", quoteDoc);
			((ObjectNode)requestNode).put("QUOTE_ID",quoteDocList.get(0).getString("id").toString());
			}else{
				log.error("Quote Id not found againt this LeadMesgId : "+requestNode.get("messageId").asText());
			}
			if(ProposalDocList.size()>0){
				log.info("Proposal ID found : "+ProposalDocList.get(0).getString("id").toString());
				JsonNode proposalDoc = objectMapper.readTree(policyTransaction.getDocBYId(ProposalDocList.get(0).getString("id").toString()).content().toString());
				((ObjectNode)requestNode).put("ProposalId",ProposalDocList.get(0).getString("id").toString());
				((ObjectNode)requestNode).put("ProposalDetail", proposalDoc );	
			}else{
				log.error("proposal document not found againt this LeadMessageId : "+requestNode.get("messageId").asText());
			}
			exchange.getIn().setBody(objectMapper.writeValueAsString(requestNode));
		}catch(Exception e){
			log.error("unable to read Quote and Proposal : "+requestNode);
			log.error("unable to read  proposal Details :",e);
		}
	}
}
