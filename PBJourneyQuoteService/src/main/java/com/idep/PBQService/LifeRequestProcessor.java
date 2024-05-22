/**
 * 
 */
package com.idep.PBQService;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/**
 * @author pravin.jakhi
 *
 */
public class LifeRequestProcessor implements Processor {

	
	Logger log = Logger.getLogger(LifeRequestProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null ;
	static JsonNode defaultQuoteParam =null;
	static JsonNode reqParamConfig=null;
	
	static{
		if(serverConfig==null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
		}
		try {
			defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultLifeQuoteParam").content().toString());
			reqParamConfig = objectMapper.readTree(serverConfig.getDocBYId("RecommendReqParamConfig").content().toString());
		} catch (Exception e) {
			Logger.getLogger(LifeRequestProcessor.class.getName()).error("unable to fetch Deafult life quote param document : ",e);
		}		
	}
	
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
				JsonNode PBQReqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
				exchange.setProperty("PBQuoteRequest", PBQReqNode);
				((ObjectNode)PBQReqNode).put("requestType", "PBQuoteRequest");
				if(!PBQReqNode.has("lob")){
					((ObjectNode)PBQReqNode).put("lob", "Life");
				}
				if(defaultQuoteParam!=null){
					((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
				}else{
					defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultLifeQuoteParam").content().toString());
					((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
				}
				
				if(PBQReqNode.has("commonInfo")){
					if(PBQReqNode.get("commonInfo").has("age")){
						if(reqParamConfig!=null){
							
							if(reqParamConfig.has("lifeDefaultMaturityAge")){
								int policyTerm  = 30;
								if(reqParamConfig.get("lifeDefaultMaturityAge").asInt() > PBQReqNode.get("commonInfo").get("age").asInt()){
									policyTerm=(reqParamConfig.get("lifeDefaultMaturityAge").asInt()-PBQReqNode.get("commonInfo").get("age").asInt());
								}
								((ObjectNode)PBQReqNode.get("commonInfo")).put("policyTerm", policyTerm);
								
								int maturityAge  = reqParamConfig.get("lifeDefaultMaturityAge").asInt();
								
								/**
								if age less than 30 then it's age+maturityAgeLimit = maturityAge  eg. 28+40 = 68(maturityAge)
								
								***/
								if(PBQReqNode.get("commonInfo").get("age").asInt() < reqParamConfig.get("validateAge").asInt()){
									maturityAge = PBQReqNode.get("commonInfo").get("age").asInt() + reqParamConfig.get("maturityAgeLimit").asInt();
								}
								
								((ObjectNode)PBQReqNode.get("commonInfo")).put("maturityAge", maturityAge);
							}
						}else{
							log.error("unable to load document : RecommendReqParamConfig");
						}
					}
				}
				exchange.getIn().setBody(PBQReqNode);
		}catch(Exception e){
			log.error("unable to process PBJ Life Request : ",e);
		}
	}
}
