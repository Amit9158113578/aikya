package com.idep.policy.carrier.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
/**
 * @author sandeep jadhav
 *
 */
public class StarHealthPolicyReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(StarHealthPolicyReqProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
    try
    {
      String policyRequest = exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
      
     log.debug("Policy Request Node : "+reqNode);
     String url="";
     String purchaseToken="";
     if(reqNode.has("purchaseToken")){
    	 purchaseToken=  reqNode.findValue("purchaseToken").asText();
    	 log.info("Policy Request purchaseToken : "+purchaseToken);
     }else{
    	 JsonDocument paymentRes= service.getDocBYId(reqNode.get("transactionStausInfo").get("apPreferId").asText());
         if(paymentRes!=null){
        	 JsonNode response = objectMapper.readTree(paymentRes.content().toString());
        	 purchaseToken=  response.findValue("purchaseToken").asText();
        	 ((ObjectNode)reqNode).put("purchaseToken", purchaseToken);
         }
     }
      JsonDocument healthPolicyConfigDoc =null;
      
      if(reqNode.has(ProposalConstants.PLAN_ID)){
      healthPolicyConfigDoc=serverConfig.getDocBYId("HealthPolicyRequest-"+reqNode.findValue(ProposalConstants.CARRIER_ID).asText()+"-"+reqNode.findValue(ProposalConstants.PLAN_ID));
      }else{
    	  healthPolicyConfigDoc=serverConfig.getDocBYId("HealthPolicyRequest-"+reqNode.findValue(ProposalConstants.CARRIER_ID).asText());
    	  log.debug("Policy Request healthPolicyConfigDoc : "+healthPolicyConfigDoc);
      }
      if(healthPolicyConfigDoc!=null){
    	  JsonNode configDoc= objectMapper.readTree(healthPolicyConfigDoc.content().toString());
    	 url=configDoc.findValue("URL").asText(); 
    	  url=url.replaceAll("<purchaseToken>", purchaseToken);
      }else{
    	  log.info("unable to find HealthPolicyDocument document : "+"HealthPolicyRequest-"+reqNode.findValue(ProposalConstants.CARRIER_ID).asText());
      }
      exchange.setProperty("requestURL", url);
      exchange.setProperty("policyRequest", reqNode);
      exchange.getIn().setBody(reqNode);     
    }
    catch (Exception e)
    {
    	log.error("error at StarHealthPolicyReqProcessor : ",e);
    	//log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|StarsHealthPolicyReqProcessor|",e);
	      throw new ExecutionTerminator();
    }
  }
}
