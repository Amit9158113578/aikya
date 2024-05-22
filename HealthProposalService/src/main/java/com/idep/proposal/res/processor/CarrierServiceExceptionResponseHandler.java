package com.idep.proposal.res.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.carrier.res.processor.ErrorXMLReader;
import com.idep.proposal.util.ProposalConstants;

public class CarrierServiceExceptionResponseHandler implements Processor {
	Logger log = Logger.getLogger(CarrierServiceExceptionResponseHandler.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@SuppressWarnings("unchecked")
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator, JsonProcessingException, IOException,Exception {
		
		String carrierProposalRes = exchange.getIn().getBody(String.class);
		

			log.info("proposalConfig Carrier CarrierServiceExceptionResponseHandler ");
		JsonNode proposalConfig = objectMapper.readTree(exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString());
		ObjectNode obj = this.objectMapper.createObjectNode();
		if(proposalConfig!=null){
			
			ErrorXMLReader errorReader = new ErrorXMLReader();	
			if(proposalConfig.has("resErrorTags")){
				
				log.info("proposalConfig in resErrorTags found : "+proposalConfig.get("resErrorTags"));
				log.info(proposalConfig.get("resErrorTags").get("parenTag").asText()+" ---"+proposalConfig.get("resErrorTags").get("errorListTag").asText()+"  ---- "+carrierProposalRes);
				@SuppressWarnings("unchecked")
				List<String> Proposalerror = new ArrayList<String>(); 
				Proposalerror=errorReader.readXMLerror(carrierProposalRes, proposalConfig.get("resErrorTags").get("parenTag").asText(), proposalConfig.get("resErrorTags").get("errorListTag").asText());
				log.info("Error method execution complted ");
				JsonDocument commonErrorList= serverConfig.getDocBYId("HealthProposalErrorList");
				if(commonErrorList!=null){
					JsonNode commonError = objectMapper.readTree(commonErrorList.content().toString());
					if(commonError.has(proposalConfig.get(ProposalConstants.CARRIER_ID).asText())){
						log.info("Carrier Common Error List commonError :"+commonError);
						JsonNode CarrierErrorList = commonError.get(proposalConfig.get(ProposalConstants.CARRIER_ID).toString());
						log.info("Carrier Common Error List CarrierErrorList : "+CarrierErrorList);		
				if(Proposalerror.size()>0){
					log.info("Error found if in Propoosal : "+Proposalerror.toString());
					ArrayNode error = objectMapper.createArrayNode();
					for(String proposalerror : Proposalerror){
						error.add(proposalerror);
					}
			
						obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
						obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
						obj.put(ProposalConstants.PROPOSAL_RES_DATA, error);
						exchange.getIn().setBody(obj);
						log.error(ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Health carrier proposal Response : "+carrierProposalRes);
						throw new ExecutionTerminator();
					}else{
						log.info("Carrier Service Resposne in no error found ");
						HashSet<String> prefixList = errorReader.validateStartTagXML(carrierProposalRes, proposalConfig.get("resErrorTags").get("parenTag").asText());
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
					log.info("Carrier Common Error document not found");
				}
				}else{
					log.info("Error found Else  in Propoosal : "+Proposalerror.toString());
					if(Proposalerror.size()>0){
						obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
						obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
						obj.put(ProposalConstants.PROPOSAL_RES_DATA, objectMapper.readTree(Proposalerror.toString()));
						exchange.getIn().setBody(obj);
						log.error(ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Health carrier proposal Response : "+Proposalerror);
						throw new ExecutionTerminator();
					}else{
						log.info("no error found in proposal for Cigna");
					}
				}
				
			}
			
			
		}else{
			log.info("unable to read proposal config property ");
		}
	}

}
