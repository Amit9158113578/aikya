 package com.idep.carquote.response;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.service.quote.cache.CarQuoteConfigCache;
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.ExchangePattern;
 import org.apache.camel.Processor;
 import org.apache.camel.ProducerTemplate;
 import org.apache.log4j.Logger;
 
 public class ResponseQueueProcessor implements Processor {
   Logger log = Logger.getLogger(ResponseQueueProcessor.class);
   
   ObjectMapper mapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode requestNode = this.mapper.readTree(exchange.getIn().getBody().toString());
       CamelContext context = exchange.getContext();
       ProducerTemplate template = context.createProducerTemplate();
       JsonNode input_request = this.mapper.readTree(exchange.getProperty("carQuoteInputRequest").toString());
       exchange.setPattern(ExchangePattern.InOnly);
       String carrierResQName = CarQuoteConfigCache.getcarQuoteDocCache().get("CarCarrierQList").get("carrierResQ").get(input_request.findValue("carrierId").asText()).textValue();
       String uri = "activemq:queue:" + carrierResQName;
       template.send(uri, exchange);
       exchange.getIn().setBody(requestNode);
     } catch (Exception e) {
       e.printStackTrace();
       this.log.error("error in Response Queue processor :");
     } 
   }
 }


