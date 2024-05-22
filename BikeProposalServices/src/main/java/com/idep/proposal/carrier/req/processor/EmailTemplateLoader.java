 package com.idep.proposal.carrier.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.policy.req.processor.AddRequestInformation;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import com.idep.rest.impl.service.SMSEmailImplService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class EmailTemplateLoader
   implements Processor {
   Logger log = Logger.getLogger(EmailTemplateLoader.class.getName());
   
   JsonNode reqInfoNode;
   
   SMSEmailImplService emailService = new SMSEmailImplService();
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   JsonNode policyPurchaseConfigNode = null;
   
   ObjectNode paramMapNode = Utils.mapper.createObjectNode();
   
   public void process(Exchange exchange) throws Exception {
     try {
       if (this.policyPurchaseConfigNode == null)
         this.policyPurchaseConfigNode = Utils.mapper.readTree(((JsonObject)this.serverConfig.getDocBYId("PolicyPurchaseConfigDoc").content()).toString()); 
       ObjectNode emailDataNode = Utils.mapper.createObjectNode();
       String proposalDoc = (String)exchange.getIn().getBody(String.class);
       ExtendedJsonNode proposalDetailsNode = new ExtendedJsonNode(Utils.mapper.readTree(proposalDoc).get("data"));
       proposalDetailsNode = (new AddRequestInformation()).addProposalInfo(proposalDetailsNode);
       ExtendedJsonNode proposalDocInfo = proposalDetailsNode.get("proposalInfo").get("proposalRequest");
       emailDataNode.put("username", proposalDocInfo.get("proposerDetails").asText("emailId"));
       JsonNode policyConfigParam = this.policyPurchaseConfigNode.get(proposalDetailsNode.findValueAsText("carrierId"));
       emailDataNode.put("funcType", policyConfigParam.get("funcType").asText());
       String userName = proposalDocInfo.get("proposerDetails").asText("firstName");
       if (proposalDocInfo.get("proposerDetails").has("lastName"))
         userName = String.valueOf(String.valueOf(userName)) + " " + proposalDocInfo.get("proposerDetails").asText("lastName"); 
       this.paramMapNode.put("TITLE", proposalDocInfo.get("proposerDetails").asText("salutation"));
       this.paramMapNode.put("USERNAME", userName);
       this.paramMapNode.put("POLICYSECRETKEY", proposalDetailsNode.findValueAsText("pKey"));
       this.paramMapNode.put("USERSECRETKEY", proposalDetailsNode.findValueAsText("uKey"));
       this.paramMapNode.put("POLICYNO", proposalDetailsNode.findValueAsText("policyNo"));
       this.paramMapNode.put("PREMIUM", proposalDocInfo.get("premiumDetails").asLong("grossPremium").toString());
       this.paramMapNode.put("IDV", proposalDocInfo.get("premiumDetails").asLong("insuredDeclareValue").toString());
       this.paramMapNode.put("REGNO", proposalDocInfo.get("vehicleDetails").getKey("registrationNumber"));
       this.paramMapNode.put("PYPTYPE", proposalDocInfo.get("insuranceDetails").getKey("prevPolicyType"));
       this.paramMapNode.put("POLICYSTARTDATE", proposalDetailsNode.findValueAsText("policyStartDate"));
       this.paramMapNode.put("POLICYENDDATE", proposalDetailsNode.findValueAsText("policyExpiryDate"));
       this.paramMapNode.put("LOGOURL", policyConfigParam.get("LOGOURL").asText());
       this.paramMapNode.put("APPURL", policyConfigParam.get("APPURL").asText());
       emailDataNode.put("paramMap", (JsonNode)this.paramMapNode);
       String emailRequest = this.emailService.sendEmailRequest(Utils.mapper.writeValueAsString(emailDataNode));
       exchange.getIn().setBody(emailRequest);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("exception in EmailTemplateLoader class : not found :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


