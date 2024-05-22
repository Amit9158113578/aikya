package com.idep.insassessment;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.insassessment.util.ProfessionalRecomServiceConstants;

public class RiskAssessmentConfigData{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(RiskAssessmentConfigData.class.getName());
	JsonNode docConfigNode = objectMapper.createObjectNode();
	static JsonNode riskDocuments = objectMapper.createObjectNode();


	static JsonNode riskValueToCategoryMappingDoc = objectMapper.createObjectNode();
	static JsonNode insValueToCategoryMappingDoc = objectMapper.createObjectNode();
	static JsonNode responseMappingDoc = objectMapper.createObjectNode();
	static JsonNode riderDocuments = objectMapper.createObjectNode();

	static JsonNode insuranceDocuments = objectMapper.createObjectNode();

	static JsonNode insSIConfigDoc = objectMapper.createObjectNode();
	static JsonNode riderSIConfigDoc = objectMapper.createObjectNode();
	static JsonNode lobSupportedRisksConfigDoc = objectMapper.createObjectNode();

	static JsonNode riskValToCatDoc = objectMapper.createObjectNode();
	static JsonNode insValToCatDoc = objectMapper.createObjectNode();
	static JsonNode insrecomresponsedoc = objectMapper.createObjectNode();
	static JsonNode insSIDoc = objectMapper.createObjectNode();

	JsonNode responseConfig = objectMapper.createObjectNode();

	static{
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		try {
		    log.info("*****STATIC BLOCK EXECUTION STARTED*********");
			//	System.out.println("RiskAssessmentConfigData:::::::::");
			JsonArray paramConfig = JsonArray.create();
			paramConfig.add("Y");
			String riskDocQuery = ProfessionalRecomServiceConstants.RISK_CONFIG;
			String insuranceDocQuery = ProfessionalRecomServiceConstants.INSURANCE_CONFIG;
			String riderDocQuery = ProfessionalRecomServiceConstants.RIDER_CONFIG;
			String riderSIConfigDocQuery = ProfessionalRecomServiceConstants.RIDER_SI_CONFIG;

			/**String riskCategoryMappingDocQuery = ProfessionalRecomServiceConstants.RISK_VALUETOCATEGORY_CONFIG;
			String insCategoryMappingDocQuery = ProfessionalRecomServiceConstants.INSURANCE_VALUETOCATEGORY_CONFIG;
			String InsuranceRecomResponseConfigQuery = ProfessionalRecomServiceConstants.INS_RECOM_RESPONSE_CONFIG;
			String InsuranceSIConfigQuery = ProfessionalRecomServiceConstants.INSURANCE_SI_CONFIG;

**/
			List<JsonObject> riderList = serverConfigService.executeConfigParamArrQuery(riderDocQuery, paramConfig);
			riderDocuments = objectMapper.readTree(riderList.toString());

			List<JsonObject> riderSiConfigList = serverConfigService.executeConfigParamArrQuery(riderSIConfigDocQuery, paramConfig);
			riderSIConfigDoc = objectMapper.readTree(riderSiConfigList.toString());

			List<JsonObject> riskList = serverConfigService.executeConfigParamArrQuery(riskDocQuery, paramConfig);
			riskDocuments = objectMapper.readTree(riskList.toString());

			List<JsonObject> insuranceList = serverConfigService.executeConfigParamArrQuery(insuranceDocQuery, paramConfig);
			insuranceDocuments = objectMapper.readTree(insuranceList.toString());

			
			/**List<JsonObject> riskValueToCategoryDoc = serverConfigService.executeConfigParamArrQuery(riskCategoryMappingDocQuery, paramConfig);
			riskValToCatDoc = objectMapper.readTree(riskValueToCategoryDoc.toString());
			//System.out.println("riskValToCatDoc"+riskValToCatDoc);
			List<JsonObject> insuranceValueToCategoryDoc = serverConfigService.executeConfigParamArrQuery(insCategoryMappingDocQuery, paramConfig);
			insValToCatDoc = objectMapper.readTree(insuranceValueToCategoryDoc.toString());
			//System.out.println("insValToCatDoc"+insValToCatDoc);
			List<JsonObject> insuranceRecomResponseConfigDoc = serverConfigService.executeConfigParamArrQuery(InsuranceRecomResponseConfigQuery, paramConfig);
			insrecomresponsedoc = objectMapper.readTree(insuranceRecomResponseConfigDoc.toString());
			//System.out.println("insrecomresponsedoc"+insrecomresponsedoc);

			List<JsonObject> insuranceSIConfigDoc = serverConfigService.executeConfigParamArrQuery(InsuranceSIConfigQuery, paramConfig);
			insSIDoc = objectMapper.readTree(insuranceSIConfigDoc.toString());
			//System.out.println("insSIDoc"+insSIDoc);
**/

			//	documents retreived by GET DOC BY ID  from CB
			riskValToCatDoc = objectMapper.readTree(serverConfigService.getDocBYId("RiskValueToCategoryMapping").content().toString());
			insValToCatDoc = objectMapper.readTree(serverConfigService.getDocBYId("InsuranceValueToCategoryMapping").content().toString());
			insrecomresponsedoc = objectMapper.readTree(serverConfigService.getDocBYId("ProfessionalInsRecomResponseConfig").content().toString());
			insSIDoc = objectMapper.readTree(serverConfigService.getDocBYId("InsuranceSIConfiguration").content().toString());
			
		    log.info("*****STATIC BLOCK EXECUTION ENDED*********");

		} catch (Exception e) {
			log.info("Failed to load  Config Document" + e);
		}
	}
}