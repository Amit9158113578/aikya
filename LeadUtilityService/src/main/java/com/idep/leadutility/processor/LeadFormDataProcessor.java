package com.idep.leadutility.processor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.leadutility.util.ICRMDBConnection;

public class LeadFormDataProcessor implements Processor{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(LeadFormDataProcessor.class.getName());
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonNode configNode = null;
	static {
		if(configNode == null){
			try {
				configNode = objectMapper.readTree(serverConfig.getDocBYId("LeadUtilityConfig").content().toString());
			} catch (IOException e) {
				log.error("error at loading LeadUtilityConfig"+e);
				e.printStackTrace();
			} 
		}
	}

	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("LeadFormDataProcessor req: "+reqNode);
		String campaignListQuery = null;
		ResultSet resultSet = null;
		ObjectNode responseNode = objectMapper.createObjectNode();
		JsonNode queryConfig = configNode.get("queryConfig");
		Connection con = null;
		if(queryConfig.has("campaignListConfig")){
			ArrayNode campaignListNode = objectMapper.createArrayNode(); 
			try {
				ICRMDBConnection dbConnect = new ICRMDBConnection();
				con = dbConnect.getCRMDBConnection();
				campaignListQuery = queryConfig.get("campaignListConfig").get("query").asText();
				PreparedStatement preparedStatement = con.prepareStatement(campaignListQuery);
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next()){
					ObjectNode campaignNode = objectMapper.createObjectNode();
					for (JsonNode jsonNode : queryConfig.get("campaignListConfig").get("selectFields")) {
						campaignNode.put(jsonNode.get("destination").asText(),resultSet.getString(jsonNode.get("source").asText()));
					}
					campaignListNode.add(campaignNode);
				}
				responseNode.put("campaignDetails", campaignListNode);
				log.info("response node :"+responseNode);
				con.close();
			} catch (Exception e) {
				log.error("Exception at getting lead utility form data",e);
				responseNode.put("error", true);
				e.printStackTrace();
			}finally{
				if(con !=null){
					con.close();
				}
			}
		}
		exchange.getIn().setBody(responseNode.toString());
	}
	
	public JsonNode createLead(JsonNode jsonObj)
	{
		log.info("In upload lead creation");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String leadResponse = null;
		JsonNode leadResponseNode = null;
		HttpResponse response;
		try {
			HttpPost httpPost = new HttpPost(configNode.get("leadService").asText());
			StringEntity entity = new StringEntity(jsonObj.toString());
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			if(configNode.has("Origin")){
				httpPost.setHeader("Origin", configNode.get("Origin").asText());
			}
			response = httpClient.execute(httpPost);
			log.info("Renewal Create Lead Status Code : "+response.getStatusLine().getStatusCode());
			leadResponse = EntityUtils.toString(response.getEntity());
			log.info("Response in JSON : "+ leadResponse.replace("\\\"", "\""));
			String kk = leadResponse.replace("\\\"", "\"");
			leadResponseNode = objectMapper.readTree(kk.substring(1, kk.length()-1));
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
