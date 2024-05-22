 package com.idep.proposal.exception.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 public class ExtendedJsonNode {
   JsonNode obj;
   
   public ExtendedJsonNode(JsonNode obj) throws Exception {
     if (obj == null)
       throw new Exception(); 
     this.obj = obj;
   }
   
   public ExtendedJsonNode(long asLong) {}
   
   public ExtendedJsonNode get(String key) throws Exception {
     try {
       return new ExtendedJsonNode(this.obj.get(key));
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   public ExtendedJsonNode get(int key) throws Exception {
     return new ExtendedJsonNode(this.obj.get(key));
   } public JsonNode getNode(String key) {
     return this.obj.get(key);
   }
   public String getKey(String key) throws Exception {
     try {
       return this.obj.get(key).asText();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public boolean asBoolean(String key) throws Exception {
     try {
       return this.obj.get(key).asBoolean();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public boolean isNull(String key) throws Exception {
     try {
       return this.obj.get(key).isNull();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   public JsonNode getJsonNode() {
     return this.obj;
   }
   public int asInt(String key) throws Exception {
     try {
       return this.obj.get(key).asInt();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public double asDouble(String key) throws Exception {
     try {
       return this.obj.get(key).asDouble();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public Long asLong(String key) throws Exception {
     try {
       return Long.valueOf(this.obj.get(key).asLong());
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public int findValueAsInt(String key) throws Exception {
     try {
       return this.obj.findValue(key).asInt();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public String asText(String key) throws Exception {
     try {
       return this.obj.get(key).asText();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   public boolean has(String key) {
     return this.obj.has(key);
   }
   public String findValueAsText(String key) throws Exception {
     try {
       return this.obj.findValue(key).asText();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode findValue(String key) throws Exception {
     try {
       return this.obj.findValue(key);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode put(String key, String value) throws Exception {
     try {
       return (JsonNode)((ObjectNode)this.obj).put(key, value);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode putInt(String key, int value) throws Exception {
     try {
       return (JsonNode)((ObjectNode)this.obj).put(key, value);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode putDouble(String key, double value) throws Exception {
     try {
       return (JsonNode)((ObjectNode)this.obj).put(key, value);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode putLong(String key, Long value) throws Exception {
     try {
       return (JsonNode)((ObjectNode)this.obj).put(key, value);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode put(String key, JsonNode value) throws Exception {
     try {
       return ((ObjectNode)this.obj).put(key, value);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode put(String key, ExtendedJsonNode value) throws Exception {
     try {
       return ((ObjectNode)this.obj).put(key, value.getJsonNode());
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   
   public JsonNode remove(String key) throws Exception {
     try {
       return ((ObjectNode)this.obj).remove(key);
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
   public ArrayNode iterator() {
     return (ArrayNode)this.obj;
   } public String toString() {
     return this.obj.toString();
   }
   public String classType(String key) throws Exception {
     try {
       return this.obj.get(key).asText().getClass().getSimpleName();
     } catch (Exception e) {
       throw new Exception(key);
     } 
   }
 }


