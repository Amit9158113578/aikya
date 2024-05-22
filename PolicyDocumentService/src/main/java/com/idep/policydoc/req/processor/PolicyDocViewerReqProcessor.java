package com.idep.policydoc.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policydoc.util.PolicyDocViewConstants;
import com.idep.user.profile.impl.UserProfileServices;
/**
 * 
 * @author sandeep.jadhav
 *
 */
public class PolicyDocViewerReqProcessor implements Processor {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(PolicyDocViewerReqProcessor.class.getName());
	  CBService service =  null;
	  JsonNode responseConfigNode = null;
	  CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	  JsonNode errorNode = null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	  @SuppressWarnings("deprecation")
	public void process(Exchange exchange)
	    
	  {
		  try{
			  
			    if(this.service==null)
			    {
			    	this.service =  CBInstanceProvider.getServerConfigInstance();
			    	this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(PolicyDocViewConstants.RESPONSE_MSG).content().toString());
			    }
			  	String docViewReq = exchange.getIn().getBody(String.class);
			    JsonNode docViewReqNode = this.objectMapper.readTree(docViewReq);
				
			    
			    /**
			     * set user policy keys in property.
			     * We are referring this while calling policy digital sign service
			     */
			    
			    exchange.setProperty("userPolicyKeys", docViewReqNode);
			    String userSecretKey = docViewReqNode.get("uKey").textValue();
			    String policySecretKey = docViewReqNode.get("pKey").textValue();
			    ObjectNode finalDataNode = this.objectMapper.createObjectNode();
			    ObjectNode userDataNode = this.objectMapper.createObjectNode();
			    
			    //JsonArray paramObj = JsonArray.create();
				//paramObj.add(userSecretKey);
				
				// get user profile from database
				JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(userSecretKey);
				if(userProfileDetailsNode == null)
				{
					log.info("user profile not found.. retry after 10 seconds");
					Thread.sleep(20000); // wait for 20 seconds to retrieve profile details again as sync gateway is taking longer time to update view
					userProfileDetailsNode = profileServices.getUserProfileByUkey(userSecretKey);
				}
				if(userProfileDetailsNode!=null)
				{
					log.info("user profile found after retry...");
					JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
					exchange.setProperty("userProfileData", userPersonalInfo);
										
					JsonNode policyNode = profileServices.getPolicyRecordByPkey(userPersonalInfo.get("mobile").asText(), policySecretKey);
					
					/*for(JsonNode policy : userPolicyInfo.get("policyDetails"))
					{
						if(policy.get("secretKey").asText().equalsIgnoreCase(policySecretKey))
						{
							policyNode = policy;
							break;
						}
					}*/
					
			
				
					// load proposalId details from database
					JsonNode proposalDataNode = this.objectMapper.readTree(transService.getDocBYId(policyNode.get("proposalId").asText()).content().toString());
					
					exchange.setProperty(PolicyDocViewConstants.LOG_REQ, "Car|"+proposalDataNode.findValue("carrierId")+"|"+PolicyDocViewConstants.POLICY_SIGN+"|"+proposalDataNode.findValue("proposalId").asText()+ "|");
						
					 //set proposal details for required stored policy pdf in alfresco.
					 exchange.setProperty(PolicyDocViewConstants.CARRIER_PROPOSAL_PRO,proposalDataNode );
					// gather all fields inside node to pass it on to mapper component
					JsonNode proposerDataNode =  proposalDataNode.get("proposalRequest").get("proposerDetails"); 
					String policyHolderName = proposerDataNode.get("firstName").asText();
					if(proposerDataNode.has("lastName"))
					{
						policyHolderName = StringUtils.capitalise(policyHolderName) +" "+StringUtils.capitalise(proposerDataNode.get("lastName").asText());
						
					}
					
					String address = "";
					if(proposerDataNode.has("doorNo"))
					{
						address = proposerDataNode.get("doorNo").asText();
					}
					if(proposerDataNode.has("address"))
					{
						address = address +" "+proposerDataNode.get("address").asText();
					}
					userDataNode.put("name",policyHolderName);
					userDataNode.put("pincode",proposerDataNode.get("pincode").asText());
					//userDataNode.put("country","India");
					String stateCode = "";
					
					
					// get vehicle details
					JsonNode vehicleDetailsNode = proposalDataNode.get("proposalRequest").get("vehicleDetails");
					
					/**
					 * find state code
					 */
					try
					{
						String rtoDocId = vehicleDetailsNode.get("RTOCode").asText().concat("-").concat(proposalDataNode.get("carrierId").asText()).concat("-").concat(proposalDataNode.get("businessLineId").asText());
						JsonNode rtoDoc = objectMapper.readTree(this.service.getDocBYId(rtoDocId).content().toString());
						stateCode = rtoDoc.get("stateCode").asText();
						
						stateCode = "("+stateCode+")";
					}
					catch(Exception e)
					{
						log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCVIEWREQPROCE+"|ERROR|"+"Exception while finding state code for KOTAK policy PDF :",e);
					}
					
					String  policyIssuedAddress = address.concat("\n")
							.concat(proposerDataNode.get("city").asText())
							.concat(" - ").concat(proposerDataNode.get("pincode").asText())
							.concat("\n").concat(proposerDataNode.get("state").asText())
							.concat(stateCode).concat(",").concat("\n").concat("India")
							.concat("\n").concat("Contact Details ")
							.concat(proposerDataNode.get("mobileNumber").asText());
					
					((ObjectNode)policyNode).put("policyIssuedAddress", policyIssuedAddress);
					
					userDataNode.put("salutation", proposerDataNode.get("salutation").asText());
					
					String  receiverAddress = address.concat("\n")
							.concat(proposerDataNode.get("city").asText())
							.concat(" - ").concat(proposerDataNode.get("pincode").asText())
							.concat("\n").concat(proposerDataNode.get("state").asText())
							.concat(stateCode).concat(",").concat("\n").concat("India");
					
					userDataNode.put("address",receiverAddress);
					finalDataNode.put("userInfo", userDataNode);
					finalDataNode.put("userProposal", this.objectMapper.readTree(policyNode.toString()));
					((ObjectNode)proposalDataNode).put("insuranceType",proposalDataNode.get("insuranceType").textValue());
					finalDataNode.put("proposalData", proposalDataNode);
					// get premium details
					finalDataNode.put("premiumDetails", proposalDataNode.get("proposalRequest").get("premiumDetails"));
					
					
					if((!(vehicleDetailsNode.has("financeInstitution"))) || vehicleDetailsNode.get("purchasedLoan").asText().equalsIgnoreCase("No"))
					{
						((ObjectNode)vehicleDetailsNode).put("financeInstitution","NA");
					}
					finalDataNode.put("vehicleDetails",vehicleDetailsNode);
					JsonNode regAddressNode = proposalDataNode.get("proposalRequest").get("vehicleDetails").get("registrationAddress");
					//regAddressNode.get("district").asText().toUpperCase();
					finalDataNode.put("vehicleRegAddress", regAddressNode);
					
					
					
					finalDataNode.put("carrierProposalRequest", proposalDataNode.get("carrierPolicyUpdateReq"));
					
					
				    //exchange.setProperty(PolicyDocViewConstants.CARRIER_INPUT_REQ, this.objectMapper.writeValueAsString(docViewReqNode));
				    exchange.setProperty(PolicyDocViewConstants.CARRIER_REQ_MAP_CONF, PolicyDocViewConstants.POLICYVIEW_CONFIG+proposalDataNode.get(PolicyDocViewConstants.CARRIER_ID).intValue()+
				    		"-"+proposalDataNode.get(PolicyDocViewConstants.PRODUCT_ID).intValue());
				    
				    exchange.getIn().setHeader(PolicyDocViewConstants.REQUESTFLAG, PolicyDocViewConstants.TRUE);
				    exchange.getIn().setBody(finalDataNode);
				}
				else
				{
					log.error(PolicyDocViewConstants.POLICYDOCVIEWREQPROCE+"|ERROR|"+"User profile not found for provided input, please check query used to load profile:"+docViewReqNode);
					exchange.getIn().setHeader(PolicyDocViewConstants.REQUESTFLAG, PolicyDocViewConstants.FALSE);
					ObjectNode obj = this.objectMapper.createObjectNode();
					obj.put(PolicyDocViewConstants.PROPOSAL_RES_CODE, this.responseConfigNode.get(PolicyDocViewConstants.NO_RECORDS_CODE).intValue());
					obj.put(PolicyDocViewConstants.PROPOSAL_RES_MSG, this.responseConfigNode.get(PolicyDocViewConstants.NO_RECORDS_MSG).textValue());
					obj.put(PolicyDocViewConstants.PROPOSAL_RES_DATA, errorNode);
					exchange.getIn().setBody(obj);
				}
			   
		  }
		  catch(Exception e)
		  {
			  
			  	log.error(PolicyDocViewConstants.POLICYDOCVIEWREQPROCE+"|ERROR|"+"Exception at PolicyDocViewerReqProcessor:",e);
			  	exchange.getIn().setHeader(PolicyDocViewConstants.REQUESTFLAG, PolicyDocViewConstants.FALSE);
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(PolicyDocViewConstants.PROPOSAL_RES_CODE, this.responseConfigNode.get(PolicyDocViewConstants.ERROR_CONFIG_CODE).intValue());
				obj.put(PolicyDocViewConstants.PROPOSAL_RES_MSG, this.responseConfigNode.get(PolicyDocViewConstants.ERROR_CONFIG_MSG).textValue());
				obj.put(PolicyDocViewConstants.PROPOSAL_RES_DATA, errorNode);
				exchange.getIn().setBody(obj);
		  }
	    
	  }
	  
	  
	  

}
