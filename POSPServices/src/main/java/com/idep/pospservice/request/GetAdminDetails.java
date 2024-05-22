package com.idep.pospservice.request;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetAdminDetails implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	Logger log = Logger.getLogger(GetAdminDetails.class.getName()); 
	JsonNode errorNode;
	static JsonNode queryConfig;
	static {
		try {
			queryConfig = objectMapper.readTree(PospData.getDocBYId("POSPQueryServerConfig").content().toString());
		} catch (Exception e) {
			Logger.getLogger(GetAdminDetails.class.getName()).error("Unabele to cache POSPQueryServerConfig : ",e);
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			List<Map<String, Object>> list  = null;
			/*if(requestNode.has("documentType")){
				if(requestNode.findValue("documentType").textValue().equalsIgnoreCase("adminDetails")){

					query =  "select PospData.* from PospData  where documentType = 'pospAdminProfile'";
				}
			}
			if(){
				query =  "select PospData.* from PospData  where documentType = 'pospAdminProfile'and adminId="+requestNode.findValue("adminId").toString();
			}
*/
			if (requestNode.has("adminId") && requestNode.findValue("documentType").textValue().equalsIgnoreCase("adminDetails")){
				String query = queryConfig.get("adminDetails").get("paramQuery").asText();
				JsonArray paramobj = JsonArray.create();
				paramobj.add(requestNode.get("adminId").asText());
				log.info("Query Admin data : "+query);
				log.info("Query Admin data param Obj : "+paramobj);
				JsonNode groupConfig = objectMapper.readTree(PospData.executeConfigParamArrQuery(query,paramobj).toString());
				log.info("Admin App Config Found : "+groupConfig);
				if(groupConfig!=null){
					ObjectNode resNode = objectMapper.createObjectNode();
						resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						resNode.put(POSPServiceConstant.RES_DATA,groupConfig);
						exchange.getIn().setBody(resNode);
					}else
					{
						log.error("User Profile not found : "+requestNode);
						ObjectNode resNode = objectMapper.createObjectNode();
						resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_CODE).asInt());
						resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_MSG).asText());
						resNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(resNode);
						
					throw new ExecutionTerminator();	
					}
			}else{
				String query = queryConfig.get("adminDetails").get("Query").asText();
				list = PospData.executeQueryCouchDB(query);
			
				this.log.info("list : " + list);
				if (list.isEmpty()) {
					ObjectNode objectNode = objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NO_RECORDS_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NORECORD_MESSAGES).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
				}
				else
				{
					String agentList = objectMapper.writeValueAsString(list);
					JsonNode agentDetails = objectMapper.readTree(agentList);
					ObjectNode objectNode = objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,agentDetails);
					exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
				}
			}

			
		}
		catch(Exception e){
			log.error("Unable to process : ",e);
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();

		}
			
		
	}

}
