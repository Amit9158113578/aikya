package com.idep.healthquote.res.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.res.processor.ErrorXMLReader;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CarrierServiceExceptionResponseHandler implements Processor {
	Logger log = Logger.getLogger(CarrierServiceExceptionResponseHandler.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@SuppressWarnings("unchecked")
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator, JsonProcessingException, IOException,Exception {
		String carrierProposalRes = exchange.getIn().getBody(String.class);
		log.info("resConfigData Carrier CarrierServiceExceptionResponseHandler ");
		JsonNode resConfigData = exchange.getProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,JsonNode.class);
		//JsonNode resConfigData = objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.PROPOSALREQ_CONFIG).toString());
		ObjectNode obj = this.objectMapper.createObjectNode();
		if(resConfigData!=null){
			ErrorXMLReader errorReader = new ErrorXMLReader();	
			if(resConfigData.has("resErrorTags")){
				log.info("resConfigData in resErrorTags found : "+resConfigData.get("resErrorTags"));
				log.info(resConfigData.get("resErrorTags").get("parenTag").asText()+" ---"+resConfigData.get("resErrorTags").get("errorListTag").asText()+"  ---- "+carrierProposalRes);
				@SuppressWarnings("unchecked")
				List<String> Proposalerror = new ArrayList<String>(); 
				Proposalerror=errorReader.readXMLerror(carrierProposalRes, resConfigData.get("resErrorTags").get("parenTag").asText(), resConfigData.get("resErrorTags").get("errorListTag").asText());
				log.info("Error method execution complted ");
				if(Proposalerror.size()>0){
					log.info("Error found if in Quote : "+Proposalerror.toString());
					ArrayNode error = objectMapper.createArrayNode();
					for(String proposalerror : Proposalerror){
						error.add(proposalerror);
					}
					obj.put(HealthQuoteConstants.QUOTE_RESECODEFAIL, HealthQuoteConstants.QUOTE_RESEMSGEFAIL);
					obj.put(HealthQuoteConstants.ERROR_CONFIG_MSG, HealthQuoteConstants.ERROR_CONFIG_MSG);
					obj.put(HealthQuoteConstants.QUOTE_RES_DATA, error);
					exchange.getIn().setBody(obj);
					log.error("|FAIL|"+"Health Quote Response Response : "+carrierProposalRes);
					throw new ExecutionTerminator();
				}else{
					log.info("Carrier Service Resposne in no error found ");
					HashSet<String> prefixList = errorReader.validateStartTagXML(carrierProposalRes, resConfigData.get("resErrorTags").get("parenTag").asText());
					/**
					 * 
					 * iterate set 
					 * */
					this.log.info("Prefixes are "+prefixList);
					for(String s:prefixList){
						carrierProposalRes = carrierProposalRes.replaceAll("<"+s+":" , "<");
						carrierProposalRes = carrierProposalRes.replaceAll("</"+s+":" , "</");
						carrierProposalRes = carrierProposalRes.replaceAll(s+":" , "");
						carrierProposalRes = carrierProposalRes.replaceAll("xmlns:"+s , "xmlns");
					}
					log.info("replaced Prefic After generated xml "+carrierProposalRes);
					exchange.getIn().setBody(carrierProposalRes);
				}

			}else{
				log.info("Carrier Common Error tags not fount in document");
			}
		}
		else{

			log.info("config document no found");
		}
	}

}






