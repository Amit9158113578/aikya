package com.idep.pospservice.userLogout;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.POSPServiceConstant;

public class LogoutUserProcessor implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(LogoutUserProcessor.class.getName());
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	SimpleDateFormat SysFormat = new SimpleDateFormat("dd-MM-yyyy");
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	JsonNode errorNode;
	static JsonNode queryConfig;
	static {
		try {
			queryConfig = objectMapper.readTree(pospData.getDocBYId("POSPQueryServerConfig").content().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			errorNode = objectMapper.createObjectNode();
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			ObjectNode  logoutRes =  objectMapper.createObjectNode();
			String Authorization="";
			if(exchange.getIn().getHeader("Authorization")!=null){
				Authorization = exchange.getIn().getHeader("Authorization").toString();
				log.info("Authorization found in headers : "+Authorization);
			
				if(reqNode.has("mobileNumber")){
					String query = queryConfig.get("findPospUserToken").asText();
					JsonArray paramobj = JsonArray.create();
					paramobj.add(Authorization); // Authorization taken from headers 
					paramobj.add(reqNode.get("mobileNumber").asText());
					JsonNode tokenDoc = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
					log.info("User App Config Found : "+tokenDoc);
					if(tokenDoc!=null){
					ObjectNode logOut = objectMapper.createObjectNode();
						JsonNode tokenDetails = tokenDoc.get(0);
						String todayate = SysFormat.format(new Date());
						if(todayate.equals(tokenDetails.get("validTill").asText())){
							
							if(tokenDetails.get("isExpired").asText().equalsIgnoreCase("N")){
								logOut.put("logoutStatus", "success");
								
								((ObjectNode)tokenDetails).put("isExpired", "Y");
								((ObjectNode)tokenDetails).put("lastUpdated", dateFormat.format(new Date()));
								
								String status = pospData.replaceDocument(tokenDetails.get("id").asText(), JsonObject.fromJson(objectMapper.writeValueAsString(tokenDetails)));
								log.info("Token Document Updated : "+tokenDetails.get("id").asText()+" "+status);
								logoutRes.put(POSPServiceConstant.RES_CODE, POSPServiceConstant.RES_CODE_SUCCESS);
								logoutRes.put(POSPServiceConstant.RES_MSG, POSPServiceConstant.RES_MSG_SUCCESS);
								logoutRes.put(POSPServiceConstant.RES_DATA,logOut );
							}else{
								log.error("User Token Already Expired : "+reqNode.get("mobileNumber").asText());
								((ObjectNode)tokenDetails).put("isExpired", "Y");
								((ObjectNode)tokenDetails).put("lastUpdated", dateFormat.format(new Date()));
								
								String status = pospData.replaceDocument(tokenDetails.get("id").asText(), JsonObject.fromJson(objectMapper.writeValueAsString(tokenDetails)));
								((ObjectNode)errorNode).put("logoutStatus", "success");
								logoutRes.put(POSPServiceConstant.RES_CODE, POSPServiceConstant.RES_CODE_SUCCESS);
								logoutRes.put(POSPServiceConstant.RES_MSG, POSPServiceConstant.RES_MSG_SUCCESS);
								logoutRes.put(POSPServiceConstant.RES_DATA,logOut );
							}
							
						}else{
							log.error("User Token Already Expired : "+reqNode.get("mobileNumber").asText());
							((ObjectNode)tokenDetails).put("isExpired", "Y");
							((ObjectNode)tokenDetails).put("lastUpdated", dateFormat.format(new Date()));
							
							String status = pospData.replaceDocument(tokenDetails.get("id").asText(), JsonObject.fromJson(objectMapper.writeValueAsString(tokenDetails)));
							((ObjectNode)errorNode).put("logoutStatus", "success");
							logoutRes.put(POSPServiceConstant.RES_CODE, POSPServiceConstant.RES_CODE_SUCCESS);
							logoutRes.put(POSPServiceConstant.RES_MSG, POSPServiceConstant.RES_MSG_SUCCESS);
							logoutRes.put(POSPServiceConstant.RES_DATA,logOut );
						}
					}else{
						log.error("User Token not found  : "+reqNode.get("mobileNumber").asText());
						((ObjectNode)errorNode).put("logoutStatus", "failure");
						logoutRes.put(POSPServiceConstant.RES_CODE, 1002);
						logoutRes.put(POSPServiceConstant.RES_MSG, "failure");
						logoutRes.put(POSPServiceConstant.RES_DATA,errorNode );
					}					
				}else{
					log.error("unable to logout user mobileNumber not found in request ");
					((ObjectNode)errorNode).put("logoutStatus", "failure");
					logoutRes.put(POSPServiceConstant.RES_CODE, 1002);
					logoutRes.put(POSPServiceConstant.RES_MSG, "failure");
					logoutRes.put(POSPServiceConstant.RES_DATA,errorNode );
				}
			
			
			
			}else{
				log.error("unable to logout user token not found in Headers ");
				((ObjectNode)errorNode).put("logoutStatus", "failure");
				logoutRes.put(POSPServiceConstant.RES_CODE, 1002);
				logoutRes.put(POSPServiceConstant.RES_MSG, "failure");
				logoutRes.put(POSPServiceConstant.RES_DATA,errorNode );
			}
				
			
			
			exchange.getIn().setBody(logoutRes);
		}catch(Exception e){
			log.error("unable to send logout user respone : ",e);
		}
		
	}
}

