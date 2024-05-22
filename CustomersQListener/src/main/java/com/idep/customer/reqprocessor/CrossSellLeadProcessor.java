package com.idep.customer.reqprocessor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.customer.service.CrossSellLeadService;
import com.idep.customer.util.CustomerConstants;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;


public class CrossSellLeadProcessor implements Processor{
	Logger log = Logger.getLogger(CrossSellLeadProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	CustomerDataPrepare customerService = new CustomerDataPrepare();
	CrossSellLeadService crossSellLeadService = new CrossSellLeadService();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	static String crossSellConfig = null;

	static
	{
		crossSellConfig = serverConfig.getDocBYId(CustomerConstants.CROSS_SELL_CONFIG).content().toString();
	}

	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {
		ObjectNode leadReqHeader = objectMapper.createObjectNode();
		ObjectNode leadReqBody = objectMapper.createObjectNode();
		ObjectNode contactInfo = objectMapper.createObjectNode();
		ObjectNode leadReqNode = objectMapper.createObjectNode();
		DataProvider dataProvider = new DataProvider();
		JsonNode proposalNode = null;

		JsonNode crossSellConfigNode = objectMapper.readTree(crossSellConfig);
		log.info("exchange request :"+exchange.getProperty("request").toString());
		String request = exchange.getProperty("request").toString();

		try{
			if(crossSellConfigNode == null){
				crossSellConfigNode = objectMapper.readTree(serverConfig.getDocBYId(CustomerConstants.CROSS_SELL_CONFIG).content().toString());
				log.info("crossSellConfigNode :"+crossSellConfigNode);
			}

			if(request != null){
				proposalNode = objectMapper.readTree(policyTransaction.getDocBYId(request).content().toString());
			}

			JsonNode leadQuoteDoc = dataProvider.getQuoteDoc(proposalNode.findValue("QUOTE_ID").asText());
			JsonNode contactInfoNode = crossSellConfigNode.get("crossSellLeadConfig").get("contactInfo");
			if (proposalNode.has("businessLineId") && proposalNode.get("businessLineId") != null){
				if(proposalNode.get("businessLineId").asInt() == 1){

				}else if(proposalNode.get("businessLineId").asInt() == 2) {
					leadReqBody.put("quoteParam",leadQuoteDoc.findValue("quoteParam"));
					leadReqBody.put("vehicleInfo",leadQuoteDoc.findValue("vehicleInfo"));
					leadReqBody.put("contactInfo",dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerDetails"), objectMapper.readValue(contactInfoNode.get("Bike").toString(),Map.class )));
				}else if(proposalNode.get("businessLineId").asInt() == 3) {
					leadReqBody.put("quoteParam",leadQuoteDoc.findValue("quoteParam"));
					leadReqBody.put("vehicleInfo",leadQuoteDoc.findValue("vehicleInfo"));
					leadReqBody.put("contactInfo",dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerDetails"), objectMapper.readValue(contactInfoNode.get("Car").toString(),Map.class )));
				}else if(proposalNode.get("businessLineId").asInt() == 4) {
					for ( JsonNode request1 : leadQuoteDoc.findValue("quoteParamRequestList")){
						leadReqBody.put("quoteParam",request1);
						break;
					}
					leadReqBody.put("contactInfo",dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerInfo"), objectMapper.readValue(contactInfoNode.get("Health").toString(),Map.class )));
				}
			}

			((ObjectNode) leadReqBody.get("contactInfo")).put("termsCondition",true);
			((ObjectNode) leadReqBody.get("contactInfo")).put("createLeadStatus",false);
			Map<String,String> crossSellConfigNodeMap = objectMapper.readValue(crossSellConfigNode.get("crossSellLeadConfig").get("otherDetails").toString(), Map.class);
			leadReqBody = dataProvider.putCustomeFields(leadReqBody, crossSellConfigNodeMap);
			crossSellConfigNodeMap = objectMapper.readValue(crossSellConfigNode.get("crossSellLeadConfig").get("header").toString(), Map.class);
			leadReqHeader = dataProvider.putCustomeFields(leadReqHeader, crossSellConfigNodeMap);

			leadReqNode.put("header", leadReqHeader);
			leadReqNode.put("body", leadReqBody);

			/*
			if(reqNode.has("leadId") && reqNode.get("leadId") != null){
				leadData = objectMapper.createObjectNode();
				log.info("Lead Id :"+reqNode.get("leadId"));
				if(proposalNode.has("businessLineId")){
					log.info("potential LOB :"+crossSellConfigNode.get("potentialLOBConfig").get(String.valueOf((proposalNode.get("businessLineId").asInt()))).get("potentialLOB"));
					leadData.put("potential_lob", crossSellConfigNode.get("potentialLOBConfig").get(String.valueOf((proposalNode.get("businessLineId").asInt()))).get("potentialLOB").asText());
				}
			}
			 */
			//leadId = crmService.updateLead(leadData, reqNode.get("leadId").asText());
			//String status = crmService.createModuleRelation(leadId, reqNode.get("policyId").asText(), "Leads", "leads_aos_contracts_1");
			log.info("update lead with Potential LOB :"+leadReqNode);
			 JsonNode leadRes = crossSellLeadService.createCrossSellLead(leadReqNode);
			 log.info("Cross Sell Lead :"+leadRes);
			

		}
		catch(Exception e){
			log.error("Error in CrossSellLeadProcessor :",e);
			e.printStackTrace();
		}

	}
}