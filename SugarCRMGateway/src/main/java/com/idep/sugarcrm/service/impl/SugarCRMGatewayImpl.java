package com.idep.sugarcrm.service.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.couchbase.client.deps.io.netty.handler.codec.http.HttpContentEncoder.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.exception.processor.ExecutionTerminator;
import com.idep.sugarcrm.util.SugarCRMConstants;

/**
 * 
 * @author sandeep.jadhav sugar CRM API methods
 */
public class SugarCRMGatewayImpl {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(SugarCRMGatewayImpl.class.getName());
	static SugarCRMGatewayImpl sugarinstance = null;
	static CBService serverConfig = null;
	static String sugarCRMURL = null;
	static String iCRMURL = null;
	static JsonNode authNode = null;
	//int serverErrCounter =0;

	static {
		if (serverConfig == null) {
			// log.info("get ServerConfig connection");
			serverConfig = CBInstanceProvider.getServerConfigInstance();

			try {
				sugarCRMURL = objectMapper
						.readTree(serverConfig.getDocBYId("ExternalServiceURLConfig").content().toString())
						.get("SugarCRMService").textValue();
				iCRMURL = objectMapper
						.readTree(serverConfig.getDocBYId("ExternalServiceURLConfig").content().toString())
						.get("iCRMService").textValue();
				authNode = objectMapper.readTree(serverConfig.getDocBYId("CRMAuthConfiguration").content().toString());
			} catch (JsonProcessingException e) {				
				log.error("sugarCRMURL Document Not Found",e);
				e.printStackTrace();
			} catch (IOException e) {

				log.error("sugarCRMURL Document Not Found",e);
				e.printStackTrace();
			}
			catch (Exception e) {
				log.error("sugarCRMURL Document Not Found",e);

				e.printStackTrace();
			}
		}
	}

	/**
	 * get single instance of the class
	 * 
	 * @return
	 */
	public static SugarCRMGatewayImpl getSugarCRMInstance() {
		if (sugarinstance == null) {
			synchronized (SugarCRMGatewayImpl.class) {
				sugarinstance = new SugarCRMGatewayImpl();
			}
		}
		return sugarinstance;
	}

	/**
	 * 
	 * @param session_id
	 *            - user gets session id after successful login
	 * @param moduleName
	 *            - module record to be created
	 * @param childNode
	 *            - this parameter holds data
	 * @return
	 */
	public String createRecord(String session_id, String moduleName, ObjectNode childNode) {

		String sessionId= null;
		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;
		try {
			try {
				// log.info(moduleName+" Data:: "+childNode);
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				//parentNode.put("session", "");
				log.info("session id length "+parentNode.get("session").asText().length());
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					sessionId=loginSugarCRM();
					parentNode.put("session", sessionId);	
					log.info("session id length is zero, craeted new SessionId");
				}
				parentNode.put("module_name", moduleName);
				parentNode.put("name_value_list", (JsonNode) childNode);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "set_entry"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				log.info("Create Record Request : "+postParameters.toString());
				log.info("session id :"+parentNode.get("session"));
				HttpResponse response = httpclient.execute(httppost);

				log.info("Create Record Response status : "+response.getStatusLine());
				// System.out.println("response : "+response.getEntity());
				// log.info("Entity :: "+response.getEntity());
				HttpEntity entity = response.getEntity();
				// log.info("entity......"+ entity);
				// log.info("create record response : "+response.toString());
				// log.info("create record post parameter"+postParameters);

				String getCreateRec = EntityUtils.toString(entity);
				log.info("Create Record Response data :"+getCreateRec);
				// log.info("create record"+getCreateRec);

				if (getCreateRec != null) {
					node = objectMapper.readTree(getCreateRec);

					// System.out.println("Id :
					// "+node.findValue("id").textValue());
					// System.out.println(moduleName + " record created : " +
					// node.findValue("id").textValue());
					// // System.out.println(moduleName + "record Created
					// text value: "+ node.findValue("id").textValue());
					if (node.has("name")&& node.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();
						return createRecord(SugarSession.sessionId, moduleName, childNode);

					} else {
						return node.findValue("id").asText();
					}

				} else {
					log.info("Create Record ERROR Response:" + getCreateRec);
					return "";
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at create record: ", e);
				throw new ExecutionTerminator();			

			}
		}
		catch (Exception e) {

			log.error("Sugar CRM Exception at create record: ", e);
			return "";
		}

		finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	/**
	 * 
	 * @param session_id
	 *            - user gets session id after successful login
	 * @param moduleName
	 *            - module record to be updated
	 * @param recordId
	 *            - record to be updated
	 * @param childNode
	 *            - this holds data
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public String updateRecord(String session_id, String moduleName, String recordId, ObjectNode childNode) throws ExecutionTerminator {


		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;
		String sessionId=null;
		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					sessionId=loginSugarCRM();
					parentNode.put("session", sessionId);	
					log.info("session id length is zero, craeted new SessionId");
				}
				parentNode.put("module_name", moduleName);
				childNode.put("id", recordId);
				parentNode.put("name_value_list", (JsonNode) childNode);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "set_entry"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				log.info("Update Record Request : "+postParameters);
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String getUpdateRec = EntityUtils.toString(entity);
				log.info("Update Record Response : "+getUpdateRec);
				if (getUpdateRec != null) {

					node = objectMapper.readTree(getUpdateRec);
					// this.log.info(moduleName + " record updated : " +
					// node.findValue("id").textValue());


					if (node.has("name")&& node.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return updateRecord(SugarSession.sessionId, moduleName, recordId,childNode);

					} else {

						return node.findValue("id").textValue();
					}

					//return node.findValue("id").textValue();
				} else {
					return "";
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at update record: ", e);
				throw new ExecutionTerminator();

			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	/**
	 * delete record from sugar CRM
	 * 
	 * @param session_id
	 *            - user gets session id after successful login
	 * @param moduleName
	 *            - provide module name to delete record from
	 * @param recordId
	 *            - record to be removed
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public String deleteRecord(String session_id, String moduleName, String recordId) throws ExecutionTerminator {

		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;
		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				parentNode.put("module_name", moduleName);

				/**
				 * prepare request attribute to delete record
				 */
				ObjectNode childNode = objectMapper.createObjectNode();
				childNode.put("id", recordId);
				childNode.put("deleted", "1");

				parentNode.put("name_value_list", (JsonNode) childNode);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "set_entry"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					node = objectMapper.readTree(EntityUtils.toString(entity));

					if (node.has("name")&& node.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return 	deleteRecord(SugarSession.sessionId,moduleName,recordId) ;

					} else 
					{					

						log.info(moduleName + " record deleted : " + node.findValue("id").textValue());
						return node.findValue("id").textValue();
					}

				} else {
					return "";
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at delete record: ", e);
				throw new ExecutionTerminator();
				//return "";
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	/**
	 * relate two modules by using link field reference
	 * 
	 * @param session_id
	 * @param moduleName
	 * @param module_id
	 * @param link_field_name
	 * @param related_ids
	 * @param delete
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public String createRelation(String session_id, String moduleName, String module_id, String link_field_name,
			String related_ids, int delete) throws ExecutionTerminator {

		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;
		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				// this.log.info((Object)("session id : " + session_id));
				parentNode.put("session", session_id);
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					session_id=loginSugarCRM();
					parentNode.put("session", session_id);	
					log.info("session id length is zero, craeted new SessionId");
				}				
				parentNode.put("module_name", moduleName);
				parentNode.put("module_id", module_id);
				log.info("link_field_name FOnd in : "+link_field_name);
				parentNode.put("link_field_name", link_field_name);
				parentNode.put("related_ids", related_ids);
				parentNode.put("delete", delete);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "set_relationship"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				log.info("Create Relation Request createRelation : "+postParameters);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String s = EntityUtils.toString(entity);

					node = objectMapper.readTree(s);
					if (node.has("name")&& node.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return createRelation(SugarSession.sessionId, moduleName,module_id,link_field_name,related_ids,delete);						
					} 					
					// this.log.debug("node : " + node.toString());
				}
				else
				{
					log.info("submit request manually to check issue");
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at create relation : ", e);
				throw new ExecutionTerminator();
				//return "";
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		// this.log.debug("relation created status : " +
		// node.findValue("created").intValue());

		if (node.findValue("created").intValue() == 1) {
			return "Relation_Created";
		}
		return "Relation_Not_Created";
	}

	/**
	 * 
	 * @param session_id
	 * @param moduleName
	 * @param module_id
	 * @param link_field_name
	 * @param related_fields
	 * @param delete
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public String getRelationData(String session_id, String moduleName, String module_id, String link_field_name,
			JsonNode related_fields, boolean delete) throws ExecutionTerminator {

		JsonNode node = null;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		String sessionId=null;
		try {

			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					sessionId=loginSugarCRM();
					parentNode.put("session", sessionId);	
					log.info("session id length is zero, craeted new SessionId");
				}
				parentNode.put("module_name", moduleName);
				parentNode.put("module_id", module_id);
				parentNode.put("link_field_name", link_field_name);
				parentNode.put("related_module_query", "");// enq_prospect_leads_1.name
				// IS NOT NULL
				parentNode.put("related_fields", related_fields);
				// parentNode.put("related_module_link_name_to_fields_array",
				// "name");
				parentNode.put("order_by", "");
				parentNode.put("offset", "0");
				parentNode.put("delete", false);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "get_relationships"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				if (entity != null) {

					String s = EntityUtils.toString(entity);
					node = objectMapper.readTree(s);

					if (node.has("name")&& node.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return getRelationData(SugarSession.sessionId,moduleName,module_id,link_field_name,
								related_fields,delete);						
					} else
					{								
						return node.toString();
					}
				} else {
					return "";
				}

			} catch (Exception e) {

				log.error("Sugar CRM Exception : ", e);
				throw new ExecutionTerminator();
				//return "";
			}
		} finally {

			httpclient.getConnectionManager().shutdown();

		}

	}

	/**
	 * fetch data from CRM based on provided criteria
	 * 
	 * @param session_id
	 * @param moduleName
	 * @param query
	 * @param select_fields
	 * @return
	 * @throws ExecutionTerminator 
	 */



	public String getModuleData(String session_id, String moduleName, String query, String select_fields) throws ExecutionTerminator

	{
		String result = "";
		DefaultHttpClient httpclient;
		httpclient = null;
		HttpPost httppost = null;
		String sessionId=null;
		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					sessionId=loginSugarCRM();
					parentNode.put("session", sessionId);	
					log.info("session id length is zero, craeted new SessionId");
				}
				//System.out.println("Session :"+sessionId);
				parentNode.put("module_name", moduleName);
				parentNode.put("query", query);
				parentNode.put("order_by", "");
				parentNode.put("offset", "0");
				parentNode.put("select_fields", select_fields);
				parentNode.put("deleted", "false");
				// log.info("Parent node Content :"+parentNode);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "get_entry_list"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				log.info("Get Entry List 1 : "+authNode);
				log.info("Get Entry List 2 : "+sugarCRMURL);
				// log.info("value of post parameter from
				// getmodule"+postParameters);
				log.info("Get Module Data Request : "+postParameters);
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				log.info("Get Module Data Response status code : "+response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String getRes = EntityUtils.toString(entity);
				log.info("Get Module Data Response : "+getRes);
				System.out.println("3");
				if (getRes != null) {
					JsonNode resultNode = objectMapper.readTree(getRes);
					if (resultNode.has("name")&& resultNode.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {
						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return getModuleData(SugarSession.sessionId,moduleName, query,select_fields);						
					}
					if (resultNode.findValue("result_count").intValue() > 0) {
						// result = "DUP_FOUND";
						ArrayNode arr = (ArrayNode) resultNode.findValue("entry_list");
						if (arr.size() > 0) {
							for (JsonNode jsonNode : arr) {
								result = jsonNode.get("id").textValue();
							}
						}
					} else {
						result = "";
					}
				}
			} catch (Exception e) {
				log.error("Sugar CRM Exception : ", e);
				throw new ExecutionTerminator();
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		log.info("sugar crm lead ID:" + result);
		return result;
	}

	/**
	 * 
	 * @param session_id
	 * @param moduleName
	 * @param query
	 * @param select_fields
	 * @param rlnNode
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public JsonNode fetchModuleData(String session_id, String moduleName, String query, JsonNode select_fields,
			JsonNode rlnNode) throws ExecutionTerminator {

		DefaultHttpClient httpclient;
		httpclient = null;
		HttpPost httppost = null;
		JsonNode resultNode = null;

		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					session_id=loginSugarCRM();
					parentNode.put("session", session_id);	
					log.info("session id length is zero, craeted new SessionId");
				}
				
				parentNode.put("module_name", moduleName);
				parentNode.put("query", query);
				parentNode.put("order_by", "");
				parentNode.put("offset", "0");
				// parentNode.put("select_fields", select_fields);
				parentNode.put("select_fields", select_fields);
				parentNode.put("link_name_to_fields_array", rlnNode);
				parentNode.put("deleted", "false");
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "get_entry_list"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));

				log.info("Fetch Module Data Request : "+postParameters);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// log.info("value of post parameter from
				// fetchmodule"+postParameters);
				// log.info("value of post parameter from getmodule
				// response"+response);
				String fetchmodule = EntityUtils.toString(entity);
				log.info("Fetch Module Data Response : "+fetchmodule);
				// log.info("value of post parameter from getmodule
				// response"+fetchmodule);
				if (fetchmodule != null) {

					resultNode = objectMapper.readTree(fetchmodule);

					if (resultNode.has("name")&& resultNode.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {

						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return fetchModuleData(SugarSession.sessionId, moduleName,query, select_fields,rlnNode);						
					}

					// log.info("from sugar resultNode"+resultNode);

				} else {
					resultNode = null;
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception : ", e);
				resultNode = null;
				throw new ExecutionTerminator();
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return resultNode;
	}

	/**
	 * get session id
	 * 
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public String loginSugarCRM() throws ExecutionTerminator {
		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;

		try {
			try {

				/*
				 * if (this.serverConfig == null) { log.info(
				 * "get ServerConfig connection"); this.serverConfig =
				 * CBInstanceProvider.getServerConfigInstance();
				 * this.sugarCRMURL =
				 * this.objectMapper.readTree(this.serverConfig.getDocBYId(
				 * "ExternalServiceURLConfig").content().toString()).get(
				 * "SugarCRMService").textValue(); }
				 */
				// authNode =
				// this.objectMapper.readTree(this.serverConfig.getDocBYId("CRMAuthConfiguration").content().toString());
				String authKey = authNode.get("authentication").textValue();
				log.info("authKey for CRM : "+authKey);
				log.info("sugarCRMURL : "+sugarCRMURL);
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "login"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));


				// postParameters.add(new BasicNameValuePair("rest_data",
				// "{\"user_auth\":{
				// \"user_name\":\"admin\",\"password\":\"2c0f8d4f3a3809deeb51e4dec049fa01\"},\"application_name\":\"test\"
				// }"));
				// 92733af72bca67ca787f8334b81c5e88
				// log.info("value of post parameter from
				// loginchmodule"+postParameters);
				postParameters.add(new BasicNameValuePair("rest_data", authKey));
				httppost.setEntity((HttpEntity) new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String loginRes = EntityUtils.toString(entity);
				log.info("CRM Login Response : "+loginRes);
				if (loginRes != null) {
					node = objectMapper.readTree(loginRes);
					return node.findValue("id").textValue();

				} else {
					node = objectMapper.readTree(loginRes);
					log.info("Suite CRM login failed : " + node.toString());
					return "";
				}
			} catch (Exception e) {
				log.error("Sugar CRM Exception at login : ", e);
				throw new ExecutionTerminator();
				//return "";
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	public String loginSugarCRM(String username, String password) throws ExecutionTerminator {

		JsonNode node;
		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		node = null;

		try {
			try {

				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(iCRMURL);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "login"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", "{\"user_auth\":{ \"user_name\":\"" + username
						+ "\",\"password\":\"" + password + "\"},\"application_name\":\"test\" }"));

				httppost.setEntity((HttpEntity) new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String loginRes = EntityUtils.toString(entity);
				log.info("loginRes : " + loginRes);

				if (loginRes != null) {
					node = objectMapper.readTree(loginRes);
					return node.findValue("id").textValue();

				} else {
					node = objectMapper.readTree(loginRes);

					log.info("Suite CRM login failed : " + node.toString());
					return "";
				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at login : ", e);
				throw new ExecutionTerminator();
				//e.printStackTrace();
				//return "";
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

	}

	/**
	 * terminate user session
	 * 
	 * @param session_id
	 * @return
	 * @throws ExecutionTerminator 
	 */
	public boolean logoutSugarCRM(String session_id) throws ExecutionTerminator {

		DefaultHttpClient httpclient = null;
		HttpPost httppost = null;
		boolean status = true;

		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "logout"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", "{\"session\":" + session_id + " }"));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String logoutRes = EntityUtils.toString(entity);
				if (logoutRes != null) {
					status = true;
				} else {
					JsonNode node = objectMapper.readTree(logoutRes);
					log.info("Suite CRM logout failed : " + node.toString());
					return false;

				}
			} catch (Exception e) {

				log.error("Sugar CRM Exception at logout: ", e);
				status = false;
				throw new ExecutionTerminator();
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return status;
	}


	public ObjectNode getSelectedFieldData(String session_id, String moduleName, String query, String select_fields) throws ExecutionTerminator
	{	
		ObjectNode result = objectMapper.createObjectNode();
		DefaultHttpClient httpclient;
		httpclient = null;
		HttpPost httppost = null;
		String sessionId=null;
		JsonNode resultNode = null;
		try {
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(sugarCRMURL);
				ObjectNode parentNode = objectMapper.createObjectNode();
				parentNode.put("session", session_id);
				
				if (parentNode.get("session") == null || parentNode.get("session").asText().length()<=0 )
				{
					sessionId=loginSugarCRM();
					parentNode.put("session", sessionId);	
					log.info("session id length is zero, craeted new SessionId");
				}
				parentNode.put("module_name", moduleName);
				parentNode.put("query", query);
				parentNode.put("order_by", "");
				parentNode.put("offset", "0");
				parentNode.put("select_fields","");
				parentNode.put("deleted", "false");
				ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
				postParameters.add(new BasicNameValuePair("method", "get_entry_list"));
				postParameters.add(new BasicNameValuePair("input_type", "JSON"));
				postParameters.add(new BasicNameValuePair("response_type", "JSON"));
				postParameters.add(new BasicNameValuePair("rest_data", parentNode.toString()));
				log.info("Get Module Data Request : "+postParameters);
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				HttpResponse response = httpclient.execute(httppost);
				log.info("Get Module Data Response status code : "+response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String getRes = EntityUtils.toString(entity);
				log.info("Get Module Data Response : "+getRes);
				if (getRes != null) {
					resultNode = objectMapper.readTree(getRes);
					if (resultNode.has("name")&& resultNode.get("name").asText().equalsIgnoreCase(SugarCRMConstants.INVALID_SESSION)) {
						log.info("Invalid Session Id so getting the latest one");
						SugarSession.getSession();						
						return getSelectedFieldData(SugarSession.sessionId,moduleName, query,select_fields);						
					}
					if (resultNode.findValue("result_count").intValue() > 0) {
						if(resultNode.has("entry_list") && resultNode.get("entry_list") != null){
							ArrayNode arr =(ArrayNode)resultNode.findValue("entry_list");
							if (arr.size() > 0) {
								for (JsonNode jsonNode : arr) {
									for (String field : select_fields.split(",")){
										log.info("Filed Name :"+field);
										result.put(field,jsonNode.get("name_value_list").get(field).get("value"));
									}
								}
							}	
						}

					} else {
						result = null;
					} 
				}
			} catch (Exception e) {
				log.error("Sugar CRM Exception : ", e);
				throw new ExecutionTerminator();
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		log.info("sugar Selected Field Data: " + result);
		return result;
	}

	public static void main(String[] args) throws JsonProcessingException, ExecutionTerminator 
{


		/*ObjectMapper objectMapper = new ObjectMapper();

		ObjectNode node = objectMapper.createObjectNode();
		node.put("name", "P365 Ticket");
		node.put("priority", "Urgent");
		node.put("status", "Old");
		node.put("type", "Query");
		node.put("bug_number", 7);
		//System.out.println("Node : " + node); */
		//SugarCRMGatewayImpl impl = new SugarCRMGatewayImpl();
		//SugarCRMModuleServices moduleServices = new SugarCRMModuleServices();
		//JsonNode dd=impl.getSelectedFieldData(impl.loginSugarCRM(), "Leads","leads_cstm.messageid_c ='LEADMSGID13857'","id,assigned_user_id,messageid_c");
		//System.out.println("dd :"+dd.asText());
		//String ll=moduleServices.findLead("leads_cstm.messageid_c = LEADMSGID13241", "id");
		//System.out.println("Printing "+ll);
		/*
		 * SimpleDateFormat format = new
		 * SimpleDateFormat("ddMMyyyyhh24mmssSSS",Locale.ENGLISH); Date date =
		 * new Date(); String currentDate = format.format(date);
		 * System.out.println("formatted date : "+currentDate);
		 * 
		 * Date date1 = new Date(); String currentDate1 = format.format(date1);
		 * System.out.println("formatted date : "+currentDate1);
		 * 
		 * Date date2 = new Date(); String currentDate2 = format.format(date2);
		 * System.out.println("formatted date : "+currentDate2);
		 */

		/*
		 * SugarCRMGatewayImpl crm = new SugarCRMGatewayImpl();
		 * System.out.println("-------------------------------------------");
		 * String query =
		 * "leadcreateddate_c>=(CURDATE() - INTERVAL 5 DAY) and leads.phone_mobile='9823027399'"
		 * ; String selectFields = "id"; String sessionId = crm.loginSugarCRM();
		 * 
		 * String status = crm.createRelation(sessionId, "Enq_Prospect",
		 * "c4de1d71-bff4-4246-687c-5972d934dfbf", "enq_prospect_leads",
		 * "a86005da-db26-f24e-a537-5972d9696fd7", 0); System.out.println(
		 * "relation status : "+status); String s = crm.getModuleData(sessionId,
		 * "Leads", query, selectFields); System.out.println("data id : "+s);
		 * 
		 * 
		 * 
		 * ObjectMapper objectMapper = new ObjectMapper(); ObjectNode rlnNode =
		 * objectMapper.createObjectNode(); ObjectNode selectFieldsNode =
		 * objectMapper.createObjectNode(); selectFieldsNode.put("id", "id");
		 * selectFieldsNode.put("email1", "email1");
		 * 
		 * rlnNode.put("email1", "rohini.jadhav221@policies365.com"); JsonNode
		 * result = crm.fetchModuleData(sessionId, "Leads", query,
		 * selectFieldsNode, rlnNode); System.out.println("result : "+result);
		 */

		/*
		 * System.out.println("sugarCRMURL : " + crm.sugarCRMURL); ObjectMapper
		 * objectMapper = new ObjectMapper(); String sessionid =
		 * crm.loginSugarCRM();
		 * 
		 * //JsonObject leadChildNode; System.out.println("session id : " +
		 * sessionid); System.out.println("sugarCRMURL : " + crm.sugarCRMURL);
		 * 
		 * 
		 * ObjectNode leadChildNode = objectMapper.createObjectNode();
		 * System.out.println("creating object node ");
		 * leadChildNode.put("first_name", "ARUN");
		 * leadChildNode.put("last_name", "VERMA");
		 * leadChildNode.put("phone_mobile", "9028326705");
		 * leadChildNode.put("email1", "sandeep.jadhav@policies365.com");
		 * 
		 * System.out.println("object node created");
		 * 
		 * //String query = "enq_prospect.phone_mobile='" +
		 * leadChildNode.get("phone_mobile").textValue() + "'" + //
		 * " and useremail_c='" + leadChildNode.get("email1").textValue() + "'";
		 * String query = "leads.status='New' and lineof_business_c='Life'";
		 * 
		 * // select field array ArrayNode arr = objectMapper.createArrayNode();
		 * arr.add("status"); arr.add("lineof_business_c"); arr.add("email1");
		 * // arr.add("campaign_name"); //arr.add("campaign_status");
		 * 
		 * JsonNode select_fields = arr;
		 * 
		 * // link name to field array
		 * 
		 * ArrayNode arrEmail = objectMapper.createArrayNode();
		 * arrEmail.add("id"); arrEmail.add("name"); // arrEmail.add("status");
		 * 
		 * ObjectNode objNode = objectMapper.createObjectNode();
		 * objNode.put("name", "enq_prospect_leads"); objNode.put("value",
		 * arrEmail);
		 * 
		 * ArrayNode arrEmailEle = objectMapper.createArrayNode();
		 * arrEmailEle.add(objNode);
		 * 
		 * ArrayNode arrFinal = objectMapper.createArrayNode();
		 * arrFinal.add(arrEmailEle); JsonNode rlnNode=arrFinal;
		 * 
		 * 
		 * //JsonNode result = crm.fetchModuleData(sessionid, "Leads", query,
		 * select_fields,rlnNode); JsonNode result =
		 * crm.fetchModuleData(sessionid, "Leads", query,
		 * select_fields,rlnNode); JsonNode leadsDataNode=null; if
		 * (result.findValue("result_count").intValue() > 0) {
		 * 
		 * leadsDataNode = result.findValue("entry_list"); }
		 * 
		 * for(JsonNode node : leadsDataNode) { System.out.println(node); }
		 * 
		 * // select field array ArrayNode arr1 =
		 * objectMapper.createArrayNode(); arr1.add("id");
		 * arr1.add("first_name"); arr1.add("last_name");
		 * arr1.add("phone_mobile"); arr1.add("email1");
		 * arr1.add("useremail_c"); arr1.add("prospect_created_date");
		 * arr1.add("prospect_status"); arr1.add("prospect_source");
		 * 
		 * // arr.add("phone_mobile"); // arr.add("campaign_name");
		 * //arr.add("campaign_status");
		 * 
		 * JsonNode related_fields = arr1;
		 * 
		 * crm.getRelationData(sessionid, "Leads",
		 * "2407a242-f718-f6df-5ce7-57d15d10f8ca", "enq_prospect_leads_1",
		 * related_fields, false);
		 * 
		 * String query = ""; String select_fields =
		 * "campaigns.name='Policies365'"; String dup_chk_result =
		 * crm.getModuleData(sessionid, "Campaigns", query, select_fields);
		 * 
		 * 
		 * System.out.println("---------------------------------"); String
		 * relStatus = crm.createRelation(sessionid, "Enq_Prospect",
		 * "15e261f7-96ff-7e8c-da3f-57d00b1240e2", "enq_prospect_leads_1",
		 * "54660696-965b-3964-ce7c-57d00bb7e166", 0);
		 * 
		 * if (relStatus.equalsIgnoreCase("Relation_Created")) {
		 * System.out.println("Lead-Prospect relation created"); } else {
		 * System.out.println("unable to create Lead-Prospect relation"); }
		 * 
		 * 
		 * 
		 * String query = "leads.status='New'";
		 * 
		 * String select_fields = "first_name"; JsonNode data =
		 * crm.fetchModuleData(sessionid, "Leads", query, select_fields);
		 * System.out.println(data);
		 * 
		 * 
		 * // System.out.println("Prospect duplicate check : " +
		 * dup_chk_result);
		 * 
		 */
	}

	/*
	 * public void testStarHealthPost() {
	 * 
	 * DefaultHttpClient httpclient = null; HttpPost httppost = null;
	 * 
	 * try { try { httpclient = new DefaultHttpClient(); httppost = new
	 * HttpPost(
	 * "http://sandbox.ig.nallantech.com:9000/api/proposal/premium/calculate");
	 * ObjectNode parentNode = this.objectMapper.createObjectNode(); ObjectNode
	 * insuredNode = this.objectMapper.createObjectNode();
	 * insuredNode.put("dob", "Oct 19, 1976"); parentNode.put("APIKEY",
	 * "9424df66b6a04bff8f69c79be403d6f5"); parentNode.put("SECRETKEY",
	 * "45a2fc0295424fc58027441d8be76d03"); parentNode.put("policyTypeName",
	 * "COMPREHENSIVE"); parentNode.put("schemeId", 1);
	 * parentNode.put("postalCode", "110002"); parentNode.put("sumInsuredId",
	 * 2); parentNode.put("insureds[0]", insuredNode); System.out.println(
	 * "input req : "+parentNode.toString()); StringEntity postingString = new
	 * StringEntity(parentNode.toString()); postingString.setContentType(new
	 * BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	 * httppost.setEntity(postingString); HttpResponse response =
	 * httpclient.execute(httppost); HttpEntity entity = response.getEntity();
	 * System.out.println("response : "+EntityUtils.toString(entity)); } catch
	 * (Exception e) { e.printStackTrace(); this.log.error((Object)
	 * "Exception occurred"); httpclient.getConnectionManager().shutdown(); } }
	 * finally {
	 * 
	 * }
	 */

}
