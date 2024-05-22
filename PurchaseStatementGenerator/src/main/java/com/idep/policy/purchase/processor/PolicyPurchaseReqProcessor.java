package com.idep.policy.purchase.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.purchase.util.PurchaseStmtConstants;
import com.idep.user.profile.impl.UserProfileServices;

public class PolicyPurchaseReqProcessor implements Processor {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(PolicyPurchaseReqProcessor.class.getName());
	  CBService service =  null;
	  JsonNode responseConfigNode = null;
	  CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	  JsonNode errorNode = null;
	  JsonObject stmtReqTypeConfig = null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	  public void process(Exchange exchange)
	  {
		  try {
			  
			    if(this.service==null)
			    {
			    	this.service =  CBInstanceProvider.getServerConfigInstance();
			    	this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(PurchaseStmtConstants.RESPONSE_MSG).content().toString());
			    	this.stmtReqTypeConfig = this.service.getDocBYId(PurchaseStmtConstants.STMT_REQ_TYPE_CONFIG).content();
			    }
			    
			  	String request = exchange.getIn().getBody(String.class);
			  	JsonNode reqNode =  objectMapper.readTree(request);
			  	log.info("purchase statement request : "+reqNode);
			  	
			  	 String userSecretKey = reqNode.get("uKey").textValue();
				 String policySecretKey = reqNode.get("pKey").textValue();
				 			 
				 // get user profile from database
					
					JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(userSecretKey);
					
					if(userProfileDetailsNode!=null)
					{
						JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");									
						JsonNode policyNode = profileServices.getPolicyRecordByPkey(userPersonalInfo.get("mobile").asText(), policySecretKey);
						
						this.log.debug("policyNode : "+policyNode);
						ObjectNode proposalDocument = (ObjectNode)this.objectMapper.readTree(this.transService.getDocBYId(policyNode.get("proposalId").asText()).content().toString());
						proposalDocument.put("requestType", this.stmtReqTypeConfig.getString(proposalDocument.get("businessLineId").asText()));
						
						exchange.getIn().setBody(proposalDocument);
						
					}
					
		  }
		  catch(Exception e)
		  {
			  log.error("Exception at PolicyPurchaseReqProcessor : ", e);
		  }
	  }

}
