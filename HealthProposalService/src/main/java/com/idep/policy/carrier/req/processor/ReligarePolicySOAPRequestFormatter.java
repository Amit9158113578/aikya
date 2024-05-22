package com.idep.policy.carrier.req.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligarePolicySOAPRequestFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligarePolicySOAPRequestFormatter.class.getName());
	  SoapConnector  soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String request  = exchange.getIn().getBody(String.class);
		  
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		  request = request.replace("<o>", "");
		  request = request.replace("</o>", "");
		  
		  
	      Map<String, String> tnsMap =  new HashMap<String,String>();
		  tnsMap.put("proposalNum", "http://intf.insurance.symbiosys.c2lbiz.com/xsd");
		  tnsMap.put("parentTns", "http://relinterface.insurance.symbiosys.c2lbiz.com");
		  
		  String response  = soapService.prepareSoapRequest("getPolicyStatus", "intSetPolicyStatusIO", request, tnsMap);
		  log.debug("response from soap connector API : "+response);
		  exchange.getIn().setBody(response);
		    
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|ReligarePolicySOAPRequestFormatter|",e);
			throw new ExecutionTerminator();
		}
		  
	}
	
}
