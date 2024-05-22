package com.idep.createPolicyDoc;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.summaryUtils.DBConnection;

public class PolicyRequest implements Processor{

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(PolicyRequest.class);
	static CBService proposalService = CBInstanceProvider.getPolicyTransInstance();
	static List<Map<String, Object>> QueryExcuteState = null;

	public void process(Exchange exchange)throws Exception{
		String req = (String)exchange.getIn().getBody(String.class);
		String proposalId = null;
		JsonNode proposalDocNode=null;
		JsonNode request = objectMapper.readTree(req);
		try{
			if(request.has("lob") && request.get("lob").asText() != null  )
			{
				if(request.get("lob").asText().equalsIgnoreCase("bike") ||  request.get("lob").asText().equalsIgnoreCase("car"))
				{
					      try{
								if(request.has("proposalId"))
								{
									proposalId=request.get("proposalId").asText();
								}
								else
								{
									  String Query = "select meta().id as ID from PolicyTransaction where documentType = '"+request.get("lob").asText()+"ProposalRequest' and mobile = '"+request.get("mobile").asText()+"' order by ID desc limit 1 ";
									  QueryExcuteState = DBConnection.getCouchBaseInstance().executeQuery(Query);
									  if (QueryExcuteState.size() > 0) {
										proposalId =  ((Map)QueryExcuteState.get(0)).get("ID").toString();
								   }
								}	 
								  proposalDocNode = objectMapper.readTree(((JsonObject)proposalService.getDocBYId(proposalId).content()).toString());
								  if(proposalDocNode==null)
								  {
									  log.error("proposal details not found for proposal Id in database :"+proposalId);
								  }
					         }catch(NullPointerException e)
					      {
					    	  log.error("Exception in propoal details functionality :",e);
					      }
						  if(request.get("requestType").textValue().equals("createPolicy"))
						      {
						              ObjectNode requestNode = objectMapper.createObjectNode();
                                      requestNode.put("proposalRequest",proposalDocNode );
                                      if(request.has("policyNumber"))
                                      {
                                    	  requestNode.put("policyNumber",request.get("policyNumber").asText());
                                      }
                                      requestNode.put("requestType","SampleCarrierRequest");
            		                  exchange.setProperty("proposalRequest",proposalDocNode);
            		                  exchange.getIn().setHeader("createPolicy","Y");
            			              exchange.getIn().setBody(objectMapper.writeValueAsString(requestNode));
                             }
                            else  if(request.get("requestType").textValue().equals("fetchPolicy"))  
                            {
	                        	 ObjectNode fetchPolicyRequest = objectMapper.createObjectNode();
							     exchange.getIn().setHeader("createPolicy","N");
	                        	 fetchPolicyRequest.put("proposalId", proposalId);
	                        	 fetchPolicyRequest.put("carrierId", proposalDocNode.get("carrierId").asInt());
	                        	 exchange.getIn().setBody(objectMapper.writeValueAsString(fetchPolicyRequest));
                            }
                            else
                             {
	                        	 log.error("please select service request type :"+request);
	                        	 new Exception();
                              }
					   }
				else{
					log.info("Request not having lob as car or bike");
				}	
			}
			else{
				log.info("Request not having lob as key");
			}
		}
		catch(Exception e){
			log.error("Error in Policy Request",e);
		}
	}

}
