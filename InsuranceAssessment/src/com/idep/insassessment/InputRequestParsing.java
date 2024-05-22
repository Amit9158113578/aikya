package com.idep.insassessment;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InputRequestParsing
{
	Logger log = Logger.getLogger(InputRequestParsing.class.getName());
	

	public ObjectNode inputRequestParsing(JsonNode request) throws JsonProcessingException, IOException {
		
		ObjectMapper objectMapper = new ObjectMapper();
		RiskAssessment calcRisk = new RiskAssessment();
		LobAssessment calcInsurance = new LobAssessment();
		RiderAssessment calcRider = new RiderAssessment();
		ArrayNode profwiseInsuranceDoc =  null;
		ArrayNode profwiseRiskDoc = null;
		ArrayNode profwiseRiderDoc = null;
		ArrayNode riskCategoryArr = objectMapper.createArrayNode();
		ArrayNode insuranceCategoryArr = objectMapper.createArrayNode();
		ArrayNode riderAnalysisArr = objectMapper.createArrayNode();
		ObjectNode response = objectMapper.createObjectNode();
		JsonNode finalResponse =  objectMapper.createObjectNode();
		
		
		//System.out.println("P365 Inside InputRequetsParsing class recieved input req : ");
		JsonNode inputReqNode = request.get("quoteParam");
		
		
	
		profwiseInsuranceDoc=objectMapper.createArrayNode();

			for(int i=0; i < RiskAssessmentConfigData.insuranceDocuments.size();i++)
		{   
			JsonNode insuranceDoc = RiskAssessmentConfigData.insuranceDocuments.get(i);
			if((inputReqNode.get("professionCode")).equals(insuranceDoc.get("professionCode")))
			{		
				profwiseInsuranceDoc.add(insuranceDoc);
			}				

		}
		profwiseRiderDoc =  objectMapper.createArrayNode();
		for(int j=0; j < RiskAssessmentConfigData.riderDocuments.size();j++)
		{   			

			JsonNode riderDoc = RiskAssessmentConfigData.riderDocuments.get(j);

			if((inputReqNode.get("professionCode")).equals(riderDoc.get("professionCode")))
			{
				profwiseRiderDoc.add(riderDoc);}
		}		
		profwiseRiskDoc =	objectMapper.createArrayNode();
		for(int k=0; k < RiskAssessmentConfigData.riskDocuments.size();k++)
		{   
			JsonNode riskDoc = RiskAssessmentConfigData.riskDocuments.get(k);
			if(inputReqNode.get("professionCode").equals(riskDoc.get("professionCode")) )
			{		//	System.out.println("RISK  PC:"+riskDoc.get("professionCode"));
				profwiseRiskDoc.add(riskDoc);
			}
		}

		riskCategoryArr = calcRisk.calculateRisk(inputReqNode,profwiseRiskDoc);
	JsonNode requestWithRiskCat =  mergeRequestWithRiskCategory(riskCategoryArr,inputReqNode);
	insuranceCategoryArr= calcInsurance.calculateInsurance(requestWithRiskCat,profwiseInsuranceDoc);
		riderAnalysisArr = calcRider.calculateRider(profwiseRiderDoc,requestWithRiskCat,insuranceCategoryArr);

		/**	System.out.println("lob: "+RiskAssessmentConfigData.lobSupportedRisksConfigDoc);
		System.out.println("risks: "+RiskAssessmentConfigData.lobSupportedRisksConfigDoc.get("lob").asText());
		if((inputReqNode.get("insuranceType").asText()).equalsIgnoreCase(RiskAssessmentConfigData.lobSupportedRisksConfigDoc.get("lob").get("lobName").asText()));
		{
			for(int i=0; i < RiskAssessmentConfigData.riskDocuments.size();i++)
			{   
				JsonNode riskDoc = RiskAssessmentConfigData.riskDocuments.get(i);
				for(JsonNode lob : RiskAssessmentConfigData.lobSupportedRisksConfigDoc.get("lob")){
				if((inputReqNode.get("professionCode").equals(riskDoc.get("professionCode"))&&(riskDoc.get("riskName").asText().equalsIgnoreCase(lob.get("riskName").asText())))) 
				{profwiseRiskDoc.add(riskDoc);}
			}}

			healthAss.getHealthAssessment(inputReqNode,profwiseRiskDoc,profwiseInsuranceDoc,profwiseRiderDoc);
		}**/
		ObjectNode algResponse =  objectMapper.createObjectNode();
		((ObjectNode) response).set("riskAnalysis", riskCategoryArr);
		((ObjectNode) response).set("insuranceAnalysis", insuranceCategoryArr);
		((ObjectNode) response).set("productAnalysis", riderAnalysisArr);
		algResponse.set("algResponse",response);
		((ObjectNode) finalResponse).set("algResponse", algResponse);
		System.out.println("RESPONSE::: "+algResponse);
		log.info("RESPONSE::: "+algResponse);
		return algResponse;
		}
		private JsonNode mergeRequestWithRiskCategory(ArrayNode riskCategoryArr, JsonNode inputReqNode) {
			try {	for(int riskcounter  = 0;riskcounter<riskCategoryArr.size();riskcounter++)
			{ 
				for(JsonNode risk: riskCategoryArr.get(riskcounter).get("applicableRisk"))
				{
					((ObjectNode)inputReqNode).put(risk.get("riskName").asText(), risk.get("riskCat").asText());
				}
			}
			log.info("InputReqNode with merged Risk Response Node:::: "+inputReqNode);
			}catch(Exception e)
			{
				log.error("Failed to merge Input Request Node with Risk Category map, ",e);
			}
			return inputReqNode;
		}
	}