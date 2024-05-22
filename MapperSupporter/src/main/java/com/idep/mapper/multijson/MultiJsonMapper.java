package com.idep.mapper.multijson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MultiJsonMapper {
  ObjectMapper objectMapper = new ObjectMapper();
  
  public ObjectNode getMultiJSonFormat(JsonNode inputNode, JsonNode configNode) {
    ObjectNode finalJsonNode = this.objectMapper.createObjectNode();
    for (JsonNode mulJsonNode : configNode.get("constantNodeList")) {
      if (inputNode.has(mulJsonNode.get("fieldValue").textValue()))
        finalJsonNode.put(mulJsonNode.get("fieldKey").textValue(), inputNode.get(mulJsonNode.get("fieldValue").textValue())); 
    } 
    for (JsonNode mulJsonNode : configNode.get("multiJsonNodeFieldList")) {
      String nodeName = mulJsonNode.get("nodeName").textValue();
      ObjectNode childJsonNode = this.objectMapper.createObjectNode();
      for (JsonNode node : mulJsonNode.get("fields")) {
        if (inputNode.has(node.get("fieldValue").textValue()))
          childJsonNode.put(node.get("fieldKey").textValue(), inputNode.get(node.get("fieldValue").textValue())); 
      } 
      if (mulJsonNode.has("childNode"))
        for (JsonNode subChildNode : mulJsonNode.get("childNode").get("nodeList")) {
          ObjectNode subChild = this.objectMapper.createObjectNode();
          String childNodeName = subChildNode.get("nodeName").textValue();
          for (JsonNode node : subChildNode.get("fields")) {
            if (inputNode.has(node.get("fieldValue").textValue()))
              subChild.put(node.get("fieldKey").textValue(), inputNode.get(node.get("fieldValue").textValue())); 
          } 
          childJsonNode.put(childNodeName, (JsonNode)subChild);
        }  
      finalJsonNode.put(nodeName, (JsonNode)childJsonNode);
    } 
    return finalJsonNode;
  }
}
