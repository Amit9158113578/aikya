package com.idep.policyrenewprocessor;

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
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class PolicyRenewDataProcessor {

	static Logger log = Logger.getLogger(PolicyRenewDataProcessor.class);
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
			quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewConstatnt.QUOTE_BUCKET);
		}
		try {
			policyRenewalNode = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
		} catch (JsonProcessingException e) {
			log.error("PolicyRenewConfigDetails Document Not Found",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("PolicyRenewConfigDetails Document Not Found",e);
			e.printStackTrace();
		}
	}

	public JsonNode prepareQuoteRequestHeader(String LOB)
	{
		headerNode = policyRenewalNode.get("headerNode");
		((ObjectNode) headerNode).put("transactionName",policyRenewalNode.get("LOBConfig").get(LOB));
		log.info("Policy Renewal Header Node : "+headerNode);
		return headerNode;
	}

	public JsonNode prepareQuoteRequestBody(String quoteId){
		quoteDataNode = getQuoteDoc(quoteId);
		bodyNode = objectMapper.createObjectNode();
		((ObjectNode) bodyNode).put(PolicyRenewConstatnt.QUOTE_PARAM,quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM));
		((ObjectNode) bodyNode).put(PolicyRenewConstatnt.VEHICLE_INFO,quoteDataNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO));
		((ObjectNode) bodyNode).put("requestType",quoteDataNode.findValue("requestType"));
		//Change Age of Person
		if(quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).has("personAge"))
		{
			int personAge = quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).get("personAge").asInt() + 1;
			//log.info("Person age :"+personAge);
			((ObjectNode) bodyNode.get(PolicyRenewConstatnt.QUOTE_PARAM)).put("personAge",personAge);
		}
		else
		{
			log.info("Person age not present in QUOTE_ID :");
		}
		//log.info("Prepare bodyNode : "+bodyNode);
		return bodyNode;
	}
	
	public JsonNode prepareHealthQuoteRequestBody(String quoteId)
	{
		ArrayNode renewSelectedFamilyMember = objectMapper.createArrayNode();
		int age;
		quoteDataNode = getQuoteDoc(quoteId);
		bodyNode = objectMapper.createObjectNode();
		((ObjectNode) bodyNode).put(PolicyRenewConstatnt.QUOTE_PARAM,quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM));
		((ObjectNode) bodyNode).put(PolicyRenewConstatnt.PERSONAL_INFO,quoteDataNode.findValue(PolicyRenewConstatnt.PERSONAL_INFO));
		((ObjectNode) bodyNode).put("requestType",quoteDataNode.findValue("requestType"));
		//Change Age of Person
		if(quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).has("selfAge"))
		{
			int selfAge = quoteDataNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).get("selfAge").asInt() + 1;
			((ObjectNode) bodyNode.get(PolicyRenewConstatnt.QUOTE_PARAM)).put("selfAge",selfAge);
		}
		else
		{
			log.info("selfAge not present in QUOTE_ID");
		}
		 renewSelectedFamilyMember = (ArrayNode) quoteDataNode.findValue(PolicyRenewConstatnt.PERSONAL_INFO).get("selectedFamilyMembers");
		 for(JsonNode member : renewSelectedFamilyMember)
		{
			 age = member.get("age").asInt()+1;
			((ObjectNode)member).put("age",age);
		}
		((ObjectNode) bodyNode.findValue(PolicyRenewConstatnt.PERSONAL_INFO)).put("selectedFamilyMembers",renewSelectedFamilyMember);
		
		//log.info("Prepare bodyNode : "+bodyNode);
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
			//log.info("In Recur :"+field);
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
		//log.info("prepareResponseNode proposalNode: "+proposalNode);
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
		//log.info("prepareResponseNode Method :"+responseNode);
		return responseNode;
	}

	public JsonNode getQuoteDoc(String docName){
		try {
			//log.info("Fetching quote DOC :"+docName);
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
		//log.info("policyDetailNode :"+policyDetailNode);
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

}