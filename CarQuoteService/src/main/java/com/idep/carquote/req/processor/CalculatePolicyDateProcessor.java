 package com.idep.carquote.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.carquote.exception.processor.ExecutionTerminator;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CalculatePolicyDateProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CalculatePolicyDateProcessor.class.getName());
   
   SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   public void process(Exchange exchange) throws ExecutionTerminator {
     try {
       String request = exchange.getIn().getBody().toString();
       JsonNode inputReqNode = this.objectMapper.readTree(request);
       ObjectNode systemPolicyDates = this.objectMapper.createObjectNode();
       if (inputReqNode.has("quoteParam"))
         if (inputReqNode.get("quoteParam").get("policyType").textValue().equals("new")) {
           Calendar cal = new GregorianCalendar();
           Date today = cal.getTime();
           String sysPolicyStartDate = this.dateFormat.format(today);
           cal.add(1, 3);
           cal.add(7, -1);
           Date nextYear = cal.getTime();
           String sysPolicyEndDate = this.dateFormat.format(nextYear);
           systemPolicyDates.put("sysPolicyStartDate", sysPolicyStartDate);
           systemPolicyDates.put("sysPolicyEndDate", sysPolicyEndDate);
           ((ObjectNode)inputReqNode).put("systemPolicyStartDate", (JsonNode)systemPolicyDates);
         } else if (inputReqNode.get("vehicleInfo").get("previousPolicyExpired").textValue().equals("Y")) {
           Calendar cal = new GregorianCalendar();
           Date today = cal.getTime();
           String sysPolicyStartDate = this.dateFormat.format(today);
           cal.add(1, 1);
           cal.add(7, -1);
           Date nextYear = cal.getTime();
           String sysPolicyEndDate = this.dateFormat.format(nextYear);
           systemPolicyDates.put("sysPolicyStartDate", sysPolicyStartDate);
           systemPolicyDates.put("sysPolicyEndDate", sysPolicyEndDate);
           ((ObjectNode)inputReqNode).put("systemPolicyStartDate", (JsonNode)systemPolicyDates);
           if (!inputReqNode.get("vehicleInfo").has("PreviousPolicyStartDate")) {
             JsonNode vehicleInfo = calculatePrePolicyStartDate(inputReqNode.get("vehicleInfo"));
             ((ObjectNode)inputReqNode).put("vehicleInfo", vehicleInfo);
           } 
         } else if (inputReqNode.has("vehicleInfo")) {
           if (inputReqNode.get("vehicleInfo").has("PreviousPolicyExpiryDate")) {
             if (!inputReqNode.get("vehicleInfo").has("PreviousPolicyStartDate")) {
               JsonNode vehicleInfo = calculatePrePolicyStartDate(inputReqNode.get("vehicleInfo"));
               ((ObjectNode)inputReqNode).put("vehicleInfo", vehicleInfo);
             } 
             if (inputReqNode.get("vehicleInfo").get("PreviousPolicyExpiryDate") != null && inputReqNode.get("vehicleInfo").get("PreviousPolicyExpiryDate").textValue() != "")
               inputReqNode = calculateDate(inputReqNode, systemPolicyDates); 
           } else {
             DateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
             Date today = new Date();
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(today);
             calendar.add(2, 1);
             calendar.set(5, 1);
             calendar.add(5, -1);
             Date EndDate = calendar.getTime();
             String prePolicyEndDate = this.dateFormat.format(EndDate);
             calendar.add(1, -1);
             calendar.add(7, 1);
             Date preYear = calendar.getTime();
             String prePolicyStartDate = this.dateFormat.format(preYear);
             ((ObjectNode)inputReqNode.get("vehicleInfo")).put("PreviousPolicyExpiryDate", prePolicyEndDate);
             ((ObjectNode)inputReqNode.get("vehicleInfo")).put("PreviousPolicyStartDate", prePolicyStartDate);
             inputReqNode = calculateDate(inputReqNode, systemPolicyDates);
           } 
         } else {
           this.log.error(CalculatePolicyDateProcessor.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- vehicle information Not found :  " + inputReqNode);
           throw new ExecutionTerminator();
         }  
       if (inputReqNode.has("quoteParam")) {
         double calculateVehicleAge = calculateVehicleAge(inputReqNode);
         ((ObjectNode)inputReqNode.get("quoteParam")).put("vehicleAge", calculateVehicleAge);
       } 
       JsonNode p365VehicleMaster = getP365VehicleMaster(inputReqNode.get("vehicleInfo").get("variantId").asText());
       if (p365VehicleMaster != null) {
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("model", p365VehicleMaster.get("model").textValue());
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("variant", p365VehicleMaster.get("variant").textValue());
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("cubicCapacity", p365VehicleMaster.get("cubicCapacity").textValue());
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("name", p365VehicleMaster.get("make").textValue());
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("make", p365VehicleMaster.get("make").textValue());
         ((ObjectNode)inputReqNode.get("vehicleInfo")).put("fuelType", p365VehicleMaster.get("fuelType").textValue());
       } 
       exchange.getIn().setBody(inputReqNode);
     } catch (Exception e) {
       this.log.error(CalculatePolicyDateProcessor.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- CalculatePolicyDateProcessor Failed ", e);
       throw new ExecutionTerminator();
     } 
   }
   
   public JsonNode calculateDate(JsonNode inputReqNode, ObjectNode systemPolicyDates) throws ParseException, ExecutionTerminator {
     try {
       String prepolicyExpDate = inputReqNode.get("vehicleInfo").get("PreviousPolicyExpiryDate").textValue();
       Date predate = this.dateFormat.parse(prepolicyExpDate);
       Calendar.getInstance().setTime(predate);
       Calendar cal = new GregorianCalendar();
       cal.setTime(predate);
       cal.add(7, 1);
       Date today = cal.getTime();
       String sysPolicyStartDate = this.dateFormat.format(today);
       cal.add(1, 1);
       Date nextYear = cal.getTime();
       Calendar.getInstance().setTime(nextYear);
       cal.add(7, -1);
       nextYear = cal.getTime();
       String sysPolicyEndDate = this.dateFormat.format(nextYear);
       systemPolicyDates.put("sysPolicyStartDate", sysPolicyStartDate);
       systemPolicyDates.put("sysPolicyEndDate", sysPolicyEndDate);
       ((ObjectNode)inputReqNode).put("systemPolicyStartDate", (JsonNode)systemPolicyDates);
       if (inputReqNode.get("quoteParam").has("onlyODApplicable") && inputReqNode.get("quoteParam").get("onlyODApplicable").asBoolean()) {
         String prepolicyStartDate = inputReqNode.get("vehicleInfo").get("PreviousPolicyStartDate").textValue();
         Date prepolicyStDate = this.dateFormat.parse(prepolicyStartDate);
         Calendar.getInstance().setTime(prepolicyStDate);
         Calendar caltp = new GregorianCalendar();
         caltp.setTime(predate);
         caltp.add(1, 2);
         Date expiredate = caltp.getTime();
         Calendar.getInstance().setTime(expiredate);
         caltp.add(7, -1);
         expiredate = caltp.getTime();
         String prePolicyTpEndDate = this.dateFormat.format(expiredate);
         systemPolicyDates.put("prePolicyTpEndDate", prePolicyTpEndDate);
       } 
       return inputReqNode;
     } catch (Exception e) {
       this.log.error(CalculatePolicyDateProcessor.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- CalculateDate Method Failed ", e);
       throw new ExecutionTerminator();
     } 
   }
   
   public JsonNode calculatePrePolicyStartDate(JsonNode vehicleInfo) throws ParseException, ExecutionTerminator {
     try {
       String prepolicyExpDate = vehicleInfo.get("PreviousPolicyExpiryDate").textValue();
       Date predate = this.dateFormat.parse(prepolicyExpDate);
       Calendar.getInstance().setTime(predate);
       Calendar cal = new GregorianCalendar();
       cal.setTime(predate);
       cal.add(1, -1);
       cal.add(7, 1);
       Date nextYear = cal.getTime();
       Calendar.getInstance().setTime(nextYear);
       nextYear = cal.getTime();
       String prePolicyStartDate = this.dateFormat.format(nextYear);
       ((ObjectNode)vehicleInfo).put("PreviousPolicyStartDate", prePolicyStartDate);
       return vehicleInfo;
     } catch (Exception e) {
       this.log.error(CalculatePolicyDateProcessor.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- calculatePrePolicyStartDate Method Failed ", e);
       throw new ExecutionTerminator();
     } 
   }
   
   public double calculateVehicleAge(JsonNode inputReqNode) throws ParseException, ExecutionTerminator {
     double age = 0.0D;
     try {
       SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
       String startdate = inputReqNode.get("systemPolicyStartDate").get("sysPolicyStartDate").textValue();
       Date policystartdate = sdf.parse(startdate);
       Calendar.getInstance().setTime(policystartdate);
       Calendar cal = new GregorianCalendar();
       cal.setTime(policystartdate);
       String startDateAddingDays = sdf.format(cal.getTime());
       String registration = inputReqNode.get("vehicleInfo").get("dateOfRegistration").textValue();
       age = Math.abs((sdf.parse(startDateAddingDays).getTime() - sdf.parse(registration).getTime()) / 86400000L) / 365.0D;
       String stringage = String.format("%.2f", new Object[] { Double.valueOf(age) });
       age = Double.parseDouble(stringage);
       return age;
     } catch (Exception e) {
       this.log.error("unable to calculate kotak car vehicle age  :" + inputReqNode);
       return age;
     } 
   }
   
   public JsonNode getP365VehicleMaster(String varientId) throws JsonProcessingException, IOException {
     JsonNode P365CarVarientIdNode = null;
     JsonDocument PBVarientDocId = this.serverConfig.getDocBYId(varientId);
     if (PBVarientDocId != null) {
       P365CarVarientIdNode = this.objectMapper.readTree(((JsonObject)PBVarientDocId.content()).toString());
       return P365CarVarientIdNode;
     } 
     return P365CarVarientIdNode;
   }
 }


