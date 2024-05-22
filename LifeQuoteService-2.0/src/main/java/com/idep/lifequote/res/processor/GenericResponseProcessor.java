package com.idep.lifequote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class GenericResponseProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GenericResponseProcessor.class.getName());
	CBService service = null;
	JsonNode responseConfigNode;
	JsonNode lifeCarrierQNode=null;
	JsonNode errorNode;

	public void process(Exchange exchange){
		try{
			this.log.debug("GenericResponseProcessor invoked for life");
			if(this.service == null){
				this.service = CBInstanceProvider.getServerConfigInstance();
				this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(LifeQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
				this.log.debug("GenericResponseProcessor:ResponseMessages configuration loaded");
				this.lifeCarrierQNode = this.objectMapper.readTree(this.service.getDocBYId(LifeQuoteConstants.LIFE_CARRIERS_Q).content().toString());
			}

			String messageId = exchange.getProperty(LifeQuoteConstants.CORRELATION_ID).toString();
			ArrayNode qList = objectMapper.createArrayNode();
			ArrayNode resQList = (ArrayNode)lifeCarrierQNode.get(LifeQuoteConstants.RES_Q_LIST);

			for(int i=0;i<resQList.size();i++){
				ObjectNode resultNode = objectMapper.createObjectNode();
				resultNode.put(LifeQuoteConstants.QNAME, resQList.get(i).textValue());
				resultNode.put(LifeQuoteConstants.MESSAGE_ID, messageId);
				qList.add(resultNode);
			}

			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(LifeQuoteConstants.SUCC_CONFIG_CODE).intValue());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(LifeQuoteConstants.SUCC_CONFIG_MSG).textValue());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, qList);
			exchange.getIn().setBody(finalresultNode);
		}catch(Exception e){
			this.log.error("Exception at GenerateLifeResponse : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(LifeQuoteConstants.ERROR_CONFIG_CODE).intValue());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(LifeQuoteConstants.ERROR_CONFIG_MSG).textValue());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}
	}
}