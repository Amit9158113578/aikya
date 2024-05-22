package com.idep.user.profile.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;
import com.idep.user.profile.impl.UserProfileBuilder;


/**
 * create or update user profile after successful health policy purchase
 */
public class UserProfileReqProcessor implements Processor {

	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	Logger log = Logger.getLogger(UserProfileReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	UserProfileBuilder profileService = new UserProfileBuilder();
	static String ExceptionHandlerQ = "ExceptionHandlerQ";
	@Override
	public void process(Exchange exchange) {
		CamelContext camelContext = exchange.getContext();
		ProducerTemplate template = camelContext.createProducerTemplate();
		JsonNode policyDataNode = null;
		try {

			String data = exchange.getIn().getBody(String.class);
			policyDataNode = objectMapper.readTree(data);;
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
			log.error("Travel|"+ProposalConstants.USERPROFILE+"|ERROR|Exception while creating user profile",e);

			String trace = "Error in Class :"+UserProfileReqProcessor.class+"   Line Number :"+Thread.currentThread().getStackTrace()[0].getLineNumber();
			log.info("Erroror messgaes UserProfileReqProcessor"+UserProfileReqProcessor.class+"    "+Thread.currentThread().getStackTrace()[0].getLineNumber());
			String uri = "activemq:queue:" + ExceptionHandlerQ;
			((ObjectNode) policyDataNode).put("transactionName","UserProfileReqProcessor");
			((ObjectNode) policyDataNode).put("Exception",e.toString());
			((ObjectNode) policyDataNode).put("ExceptionMessage",trace);
			exchange.getIn().setBody(policyDataNode.toString());
			log.info("sending to exception handler queue"+policyDataNode);
			exchange.setPattern(ExchangePattern.InOnly);
			template.send(uri, exchange);


		}

	}

}


