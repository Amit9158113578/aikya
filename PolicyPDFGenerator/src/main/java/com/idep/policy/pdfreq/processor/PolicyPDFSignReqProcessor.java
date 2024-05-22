package com.idep.policy.pdfreq.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyPDFSignReqProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PolicyPDFSignReqProcessor.class.getName());
	CBService service =  CBInstanceProvider.getServerConfigInstance();
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	SoapConnector  soapService = new SoapConnector();
	  
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		String inputReq = exchange.getIn().getBody().toString();
		
		JsonNode reqNode =  this.objectMapper.readTree(inputReq);
		LinkedHashMap<String,Object> orderedReq = new LinkedHashMap<String,Object>();
		
		LinkedHashMap<String,Object> sampleMap = new LinkedHashMap<String,Object>();
		sampleMap.put("strUserId", "");
		sampleMap.put("strPassword", "");
		sampleMap.put("strProdType", "");
		sampleMap.put("strPolicyNo", "");
		sampleMap.put("strPolicyIssueTime", "");
		sampleMap.put("strTransactionID", "");
		sampleMap.put("strTransactionRef", "");
		sampleMap.put("strCustomerName", "");
		sampleMap.put("strPolicyPDF", "");
		
		for (Map.Entry<String,Object> entry : sampleMap.entrySet()) {
		    
			orderedReq.put(entry.getKey(),reqNode.get(entry.getKey()).textValue());
		}
		
		log.debug("orderedReq map : "+orderedReq);
		String soapRequest = soapService.prepareSoapRequest("GetSignPolicyPDF", "http://tempuri.org/", orderedReq);
		log.debug("PolicyPDFSignReqProcessor soapRequest : "+soapRequest);
		
		exchange.getIn().setBody(soapRequest);
		
		}
		catch(Exception e)
		{
			log.error("Policy PDF digital sign process could not be completed ",e);
		}
		
	}
	
}
