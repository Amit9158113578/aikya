package com.idep.policy.pdfreq.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.user.profile.impl.UserProfileServices;

/**
 * 
 * @author sandeep.jadhav
 * Load user proposal information to get data required for signed PDF web service
 */
public class PolicyPDFSignProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PolicyPDFSignProcessor.class.getName());
	CBService service =  CBInstanceProvider.getServerConfigInstance();
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	UserProfileServices profileServices = new UserProfileServices();
	  
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String inputReq = exchange.getIn().getBody(String.class);
		  log.info("Policy PDF digital Sign Request initiated : "+inputReq);
		  JsonNode policyPDFDataNode =  this.objectMapper.readTree(inputReq);
		  ObjectNode pdfReqNode =  this.objectMapper.createObjectNode();
		
		  String userSecretKey = policyPDFDataNode.get("uKey").textValue();
		  String policySecretKey = policyPDFDataNode.get("pKey").textValue();
		  
		  //JsonArray paramObj = JsonArray.create();
		  //paramObj.add(userSecretKey);
		 
		  // get user profile from database
		  	JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(userSecretKey);
			if(userProfileDetailsNode!=null)
			{
				JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
				exchange.setProperty("userProfileData", userPersonalInfo);
									
				JsonNode policyNode = profileServices.getPolicyRecordByPkey(userPersonalInfo.get("mobile").asText(), policySecretKey);
				
				this.log.debug("userProposal : "+policyNode);
				ObjectNode proposalDocument = (ObjectNode)this.objectMapper.readTree(this.transService.getDocBYId(policyNode.get("proposalId").asText()).content().toString());
		
				pdfReqNode.put("carrierPolicyUpdateReq",proposalDocument.get("carrierPolicyUpdateReq"));
				pdfReqNode.put("carrierPolicyResponse",proposalDocument.get("carPolicyResponse"));
				pdfReqNode.put("requestType","CarPolicyPDFSignRequest");
				pdfReqNode.put("carrierId",proposalDocument.get("carrierId").asInt());
				pdfReqNode.put("productId",proposalDocument.get("productId").asInt());
				String pdfData = policyPDFDataNode.get("policyDocStream").textValue().replaceAll("data:application/pdf;base64,", "");
				pdfReqNode.put("signPDFInput",pdfData);
				/**
				 * set property to get sample request structure
				 */
				exchange.setProperty("CarPolicyPDFSignRequestSample", "CarPolicyPDFSignRequest-"+proposalDocument.get("carrierId").asInt()+"-"+proposalDocument.get("productId").asInt()+"-sample");
				exchange.getIn().setBody(pdfReqNode);
				
			}
			else
			{
				log.error("User profile not found with provided user key : "+userSecretKey);
			}
		}
		catch(Exception e)
		{
			log.error("Policy PDF digital sign process could not be completed ",e);
		}
	}

	
	
}
