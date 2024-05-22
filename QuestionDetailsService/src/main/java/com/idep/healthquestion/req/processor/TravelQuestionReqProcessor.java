package com.idep.healthquestion.req.processor;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class TravelQuestionReqProcessor implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode questionPlanConfig = null;
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(TravelQuestionReqProcessor.class.getName());
	
	List<Map<String, Object>> TravelQSDetailsList;
	@Override
	public void process(Exchange exchange){
		
		try
		{
			String quotedata = exchange.getIn().getBody().toString();
			JsonNode masterReqNode =  objectMapper.readTree(quotedata);
			String TravelQS_Details = null;
			if(questionPlanConfig==null)
			{
				/**
				 * To display TravelDiseasequestions based on carrierId and planId information is stored in HealthPlanQuestionConfig document
				 */
				try 
				{
					JsonDocument questionConfigDoc = serviceConfig.getDocBYId("TravelPlanQuestionConfig");
					if(questionConfigDoc!=null)
					{
						questionPlanConfig = objectMapper.readTree(questionConfigDoc.content().toString());
					}
					else
					{
						log.error("TravelPlanQuestionConfig document not found in DB");
					}
				}
				catch(Exception e)
				{
					log.error("failed to fetch HealthPlanQuestionConfig document from DB ",e);
				}
				
			}
			JsonNode planIdNode = masterReqNode.get("planId");
			if(planIdNode!=null)
			{
				if(questionPlanConfig.has(masterReqNode.get("carrierId").intValue()+"-"+masterReqNode.get("planId").intValue()))
				{
					/**
					 * Select TravelDiseasequestions based on carrierId and planId
					 */
					TravelQS_Details ="select ProductData.* from ProductData where documentType='TravelDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue()+
										" and planId = "+masterReqNode.get("planId").intValue();
				}
				else
				{
					/**
					 *  Select TravelDiseasequestions based on carrierId
					 */
					TravelQS_Details ="select ProductData.* from ProductData where documentType='TravelDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue();
				}
			}
			
			else
			{
				TravelQS_Details ="select ProductData.* from ProductData where documentType='TravelDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue();
			}
			
			try
			{
				TravelQSDetailsList = productService.executeQueryCouchDB(TravelQS_Details);
			}
			catch(Exception e)
			{
				log.error("failed to execute query "+TravelQS_Details,e );
			}
			findQSDetails(masterReqNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(masterReqNode));
		}
		catch(Exception e)
		{
			log.error("Exception at TravelQuestionReqProcessor ",e);
		 }
	}

	/**
	 * This Function is used to find question based on rider selection
	 * @param masterReqNode
	 * @return
	 */
	 public  JsonNode findQSDetails(JsonNode masterReqNode)
		{
		 	Boolean present;
		 	/**
		 	 * Questions to be displayed on UI
		 	 */
		 	ArrayNode carrierFinalQuestionDetailsArray = objectMapper.createArrayNode();
		 	//List of carrier riders
		 	JsonNode carrierRiderList = masterReqNode.get("riders");
		 			 	
			 try
			 {		
				JsonNode travelQSListNode = objectMapper.readTree(objectMapper.writeValueAsString(TravelQSDetailsList));
				/**
				 * If carrier does not support any riders then display all questions on UI
				 */
				for(JsonNode qsDetailsNode : travelQSListNode)
				 {
					present = false;
					if(!qsDetailsNode.has("nsRiders"))
					{
						carrierFinalQuestionDetailsArray.addAll((ArrayNode) travelQSListNode);
						break;
					}
					
					String qsRidersArray = qsDetailsNode.get("nsRiders").asText();
					
					/**
					 * List of riders for which question is not applicable
					 */
					if(qsRidersArray.length()>0)
					{
						for(JsonNode rider : carrierRiderList)
						{
							/**
							 * Check if rider list of question contains input rider id
							 */
							if(qsRidersArray.contains(rider.get("riderId").asText()))
							{
								present = true;
								break;
							}
						}
						/**
						 * If rider list of question does not contain input rider id then add that question into UI questions list.
						 */
						if(!present && carrierRiderList.size() > 0)
						{
							carrierFinalQuestionDetailsArray.add(qsDetailsNode);
						}
					}
					else
					{
						/**
						 * If  rider list of question is empty then add question into UI questions list.
						 */
						carrierFinalQuestionDetailsArray.add(qsDetailsNode);
					}			
				 }
				 log.debug("Final question list to displayed on UI:"+carrierFinalQuestionDetailsArray );
				 ((ObjectNode)masterReqNode).put("data", carrierFinalQuestionDetailsArray);
			 }
			 catch(Exception e)
			 {
				 log.error("Exception at findQSDetails : ",e);
			 }	
			 return masterReqNode;		  
		}
}
