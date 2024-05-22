 package com.idep.proposal.carrier.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.policy.req.processor.AddRequestInformation;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import com.idep.rest.impl.service.SMSEmailImplService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class EmailTemplateLoader implements Processor {
   Logger log = Logger.getLogger(EmailTemplateLoader.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   JsonNode reqInfoNode;
   
   SMSEmailImplService emailService = new SMSEmailImplService();
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   JsonNode policyPurchaseConfigNode = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       if (this.policyPurchaseConfigNode == null)
         this.policyPurchaseConfigNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("PolicyPurchaseConfigDoc").content()).toString()); 
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
         userName = String.valueOf(String.valueOf(String.valueOf(userName))) + " " + proposalDocInfo.get("proposerDetails").asText("lastName"); 
       ObjectNode paramMapNode = this.objectMapper.createObjectNode();
       paramMapNode.put("TITLE", proposalDocInfo.get("proposerDetails").asText("salutation"));
       paramMapNode.put("USERNAME", userName);
       paramMapNode.put("POLICYSECRETKEY", proposalDetailsNode.findValueAsText("pKey"));
       paramMapNode.put("USERSECRETKEY", proposalDetailsNode.findValueAsText("uKey"));
       paramMapNode.put("POLICYNO", proposalDetailsNode.findValueAsText("policyNo"));
       paramMapNode.put("PREMIUM", proposalDocInfo.get("premiumDetails").asLong("grossPremium").toString());
       paramMapNode.put("IDV", proposalDocInfo.get("premiumDetails").asLong("insuredDeclareValue").toString());
       paramMapNode.put("REGNO", proposalDocInfo.get("vehicleDetails").getKey("registrationNumber"));
       paramMapNode.put("PYPTYPE", proposalDocInfo.get("insuranceDetails").getKey("prevPolicyType"));
       paramMapNode.put("POLICYSTARTDATE", proposalDetailsNode.findValueAsText("policyStartDate"));
       paramMapNode.put("POLICYENDDATE", proposalDetailsNode.findValueAsText("policyExpiryDate"));
       paramMapNode.put("LOGOURL", policyConfigParam.get("LOGOURL").asText());
       paramMapNode.put("APPURL", policyConfigParam.get("APPURL").asText());
       emailDataNode.put("paramMap", (JsonNode)paramMapNode);
       String emailRequest = this.emailService.sendEmailRequest(this.objectMapper.writeValueAsString(emailDataNode));
       exchange.getIn().setBody(emailRequest);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CAREMAILTEMPLOADER" + "|ERROR|" + "Exception at EmailTemplateLoader:", e);
       ObjectNode emailDataNode = this.objectMapper.createObjectNode();
       emailDataNode.put("username", "ERROR");
       emailDataNode.put("emailBody", "ERROR");
       emailDataNode.put("emailSubject", "ERROR");
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(emailDataNode));
     } 
   }
 }


