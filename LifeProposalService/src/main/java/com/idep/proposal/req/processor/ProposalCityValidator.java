package com.idep.proposal.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ProposalCityValidator
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(ProposalCityValidator.class.getName());
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange)
  {
    try
    {
      String proposalReq = (String)exchange.getIn().getBody(String.class);
      JsonNode requestNode = this.objectMapper.readTree(proposalReq);
      
      this.log.info("Request inside CityValidator" + requestNode);
      if (requestNode.has("addressDetails"))
      {
        if (requestNode.get("addressDetails").get("communicationAddress").has("pincode"))
        {
          this.log.info("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("addressDetails").get("communicationAddress").get("pincode").asText());
          this.log.info("commu pinCode for city:" + requestNode.get("addressDetails").get("communicationAddress").get("city").asText() + "::" + requestNode.get("addressDetails").get("communicationAddress").get("pincode").asText());
          JsonNode healthZoneDetailsNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("addressDetails").get("communicationAddress").get("pincode").asText()).content()).toString());
          if (healthZoneDetailsNode != null)
          {
            ((ObjectNode)requestNode.get("addressDetails").get("communicationAddress")).put("city", healthZoneDetailsNode.get("city").asText());
          }
          else
          {
            this.log.error("This Address unavilable for the product!");
            throw new ExecutionTerminator();
          }
        }
        if (requestNode.get("addressDetails").get("permanentAddress").has("pincode"))
        {
          this.log.info("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("addressDetails").get("permanentAddress").get("pincode").asText());
          this.log.info("perm pinCode for city:" + requestNode.get("addressDetails").get("permanentAddress").get("city").asText() + "::" + requestNode.get("addressDetails").get("permanentAddress").get("pincode").asText());
          this.log.info("perm state for city:" + requestNode.get("addressDetails").get("permanentAddress").get("state").asText());
          JsonNode healthZoneDetailsNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("addressDetails").get("permanentAddress").get("pincode").asText()).content()).toString());
          if (healthZoneDetailsNode != null)
          {
            ((ObjectNode)requestNode.get("addressDetails").get("permanentAddress")).put("city", healthZoneDetailsNode.get("city").asText());
            this.log.info("perm state for city:" + requestNode.get("addressDetails").get("permanentAddress").get("state").asText());
          }
          else
          {
            this.log.error("This Address unavilable for the product!");
            throw new ExecutionTerminator();
          }
        }
      }
      if (requestNode.has("nominationDetails"))
      {
        this.log.info("check for nominee");
        this.log.info("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("nominationDetails").get("nomineeAddressDetails").get("pincode").asText());
        if (requestNode.get("nominationDetails").get("nomineeAddressDetails").has("pincode"))
        {
          this.log.info("Appointee City Document: CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("nominationDetails").get("nomineeAddressDetails").get("pincode").asText());
          this.log.info("nom pinCode for city:" + requestNode.get("nominationDetails").get("nomineeAddressDetails").get("city").asText() + "::" + requestNode.get("nominationDetails").get("nomineeAddressDetails").get("pincode").asText());
          JsonNode healthZoneDetailsNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("nominationDetails").get("nomineeAddressDetails").get("pincode").asText()).content()).toString());
          if (healthZoneDetailsNode != null)
          {
            ((ObjectNode)requestNode.get("nominationDetails").get("nomineeAddressDetails")).put("city", healthZoneDetailsNode.get("city").asText());
          }
          else
          {
            this.log.error("This Address unavilable for the product!");
            throw new ExecutionTerminator();
          }
        }
        if (requestNode.get("nominationDetails").get("appointeeDetails").has("appointeeAddressDetails"))
        {
          this.log.info("Appointee City Document: CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("pincode").asText());
          JsonNode healthZoneDetailsNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("CityDetails-" + requestNode.get("carrierId").intValue() + "-" + requestNode.get("businessLineId") + "-" + requestNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("pincode").asText()).content()).toString());
          this.log.info("City:" + healthZoneDetailsNode.get("city").asText());
          if (healthZoneDetailsNode != null)
          {
            ((ObjectNode)requestNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")).put("city", healthZoneDetailsNode.get("city").asText());
          }
          else
          {
            this.log.error("This Address unavilable for the product!");
            throw new ExecutionTerminator();
          }
        }
      }
      this.log.info("Request after CityValidator" + requestNode);
      exchange.getIn().setBody(requestNode);
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "SERVICEINVOKE" + "|ERROR|CarrierDataLoader|", e);
    }
  }
}
