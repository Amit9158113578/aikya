package com.idep.PolicyRenewal.processor;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class RenewalLeadReqProcessor implements Processor {
	static PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider(); 
	static ObjectMapper objectMapper = new ObjectMapper();
	static SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	static Logger log = Logger.getLogger(RenewalLeadReqProcessor.class.getName());
	static String renewalLeadConfig = null;
	JsonNode proposalNode = null;

	static
	{
		renewalLeadConfig = serverConfig.getDocBYId(PolicyRenewalConstatnt.RENEWAL_LEAD_CONFIG).content().toString();
	}

	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		JsonNode renewalLeadConfigNode = objectMapper.readTree(renewalLeadConfig);
		ObjectNode leadNode = objectMapper.createObjectNode();
		ObjectNode contactInfo = objectMapper.createObjectNode();
		ObjectNode selectedLeadData = null;
		log.info("Got Renewal Lead Request : "+reqNode);

		String query = dataProvider.prepareLeadFindQuery(reqNode, renewalLeadConfigNode.get("renewalQueryConfig"));
		selectedLeadData = crmService.getLeadData(query, renewalLeadConfigNode.get("renewalQueryConfig").get("selectedFields").asText());
		log.info("search lead response :"+selectedLeadData);

		if(selectedLeadData != null){
			String stage = null;
			String description = null;
			ObjectNode updateLeadNode = objectMapper.createObjectNode();
			if(selectedLeadData.has("stage_c") && selectedLeadData.get("stage_c") != null ){
				if(selectedLeadData.get("stage_c").asText().equalsIgnoreCase(PolicyRenewalConstatnt.PRE_QUOTE)){
					if((reqNode.get("intervalDay").asInt()<=30) || (reqNode.get("intervalDay").asInt()<=45 && reqNode.get("businessLineId").asInt()==2)){
						stage = "quote";
					}
				}
			}
			if(selectedLeadData.has("description_c") && selectedLeadData.get("description_c") != null ){
				if(reqNode.has("PreviousPolicyNumber") && reqNode.get("PreviousPolicyNumber") != null ){
					if(!selectedLeadData.get("description_c").asText().contains(reqNode.get("PreviousPolicyNumber").asText())){
						description = dataProvider.prepareLeadDecription(selectedLeadData.get("description_c").asText()+" >>> ",reqNode,renewalLeadConfigNode.get("descriptionField"));
					}
				}
			}

			if(stage!=null)
				updateLeadNode.put("stage_c", stage);
			if(description != null)
				updateLeadNode.put("description_c", description);

			if (updateLeadNode.size() > 0){
				String leadId = crmService.updateLead(updateLeadNode, selectedLeadData.get("id").asText());
				log.info("lead stage changed to quote :"+leadId);
			}

			leadNode.put("messageId", selectedLeadData.get("messageid_c"));
			leadNode.put("renewalProposalId", reqNode.get("proposalId"));
		}else{
			ObjectNode leadReqBody = objectMapper.createObjectNode();
			ObjectNode leadReqHeader = objectMapper.createObjectNode();
			proposalNode = objectMapper.readTree(policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
			log.info("Fetched PROP DOC"+proposalNode);
			JsonNode renewalQuoteDoc = dataProvider.getQuoteDoc(proposalNode.findValue("QUOTE_ID").asText());
			if (proposalNode.has("businessLineId") && proposalNode.get("businessLineId") != null){
				if(proposalNode.get("businessLineId").asInt() == 1){
					// Life renewal not present still
				}else if(proposalNode.get("businessLineId").asInt() == 2) {
					leadReqBody.put(PolicyRenewalConstatnt.QUOTE_PARAM,renewalQuoteDoc.findValue(PolicyRenewalConstatnt.QUOTE_PARAM));
					leadReqBody.put(PolicyRenewalConstatnt.VEHICLE_INFO,renewalQuoteDoc.findValue(PolicyRenewalConstatnt.VEHICLE_INFO));
					leadReqBody.put("contactInfo",dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerDetails"), objectMapper.readValue(renewalLeadConfigNode.get("contactInfo").get("Bike").toString(),Map.class )));
				}else if(proposalNode.get("businessLineId").asInt() == 3) {
					leadReqBody.put(PolicyRenewalConstatnt.QUOTE_PARAM,renewalQuoteDoc.findValue(PolicyRenewalConstatnt.QUOTE_PARAM));
					leadReqBody.put(PolicyRenewalConstatnt.VEHICLE_INFO,renewalQuoteDoc.findValue(PolicyRenewalConstatnt.VEHICLE_INFO));
					leadReqBody.put("contactInfo",dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerDetails"), objectMapper.readValue(renewalLeadConfigNode.get("contactInfo").get("Car").toString(),Map.class )));
				}else if(proposalNode.get("businessLineId").asInt() == 4) {
					if(renewalQuoteDoc.has("quoteParamRequestList")){
						for ( JsonNode request1 : renewalQuoteDoc.findValue("quoteParamRequestList")){
							leadReqBody.put(PolicyRenewalConstatnt.QUOTE_PARAM,request1);
							break;
						}
					}else if(renewalQuoteDoc.has("quoteRequest") && renewalQuoteDoc.get("quoteRequest") != null ){
						leadReqBody.put(PolicyRenewalConstatnt.QUOTE_PARAM,renewalQuoteDoc.get("quoteRequest"));
						((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("quoteType",4);
						((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("requestSource",PolicyRenewalConstatnt.REQ_ONLINE_SOURCE);
					}
				}
				ObjectNode filterData = dataProvider.filterMapData(contactInfo, proposalNode.findValue("proposerInfo"), objectMapper.readValue(renewalLeadConfigNode.get("contactInfo").get("Health").toString(),Map.class )); 
				if( !filterData.has("mobileNumber")){
					log.info("mobile in if"+proposalNode.findValue("mobile"));
					filterData.put("mobileNumber", proposalNode.findValue("mobile").asText());
				}
				leadReqBody.put("contactInfo",filterData);
			}
			((ObjectNode) leadReqBody.get("contactInfo")).put("termsCondition",true);
			((ObjectNode) leadReqBody.get("contactInfo")).put("createLeadStatus",false);
			log.info("Req before source :"+leadReqBody);
			/*
			 * lead_source of lead should be policy source
			 */
			if(reqNode.has("source") && (reqNode.get("source").textValue() != null || reqNode.get("source")!= null)) {
				if(renewalLeadConfigNode.get("leadSourceConfig").has(reqNode.get("source").asText())){
					leadReqBody.put("requestSource",renewalLeadConfigNode.get("leadSourceConfig").get(reqNode.get("source").asText()));					
				}else{
					log.info("In else1");
					leadReqBody.put("requestSource",PolicyRenewalConstatnt.REQ_ONLINE_SOURCE);
				}
			}else{
				log.info("In else");
				leadReqBody.put("requestSource",PolicyRenewalConstatnt.REQ_ONLINE_SOURCE);
			}
			/*
			 * policy end date in lead 
			 */
			if(reqNode.has("policyEndDateStr") && reqNode.get("policyEndDateStr")!=null){
				log.info("in if end date");
				leadReqBody.put("policyEndDate",dataProvider.convertDate(reqNode.get("policyEndDateStr").asText()));
			}
			/*
			 * Prepare description field
			 */
			String description = dataProvider.prepareLeadDecription("",reqNode,renewalLeadConfigNode.get("descriptionField"));
			leadReqBody.put("description_c", description);
			// For lead stage
			if(!(reqNode.has("businessLineId") && reqNode.get("businessLineId").asInt()==3)){
				if(!(reqNode.get("intervalDay").asInt()<=45 && reqNode.get("businessLineId").asInt()==2)){
					((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("transaction",renewalLeadConfigNode.get("quoteParamDetails").get("leadStage").asText());
				}else{
					((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("transaction","quote");
				}
			}else{
				((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("transaction","quote");
			}
			//Online offline changes
			((ObjectNode) leadReqBody.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("source",PolicyRenewalConstatnt.RENEWAL_ONLINE_SOURCE);
			
			//renewalProposalId used for performing quote and sending email
			leadReqBody.put("renewalProposalId", reqNode.get("proposalId").asText());
			Map<String,String> renewalLeadConfigNodeMap = objectMapper.readValue(renewalLeadConfigNode.get("otherDetails").toString(), Map.class);
			leadReqBody = dataProvider.putCustomeFields(leadReqBody, renewalLeadConfigNodeMap);
			renewalLeadConfigNodeMap = objectMapper.readValue(renewalLeadConfigNode.get("header").toString(), Map.class);
			leadReqHeader = dataProvider.putCustomeFields(leadReqHeader, renewalLeadConfigNodeMap);
			leadNode.put("header", leadReqHeader);
			leadNode.put("body", leadReqBody);
		}
		log.info("Prepared Lead Request :"+leadNode);
		exchange.getIn().setBody(leadNode);
	}
}
