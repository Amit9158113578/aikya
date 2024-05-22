package com.idep.insassessment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RiskAssessment {
	Logger log = Logger.getLogger(RiskAssessment.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	//ObjectNode riskAnalysis = objectMapper.createObjectNode();

	/**
	 * @param inputReqNode
	 * @param riskDocuments
	 * returns HashMap of risk and its weight
	 */
	/**
	 * @param inputReqNode
	 * @param riskDocuments
	 * @param responseMappingDoc 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public ArrayNode calculateRisk(JsonNode inputReqNode, ArrayNode riskDocuments) throws JsonParseException, JsonMappingException, IOException
	{	// TODO Auto-generated method stub
		int factorWt = 0;
		int subFactorWt = 0;
		int riskWt = 0;
		//ArrayNode riskArr = objectMapper.createArrayNode();
		//Map<String, String> inputReqMap = new ObjectMapper().readValue(inputReqNode.toString(), HashMap.class);
		//ObjectNode riskwise = objectMapper.createObjectNode();
		JsonNode riskSubFactorList = objectMapper.createObjectNode();
		ArrayNode highRiskBucket = objectMapper.createArrayNode();
		ArrayNode lowRiskBucket = objectMapper.createArrayNode();
		ArrayNode mediumRiskBucket = objectMapper.createArrayNode();
		ArrayNode veryHighRiskBucket = objectMapper.createArrayNode();
		ArrayNode veryLowRiskBucket = objectMapper.createArrayNode();
		ArrayNode riskAnalysis = objectMapper.createArrayNode();
		ArrayNode riskAnalysisArray = objectMapper.createArrayNode();
		try{				
			for (JsonNode riskDocument : riskDocuments)
			{
				log.info("inputReqNode:: "+inputReqNode);

				JsonNode riskFactorList = riskDocument.get("RiskFactorList");
				for (JsonNode riskFactor : riskFactorList) {
					if(riskFactor.get("riskFactorType").asText().equalsIgnoreCase("single"))
					{
						factorWt = riskFactor.get("riskFactorWeight").asInt();
						String factor =riskFactor.get("riskFactorName").asText();
						String factorval = inputReqNode.findValue(factor).asText();
						if(!factorval.isEmpty()){
							riskSubFactorList = riskFactor.get("RiskSubFactorList");

							for (JsonNode subFactor : riskSubFactorList) {
								if ((subFactor.get("riskSubFactorName").asText()).equalsIgnoreCase(factorval))	
								{
									subFactorWt = subFactor.get("riskSubFactorWeight").asInt();
									break;
								}
								else{subFactorWt = 0;
									}
								}
						}
						else{
							log.info("VALUE In INPUT REQUEST is empty!! for : "+factor);
						}
					}else
					{	

						String factor =riskFactor.get("riskFactorName").asText();
						Integer rangeval =  inputReqNode.findValue(factor).asInt();
						factorWt = riskFactor.get("riskFactorWeight").asInt();

						//Integer rangeval =  inputReqNode.findValue(factor).asInt();
						if(!rangeval.equals("")){
							riskSubFactorList = riskFactor.get("RiskSubFactorList");
							for (JsonNode subFactor : riskSubFactorList) {
								if((subFactor.get("min").asInt()<= rangeval) &&(rangeval< subFactor.get("max").asInt()))
								{	
									subFactorWt = subFactor.get("riskSubFactorWeight").asInt();
									break;
								}
							}
						}//rangeval empty or not if loop
						else{
							log.info("VALUE In INPUT REQUEST is eMpty!! for : "+factor);

						}
					}//Subrisk loop END
					System.out.println("riskWt: "+riskWt+"+("+"factorWt: "+factorWt+"*"+"subFactorWt: "+subFactorWt);

					riskWt = riskWt + (factorWt * subFactorWt);
			}
			

				double riskWtInPercentage = Math.round(((Double.valueOf(riskWt)/1000) *100));
				String riskCategory = getRiskCategory(riskWtInPercentage);
				
				ObjectNode tempRiskWeightList = objectMapper.createObjectNode();
				tempRiskWeightList.removeAll();

				tempRiskWeightList.put("riskId",riskDocument.get("riskId").asInt());

				tempRiskWeightList.put("riskLabel",riskDocument.get("riskLabel").asText());
				tempRiskWeightList.put("riskName",riskDocument.get("riskName").asText());
				tempRiskWeightList.put("riskValue", riskWt);
				tempRiskWeightList.put("riskPercentage", riskWtInPercentage);

				
				if(riskCategory.equalsIgnoreCase("high"))
				{
					tempRiskWeightList.put("riskCat", riskCategory);
					highRiskBucket.add(tempRiskWeightList);
				}else if(riskCategory.equalsIgnoreCase("medium"))
				{
					tempRiskWeightList.put("riskCat", riskCategory);
					mediumRiskBucket.add(tempRiskWeightList);
				}else if(riskCategory.equalsIgnoreCase("low"))
				{
					tempRiskWeightList.put("riskCat", riskCategory);
					lowRiskBucket.add(tempRiskWeightList);	
				}else if(riskCategory.equalsIgnoreCase("veryHigh"))
				{
					tempRiskWeightList.put("riskCat", riskCategory);
					veryHighRiskBucket.add(tempRiskWeightList);	
				}else if(riskCategory.equalsIgnoreCase("veryLow"))
				{						tempRiskWeightList.put("riskCat", riskCategory);
				veryLowRiskBucket.add(tempRiskWeightList);	
				}

				//last for loop riskdoc.size		
				//} commented by vipin
				riskWt = 0;

				//riskArr.add(riskwise);
				//String riskList = objectMapper.writeValueAsString(riskArr);
				//JsonNode riskData = objectMapper.readTree(riskList);
				
			}
			for(JsonNode riskConf : RiskAssessmentConfigData.insrecomresponsedoc.findValue("riskConfig"))
			{
				Map<String, String> riskConfig = new ObjectMapper().readValue(riskConf.toString(), HashMap.class);
				ObjectNode riskBucketList = objectMapper.createObjectNode();
				for (Map.Entry<String, String> config : riskConfig.entrySet()) {
					riskBucketList.put(config.getKey(), config.getValue());

					if (config.getValue().equalsIgnoreCase("high")) {
						riskBucketList.put("applicableRisk", highRiskBucket);
						riskBucketList.put("range",riskConfig.get("range"));
					} else if (config.getValue().equalsIgnoreCase("medium")) {
						riskBucketList.put("applicableRisk", mediumRiskBucket);
						riskBucketList.put("range",riskConfig.get("range"));

					} else if (config.getValue().equalsIgnoreCase("low")) {
						riskBucketList.put("applicableRisk", lowRiskBucket);
						riskBucketList.put("range",riskConfig.get("range"));

					}else if (config.getValue().equalsIgnoreCase("veryHigh")) {
						riskBucketList.put("applicableRisk", veryHighRiskBucket);
						riskBucketList.put("range",riskConfig.get("range"));

					}else if (config.getValue().equalsIgnoreCase("veryLow")) {
						riskBucketList.put("applicableRisk", veryLowRiskBucket);
						riskBucketList.put("range",riskConfig.get("range"));
					}
				}
				//	log.info("riskBucketList:" +riskBucketList);
				riskAnalysis.add(riskBucketList);
				log.info("riskAnalysis:"+riskAnalysis);
				riskAnalysisArray.add(riskAnalysis);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("Failed to calculate Risk Value,",e);
		}
		return riskAnalysis;
	}
	private String getRiskCategory(double riskWtInPercentage) {
		String riskCategory = null;
		JsonNode categoryArr = RiskAssessmentConfigData.riskValToCatDoc.findValue("RiskAssesmentValueToCategory");
		try{

			for(JsonNode category : categoryArr )
			{
				if((riskWtInPercentage >= category.get("min").asDouble()) && (riskWtInPercentage <= category.get("max").asDouble()))				
				{	riskCategory= category.get("category").asText();
				}
			}
		}
		catch(Exception e)
		{
			log.error("Failed To Find RISK CATEGORY",e);
		}
		return riskCategory;
	}
}