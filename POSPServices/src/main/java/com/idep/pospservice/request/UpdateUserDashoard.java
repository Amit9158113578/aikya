package com.idep.pospservice.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;

public class UpdateUserDashoard implements Processor{
	
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	static CBService policyTrans = CBInstanceProvider.getPolicyTransInstance();
	Logger log = Logger.getLogger(UpdateUserDashoard.class.getName()); 
	static JsonDocument QueryDoc =null;
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	
	static{
		QueryDoc = pospData.getDocBYId("POSPQueryServerConfig");
	}
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			JsonNode userReq = objectMapper.readTree(exchange.getIn().getBody(String.class));
			log.info("User DashBoard Update REQUE BODY : "+userReq);
			String leadDocId = "";
			JsonNode userAppConfig = null;
			if(userReq.has("messageId")){
				leadDocId="LeadProfile-"+userReq.get("messageId").asText();
				
			}else if(userReq.has("leadProfile")){
				leadDocId=userReq.get("leadProfile").asText();
			}
			
			JsonDocument leadProfileDoc = policyTrans.getDocBYId(leadDocId);
			
			JsonNode queryCofig = null;
			if(QueryDoc!=null){
				queryCofig =objectMapper.readTree(QueryDoc.content().toString());
			}
			if(leadProfileDoc!=null){
				JsonNode leadDetails = objectMapper.readTree(leadProfileDoc.content().toString());
				log.info("Lead Profile Document Fetched : "+leadDocId+" \n  "+leadDetails);
				if(leadDetails.findValue("userName")!=null){
					log.info("UserName in leadProfile : "+leadDetails.findValue("userName").asText());
					String query = queryCofig.get("getAgentProfile").asText();
					
					JsonArray paramobj = JsonArray.create();
					paramobj.add(leadDetails.findValue("userName").asText());
					JsonNode groupConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
					log.debug("User App Config Found : "+groupConfig);
					if(groupConfig!=null){
						userAppConfig =groupConfig.get(0).get("DashBoard");
						if(userAppConfig.size()!= 0){
					
							if(leadDetails.has("leadStage")){
								String leadStge = leadDetails.get("leadStage").asText();
								log.debug("User Lead Stage from leadProfile  : "+leadStge);			
								log.debug("User App Config Lead Summary : "+userAppConfig);	
								if(leadStge.equalsIgnoreCase("PAYINIT") || leadStge.equalsIgnoreCase("PAYSUCC") || leadStge.equalsIgnoreCase("PROPOSAL") ){
									if(userAppConfig.has("leadSummary"))
									{
										ArrayNode leadArray = (ArrayNode )userAppConfig.get("leadSummary");
										for(JsonNode Summary : leadArray){
											if(Summary.has("functionId")){
												if(leadDetails.get("leadStage").asText().equalsIgnoreCase("PAYINIT")){
												if(Summary.get("functionId").asText().equalsIgnoreCase("PAIDPAYMENT")){
													log.info("Updatin PAYINIT Stage in POSPRequestQ");
													((ObjectNode)Summary).put("value",(Summary.get("value").asInt()+1));
												}
												//PAYINIT IF END 
											 }else if(leadDetails.get("leadStage").asText().equalsIgnoreCase("PROPOSAL")){
												 if(Summary.get("functionId").asText().equalsIgnoreCase("PENDINGPROPS")){
													 log.info("Updatin PENDINGPROPS Stage in POSPRequestQ");
														((ObjectNode)Summary).put("value",(Summary.get("value").asInt()+1));
													}
													//PAYSUCC IF END 
											}
										}
									 }
										((ObjectNode)groupConfig.get(0).get("DashBoard")).put("leadSummary", leadArray);
									}
								String queryForFetchCust=queryCofig.get("getSaleSummary").asText();
								queryForFetchCust=queryForFetchCust.replace("{1}",leadDetails.findValue("userName").asText());
								log.info("query : "+queryForFetchCust);
								JsonNode customerPolicyNode =objectMapper.readTree(objectMapper.writeValueAsString(policyTrans.executeQuery(queryForFetchCust)));
							  	this.log.info("Response for Fetch Sale Summary : "+customerPolicyNode);
							  	int counter=0;
							  	double totalPremiumOfAll=0;
							  	if(customerPolicyNode!=null){ 
									for(JsonNode policyList:customerPolicyNode){
							        if(policyList.has("counter"))
							         counter=policyList.get("counter").asInt();
									if(policyList.has("totalPremium"))
							          totalPremiumOfAll = policyList.get("totalPremium").asDouble();
							        }    
					              }
							  	if(userAppConfig.has("businessSummary"))
								{
									ArrayNode businessArray = (ArrayNode )userAppConfig.get("businessSummary");
									for(JsonNode Summary : businessArray){
										if(Summary.has("functionId")){
											if(Summary.get("functionId").asText().equalsIgnoreCase("NOPOLICIES")){
												((ObjectNode)Summary).put("value",counter);
											}
											if(Summary.get("functionId").asText().equalsIgnoreCase("TOTPREIUMAMT")){
												((ObjectNode)Summary).put("value",totalPremiumOfAll);
											}
									}
								 }
									((ObjectNode)groupConfig.get(0).get("DashBoard")).put("businessSummary", businessArray);
								}
							  	String docId= "POSPUserProfile-"+groupConfig.get(0).get("mobileNumber").asText();
								log.info("Updated UserProfile Document : "+groupConfig);
								JsonObject userDetails = JsonObject.fromJson(groupConfig.get(0).toString());
						        String docStatus = pospDataSync.replacePospDataDocumentBySync(docId,userDetails);
								log.info("Updated UserProfile Document updated Status : "+docStatus+" \t "+docId);
								}
							}//lead stage Condition
						}
					}else{
						log.error("Unabel tot find user Profile : "+leadDetails.findValue("userName").asText());
						
					}
					
				}
			}
		}catch(Exception e){
			log.error("Unable to process Request POSP dashboard Update : ",e);
		}
		
		
		
	}	
}
