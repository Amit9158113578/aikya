 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.PaymentResponseFormat;
 import com.idep.service.payment.util.PaymentConstant;
 import java.io.IOException;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PaymentProposalDataReader implements Processor {
   Logger log = Logger.getLogger(PaymentProposalDataReader.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) {
     this.log.info("Inside PaymentRequestCreation.");
     JsonNode proposerDetailsNode = null;
     JsonNode paymentConstantNode = this.paymentDataAccessor.fetchDBDocument("PaymentConstantConfig", "serverConfig");
     ObjectNode requestDocNode = this.objectMapper.createObjectNode();
     String proposalResponse = (String)exchange.getIn().getBody(String.class);
     try {
       JsonNode proposalInfo = this.objectMapper.readTree(proposalResponse);
       String proposalNumber = proposalInfo.get(paymentConstantNode.get("PROPOSAL_NUMBER").textValue()).textValue();
       proposerDetailsNode = this.paymentDataAccessor.fetchDBDocument(proposalNumber, "policyTransaction");
       this.log.info(String.valueOf(proposalNumber) + " : proposal document fetched  : " + proposerDetailsNode);
       ((ObjectNode)proposerDetailsNode).put(paymentConstantNode.get("BUSINESSLINE_ID").textValue(), proposalInfo.get(paymentConstantNode.get("BUSINESSLINE_ID").textValue()).intValue());
       requestDocNode.put(paymentConstantNode.get("PAYMENT_MAPPER_REQUEST_TYPE").textValue(), paymentConstantNode.get("PAYMENT_MAPPER_REQUEST_TYPE_VALUE").textValue());
       requestDocNode.put(paymentConstantNode.get("PAYMENT_MAPPER_CARRIER_RESP_KEY").textValue(), proposerDetailsNode);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("proposalNumber", proposalNumber);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
     } catch (JsonProcessingException e) {
       this.log.error("JsonProcessingException at PaymentProposalDataReader : ", (Throwable)e);
       exchange.getIn().setBody(PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_FAILED_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_FAILED_MESSAGE").textValue(), null));
     } catch (IOException e) {
       this.log.error("IOException at PaymentProposalDataReader : ", e);
       exchange.getIn().setBody(PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_FAILED_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_FAILED_MESSAGE").textValue(), null));
     } 
     this.log.info("Inside PaymentRequestCreation done.");
   }
 }


