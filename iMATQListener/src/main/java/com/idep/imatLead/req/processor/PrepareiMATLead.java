package com.idep.imatLead.req.processor;


import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.sun.jersey.core.util.Base64;

//public class PrepareiMATLeadRequest implements Processor{
public class PrepareiMATLead{

	static Logger log = Logger.getLogger(PrepareiMATLead.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	iMATLeadRequest leadreq = new iMATLeadRequest();
	static JsonNode authNode = null;
	static
	{
		if (serverConfig != null)
		{
			try
			{
				authNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATConfiguration").content()).toString());
				log.info("iMAT authNode"+authNode);
			}
			catch (Exception e)
			{
				log.error("iMATConfiguration Document Not Found", e);
				e.printStackTrace();
			}
		}
	}


	/*public static void main(String[] args) throws  IOException {
		HttpPost request = new HttpPost("http://imat.infintus.com/api/contacts/new");
		String auth = "admin" + ":" + "imatdev@123";
		byte[] encodedAuth = Base64.encode(auth.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		log.info("123 authHeader"+authHeader);
		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

		ObjectNode dataNode = objectMapper.createObjectNode();
		dataNode.put("lastname", "gb");
		dataNode.put("email", "hgvg@fc.nvn");
		//String JSON_STRING = "{\"firstname\": \"Gauri1\",\"lastname\": \"2Bhalerao\",\"email\": \"Gauri1.bhalerao@infintus.com\",\"mobile\": \"9879778373\"}";
		StringEntity params = new StringEntity(dataNode.toString(),"application/json","UTF-8");
		request.setEntity(params);
		log.info("request"+request.getConfig());
		HttpClient client = HttpClientBuilder.create().build();	
		HttpResponse response = client.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();	
		log.info("123 response..."+response.toString());
	}
	 */
	public String prepareLead(JsonNode reqNode) throws Exception {
		// TODO Auto-generated method stub
		try{
			log.info("Request got in iMAT prepare LEAD Processsor"+reqNode);

			log.info("request node iMAT : " + reqNode);
			String resDataNode = leadreq.prepareLeadRequest(reqNode);
			log.info("Responsee.."+resDataNode);
			createContact(resDataNode);
		}
		catch (Exception e)
		{
			log.error("Exception : ", e);
			log.error("ERROR in iMAT Preapre Lead Processor");
		}
		return null;
	}

	public String createContact(String resDataNode) throws ClientProtocolException, IOException{
		HttpResponse response = null;
		try{
			HttpPost request = new HttpPost(authNode.get("Config").get("url").asText());
			String auth = authNode.get("Config").findValue("username").asText() + ":" + authNode.get("Config").findValue("password").asText();
			byte[] encodedAuth = Base64.encode(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			log.info("123 authHeader"+authHeader);
			request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
			request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

			StringEntity params = new StringEntity(resDataNode,"application/json","UTF-8");
			request.setEntity(params);
			log.info("request"+request);
			HttpClient client = HttpClientBuilder.create().build();	
			response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();	
			log.info("123 response..."+response.toString());
			log.info("123 Status Code..."+statusCode);
			return response.toString() ; 		
		}

		catch(Exception e){
			log.error("Eroor in createContact method",e);
		}
		return response.toString();
	}
}
