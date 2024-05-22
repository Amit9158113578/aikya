package com.idep.imatLead.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.filter.function.regexMatchFunction;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class CrmCreateLead {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(CrmCreateLead.class.getName());
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static String sugarCRMURL = null;
	static String iCRMURL = null;
	static JsonNode authNode = null;
	static JsonNode leadConfiguration = null;
	static
	{
		if (serverConfig != null) {
			try
			{
				sugarCRMURL = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ExternalServiceURLConfig").content()).toString()).get("SugarCRMService").textValue();
				iCRMURL = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ExternalServiceURLConfig").content()).toString()).get("iCRMService").textValue();
				authNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CRMAuthConfiguration").content()).toString());
				leadConfiguration = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CRMLeadConfiguration").content()).toString());
			}
			catch (Exception e)
			{
				log.error("iMATConfiguration Document Not Found", e);
				e.printStackTrace();
			}
		}
	}

	public String createCRMLead(String session_id, String moduleName, ObjectNode childNode) throws Exception
	{
		if (childNode.has("lob") && childNode.get("lob") != null){
			if ( childNode.get("lob").asText().equalsIgnoreCase("life"))
			{
				((ObjectNode) childNode).with("quoteParam").put("quoteType",1);
			}
			if ( childNode.get("lob").asText().equalsIgnoreCase("bike"))
			{
				((ObjectNode) childNode).with("quoteParam").put("quoteType",2);
			}
			if ( childNode.get("lob").asText().equalsIgnoreCase("car"))
			{
				((ObjectNode) childNode).with("quoteParam").put("quoteType",3);
			}
			if ( childNode.get("lob").asText().equalsIgnoreCase("health"))
			{
				((ObjectNode) childNode).with("quoteParam").put("quoteType",4);
			}
		}

		ObjectNode dataNode = objectMapper.createObjectNode();
		JsonNode quoteTypeNode = leadConfiguration.get("leadConfig").get("QuoteType" + childNode.findValue("quoteType").asText());
		if (quoteTypeNode.has("defaultParam"))
		{
			Map<String, String> defaultDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.get("defaultParam").toString(), Map.class);
			for (Map.Entry<String, String> field1 : defaultDataNodeMap.entrySet()) {

				dataNode.put((String)field1.getKey(), (String)field1.getValue());
			}
		}

		log.info("DataNode before default param..."+dataNode);			

		Map<String, String> configDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.toString(), Map.class);
		log.info("configDataNodeMap in iMAT"+configDataNodeMap);
		configDataNodeMap.remove("defaultParam");

		log.info("dataNode"+dataNode);
		log.info("reqNode"+childNode);
		log.info("configDataNodeMap1"+configDataNodeMap);

		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()) {
			log.info("Field to map"+childNode.findValue((String)field.getKey()));
			if (childNode.findValue(field.getKey().toString())!=null && childNode.findValue(field.getKey().toString()).isTextual()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).textValue());
			} else if (childNode.findValue(field.getKey().toString())!=null && childNode.findValue(field.getKey().toString()).isInt()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).intValue());
			} else if (childNode.findValue(field.getKey().toString())!=null && childNode.findValue(field.getKey().toString()).isLong()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).longValue());
			} else if (childNode.findValue(field.getKey().toString())!=null &&   childNode.findValue(field.getKey().toString()).isDouble()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).doubleValue());
			} else if (childNode.findValue(field.getKey().toString())!=null &&   childNode.findValue(field.getKey().toString()).isBoolean()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).booleanValue());
			} else if (childNode.findValue(field.getKey().toString())!=null &&   childNode.findValue(field.getKey().toString()).isFloat()) {
				dataNode.put((String)field.getValue(), childNode.findValue((String)field.getKey()).floatValue());
			}
			log.info("Mapped value childNode"+childNode);
			log.info("Mapped value dataNode"+dataNode);
		}	
		String sessionId = null;
		String getCreateRec = null;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		JsonNode node = null;
		try
		{
			httpclient = new DefaultHttpClient();
			httppost = new HttpPost(sugarCRMURL);
			ObjectNode parentNode = objectMapper.createObjectNode();
			if ((session_id == null) || (session_id.length() <= 0))
			{
				log.info("In if for getting sessionId");
				session_id = loginSugarCRM();
				parentNode.put("session", session_id);
				log.info("session id length is zero, craeted new SessionId");
			}
			else
			{
				parentNode.put("session", session_id);
			}
			parentNode.put("module_name", moduleName);


			if(dataNode.findValue("contactInfo")!=null || dataNode.findValue("quoteParam")!=null){
				dataNode.remove("contactInfo");
				dataNode.remove("quoteParam");
			}
			parentNode.put("name_value_list", dataNode);
			ArrayList postParameters = new ArrayList();
			postParameters.add(new BasicNameValuePair("method", "set_entry"));
			postParameters.add(new BasicNameValuePair("input_type", "JSON"));
			postParameters.add(new BasicNameValuePair("response_type", "JSON"));


			log.info("restData parentNode"+parentNode);

			postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			log.info("Create Record Request : " + postParameters.toString());
			log.info("session id :" + parentNode.get("session"));
			HttpResponse response = httpclient.execute(httppost);

			log.info("Create Record Response status : " + response.getStatusLine());

			HttpEntity entity = response.getEntity();

			getCreateRec = EntityUtils.toString(entity);
			log.info("Create Record Response data :" + getCreateRec);

			log.info("Create Record ERROR Response:" + getCreateRec);
			return getCreateRec;
		}
		catch (Exception e)
		{
			log.error("Sugar CRM Exception at create record: ", e);

		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}
		return getCreateRec;
	}
	public static String loginSugarCRM() 
	{
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		JsonNode node = null;
		String loginRes =null;
		try
		{
			String authKey = authNode.get("authentication").textValue();
			log.info("authKey for CRM : " + authKey);
			log.info("sugarCRMURL : " + sugarCRMURL);
			httpclient = new DefaultHttpClient();
			httppost = new HttpPost(sugarCRMURL);
			ArrayList<BasicNameValuePair> postParameters = new ArrayList();
			postParameters.add(new BasicNameValuePair("method", "login"));
			postParameters.add(new BasicNameValuePair("input_type", "JSON"));
			postParameters.add(new BasicNameValuePair("response_type", "JSON"));
			postParameters.add(new BasicNameValuePair("rest_data", authKey));
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			loginRes = EntityUtils.toString(entity);
			log.info("CRM Login Response : " + loginRes);
			if (loginRes != null)
			{
				node = objectMapper.readTree(loginRes);
				return node.findValue("id").textValue();
			}
			node = objectMapper.readTree(loginRes);
			log.info("Suite CRM login failed : " + node.toString());
			return "";
		}
		catch (Exception e)
		{
			log.error("Sugar CRM Exception at login : ", e);

		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}

		return loginRes;
	}
}