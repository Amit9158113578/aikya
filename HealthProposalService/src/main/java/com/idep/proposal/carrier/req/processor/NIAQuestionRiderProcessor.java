/**
 * 
 */
package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.req.processor.ProposalReqProcessor;
import com.idep.proposal.util.ProposalConstants;
//import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
/**
 * @author sayli.boralkar
 *
 */
public class NIAQuestionRiderProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(NIAQuestionRiderProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String input = exchange.getIn().getBody(String.class);
			JsonNode inputReq=objectMapper.readTree(input);
			JsonNode proposalReqForQuestion = inputReq.get("proposalRequest");
			ArrayNode mediQuestion =(ArrayNode) proposalReqForQuestion.get("medicalQuestionarrie");
			JsonNode proposalReq = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
			JsonNode proposalConfiguration = objectMapper.readTree(exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString());
			ArrayNode rider =(ArrayNode) proposalReq.get("coverageDetails").get("riders");
			if(proposalReqForQuestion.has("medicalQuestionarrie"))
			{
				ArrayNode finalNIAQuestionList = objectMapper.createArrayNode();
				if(mediQuestion.size() > 0){
					for(JsonNode listData:mediQuestion){
						if(listData.get("applicable").asText() != "false"){
							finalNIAQuestionList.add(listData);
						}else{
							finalNIAQuestionList.add(listData);
						}

					}
					((ObjectNode)proposalReqForQuestion).put("medicalQuestionarrie", finalNIAQuestionList);
				}
			}
			if(proposalConfiguration.has("optionalCoverI")){

				if(rider.size() > 0){
					JsonNode riderData =inputReq;
					
					for(JsonNode riderArrayData : rider){
						
						if(riderArrayData.get("riderId").asText().equals("38")){
							if(riderArrayData.get("riderFlag").asText() != "false"){
								((ObjectNode)riderArrayData).put("name","Optional Cover I- No Proportionate Deduction");
								((ObjectNode)riderArrayData).put("value","YES");
								((ObjectNode)riderData).put("optionalCoverI", riderArrayData);
								
							}
						}
					}
					((ObjectNode)inputReq).putAll((ObjectNode)riderData);
				}
			}
			else if(proposalConfiguration.has("optionalCoverII")){
				if(rider.size() > 0){
					JsonNode riderData =inputReq;

					for(JsonNode riderArrayData : rider){
						if(riderArrayData.get("riderId").asText().equals("45")){
							if(riderArrayData.get("riderFlag").asText() != "false"){
								((ObjectNode)riderArrayData).put("name","Optional Cover III- Revision in Cataract Limit");
								((ObjectNode)riderArrayData).put("value","YES");
								((ObjectNode)riderData).put("optionalCoverII", riderArrayData);
							}
						}
					}
					((ObjectNode)inputReq).putAll((ObjectNode)riderData);
				}
				}else if(proposalConfiguration.has("optionalCoverIII")){
					if(rider.size() > 0){
						JsonNode riderData =inputReq;

						for(JsonNode riderArrayData : rider){
							if(riderArrayData.get("riderId").asText().equals("39")){
								if(riderArrayData.get("riderFlag").asText() != "false"){
									((ObjectNode)riderArrayData).put("name","Optional Cover IV- Voluntary Co-pay");
									((ObjectNode)riderArrayData).put("value","YES");
									((ObjectNode)riderData).put("optionalCoverIII", riderArrayData);
								}
							}
						}
						((ObjectNode)inputReq).putAll((ObjectNode)riderData);
						
					}
				}
				log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERRES+"|SUCCESS|PROPOAL REQUEST MAPPING COMPLETED");
				exchange.getIn().setBody(inputReq);

			}catch(Exception e){
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NIAQuestionRiderProcessor|",e);
			}
		}

	}
