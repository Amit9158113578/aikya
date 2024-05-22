 package com.idep.policy.carrier.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class KotakReqFormatter implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(KotakReqFormatter.class.getName());
   
   CBService service = CBInstanceProvider.getServerConfigInstance();
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
   
   public void process(Exchange exchange) throws Exception {
     try {
       String inputReq = exchange.getIn().getBody().toString();
       JsonNode proposalDataNode = this.objectMapper.readTree(inputReq);
       JsonNode paymentResNode = this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(proposalDataNode.findValue("paymentResponse").get("apPreferId").asText()).content()).toString());
       ((ObjectNode)proposalDataNode).put("paymentResponse", paymentResNode);
       exchange.setProperty("deviceId", proposalDataNode.findValue("deviceId"));
       ((ObjectNode)proposalDataNode).put("requestType", "CarPolicyUpdateRequest");
       ((ObjectNode)proposalDataNode).put("IMTNos", getIMTNos(proposalDataNode));
       JsonNode quoteDataNode = this.objectMapper.readTree(((JsonObject)this.quoteData.getDocBYId(proposalDataNode.findValue("QUOTE_ID").asText()).content()).toString());
       JsonNode quoteData = quoteDataNode.get("carrierTransformedReq").get(proposalDataNode.get("productId").asText());
       ((ObjectNode)proposalDataNode).put("carrierQuoteTransformedReq", quoteData);
       exchange.getIn().setBody(proposalDataNode);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "KOTAKREQFORM" + "|ERROR|" + "Exception at KotakReqFormatter:", e);
       throw new ExecutionTerminator();
     } 
   }
   
   public String getIMTNos(JsonNode proposalDataNode) {
     StringBuffer imtCodes = new StringBuffer("");
     imtCodes.append("22,");
     JsonNode proposalRequestNode = proposalDataNode.get("proposalRequest");
     JsonNode insuranceDetailsNode = proposalRequestNode.get("insuranceDetails");
     JsonNode premiumDetailsNode = proposalRequestNode.get("premiumDetails");
     JsonNode vehicleDetailsNode = proposalRequestNode.get("vehicleDetails");
     if (insuranceDetailsNode.get("isNCB").asText().equalsIgnoreCase("Y") && 
       insuranceDetailsNode.get("ncb").asInt() > 0)
       imtCodes.append("GR27,"); 
     if (premiumDetailsNode.has("voluntaryDeductibleSI") && 
       premiumDetailsNode.get("voluntaryDeductibleSI").asInt() > 0)
       imtCodes.append("22A,"); 
     if (premiumDetailsNode.has("paidDriverCover"))
       imtCodes.append("GR36A,"); 
     if (vehicleDetailsNode.get("purchasedLoan").asText().equalsIgnoreCase("Yes"))
       if (vehicleDetailsNode.get("vehicleLoanType").asText().equalsIgnoreCase("Hire Purchase")) {
         imtCodes.append("5,");
       } else if (vehicleDetailsNode.get("vehicleLoanType").asText().equalsIgnoreCase("Lease")) {
         imtCodes.append("6,");
       } else if (vehicleDetailsNode.get("vehicleLoanType").asText().equalsIgnoreCase("Hypothecation")) {
         imtCodes.append("7,");
       }  
     if (premiumDetailsNode.has("ElectricalAccessoriesSI") && 
       premiumDetailsNode.get("ElectricalAccessoriesSI").asInt() > 0)
       imtCodes.append("24,"); 
     if (premiumDetailsNode.has("LPGCNGKitSI") && 
       premiumDetailsNode.get("LPGCNGKitSI").asInt() > 0)
       imtCodes.append("25,"); 
     if (premiumDetailsNode.has("ridersList"))
       for (JsonNode rider : premiumDetailsNode.get("ridersList")) {
         if (rider.get("riderId").asInt() == 20 && rider.get("riderValue").asDouble() > 0.0D) {
           imtCodes.append("17,");
           imtCodes.append("28,");
           continue;
         } 
         if (rider.get("riderId").asInt() == 21 && rider.get("riderValue").asDouble() > 0.0D)
           imtCodes.append("16,"); 
       }  
     return imtCodes.toString().substring(0, imtCodes.toString().length() - 1);
   }
 }


