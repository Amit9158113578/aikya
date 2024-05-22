package com.idep.imatUpdate.req.processor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
import com.idep.imatLead.req.processor.CrmCreateLead;
import com.idep.imatLead.req.processor.PrepareiMATLead;

public class iMATUpdateRenewalLeadProcessor implements Processor {

	static Logger log = Logger.getLogger(iMATUpdateRenewalLeadProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static  CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	static JsonNode renewalConfigNode = null;
	static JsonNode serviceConfigNode = null;
	static JsonNode WebHostConfigNode = null;
	static JsonNode offlineRenewalConfigNode = null;
	JsonNode proposalDoc= null;
	ObjectNode reqNode = objectMapper.createObjectNode();
	PrepareiMATLead createcontact = new PrepareiMATLead();
	static
	{
		if (serverConfig != null)
		{
			try
			{
				WebHostConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("WEBHOSTConfig").content()).toString());
				serviceConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ServiceURLConfig").content()).toString());
				renewalConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATRenewalLeadConfig").content()).toString());
				offlineRenewalConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATOfflineRenewal").content()).toString());
			}
			catch (Exception e)
			{
				log.error("iMATRenewalLeadConfig Document Not Found", e);
				e.printStackTrace();
			}
		}
	}

	public void process(Exchange exchange)   throws Exception
	{
		try{
			reqNode = (ObjectNode) objectMapper.readTree((String)exchange.getIn().getBody(String.class));

			log.info("MESSAGE in iMATUpdateRenewalLeadProcessor : " + reqNode);

			if(reqNode.has("policyDetails") && reqNode.findValue("proposalId").asText()!=null)
			{
				proposalDoc = objectMapper.readTree(((JsonObject)policyTransaction.getDocBYId(reqNode.get("policyDetails").get("renewPolicyDetails").findValue("proposalId").asText()).content()).toString());
				log.info("ProposalDocument"+proposalDoc);

				reqNode.put("last_name",proposalDoc.get("lastName").asText());			
				reqNode.put("first_name",proposalDoc.get("firstName").asText());
				reqNode.put("mobile",proposalDoc.get("mobile").asText());
				reqNode.put("leadmsgid",proposalDoc.get("messageId").asText());
				reqNode.put("totalpremium",proposalDoc.get("totalPremium").asText());
				reqNode.put("email",proposalDoc.get("emailId").asText());	        
				reqNode.put("zipcode",proposalDoc.get("pincode").asText());
				reqNode.put("carrierid",proposalDoc.get("carrierId").asText());
				reqNode.put("lastquoteid",proposalDoc.get("QUOTE_ID").asText());

				if ( proposalDoc.get("businessLineId").asInt() == 1)
				{
					reqNode.put("lob","life");
					reqNode.put("lifeproposalid",proposalDoc.get("proposalId").asText());
				}
				if ( proposalDoc.get("businessLineId").asInt() == 2)
				{
					reqNode.put("lob","bike");
					reqNode.put("bikeproposalid",proposalDoc.get("proposalId").asText());
				}
				if ( proposalDoc.get("businessLineId").asInt() == 3)
				{
					reqNode.put("lob","car");
					reqNode.put("carproposalid",proposalDoc.get("proposalId").asText());
				}
				if ( proposalDoc.get("businessLineId").asInt() == 4)
				{
					reqNode.put("lob","health");
					reqNode.put("healthproposalid",proposalDoc.get("proposalId").asText());
				}
				log.info("Resquest created"+reqNode);
				String res = prepareRequest(reqNode);
				log.info("Method Output of prepare Request...Request prepared"+res);


				ObjectNode requestNode = (ObjectNode) objectMapper.readTree(res);
				log.info("requestNode"+requestNode);
				ObjectNode contactInfo = objectMapper.createObjectNode();
				contactInfo.put("mobileNumber", reqNode.get("mobile"));
				contactInfo.put("emailId", reqNode.get("email"));
				log.info("contactInfo"+contactInfo);
				requestNode.put("contactInfo",contactInfo);
				requestNode.put("requestSource","renewal");
				if(requestNode.get("lob").asText().equalsIgnoreCase("bike") || requestNode.get("lob").asText().equalsIgnoreCase("car")){
					String additionalinfo = proposalDoc.get("proposalRequest").get("insuranceDetails").get("insurerName").asText()+"_"+proposalDoc.get("proposalRequest").get("insuranceDetails").get("policyNumber").asText()+"_"+proposalDoc.get("proposalRequest").get("vehicleDetails").get("registrationDate").asText()+"_"+proposalDoc.get("proposalRequest").get("vehicleDetails").get("registrationNumber").asText()+"_CHA:"+proposalDoc.get("proposalRequest").get("vehicleDetails").get("chassisNumber").asText()+"_ENG:"+proposalDoc.get("proposalRequest").get("vehicleDetails").get("engineNumber").asText()+"_"+requestNode.get("policyexpirydate").asText()+"_"+requestNode.get("totalpremium").asText();
					requestNode.put("description_c",additionalinfo );
					log.info("AdditionalInfo"+additionalinfo);
				}

				log.info("Final Request to created lead"+requestNode);

				String leadResponse = createRenewalLead(requestNode);	
				log.info("create leadv master service call response"+leadResponse);

				ObjectNode leadResponseNode = (ObjectNode) objectMapper.readTree(leadResponse);
				ObjectNode respNode = (ObjectNode) objectMapper.readTree(res);
				respNode.put("leadmsgid",leadResponseNode.findValue("messageId").asText());
				log.info("request for creating lead in mautic"+respNode);
				String responseNode = createcontact.createContact(respNode.toString());
				log.info("create lead mautic service call response"+responseNode);

				String session_id = null;
				CrmCreateLead crmcreatelead = new CrmCreateLead();
				requestNode.put("leadmsgid",leadResponseNode.findValue("messageId").asText());
				log.info("Craete Lead Request"+requestNode);
				String crmLeadResNode = crmcreatelead.createCRMLead(session_id,"Leads",requestNode);
				log.info("create lead CRM set_entry call response"+crmLeadResNode);


				exchange.getIn().setBody(responseNode);
			}
			else{
				log.info("reqNode in offline renewal"+reqNode);
				reqNode.put("requestSource","renewal");
				log.info("Final Request to created lead"+reqNode);


				((ObjectNode) reqNode.get("body")).put("infoNode",reqNode.get("infoNode"));
				log.info("MEasshae REQQUEST"+reqNode);
				reqNode.remove("infoNode");
				String leadResponse = createRenewalLead(reqNode.get("body"));	
				log.info("create lead master service call response"+leadResponse);

				ObjectNode leadResponseNode = (ObjectNode) objectMapper.readTree(leadResponse);
				reqNode.put("leadmsgid",leadResponseNode.findValue("messageId").asText());
				log.info("request for creating lead in mautic OFFLINE"+reqNode);

				String dataFormNode = PrepareMauticRequest(reqNode);
				ObjectNode dataFormed = (ObjectNode) objectMapper.readTree(dataFormNode);
				log.info("dataFormed dataFormed"+dataFormed);
				String responseNode = createcontact.createContact(reqNode.toString());
				log.info("create lead mautic service call response"+responseNode);

				String session_id = null;
				CrmCreateLead crmcreatelead = new CrmCreateLead();
				log.info("Craete Lead Request"+reqNode);
				String crmLeadResNode = crmcreatelead.createCRMLead(session_id,"Leads",reqNode);
				log.info("create lead CRM set_entry call response"+crmLeadResNode);

				exchange.getIn().setBody(responseNode);

			}

		}
		catch(Exception e){
			log.error("Error",e);	
		}
	}
	public String PrepareMauticRequest(ObjectNode leadDataJson) throws JsonParseException, JsonMappingException, IOException {
		String prepareRequestOutuput=null;
		try{

			if(leadDataJson.has("header")){
				String keys = offlineRenewalConfigNode.get("renewal").get("offlineRenewal").asText();
				log.info("STringgg :"+keys);
				String[] strArr= keys.split(",");
				ObjectNode finalnode = objectMapper.createObjectNode();
				for(String str:strArr)
				{
					if(leadDataJson.findValue(str)!=null){
						finalnode.put(str,leadDataJson.findValue(str));	
					}
				}
				log.info("FINAL NODE"+finalnode);
				prepareRequestOutuput = prepareRequest(finalnode);
				log.info("Output"+prepareRequestOutuput);
			}
		}
		catch(Exception e){log.error("error",e);}
		return prepareRequestOutuput;
	}
	public String prepareRequest(JsonNode reqNode) throws JsonParseException, JsonMappingException, IOException
	{
		log.info("in prepareRequest Processor" + reqNode);

		ObjectNode dataNode = objectMapper.createObjectNode();

		JsonNode quoteTypeNode = renewalConfigNode.get("renewalLeadConfig");
		if (quoteTypeNode.has("defaultParam"))
		{
			Map<String, String> defaultDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.get("defaultParam").toString(), Map.class);
			for (Map.Entry<String, String> field1 : defaultDataNodeMap.entrySet()) {
				dataNode.put((String)field1.getKey(), (String)field1.getValue());
			}
		}
		log.info("DataNode before default param..." + dataNode);

		Map<String, String> configDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.toString(), Map.class);
		log.info("configDataNodeMap in iMAT" + configDataNodeMap);
		configDataNodeMap.remove("defaultParam");

		log.info("dataNode" + dataNode);
		log.info("reqNode" + reqNode);
		log.info("configDataNodeMap1" + configDataNodeMap);
		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()) {
			try
			{
				if (reqNode.findValue((String)field.getKey()).isTextual()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).textValue());
				} else if (reqNode.findValue((String)field.getKey()).isInt()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).intValue());
				} else if (reqNode.findValue((String)field.getKey()).isLong()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).longValue());
				} else if (reqNode.findValue((String)field.getKey()).isDouble()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).doubleValue());
				} else if (reqNode.findValue((String)field.getKey()).isBoolean()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).booleanValue());
				} else if (reqNode.findValue((String)field.getKey()).isFloat()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).floatValue());
				}
			}
			catch (NullPointerException localNullPointerException) {}
		}
		log.info("DataNode..." + dataNode);
		return dataNode.toString();
	}


	public String createRenewalLead(JsonNode reqNode) {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		String serviceURL = null;
		String createLeadResponse = null;
		JsonNode createLeadResponseNode= null; 
		serviceURL = serviceConfigNode.get("createLead").asText().replaceAll("netty4-http:", "") + "/integrate/invoke";
		try
		{
			HttpPost httpPost = new HttpPost(serviceURL);

			StringEntity entity = new StringEntity(((ObjectNode)reqNode).toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			httpPost.setHeader("Origin", WebHostConfigNode.get("origin").asText());
			httpPost.setHeader("deviceId", "ABCD12345");
			httpPost.setHeader("transactionName", "createLead");
			HttpResponse SubmitDataResponse = httpClient.execute(httpPost);
			log.info("Submit  Proposal Response Status Code : " + SubmitDataResponse.getStatusLine().getStatusCode());
			createLeadResponse = EntityUtils.toString(SubmitDataResponse.getEntity());
			log.info("Response in JSON without slashremoval: " + createLeadResponse);
			log.info("Response in JSON : " + createLeadResponse.replace("\\\"", "\""));
			String SlashRemoval = null;
			if (createLeadResponse.contains("\\\""))
			{
				SlashRemoval = createLeadResponse.replace("\\\"", "\"");
				createLeadResponseNode = objectMapper.readTree(SlashRemoval.substring(1, SlashRemoval.length() - 1));
			}
			else
			{
				SlashRemoval = createLeadResponse;
				createLeadResponseNode = objectMapper.readTree(SlashRemoval);
			}
			httpClient.getConnectionManager().shutdown();
		}
		catch (ClientProtocolException e)
		{
			log.info("Exception While Getting submit proposal Response  ", e);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			log.info("Exception While  Getting submit proposal Response  ", e);
			e.printStackTrace();
		}
		return createLeadResponse;
	}
}







