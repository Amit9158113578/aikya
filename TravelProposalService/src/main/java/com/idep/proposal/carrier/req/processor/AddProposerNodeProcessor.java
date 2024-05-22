package com.idep.proposal.carrier.req.processor;


import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;


public class AddProposerNodeProcessor implements Processor {
	
	Logger log = Logger.getLogger(AddProposerNodeProcessor.class.getName());
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  ObjectMapper objectMapper = new ObjectMapper();
	  @Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			ArrayNode travellerArray = new ObjectMapper().createArrayNode();
			ObjectNode ProposerNode = objectMapper.createObjectNode();
			//((ObjectNode)details).put("roleCode", "PROPOSER");
		
			if(inputReqNode.has("travellerDetails") && inputReqNode.has("proposerDetails")){
				//ProposerNode=(ObjectNode)objectMapper.readTree(inputReqNode.get("travellerDetails").get(0).toString());

				ProposerNode.put("salutation",inputReqNode.get("proposerDetails").get("salutation").asText());
				ProposerNode.put("firstName",inputReqNode.get("proposerDetails").get("firstName").asText());
				ProposerNode.put("lastName",inputReqNode.get("proposerDetails").get("lastName").asText());
				ProposerNode.put("gender",inputReqNode.get("proposerDetails").get("gender").asText());
				ProposerNode.put("dateOfBirth",inputReqNode.get("proposerDetails").get("dateOfBirth").asText());
				ProposerNode.put("relation","Self");
				ProposerNode.put("roleCode","PROPOSER");
				ProposerNode.put("guIdcode",getGUIDCode("Self"));
				
				ProposerNode.put("preExistingDiseases",inputReqNode.get("travellerDetails").get(0).get("preExistingDiseases").asText());
				ProposerNode.put("passportNo",inputReqNode.get("travellerDetails").get(0).get("passportNo").asText());
				ProposerNode.put("nomineeDetails",inputReqNode.get("travellerDetails").get(0).get("nomineeDetails"));
				ProposerNode.put("diseaseDetails",inputReqNode.get("travellerDetails").get(0).get("diseaseDetails"));
				ProposerNode.put("carrierMedicalQuestion",inputReqNode.get("travellerDetails").get(0).get("carrierMedicalQuestion"));
				
				travellerArray.add(ProposerNode);
				
				for(JsonNode details : inputReqNode.get("travellerDetails")){
						((ObjectNode)details).put("roleCode", "PRIMARY");
					/**
					 * Generating guid code base on system date and time millisecond  using genrateGuid()
					 * 
					 * **/
					((ObjectNode)details).put("guIdcode",getGUIDCode(details.get("relation").textValue()));
						travellerArray.add(details);
				}			
				log.info("Proposer node added in before ProposerNode Add : "+travellerArray);
				
				
				((ObjectNode)inputReqNode).put("travellerDetails", travellerArray);
			}
			
			log.info("Proposer node added in travellerDetails : "+inputReqNode);
			exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ,inputReqNode );
			exchange.getIn().setBody(inputReqNode);
		}catch(Exception e){
			log.error("Error at AddProposerNodeProcessor :  ",e);
		}
	}
	  
	  public String getGUIDCode(String relation)
		{
			String guiCode="";
			if(relation.equalsIgnoreCase("Self"))
			{
				guiCode = "D3714FF2-B24F-4463-905D-8E6B97145113";
			}
			else
			{
				UUID uniqueKey = UUID.randomUUID();
				guiCode = uniqueKey.toString() ;
			}
			
			return guiCode;
		}
}
