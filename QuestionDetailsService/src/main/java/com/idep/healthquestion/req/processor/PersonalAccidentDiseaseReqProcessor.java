package com.idep.healthquestion.req.processor;



import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class PersonalAccidentDiseaseReqProcessor
  implements Processor
{
  Logger log = Logger.getLogger(PersonalAccidentDiseaseReqProcessor.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService productService = CBInstanceProvider.getProductConfigInstance();
  CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
      this.log.info("Inside Personal Accident DiseaseReqProcessor");
      String inputReq = exchange.getIn().getBody().toString();
      String diseaseQuery = null;
      JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
      JsonNode DiseasePlanConfig = null;
      List<Map<String, Object>> diseaseList = null;
      diseaseQuery = "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseMapping' and carrierId=" + inputReqNode.get("carrierId").asText() + " " + 
        "and planId=" + inputReqNode.get("planId").asText() + " ORDER BY preference";
      this.log.info(" diseaseQuery: " + diseaseQuery);
      if ((inputReqNode.has("carrierId")) && (inputReqNode.has("planId"))) {
        try
        {
          this.log.info("inside Personal Accident  disease req processsor");
          DiseasePlanConfig = this.objectMapper.readTree(((JsonObject)this.serviceConfig.getDocBYId("PersonalAccidentPlanDiseaseConfig").content()).toString());
          if (DiseasePlanConfig != null)
          {
            if (DiseasePlanConfig.has(inputReqNode.get("carrierId").asInt() + "-" + inputReqNode.get("planId").asInt()))
            {
              diseaseQuery = 
                "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseMapping' and carrierId=" + inputReqNode.get("carrierId").asInt() + " " + "and planId=" + inputReqNode.get("planId").asInt() + " ORDER BY preference";
              this.log.info(" diseaseQuery: " + diseaseQuery);
            }
            else
            {
              diseaseQuery = "select ProductData.* from ProductData where documentType='PersonalAccidentDiseaseMapping' and carrierId=" + inputReqNode.get("carrierId").asInt() + " ORDER BY preference";
              this.log.info(" diseaseQuery: " + diseaseQuery);
            }
            try
            {
              diseaseList = this.productService.executeQueryCouchDB(diseaseQuery);
              this.log.info(" diseaseQuery: " + diseaseList);
            }
            catch (Exception e)
            {
              this.log.error("failed to execute query " + diseaseQuery, e);
            }
            JsonNode diseaseFinalList = this.objectMapper.readTree(this.objectMapper.writeValueAsString(diseaseList));
            this.log.debug("diseaseList Result After Query Executed : " + diseaseList);
            ((ObjectNode)inputReqNode).put("data", diseaseFinalList);
          }
          else
          {
            this.log.error("Personal Accident PlanDiseaseConfig document not found in DB");
          }
        }
        catch (Exception e)
        {
          this.log.error("failed to fetch Personal Accident PlanDiseaseConfig document from DB ", e);
        }
      }
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(inputReqNode));
    }
    catch (Exception e)
    {
      this.log.error("Error AT Personal Accident DiseaseReqProcessor : ", e);
    }
  }
}
