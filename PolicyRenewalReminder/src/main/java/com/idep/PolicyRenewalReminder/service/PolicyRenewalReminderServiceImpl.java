package com.idep.PolicyRenewalReminder.service;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.processor.PolicyRenwalDataProvider;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyRenewalReminderServiceImpl {
	PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();

	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static Logger log = Logger.getLogger(PolicyRenewalReminderServiceImpl.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static String policyRenewalConfig = serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content().toString();
	String URLShortnerConfig = serverConfig.getDocBYId(PolicyRenewalConstatnt.URL_SHORTNER_CONFIG).content().toString();
	public String PolicyReminderData(String proposalId) throws JsonProcessingException, IOException
	{
		log.info("Recieved Policy Reminder Proposal Id : " + proposalId);
		return proposalId;
	}
	public String offlineRenewalReminder(String policyId) throws JsonProcessingException, IOException
	{
		log.info("Recieved Policy CreateLead request with Policy Id : " + policyId);
		return policyId;
	}
	
	public String runOfflineReminderManual(String request)
	{
		return request;
	}

	public String readEmailStatus(String readEmailStatusReq) throws JsonProcessingException, IOException
	{
		return readEmailStatusReq;
	}

	public JsonNode getQuote(JsonNode jsonObj)
	{
		log.info("In getQuote method");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String quoteResponse = null;
		JsonNode quoteResponseNode = null;
		JsonNode resultNode = null; 
		HttpResponse response;
		try {
			JsonNode policyRenewalConfigNode = objectMapper.readTree(policyRenewalConfig);
			HttpPost httpPost = new HttpPost(policyRenewalConfigNode.get("masterService").asText());
			StringEntity entity = new StringEntity(jsonObj.toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if(policyRenewalConfigNode.has("Origin"))
			{
				httpPost.setHeader("Origin", policyRenewalConfigNode.get("Origin").asText());
			}
			response = httpClient.execute(httpPost);
			log.info("Policy Renewal Response Status Code : "+response.getStatusLine().getStatusCode());
			quoteResponse = EntityUtils.toString(response.getEntity());
			log.info("Response in JSON : "+ quoteResponse.replace("\\\"", "\""));
			String kk = quoteResponse.replace("\\\"", "\"");
			quoteResponseNode = objectMapper.readTree(kk.substring(1, kk.length()-1));
			httpClient.getConnectionManager().shutdown();
			if (quoteResponseNode != null && quoteResponseNode.has("responseCode") && quoteResponseNode.get("responseCode").asInt() != 1002 ){
				log.info("quoteResponseNode :"+quoteResponseNode);
				try {
					log.info("Waiting to generate Quote Doc");
					TimeUnit.SECONDS.sleep(PolicyRenewalConstatnt.SLEEP_SECOND);
					resultNode = dataProvider.getQuoteDoc(quoteResponseNode.get("QUOTE_ID").asText());
				} catch (InterruptedException e) {
					log.info("Waiting to generate Quote Doc failed",e);
					e.printStackTrace();
				}

				log.info("Printing resultNode :"+resultNode);
			}
		} catch (ClientProtocolException e) {
			log.info("Exception While Getting Quote Response  ",e);
			resultNode = null;
		} catch (IOException e) {
			log.info("Exception While Getting Quote Response  ",e);
			resultNode = null;
		}
		catch (NullPointerException e) {
			log.info("Exception While Getting Quote Response  ",e);
			resultNode = null;
		}
		return resultNode;
	} 

	public JsonNode createRenewalLead(JsonNode jsonObj)
	{
		log.info("In createRenewalLead");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String leadResponse = null;
		JsonNode leadResponseNode = null;
		HttpResponse response;
		try {
			JsonNode policyRenewalConfigNode = objectMapper.readTree(policyRenewalConfig);
			HttpPost httpPost = new HttpPost(policyRenewalConfigNode.get("masterService").asText());
			StringEntity entity = new StringEntity(jsonObj.toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if(policyRenewalConfigNode.has("Origin")){
				httpPost.setHeader("Origin", policyRenewalConfigNode.get("Origin").asText());
			}
			response = httpClient.execute(httpPost);
			log.info("Renewal Create Lead Status Code : "+response.getStatusLine().getStatusCode());
			leadResponse = EntityUtils.toString(response.getEntity());
			log.info("Response in JSON : "+ leadResponse.replace("\\\"", "\""));
			String kk = leadResponse.replace("\\\"", "\"");
			leadResponseNode = objectMapper.readTree(kk);
			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			log.info("Exception While Getting Quote Response  ",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.info("Exception While Getting Quote Response  ",e);
			e.printStackTrace();
		}
		return leadResponseNode;
	}

	public String getShortURL(String longURL){
		log.info("Request at renewal short url: " + longURL);
		String shortURL = null;
		JsonNode shortURLRequest = objectMapper.createObjectNode();
		((ObjectNode)shortURLRequest).put("longURL", longURL);
		try
		{
			JsonNode configNode = objectMapper.readTree(URLShortnerConfig);
			HttpClient httpClient = new DefaultHttpClient();;
			HttpPost httpPost = new HttpPost(configNode.get("internalServiceURL").asText());
			StringEntity entity = new StringEntity(shortURLRequest.toString());
			entity.setContentType("application/json");
			httpPost.setHeader("Origin", "http://icrm.policies365.com");
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			log.info("Short URL Response Code: " + response.getStatusLine().getStatusCode());
			String getRes = EntityUtils.toString(response.getEntity());
			log.info("Short URL Response Data: " + getRes);
			if ((response.getStatusLine().getStatusCode() == 200) && (getRes != null)) {
				
				shortURL = objectMapper.readTree(getRes).get("data").get("shortURL").asText();
			} else {
				shortURL = "Error";
			}
		}
		catch (UnsupportedOperationException e){
			shortURL = "Error";
			log.info("Error at renewal short url ", e);
		}
		catch (IOException e){
			shortURL = "Error";
			log.info("Error at renewal short url", e);
		}
		if(shortURL.equalsIgnoreCase("error") || shortURL == null){
			log.info("Got error in short link");
			shortURL = longURL;
		}
		return shortURL;
	}
	
	public static JsonNode runOfflineRenewal(JsonNode jsonObj)
	{
		log.info("Running manual offline renewal");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String leadResponse = null;
		JsonNode leadResponseNode = null;
		HttpResponse response;
		try {
			JsonNode policyRenewalConfigNode = objectMapper.readTree(policyRenewalConfig);
			HttpPost httpPost = new HttpPost(policyRenewalConfigNode.get("masterService").asText());
			StringEntity entity = new StringEntity(jsonObj.toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if(policyRenewalConfigNode.has("Origin")){
				httpPost.setHeader("Origin", policyRenewalConfigNode.get("Origin").asText());
			}
			response = httpClient.execute(httpPost);
			log.info("Renewal runOfflineRenewal Status Code : "+response.getStatusLine().getStatusCode());
			leadResponse = EntityUtils.toString(response.getEntity());
			log.info("Response of runOfflineRenewal in JSON : "+ leadResponse.replace("\\\"", "\""));
			String kk = leadResponse.replace("\\\"", "\"");
			leadResponseNode = objectMapper.readTree(kk);
			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			log.info("Exception While Getting Quote Response  ",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.info("Exception While Getting Quote Response  ",e);
			e.printStackTrace();
		}
		return leadResponseNode;
	}

}
