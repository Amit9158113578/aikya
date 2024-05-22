package com.idep.insassessment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
public class LobAssessment {
	static ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LobAssessment.class.getName());
	public ArrayNode calculateInsurance(JsonNode requestWithRiskCat, JsonNode insuranceDocuments) throws JsonParseException, JsonMappingException, IOException {

		JsonNode insuranceSubFactorList = objectMapper.createObjectNode();
		Double insuranceWtInPercentage;

		ArrayNode insuranceBucket = objectMapper.createArrayNode();

		try {
			//	log.info("INSURANCE ASSESSMENT STARTED");
			//	System.out.println("insuranceDocumentsSIZE: "+insuranceDocuments.size());
			Double insuranceWt = 1.0;
			Double calculatedInsWt = 1.0;
			for (JsonNode insurance : insuranceDocuments) 
			{	
				//	log.info("insurance::::"+insurance);
				//log.info("start insurance loop : calinsuranceWt::: "+calculatedInsWt);
				List<String> mandatoryFldList = new ArrayList<String>();
				JsonNode insuranceFactorList = insurance.get("FactorList");
				//	log.info("insuranceFactorList InsuranceFactorList: "+insuranceFactorList);
				for (JsonNode insFactors : insuranceFactorList)
				{	
					if((insFactors.get("mandatory").asText().equalsIgnoreCase("Y")))
					{				
						mandatoryFldList.add(insFactors.get("insuranceFactorName").asText());
						if((insFactors.get("factorType").asText()).equalsIgnoreCase("single"))
						{					
							String factor =insFactors.get("insuranceFactorName").asText();
							String value = requestWithRiskCat.findValue(factor).asText();
							if(!value.isEmpty()){

								insuranceSubFactorList = insFactors.get("InsuranceSubFactorList");
								for (JsonNode subInsuranceFactor : insuranceSubFactorList) {
									if ((subInsuranceFactor.get("riskSubFactorName").asText()).equalsIgnoreCase(value))	
									{
										insuranceWt =subInsuranceFactor.get("riskSubFactorWeight").asDouble(); 
										System.out.println("SINGLEinsuranceWt: "+insuranceWt+"SUBFACTOR NaME::"+subInsuranceFactor.get("riskSubFactorName").asText());
									}								
								}
							}else{
								log.error("VALUE In INPUT REQUEST is eMpty!! for : "+factor);
							}
						}else
						{
							String factor =insFactors.get("insuranceFactorName").asText();
							Integer rangeval =  requestWithRiskCat.findValue(factor).asInt();
							System.out.println("FACATOR : "+factor+"RANGEVAL::: "+rangeval);
							if(!rangeval.equals("")){
								insuranceSubFactorList = insFactors.get("InsuranceSubFactorList");
								for (JsonNode subInsuranceFactor : insuranceSubFactorList) {
									if((subInsuranceFactor.get("min").asInt()<= rangeval) &&(rangeval< subInsuranceFactor.get("max").asInt()))
									{	
										insuranceWt = subInsuranceFactor.get("riskSubFactorWeight").asDouble();
										System.out.println("RANGE LOOP :insuranceWt: "+insuranceWt+"  SUBFactor Name: "+subInsuranceFactor.get("riskSubFactorName").asText());
										break;
									}
								}
							}else{
								log.error("VALUE In INPUT REQUEST is eMpty!! for : "+factor);

							}
						}calculatedInsWt = calculatedInsWt*insuranceWt;	
					}	//insuranceWt = insuranceWt*insuranceWt;

				}
				Double powerof2val =Math.pow(2,mandatoryFldList.size());
				int percentOf = RiskAssessmentConfigData.insValToCatDoc.findValue("percentOf").asInt();
				insuranceWtInPercentage= ((calculatedInsWt)/powerof2val)*100;
				String insuranceCategory = getInsuranceCategory(Math.round(insuranceWtInPercentage),RiskAssessmentConfigData.insValToCatDoc);
				ObjectNode tempInsuranceWeightList = objectMapper.createObjectNode();
				tempInsuranceWeightList.removeAll();
				tempInsuranceWeightList.put("insuranceName",insurance.get("insuranceName").asText());
				tempInsuranceWeightList.put("insuranceLabel",insurance.get("insuranceLabel").asText());
				tempInsuranceWeightList.put("insuranceId", insurance.get("insuranceId").asInt());
				tempInsuranceWeightList.put("insuranceValue", (calculatedInsWt)/powerof2val);
				tempInsuranceWeightList.put("insurancePercentage", insuranceWtInPercentage);
				tempInsuranceWeightList.put("insuranceCat", insuranceCategory);
                if(insurance.get("insuranceName").asText().equalsIgnoreCase("HealthInsurance"))
                {			
                	tempInsuranceWeightList.put("insCatAsPerRisk", requestWithRiskCat.findValue("HealthRisk").asText());
                }else if (insurance.get("insuranceName").asText().equalsIgnoreCase("LifeInsurance"))
                {
                	tempInsuranceWeightList.put("insCatAsPerRisk", requestWithRiskCat.findValue("LifeRisk").asText());

                }else if (insurance.get("insuranceName").asText().equalsIgnoreCase("RetirementInsurance"))
                {
                	tempInsuranceWeightList.put("insCatAsPerRisk", requestWithRiskCat.findValue("RetirementRisk").asText());

                }else if (insurance.get("insuranceName").asText().equalsIgnoreCase("CriticalIllnessInsurance"))
                {
                	tempInsuranceWeightList.put("insCatAsPerRisk", requestWithRiskCat.findValue("CriticalIllnessRisk").asText());

                }

				
				
				
				
				for(JsonNode insuranceConfigNode  : RiskAssessmentConfigData.insrecomresponsedoc.findValue("insuranceConfig")){
					if(insuranceCategory.equalsIgnoreCase(insuranceConfigNode.get("name").asText()))
					{
						tempInsuranceWeightList.put("sumInsured",calculateSumInsured(requestWithRiskCat,insurance.get("insuranceName").asText(),insuranceFactorList));
						tempInsuranceWeightList.put("id",insuranceConfigNode.get("id").asText());
						tempInsuranceWeightList.put("range",insuranceConfigNode.get("range").asText());
						if(tempInsuranceWeightList.get("sumInsured").get("MinimumSumInsured").asInt() == 0){
						}
						else{
							insuranceBucket.add(tempInsuranceWeightList);
						}

					}
					else if(insuranceCategory.equalsIgnoreCase(insuranceConfigNode.get("name").asText()))	
					{
						tempInsuranceWeightList.put("sumInsured",calculateSumInsured(requestWithRiskCat,insurance.get("insuranceName").asText(),insuranceFactorList));
						tempInsuranceWeightList.put("id",insuranceConfigNode.get("id").asText());
						tempInsuranceWeightList.put("range",insuranceConfigNode.get("range").asText());
						if(tempInsuranceWeightList.get("sumInsured").get("MinimumSumInsured").asInt() == 0){
						}
						else{
							insuranceBucket.add(tempInsuranceWeightList);
						}

					}
					else if(insuranceCategory.equalsIgnoreCase(insuranceConfigNode.get("name").asText()))
					{
						tempInsuranceWeightList.put("sumInsured",calculateSumInsured(requestWithRiskCat,insurance.get("insuranceName").asText(),insuranceFactorList));
						tempInsuranceWeightList.put("id",insuranceConfigNode.get("id").asText());
						tempInsuranceWeightList.put("range",insuranceConfigNode.get("range").asText());
						if(tempInsuranceWeightList.get("sumInsured").get("MinimumSumInsured").asInt() == 0){
							//	log.info("COMPREHENSIVE minSumInsured: 0 ");
						}
						else{
							insuranceBucket.add(tempInsuranceWeightList);
						}
					}
				}
				calculatedInsWt =  1.0;

			}		
			//Commented by vipin

			/*for(JsonNode riskConf : responseMappingDoc.get("insuranceConfig"))
			{
				Map<String, String> riskConfig = new ObjectMapper().readValue(riskConf.toString(), HashMap.class);
				ObjectNode riskBucketList = objectMapper.createObjectNode();
				for (Map.Entry<String, String> config : riskConfig.entrySet()) {
					riskBucketList.put(config.getKey(), config.getValue());

					if (config.getValue().equalsIgnoreCase("recommended")) {
						//riskBucketList.put("applicableInsurance", recommendedBucket);
						riskBucketList.put("applicableInsurance", insuranceBucket);
						riskBucketList.put("range",riskConfig.get("range"));

					} else if (config.getValue().equalsIgnoreCase("minimum")) {
						//riskBucketList.put("applicableInsurance", minimumBucket);
						riskBucketList.put("applicableInsurance", insuranceBucket);
						riskBucketList.put("range",riskConfig.get("range"));

					} else if (config.getValue().equalsIgnoreCase("comprehensive")) {
						//riskBucketList.put("applicableInsurance", comprehensiveBucket);
						riskBucketList.put("applicableInsurance", insuranceBucket);
						riskBucketList.put("range",riskConfig.get("range"));
					}
				}
				insuranceAnalysis.add(riskBucketList);
			}
			//insuranceAnalysis.add(insuranceBucket);
				//riskAnalysisArray.add(riskAnalysis);*/
		}
		catch(Exception e)
		{

			log.error("Failed to calculate Insurance Value due to , "+e);
		}
		return insuranceBucket;
	}

	private JsonNode calculateSumInsured(JsonNode requestWithRiskCat, String insName, JsonNode insuranceFactorList) {
		Long minSumInsured ;
		Long comprehensiveSumInsured ;
		Long recommSumInsured ;
		ObjectNode siVal = objectMapper.createObjectNode();
		try {
			minSumInsured = getMinimumSumInsured(requestWithRiskCat,insName,insuranceFactorList);
			recommSumInsured = getRecommendedSumInsured(requestWithRiskCat,insName,insuranceFactorList);
			comprehensiveSumInsured = getComprehensiveSumInsured(requestWithRiskCat,insName,insuranceFactorList);
			siVal.put("MinimumSumInsured", minSumInsured);
			siVal.put("RecommendedSumInsured", recommSumInsured); 
			siVal.put("ComprehensiveSumInsured", comprehensiveSumInsured);

			siVal.put("MinimumSumInsuredInPercentage", (((double)minSumInsured/comprehensiveSumInsured)*100));
			siVal.put("RecommendedSumInsuredInPercentage", (((double)recommSumInsured/comprehensiveSumInsured)*100)); 
			siVal.put("ComprehensiveSumInsuredInPercentage",(((double)comprehensiveSumInsured/comprehensiveSumInsured)*100));

		}catch(Exception e) {

			log.error("Failed To Calculate SI for Insurance due to , "+e);
		}
		return siVal;
	}

	private long getMinimumSumInsured(JsonNode requestWithRiskCat, String insName, JsonNode insuranceFactorList) {
		long siVal = 0;
		Double ageinsuranceWt = 1.0;
		Double healthRiskWt = 1.0;
		Double employmentWt = 1.0;
		try {
			if(insName.equalsIgnoreCase("LifeInsurance"))
			{
				siVal = (long) (7.5 * (requestWithRiskCat.findValue("annualIncomeAmt").asLong()));
			}else if(insName.equalsIgnoreCase("BikeInsurance") )
			{			
				long minidv = (long) (0.7*requestWithRiskCat.findValue("bikeIDV").asLong());
				siVal = minidv;
			}else if(insName.equalsIgnoreCase("CriticalIllnessInsurance"))
			{
				siVal =5*requestWithRiskCat.findValue("annualIncomeAmt").asLong();

				if(siVal> 5000000)
				{
					siVal = 5000000;
				}
			}else if(insName.equalsIgnoreCase("CarInsurance"))
			{
				long minidv = (long) (0.7*requestWithRiskCat.findValue("carIDV").asLong());
				siVal =  minidv;
			}
			else if(insName.equalsIgnoreCase("HealthInsurance"))
			{		

				healthRiskWt = gethealthWeightforSI(requestWithRiskCat,insuranceFactorList);
				ageinsuranceWt = getAgeWeightforSI(requestWithRiskCat,insuranceFactorList);
				employmentWt  = getEmploymentWeightforSI(requestWithRiskCat,insuranceFactorList);

				siVal = (long) (0.15*0.5* requestWithRiskCat.findValue("annualIncomeAmt").asLong()*requestWithRiskCat.findValue("familyMember").asLong() * ageinsuranceWt * healthRiskWt * employmentWt);
				double div = ((double)siVal)/100000;
				siVal = (Math.round(div))*100000;
				if(siVal < 300000)
				{
					siVal = 300000;
				}else if(siVal > 1000000)
				{
					siVal = 1000000;
				}
			}else if(insName.equalsIgnoreCase("RetirementInsurance"))
			{
				siVal = (long) ((60- (requestWithRiskCat.findValue("age").asInt())) * (requestWithRiskCat.findValue("annualIncomeAmt").asLong()) * 0.3);
			}
		}
		catch(Exception e)
		{
			log.error("Failed To calculate Minimum SI due to ,"+e);
		}
		return siVal;

	}
	private Double getEmploymentWeightforSI(JsonNode requestWithRiskCat,JsonNode insuranceFactorList) {
		Double empWt = 1.0;
		for (JsonNode insFactors : insuranceFactorList)
		{	
			String factor = "employmentType";
			String value = requestWithRiskCat.findValue(factor).asText();
			if((insFactors.get("insuranceFactorName").asText()).equalsIgnoreCase(factor))
			{
				JsonNode insuranceSubFactorList = insFactors.get("InsuranceSubFactorList");
				for (JsonNode subInsuranceFactor : insuranceSubFactorList) 
				{if(subInsuranceFactor.get("riskSubFactorName").asText().equalsIgnoreCase(value))
				{

					empWt =subInsuranceFactor.get("riskSubFactorWeight").asDouble(); 
				}
				}	
			}
		}
		return empWt;}

	private Double gethealthWeightforSI(JsonNode requestWithRiskCat, JsonNode insuranceFactorList) {
		Double healthriskeWt = 1.0;
		for (JsonNode insFactors : insuranceFactorList)
		{	
			String factor = "HealthRisk";
			String value = requestWithRiskCat.findValue(factor).asText();
			if((insFactors.get("insuranceFactorName").asText()).equalsIgnoreCase(factor))
			{
				JsonNode insuranceSubFactorList = insFactors.get("InsuranceSubFactorList");
				for (JsonNode subInsuranceFactor : insuranceSubFactorList) 
				{
					if(subInsuranceFactor.get("riskSubFactorName").asText().equalsIgnoreCase(value))
					{
						healthriskeWt =subInsuranceFactor.get("riskSubFactorWeight").asDouble(); 
					}	}	
			}
		}
		return healthriskeWt;}

	private Double getAgeWeightforSI(JsonNode requestWithRiskCat, JsonNode insuranceFactorList) {
		Double ageinsuranceWt = null;
		for (JsonNode insFactors : insuranceFactorList)
		{	
			String factor = "age";
			Integer rangeval =  requestWithRiskCat.findValue(factor).asInt();
			if((insFactors.get("insuranceFactorName").asText()).equalsIgnoreCase(factor))
			{
				JsonNode insuranceSubFactorList = insFactors.get("InsuranceSubFactorList");
				for (JsonNode subInsuranceFactor : insuranceSubFactorList) {
					if((subInsuranceFactor.get("min").asInt()<= rangeval) &&(rangeval< subInsuranceFactor.get("max").asInt()))
					{			
						ageinsuranceWt = subInsuranceFactor.get("riskSubFactorWeight").asDouble();
						break;

					}
				}
				break;
			}
		}
		return ageinsuranceWt;}

	private long getRecommendedSumInsured(JsonNode requestWithRiskCat, String insName , JsonNode insuranceFactorList) {
		long siVal =0;
		Double ageinsuranceWt = 1.0;
		Double healthRiskWt = 1.0;
		Double employmentWt =1.0;
		try {
			if(insName.equalsIgnoreCase("LifeInsurance"))
			{
				siVal = 10 * (requestWithRiskCat.findValue("annualIncomeAmt").asLong());
			}else if(insName.equalsIgnoreCase("BikeInsurance") )
			{			
				siVal = requestWithRiskCat.findValue("bikeIDV").asLong();
			}else if(insName.equalsIgnoreCase("CarInsurance"))
			{
				siVal = requestWithRiskCat.findValue("carIDV").asLong();
			}else if(insName.equalsIgnoreCase("CriticalIllnessInsurance"))
			{
				siVal =(long) (7.5*requestWithRiskCat.findValue("annualIncomeAmt").asLong());
				if(siVal> 10000000)
				{
					siVal = 10000000;
				}
			}
			else if(insName.equalsIgnoreCase("HealthInsurance"))
			{
				healthRiskWt = gethealthWeightforSI(requestWithRiskCat,insuranceFactorList);
				ageinsuranceWt = getAgeWeightforSI(requestWithRiskCat,insuranceFactorList);
				employmentWt  = getEmploymentWeightforSI(requestWithRiskCat,insuranceFactorList);

				siVal = (long) (0.2*0.5* requestWithRiskCat.findValue("annualIncomeAmt").asLong()*requestWithRiskCat.findValue("familyMember").asLong() * ageinsuranceWt * healthRiskWt * employmentWt);
				double div = ((double)siVal)/100000;
				siVal = (Math.round(div))*100000;
				if(siVal < 400000)
				{
					siVal = 400000;
				}else if(siVal > 1200000)
				{
					siVal = 1200000;
				}

			}else if(insName.equalsIgnoreCase("RetirementInsurance"))
			{
				siVal = (long) ((60- (requestWithRiskCat.findValue("age").asInt())) * (requestWithRiskCat.findValue("annualIncomeAmt").asLong()) * 0.4);
			}
		}catch(Exception e)
		{
			log.error("Failed To calculate Recommended SI due to ,"+e);}
		return siVal;
	}
	private long getComprehensiveSumInsured(JsonNode requestWithRiskCat, String insName, JsonNode insuranceFactorList) {
		long siVal = 0;
		Double ageinsuranceWt = 1.0;
		Double healthRiskWt = 1.0;
		Double employmentWt = 1.0;
		try {
			if(insName.equalsIgnoreCase("LifeInsurance"))
			{
				siVal = 15 * (requestWithRiskCat.findValue("annualIncomeAmt").asLong());
			}else if(insName.equalsIgnoreCase("BikeInsurance") )
			{	
				long maxidv = (long) (1.4*requestWithRiskCat.findValue("bikeIDV").asLong());
				siVal = maxidv;
			}else if(insName.equalsIgnoreCase("CarInsurance"))
			{
				long maxidv = (long) (1.4*requestWithRiskCat.findValue("carIDV").asLong());
				siVal = maxidv;
			}else if(insName.equalsIgnoreCase("CriticalIllnessInsurance"))
			{
				siVal =10*requestWithRiskCat.findValue("annualIncomeAmt").asLong();

				if(siVal> 20000000)
				{
					siVal = 20000000;
				}
			}else if(insName.equalsIgnoreCase("HealthInsurance"))
			{
				healthRiskWt = gethealthWeightforSI(requestWithRiskCat,insuranceFactorList);
				ageinsuranceWt = getAgeWeightforSI(requestWithRiskCat,insuranceFactorList);
				employmentWt  = getEmploymentWeightforSI(requestWithRiskCat,insuranceFactorList);
				siVal = (long) (0.25 * 0.5 * requestWithRiskCat.findValue("annualIncomeAmt").asLong()*requestWithRiskCat.findValue("familyMember").asLong() * ageinsuranceWt * healthRiskWt * employmentWt);
				double div = ((double)siVal)/100000;
				siVal = (Math.round(div))*100000;
			
				if(siVal < 500000)
				{
					siVal = 500000;
				}else if(siVal > 1500000)
				{
					siVal = 1500000;
				}


			}else if(insName.equalsIgnoreCase("RetirementInsurance"))
			{
				siVal = (long) ((60- (requestWithRiskCat.findValue("age").asInt())) * (requestWithRiskCat.findValue("annualIncomeAmt").asLong()) * 0.5);
			}
		}catch(Exception e)
		{log.error("Failed To calculate Comprehensive SI due to ,"+e);}
		return siVal;
	}	
	private String getInsuranceCategory(double insuranceWtInPercentage, JsonNode valueToCategoryMappingDoc) {
		String insuranceCategory = null;
		JsonNode categoryArr = valueToCategoryMappingDoc.findValue("InsuranceAssesmentValueToCategory");
		try {for(JsonNode category : categoryArr )
		{
			if((insuranceWtInPercentage >= category.get("min").asLong()) &&(insuranceWtInPercentage <= category.get("max").asLong()))				
			{
				insuranceCategory= category.get("category").asText();
			}
		}			
		}catch(Exception e)
		{
			log.error("Failed to Find catefory for INSURANCE Percentage ,"+e);
		}
		return insuranceCategory;
	}

}