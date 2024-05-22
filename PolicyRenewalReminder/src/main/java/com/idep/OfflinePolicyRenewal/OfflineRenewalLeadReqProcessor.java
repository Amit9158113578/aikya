package com.idep.OfflinePolicyRenewal;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.processor.PolicyRenwalDataProvider;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class OfflineRenewalLeadReqProcessor  implements Processor {

	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	static Logger log = Logger.getLogger(OfflineRenewalLeadReqProcessor.class.getName());
	static PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
	static SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	static JsonNode renewalLeadConfig = null;

	static{
		try {
			renewalLeadConfig = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("RenewalLeadConfig").content()).toString());	
		} catch (JsonProcessingException e) {
			log.info("Errors in Couchbase document");
			e.printStackTrace();
		} catch (IOException e) {
			log.info("Couchbase Document Not found");
			e.printStackTrace();
		} catch (Exception e) {
			log.info("Could not generate session Id :" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void process(Exchange exchange) throws Exception {
		try{
			String request = exchange.getIn().getBody().toString();
			JsonNode reqNode = objectMapper.readTree(request);
			log.info("reqNode for offline :"+reqNode);
			int quoteType = 2;
			String contractType = "Bike Insurance"; 
			ObjectNode leadReqBody = objectMapper.createObjectNode();
			ObjectNode leadReqHeader = objectMapper.createObjectNode();
			ObjectNode selectedLeadData = objectMapper.createObjectNode();
			JsonNode leadReqNode = objectMapper.createObjectNode();
			ObjectNode infoNode = objectMapper.createObjectNode();

			if (reqNode.has("contract_type") && reqNode.get("contract_type") != null){
				log.info("lead request LOB"+reqNode.get("contract_type").asText());
				if(reqNode.get("contract_type").asText().equalsIgnoreCase("LifeInsurance")) {
					quoteType = 1;
					contractType = "Life Insurance";
				}
				else if(reqNode.get("contract_type").asText().equalsIgnoreCase("CarInsurance")){
					quoteType = 3;
					contractType = "Car Insurance";
				}
				else if(reqNode.get("contract_type").asText().equalsIgnoreCase("HealthInsurance")) {
					quoteType = 4;
					contractType = "Health Insurance";
				}
				else if(reqNode.get("contract_type").asText().equalsIgnoreCase("TravelInsurance") ){
					quoteType = 5;
					contractType = "Travel Insurance";
				}
				infoNode.put("businessLineId", quoteType);
				infoNode.put("contractType", contractType);
				infoNode.put("policynumber_c", reqNode.get("policynumber_c").asText());
				infoNode.put("contract_type", contractType);
				infoNode.put("end_date1_c", reqNode.get("end_date1_c").asText());
				infoNode.put("carriername_c", reqNode.get("carriername_c").asText());
				infoNode.put("emailId", reqNode.get("email_c").asText());
				infoNode.put("mobileNumber", reqNode.get("mobile_c").asText());
				infoNode.put("Source", reqNode.get("source_c").asText());
				infoNode.put("petrolPumpName", reqNode.get("petrol_pump_name_c").asText());
				infoNode.put("vehicleRegNo", reqNode.get("vehicle_reg_no_c").asText());
				infoNode.put("name", reqNode.get("name").asText());

				if(reqNode.has("email_c") && reqNode.get("email_c").asText() != null && reqNode.get("email_c").asText().length() > 1){
					infoNode.put("reminderMailId", reqNode.get("email_c").asText());
				}
				infoNode.put("smsTo", reqNode.get("mobile_c").asText());
			}

			/*
			 *  Search lead 
			 */
			String query = dataProvider.prepareLeadFindQuery(infoNode, renewalLeadConfig.get("renewalQueryConfig"));
			selectedLeadData = crmService.getLeadData(query, renewalLeadConfig.get("renewalQueryConfig").get("selectedFields").asText());
			log.info("search lead response in offline :"+selectedLeadData);

			/*
			 * if lead present then don't create or update, because it will override values  
			 */

			if(selectedLeadData != null){
				String description = null;
				ObjectNode updateLeadNode = objectMapper.createObjectNode();
				if(selectedLeadData.has("description_c") && selectedLeadData.get("description_c") != null ){
					if(reqNode.has("description_c") && reqNode.get("description_c") != null ){
						if(!selectedLeadData.get("description_c").asText().contains(reqNode.get("description_c").asText())){
							//description = dataProvider.prepareLeadDecription(selectedLeadData.get("description_c").asText()+" >>> ",reqNode,renewalLeadConfig.get("descriptionField"));
							description = selectedLeadData.get("description_c").asText()+" >>> "+reqNode.get("description_c").asText();
							log.info("Offline lead description field :"+description);
						}
					}
				}

				if(description != null)
					updateLeadNode.put("description_c", description);

				if (updateLeadNode.size() > 0){
					String leadId = crmService.updateLead(updateLeadNode, selectedLeadData.get("id").asText());
					log.info("lead stage changed to quote :"+leadId);
				}
				infoNode.put("messageId", selectedLeadData.get("messageid_c"));
			}
			else
			{

				String description = "";
				((ObjectNode) leadReqBody.with("quoteParam")).put("quoteType",quoteType);
				((ObjectNode) leadReqBody.with("contactInfo")).put("termsCondition",true);
				((ObjectNode) leadReqBody.get("contactInfo")).put("createLeadStatus",false);
				((ObjectNode) leadReqBody.get("quoteParam")).put("description_c",reqNode.get("description_c"));
				((ObjectNode) leadReqBody.get("quoteParam")).put("source",PolicyRenewalConstatnt.RENEWAL_OFFLINE_SOURCE);
				((ObjectNode) leadReqBody.get("contactInfo")).put("firstName",reqNode.get("name"));
				((ObjectNode) leadReqBody.get("contactInfo")).put("lastName",reqNode.get("customerlastname_c"));
				((ObjectNode) leadReqBody.get("contactInfo")).put("emailId",reqNode.get("email_c"));
				((ObjectNode) leadReqBody.get("contactInfo")).put("mobileNumber",reqNode.get("mobile_c"));

				/*
				 * lead_source of lead should be policy source
				 */
				leadReqBody.put("requestSource",PolicyRenewalConstatnt.REQ_OFFLINE_SOURCE);


				/*
				 * policy end date in lead 
				 */
				if(reqNode.has("policyEndDate") && reqNode.get("policyEndDate")!=null){
					leadReqBody.put("policyEndDate",dataProvider.convertDate(reqNode.get("policyEndDate").asText()));
				}
				/*
				 * prepare description
				 */

				if(reqNode.has("description_c") && reqNode.get("description_c") != null ){
					//description = dataProvider.prepareLeadDecription(description,reqNode,renewalLeadConfig.get("descriptionField"));
					description = reqNode.get("description_c").asText();
					log.info("Offline lead description field :"+description);
					((ObjectNode) leadReqBody.get("quoteParam")).put("description_c",description);
				}

				leadReqHeader.put("deviceId",renewalLeadConfig.get("header").get("deviceId").asText());
				leadReqHeader.put("transactionName",renewalLeadConfig.get("header").get("transactionName").asText());
				if(reqNode.get("source_c").asText().equalsIgnoreCase("petrolpump")){
					leadReqBody.put("campaign_id",renewalLeadConfig.get("otherDetails").get("petrolPumpCampaign").asText());
				}else{
					leadReqBody.put("campaign_id",renewalLeadConfig.get("otherDetails").get("campaign_id").asText());
				}
				((ObjectNode) leadReqNode).put("header", leadReqHeader);
				((ObjectNode) leadReqNode).put("body", leadReqBody);
			}
			((ObjectNode) leadReqNode).put("infoNode", infoNode);
			log.info("Prepared renewal lead request in offline :"+leadReqNode);		
			log.info(" isOfflineRenewalFlag request value : "+leadReqBody.get("requestSource").asText());
			exchange.getIn().setHeader("isOfflineRenewalFlag", leadReqBody.get("requestSource").textValue());
			//exchange.getIn().setBody(leadReqNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(leadReqNode));
		}catch(Exception e){
			log.error("unable to process request : ",e);
		}
	}
}