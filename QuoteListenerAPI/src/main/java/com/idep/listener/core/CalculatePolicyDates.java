package com.idep.listener.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class CalculatePolicyDates {
  ObjectMapper objectMapper = new ObjectMapper();
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
  
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  Logger log = Logger.getLogger(CalculatePolicyDates.class);
  
  ObjectNode systemPolicyDates = this.objectMapper.createObjectNode();
  
  public JsonNode calculateDates(JsonNode inputReqNode) {
    try {
      if (inputReqNode.has("quoteParam"))
        if (inputReqNode.get("quoteParam").get("policyType").textValue().equals("new")) {
          calculatePolicyDatesForNewVehicle(inputReqNode);
        } else {
          boolean validatePrePolicyDates = validatePrePolicyExpiryDates(inputReqNode);
          if (validatePrePolicyDates) {
            calculateDateForPrePolicyExpired(inputReqNode);
          } else {
            calculateDate(inputReqNode, this.systemPolicyDates);
          } 
          if (inputReqNode.has("vehicleInfo")) {
            if (inputReqNode.get("vehicleInfo").has("PreviousPolicyExpiryDate")) {
              JsonNode vehicleInfo = calculatePrePolicyStartDate(inputReqNode.get("vehicleInfo"));
              ((ObjectNode)inputReqNode).put("vehicleInfo", vehicleInfo);
              int expirydays = (int)calculatePreviousPolicyExpiryDays(vehicleInfo.get("PreviousPolicyExpiryDate").asText());
              String expirydaysting = String.valueOf(expirydays);
              if (expirydaysting.contains("-"))
                expirydaysting = expirydaysting.replace("-", ""); 
              expirydays = Integer.parseInt(expirydaysting);
              ((ObjectNode)inputReqNode.get("vehicleInfo")).put("policyDays", expirydays);
            } else {
              calculatePreviousPolicyDates(inputReqNode, this.systemPolicyDates);
            } 
          } else {
            this.log.error(CalculatePolicyDates.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + 
                Thread.currentThread().getStackTrace()[2].getLineNumber() + "- vehicle information Not found :  " + inputReqNode);
          } 
        }  
      if (inputReqNode.has("quoteParam") && 
        !inputReqNode.get("quoteParam").has("vehicleAge")) {
        double calculateVehicleAge = calculateVehicleAge(inputReqNode);
        ((ObjectNode)inputReqNode.get("quoteParam")).put("vehicleAge", calculateVehicleAge);
      } 
    } catch (NullPointerException e) {
      e.printStackTrace();
      this.log.error("error in calculate dates methods at :" + 
          Thread.currentThread().getStackTrace()[0].getFileName() + 
          "-" + 
          Thread.currentThread().getStackTrace()[0].getMethodName() + 
          "-" + 
          Thread.currentThread().getStackTrace()[0].getLineNumber());
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in calculate dates methods");
    } 
    return inputReqNode;
  }
  
  public JsonNode calculateDate(JsonNode inputReqNode, ObjectNode systemPolicyDates) throws ParseException {
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
      return inputReqNode;
    } catch (Exception e) {
      this.log.error(
          CalculatePolicyDates.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + 
          " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- CalculateDate Method Failed ", e);
      return inputReqNode;
    } 
  }
  
  public JsonNode calculatePrePolicyStartDate(JsonNode vehicleInfo) throws ParseException {
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
      this.log.error(CalculatePolicyDates.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + 
          Thread.currentThread().getStackTrace()[2].getLineNumber() + "- calculatePrePolicyStartDate Method Failed ", e);
      return vehicleInfo;
    } 
  }
  
  public double calculateVehicleAge(JsonNode inputReqNode) throws ParseException {
    double age = 0.0D;
    try {
      String startdate = inputReqNode.get("systemPolicyStartDate").get("sysPolicyStartDate").textValue();
      Date policystartdate = this.dateFormat.parse(startdate);
      Calendar.getInstance().setTime(policystartdate);
      Calendar cal = new GregorianCalendar();
      cal.setTime(policystartdate);
      String startDateAddingDays = this.dateFormat.format(cal.getTime());
      String registration = inputReqNode.get("vehicleInfo").get("dateOfRegistration").textValue();
      age = Math.abs((this.dateFormat.parse(startDateAddingDays).getTime() - this.dateFormat.parse(registration).getTime()) / 86400000L) / 365.0D;
      String stringage = String.format("%.2f", new Object[] { Double.valueOf(age) });
      age = Double.parseDouble(stringage);
      return age;
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("unable to calculate kotak car vehicle age  :" + inputReqNode);
      return age;
    } 
  }
  
  public JsonNode calculatePreviousPolicyDates(JsonNode inputReqNode, ObjectNode systemPolicyDates) throws ParseException {
    try {
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
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in calculatePreviousPolicyDates method :");
    } 
    return inputReqNode = calculateDate(inputReqNode, systemPolicyDates);
  }
  
  public boolean validatePrePolicyExpiryDates(JsonNode inputRequest) throws ParseException {
    try {
      Calendar cal = new GregorianCalendar();
      String today = this.dateFormat.format(cal.getTime());
      String prePolicyExpiryDate = inputRequest.get("vehicleInfo").get("PreviousPolicyExpiryDate").asText();
      Date prePolicyEndDate = this.dateFormat.parse(prePolicyExpiryDate);
      Date todays = this.dateFormat.parse(today);
      if (prePolicyEndDate.before(todays)) {
        ((ObjectNode)inputRequest.get("vehicleInfo")).put("previousPolicyExpired", "Y");
        return true;
      } 
      ((ObjectNode)inputRequest.get("vehicleInfo")).put("previousPolicyExpired", "N");
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in validatePrePolicyExpiryDates method :");
      return false;
    } 
  }
  
  public JsonNode calculatePolicyDatesForNewVehicle(JsonNode inputReqNode) {
    try {
      Calendar cal = new GregorianCalendar();
      Date today = cal.getTime();
      String sysPolicyStartDate = this.dateFormat.format(today);
      cal.add(1, 5);
      cal.add(7, -1);
      Date nextYear = cal.getTime();
      String sysPolicyEndDate = this.dateFormat.format(nextYear);
      this.systemPolicyDates.put("sysPolicyStartDate", sysPolicyStartDate);
      this.systemPolicyDates.put("sysPolicyEndDate", sysPolicyEndDate);
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in calculatePolicyDatesForNewVehicle method :");
    } 
    return ((ObjectNode)inputReqNode).put("systemPolicyStartDate", (JsonNode)this.systemPolicyDates);
  }
  
  public JsonNode calculateDateForPrePolicyExpired(JsonNode inputReqNode) throws ParseException {
    try {
      Calendar cal = new GregorianCalendar();
      cal.add(7, 3);
      Date today = cal.getTime();
      String sysPolicyStartDate = this.dateFormat.format(today);
      cal.add(1, 1);
      Date nextYear = cal.getTime();
      Calendar.getInstance().setTime(nextYear);
      cal.add(7, -1);
      nextYear = cal.getTime();
      String sysPolicyEndDate = this.dateFormat.format(nextYear);
      this.systemPolicyDates.put("sysPolicyStartDate", sysPolicyStartDate);
      this.systemPolicyDates.put("sysPolicyEndDate", sysPolicyEndDate);
      ((ObjectNode)inputReqNode).put("systemPolicyStartDate", (JsonNode)this.systemPolicyDates);
      if (!inputReqNode.get("vehicleInfo").has("PreviousPolicyStartDate")) {
        JsonNode vehicleInfo = calculatePrePolicyStartDate(inputReqNode.get("vehicleInfo"));
        ((ObjectNode)vehicleInfo).put("previousPolicyExpired", "Y");
        ((ObjectNode)inputReqNode).put("vehicleInfo", vehicleInfo);
      } 
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in calculateDateForPrePolicyExpired method :");
    } 
    return inputReqNode;
  }
  
  public long calculatePreviousPolicyExpiryDays(String previousPolicyExpiryDate) throws ParseException {
    try {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      Date expiryDate = sdf.parse(previousPolicyExpiryDate);
      cal.add(7, 3);
      Date date = cal.getTime();
      String format = sdf.format(date);
      date = sdf.parse(format);
      long days = date.getTime() - expiryDate.getTime();
      return TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      this.log.error(" calculate previous policy expity get error :" + previousPolicyExpiryDate, e);
      return 0L;
    } 
  }
}
