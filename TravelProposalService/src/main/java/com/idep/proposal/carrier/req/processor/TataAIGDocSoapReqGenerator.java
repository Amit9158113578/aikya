package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

public class TataAIGDocSoapReqGenerator implements Processor{
	Logger log = Logger.getLogger(TataAIGSoapReqGenerator.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	JsonNode docConfigNode = null; 
	ArrayNode logNode=null;
//HashMap<String, String> docMap=
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try {
			
			 
			
			
			
			String message = exchange.getIn().getBody().toString();
		
			CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
			//JsonNode UIReq = exchange.getProperty(TravelQuoteConstants.CARRIER_REQ_MAP_CONF,JsonNode.class);
			//log.info(" in TataAIG  TataAIGSoapReqGenerator : "+UIReq.get("carrierId") +  " plan Id "+UIReq.get("planId"));
			//docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("TravelQuoteRequest-"+UIReq.get("carrierId").intValue()+"-"+UIReq.get("planId").intValue()).content().toString());
			
			String configdoc=exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString();
		docConfigNode = this.objectMapper.readTree(configdoc);
			 log.debug("Carrier Config Document  :  "+docConfigNode);
			
			
			//docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("TravelQuoteRequest-40-1" ).content().toString());
			 
			String inputReq="";
		if(docConfigNode.has("requestValues"))
			{
			
				
				//<?xml version="1.0" encoding="UTF-8"?>
			 logNode = (ArrayNode)docConfigNode.get("requestValues").get("valuesList");
			
			for(JsonNode node : logNode){
				
				if(node.has("defaultValue")&& node.get("defaultValue").asText().equals("Y"))
				{
					inputReq+=(node.get("key").asText()+("=")+node.get("value").asText()+(docConfigNode.get("requestValues").get("appendby").asText()));
					
				}
				else if (node.has("defaultValue")&& node.get("defaultValue").asText().equals("N")) {
					 inputReq+=(node.get("key").asText()+("=")+node.get("genrateValue").asText()+(docConfigNode.get("requestValues").get("appendby").asText()));
					
				}
				else if (node.has("ReqKey")&& (node.get("ReqKey").asText().equals("getbody"))) {
					String modifiedRequest = message.replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
					//<?xml version="1.0" encoding="UTF-8"?
					 inputReq+=node.get("key").asText()+("=")+(modifiedRequest);	
			

					}
				}
			
			}
			log.info("TataAIG TataAIGSoapReqGenerator  ::::::::>>>"+message);
			  exchange.getIn().setBody(message);
		}
		
		catch(Exception e)
		{
			log.error("TataAIG Failed to load Log Config Document",e);
		}
		
		
	}
	



}
