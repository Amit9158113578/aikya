package com.idep.customer.reqprocessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.customer.util.CustomerConstants;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;


public class CustomerRequestProcessor implements Processor{
	static Logger log = Logger.getLogger(CustomerRequestProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	//JsonNode reqInfoNode;
	static SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	static CustomerDataPrepare customerService =new CustomerDataPrepare();
	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {
		CBService serverConfig = null;
		CBService policyTransaction = null;
		JsonNode customerConfigNode = null;
		JsonNode carrierNode = null;
		JsonNode proposalNode = null;
		ObjectNode policyDataNode=null;
		String leadSearchQuery = null;
		String date_entered = null;	
		String customerId = null;
		String policyId = null;
		String status = null;
		ObjectNode leadData = null;

		try{
			if(serverConfig == null){
				serverConfig = CBInstanceProvider.getServerConfigInstance();
				policyTransaction = CBInstanceProvider.getPolicyTransInstance();
				log.info("Customer configuration loaded");
				customerConfigNode = objectMapper.readTree(serverConfig.getDocBYId("CustomerConfiguration").content().toString());
			}
			// Find lead to get Assigned agent 
			String request = exchange.getIn().getBody().toString();
			exchange.setProperty("request", request);
			JsonNode reqNode = objectMapper.readTree(request);
			log.info(" Customer reqNode :"+request);
			log.info(" Customer customerConfigNode :"+customerConfigNode);
			if(reqNode !=null && reqNode.has("proposalId") && reqNode.get("proposalId") != null){
				proposalNode = objectMapper.readTree(policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
				if(proposalNode.has("paymentResponse")){
					if(proposalNode.get("paymentResponse").has("updatedDate") && proposalNode.get("paymentResponse").get("updatedDate").asText() != null){
						date_entered = proposalNode.get("paymentResponse").get("updatedDate").asText();
						log.info("Got Date Entered : "+date_entered);
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CustomerConstants.PROPOSAL_DATE_FORMAT);
						Date date = simpleDateFormat.parse(date_entered);
						date_entered = new SimpleDateFormat(CustomerConstants.POLICY_DATE_ENTERED_FORMAT).format(date);
						log.info("After Got Date Entered : "+date_entered);
					}
				}

				// End of date_entered

				/*
				 * Retrieving carrier name
				 */
				carrierNode=objectMapper.readTree(serverConfig.getDocBYId("Carrier-"+proposalNode.findValue("carrierId")).content().toString());
				((ObjectNode) proposalNode).put("CarrierName",carrierNode.get("carrierName"));
				log.info("PROPOSAL NO. FOUND :"+proposalNode.findValue("proposalId"));

				//userProfileNode = objectMapper.readTree(policyTransaction.getDocBYId("PolicyDetails-"+proposalNode.findValue("mobile").asText()).content().toString());
				//log.info("User Profile Node :"+userProfileNode);

			}
			else{
				log.info("Proposal Id Not Found to Create Customer And Policy");
			}

			ObjectNode custDataNode = customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("customerConfig"));
			log.info("Data Prepared to create customer in crm :"+custDataNode);
			customerId = customerService.isCustomerExist(custDataNode,customerConfigNode.get("CustomerValidate"));
			log.info("Is customer exists,customerId :"+customerId);

			if(proposalNode.has("businessLineId") && proposalNode.get("businessLineId")!=null)
			{				
				if(proposalNode.get("businessLineId").asInt()==1){
					policyDataNode=customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("lifepolicyConfig"));
					JsonNode policyPersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("lifePersonalDetails"));
					Map<String, String> personalDataNodeMap = objectMapper.readValue(policyPersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : personalDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}

					JsonNode policyNumberDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("lifeProposalResponse"),customerConfigNode.get("lifePolicyNumber"));
					Map<String, String> policyNumberDataNodeMap=objectMapper.readValue(policyNumberDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : policyNumberDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}	
					// Policy issue date/start date not available at this moment so leaved blank
					//policyDataNode.put("policy_issue_date_c", proposalNode.findValue("policyStartDate"));
					policyDataNode.put("contract_type", CustomerConstants.CONTRACT_TYPE_LIFE);

					JsonNode customePersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("customerPersonalDetails"));
					Map<String, String> customePersonalDataNodeMap=objectMapper.readValue(customePersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : customePersonalDataNodeMap.entrySet()){
						custDataNode.put(field.getKey(), field.getValue());		   
					}
					custDataNode.put("line_of_business_c", CustomerConstants.LIFE_LINE_OF_BUSINESS);
				}else if(proposalNode.get("businessLineId").asInt()==2){
					policyDataNode=customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("bikepolicyConfig"));
					JsonNode policyPersonalDataNode =customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("bikePersonalDetails"));
					Map<String, String> personalDataNodeMap=objectMapper.readValue(policyPersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : personalDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					JsonNode policyNumberDataNode =customerService.prepareCustomerDataSet(proposalNode.findValue("bikePolicyResponse"),customerConfigNode.get("bikePolicyNumber"));
					Map<String, String> policyNumberDataNodeMap=objectMapper.readValue(policyNumberDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : policyNumberDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}	
					policyDataNode.put("contract_type", CustomerConstants.CONTRACT_TYPE_BIKE);
					JsonNode customePersonalDataNode =customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("customerPersonalDetails"));
					Map<String, String> customePersonalDataNodeMap=objectMapper.readValue(customePersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : customePersonalDataNodeMap.entrySet()){
						custDataNode.put(field.getKey(), field.getValue());		   
					}
					custDataNode.put("line_of_business_c", CustomerConstants.BIKE_LINE_OF_BUSINESS);
				}else if(proposalNode.get("businessLineId").asInt()==3){
					policyDataNode = customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("carpolicyConfig"));
					JsonNode policyPersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("carPersonalDetails"));
					Map<String, String> personalDataNodeMap = objectMapper.readValue(policyPersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : personalDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					JsonNode policyNumberDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("carPolicyResponse"),customerConfigNode.get("carPolicyNumber"));
					Map<String, String> policyNumberDataNodeMap = objectMapper.readValue(policyNumberDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : policyNumberDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					policyDataNode.put("contract_type", CustomerConstants.CONTRACT_TYPE_CAR);

					JsonNode customePersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("customerPersonalDetails"));
					Map<String, String> customePersonalDataNodeMap = objectMapper.readValue(customePersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : customePersonalDataNodeMap.entrySet()){
						custDataNode.put(field.getKey(), field.getValue());		   
					}
					custDataNode.put("line_of_business_c", CustomerConstants.CAR_LINE_OF_BUSINESS);
				}else if(proposalNode.get("businessLineId").asInt()==4){
					log.info("In Health Policy");
					policyDataNode=customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("healthpolicyConfig"));
					log.info("policyPersonalDataNode");
					JsonNode policyPersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("personalInfo"),customerConfigNode.get("healthPersonalDetails"));
					log.info("personalDataNodeMap ");
					Map<String, String> personalDataNodeMap = objectMapper.readValue(policyPersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : personalDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					log.info("policyNumberDataNode");
					JsonNode policyNumberDataNode =customerService.prepareCustomerDataSet(proposalNode.findValue("healthPolicyResponse"),customerConfigNode.get("healthPolicyNumber"));
					Map<String, String> policyNumberDataNodeMap = objectMapper.readValue(policyNumberDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : policyNumberDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}	
					// Policy issue date not available at this moment so adding start date
					policyDataNode.put("policy_issue_date_c", proposalNode.findValue("policyStartDate"));
					policyDataNode.put("contract_type", CustomerConstants.CONTRACT_TYPE_HEALTH);

					JsonNode customePersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("personalInfo"),customerConfigNode.get("customerPersonalDetails"));
					Map<String, String> customePersonalDataNodeMap = objectMapper.readValue(customePersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : customePersonalDataNodeMap.entrySet()){
						custDataNode.put(field.getKey(), field.getValue());		   
					}
					custDataNode.put("line_of_business_c", CustomerConstants.HEALTH_LINE_OF_BUSINESS);
				}else if(proposalNode.get("businessLineId").asInt()==5){
					log.info("Entered In Travel");
					policyDataNode = customerService.prepareCustomerDataSet(proposalNode,customerConfigNode.get("travelpolicyConfig"));
					log.info("travelpolicyConfig  " +policyDataNode);
					JsonNode policyPersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("proposerDetails"),customerConfigNode.get("travelPersonalDetails"));
					Map<String, String> personalDataNodeMap = objectMapper.readValue(policyPersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : personalDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					JsonNode policyNumberDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("travelPolicyResponse"),customerConfigNode.get("travelPolicyNumber"));
					Map<String, String> policyNumberDataNodeMap = objectMapper.readValue(policyNumberDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : policyNumberDataNodeMap.entrySet()){
						policyDataNode.put(field.getKey(), field.getValue());		   
					}
					// Policy issue date not available at this moment so adding start date
					policyDataNode.put("policy_issue_date_c", proposalNode.findValue("policyStartDate"));
					policyDataNode.put("contract_type", CustomerConstants.CONTRACT_TYPE_TRAVEL);

					JsonNode customePersonalDataNode = customerService.prepareCustomerDataSet(proposalNode.findValue("personalInfo"),customerConfigNode.get("customerPersonalDetails"));
					Map<String, String> customePersonalDataNodeMap = objectMapper.readValue(customePersonalDataNode.toString(), Map.class);
					for (Map.Entry<String, String> field : customePersonalDataNodeMap.entrySet()){
						custDataNode.put(field.getKey(), field.getValue());		   
					}
					custDataNode.put("line_of_business_c", CustomerConstants.TEAVEL_LINE_OF_BUSINESS);

				}
				log.info("Policy Data Node "+policyDataNode);
				log.info("Customer Data Node "+custDataNode);
				//For Now assigning hard coded value as policy status 
				policyDataNode.put("status", "Converted");

				// commented becoz stopped closing leads 
				
				if(proposalNode.has("leadMessageId") && proposalNode.findValue("leadMessageId") != null){
					leadSearchQuery= "leads_cstm.messageid_c='"+proposalNode.get("leadMessageId").asText()+"'";
					log.info("Query for policy :"+leadSearchQuery);
					leadData = crmService.getLeadData(leadSearchQuery,customerConfigNode.get("leadDetails").get("select_fields").toString().replace("\"", ""));
					log.info("Lead Data :"+leadData);
				}
				if( leadData == null && (policyDataNode.has("mobile_c") || policyDataNode.has("email_c"))){
					if(policyDataNode.get("mobile_c").asText() != null ){
						leadSearchQuery = "(leads.phone_mobile = '"+policyDataNode.get("mobile_c").asText()+"'";
					}else{
						leadSearchQuery = "(leads.phone_mobile = \'ERROR\'";
					}
					if(policyDataNode.get("email_c").asText() != null ){
						leadSearchQuery += " OR leads_cstm.useremail_c = '"+policyDataNode.get("email_c").asText()+"')";
					}else{
						leadSearchQuery += " OR leads_cstm.useremail_c = \'ERROR\')";
					}
					leadSearchQuery += " AND leads.status != \'closed\'";
					log.info("Query for policy :"+leadSearchQuery);
					leadData = crmService.getLeadData(leadSearchQuery,customerConfigNode.get("leadDetails").get("select_fields").toString().replace("\"", ""));
					log.info("Lead Data :"+leadData);
				}
				leadData = customerService.prepareCustomerDataSet(leadData, customerConfigNode.get("policyAssignmentConfig"));
				log.info("Lead Data Node :"+leadData);
				policyDataNode.putAll(leadData);
				//policy end_date
				if(policyDataNode.has("end_date1_c") && policyDataNode.get("end_date1_c") != null){
					String end_date = customerService.convertDate(policyDataNode.get("end_date1_c").asText());
					if(end_date !=null){
						policyDataNode.put("end_date", end_date);
					}
				}

				// policy start_date
				if(policyDataNode.has("start_date1_c") && policyDataNode.get("start_date1_c") != null){
					String start_date = customerService.convertDate(policyDataNode.get("start_date1_c").asText());
					if(start_date !=null){
						policyDataNode.put("start_date", start_date);
					}
				}

				// policy source 
				if(!policyDataNode.has("source_c")){
					policyDataNode.put("source_c", "web");
				}
				// update lead status to close
				/*if(leadData.has("leadid_c") && leadData.get("leadid_c") != null){
					log.info("In prev. lead closing");
					ObjectNode updateLeadNode = objectMapper.createObjectNode();
					updateLeadNode.put("status", "closed");
					crmService.updateLead(updateLeadNode, leadData.get("leadid_c").asText());
					log.info("Lead Closed :"+leadData.get("leadid_c"));
				}*/
				if(date_entered != null){
					log.info("putty : "+date_entered);
					// Commented because date_entered used while ago for data cleaning
					//policyDataNode.put("date_entered", date_entered);
					//custDataNode.put("date_entered", date_entered);
				}
			}

			// Policy documents in iCRM  
			/*
			for(JsonNode policyDetailsNode : userProfileNode.get("policyDetails")){
				log.info("policyDetailsNode : "+policyDetailsNode);
				if(policyDetailsNode.has("proposalId") && policyDetailsNode.get("proposalId").asText().equalsIgnoreCase(reqNode.get("proposalId").asText())){
					if(policyDetailsNode.has("filePath") && policyDetailsNode.get("filePath") != null){
						policyDataNode.put("policy_url_c", policyDetailsNode.get("filePath"));
					}
					else{
						policyDataNode.put("policy_url_c", "Not Available");
					}
					break;
				}
			}
			 */

			// Added by Gauri
			if(custDataNode.has("email_c") && custDataNode.get("email_c") != null){
				log.info("Customer Have email"+custDataNode.get("email_c"));
				custDataNode.put("email1", custDataNode.findValue("email_c").asText());
			}

			if(!customerId.equalsIgnoreCase("ERROR") && customerId.length() > 0 ){
				policyId = crmService.createPolicy(policyDataNode);
				if(policyId.length()>0){
					status = crmService.createCustomerPolicyRelation(customerId, policyId);
					log.info("customer-policy relation status : "+status);
				}
				log.info("Policy Created, policyId :"+policyId);
			}
			else{
				/*
				 * Create new customer 
				 */
				customerId = crmService.createCustomer(custDataNode);
				log.info("Customer Created, customerId : "+customerId);
				policyId = crmService.createPolicy(policyDataNode);
				log.info("Policy Created, policyId :"+policyId);
				/*
				 * create customer-policy relation
				 */
				if(customerId.length() > 0 && policyId.length()>0)
				{
					status = crmService.createCustomerPolicyRelation(customerId, policyId);
					log.info("customer-policy relation status : "+status);  
				}
			}
		}
		catch(NullPointerException e)
		{
			policyId = "Failed to create policy in crm";
			log.error("NullPointerException occurred",e);
		}
		exchange.getIn().setBody(policyId);
	}
}
