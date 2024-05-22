package com.idep.profession.quote.DB;

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
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.profession.exception.ExecutionTerminator;
import com.idep.profession.quote.constants.ProfessionQuoteConstant;



public class RiskQuoteDBStore implements Processor {

	
	Logger log = Logger.getLogger(RiskQuoteDBStore.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService QuoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	SimpleDateFormat sysDateFormat =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			String riskQuoteId =null;
			String professionQuoteId=null;
			if(exchange.getProperty(ProfessionQuoteConstant.PROF_QUOTE_ID)!=null){
				professionQuoteId=  exchange.getProperty(ProfessionQuoteConstant.PROF_QUOTE_ID).toString();
			}else{
				log.error("Professional QuoteId not found in Request");
			}
			if(reqNode!=null){
				JsonNode profIdDocNode =null;
				JsonDocument professionDocument =QuoteData.getDocBYId(professionQuoteId); 
				if(professionDocument!=null){
					profIdDocNode = objectMapper.readTree(professionDocument.content().toString());
					
				}else{
					log.error("Unable to fetch ProfessionQUoteId from DB : "+professionQuoteId);
				}
				/**
				 * if profession quoteId Contain Risk QuoteId then updating same document.
				 * **/
				if(profIdDocNode.has("RISK_QUOTE_ID")){
					/**
					 * verifying risk analysis response 
					 * **/
					if(reqNode.has("algResponse")){
						if(reqNode.get("algResponse").get("riskAnalysis").size()<1){
							log.error("Risk Analysis response in riskAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
							}
						if(reqNode.get("algResponse").get("insuranceAnalysis").size()<1){
							log.error("Risk Analysis response in insuranceAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
						}
						if(reqNode.get("algResponse").get("productAnalysis").size()<1){
							log.error("Risk Analysis response in productAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
							
							}
					}
					JsonDocument riskDoc = QuoteData.getDocBYId(profIdDocNode.get("RISK_QUOTE_ID").asText());
					if(riskDoc!=null){
						riskQuoteId=	profIdDocNode.get("RISK_QUOTE_ID").asText();
						if(professionQuoteId!=null){
							((ObjectNode)reqNode).put(ProfessionQuoteConstant.PROF_QUOTE_ID, professionQuoteId);
						}
						((ObjectNode)reqNode).put("lasUpdatedDate",sysDateFormat.format(new Date()));
						String  doc_status = QuoteData.replaceDocument(riskQuoteId,JsonObject.fromJson(reqNode.toString()));
						log.info("Document DB Status : "+doc_status+" : Updated document : "+riskQuoteId);
						exchange.setProperty("RISK_QUOTE_ID",riskQuoteId);
						((ObjectNode)reqNode).put("RISK_QUOTE_ID", riskQuoteId);
					}else{
						
						((ObjectNode)reqNode).put("documentType","RiskAnalysisResponse");
						((ObjectNode)reqNode).put("creationDate",sysDateFormat.format(new Date()));
						if(professionQuoteId!=null){
							((ObjectNode)reqNode).put(ProfessionQuoteConstant.PROF_QUOTE_ID, professionQuoteId);
						}
						String doc_status =   QuoteData.createDocument(riskQuoteId,JsonObject.fromJson(reqNode.toString()));
						JsonDocument profQuoteDoc = QuoteData.getDocBYId(professionQuoteId);
						
						if(profQuoteDoc!=null){
							profQuoteDoc.content().put("RISK_QUOTE_ID", riskQuoteId);
							((ObjectNode)reqNode).put("lasUpdatedDate",sysDateFormat.format(new Date()));
							QuoteData.replaceDocument(professionQuoteId, JsonObject.fromJson(profQuoteDoc.content().toString()));
						}
						exchange.setProperty("RISK_QUOTE_ID",riskQuoteId);
						((ObjectNode)reqNode).put("RISK_QUOTE_ID", riskQuoteId);
					}	
				}else{
					riskQuoteId = DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("riskQuoteId").asText() + this.serverConfig.updateDBSequence("SEQRISKQUOTE");
					log.info("Professional RISK Quote Id : "+riskQuoteId);
					if(reqNode.has("algResponse")){
						if(reqNode.get("algResponse").get("riskAnalysis").size()<1){
							log.error("Risk Analysis response in riskAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
							}
						if(reqNode.get("algResponse").get("insuranceAnalysis").size()<1){
							log.error("Risk Analysis response in insuranceAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
						}
						if(reqNode.get("algResponse").get("productAnalysis").size()<1){
							log.error("Risk Analysis response in productAnalysis not found : "+reqNode);
							ObjectNode resNode = objectMapper.createObjectNode();
							resNode.put("isError", true);
							resNode.put("msg", "unable to calculate Risk Assessment due to technical issue....");
							exchange.getIn().setBody(resNode);
							throw new ExecutionTerminator();
							
							}
					}
					if(professionQuoteId!=null){
						((ObjectNode)reqNode).put(ProfessionQuoteConstant.PROF_QUOTE_ID, professionQuoteId);
					}
					((ObjectNode)reqNode).put("documentType","RiskAnalysisResponse");
					((ObjectNode)reqNode).put("creationDate",sysDateFormat.format(new Date()));
					String doc_status =   QuoteData.createDocument(riskQuoteId,JsonObject.fromJson(reqNode.toString()));
					
					if(doc_status!=null && doc_status.equalsIgnoreCase("doc_exist") ){
						((ObjectNode)reqNode).put("lasUpdatedDate",sysDateFormat.format(new Date()));
						 doc_status = QuoteData.replaceDocument(riskQuoteId,JsonObject.fromJson(reqNode.toString()));
						log.info("Document DB Status : "+doc_status+" : Updated document : "+riskQuoteId);
					}
					
					JsonDocument profQuoteDoc = QuoteData.getDocBYId(professionQuoteId);
					
					if(profQuoteDoc!=null){
						profQuoteDoc.content().put("RISK_QUOTE_ID", riskQuoteId);
						QuoteData.replaceDocument(professionQuoteId, JsonObject.fromJson(profQuoteDoc.content().toString()));
					}
					exchange.setProperty("RISK_QUOTE_ID",riskQuoteId);
					((ObjectNode)reqNode).put("RISK_QUOTE_ID", riskQuoteId);
				}
			}
			exchange.getIn().setBody(reqNode);
		}catch(Exception e){
			log.error("Unable to store request in DB :",e);
			throw new ExecutionTerminator();
		}
		
	}

	
	
	
}
