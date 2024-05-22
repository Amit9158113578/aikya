package com.idep.listener.core;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.error.response.ErrorResponse;
import com.idep.listener.services.ICarProductValidation;
import com.idep.listener.utils.QuoteListenerConstants;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class CarProductValidation implements ICarProductValidation {
  Logger log = Logger.getLogger(CarProductValidation.class.getName());
  
  ProductMetaData productMetaData = new ProductMetaData();
  
  CBService productService = CBInstanceProvider.getProductConfigInstance();
  
  public boolean validateCarProduct(JsonNode product, JsonNode quoteParamNode, JsonNode vehicleParamNode) {
    boolean validProductFlag = false;
    try {
      validProductFlag = validateRTO(vehicleParamNode.get("RTOCode").textValue(), 
          product.get("productId").toString());
      if (validProductFlag) {
        if (product.has("policyTypeSupport"))
          validProductFlag = validatePolicyType(quoteParamNode.get("policyType").asText(), product.get("policyTypeSupport")); 
        if (validProductFlag) {
          validProductFlag = validateVehicleAge(quoteParamNode.get("vehicleAge").doubleValue(), 
              product.get("maxAllowedVehicleAge").doubleValue());
          if (validProductFlag)
            if (vehicleParamNode.has("previousPolicyExpired") && vehicleParamNode.get("previousPolicyExpired").textValue().equalsIgnoreCase("Y"))
              validProductFlag = isExpiredPolicyAllowed(product.get("isExpiredPolicyAllowed").textValue());  
          if (validProductFlag) {
            this.log.info("quoteParamNode :" + quoteParamNode);
            if (quoteParamNode.get("ownedBy").asText().equalsIgnoreCase("Organization")) {
              this.log.info("product :" + product);
              if (product.has("vehicleOwneredBy")) {
                validProductFlag = true;
              } else {
                validProductFlag = false;
              } 
            } 
          } 
        } 
      } 
      return validProductFlag;
    } catch (Exception e) {
      e.printStackTrace();
      return validProductFlag;
    } 
  }
  
  public boolean isExpiredPolicyAllowed(String productExpiredPolicyFlag) {
    if (productExpiredPolicyFlag.equals("Y"))
      return true; 
    return false;
  }
  
  public boolean validateRTO(String RTO, String productId) {
    JsonDocument jsondoc = this.productService.getDocBYId("BlockedRTO-" + RTO);
    if (jsondoc != null) {
      if (((JsonObject)jsondoc.content()).containsKey(productId))
        return false; 
      return true;
    } 
    return true;
  }
  
  public boolean validateVehicleAge(double vehicleAge, double maxAllowedVehicleAge) {
    if (vehicleAge <= maxAllowedVehicleAge)
      return true; 
    return false;
  }
  
  public boolean validatePolicyType(String policyType, JsonNode policyTypeSupport) {
    boolean flag = false;
    for (JsonNode prodPolicyType : policyTypeSupport) {
      if (prodPolicyType.get("policyType").asText().equalsIgnoreCase(policyType)) {
        flag = true;
        break;
      } 
      flag = false;
    } 
    return flag;
  }
  
  public boolean isRTOBlocked(String rtoCode, String carrierId) {
    JsonObject rtoDetails = this.productMetaData.getBlockedRTODetails(rtoCode);
    if (rtoDetails != null && rtoDetails.containsKey(carrierId))
      return true; 
    return false;
  }
  
  public boolean isVehicleBlocked(String variantId, String carrierId) {
    this.log.info("isVehicleBlocked variantId :" + variantId + "and carrierId :" + carrierId);
    JsonObject vehicleDetails = null;
    vehicleDetails = this.productMetaData.getVehicleDetails(variantId, carrierId);
    if (vehicleDetails != null) {
      if (vehicleDetails.containsKey("vehicleBlocked"))
        if (vehicleDetails.get("vehicleBlocked").toString().equalsIgnoreCase("Y")) {
          if (vehicleDetails.containsKey("isRTOBlocked") && vehicleDetails.get("isRTOBlocked").toString().equalsIgnoreCase("N"))
            return false; 
          if (vehicleDetails.containsKey("isRTOBlocked") && vehicleDetails.get("isRTOBlocked").toString().equalsIgnoreCase("NN"))
            return false; 
          return true;
        }  
    } else {
      this.log.info("vehicle details not found for varient id :" + variantId + "-" + carrierId);
      return true;
    } 
    return false;
  }
  
  public ObjectNode isRiderBlocked(ObjectNode input, JsonNode vehicleDetailsNode, JsonNode rtoDetailsNode) {
    String blockedRider = "";
    String rtoBlockedRiderIds = "";
    String vehicleBlockedRiderIds = "";
    if (vehicleDetailsNode.has("blockedRiders") && vehicleDetailsNode.get("blockedRiders").asText().trim().length() >= 1) {
      vehicleBlockedRiderIds = vehicleDetailsNode.get("blockedRiders").asText();
      blockedRider = String.valueOf(vehicleBlockedRiderIds) + ",";
    } 
    if (rtoDetailsNode != null)
      if (rtoDetailsNode.has("blockedRiders") && rtoDetailsNode.get("blockedRiders").asText().trim().length() >= 1) {
        rtoBlockedRiderIds = rtoDetailsNode.get("blockedRiders").asText();
        blockedRider = String.valueOf(blockedRider) + rtoBlockedRiderIds;
      }  
    if (blockedRider.equals(""))
      return input; 
    if (blockedRider.endsWith(","))
      blockedRider = blockedRider.substring(0, blockedRider.length() - 1); 
    String[] blockedRiderIds = blockedRider.split(",");
    ArrayNode requestedRiders = (ArrayNode)input.get("quoteParam").get("riders");
    for (int j = 0; j < requestedRiders.size(); j++) {
      JsonNode rider = requestedRiders.get(j);
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = blockedRiderIds).length, b = 0; b < i; ) {
        String riderId = arrayOfString[b];
        if (rider.get("riderId").toString().equals(riderId))
          requestedRiders.remove(j); 
        b++;
      } 
    } 
    return input;
  }
  
  public String findMaritalStatus(String gender, String maritalStatus) {
    String maritalStatusWidow = null;
    if (maritalStatus.equals("WIDOW(ER)"))
      if (gender.equals("Male")) {
        maritalStatusWidow = "WIDOWER";
      } else {
        maritalStatusWidow = "WIDOW";
      }  
    return maritalStatusWidow;
  }
  
  public boolean isVarientBlockedByRTO(JsonObject vehicleDetailsNode, JsonObject rtoDetailsNode) {
    if (vehicleDetailsNode.containsKey("vehicleBlocked"))
      if (vehicleDetailsNode.get("vehicleBlocked").toString().equalsIgnoreCase("Y")) {
        if (rtoDetailsNode.containsKey("vehicleRTOBlocked")) {
          if (rtoDetailsNode.get("vehicleRTOBlocked").toString().equalsIgnoreCase("N")) {
            this.log.info("rtoDetailsNode false:" + rtoDetailsNode);
            return false;
          } 
          if (rtoDetailsNode.get("vehicleRTOBlocked").toString().equalsIgnoreCase("YY") && vehicleDetailsNode.get("isRTOBlocked").toString().equalsIgnoreCase("NN")) {
            this.log.info("rtoDetailsNode false:" + rtoDetailsNode);
            return false;
          } 
          return true;
        } 
        return true;
      }  
    return false;
  }
  
  public ObjectNode validate(ObjectNode input, String carrierId) {
    try {
      JsonObject validationDetails = this.productMetaData.getCarValidationDetails(carrierId, input.get("productInfo").get("productId").asText());
      if (validationDetails == null) {
        this.log.info("No validations available for this product in the system");
        return input;
      } 
      List<Map<String, Object>> validationList = (List<Map<String, Object>>)validationDetails.toMap().get("validations");
      for (Map<String, Object> validation : validationList) {
        if ((Integer)validation.get("validationId") == QuoteListenerConstants.V_BLOCKEDVEHICLE) {
          if (isVehicleBlocked(input.get("vehicleInfo").get("variantId").asText(), carrierId)) {
            this.log.info("vehicle blocked by insurance company");
            return (new ErrorResponse()).validationRes(carrierId, input.findValue("uniqueKey").asText(), "vehicle blocked from insurance company");
          } 
          continue;
        } 
        if ((Integer)validation.get("validationId") == QuoteListenerConstants.V_BLOCKEDRTO)
          if (input.get("carrierRTOInfo").has("isBlocked"))
            if (input.get("carrierRTOInfo").get("isBlocked").textValue().equals("Y"))
              return (new ErrorResponse()).validationRes(carrierId, input.findValue("uniqueKey").asText(), "rto blocked from insurance company");   
      } 
      if (input.get("vehicleInfo").has("previousPolicyExpired"))
        if (input.get("vehicleInfo").get("previousPolicyExpired").textValue().equals("Y") && 
          input.get("carrierRTOInfo").has("isPolicyExpired"))
          if ((!input.get("carrierRTOInfo").get("isPolicyExpired").isNull() || 
            input.get("carrierRTOInfo").get("isPolicyExpired").textValue() != "") && 
            input.get("carrierRTOInfo").get("isPolicyExpired").textValue().equals("Y"))
            this.log.info("Kotak Bike is policy Expired Validation :");   
    } catch (NullPointerException e) {
      this.log.error("error at validate method:");
      return (new ErrorResponse()).nullPointerException(carrierId, input.findValue("uniqueKey").asText(), String.valueOf(e.getMessage()) + " at :" + Thread.currentThread().getStackTrace()[1].getClassName());
    } 
    this.log.info("validate successfull..");
    return input;
  }
}
