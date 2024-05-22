 package com.idep.policy.document.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PolicyPDFResProcessor implements Processor {
   Logger log = Logger.getLogger(PolicyPDFResProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   JsonNode errorNode;
   
   public void process(Exchange exchange) throws Exception {
     try {
       exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1L);
       String pdfResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode pdfResNode = this.objectMapper.readTree(pdfResponse);
       ObjectNode finalResponse = this.objectMapper.createObjectNode();
       ObjectNode obj = this.objectMapper.createObjectNode();
       String pdfString = "data:application/pdf;base64,";
       if (pdfResNode.get("GetSignPolicyPDFResult").hasNonNull("PolicyPDF")) {
         finalResponse.put("signedPDF", pdfString.concat(pdfResNode.get("GetSignPolicyPDFResult").get("PolicyPDF").asText()));
         obj.put("responseCode", 1000);
         obj.put("message", "success");
         obj.put("data", (JsonNode)finalResponse);
       } else {
         finalResponse.put("signedPDF", "NA");
         obj.put("responseCode", 1050);
         obj.put("message", "PDF cannot be signed as of now, please try later");
         obj.put("data", (JsonNode)finalResponse);
       } 
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYPDFRESPRO|ERROR|policy pdf response processing failed :", e);
       ObjectNode obj = this.objectMapper.createObjectNode();
       obj.put("responseCode", 1002);
       obj.put("message", "server error, please try later");
       obj.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
     } 
   }
 }


