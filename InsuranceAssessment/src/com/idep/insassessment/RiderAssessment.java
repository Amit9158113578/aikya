package com.idep.insassessment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RiderAssessment {
	Logger log = Logger.getLogger(RiskAssessment.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	public ArrayNode calculateRider(JsonNode riderDocuments, JsonNode requestWithRiskCat, ArrayNode insuranceCategoryArr) {
		JsonNode riderSubFactorList = objectMapper.createObjectNode();
		Double riderWtInPercentage;
		Double riderWt ;
		ArrayNode healthBucket = objectMapper.createArrayNode();
		ArrayNode lifeBucket = objectMapper.createArrayNode();
		ArrayNode bikeBucket = objectMapper.createArrayNode();
		ArrayNode retirementBucket = objectMapper.createArrayNode();
		ArrayNode carBucket = objectMapper.createArrayNode();
		ArrayNode ciBucket = objectMapper.createArrayNode();

		

		ArrayNode riderAnalysis = objectMapper.createArrayNode();
		//insuranceCategoryArr ::ArrayNode which contains output of insurance assessment
		try {
			//log.info("***********RIDERRRRRRRR*************");
			System.out.println("riderSIConfigDoc SIZE: "+riderDocuments.size());
			for (JsonNode rider : riderDocuments) 
			{		
				List<String> mandatoryFldList = new ArrayList<String>();
				riderWt=1.0;
				JsonNode riderFactorList = rider.get("RiderFactorList");
				for (JsonNode riderFactors : riderFactorList)
				{			
					if((riderFactors.get("mandatory").asText().equalsIgnoreCase("Y")))
					{
						mandatoryFldList.add(riderFactors.get("riderFactorName").asText());
						if((riderFactors.get("riderFactorType").asText()).equalsIgnoreCase("single"))
						{					
							String factor =riderFactors.get("riderFactorName").asText();

							String value = requestWithRiskCat.findValue(factor).asText();
							riderSubFactorList = riderFactors.get("RiderSubFactorList");
							for (JsonNode subRiderFactor : riderSubFactorList) {
								if ((subRiderFactor.get("riderSubFactorName").asText()).equalsIgnoreCase(value))	
								{
									riderWt =subRiderFactor.get("riderSubFactorWeight").asDouble(); 
								}								
							}
						}else
						{
							String factor =riderFactors.get("riderFactorName").asText();
							Integer rangeval =  requestWithRiskCat.findValue(factor).asInt();
							riderSubFactorList = riderFactors.get("RiderSubFactorList");
							for (JsonNode subRiderFactor : riderSubFactorList) {
								if((subRiderFactor.get("min").asInt()<= rangeval) &&(rangeval< subRiderFactor.get("max").asInt()))
								{	
									if ((subRiderFactor.get("riderSubFactorName").asInt()) == (rangeval))	
									{
										riderWt = subRiderFactor.get("riderSubFactorWeight").asDouble();
										break;
									}}
							}
						}riderWt = riderWt*riderWt;
					}
				}
				Double powerof2val =Math.pow(2,mandatoryFldList.size());
				riderWtInPercentage = ((riderWt)/powerof2val)*100;
				String riderCategory = getRiderCategory(Math.round(riderWtInPercentage));

				ObjectNode tempInsuranceWeightList = objectMapper.createObjectNode();
				tempInsuranceWeightList.removeAll();
				tempInsuranceWeightList.put("riderName",rider.get("riderName").asText());
				tempInsuranceWeightList.put("riderId", rider.get("riderId").asInt());
				tempInsuranceWeightList.put("riderValue", (riderWt)/powerof2val);
				tempInsuranceWeightList.put("riderCategory", riderCategory);
				tempInsuranceWeightList.put("riderPercentage", riderWtInPercentage);

				if((rider.get("IsSIApplicable").asText()).equalsIgnoreCase("Y")){

					tempInsuranceWeightList.put("sumInsured",calculateSumInsured(requestWithRiskCat,insuranceCategoryArr,rider.get("insuranceId").asInt(),rider.get("riderId").asInt()));
				}
				else{
					//log.info("SI not applicable.");
				}
				if((rider.get("LOB").asText()).equalsIgnoreCase("Health"))
				{
					healthBucket.add(tempInsuranceWeightList);
				}else if(rider.get("LOB").asText().equalsIgnoreCase("life"))
				{
					lifeBucket.add(tempInsuranceWeightList);
				}else if(rider.get("LOB").asText().equalsIgnoreCase("bike"))
				{
					bikeBucket.add(tempInsuranceWeightList);	
				}else if(rider.get("LOB").asText().equalsIgnoreCase("car"))
				{
					carBucket.add(tempInsuranceWeightList);	
				}else if(rider.get("LOB").asText().equalsIgnoreCase("retirement"))
				{
					retirementBucket.add(tempInsuranceWeightList);	
				}else if(rider.get("LOB").asText().equalsIgnoreCase("criticalillness"))
				{
					ciBucket.add(tempInsuranceWeightList);	
				}
			}
			ObjectNode rider24by7node =objectMapper.createObjectNode();
			rider24by7node.put("riderName","24X7 Road Side Assistance Rider");
			rider24by7node.put("riderId",9);
			rider24by7node.put("riderCategory","Recommended");
			carBucket.add(rider24by7node);

			for(JsonNode riderConf : RiskAssessmentConfigData.insrecomresponsedoc.findValue("productAnalysis"))
			{ 
				Map<String, String> riderConfig = new ObjectMapper().readValue(riderConf.toString(), HashMap.class);
				ObjectNode riderBucketList = objectMapper.createObjectNode();
				for (Map.Entry<String, String> config : riderConfig.entrySet()) {
					riderBucketList.put(config.getKey(), config.getValue());
					if (config.getValue().equalsIgnoreCase("life")) {
						riderBucketList.put("riders", lifeBucket);
					} else if (config.getValue().equalsIgnoreCase("bike")) {
						riderBucketList.put("riders", bikeBucket);
					} else if (config.getValue().equalsIgnoreCase("car")) {
						
						riderBucketList.put("riders", carBucket);
					} else if (config.getValue().equalsIgnoreCase("retirement")) {
						riderBucketList.put("riders", retirementBucket);
					} else if (config.getValue().equalsIgnoreCase("health")) {
						riderBucketList.put("riders", healthBucket);
					} else if (config.getValue().equalsIgnoreCase("criticalillness")) {
						riderBucketList.put("riders", ciBucket);
					}	
				}
				riderAnalysis.add(riderBucketList);
			}
			//System.out.println("********RIDER Analysis: :"+riderAnalysis);
			//riskAnalysisArray.add(riskAnalysis);
		}catch(Exception e)
		{
			log.error("Failed to calculate rider total wt due to, ",e);
		}
		return riderAnalysis;
	}
	private JsonNode calculateSumInsured(JsonNode requestWithRiskCat, ArrayNode insuranceCategoryArr,int insuranceId, int riderId) {
		double minSumInsured ;
		double comprehensiveSumInsured ;
		double recommSumInsured ;
		ObjectNode siVal = objectMapper.createObjectNode();
		try{
			minSumInsured = getMinimumSumInsured(requestWithRiskCat,insuranceCategoryArr,insuranceId,riderId);
			recommSumInsured = getRecommendedSumInsured(requestWithRiskCat,insuranceCategoryArr,insuranceId,riderId);
			comprehensiveSumInsured= getComprehensiveSumInsured(requestWithRiskCat,insuranceCategoryArr,insuranceId,riderId);

			siVal.put("MinimumSumInsured", minSumInsured);
			siVal.put("ComprehensiveSumInsured", comprehensiveSumInsured);
			siVal.put("RecommendedSumInsured", recommSumInsured);
			siVal.put("MinimumSumInsuredInPercentage", ((double)(minSumInsured/comprehensiveSumInsured)*100));
			siVal.put("RecommendedSumInsuredInPercentage", ((double)(recommSumInsured/comprehensiveSumInsured)*100)); 
			siVal.put("ComprehensiveSumInsuredInPercentage",((double)(comprehensiveSumInsured/comprehensiveSumInsured)*100));
		}catch(Exception e) {
			log.error("Failed To Calculate SI for RIDER due to , "+e);
		}
		return siVal;

	}
	private long getMinimumSumInsured( JsonNode requestWithRiskCat, ArrayNode insuranceCategoryArr, int insuranceId,int riderId) {
		long siVal = 0;
		try {
			for(JsonNode riderSI : RiskAssessmentConfigData.riderSIConfigDoc){
				if(riderSI.get("riderId").asInt()  == riderId){
					for(JsonNode sicategory : riderSI.get("sicategory")){

						if((sicategory.findValue("siCategoryValue").asText()).equalsIgnoreCase("minimum"))
						{			
							for(JsonNode insResponse :insuranceCategoryArr )
							{
								if(insResponse.findValue("insuranceId").asInt() == insuranceId){

									String factor = sicategory.findValue("factorName").asText();
									siVal = (sicategory.findValue("percentofvalue").asLong())*(insResponse.get("sumInsured").findValue(factor).asLong())/100;
								}}
						}
					}
				}
			}

		}catch(Exception e)
		{log.error("Failed To calculate Minimum SI due to ,",e);}
		return siVal;
	}

	private long getRecommendedSumInsured(JsonNode requestWithRiskCat, ArrayNode insuranceCategoryArr, int insuranceId,int riderId) {
		long siVal = 0;
		try {
			for(JsonNode riderSI : RiskAssessmentConfigData.riderSIConfigDoc){
				if(riderSI.get("riderId").asInt()  == riderId){
					for(JsonNode sicategory : riderSI.get("sicategory")){
						if((sicategory.findValue("siCategoryValue").asText()).equalsIgnoreCase("recommended"))
						{			
							for(JsonNode insResponse :insuranceCategoryArr )
							{
								if(insResponse.findValue("insuranceId").asInt() == insuranceId){
									String factor = sicategory.findValue("factorName").asText();
									siVal = (sicategory.findValue("percentofvalue").asInt())*(insResponse.get("sumInsured").findValue(factor).asInt())/100;
								}}
						}
					}
				}
			}
		}		catch(Exception e)
		{log.error("Failed To calculate Recommended SI due to ,",e);}
		return siVal;	}
	private long getComprehensiveSumInsured(JsonNode requestWithRiskCat, ArrayNode insuranceCategoryArr,int insuranceId, int riderId) {
		long siVal = 0;
		try{
			for(JsonNode riderSI : RiskAssessmentConfigData.riderSIConfigDoc){
				if(riderSI.get("riderId").asInt()  == riderId){
					for(JsonNode sicategory : riderSI.get("sicategory")){
						if((sicategory.findValue("siCategoryValue").asText()).equalsIgnoreCase("Comprehensive"))
						{			
							for(JsonNode insResponse :insuranceCategoryArr )
							{
								if(insResponse.findValue("insuranceId").asInt() == insuranceId){
																		String factor = sicategory.findValue("factorName").asText();
									double insSiVal = insResponse.get("sumInsured").findValue(factor).asInt();
									siVal = (long) ((sicategory.findValue("percentofvalue").asInt())*(insSiVal)/100);
								}}
						}
					}
				}
			}
		}
		catch(Exception e)
		{log.error("Failed To calculate Comprehensive SI due to ,",e);}
		return siVal;
	}

	private String getRiderCategory(long l) {
		String insuranceCategory = null;
		JsonNode categoryArr = RiskAssessmentConfigData.insValToCatDoc.findValue("InsuranceAssesmentValueToCategory");
       // System.out.println("InsuranceAssesmentValueToCategory: "+categoryArr);
		try {
			for(JsonNode category : categoryArr )
			{
				if((l >= category.get("min").asDouble()) &&(l <= category.get("max").asDouble()))				
				{
					insuranceCategory= category.get("category").asText();
				}
			}
		}
		catch(Exception e)
		{
			log.error("Failed to find Riderr Category due to , ",e);
		}
		return insuranceCategory;
	}
}