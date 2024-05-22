package com.idep.polr.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.urlshortner.util.URLShortnerConstant;

public class PolrService {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(PolrService.class.getName());
	static CBService serverConfig = null;
	static JsonNode shortURLConfig = null;

	static{
		if (serverConfig == null)
		{
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			try{
				shortURLConfig = objectMapper.readTree(serverConfig.getDocBYId(URLShortnerConstant.SHORT_URL_CONFIG).content().toString());
			}catch(Exception e){
				log.error("Error while fetching ShortURLConfig DOC",e);
				e.printStackTrace();
			}
		}
	}

	public JsonNode getShortURL(JsonNode request){
		log.info("Request at getShortURL: "+request);
		ObjectNode resNode = objectMapper.createObjectNode();
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(shortURLConfig.get("p365ShortnerURL").asText()
					+shortURLConfig.get("key").asText());
			httpPost.setHeader("Accept", "Appication/json");
			httpPost.setHeader("Content-Type", "Appication/json");
			httpPost.setEntity(new StringEntity(request.toString()));
			HttpResponse response = httpClient.execute(httpPost);
			log.info("Short URL Response Code: "+response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String getRes = EntityUtils.toString(entity);
			log.info("Short URL Response Data: "+getRes);
			if(response.getStatusLine().getStatusCode()== 200 && getRes != null){
				resNode.put(URLShortnerConstant.SHORT_URL,getRes);
			}else{
				resNode.put(URLShortnerConstant.SHORT_URL,URLShortnerConstant.ERROR);
			}
		} catch (UnsupportedEncodingException e) {
			resNode.put(URLShortnerConstant.SHORT_URL,URLShortnerConstant.ERROR);
			log.error("Error at PolrService ",e);
		}catch (UnsupportedOperationException e) {
			resNode.put(URLShortnerConstant.SHORT_URL,URLShortnerConstant.ERROR);
			log.error("Error at PolrService ",e);
		} catch (IOException e) {
			resNode.put(URLShortnerConstant.SHORT_URL,URLShortnerConstant.ERROR);
			log.error("Error at PolrService ",e);
		}

		return resNode;
	}

}
