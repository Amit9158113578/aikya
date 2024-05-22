package com.idep.user.profile.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;
import com.idep.user.profile.impl.UserProfileBuilder;


/**
* @author  Sandeep Jadhav
* @version 2.0
* @since   01-MAY-2017
* create or update user profile after successful health policy purchase
*/
public class UserProfileReqProcessor implements Processor {
	
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	Logger log = Logger.getLogger(UserProfileReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	UserProfileBuilder profileService = new UserProfileBuilder();
	
	@Override
	public void process(Exchange exchange) {
		
		try {

			String data = exchange.getIn().getBody(String.class);
			JsonNode policyDataNode = objectMapper.readTree(data);;
			// call API method to build user profile			
			JsonNode userProfileDataNode  = profileService.buildUserProfile(policyDataNode);
			if(userProfileDataNode!=null)
			{
				exchange.getIn().setBody(objectMapper.writeValueAsString(userProfileDataNode));
			}
			else
			{
				log.error("ERROR : user profile creation failed : "+data);
			}
		}
		catch(Exception e)
		{
			log.error("Health|"+ProposalConstants.USERPROFILE+"|ERROR|Exception while creating user profile",e);
		}
	
	}
	
}

	
