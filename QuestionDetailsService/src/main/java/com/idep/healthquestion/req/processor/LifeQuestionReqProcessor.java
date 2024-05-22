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

public class LifeQuestionReqProcessor implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode questionPlanConfig = null;
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(LifeQuestionReqProcessor.class.getName());
	
	List<Map<String, Object>> LifeQSDetailsList;
	@Override
	public void process(Exchange exchange){
		
		try
		{
			
			String quotedata = exchange.getIn().getBody().toString();
			log.info("Inside LifeQuestionReqProcessor"+quotedata);
			JsonNode masterReqNode =  objectMapper.readTree(quotedata);
			String LifeQS_Details = null;
			{
				
				LifeQS_Details ="select ProductData.* from ProductData where documentType='LifeDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue()+
						" and planId = "+masterReqNode.get("planId").intValue()+"  and applicableGender like '%"+masterReqNode.get("gender").asText()+"%'";
				log.info("LifeQS_Details query: "+LifeQS_Details);
				
				
			}
			
			if(questionPlanConfig==null)
			{
				/**
				 * To display LifeDiseasequestions based on carrierId and planId information is stored in HealthPlanQuestionConfig document
				 */
				try 
				{
					JsonDocument questionConfigDoc = serviceConfig.getDocBYId("LifePlanQuestionConfig");
					if(questionConfigDoc!=null)
					{
						questionPlanConfig = objectMapper.readTree(questionConfigDoc.content().toString());
					}
					else
					{
						log.error("LifePlanQuestionConfig document not found in DB");
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
					 * Select LifeDiseasequestions based on carrierId and planId
					 */
					LifeQS_Details ="select ProductData.* from ProductData where documentType='LifeDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue()+
										" and planId = "+masterReqNode.get("planId").intValue()+"  and applicableGender like '%"+masterReqNode.get("gender").asText()+"%'";
					log.info("LifeQS_Details : "+LifeQS_Details);
				}
				else
				{
					/**
					 *  Select LifeDiseasequestions based on carrierId
					 */
					LifeQS_Details ="select ProductData.* from ProductData where documentType='LifeDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue()+"  and applicableGender like %"+masterReqNode.get("gender").asText()+"%";
					log.info("LifeQS_Details : "+LifeQS_Details);
				}
			}
			
			else
			{
				LifeQS_Details ="select ProductData.* from ProductData where documentType='LifeDiseaseQuestion' and carrierId="+masterReqNode.get("carrierId").intValue()+"  and applicableGender like %"+masterReqNode.get("gender").asText()+"%";
				log.info("LifeQS_Details : "+LifeQS_Details);
			}
			
			try
			{
				LifeQSDetailsList = productService.executeQueryCouchDB(LifeQS_Details);
				log.info("LifeQSDetailsList : "+LifeQSDetailsList);
			}
			catch(Exception e)
			{
				log.error("failed to execute query "+LifeQS_Details,e );
			}
			findQSDetails(masterReqNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(masterReqNode));
		}
		catch(Exception e)
		{
			log.error("Exception at LifeQuestionReqProcessor ",e);
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
				JsonNode LifeQSListNode = objectMapper.readTree(objectMapper.writeValueAsString(LifeQSDetailsList));
				/**
				 * If carrier does not support any riders then display all questions on UI
				 */
				for(JsonNode qsDetailsNode : LifeQSListNode)
				 {
					present = false;
					if(!qsDetailsNode.has("nsRiders"))
					{
						carrierFinalQuestionDetailsArray.addAll((ArrayNode) LifeQSListNode);
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

