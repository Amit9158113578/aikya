package com.idep.assisment.info.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.assisment.info.constant.MiaAssesstmentInfoConstant;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.geostatistics.restService.GeoStatisticsAPI;

/**
 * @author pravin.jakhi
 *
 */

public class GetAssessmentInfoProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GetAssessmentInfoProcessor.class);
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	GetProfessionQuestionConfiguration pqc = new GetProfessionQuestionConfiguration();
	private JsonNode questionConfig;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
			if(request.has("professionCode")){
				log.info("in get assismentinfoProcessor"+request);
				questionConfig  = pqc.getProfessionQuestionConfig(request);
				log.info("in get assismentinfoProcessor");
			}
			ObjectNode responseNode = 	objectMapper.createObjectNode();
			responseNode.put(MiaAssesstmentInfoConstant.RES_CODE_TXT, MiaAssesstmentInfoConstant.RESECODESUCESS);
			responseNode.put(MiaAssesstmentInfoConstant.RES_MSG_TXT, MiaAssesstmentInfoConstant.RESEMSGSUCESS);
			log.info("in get assismentinfoProcessor responsssseeee"+questionConfig.get("Data"));

			responseNode.put(MiaAssesstmentInfoConstant.RES_DATA_TXT, questionConfig.get("Data"));
			log.info("in get assismentinfoProcessor responsssseeee"+responseNode);
			exchange.getIn().setBody(responseNode);

		}catch(Exception e){
			log.error("unable to process MIA assessment  request : ",e);
		}

	}
}
