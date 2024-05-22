package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.carrier.req.processor.ABHIProposalReqStoreProcessor;
import com.idep.proposal.util.ProposalConstants;
import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
public class ABHIMemberPEDProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIMemberPEDProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String input = exchange.getIn().getBody(String.class);
			JsonNode inputReq=objectMapper.readTree(input);
			ArrayNode member =(ArrayNode)inputReq.get("MemObj").get("Member"); 
			
			JsonNode proposalReq = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
			log.debug(" exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ) ABHIMemberPEDProcessor :  " +proposalReq);
			
			JsonDocument realtiondoc = serverConfig.getDocBYId("HealthRealtionCodeMappingConf-60");
			JsonNode realtionShip = objectMapper.readTree(realtiondoc.content().get("relationship").toString());
			ArrayNode rider =(ArrayNode) proposalReq.get("coverageDetails").get("riders");
		    JsonNode proposalConfiguration = objectMapper.readTree(exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString());
		    int memberNo=0;
		    ArrayNode productComponents =null;
		
		    for(JsonNode memberList : member){
				((ObjectNode)memberList).put("MemberNo", ++memberNo);
				ArrayNode memberPED = (ArrayNode)memberList.get("MemberPED");
				if(memberPED == null)
				{
					((ObjectNode)memberList).put("MemberPED", "");
				}
				else
				{
				ArrayNode finalPEDList = objectMapper.createArrayNode();
				int index =0;
				for(JsonNode list : memberPED ){
					
					if(list.has("PEDCode") && list.has("Remarks")){
						if(!list.get("Remarks").asText().equalsIgnoreCase("0")){
						log.debug("ABHIMemberPEDProcessor PED Added in Array : "+list.get("PEDCode"));
						finalPEDList.add(list);
						}
					}
				}
		
				((ObjectNode)memberList).put("MemberPED", finalPEDList);
				}
				/**
				 * Removing negative question node 
				 * */
				ArrayNode questionNode = (ArrayNode)memberList.get("MemberQuestionDetails");
				ArrayNode finalQuestionList = objectMapper.createArrayNode();
				
				for(JsonNode list : questionNode ){
					
					if(list.has("Answer") && list.has("Remarks")){
						if(!list.get("Answer").asText().equalsIgnoreCase("0")){
						finalQuestionList.add(list);
						}
					}
				}
				log.info("finalQuestionList :"+finalQuestionList);
				((ObjectNode)memberList).put("MemberQuestionDetails", finalQuestionList);
			
				if(proposalReq.get("carrierId").asInt() == 60)
				{
					if(proposalReq.get("planId").asInt() == 67 || proposalReq.get("planId").asInt() == 68)
					{
						ArrayNode habitQuestions = (ArrayNode)memberList.get("personalHabitDetail");
						ArrayNode HabitQuestionList = objectMapper.createArrayNode();
						
						for(JsonNode habitList : habitQuestions){

							if(habitList.get("type").asText().trim().equalsIgnoreCase("Smoking") ){
								if(habitList.get("count").toString() != "0"){
									int countdata = habitList.get("count").asInt() * 7;
									((ObjectNode)habitList).put("count", countdata);
									HabitQuestionList.add(habitList);
								}
							}
							if(habitList.get("type").asText().trim().equalsIgnoreCase("Tobacco") ){
								if(habitList.get("count").toString() != "0"){
									int countdata = habitList.get("count").asInt() * 7;
									
									((ObjectNode)habitList).put("count", countdata);
									HabitQuestionList.add(habitList);
									
								}
							}
							if(habitList.get("type").asText().trim().equalsIgnoreCase("Alcohol") ){
								if(habitList.get("count").toString() != "0"){
									int countdata = habitList.get("count").asInt() * 1;
									
									((ObjectNode)habitList).put("count", countdata);
									HabitQuestionList.add(habitList);
									
								}
							}
							
						}
						log.info("habitQuestionList: "+HabitQuestionList);
						((ObjectNode)memberList).put("personalHabitDetail", HabitQuestionList);
					}
				}
				/**
				 * adding rider configuration after mapper response provided(ABHi pure request)
				 * 
				 * */
			if(proposalConfiguration.has("isOptionalCoverValue"))
			{
					if(rider.size()>0){
					ArrayNode optionalCovers = (ArrayNode)memberList.get("optionalCovers");
					JsonNode riderList = proposalConfiguration.get("riderList");
					for(JsonNode riderId : rider){
						if(riderList.has(riderId.get("riderId").asText())){
							ObjectNode riderNode = objectMapper.createObjectNode(); 
							riderNode.put("optionalCoverName", riderList.get(riderId.get("riderId").asText()));
							riderNode.put("optionalCoverValue",0);
							optionalCovers.add(riderNode);
						}
					}
					log.info("optionalCoverValue in optionalCovers ABHI: "+optionalCovers);
					}
			}else
			{
				if(rider.size()>0){
					ArrayNode optionalCovers = (ArrayNode)memberList.get("optionalCovers");
					JsonNode riderList = proposalConfiguration.get("riderList");
					for(JsonNode riderId : rider){
						if(riderList.has(riderId.get("riderId").asText())){
							ObjectNode riderNode = objectMapper.createObjectNode(); 
							riderNode.put("optionalCoverName", riderList.get(riderId.get("riderId").asText()));
							riderNode.put("optionalCoverValue",1);
							optionalCovers.add(riderNode);
						}
					}
					log.info("optionalCovers are: "+optionalCovers);
					}
			}
				
				ObjectNode Diease=null;  //= objectMapper.createObjectNode();
				ArrayNode insured = (ArrayNode)proposalReq.get("insuredMembers");
				JsonNode dieaseList = proposalConfiguration.get("dieaseList");
					for(JsonNode listmember : insured ){
						int count =0;
						log.debug("listmember : "+listmember);
						log.debug("realtionShip.get(listmember.get(relationship).asText()).asText() "+realtionShip.get(listmember.get("relationship").asText()).asText());
						if(realtionShip.get(listmember.get("relationship").asText()).asText().equalsIgnoreCase(memberList.get("Relation_Code").asText())){
							ArrayNode dieaseDetails = (ArrayNode)listmember.get("dieaseDetails");
							productComponents =(ArrayNode) memberList.get("productComponents");
							if(proposalConfiguration.has("dieaseList"))
						{
							if(dieaseDetails.size() > 0 )
						{
							
							for(JsonNode diease : dieaseDetails){
							log.debug("dieaseList.get(diease.get(dieaseCode).asText()) : "+dieaseList.has(diease.get("dieaseCode").asText()));
							if(dieaseList.has(diease.get("dieaseCode").asText())&& diease.has("applicable") && diease.get("applicable").toString().equalsIgnoreCase("true")){ //.asText())!=null || !dieaseList.get(diease.get("dieaseCode").asText()).equals("")){
								log.debug("dieaseList.get(diease.get(dieaseCode).asText()) "+diease.get("dieaseCode").asText()+"\tdiease.get(applicable).asText() "+diease.get("applicable").toString());
								Diease= objectMapper.createObjectNode();
								Diease.put("productComponentName", "Conditions");
								Diease.put("productComponentValue", dieaseList.get(diease.get("dieaseCode").asText()));
							count++;
							productComponents.add(Diease);
							}
						}
						log.debug("dieaseList.get(diease.get(dieaseCode).asText())  Count : "+count);
						if(count==0){
							Diease= objectMapper.createObjectNode();
							Diease.put("productComponentName", "Conditions");
							Diease.put("productComponentValue", dieaseList.get("default"));
							productComponents.add(Diease);
						}
						}
						}
						if(rider.size()>0){
							log.info("rider size is greater than 0");
							
							for(JsonNode riderId : rider){ 
								if(proposalConfiguration.has(riderId.get("riderId").asText())){
									JsonNode riderIDinConf = proposalConfiguration.get(riderId.get("riderId").asText());
									if(riderIDinConf.has("isDefaultValue"))
									{
									Diease= objectMapper.createObjectNode(); 
									Diease.put("productComponentName", "RoomCategory");
									Diease.put("productComponentValue", riderIDinConf.get("defaultValue").asText());
									productComponents.add(Diease);
									}
									else
									{
										log.info("riderId present in confi");
										Diease= objectMapper.createObjectNode(); 
										Diease.put("productComponentName", "RoomCategory");
										Diease.put("productComponentValue", proposalConfiguration.get(riderId.get("riderId").asText()).get(riderId.get("value").asText()));
										productComponents.add(Diease);
									}
								}
								
							}
						}
						((ObjectNode)memberList).put("productComponents", productComponents);
					}
					log.debug("Member List Updated  IN ABHi : "+memberList);
					log.info("Member List Updated  : "+memberList);
				}
				}
				
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERRES+"|SUCCESS|PROPOAL REQUEST MAPPING COMPLETED");
			exchange.getIn().setBody(inputReq);
			
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIMemberPEDProcessor|",e);
		}
	}
	
	

}
