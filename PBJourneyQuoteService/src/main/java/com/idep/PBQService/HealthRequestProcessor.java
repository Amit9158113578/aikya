package com.idep.PBQService;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/**
 * @author shweta.joshi
 *
 */
public class HealthRequestProcessor implements Processor {

	
	Logger log = Logger.getLogger(HealthRequestProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null ;
	static JsonNode defaultQuoteParam =null;
	static JsonDocument healthReqConfig=null;
	
	static{
		if(serverConfig==null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			healthReqConfig = serverConfig.getDocBYId("HealthQuoteReqestConfig");
		}
		try {
			defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultHealthQuoteParam").content().toString());
		} catch (Exception e) {
			Logger.getLogger(HealthRequestProcessor.class.getName()).error("unable to fetch Deafult life quote param document : ",e);
		}		
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
				JsonNode PBQReqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
				exchange.setProperty("PBQuoteRequest", PBQReqNode);
				((ObjectNode)PBQReqNode).put("requestType", "PBQuoteRequest");
				if(!PBQReqNode.has("lob")){
					((ObjectNode)PBQReqNode).put("lob", "Health");
				}
				if(defaultQuoteParam!=null){
					((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
				}else{
					defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultHealthQuoteParam").content().toString());
					((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
				}
				if(healthReqConfig==null){
					healthReqConfig = serverConfig.getDocBYId("HealthQuoteReqestConfig");
				}
				
				if(healthReqConfig!=null){
				JsonNode configHealth = objectMapper.readTree(healthReqConfig.content().toString());
				int childCount=0;
				int adultCount=0;
				int totalCount=0;
				log.info("PBJ Health Quote Request : "+PBQReqNode);
				if(configHealth.has("defaultChildAge")){
					if(PBQReqNode.has("healthInfo")){
						if(PBQReqNode.get("healthInfo").has("selectedFamilyMembers")){
						
							ArrayNode selectedFamilyMember = (ArrayNode)PBQReqNode.get("healthInfo").get("selectedFamilyMembers");
							totalCount = selectedFamilyMember.size();
							if(totalCount>1){
								((ObjectNode)PBQReqNode.get("healthInfo")).put("planType","F");
							}else if(totalCount!=0 && totalCount==1) {
								((ObjectNode)PBQReqNode.get("healthInfo")).put("planType","I");
							}
							boolean childAgeFlag=false;
							for(JsonNode member : selectedFamilyMember ){
								
								if(member.get("age").asInt() > configHealth.get("defaultChildAge").asInt()){
									adultCount = adultCount+1;
								} else{
									childCount = childCount+1;
								}
								if(member.get("relationship").asText().equalsIgnoreCase("CH") && member.get("age").asInt() > configHealth.get("maxChildAgeValid").asInt()){
									childAgeFlag=true;
									break;
								}
							}
							if(childAgeFlag){
								exchange.getIn().setHeader("validFamillyMember", "inValid");
								((ObjectNode)PBQReqNode).put("validFamillyMember","inValid");
							}else{
								exchange.getIn().setHeader("validFamillyMember", "valid");
								((ObjectNode)PBQReqNode).put("validFamillyMember","valid");
							}
							
							((ObjectNode)PBQReqNode.get("healthInfo")).put("childCount",childCount);
							((ObjectNode)PBQReqNode.get("healthInfo")).put("adultCount",adultCount);
							((ObjectNode)PBQReqNode.get("healthInfo")).put("totalCount",totalCount);
						}
					}
				}
				
					log.info("Configuratiion Field Added in Request : "+PBQReqNode);
					
					
				}else{
					log.error("unable to load CB document : HealthQuoteReqestConfig");
				}
				
				
				
				exchange.setProperty("carrierReqMapConf", "PBQuoteRequest-health");
		    	exchange.getIn().setBody(objectMapper.writeValueAsString(PBQReqNode));
		}catch(Exception e){
			log.error("unable to process Request : ",e);
		}
	}
}
