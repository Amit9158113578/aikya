package com.idep.policydoc.retry.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.user.profile.impl.UserProfileServices;
public class GetPolicyDocDetailsProcessor implements Processor {

	Logger log = Logger.getLogger(GetPolicyDocDetailsProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	UserProfileServices profileServices = new UserProfileServices();
	CBService serverConfig =CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			
			JsonNode inputReq = objectMapper.readTree(exchange.getIn().getBody(String.class));
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String policyNo =null;
			String proposalId =null;
			String carrierId=null;
			String carrierReqQName=null;
			
			if(inputReq.has("proposalId")){
				proposalId=inputReq.findValue("proposalId").asText();
			}
			if(inputReq.has("policyNo")){
				policyNo=inputReq.findValue("policyNo").asText();
			}
			
			if(inputReq.has("policyNumber")){
				policyNo=inputReq.findValue("policyNumber").asText();
			}
			if(inputReq.has("carrierId")){
				carrierId=inputReq.findValue("carrierId").asText();
			}
			JsonNode proposalNode=null;
			
			JsonDocument proposalDoc= policyTransaction.getDocBYId(proposalId);
			
				if(proposalDoc!=null){
					/*exchange.setProperty("userPolicyProfileData", proposalDoc);*/
					proposalNode= objectMapper.readTree( proposalDoc.content().toString());
					
					String mobileNo =proposalNode.findValue("mobile").asText();
					
					String businessLineId = proposalNode.findValue("businessLineId").asText();
					JsonDocument carrierPolicyQList = serverConfig.getDocBYId("PolicyDocumentRequestQList");
				
					if(carrierPolicyQList!=null)
					{
						JsonNode policyDocumentRequestQList = objectMapper.readTree(carrierPolicyQList.content().toString());
						String queueKey=String.valueOf(carrierId+"-"+businessLineId);
						log.info("carrier queue key :"+queueKey);
						
					    carrierReqQName=policyDocumentRequestQList.findValue(queueKey).asText();
					}
					else
					{
						exchange.getIn().setHeader("requestFlage", "false");
						log.error("Fetch Policy Doc DOwnload  : Unable to read PolicyDocumentRequestQList document : "+carrierPolicyQList);
					    new Exception();
					}
					
				   JsonNode userProfileDetails = profileServices.getUserProfileByMobile(mobileNo);
					
					if(userProfileDetails!=null)
					{
						log.info("userProfileDetails : "+userProfileDetails);
						JsonNode policyDetails  =  userProfileDetails.get("policyDetails");
						JsonNode userDetails = userProfileDetails.get("userProfile");
						if(policyDetails!=null)
						{
						
						try{
							
								for(JsonNode policy: policyDetails){
									ObjectNode queueNode =objectMapper.createObjectNode();
									if(proposalId.equals(policy.get("proposalId").asText()))
									{
										log.info("PolicyFound : "+policy.get("policyNo").asText());
										((ObjectNode)queueNode).put("pKey",policy.get("secretKey").asText());
										((ObjectNode)queueNode).put("uKey",userDetails.get("secretKey").asText() );
										((ObjectNode)queueNode).put("proposalId",proposalId );
										((ObjectNode)queueNode).put("policyNo",policy.get("policyNo").asText());
										//((ObjectNode)queueNode).put("carrierId",carrierId);
										//((ObjectNode)queueNode).put("businessLineId",businessLineId);
										if(carrierReqQName!=null)
										{
										  exchange.setPattern(ExchangePattern.InOnly);
										  log.info("Final proposal RES  Node : "+queueNode);
										  exchange.getIn().setHeader("requestFlage", "true");
										  exchange.getIn().setBody(objectMapper.writeValueAsString(queueNode));
										  template.send(carrierReqQName, exchange);

										}
										else
										{
											exchange.getIn().setHeader("requestFlage", "false");
											log.error("Fetch Policy Doc DOwnload  :Unable to read carrier Q list name  : "+carrierReqQName);
										    new Exception();
										}
									}
									else
									{
										exchange.getIn().setHeader("requestFlage", "false");
										log.error("Fetch Policy Doc DOwnload  :ProposalID does not match please check  : "+proposalId);
									    new Exception();
									}
								}
							}catch(Exception e)
							{
								exchange.getIn().setHeader("requestFlage", "false");
								log.error("Error at policy under policy Details : "+policyDetails,e);
								  new  Exception();
							}
						}
						else
						{
							exchange.getIn().setHeader("requestFlage", "false");
							log.error("Fetch Policy Doc DOwnload  : Unable to read User Profile  proposalDetails : "+policyDetails);
						    new Exception();
						}
					}
					else
					{
						exchange.getIn().setHeader("requestFlage", "false");
						log.error("Fetch Policy Doc DOwnload  : Unable to read User Profile Details : "+userProfileDetails);
						  new  Exception();
					}
					}
				
			else
			{
				exchange.getIn().setHeader("requestFlage", "false");
				log.error("Fetch Policy Doc DOwnload  : Unable to read proposal document  :"+proposalId+"Document :"+proposalDoc);
				  new  Exception();
			}
		}catch(Exception e){
			exchange.getIn().setHeader("requestFlage", "false");
			log.error("Error at policy Fretch Details : ",e);
			  new  Exception();
		}
	}

	
}
