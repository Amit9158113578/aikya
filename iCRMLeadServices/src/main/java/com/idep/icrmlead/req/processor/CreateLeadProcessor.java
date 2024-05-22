package com.idep.icrmlead.req.processor;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.exception.processor.ExecutionTerminator;

public class CreateLeadProcessor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(CreateLeadProcessor.class.getName());
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static String sugarCRMURL = null;
	static String iCRMURL = null;
	static JsonNode authNode = null;
	
	static
	{
		if (serverConfig != null) {
			try
			{
				sugarCRMURL = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ExternalServiceURLConfig").content()).toString()).get("SugarCRMService").textValue();
				iCRMURL = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ExternalServiceURLConfig").content()).toString()).get("iCRMService").textValue();
				authNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("CRMAuthConfiguration").content()).toString());

			}
			catch (Exception e)
			{
				log.error("iMATConfiguration Document Not Found", e);
				e.printStackTrace();
			}
		}
	}

	public String createRecord(String session_id, String moduleName, ObjectNode childNode) throws Exception
	{
		String sessionId = null;

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
			}else{
				parentNode.put("session", session_id);
			}
			parentNode.put("module_name", moduleName);
			parentNode.put("name_value_list", childNode);
			ArrayList postParameters = new ArrayList();
			postParameters.add(new BasicNameValuePair("method", "set_entry"));
			postParameters.add(new BasicNameValuePair("input_type", "JSON"));
			postParameters.add(new BasicNameValuePair("response_type", "JSON"));
			postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			log.info("Create Record Request : " + postParameters.toString());
			log.info("session id :" + parentNode.get("session"));
			HttpResponse response = httpclient.execute(httppost);

			log.info("Create Record Response status : " + response.getStatusLine());

			HttpEntity entity = response.getEntity();

			String getCreateRec = EntityUtils.toString(entity);
			log.info("Create Record Response data :" + getCreateRec);
			if (getCreateRec != null)
			{
				node = objectMapper.readTree(getCreateRec);
				if ((node.has("name")) && (node.get("name").asText().equalsIgnoreCase("Invalid Session ID")))
				{
					log.info("Invalid Session Id so getting the latest one");
					getSession();
					return createRecord(sessionId, moduleName, childNode);
				}
				return node.findValue("id").asText();
			}
			log.info("Create Record ERROR Response:" + getCreateRec);
			return "";
		}
		catch (Exception e)
		{
			log.error("Sugar CRM Exception at create record: ", e);
			throw new ExecutionTerminator();
		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}
	}

	public String updateLead(ObjectNode data, String recordId)
	{
		this.log.info("WriteUpdateLead Record Id: " + recordId);
		String leadId = "";
		try
		{
			String sessionId = null;
			leadId = updateRecord(sessionId, "Leads", recordId, data);
			this.log.info("lead Updated Successfuly, Lead Id :" + leadId);
		}
		catch (Exception e)
		{
			leadId = "";
			this.log.error("Exception while updating lead record : ", e);
		}
		return leadId;
	}

	public String updateRecord(String session_id, String moduleName, String recordId, ObjectNode childNode)
			throws ExecutionTerminator
	{
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		JsonNode node = null;
		String sessionId = null;
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
				}else{
					parentNode.put("session", session_id);
				}
			 
			parentNode.put("module_name", moduleName);
			childNode.put("id", recordId);
			parentNode.put("name_value_list", childNode);
			ArrayList<BasicNameValuePair> postParameters = new ArrayList();
			postParameters.add(new BasicNameValuePair("method", "set_entry"));
			postParameters.add(new BasicNameValuePair("input_type", "JSON"));
			postParameters.add(new BasicNameValuePair("response_type", "JSON"));
			postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
			log.info("Update Record Request : " + postParameters);
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			String getUpdateRec = EntityUtils.toString(entity);
			log.info("Update Record Response : " + getUpdateRec);
			
			if (getUpdateRec != null)
			{
				node = objectMapper.readTree(getUpdateRec);
				if ((node.has("name")) && (node.get("name").asText().equalsIgnoreCase("Invalid Session ID")))
				{
					log.info("Invalid Session Id so getting the latest one");
					getSession();
					return updateRecord(sessionId, moduleName, recordId, childNode);
				}
				return node.findValue("id").textValue();
			}
			return "";
		}
		catch (Exception e)
		{
			log.error("Sugar CRM Exception at update record: ", e);
			throw new ExecutionTerminator();
		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}
	}
	public String createLead(ObjectNode data)
	{
		String leadId = "";
		try
		{
			String sessionId = null;
			leadId = createRecord(sessionId, "Leads", data);
			this.log.info("Lead Created Successfuly, Lead Id :" + leadId);
		}
		catch (Exception e)
		{
			leadId = "";
			this.log.error("Exception while creating lead record : ", e);
		}
		return leadId;
	}
	public String findLead(String query, String select_fields)
	{
		String leadId = "";
		try
		{
			String sessionId = null;
			log.info("Calling GetModule Method : "+sessionId+" \t "+query+" "+select_fields);
			leadId = getModuleData(sessionId, "Leads", query, select_fields);
			this.log.info("Lead Found With lead ID: " + leadId);
		}
		catch (Exception e)
		{
			leadId = "";
			this.log.error("Exception while finding lead record : ", e);
		}
		return leadId;
	}

	public static String loginSugarCRM() throws ExecutionTerminator
	{
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		JsonNode node = null;
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
			String loginRes = EntityUtils.toString(entity);
			log.info("CRM Login Response : " + loginRes);
			String str1;
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
			throw new ExecutionTerminator();
		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}
	}

	public static void getSession() throws ExecutionTerminator
	{
		String sessionId = loginSugarCRM();
	}
	
	public String getModuleData(String session_id, String moduleName, String query, String select_fields) throws ExecutionTerminator
	{
		String result = "";		    
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		
		try
		{
			try
			{
				log.info("getModuleData() execution STarted");
				
				httpclient = new DefaultHttpClient();
				log.info("getModuleData() service url : "+sugarCRMURL);
				if(sugarCRMURL == null){
				sugarCRMURL=objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("ExternalServiceURLConfig").content()).toString()).get("SugarCRMService").textValue();
				log.info("getModuleData() service url fetched : "+sugarCRMURL);
				}
				
				httppost = new HttpPost(sugarCRMURL);
				log.info("getModuleData() http connection created : "+httppost.getAllHeaders());
				ObjectNode parentNode = objectMapper.createObjectNode();
				//parentNode.put("session", session_id);
				log.info("getModuleData() request Recived : "+moduleName+" "+query);
				if ((session_id == null) || (session_id.length() <= 0))
				{
					log.info("In if for getting sessionId");
					session_id = loginSugarCRM();
					parentNode.put("session", session_id);
					log.info("session id length is zero, craeted new SessionId");
				}else{
					parentNode.put("session", session_id);
				}
				log.info("parent Node"+parentNode);
				parentNode.put("module_name", moduleName);
				parentNode.put("query", query);
				parentNode.put("order_by", "");
				parentNode.put("offset", "0");
				parentNode.put("select_fields", select_fields);
				parentNode.put("deleted", "false");

				ArrayList<BasicNameValuePair> postParameters = new ArrayList();
				postParameters.add(new BasicNameValuePair("method", "get_entry_list"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				log.info("Get Entry List 1 : " + authNode);
				log.info("Get Entry List 2 : " + sugarCRMURL);

				log.info("Get Module Data Request : " + postParameters);
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				log.info("Get Module Data Response status code : " + response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String getRes = EntityUtils.toString(entity);
				log.info("Get Module Data Response : " + getRes);
				log.info("3");
				if (getRes != null)
				{
					JsonNode resultNode = objectMapper.readTree(getRes);
					if ((resultNode.has("name")) && (resultNode.get("name").asText().equalsIgnoreCase("Invalid Session ID")))
					{
						log.info("Invalid Session Id so getting the latest one");
						getSession();
						return getModuleData(session_id, moduleName, query, select_fields);
					}
					if (resultNode.findValue("result_count").intValue() > 0)
					{
						ArrayNode arr = (ArrayNode)resultNode.findValue("entry_list");
						if (arr.size() > 0) {
							for (JsonNode jsonNode : arr) {
								result = jsonNode.get("id").textValue();
							}
						}
					}
					else
					{
						result = "";
					}
				}
			}
			catch (Exception e)
			{
				log.error("Sugar CRM Exception : ", e);
				throw new ExecutionTerminator();
			}
		}
		finally
		{
			httpclient.getConnectionManager().shutdown();
		}
		log.info("sugar crm lead ID:" + result);
		return result;
	}
}
