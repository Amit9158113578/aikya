package com.idep.pospservice.user.service;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetUserAppConfig implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetUserAppConfig.class.getName());
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	static JsonNode queryConfig;
	static JsonNode error;
	JsonNode errorNode;
	static {
		try {
			queryConfig = objectMapper.readTree(pospData.getDocBYId("POSPQueryServerConfig").content().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
	try{
		JsonNode userAppConfig = null;
		List<Map<String, Object>> list  = null;
		JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
		
		if(reqNode.has("mobileNumber") && reqNode.has("requestSource")   ){
			if(reqNode.get("requestSource").asText().equalsIgnoreCase("pospAgentPortal") ){
			String docId = POSPServiceConstant.POSP_USER_PROFILE+reqNode.get("mobileNumber").asText();
			log.info("POSP User Doc Id:"+docId);
			JsonDocument userProfile = pospData.getDocBYId(docId);
			if(userProfile!=null){
				userAppConfig = getGroupConfig(objectMapper.readTree(userProfile.content().toString()));
				if(userAppConfig.size()== 0){
					log.error("User Profile not found : "+reqNode);
					ObjectNode resNode = objectMapper.createObjectNode();
					resNode.put(POSPServiceConstant.RES_CODE,1001);
					resNode.put(POSPServiceConstant.RES_MSG ,"failure");
					resNode.put(POSPServiceConstant.RES_DATA,error);
					exchange.getIn().setBody(resNode);
				throw new ExecutionTerminator();	
				}	
			}
			}//End of requestSource 
		}else if (reqNode.has("agentId")){
			
			String query = queryConfig.get("getAgentProfile").asText();
			
			JsonArray paramobj = JsonArray.create();
			paramobj.add(reqNode.get("agentId").asText());
			JsonNode profileConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
			log.info("User App Config Found : "+profileConfig);
			if(profileConfig!=null){
				userAppConfig = getGroupConfig(profileConfig.get(0));
				if(userAppConfig.size()== 0){
					log.error("User Profile not found : "+reqNode);
					ObjectNode resNode = objectMapper.createObjectNode();
					resNode.put(POSPServiceConstant.RES_CODE,1001);
					resNode.put(POSPServiceConstant.RES_MSG ,"failure");
					resNode.put(POSPServiceConstant.RES_DATA,error);
					exchange.getIn().setBody(resNode);
				throw new ExecutionTerminator();	
				}				
			}else{
				log.error("unable to get User Group for : "+reqNode.get("agentId"));
			}
		}else if (reqNode.has("adminId")){
			String query = queryConfig.get("adminDetails").get("paramQuery").asText();
			
			JsonArray paramobj = JsonArray.create();
			paramobj.add(reqNode.get("adminId").asText());
			log.info("Query Admin data : "+query);
			log.info("Query Admin data param Obj : "+paramobj);
			JsonNode profileConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(query,paramobj).toString());
			log.info("Admin App Config Found : "+profileConfig);
			if(profileConfig!=null){
					userAppConfig = getGroupConfig(profileConfig.get(0));
					if(userAppConfig.size()== 0){
						log.error("User Profile not found : "+reqNode);
						ObjectNode resNode = objectMapper.createObjectNode();
						resNode.put(POSPServiceConstant.RES_CODE,1001);
						resNode.put(POSPServiceConstant.RES_MSG ,"failure");
						resNode.put(POSPServiceConstant.RES_DATA,error);
						exchange.getIn().setBody(resNode);
						
					throw new ExecutionTerminator();	
					}	
				}
		}else {
			log.error("unable to get user Group and configurration : "+reqNode);
		}
		
		ObjectNode resNode = objectMapper.createObjectNode();
		resNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.RES_CODE_SUCCESS);
		resNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.RES_MSG_SUCCESS);
		resNode.put(POSPServiceConstant.RES_DATA,userAppConfig);
		
		exchange.getIn().setBody(resNode);
		
	}catch(Exception e){
		log.error("unable to send user group config : ",e);
		ObjectNode resNode = objectMapper.createObjectNode();
		resNode.put(POSPServiceConstant.RES_CODE,1001);
		resNode.put(POSPServiceConstant.RES_MSG ,"failure");
		resNode.put(POSPServiceConstant.RES_DATA,error);
		exchange.getIn().setBody(resNode);
		throw new ExecutionTerminator();
	}
	
	

	}
	
	
	public static JsonNode getGroupConfig(JsonNode userProfileNode){
		JsonNode responseNode = objectMapper.createObjectNode();
		try{ 
		
		if(userProfileNode!=null){
			if(userProfileNode.has("userRole")){
				log.info("getGroupConfig input Node : "+userProfileNode);
				if(userProfileNode.get("userRole").asText().length() > 0 ){
					((ObjectNode)responseNode).put("userRole",userProfileNode.get("userRole").asText() );
					JsonArray paramobj = JsonArray.create();
					paramobj.add(userProfileNode.get("userRole").asText());
					JsonNode screenConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(queryConfig.get("GetRoleDoc").get("query").asText(),paramobj).toString());
					log.info("User Group Role Query output Size : "+screenConfig.size());
					if(screenConfig!=null){
						if(screenConfig.size() > 1){
							
							ArrayNode screenConfigResNode  = (ArrayNode) screenConfig.get(0).get(POSPServiceConstant.POSPMENUCONFIG);
							
							for(int i=1 ;i < screenConfig.size(); i++ ){
								
								ArrayNode itrArraynode = (ArrayNode)screenConfig.get(i).get(POSPServiceConstant.POSPMENUCONFIG);
								for(JsonNode innerMenu : itrArraynode){
									for(JsonNode outerMenu : screenConfigResNode){
										if(outerMenu.get(POSPServiceConstant.MENUID).asText().equalsIgnoreCase(innerMenu.get(POSPServiceConstant.MENUID).asText())){
											if(innerMenu.get(POSPServiceConstant.ISACTIVE).asBoolean()){
												((ObjectNode)outerMenu).put(POSPServiceConstant.ISACTIVE, true);
											}
											if(innerMenu.get(POSPServiceConstant.READ).asBoolean()){
												((ObjectNode)outerMenu).put(POSPServiceConstant.READ, true);
											}
											if(innerMenu.get(POSPServiceConstant.WRITE).asBoolean()){
												((ObjectNode)outerMenu).put(POSPServiceConstant.WRITE, true);
											}
											if(innerMenu.get(POSPServiceConstant.DELETE).asBoolean()){
												((ObjectNode)outerMenu).put(POSPServiceConstant.DELETE, true);
											}
											if(outerMenu.has(POSPServiceConstant.SUBMENU)){
												ArrayNode outerSubmenu = (ArrayNode)outerMenu.get(POSPServiceConstant.SUBMENU);
												for(JsonNode menuOuterSubmenu : outerSubmenu){
													if(innerMenu.has(POSPServiceConstant.SUBMENU)){
														for(JsonNode innerSubmenu : innerMenu.get(POSPServiceConstant.SUBMENU)){
															if(innerSubmenu.get(POSPServiceConstant.SUBMENUID).asText().equalsIgnoreCase(menuOuterSubmenu.get(POSPServiceConstant.SUBMENUID).asText())){
																if(innerSubmenu.get(POSPServiceConstant.ISACTIVE).asBoolean()){
																	((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.ISACTIVE, true);
																}
																if(innerSubmenu.get(POSPServiceConstant.READ).asBoolean()){
																	((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.READ, true);
																}
																if(innerSubmenu.get(POSPServiceConstant.WRITE).asBoolean()){
																	((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.WRITE, true);
																}
																if(innerSubmenu.get(POSPServiceConstant.DELETE).asBoolean()){
																	((ObjectNode)menuOuterSubmenu).put(POSPServiceConstant.DELETE, true);
																}
																break;
															}
														}
													}// innerMenu if condition END
												}
											}// outerMenu if condition END
										}
									}// outerMenu iteration for END
								}	
							 }//ScreenConfig response iteration for end 
								((ObjectNode)responseNode).put(POSPServiceConstant.POSPMENUCONFIG,screenConfigResNode);
							}else if(screenConfig.size() ==1 ){
								if(screenConfig.get(0).has(POSPServiceConstant.POSPMENUCONFIG)){
									((ObjectNode)responseNode).put("pospMenuConfig",screenConfig.get(0).get(POSPServiceConstant.POSPMENUCONFIG));
								}
							}else{
								log.error("Group Query response not found user : "+userProfileNode);
							}
					}
						log.info("Group Configuration added into response : "+responseNode);
						return responseNode;
					}
				 }
			 }else{
				 log.error("userProfile not in DB  : "+userProfileNode);
			 }
		}catch(Exception e){
			log.error("unable to find UserApp Config : ",e);
		}
		return responseNode;

	}

}
