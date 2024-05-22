package com.idep.request.validator;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

public class Validator {
  Logger log = Logger.getLogger(Validator.class);
  
  public String verifyFieldCount(JsonNode request, JsonNode schema) {
    String key = "";
    String keyNotExist = "request field not found in schema :";
    Iterator<String> it = request.fieldNames();
    while (it.hasNext()) {
      key = it.next();
      if (schema.get(key) == null)
        return String.valueOf(keyNotExist) + key; 
      if (schema.get(key).has("requestNode")) {
        JsonNode childNode = request.get(key);
        Iterator<String> child = childNode.fieldNames();
        while (child.hasNext()) {
          key = child.next();
          if (schema.get(key) == null)
            return String.valueOf(keyNotExist) + key; 
        } 
      } 
    } 
    return "success";
  }
  
  public String verifyMinimumFields(JsonNode request, JsonNode schema) {
    Iterator<Map.Entry<String, JsonNode>> fields = schema.fields();
    boolean isFieldNotPresent = false;
    String name = null;
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      name = field.getKey();
      JsonNode value = field.getValue();
      if (value.get("isMandatory") != null) {
        boolean isMandatory = value.get("isMandatory").asBoolean();
        if (isMandatory) {
          if (value.has("requestNode")) {
            if (!value.get("type").asText().equals("Json"))
              if (request.findValue(value.get("requestNode").asText()).get(name) == null || request.findValue(value.get("requestNode").asText()).get(name).asText().length() == 0) {
                isFieldNotPresent = true;
                name = String.valueOf(name) + " in " + value.get("requestNode").asText();
                break;
              }  
          } else if (request.get(name) == null || request.get(name).asText().length() == 0) {
            isFieldNotPresent = true;
            break;
          } 
          if (!value.has("requestNode"))
            if (request.findValue(name).asText().isEmpty() || request.get(name).asText().length() == 0) {
              isFieldNotPresent = true;
              break;
            }  
        } 
      } 
    } 
    if (isFieldNotPresent)
      return name; 
    return null;
  }
  
  public boolean verifyDate(String key, String formate, JsonNode request) {
    String date = request.findValue(key).asText();
    SimpleDateFormat sdf = new SimpleDateFormat(formate);
    sdf.setLenient(false);
    try {
      Date date1 = sdf.parse(date);
    } catch (ParseException pe) {
      pe.printStackTrace();
      return false;
    } 
    return true;
  }
  
  public boolean verifyString(String key, JsonNode fieldData, JsonNode request) {
    if (verifyDataType(key, request, "TextNode")) {
      String field = request.findValue(key).asText();
      boolean isTextOnly = (fieldData.get("isTextOnly") != null) ? fieldData.get("isTextOnly").asBoolean() : false;
      return !(isTextOnly && !verifyPattern(field));
    } 
    return false;
  }
  
  public boolean verifyPattern(String input) {
    String onlyTextRegex = "^[a-zA-Z ]*$";
    boolean result = input.matches(onlyTextRegex);
    return result;
  }
  
  public boolean verifyNumber(String key, JsonNode fieldData, JsonNode request) {
    return verifyDataType(key, request, "IntNode");
  }
  
  public boolean verifyBoolean(String key, JsonNode fieldData, JsonNode request) {
    return verifyDataType(key, request, "BooleanNode");
  }
  
  public boolean verifyArray(String key, JsonNode fieldData, JsonNode request) {
    return verifyDataType(key, request, "ArrayNode");
  }
  
  public boolean verifyJson(String key, JsonNode fieldData, JsonNode request) {
    return verifyDataType(key, request, "ObjectNode");
  }
  
  public boolean verifyDouble(String key, JsonNode fieldData, JsonNode request) {
    return verifyDataType(key, request, "DoubleNode");
  }
  
  public boolean verifyMaxLength(String key, int input) {
    return (key.length() <= input);
  }
  
  public boolean verifyMinLength(String key, int input) {
    return (key.length() >= input);
  }
  
  public boolean verifyJSONArray() {
    return false;
  }
  
  public boolean verifyDataType(String key, JsonNode node, String Type) {
    Object field = node.findValue(key);
    String nodeType = field.getClass().getSimpleName();
    if (nodeType.equalsIgnoreCase(Type))
      return true; 
    return false;
  }
}
