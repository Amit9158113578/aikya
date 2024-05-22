package com.idep.pospservice.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetDashBoardInfo implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	Logger log = Logger.getLogger(GetAdminDetails.class.getName()); 
	JsonNode errorNode;
	private Object organisationName;
	static JsonNode queryConfig;
	static JsonNode adminDashQueryConfig;
	static {
		try {
			queryConfig = objectMapper.readTree(pospData.getDocBYId("POSPQueryServerConfig").content().toString());
			adminDashQueryConfig = objectMapper.readTree(pospData.getDocBYId("AdminDashBoardQueryConfig").content().toString());
		} catch (Exception e) {
			Logger.getLogger(GetAdminDetails.class.getName()).error("Unabele to cache POSPQueryServerConfig : ",e);
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
			try{
				JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
				JsonNode userAppConfig = null;
				ObjectNode resNode = objectMapper.createObjectNode();
				if (requestNode.has("agentId")){
					
					String query = queryConfig.get("getAgentProfile").asText();
					
					JsonArray paramobj = JsonArray.create();
					paramobj.add(requestNode.get("agentId").asText());
					JsonNode groupConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
					log.info("User App Config Found : "+groupConfig);
					if(groupConfig!=null){
						
						userAppConfig =groupConfig.get(0).get("DashBoard");
						if(userAppConfig.size()== 0){
							log.error("User Profile not found : "+requestNode);
							
							resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_CODE).asInt());
							resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_MSG).asText());
							resNode.put(POSPServiceConstant.RES_DATA,errorNode);
							exchange.getIn().setBody(resNode);
						throw new ExecutionTerminator();	
						}				
					}else{
						log.error("unable to get User Group for : "+requestNode.get("agentId"));
					}
					resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					resNode.put(POSPServiceConstant.RES_DATA,userAppConfig);
					
				}else if (requestNode.has("adminId")){
					String query = queryConfig.get("adminDetails").get("paramQuery").asText();
					JsonArray paramobj = JsonArray.create();
					paramobj.add(requestNode.get("adminId").asText());
					log.info("Query Admin data : "+query);
					log.info("Query Admin data param Obj : "+paramobj);
					JsonNode groupConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
					log.info("Admin App Config Found : "+groupConfig);
				    
					JsonNode adminProfileNode = objectMapper.readTree(groupConfig.get(0).toString());
					
					log.info("Admin Profile Node : "+adminProfileNode);
					if(adminDashQueryConfig==null){
						adminDashQueryConfig = objectMapper.readTree(pospData.getDocBYId("AdminDashBoardQueryConfig").content().toString());
					}
					ObjectNode adminBashbaord = objectMapper.createObjectNode();
					if(adminProfileNode.has("organisationName")){
						log.info("organisationName : "+adminProfileNode.get("organisationName").asText());
						JsonArray AgentCountOrgWise = JsonArray.create();
						AgentCountOrgWise.add(adminProfileNode.get("organisationName").asText());
						JsonNode AgentCount = objectMapper.readTree(pospData.executeConfigParamArrQuery(adminDashQueryConfig.get("AgentCount").get("AgentCountOrgWise").asText(),AgentCountOrgWise).toString());
						log.info("AgentCount Found : "+AgentCount);
						log.info("Execution Started for LeadCount : ");
						JsonArray LeadCountParam = JsonArray.create();
						LeadCountParam.add(adminProfileNode.get("organisationName").asText());
						JsonNode LeadCount = objectMapper.readTree(pospData.executeConfigParamArrQuery(adminDashQueryConfig.get("LeadCount").get("LeadCountOrgWise").asText(),LeadCountParam).toString());
						log.info("Lead Count Found : "+LeadCount);
						JsonArray saleCountParam = JsonArray.create();
						saleCountParam.add(adminProfileNode.get("organisationName").asText());
						JsonNode saleCount = objectMapper.readTree(pospData.executeConfigParamArrQuery(adminDashQueryConfig.get("SaleCount").get("SaleCountOrgWise").asText(),saleCountParam).toString());
						log.info("saleCount Count Found : "+saleCount);
						JsonArray TranningCountParam = JsonArray.create();
						TranningCountParam.add(adminProfileNode.get("organisationName").asText());
						JsonNode TranningCount = objectMapper.readTree(pospData.executeConfigParamArrQuery(adminDashQueryConfig.get("TranningCount").get("TranningCountOrgWise").asText(),TranningCountParam).toString());
						log.info("TranningCount  Found : "+TranningCount);
						adminBashbaord.put("agentCount",AgentCount.get(0).get("agentCount").asText());
						adminBashbaord.put("LeadCount",LeadCount.get(0).get("LeadCount").asText());
						adminBashbaord.put("salesCount",saleCount.get(0).get("saleCount").asText());
						adminBashbaord.put("TranningCount",TranningCount.get(0).get("TranningCount").asText());
						resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						resNode.put(POSPServiceConstant.RES_DATA,adminBashbaord);
					}else{
						
						JsonNode AgentCount = objectMapper.readTree(pospData.executeQuery(adminDashQueryConfig.get("AgentCount").get("AllAgentCount").asText()).toString());
						log.info("All AgentCount Found : "+AgentCount);
						log.info("Execution Started for All LeadCount : ");
						JsonNode LeadCount = objectMapper.readTree(pospData.executeQuery(adminDashQueryConfig.get("LeadCount").get("AllLeadCount").asText()).toString());
						log.info("Lead Count Found : "+LeadCount);
						JsonNode saleCount = objectMapper.readTree(pospData.executeQuery(adminDashQueryConfig.get("SaleCount").get("AllSaleCount").asText()).toString());
						log.info("All sale Count Found : "+saleCount);
						JsonNode TranningCount = objectMapper.readTree(pospData.executeQuery(adminDashQueryConfig.get("TranningCount").get("AllTranningCount").asText()).toString());
						log.info("All TranningCount  Found : "+TranningCount);
						adminBashbaord.put("agentCount",AgentCount.get(0).get("agentCount").asText());
						adminBashbaord.put("LeadCount",LeadCount.get(0).get("LeadCount").asText());
						adminBashbaord.put("salesCount",saleCount.get(0).get("saleCount").asText());
						adminBashbaord.put("TranningCount",TranningCount.get(0).get("TranningCount").asText());
						resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						resNode.put(POSPServiceConstant.RES_DATA,adminBashbaord);
					}
				}else {
					log.error("unable to get user Group and configurration : "+requestNode);
				}
				
				
				
				
				exchange.getIn().setBody(resNode);
				
			}catch(Exception e){
				log.error("unable to send user group config : ",e);
				ObjectNode resNode = objectMapper.createObjectNode();
				resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_CODE).asInt());
				resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_MSG).asText());
				resNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(resNode);
				throw new ExecutionTerminator();
			}
			
			

			}

}
