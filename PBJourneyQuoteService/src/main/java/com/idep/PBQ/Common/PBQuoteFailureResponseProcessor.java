package com.idep.PBQ.Common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.PBQ.util.PBJourneyQuoteConstant;



/***
 * 
 * @author kuldeep.patil
 *
 */
public class PBQuoteFailureResponseProcessor implements Processor {

	static CBService serverConfig =CBInstanceProvider.getServerConfigInstance();
	JsonNode lobRequestNode;
	ObjectMapper objectMapper=new ObjectMapper();
	Logger log=Logger.getLogger(PBQuoteFailureResponseProcessor.class);
	static JsonDocument healthReqConfig=null;
	static{
		
		healthReqConfig = serverConfig.getDocBYId("HealthQuoteReqestConfig");
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String request = exchange.getIn().getBody().toString();
			String pbquoterequeststr = exchange.getProperty("PBQuoteRequest").toString();
			JsonNode pbquoterequestnode = objectMapper.readTree(pbquoterequeststr);
			JsonNode requestNode = objectMapper.readTree(request);
			JsonNode errorNode=null;
			if(healthReqConfig==null){
				healthReqConfig = serverConfig.getDocBYId("HealthQuoteReqestConfig");
			}
			JsonNode ConfigDOC = objectMapper.readTree(healthReqConfig.content().toString());
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			if(requestNode.has("validFamillyMember")){
				if(requestNode.get("validFamillyMember").asText().equalsIgnoreCase("inValid")){
					    objectNode.put(PBJourneyQuoteConstant.RES_CODE_TXT, 1010);
					    objectNode.put(PBJourneyQuoteConstant.RES_MSG_TXT, "failure");
					    if(ConfigDOC.has("invalidFamilyMemberMsg")){
					    	log.info("FOR INVALID Family Member sending message");
					    	String msg = ConfigDOC.get("invalidFamilyMemberMsg").asText();
					    	log.info("message FOE INVALID Family Member : "+msg);
					    	JsonNode data = objectMapper.createObjectNode();
					    	((ObjectNode)data).put("msg",msg );
					        objectNode.put(PBJourneyQuoteConstant.RES_DATA_TXT, data);
					    }else{
					    objectNode.put(PBJourneyQuoteConstant.RES_DATA_TXT, errorNode);
					    }
					   
				}else{
					
				    objectNode.put(PBJourneyQuoteConstant.RES_CODE_TXT, 1010);
				    objectNode.put(PBJourneyQuoteConstant.RES_MSG_TXT, "failure");
				    objectNode.put(PBJourneyQuoteConstant.RES_DATA_TXT, errorNode);
				    exchange.getIn().setBody(objectNode);
				}
			}else{
				
			    objectNode.put(PBJourneyQuoteConstant.RES_CODE_TXT, 1010);
			    objectNode.put(PBJourneyQuoteConstant.RES_MSG_TXT, "failure");
			    objectNode.put(PBJourneyQuoteConstant.RES_DATA_TXT, errorNode);
			    exchange.getIn().setBody(objectNode);
			}
			log.info("Familure response code mjsmm messageId : "+pbquoterequestnode.get("ProfmessageId").asText());
			 exchange.getIn().setHeader("JMSCorrelationID", pbquoterequestnode.get("ProfmessageId").asText());
			 exchange.getIn().setBody(objectNode.toString());
		}catch(Exception e)
		{
			log.error("error found at PBQuoteResponseProcessor processor :",e);
		}
	}
}
