package com.idep.lifequote.req.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class KotakRequestTransformer implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(KotakRequestTransformer.class.getName());
  
  public void process(Exchange exchange)
    throws Exception
  {
    String carrierResponse = (String)exchange.getIn().getBody(String.class);
    JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
    JsonNode RiderNode = this.objectMapper.createObjectNode();
    ArrayNode riderList = this.objectMapper.createArrayNode();
    //this.log.info("carrier Response:" + carrierResponse);
    
    String inputReq = exchange.getProperty("inputRequest").toString();
    JsonNode inputRequest = this.objectMapper.readTree(inputReq);
    if (carrierResNode.get("quoteResult").get("TraditionalRating").get("Premium").get("CIBRiderModalPremium").asDouble() != 0.0D)
    {
    	//monthly check
      this.log.debug("Frequency: " + carrierResNode.get("quoteResult").get("TraditionalRating").get("BaseCover").get("PremiumPaymentFrequency").asText());
      if (carrierResNode.get("quoteResult").get("TraditionalRating").get("BaseCover").get("PremiumPaymentFrequency").asText().equalsIgnoreCase("Monthly"))
      {
        if (inputRequest.get("quoteParam").get("riders").size() != 0)
        {
          Integer riderID = Integer.valueOf(inputRequest.get("productInfo").get("riders").get(0).get("riderId").intValue());
          String riderName = inputRequest.get("productInfo").get("riders").get(0).get("riderName").asText();
          
          ((ObjectNode)RiderNode).put("riderMonthlyPremium", carrierResNode.get("quoteResult").get("TraditionalRating").get("Premium").get("CIBRiderModalPremium").asText());
          ((ObjectNode)RiderNode).put("riderId", riderID);
          ((ObjectNode)RiderNode).put("riderName", riderName);
          ((ObjectNode)RiderNode).put("riderSumAssured", "");
          ((ObjectNode)RiderNode).put("riderType", "selected");
          riderList.add(RiderNode);
          this.log.debug("Rider Node with riders: " + riderList);
          ((ObjectNode)carrierResNode).put("riderList", riderList);
        }
      }
      else
      {//annual check
    	  if (inputRequest.get("quoteParam").get("riders").size() != 0)
          {
            Integer riderID = Integer.valueOf(inputRequest.get("productInfo").get("riders").get(0).get("riderId").intValue());
            String riderName = inputRequest.get("productInfo").get("riders").get(0).get("riderName").asText();
            
        ((ObjectNode)RiderNode).put("riderPremiumAmount", carrierResNode.get("quoteResult").get("TraditionalRating").get("Premium").get("CIBRiderModalPremium").asText());
        ((ObjectNode)RiderNode).put("riderId", riderID);
        ((ObjectNode)RiderNode).put("riderName", riderName);
        ((ObjectNode)RiderNode).put("riderSumAssured", "");
        ((ObjectNode)RiderNode).put("riderType", "selected");
        riderList.add(RiderNode);
        this.log.info("Rider Node with riders: " + riderList);
        ((ObjectNode)carrierResNode).put("riderList", riderList);
          }
      }
    }
    else
    {
      this.log.info("Rider Not Selected ");
      this.log.debug("Rider Node with riders: " + riderList);
      ((ObjectNode)carrierResNode).put("riderList", riderList);
    }
    exchange.getIn().setBody(carrierResNode);
  }
}
