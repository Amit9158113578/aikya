/**
 * 
 */
package com.idep.insurancerecommender.form.req;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.insurancerecommender.util.InsuranceRecConstants;

/**
 * @author deepak.surapaneni
 *
 */
public class GetFeatureWeights implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetFeatureWeights.class.getName());
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static JsonNode riskDocuments = objectMapper.createObjectNode();
	static JsonNode lobDocuments = objectMapper.createObjectNode();
	static JsonNode responseConfig = objectMapper.createObjectNode();
	static ObjectMapper mapper = new ObjectMapper();
	{
		CBService serverConfigService = CBInstanceProvider
				.getServerConfigInstance();
		try {
			JsonArray paramConfig = JsonArray.create();
			paramConfig.add("Y");
			String riskDocQuery = InsuranceRecConstants.RISK_CONFIG;
			String lobQuery = InsuranceRecConstants.LOB_CONFIG;
			String resConfQuery = InsuranceRecConstants.RES_CONFIG;
			List<JsonObject> riskList = serverConfigService
					.executeConfigParamArrQuery(riskDocQuery, paramConfig);
			List<JsonObject> lobList = serverConfigService
					.executeConfigParamArrQuery(lobQuery, paramConfig);
			List<JsonObject> respConf = serverConfigService
					.executeConfigParamArrQuery(resConfQuery, paramConfig);
			riskDocuments = mapper.readTree(riskList.toString());
			lobDocuments = mapper.readTree(lobList.toString());
			responseConfig = mapper.readTree(respConf.toString());
		} catch (Exception e) {
			log.info("Failed to load Log Config Document" + e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(Exchange exchange) throws Exception {
		String inputRequest = exchange.getIn().getBody().toString();
		JsonNode inputRequestNode = GetFeatureWeights.objectMapper
				.readTree(inputRequest);
		JsonNode quoteParam = inputRequestNode.get("quoteParam");
		ObjectNode riskWeightList = mapper.createObjectNode();
		ObjectNode lobWeightList = mapper.createObjectNode();
		ObjectNode riskIdList = mapper.createObjectNode();
		ObjectNode lobIdList = mapper.createObjectNode();
		float riskPct = 0;
		float lobRisk = 0;
		int nonNullCount = 0;
	/*	try
	    {
	      Map<String, String> quoteParamMap = (Map)new ObjectMapper().readValue(
	        quoteParam.toString(), HashMap.class);
	      for (JsonNode risk : riskDocuments)
	      {
	        riskPct = getRisk(quoteParamMap, risk.get("RiskFactorList"));
	        riskIdList.put(
	          risk.get("riskName").toString().replaceAll("\\s+", ""), 
	          risk.get("riskId").toString());
	        riskWeightList.put(
	          risk.get("riskName").toString().replaceAll("\\s+", ""), 
	          riskPct);
	      }
	      for (JsonNode lob : lobDocuments)
	      {
	        lobRisk = 0.0F;
	        nonNullCount = 0;
	        for (JsonNode risk : lob.get("RiskDetails"))
	        {
	          if (risk.get("riskWeightage").asInt() != 0) {
	            nonNullCount++;
	          }
	          lobRisk = lobRisk + riskWeightList.get(
	            risk.get("riskName").toString()
	            .replaceAll("\\s+", "")).floatValue() * risk
	            .get("riskWeightage").asInt();
	        }
	        if (nonNullCount != 0) {
	          lobRisk = lobRisk / nonNullCount * 10.0F;
	        }
	        lobIdList.put(lob.get("insuranceType").toString().replaceAll("\\s+", ""), 
	          lob.get("insuranceId").toString());
	        lobWeightList.put(lob.get("insuranceType").toString().replaceAll("\\s+", ""), lobRisk);
	      }*/
		
/*Only changes is replace tostring() method used astext()
Added by Kuldeep,Pravin*/
		
		try{
			Map<String, String> quoteParamMap = new ObjectMapper().readValue(
					quoteParam.toString(), HashMap.class);
			for (JsonNode risk : riskDocuments) {
				riskPct = getRisk(quoteParamMap, risk.get("RiskFactorList"));
				riskIdList.put(
						risk.get("riskName").asText().replaceAll("\\s+", ""),
						risk.get("riskId").asText());
				riskWeightList.put(
						risk.get("riskName").asText().replaceAll("\\s+", ""),
						riskPct);
			log.info("risk weight list details : "+riskWeightList);
			}
			
			for (JsonNode lob : lobDocuments) {
				lobRisk = 0;
				nonNullCount = 0;
				for (JsonNode risk : lob.get("RiskDetails")) {
					log.info("risk details interation : "+risk);
					if (risk.get("riskWeightage").asInt() != 0) {
						nonNullCount++;
					}
					lobRisk = lobRisk
							+ (riskWeightList.get(
									risk.get("riskName").asText()
											.replaceAll("\\s+", "")).floatValue() * risk
									.get("riskWeightage").asInt());
				}
				if (nonNullCount != 0) {
					lobRisk = (lobRisk / nonNullCount * 10);
				}
				lobIdList.put(lob.get("insuranceType").asText().replaceAll("\\s+", ""),
						lob.get("insuranceId").asText());
				lobIdList.put(lob.get("insuranceType").asText().replaceAll("\\s+", ""),
						lob.get("insuranceId").asText());
				lobWeightList.put(lob.get("insuranceType").asText().replaceAll("\\s+", ""), lobRisk);
			}
			ObjectNode response = generateResponse(riskWeightList, lobWeightList,
					riskIdList, lobIdList, quoteParamMap);
			JsonNode root =  objectMapper.createObjectNode();
			ArrayNode algResponse =  objectMapper.createArrayNode();
			algResponse.add(response);
			((ObjectNode) root).set("algResponse", algResponse);
			exchange.getIn().setBody(root);
		}
		catch(Throwable e){
			log.info("Failed to Calculate LOB" + e);
		}
	}

	public float getRisk(Map<String, String> quoteParam, JsonNode riskFactorList) {
		int featureWeight = 0;
		int valueWeight = 0;
		float Y = 0;
		try{
			JsonNode riskSubFactorList = objectMapper.createObjectNode();
			for (Map.Entry<String, String> entry : quoteParam.entrySet()) {
				for (JsonNode riskFactor : riskFactorList) {
					if (riskFactor.get("riskFactorName").asText()
							.equalsIgnoreCase(entry.getKey().toString())) {
						featureWeight = riskFactor.get("riskFactorWeight").asInt();
						riskSubFactorList = riskFactor.get("RiskSubFactorList");
						for (JsonNode subFacctor : riskSubFactorList) {
							if (subFacctor.get("riskSubFactorName").asText()
									.equalsIgnoreCase(entry.getValue().toString())) {
								valueWeight = subFacctor.get("riskSubFactorWeight")
										.asInt();
								Y = Y + (featureWeight * valueWeight);
								break;
							}
						}
						break;
					}
				}
			}
			Y = Y / (1000);
			return Y;
		}
		catch(Exception e){
			log.info("Failed to Calculate Risk Value" + e);
			return Y;
		}
	}

	@SuppressWarnings({ "unchecked", "finally" })
	public ObjectNode generateResponse(ObjectNode riskWeightList,
			ObjectNode lobWeightList, ObjectNode riskIdList,
			ObjectNode lobIdList, Map<String, String> quoteParam) throws Throwable {
		ArrayNode highRiskBucket = mapper.createArrayNode();
		ArrayNode lowRiskBucket = mapper.createArrayNode();
		ArrayNode mediumRiskBucket = mapper.createArrayNode();

		ArrayNode minimumBucket = mapper.createArrayNode();
		ArrayNode recommendedBucket = mapper.createArrayNode();
		ArrayNode comprehensiveBucket = mapper.createArrayNode();
		
		ArrayNode riskAnalysisArray = mapper.createArrayNode();
		ArrayNode lobAnalysisArray = mapper.createArrayNode();

		ObjectNode response = objectMapper.createObjectNode();

		Map<String, Double> riskWeightMap = new ObjectMapper().readValue(
				riskWeightList.toString(), HashMap.class);
		Map<String, Double> lobWeightMap = new ObjectMapper().readValue(
				lobWeightList.toString(), HashMap.class);
		String riskCat = "";
		try{
			for (Map.Entry<String, Double> risk : riskWeightMap.entrySet()) {
				ObjectNode tempRiskWeightList = mapper.createObjectNode();
				tempRiskWeightList.removeAll();
				tempRiskWeightList.put("riskId",riskIdList.get(risk.getKey().toString()));
				tempRiskWeightList.put("riskName", risk.getKey().toString());
				tempRiskWeightList.put("riskValue", risk.getValue().floatValue());
				if (risk.getValue().floatValue() * 100 > 60) {
					riskCat = "High";
					tempRiskWeightList.put("riskCat", riskCat);
					highRiskBucket.add(tempRiskWeightList);
				} else if (risk.getValue().floatValue() * 100 > 30
						&& risk.getValue().floatValue() * 100 <= 60) {
					riskCat = "Medium";
					tempRiskWeightList.put("riskCat", riskCat);
					mediumRiskBucket.add(tempRiskWeightList);
				} else if (risk.getValue().floatValue() * 100 <= 30) {
					riskCat = "Low";
					tempRiskWeightList.put("riskCat", riskCat);
					lowRiskBucket.add(tempRiskWeightList);
//					tempRiskWeightList.removeAll();
				}
			}

			for (Map.Entry<String, Double> lob : lobWeightMap.entrySet()) {
				ObjectNode tempLobWeightList = mapper.createObjectNode();
				String insuranceCat = "";
				tempLobWeightList.removeAll();
				tempLobWeightList.put("insuranceId",lobIdList.get(lob.getKey().toString()));
				tempLobWeightList.put("insuranceName", lob.getKey().toString());
				tempLobWeightList.put("insuranceValue", lob.getValue().floatValue());
				if (lob.getValue().floatValue() >= 40) {
					insuranceCat = "Minimum";
//					tempLobWeightList.put("insuranceCat", insuranceCat);
					tempLobWeightList.put("sumAssured",caclSumAssured(quoteParam,insuranceCat,lob.getKey().toString()));
					minimumBucket.add(tempLobWeightList);
				}
				if (lob.getValue().floatValue() >= 35) {
					insuranceCat = "Recommended";
//					tempLobWeightList.put("insuranceCat", insuranceCat);
					tempLobWeightList.put("sumAssured",caclSumAssured(quoteParam,insuranceCat,lob.getKey().toString()));
					recommendedBucket.add(tempLobWeightList);
				}
				if (lob.getValue().floatValue() >= 30) {
					insuranceCat = "Comprehensive";
//					tempLobWeightList.put("insuranceCat", insuranceCat);
					tempLobWeightList.put("sumAssured",caclSumAssured(quoteParam,insuranceCat,lob.getKey().toString()));
					comprehensiveBucket.add(tempLobWeightList);
				}
			}

			for (JsonNode respConfig : responseConfig) {
				JsonNode resConfig = respConfig.get("responseConfig");
				for(JsonNode riskConf : resConfig.get("riskConfig")) {
					Map<String, String> riskConfig = new ObjectMapper().readValue(
							riskConf.toString(), HashMap.class);
					ObjectNode riskAnalysis = mapper.createObjectNode();
					for (Map.Entry<String, String> config : riskConfig.entrySet()) {
						riskAnalysis.put(config.getKey(), config.getValue());
						if (config.getValue().equalsIgnoreCase("high")) {
							riskAnalysis.put("applicableRisk", highRiskBucket);
						} else if (config.getValue().equalsIgnoreCase("medium")) {
							riskAnalysis.put("applicableRisk", mediumRiskBucket);
						} else if (config.getValue().equalsIgnoreCase("low")) {
							riskAnalysis.put("applicableRisk", lowRiskBucket);
						}
					}
					riskAnalysisArray.add(riskAnalysis);
				}
				for(JsonNode lobConf : resConfig.get("lobConfig")) {
					Map<String, String> lobConfig = new ObjectMapper().readValue(
							lobConf.toString(), HashMap.class);
					ObjectNode lobAnalysis = mapper.createObjectNode();
					for (Map.Entry<String, String> config : lobConfig.entrySet()) {
						lobAnalysis.put(config.getKey(), config.getValue());
						if (config.getValue().equalsIgnoreCase("Comprehensive")) {
							lobAnalysis.put("applicableInsurance", comprehensiveBucket);
						} else if (config.getValue().equalsIgnoreCase("Recommended")) {
							lobAnalysis.put("applicableInsurance", recommendedBucket);
						} else if (config.getValue().equalsIgnoreCase("Minimum")) {
							lobAnalysis.put("applicableInsurance", minimumBucket);
						}
					}	
					lobAnalysisArray.add(lobAnalysis);
				}
				((ObjectNode) response).set("riskAnalysis", riskAnalysisArray);
				((ObjectNode) response).set("lobAnalysis", lobAnalysisArray);
				return response;
			}
		}
		catch(Exception e){
			log.info("Failed to create Response");
			return response;
		}
		finally{
			finalize();	
			return response;
		}

	}
	
	@SuppressWarnings("unchecked")
	public double caclSumAssured(Map<String, String> quoteParam, String insuranceCat, String insuranceName) throws JsonParseException, JsonMappingException, IOException{
		double sumAssured=0;
		int temp = 0;
		JsonNode factorList = objectMapper.createObjectNode();
		JsonNode multiplierList = objectMapper.createObjectNode();
		for (JsonNode respConfig : responseConfig) {
			JsonNode resConfig = respConfig.get("responseConfig");
			for(JsonNode incomeFactor : resConfig.get("incomeFactor")) {
				log.info("insuranceNameIs"+insuranceName);
				log.info("insuranceNameConfIs"+incomeFactor.get("insuranceName").toString().replaceAll("\\s+", ""));
				if(incomeFactor.get("insuranceName").toString().replaceAll("\\s+", "").equalsIgnoreCase(insuranceName)) {
					factorList = incomeFactor.get("factors");
					multiplierList = incomeFactor.get("multiplier");
					for(JsonNode multiplier: multiplierList){
						log.info("multiplierIs"+multiplier);
						log.info("insuranceCatIs"+insuranceCat);
						log.info("insuranceCatConfIS"+multiplier.get("insuranceCat").asText());
						log.info("incomeFactorIs"+multiplier.get("incomeFactor"));
						log.info("incomeFactorFloatIs"+multiplier.get("incomeFactor").floatValue());
						if( multiplier.get("insuranceCat").asText().equalsIgnoreCase(insuranceCat)){
							sumAssured = multiplier.get("incomeFactor").floatValue();
							for(JsonNode factor: factorList){
								log.info("sumAssured"+sumAssured);
								log.info("factorNameIs"+factor.get("factorName").toString());
								log.info("factorValueIs"+quoteParam.get(factor.get("factorName").asText()));
//								log.info("annualIncomeFloatIs"+Integer.parseInt(quoteParam.get(factor.get("factorName").toString()).toString()));
								temp = Integer.parseInt(quoteParam.get(factor.get("factorName").asText()));
								sumAssured = sumAssured * temp;
							}
						break;}
					}
				break;}
				
			}
		}
		return sumAssured;
		
	}

}
