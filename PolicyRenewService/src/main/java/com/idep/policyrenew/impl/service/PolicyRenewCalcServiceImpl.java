package com.idep.policyrenew.impl.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policyrenewprocessor.PolicyRenewDataProcessor;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyRenewCalcServiceImpl 
{
	PolicyRenewDataProcessor dataProvider = new PolicyRenewDataProcessor();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static Logger log = Logger.getLogger(PolicyRenewCalcServiceImpl.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static String policyRenewalConfig = serverConfig.getDocBYId(PolicyRenewConstatnt.POLICY_RENEWAL_CONFIG).content().toString();
	//String URLShortnerConfig = serverConfig.getDocBYId(PolicyRenewConstatnt.URL_SHORTNER_CONFIG).content().toString();
	public String getRenewProposalData(String proposalId) throws JsonProcessingException, IOException
	{
		log.info("Recieved Policy Renew Proposal Id : " + proposalId);
		return proposalId;
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
			//log.info("Policy Renewal Response Status Code : "+response.getStatusLine().getStatusCode());
			quoteResponse = EntityUtils.toString(response.getEntity());
			//log.info("Response in JSON : "+ quoteResponse.replace("\\\"", "\""));
			String kk = quoteResponse.replace("\\\"", "\"");
			quoteResponseNode = objectMapper.readTree(kk.substring(1, kk.length()-1));
			httpClient.getConnectionManager().shutdown();
			if (quoteResponseNode != null && quoteResponseNode.has("responseCode") && quoteResponseNode.get("responseCode").asInt() != 1002 ){
				//log.info("quoteResponseNode :"+quoteResponseNode);
				try {
					log.info("Waiting to generate Quote Doc");
					TimeUnit.SECONDS.sleep(PolicyRenewConstatnt.SLEEP_SECOND);
					resultNode = dataProvider.getQuoteDoc(quoteResponseNode.get("QUOTE_ID").asText());
				} catch (InterruptedException e) {
					log.info("Waiting to generate Quote Doc failed",e);
					e.printStackTrace();
				}
				//log.info("Printing resultNode :"+resultNode);
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

}
