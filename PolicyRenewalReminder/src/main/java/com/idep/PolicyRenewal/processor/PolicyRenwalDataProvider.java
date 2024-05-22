package com.idep.PolicyRenewal.processor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyRenwalDataProvider {

	static Logger log = Logger.getLogger(PolicyRenwalDataProvider.class);
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null;
	static JsonNode policyRenewalNode = null;
	static CBService quoteDataInstance ;
	JsonNode headerNode = null;
	JsonNode bodyNode = null;
	JsonNode quoteDataNode = null;
	static {
		if(serverConfig == null || quoteDataInstance == null) {
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewalConstatnt.QUOTE_BUCKET);
		}
		try {
			policyRenewalNode = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
		} catch (JsonProcessingException e) {
			log.error("PolicyRenewalConfiguration Document Not Found",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("PolicyRenewalConfiguration Document Not Found",e);
			e.printStackTrace();
		}
	}

	public JsonNode prepareURL(JsonNode reqNode){
		String emailId = "MAILID"+getTrackId();
		String smsId = "SMSID"+getTrackId();
		String domainURL = policyRenewalNode.get("URLConfig").get("domainName").asText();
		String emailURL = null;
		String smsURL = null;
		ObjectNode urlNode = objectMapper.createObjectNode();
		//Email Link prepare
		if( reqNode.has("isResponseNull") && reqNode.get("isResponseNull").asBoolean() == false ){
			try {
				emailURL = policyRenewalNode.get("URLConfig").get("emailURL").asText();
				emailURL = emailURL.replace("$QUOTE_ID", reqNode.findValue("QUOTE_ID").asText());
				emailURL = emailURL.replace("$proposalId", reqNode.findValue("replaceProposalID").asText());
				emailURL = emailURL.replace("$mailId",emailId);
				urlNode.put("emailURL", domainURL+emailURL);
			} catch (Exception e) {
				log.info("Error at prepare renewal URL");
				emailURL = policyRenewalNode.get("URLConfig").get("otherURL").asText();
				emailURL = emailURL.replace("$smsId",emailId);
				emailURL = emailURL.replace("$lob",reqNode.get("businessLineId").asText());
				urlNode.put("emailURL", domainURL+emailURL);
			}
		}else{
			emailURL = policyRenewalNode.get("URLConfig").get("otherURL").asText();
			emailURL = emailURL.replace("$smsId",emailId);
			emailURL = emailURL.replace("$lob",reqNode.get("businessLineId").asText());
			urlNode.put("emailURL", domainURL+emailURL);
		}

		// SMS Link Prepare
		smsURL = policyRenewalNode.get("URLConfig").get("smsURL").asText();
		smsURL = smsURL.replace("$smsId",smsId);
		smsURL = smsURL.replace("$lob",reqNode.get("businessLineId").asText());
		urlNode.put("smsURL", domainURL+smsURL);
		urlNode.put("emailId", emailId);
		urlNode.put("smsId", smsId);
		log.info("urlNode :"+urlNode);
		return urlNode;
	}

	public long getTrackId(){
		synchronized (this)
		{
			long seq = 0L;
			try{
				seq = serverConfig.updateDBSequence("SEQSMSEMAIL");
			}catch (Exception e)	{
				seq = -1L;
				log.info("failed to update sms/mail sequence hence retrying...", e);
				seq = serverConfig.updateDBSequence("SEQSMSEMAIL");
			}
			if(seq == -1L){
				log.info("failed to update email sequence hence retrying...");
			}
			log.info("SMS/EMAIL SEQ Id :"+seq);
			return seq;
		}
	}

	public JsonNode prepareQuoteRequestHeader(String LOB){
		headerNode = policyRenewalNode.get("headerNode");
		((ObjectNode) headerNode).put("transactionName",policyRenewalNode.get("LOBConfig").get(LOB));
		log.info("Policy Renewal Header Node : "+headerNode);
		return headerNode;
	}

	public JsonNode prepareQuoteRequestBody(String quoteId){
		quoteDataNode = getQuoteDoc(quoteId);
		bodyNode = objectMapper.createObjectNode();
		((ObjectNode) bodyNode).put(PolicyRenewalConstatnt.QUOTE_PARAM,quoteDataNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM));
		((ObjectNode) bodyNode).put(PolicyRenewalConstatnt.VEHICLE_INFO,quoteDataNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO));
		((ObjectNode) bodyNode).put("requestType",quoteDataNode.findValue("requestType"));
		//Change Age of Person
		int personAge = quoteDataNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM).get("personAge").asInt() + 1;
		log.info("Person age :"+personAge);
		((ObjectNode) bodyNode.get(PolicyRenewalConstatnt.QUOTE_PARAM)).put("personAge",personAge);
		log.info("Prepare bodyNode : "+bodyNode);
		return bodyNode;
	}

	ObjectNode getValue(JsonNode dataNode , String fields){
		//log.info("data field :"+dataNode);
		String field = fields.split("\\.")[0];
		//log.info("Field  :"+field);
		if(fields.split("\\.").length == 1){
			if(dataNode.has(field))
			{
				ObjectNode valueNode = objectMapper.createObjectNode();
				if(dataNode.get(fields).isTextual()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).textValue());
				} else if (dataNode.get(fields).isInt()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).intValue());
				} else if (dataNode.get(fields).isLong()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).longValue());
				} else if (dataNode.get(fields).isDouble()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).doubleValue());
				} else if (dataNode.get(fields).isBoolean()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).booleanValue());
				} else if (dataNode.get(fields).isFloat()) {
					return  ((ObjectNode) valueNode).put("value",dataNode.get(fields).floatValue());
				} 
			}
			else
			{
				log.info("In ELSE :");
				return null;

			}
		}
		else if(dataNode.has(field))
		{
			log.info("In Recur :"+field);
			return getValue(dataNode.get(field),fields.substring(fields.indexOf(".")+1,fields.length()));
		}
		return null;
	}

	public ObjectNode putCustomeFields(ObjectNode reqNode , Map<String,String>  renewalLeadConfigNodeMap ){
		for(Map.Entry<String,String> fields : renewalLeadConfigNodeMap.entrySet()){
			try{
				reqNode.put(fields.getKey().toString(),fields.getValue());
			}catch (Exception e){
				log.info("exception for "+fields.getKey());
				log.info("exception is "+e);
			}
		}
		return reqNode;
	}

	public ObjectNode prepareResponseNode(JsonNode proposalNode,JsonNode responseConfigNode){
		log.info("prepareResponseNode proposalNode: "+proposalNode);
		ObjectNode responseNode = objectMapper.createObjectNode();
		for(JsonNode replaceNode : responseConfigNode){
			if(proposalNode.has(replaceNode.get("key").asText())){
				responseNode.put(replaceNode.get("key").asText(),proposalNode.findValue(replaceNode.get("value").asText()));
			}else{
				try {
					responseNode.put(replaceNode.get("key").asText(),proposalNode.findValue(replaceNode.get("key").asText()));
				}catch(NullPointerException e){
					log.info("Not found value of :"+replaceNode.get("key").asText());
				}
			}
		}
		log.info("prepareResponseNode Method :"+responseNode);
		return responseNode;
	}

	public JsonNode getQuoteDoc(String docName){
		try {
			log.info("Fetching quote DOC :"+docName);
			JsonDocument QuoteDocId = quoteDataInstance.getDocBYId(docName);
			if(QuoteDocId !=null){
				return objectMapper.readTree(QuoteDocId.content().toString());
			}else{
				new Exception();
				return null;
			}
		} catch (JsonProcessingException e) {
			log.info("Error while getting quote doc",e);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			log.info("Error while getting quote doc",e);
			e.printStackTrace();
			return null;
		}

	}


	public ObjectNode filterMapData(ObjectNode responseNode,JsonNode reqNode,Map<String, String> configDataNodeMap){
		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()){
			try{
				if(reqNode.findValue(field.getKey()).isTextual()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).textValue());
				} else if (reqNode.findValue(field.getKey()).isInt()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).intValue());
				} else if (reqNode.findValue(field.getKey()).isLong()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).longValue());
				} else if (reqNode.findValue(field.getKey()).isDouble()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).doubleValue());
				} else if (reqNode.findValue(field.getKey()).isBoolean()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).booleanValue());
				} else if (reqNode.findValue(field.getKey()).isFloat()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).floatValue());
				} 
			}
			catch(NullPointerException e){
				log.info("Null Pointer Exception at filter :"+field.getValue());
			}
		}
		log.info(" After Filter :"+responseNode.toString());
		return responseNode;
	}

	public JsonNode getPolicyDetails(JsonNode policyDetailNode, String proposalId, String key, String value){
		log.info("policyDetailNode :"+policyDetailNode);
		JsonNode policyDetail = objectMapper.createObjectNode();
		if(policyDetailNode.has("policyDetails")){
			log.info("policyNo 1");
			for(JsonNode policyNode :policyDetailNode.get("policyDetails")){
				if(policyNode.has("proposalId") && policyNode.get("proposalId").asText().equalsIgnoreCase(proposalId)){
					log.info("policyNo 2");
					if(policyNode.has("policyNo") && policyNode.get("policyNo") !=null ){
						log.info("policyNo 3");
						((ObjectNode) policyDetail).put(key,policyNode.findValue(value));
					}
				}
			}
		}
		System.out.println("getPolicyDetails node :"+policyDetail);
		return policyDetail;	
	}

	public String updateDate(String oldDate) {
		log.info("Date before Addition date: "+oldDate);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		try{
			c.setTime(sdf.parse(oldDate));
		}catch(ParseException e){
			log.info("Exception at updateDate:",e);
		}
		//Incrementing the date by 1 day
		c.add(Calendar.DAY_OF_MONTH, 1);  
		return sdf.format(c.getTime());
	}

	public String updateYear(String oldDate) {
		log.info("Date before Addition Year: "+oldDate);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		try{
			c.setTime(sdf.parse(oldDate));
		}catch(ParseException e){
			log.info("Exception at updateyear:",e);
		}
		//Incrementing the date by 1 day
		c.add(Calendar.YEAR, 1);  
		return sdf.format(c.getTime());
	}

	int getNewNCB(String oldNCB){
		if(policyRenewalNode.get("NCBSlabs").has(oldNCB)){
			return policyRenewalNode.get("NCBSlabs").get(oldNCB).asInt();
		}
		return 0;		
	}

	int getModifiedIDV(String oldIdv){
		log.info("Modified Value"+ (int)(1 *(Integer.valueOf(oldIdv))));
		return  (int)(1 *(Integer.valueOf(oldIdv)));		
	}

	public String convertDate(String dateString) {
		DateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String changed_date = null;
		try {
			Date date = inputDateFormat.parse(dateString);
			changed_date = outputDateFormat.format(date);

		} catch (ParseException e) {
			log.info("Exception in changing end date format");
			e.printStackTrace();
		}
		return changed_date;
	}

	public String prepareLeadDecription(String description,JsonNode reqNode, JsonNode decriptionConfig){
		try{
			for (JsonNode fieldName : decriptionConfig) {
				if(reqNode.has(fieldName.get("value").asText())){
					description += fieldName.get("label").asText()+reqNode.get(fieldName.get("value").asText()).asText();
					description += ", ";
				}
			}
			// removed comma and space
			description = description.substring(0,description.length()-2);
		}catch(NullPointerException e){
			log.error("Error at description in lead",e);
		}
		return description;
	}

	public String prepareLeadFindQuery(JsonNode reqNode,JsonNode queryConfig){
		log.info("reqNode in query :"+reqNode);
		log.info("queryConfig in query:"+queryConfig);
		String leadSearchQuery = queryConfig.get("renewalLeadFindQuery").asText();
		for (JsonNode fieldName : queryConfig.get("conditionParameter")) {
			if( reqNode.has(fieldName.get("sourceFieldName").asText())){
				leadSearchQuery = leadSearchQuery.replace(fieldName.get("destFieldName").asText(), reqNode.get(fieldName.get("sourceFieldName").asText()).asText());
			}else{
				leadSearchQuery = leadSearchQuery.replace(fieldName.get("destFieldName").asText(), "404");
			}
		}
		log.info("Renewal Lead search query :"+leadSearchQuery);
		return leadSearchQuery;
	}
}