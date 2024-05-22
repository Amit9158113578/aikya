package com.idep.error.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ErrorResponse {
  ObjectMapper objectMapper = new ObjectMapper();
  
  ObjectNode node = this.objectMapper.createObjectNode();
  
  public ObjectNode vehicleDetailsNotFound(String carrierId, String uniqueId) {
    this.node.put("responseCode", "P365RES110");
    this.node.put("message", "vehicle details not found for carrier");
    this.node.put("data", "error");
    this.node.put("carrierId", carrierId);
    this.node.put("uniqueKey", uniqueId);
    return this.node;
  }
  
  public ObjectNode rtoDetailsNotFound(String carrierId, String uniqueId) {
    this.node.put("responseCode", "P365RES110");
    this.node.put("message", "rto details not found for carrier");
    this.node.put("data", "error");
    this.node.put("carrierId", carrierId);
    this.node.put("uniqueKey", uniqueId);
    return this.node;
  }
  
  public ObjectNode nullPointerException(String carrierId, String uniqueId, String exception) {
    this.node.put("responseCode", "P365RES101");
    this.node.put("message", exception);
    this.node.put("carrierId", carrierId);
    this.node.put("uniqueKey", uniqueId);
    this.node.put("data", "");
    return this.node;
  }
  
  public ObjectNode validationRes(String carrierId, String uniqueId, String msg) {
    this.node.put("responseCode", "P365RES101");
    this.node.put("message", msg);
    this.node.put("carrierId", carrierId);
    this.node.put("uniqueKey", uniqueId);
    this.node.put("data", "");
    return this.node;
  }
}
