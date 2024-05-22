package com.idep.request.validate.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.request.validator.Validator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

public class ValidateJsonImpl {
  ObjectMapper objectMapper = new ObjectMapper();
  
  Validator validateField = new Validator();
  
  Logger log = Logger.getLogger(ValidateJsonImpl.class);
  
  public JsonNode parseJson(JsonNode request, String Schema) throws JsonProcessingException, IOException {
    try {
      JsonNode schema = this.objectMapper.readTree(Schema).get("schema");
      if (schema != null)
        return validateRequest(request, schema); 
    } catch (Exception ps) {
      ps.printStackTrace();
      this.log.error("exception in parse json method :");
    } 
    return null;
  }
  
  public JsonNode parseCarrierJson(JsonNode request, String Schema) throws JsonProcessingException, IOException {
    try {
      JsonNode schema = this.objectMapper.readTree(Schema).get("schema");
      if (schema != null)
        return validateCarrierRequest(request, schema); 
    } catch (Exception ps) {
      ps.printStackTrace();
      this.log.error("exception in parse json method :");
    } 
    return null;
  }
  
  private JsonNode validateRequest(JsonNode request, JsonNode schema) {
    ObjectNode result = this.objectMapper.createObjectNode();
    try {
      if (this.validateField.verifyFieldCount(request, schema).equals("success")) {
        String response = this.validateField.verifyMinimumFields(request, schema);
        if (response == null) {
          response = verifyFormats(request, schema);
          if (response == null) {
            result.put("responseCode", "P365RES100");
            result.put("message", "Schema Validated");
            result.put("data", "success");
          } else {
            result.put("responseCode", "P365RES112");
            result.put("message", response);
            result.put("data", "failed");
          } 
        } else {
          result.put("responseCode", "P365RES109");
          result.put("message", "Request does not contain mandatory field " + response);
          result.put("data", "failed");
        } 
      } else {
        result.put("responseCode", "P365RES111");
        result.put("message", this.validateField.verifyFieldCount(request, schema));
        result.put("data", "failed");
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      this.log.error("exception in validate request method :");
    } 
    return (JsonNode)result;
  }
  
  private JsonNode validateCarrierRequest(JsonNode request, JsonNode schema) {
    ObjectNode result = this.objectMapper.createObjectNode();
    try {
      String response = this.validateField.verifyMinimumFields(request, schema);
      if (response == null) {
        response = verifyFormats(request, schema);
        if (response == null) {
          result.put("responseCode", "P365RES100");
          result.put("message", "Schema Validated");
          result.put("data", "success");
        } else {
          result.put("responseCode", "P365RES112");
          result.put("message", response);
          result.put("data", "failed");
        } 
      } else {
        result.put("responseCode", "P365RES109");
        result.put("message", "Request does not contain mandatory field " + response);
        result.put("data", "failed");
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      this.log.error("exception in validate request method :");
    } 
    return (JsonNode)result;
  }
  
  private String verifyFormats(JsonNode request, JsonNode schema) {
    try {
      boolean checkerResponse = true;
      String key = null;
      Iterator<Map.Entry<String, JsonNode>> entryItr = schema.fields();
      String responseStatus = "";
      while (entryItr.hasNext()) {
        Map.Entry<String, JsonNode> entry = entryItr.next();
        key = entry.getKey();
        JsonNode fieldData = entry.getValue();
        if (!fieldData.get("isMandatory").asBoolean() && !request.has(key))
          continue; 
        if (fieldData.get("maxLength") != null) {
          checkerResponse = checkMaxLength(request.findValue(key).asText(), fieldData.get("maxLength").asInt());
          if (!checkerResponse)
            if (fieldData.has("crossFieldValidate")) {
              if (request.findValue(fieldData.get("crossFieldValidate").asText()).isNull() || request.findValue(fieldData.get("crossFieldValidate").asText()).asText().length() == 0)
                return responseStatus = String.valueOf(fieldData.get("crossFieldValidate").asText()) + " not be null/empty if " + key + " is more than " + fieldData.get("maxLength").asInt(); 
              checkerResponse = true;
            } else {
              return responseStatus = String.valueOf(key) + " must not exceed maximum limit of " + fieldData.get("maxLength").asInt() + " characters";
            }  
        } 
        if (checkerResponse && fieldData.get("minLength") != null) {
          checkerResponse = checkMinLength(request.findValue(key).asText(), fieldData.get("minLength").asInt());
          if (!checkerResponse)
            return responseStatus = String.valueOf(key) + " must have minumum " + fieldData.get("minLength").asInt() + " characters"; 
        } 
        if (checkerResponse && fieldData.get("type") != null) {
          checkerResponse = checkDataTypes(key, fieldData, request);
          if (!checkerResponse)
            return responseStatus = "Datatype mismatch for " + key + ". Expected valid " + fieldData.get("type").asText() + " format"; 
        } else if (checkerResponse) {
          checkerResponse = this.validateField.verifyString(key, fieldData, request);
          if (!checkerResponse)
            return responseStatus = "Datatype mismatch for " + key; 
        } 
        if (!checkerResponse)
          break; 
      } 
      if (!checkerResponse)
        return responseStatus; 
    } catch (Exception ex) {
      ex.printStackTrace();
      this.log.error("error in verifyFormats methods :");
    } 
    return null;
  }
  
  private boolean checkMinLength(String key, int lengthToCheck) {
    return this.validateField.verifyMinLength(key, lengthToCheck);
  }
  
  private boolean checkMaxLength(String key, int lengthToCheck) {
    return this.validateField.verifyMaxLength(key, lengthToCheck);
  }
  
  private boolean checkDataTypes(String key, JsonNode fieldData, JsonNode request) {
    String type = fieldData.get("type").asText();
    if (type.equalsIgnoreCase("String"))
      return this.validateField.verifyString(key, fieldData, request); 
    if (type.equalsIgnoreCase("Boolean"))
      return this.validateField.verifyBoolean(key, fieldData, request); 
    if (type.equalsIgnoreCase("Array"))
      return this.validateField.verifyArray(key, fieldData, request); 
    if (type.equalsIgnoreCase("Json"))
      return this.validateField.verifyJson(key, fieldData, request); 
    if (type.equalsIgnoreCase("Double"))
      return this.validateField.verifyDouble(key, fieldData, request); 
    if (type.equalsIgnoreCase("Date"))
      return this.validateField.verifyDate(key, fieldData.get("formate").asText(), request); 
    if (type.equalsIgnoreCase("Number"))
      return this.validateField.verifyNumber(key, fieldData, request); 
    return false;
  }
}
