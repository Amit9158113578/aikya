package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExceptionResponse;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.exception.processor.ExtendedJsonNode;
import com.idep.proposal.util.Utils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class UpdateStageProcessor
    implements Processor {
  Logger log = Logger.getLogger(UpdateStageProcessor.class);

  public void process(Exchange exchange) throws Exception {
    try {
      JsonNode requestNode = Utils.mapper.readTree(exchange.getIn().getBody().toString());
      JsonNode configDoc = Utils.mapper.readTree(exchange.getProperty("configDoc").toString());
      exchange.getIn().setHeader("webserviceType",
          configDoc.get(requestNode.findValue("carrierId").asText()).get("webserviceType").asText());
      exchange.getIn().setHeader("requestURL", configDoc.get(requestNode.findValue("carrierId").asText())
          .get(exchange.getIn().getHeader("webserviceType").toString()).asText());

      String loopIndex = exchange.getProperty("CamelLoopIndex").toString();
      String stage = null;
      if (requestNode.get("request").has("insuranceDetails")
          && requestNode.get("request").get("insuranceDetails").has("insurerId")
          && requestNode.get("request").get("insuranceDetails").get("insurerId").asText().equalsIgnoreCase("25")
          && requestNode
              .get("request").has("carrierId")
          && requestNode.get("request").get("carrierId").asText() == "25") {
        stage = configDoc.get(requestNode.findValue("carrierId").asText()).get("renewalProposalStages").get(loopIndex)
            .asText();
      } else {
        stage = configDoc.get(requestNode.findValue("carrierId").asText()).get("proposalStages").get(loopIndex)
            .asText();
      }

      if (configDoc.get(requestNode.findValue("carrierId").asText()).has(stage + "MethodType")) {

        configDoc.get(requestNode.findValue("carrierId").asText())
            .get(configDoc.get(requestNode.findValue("carrierId").asText()).get(stage + "MethodType").asText())
            .asText();
        String mType = configDoc.get(requestNode.findValue("carrierId").asText()).get(stage + "MethodType").asText();

        exchange.getIn().setHeader("webserviceType", mType);
        String serviceType = configDoc.get(requestNode.findValue("carrierId").asText()).get(stage + "MethodType")
            .asText();

        exchange.getIn().setHeader("requestURL",
            configDoc.get(requestNode.findValue("carrierId").asText()).get(serviceType).asText());
      }
      if (stage.isEmpty() || stage == null || stage.equalsIgnoreCase("NA")) {
        exchange.getIn().setBody((new ExceptionResponse()).configDocMissing(
            "proposal stage field not found for carrierId :" + requestNode.findValue("carrierId").asText()));
        throw new ExecutionTerminator();
      }
      ((ObjectNode) requestNode).put("stage", stage);
      exchange.setProperty("stage", stage);
      exchange.getIn().setBody(requestNode);
    } catch (Exception e) {
      ExtendedJsonNode failure = (new ExceptionResponse())
          .failure("Exception in Update Stage Processor request processor :" + e.toString());
      exchange.getIn().setBody(failure);
      throw new ExecutionTerminator();
    }
  }
}
