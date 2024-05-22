package com.idep.pospservice.leads;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class FetchLeadReqProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(FetchLeadReqProcessor.class.getName());
  JsonNode errorNode;
  static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
  
  public void process(Exchange data)
    throws Exception
  {
    try
    {
      JsonNode request = this.objectMapper.readTree(data.getIn().getBody().toString());
      this.log.info("parameters to jsonNode" + request);
      
      String query = "select leads.leadRequest.contactInfo.firstName,leads.leadRequest.contactInfo.lastName,leads.leadRequest.contactInfo.emailId,leads.leadRequest.contactInfo.mobileNumber,leads.leadRequest.quoteParam.quoteType as LOB ,leads.leadRequest.requestSource as leadSource from PospData as leads where documentType =  'TransactionDetails' and EventType = 'Lead'  order by LeadCreatedDate desc";
      this.log.info("query" + query);
      List<Map<String, Object>> queryoutput = PospData.executeQuery(query);
      if (queryoutput.size() > 0)
      {
        this.log.info("output size" + queryoutput.size());
        String output = this.objectMapper.writeValueAsString(queryoutput);
        this.log.info("Update Proposal Caar Document : " + output);
        JsonNode customerDetails = this.objectMapper.readTree(output);
        this.log.info("....reqNode" + customerDetails);
        for (int i = 0; i < queryoutput.size(); i++)
        {
          this.log.info("in forrr" + customerDetails.get(i));
          if ((customerDetails.get(i).has("LOB")) && (customerDetails.get(i).get("LOB") != null))
          {
            this.log.info("quote type got" + customerDetails.get(i));
            if (customerDetails.get(i).findValue("LOB").asInt() == 1) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Life");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 2) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Bike");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 3) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Car");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 4) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Health");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 5) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Travel");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 6) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "CriticalIllness");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 7) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "Home");
            } else if (customerDetails.get(i).findValue("LOB").asInt() == 8) {
              ((ObjectNode)customerDetails.get(i)).put("lob", "PersonalAccident");
            } else {
              this.log.info("businessLineId is not matched...");
            }
            this.log.info("customerDetails" + customerDetails);
          }
        }
        ObjectNode objectNode = this.objectMapper.createObjectNode();
        objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").asInt());
        objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
        objectNode.put("data", customerDetails);
        data.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
      }
      else
      {
        this.log.info("no documnets with this type");
      }
    }
    catch (Exception e)
    {
      this.log.error("Unable to process : ", e);
      ObjectNode objectNode = this.objectMapper.createObjectNode();
      objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("failureCode").asInt());
      objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("failureMessage").asText());
      objectNode.put("data", this.errorNode);
      data.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
      throw new ExecutionTerminator();
    }
  }
}
