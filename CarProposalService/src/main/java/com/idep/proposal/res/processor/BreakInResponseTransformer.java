 package com.idep.proposal.res.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.api.impl.SoapConnector;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class BreakInResponseTransformer implements Processor {
   Logger log = Logger.getLogger(BreakInResponseTransformer.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   JsonNode configDocNode = null;
   
   public void process(Exchange exchange) throws ExecutionTerminator {
     try {
       this.configDocNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId(exchange.getProperty("CarProposalRequestConfigDoc").toString()).content()).toString());
       String proposalResForm = (String)exchange.getIn().getBody(String.class);
       if (proposalResForm.contains("&gt;") || proposalResForm.contains("&lt;")) {
         proposalResForm = proposalResForm.replaceAll("&gt;", ">");
         proposalResForm = proposalResForm.replaceAll("&lt;", "<");
       } 
       proposalResForm = proposalResForm.replace(" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"urn:server\"", "")
         .replace(" xmlns=\"urn:server\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"", "")
         .replace(" xsi:type=\"xsd:string\"", "")
         .replace(" xsi:type=\"xsd:int\"", "");
       SoapConnector extService = new SoapConnector();
       String formattedSooapRes = extService.getSoapResult(proposalResForm, this.configDocNode.get("resultTagName").asText());
       this.log.info("Formatted Soap Response : " + formattedSooapRes.toString());
       exchange.getIn().setBody(formattedSooapRes);
     } catch (Exception e) {
       this.log.error("Exception at CarCarrierResponseTransformer : ", e);
       throw new ExecutionTerminator();
     } 
   }
 }


