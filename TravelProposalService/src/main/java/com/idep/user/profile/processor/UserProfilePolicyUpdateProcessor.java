package com.idep.user.profile.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.user.profile.impl.UserProfileServices;

public class UserProfilePolicyUpdateProcessor implements Processor
{
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(UserProfilePolicyUpdateProcessor.class.getName());
	  CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	  UserProfileServices profileServices = new UserProfileServices();
	  
	  public void process(Exchange exchange) {
		  
		    try
		    {
		    	
		      String userPolicyDetails = exchange.getIn().getBody(String.class);
		      JsonNode userPolicyDetailsNode = this.objectMapper.readTree(userPolicyDetails);
		      
		      /**
		       * update policy details
		       */
			  updatePolicyDocument(userPolicyDetailsNode);
		      
		    }
		    catch(Exception e)
		    {
		    	log.error("Exception at UserProfilePolicyUpdateProcessor : ",e);
		    }
	  }
	  
	  

		public void updatePolicyDocument(JsonNode policyResNodeDetails)
		{
					try{
						// get user profile from database
						JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(policyResNodeDetails.get("uKey").asText());
						if(userProfileDetailsNode!=null)
						{
							JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
							
							ObjectNode policyDetailsNode = objectMapper.createObjectNode();
							policyDetailsNode.put("filePath", policyResNodeDetails.get("filePath").asText());
							policyDetailsNode.put("contentRepoType", policyResNodeDetails.get("contentRepoType").asText());// this path will be read by document download service to get the policy PDF
							if(policyResNodeDetails.has("contentRepoName"))
							{
								policyDetailsNode.put("contentRepoName", policyResNodeDetails.get("contentRepoName").asText());
							}
							String mobile = userPersonalInfo.get("mobile").asText();
							/**
							 * update policy details
							 */
							boolean status = profileServices.updatePolicyRecordByPkey(mobile, policyResNodeDetails.get("pKey").asText(), policyDetailsNode);
							log.info("policy details updated status : "+status);	
							
						}
						else
				         {
				        	 log.error("User profile not found : UserKey"+policyResNodeDetails.get("uKey").asText() +"Pkey "+policyResNodeDetails.get("pKey").asText());
				         }
					}
					catch(Exception e)
					{
						log.error("Failed to update policy document details",e);
					}
		}

}
