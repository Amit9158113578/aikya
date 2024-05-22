package com.idep.PBQ.updateReq;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PBQuoteUpdateReqConfiguration implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PBQuoteUpdateReqConfiguration.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	@Override
	public void process(Exchange exchange) throws Exception {
		JsonNode reqNode =null;
		ArrayNode paramList = null;
		try{
			reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			String docId = exchange.getProperty("ConfigdocId").toString();
			JsonNode QuoteConfig =  objectMapper.readTree(serverConfig.getDocBYId(docId).content().toString());
			log.info("Professional Quote request update Initiated for :  "+docId);
			JsonNode profQuote =null;
			if(QuoteConfig!=null){
				paramList = (ArrayNode)QuoteConfig.get("updateReqParamList");
			}else{
				log.error("unable to find document : "+docId);
			}
			if(reqNode.has("quoteParam")){
				
				String professionalQuoteId = null;
				if(reqNode.findValue("PROF_QUOTE_ID")!=null){
					professionalQuoteId = reqNode.findValue("PROF_QUOTE_ID").asText();
				}else if (reqNode.get("quoteParam").has("professionalQuoteId")){
					professionalQuoteId = reqNode.get("quoteParam").get("professionalQuoteId").asText();
				}
				
				JsonDocument profQuoteIdDoc =  quoteData.getDocBYId(professionalQuoteId);
				if(profQuoteIdDoc!=null){
					profQuote = objectMapper.readTree(profQuoteIdDoc.content().toString());
						for(JsonNode fieldList : paramList){
							
							if(fieldList.has("reqNode")){
								String reqnode = fieldList.get("reqNode").asText();
								String requestParm = fieldList.get("reqParam").asText();
								String pbqField =fieldList.get("pbqParam").asText();
								
								
								if(reqNode.has(fieldList.get("reqNode").asText())){
									if(fieldList.has("reqParamType")){
										
										String pbqNode = null;
										if(fieldList.has("pbqNode")){
											pbqNode=fieldList.get("pbqNode").asText();
										}
										if(profQuote.has(pbqNode) && (!profQuote.get(pbqNode).asText().equalsIgnoreCase("") ||  !profQuote.get(pbqNode).asText().equalsIgnoreCase("NA") )){
											if(reqNode.get(reqnode).has(requestParm)){
											if(fieldList.get("reqParamType").asText().equalsIgnoreCase("string")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(reqnode).get(requestParm).asText());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("int")){
												
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(reqnode).get(requestParm).asInt());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("double")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(reqnode).get(requestParm).asDouble());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("boolean")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(reqnode).get(requestParm).asBoolean());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("mappingConfig")){
												JsonNode mappingConfig =null;
												if(fieldList.has("mappingConfig")){
													mappingConfig = fieldList.get("mappingConfig");
													if(mappingConfig.has(reqNode.get(reqnode).get(requestParm).asText())){
														((ObjectNode)profQuote.get(pbqNode)).put(pbqField,mappingConfig.get(reqNode.get(reqnode).get(requestParm).asText()));	
													}
												}
												
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("findParent")){
												
												if(fieldList.has("pbqChildNode")){
													((ObjectNode)profQuote.get(pbqNode).get(fieldList.get("pbqChildNode").asText())).put(pbqField,reqNode.get(reqnode).get(requestParm));
												}
											}else{
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(reqnode).get(requestParm));
											}
										 }else{
											 log.error("unable to find requestParm field : "+requestParm); 
										 }
											
											
										}else{
											if(fieldList.get("reqParamType").asText().equalsIgnoreCase("string")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm).asText());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("int")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm).asInt());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("double")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm).asDouble());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("boolean")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm).asBoolean());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("mappingConfig")){
												JsonNode mappingConfig =null;
												if(fieldList.has("mappingConfig")){
													mappingConfig = fieldList.get("mappingConfig");
													if(mappingConfig.has(reqNode.get(reqnode).get(requestParm).asText())){
														((ObjectNode)profQuote).put(pbqField,mappingConfig.get(reqNode.get(reqnode).get(requestParm).asText()));	
													}
												}
											}else{
												((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm));
											}
										} 
									}else{
										((ObjectNode)profQuote).put(pbqField,reqNode.get(reqnode).get(requestParm));
									}
								}else{
									log.error("unable to find reqNode in request : "+fieldList.get("reqNode").asText());
								}
								
							}else{
								String requestParm = fieldList.get("reqParam").asText();
								String pbqField =fieldList.get("pbqParam").asText();
								
									if(fieldList.has("reqParamType")){
										
										String pbqNode = null;
										if(fieldList.has("pbqNode")){
											pbqNode=fieldList.get("pbqNode").asText();
										}
										if(profQuote.has(pbqNode) && (!profQuote.get(pbqNode).asText().equalsIgnoreCase("") ||  !profQuote.get(pbqNode).asText().equalsIgnoreCase("NA") )){
											if(hasFieldNode(reqNode,"",requestParm)){
											if(fieldList.get("reqParamType").asText().equalsIgnoreCase("string")){
													((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm).asText());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("int")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm).asInt());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("double")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm).asDouble());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("boolean")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm).asBoolean());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("mappingConfig")){
												JsonNode mappingConfig =null;
												if(fieldList.has("mappingConfig")){
													mappingConfig = fieldList.get("mappingConfig");
													if(mappingConfig.has(reqNode.get(requestParm).asText())){
														((ObjectNode)profQuote.get(pbqNode)).put(pbqField,mappingConfig.get(reqNode.get(requestParm).asText()));	
													}
												}
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("findParent")){
												
												if(fieldList.has("pbqChildNode")){
													((ObjectNode)profQuote.get(pbqNode).get(fieldList.get("pbqChildNode").asText())).put(pbqField,reqNode.get(requestParm));
												}
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("NA")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm));
											}
											}else{
												log.error("field not found at PBQ update request : "+requestParm);
											}
										}else{
											if(hasFieldNode(reqNode,"",requestParm)){
											if(fieldList.get("reqParamType").asText().equalsIgnoreCase("string")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm).asText());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("int")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm).asInt());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("double")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm).asDouble());
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("boolean")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm).asBoolean());
											}else if(fieldList.get("reqParamType").asText().equalsIgnoreCase("mappingConfig")){
												JsonNode mappingConfig =null;
												if(fieldList.has("mappingConfig")){
													mappingConfig = fieldList.get("mappingConfig");
													if(mappingConfig.has(reqNode.get(requestParm).asText())){
														((ObjectNode)profQuote).put(pbqField,mappingConfig.get(reqNode.get(requestParm).asText()));	
													}
												}
											}else if (fieldList.get("reqParamType").asText().equalsIgnoreCase("NA")){
												((ObjectNode)profQuote).put(pbqField,reqNode.get(requestParm));
											}else{
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm));
											}
											}else{
												log.error("field not found at PBQ update request : "+requestParm);
											}
										} 
									}else{
										String pbqNode = null;
										if(fieldList.has("pbqNode")){
											pbqNode=fieldList.get("pbqNode").asText();
											if(fieldList.has("reqNode")){
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(fieldList.get("reqNode").asText()).get(requestParm));	
											}else{
												((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm));
											}
										}else{
											((ObjectNode)profQuote.get(pbqNode)).put(pbqField,reqNode.get(requestParm));
										}
										
												
										
										
									}
							}
							
						}
						
						
						log.info("upated CarQuote inot ProfessinalQuoteID : : "+profQuote);
						
						if(profQuote!=null){
							
							if(profQuote.has("lobQuoteId")){
								
								boolean updated = false;
								ArrayNode lobQuoteId = (ArrayNode)profQuote.get("lobQuoteId");
								
								for(JsonNode quoteIds : lobQuoteId){
									
									if(quoteIds.has("businessLineId")){
										
										String bussinessId = null;
										if(reqNode.get("quoteParam").has("quoteType")){
											bussinessId = reqNode.get("quoteParam").get("quoteType").asText();
										}
									if(quoteIds.get("businessLineId").asText().equalsIgnoreCase(bussinessId)){
										((ObjectNode)quoteIds).put("QUOTE_ID",reqNode.get("QUOTE_ID").asText());
										updated=true;
									}	
										
									}
								}
								if(!updated){
									ObjectNode quoteIds = objectMapper.createObjectNode();
									((ObjectNode)quoteIds).put("businessLineId",reqNode.get("quoteParam").get("quoteType").asInt());
									((ObjectNode)quoteIds).put("QUOTE_ID",reqNode.get("QUOTE_ID").asText());
									lobQuoteId.add(quoteIds);
								}
								
								
								((ObjectNode)profQuote).put("lobQuoteId", lobQuoteId);
							}
						String doc_status = 	quoteData.replaceAsyncDocument(professionalQuoteId,JsonObject.fromJson(profQuote.toString()));
							
							log.info("Professional Quote request document updated : "+professionalQuoteId+" doc_status : "+doc_status );
						}
						
				}
				if(profQuote!=null && profQuote.has("RISK_QUOTE_ID")){
					log.info("sending request for Recaluate Risk assisment : "+profQuote);
					CamelContext camelContext = exchange.getContext();
					ProducerTemplate template = camelContext.createProducerTemplate();
					String uri = "activemq:queue:RiskUpdateRequestQ";
					exchange.getIn().setBody(profQuote.toString());
					exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
					template.send(uri, exchange);
				}
			}
			
			
			
			
			
			
			exchange.getIn().setBody(reqNode);
			
		}catch(Exception e){
			log.error("unable to convert CarQuoteRequest to PBQuote reques : "+reqNode);
			log.error("unable to convert CarQuoteRequest to PBQuote reques : ",e);
		}
	}
	
	public boolean hasReqNode(JsonNode request,String reqNode){
		return  request.has(reqNode);
	}
	
	public boolean hasFieldNode(JsonNode request,String reqNode,String reqField){
		if(reqNode!=null){
			return  request.get(reqNode).has(reqField);	
		}else if(reqNode==null || reqNode.equalsIgnoreCase("")){
			return  request.has(reqField);	
		}
		return false;
	}

}
