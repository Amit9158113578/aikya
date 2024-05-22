package com.idep.customer.service;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.customer.util.CustomerConstants;

public class CrossSellLeadService {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CrossSellLeadService.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	String crossSellConfig = serverConfig.getDocBYId(CustomerConstants.CROSS_SELL_CONFIG).content().toString();

	public JsonNode createCrossSellLead(JsonNode jsonObj)
	{
		log.info("In createCrossSellLead");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String leadResponse = null;
		JsonNode leadResponseNode = null;
		HttpResponse response;
		try {
			JsonNode policyRenewalConfigNode = objectMapper.readTree(crossSellConfig);
			HttpPost httpPost = new HttpPost(policyRenewalConfigNode.get("serviceURL").get("masterService").asText());
			StringEntity entity = new StringEntity(jsonObj.toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if(policyRenewalConfigNode.get("serviceURL").has("Origin")){
				httpPost.setHeader("Origin", policyRenewalConfigNode.get("serviceURL").get("Origin").asText());
			}
			response = httpClient.execute(httpPost);
			log.info("Renewal Create Lead Status Code : "+response.getStatusLine().getStatusCode());
			leadResponse = EntityUtils.toString(response.getEntity());
			log.info("Response in JSON : "+ leadResponse.replace("\\\"", "\""));
			String resp = leadResponse.replace("\\\"", "\"");
			leadResponseNode = objectMapper.readTree(resp.substring(1, resp.length()-1));
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
