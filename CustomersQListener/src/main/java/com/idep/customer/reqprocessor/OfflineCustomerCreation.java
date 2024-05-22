package com.idep.customer.reqprocessor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class OfflineCustomerCreation implements Processor{
	static Logger log = Logger.getLogger(OfflineCustomerCreation.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	CustomerDataPrepare customerService = new CustomerDataPrepare();
	DataProvider dataProvider = new DataProvider();
	static JsonNode customerConfig = null;

	static {
		try {
			if(customerConfig == null)
				customerConfig = objectMapper.readTree(serverConfig.getDocBYId("CustomerConfiguration").content().toString());
		} catch (IOException e) {
			log.error("unable to fetch doc CustomerConfiguration",e);
		}
	}
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);

		if(reqNode.has("policyList") && reqNode.get("policyList")!= null){
			for(JsonNode policyNode : reqNode.get("policyList")){
				String customerId = null;
				String policyId = null;
				String relationship = null; 
				log.info("Offline Policy Node :"+policyNode);
				String findPolicyQuery = dataProvider.prepareQuery(policyNode,customerConfig.get("offlineCustomerConfig").get("policyQueryConfig"));
				policyId = crmService.findModuleRecord(findPolicyQuery, "AOS_Contracts", "id");
				if(policyId.length() > 0) {
					log.info(policyNode.get("Policy_Number")+" policy already created with id:"+policyId);
					continue;
				}

				String findCustomerQuery = dataProvider.prepareQuery(policyNode,customerConfig.get("offlineCustomerConfig").get("customerQueryConfig"));
				customerId = crmService.findModuleRecord(findCustomerQuery, "Accounts", "id");
				log.info("customerId :"+customerId);
				if(customerId.length() < 1) {
					ObjectNode custDataNode = customerService.prepareCustomerDataSet(policyNode,customerConfig.get("offlineCustomerConfig").get("customerConfig"));
					log.info("custDataNode :"+custDataNode);
					customerId = crmService.createCustomer(custDataNode);
					log.info("customerId created :"+customerId);
				}
				ObjectNode policyDataNode = customerService.prepareCustomerDataSet(policyNode,customerConfig.get("offlineCustomerConfig").get("policyConfig"));
				if(reqNode.has("source")){
					policyDataNode.put("source_c", reqNode.get("source").asText());
				}
				if(policyNode.has("endDate") && policyNode.get("endDate") != null){
					policyDataNode.put("end_date", customerService.convertDate(policyNode.get("endDate").asText()));
				}
				if(policyNode.has("startDate") && policyNode.get("startDate") != null){
					policyDataNode.put("start_date", customerService.convertDate(policyNode.get("startDate").asText()));
					policyDataNode.put("date_entered", customerService.convertDate(policyNode.get("startDate").asText()));
				}

				log.info("policyDataNode :"+policyDataNode);
				policyId = crmService.createPolicy(policyDataNode);
				log.info("policyId created : "+policyId);
				if(policyId.length()>0 && customerId.length()>0){
					relationship = crmService.createCustomerPolicyRelation(policyId,customerId);
				}
				log.info("policy and customer relationship created : "+relationship);
			}
		}
	}
}
