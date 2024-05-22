package com.idep.healthquestion.req.processor;



import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class PersonalAccidentQuestionReqProcessor implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode questionPlanConfig = null;
  CBService productService = CBInstanceProvider.getProductConfigInstance();
  CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
  Logger log = Logger.getLogger(PersonalAccidentQuestionReqProcessor.class.getName());
  List<Map<String, Object>> PAQSDetailsList;
  
  public void process(Exchange exchange)
  {
    try
    {
      this.log.info("Inside Personal Accident QuestionReqProcessor");
      String quotedata = exchange.getIn().getBody().toString();
      JsonNode masterReqNode = this.objectMapper.readTree(quotedata);
      this.log.info("masterReqNode::" + masterReqNode);
      this.log.info("quotedata::" + quotedata);
      String PAQS_Details = null;
      if (this.questionPlanConfig == null) {
        try
        {
          JsonDocument questionConfigDoc = this.serviceConfig.getDocBYId("PersonalAccidentPlanQuestionConfig");
          this.log.info("questionConfigDoc" + questionConfigDoc);
          if (questionConfigDoc != null) {
            this.questionPlanConfig = this.objectMapper.readTree(((JsonObject)questionConfigDoc.content()).toString());
          } else {
            this.log.error("Personal Accident PlanQuestionConfig document not found in DB");
          }
        }
        catch (Exception e)
        {
          this.log.error("failed to fetch HealthPlanQuestionConfig document from DB ", e);
        }
      }
      JsonNode planIdNode = masterReqNode.get("planId");
      if (planIdNode != null)
      {
        if (this.questionPlanConfig.has(masterReqNode.get("carrierId").intValue() + "-" + masterReqNode.get("planId").intValue()))
        {
          PAQS_Details = 
            "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseQuestion' and carrierId=" + masterReqNode.get("carrierId").intValue() + " and planId = " + masterReqNode.get("planId").intValue();
          this.log.info("PAQS_Details : " + PAQS_Details);
        }
        else
        {
          PAQS_Details = "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseQuestion' and carrierId=" + masterReqNode.get("carrierId").intValue();
          this.log.info("PAQS_Details : " + PAQS_Details);
        }
      }
      else
      {
        PAQS_Details = "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseQuestion' and carrierId=" + masterReqNode.get("carrierId").intValue();
        this.log.info("PAQS_Details : " + PAQS_Details);
      }
      try
      {
        this.PAQSDetailsList = this.productService.executeQueryCouchDB(PAQS_Details);
        this.log.info("PAQSDetailsList : " + this.PAQSDetailsList);
      }
      catch (Exception e)
      {
        this.log.error("failed to execute query " + PAQS_Details, e);
      }
      findQSDetails(masterReqNode);
      
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(masterReqNode));
    }
    catch (Exception e)
    {
      this.log.error("Exception at Personal Accident QuestionReqProcessor ", e);
    }
  }
  
  public JsonNode findQSDetails(JsonNode masterReqNode)
  {
    ArrayNode carrierFinalQuestionDetailsArray = this.objectMapper.createArrayNode();
    
    JsonNode carrierRiderList = masterReqNode.get("riders");
    try
    {
      JsonNode PAQSListNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(this.PAQSDetailsList));
      for (JsonNode qsDetailsNode : PAQSListNode)
      {
        Boolean present = Boolean.valueOf(false);
        if (!qsDetailsNode.has("nsRiders"))
        {
          carrierFinalQuestionDetailsArray.addAll((ArrayNode)PAQSListNode);
          break;
        }
        String qsRidersArray = qsDetailsNode.get("nsRiders").asText();
        if (qsRidersArray.length() > 0)
        {
          for (JsonNode rider : carrierRiderList) {
            if (qsRidersArray.contains(rider.get("riderId").asText()))
            {
              present = Boolean.valueOf(true);
              break;
            }
          }
          if ((!present.booleanValue()) && (carrierRiderList.size() > 0)) {
            carrierFinalQuestionDetailsArray.add(qsDetailsNode);
          }
        }
        else
        {
          carrierFinalQuestionDetailsArray.add(qsDetailsNode);
        }
      }
      this.log.debug("Final question list to displayed on UI:" + carrierFinalQuestionDetailsArray);
      ((ObjectNode)masterReqNode).put("data", carrierFinalQuestionDetailsArray);
    }
    catch (Exception e)
    {
      this.log.error("Exception at findQSDetails : ", e);
    }
    return masterReqNode;
  }
}
