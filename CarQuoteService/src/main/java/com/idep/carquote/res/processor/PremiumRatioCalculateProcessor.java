 package com.idep.carquote.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import java.text.DecimalFormat;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PremiumRatioCalculateProcessor implements Processor {
   Logger log = Logger.getLogger(PremiumRatioCalculateProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   DecimalFormat decimalFormat = new DecimalFormat("##.#####");
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode resNode = this.objectMapper.readTree((String)exchange.getIn().getBody(String.class));
       this.log.debug("carrier response generated :  " + resNode);
       JsonNode quoteResult = resNode.get("data").get("quotes").get(0);
       double premium = 0.0D;
       double idv = 0.0D;
       if (quoteResult.has("netPremium"))
         premium = quoteResult.get("netPremium").asDouble(); 
       if (quoteResult.has("insuredDeclareValue"))
         idv = quoteResult.get("insuredDeclareValue").asDouble(); 
       double premiumRatio = 0.0D;
       if (premium > 0.0D && idv > 0.0D)
         premiumRatio = Double.parseDouble(this.decimalFormat.format(premium / idv)); 
       if (quoteResult.has("policyType") && quoteResult.get("policyType").equals("new") && quoteResult
         .has("ownDamagePolicyTerm"))
         premiumRatio /= quoteResult.get("ownDamagePolicyTerm").asInt(); 
       this.log.info("Carrier Response in added Premium Ratio : " + premiumRatio);
       ((ObjectNode)quoteResult).put("premiumRatio", premiumRatio);
       ((ObjectNode)resNode.get("data").get("quotes").get(0)).putAll((ObjectNode)quoteResult);
       exchange.getIn().setBody(resNode);
     } catch (Exception e) {
       this.log.error("unable to calculate premium Ratio: ", e);
     } 
   }
 }


