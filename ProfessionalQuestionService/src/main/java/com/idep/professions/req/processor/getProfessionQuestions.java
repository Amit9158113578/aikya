/**
 * 
 */
package com.idep.professions.req.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.professions.exception.ExecutionTerminator;

/**
 * @author pravin.jakhi
 *
 */
public class getProfessionQuestions implements Processor {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(getProfessionQuestions.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static ObjectNode profssionQuesList = objectMapper.createObjectNode();
	static ArrayNode professionList = null;
	static{

		try {
			professionList= (ArrayNode)objectMapper.readTree(serverConfig.getDocBYId("ProfessionsList").content().get("Professions").toString());
		JsonNode configDoc =  objectMapper.readTree(serverConfig.getDocBYId("PBQueryConfig").content().toString());
		
		
			log.info("Profession Question Cache Process Started.........");
		
		for(JsonNode profession : professionList){
			if(profession.has("professionCode")){
				String query=configDoc.get("professionQuery").asText().replaceAll("<professionCode>", profession.get("professionCode").toString());
				System.out.println(" QUERY : "+query);
				List<Map<String, Object>> questionList = serverConfig.executeQuery(query);
		
					JsonNode questionListNode = objectMapper.readTree(objectMapper.writeValueAsString(questionList));
					profssionQuesList.put(profession.get("professionCode").asText(), questionListNode);
			}
		}
		Thread.sleep(10000);
		log.info("Profession Question Cache Process Completed.........");
		
		} catch (Exception e) {
			log.error("unable to cache Prfesson Question : ",e);
			new ExecutionTerminator();
		}
	}
	
	
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			List<JsonObject> questionList=null;
			JsonNode questions =null;
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode configDoc =  objectMapper.readTree(serverConfig.getDocBYId("PBQueryConfig").content().toString());
			/*if(request.has("professionCode")){
				if(configDoc.has("professionQuery")){
				String query=configDoc.get("professionQuery").asText();
				
				JsonArray paramobj = JsonArray.create();
				if(request.has("professionCode")){
				paramobj.add(request.get("professionCode").asText());
				}
				log.info("Query for get question : "+query);
				log.info("Query for get question param : "+paramobj);
				questionList= serverConfig.executeConfigParamArrQuery(query,paramobj);
				questions = objectMapper.readTree(questionList.toString());
				}
				log.info("Question found for "+request.get("professionName").asText()+"Profession : "+questionList);
			}
			*/
			if(request.has("professionCode")){
				if(profssionQuesList.has(request.get("professionCode").asText())){
					exchange.getIn().setBody(profssionQuesList.get(request.get("professionCode").asText()));
				}
			}else if(request.has("professionName")){
				
				for(JsonNode profession : professionList){
					
					
					if(profession.get("professionName").asText().equalsIgnoreCase(request.get("professionName").asText())){
						String professionCode = profession.get("professionCode").asText();
						if(profssionQuesList.has(professionCode)){
							exchange.getIn().setBody(profssionQuesList.get(professionCode));
						}
						break;
					}
				}
			}
			
		}catch(Exception e){
			log.error("unbale to fetch professiona question : ",e);
			new ExecutionTerminator();
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println("Executed : "+profssionQuesList);
	}

}
