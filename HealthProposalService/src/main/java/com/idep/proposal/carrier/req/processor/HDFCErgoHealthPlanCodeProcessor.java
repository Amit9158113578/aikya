package com.idep.proposal.carrier.req.processor;


/***
 * @author pravin.jakhi
 * 
 * This class created purpose is get PlanCode for HDFC Ergo as per adultCount,childCount,planId,minSumAssured
 * 
 * **/
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.HealthGenericFunction;
import com.idep.proposal.util.ProposalConstants;

public class HDFCErgoHealthPlanCodeProcessor implements Processor {

	Logger log = Logger.getLogger(HDFCErgoHealthPlanCodeProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	HealthGenericFunction healthgenricfun = new HealthGenericFunction();
	
	
	
	/**
	 * Required Attribute 
	 * carrierId,planId,adultCount,childCount,age,minAllowedSumInsured,policyTerm
	 * 
	 * */
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
	try{
		List<JsonObject> planCodeList=null;
		String inputReq=exchange.getIn().getBody(String.class);
		ArrayList<Integer> ageCount = null;	
		JsonNode inputReqNode = objectMapper.readTree(inputReq);
		int carrierId=inputReqNode.get("carrierId").asInt();
		int planId = inputReqNode.get("planId").asInt();
		log.info("Health Proposal Request Document Property :"+exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG));
		JsonNode HPR =  (JsonNode) exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG);
		long sumInsured=inputReqNode.get("coverageDetails").get("sumAssured").asLong();
		int poliyTerm=inputReqNode.get("coverageDetails").get("policyTerm").asInt();
		int adultCount=0;
		int childCount=0;
		
		log.info("input node in HDFCErgoHealthPlanCodeProcessor :"+inputReqNode);
		if(inputReqNode.has("hdfcBMI")){
			((ObjectNode)inputReqNode.get("acceptedPreExistDisease")).put("applicable", "true");
		}
		
		if(HPR.has("isMedisureClassic") || HPR.has("isTopUp")){
			 ArrayNode insuredMember = (ArrayNode)objectMapper.readTree(inputReqNode.get("insuredMembers").toString());
			 log.info("log after insured members :"+insuredMember);
			 for(JsonNode memberNode : insuredMember){
				 ArrayNode diseaseDetail = (ArrayNode)objectMapper.readTree(memberNode.get("dieaseDetails").toString());
				log.info("log after diseaseDetail :"+diseaseDetail);
				
				for(JsonNode memberNodeofDD : diseaseDetail){
				BooleanNode applicable = (BooleanNode)objectMapper.readTree(memberNodeofDD.get("applicable").asText());
				log.info("log after applicable field :"+applicable);
				if (applicable.asBoolean()){
					log.info("done dana done done");
					((ObjectNode)inputReqNode.get("acceptedPreExistDisease")).put("applicable", "true");
				}
			}
		}
	}
		ArrayNode ridersListNode = (ArrayNode)inputReqNode.get("coverageDetails").get("riders");
	      
	      if(ridersListNode!=null && (HPR.has("isMedisureClassic")&& HPR.get("isMedisureClassic").asText().equalsIgnoreCase("Y")))
	      {
	    	  log.info("ridedrList node is not null");
	    	  	  for(JsonNode rider:ridersListNode)
		    	  {
	    	  		if(rider.has("riderPremiumAmount")){
	    	  		int RID = rider.get("riderId").asInt();
	    	  		 log.info("value of Rider ID :"+RID);
		    		  if(RID == 43 || RID == 16){
	    				  int basePremium = inputReqNode.get("coverageDetails").get("basePremium").asInt();
	    				  int riderPremium = rider.get("riderPremiumAmount").asInt();
	    				  int basePremium1 = basePremium + riderPremium;
	    				  log.info("basePremium1 value :"+basePremium1);
	    				  ((ObjectNode)inputReqNode.get("coverageDetails")).put("basePremium", basePremium1);
		    		  }
		    	  }
		    	  }
	      }	
			
		JsonNode membersList = inputReqNode.get("insuredMembers");
		ageCount= new ArrayList<Integer>();
		 int count = 1;
		for(JsonNode member : membersList){
			String realtionCode = member.get("relationshipCode").asText();
			String date = member.get("dateOfBirth").asText();
			DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			int age =healthgenricfun.getAge(sdf.parse(member.get("dateOfBirth").asText()));
			
			if ( HPR.has("isMedisureClassic")&& HPR.get("isMedisureClassic").asText().equalsIgnoreCase("Y")){
				if(age >= 51){
					((ObjectNode)inputReqNode.get("acceptedPreExistDisease")).put("applicable", "true");
				}
			}
			if( HPR.has("isTopUp") && HPR.get("isTopUp").asText().equalsIgnoreCase("Y")){
				if(age >= 56){
					((ObjectNode)inputReqNode.get("acceptedPreExistDisease")).put("applicable", "true");
				}
			}
		
			/*if(age > 18){
				adultCount++;
			}else
			{
				childCount++;
			}*/
			if(realtionCode.equalsIgnoreCase("CH")){
				childCount++;
		}else
		{
			adultCount++;
		}
			
			ageCount.add(age);
		}
		
		Collections.sort(ageCount);
		/*log.info( " HDFCErgoHealthPlanCodeProcessor MAXIMUM AGE : "+ageCount.toString());
		log.info( " HDFCErgoHealthPlanCodeProcessor MAXIMUM AGE : "+ageCount.get(ageCount.size()-1));
		log.info(" HDFCErgoHealthPlanCodeProcessor Adult Cunt : "+adultCount+"\t ChildCount : "+childCount);
		*/
			JsonArray paramobj = JsonArray.create();
			/***
			 * validating planType ,  F means Family then load query for familyPlan
			 * if I then load query for INDIVIDUAL
			 * 
			 * **/
			log.debug( " HDFCErgoHealthPlanCodeProcessor MAXIMUM AGE : "+inputReqNode.get("planType").asText());
			if(inputReqNode.get("planType").asText().equalsIgnoreCase("F")){
			paramobj.add(carrierId);
			paramobj.add(planId);
			paramobj.add(adultCount);
			paramobj.add(childCount);
			paramobj.add(ageCount.get(ageCount.size()-1));
			paramobj.add(sumInsured);
			paramobj.add(poliyTerm);
			log.debug("Family Plan Paramer to Query HDFC : "+paramobj);
			planCodeList= serverConfigService.executeConfigParamArrQuery(ProposalConstants.PROPSALFAMILYQUERY,paramobj);
			}else{
				paramobj.add(carrierId);
				paramobj.add(planId);
				paramobj.add(ageCount.get(ageCount.size()-1));
				paramobj.add(sumInsured);
				paramobj.add(poliyTerm);
				log.debug("PROPSAL INDIVIDUAL Plan Paramer to Query HDFC : "+paramobj);
				planCodeList= serverConfigService.executeConfigParamArrQuery(ProposalConstants.PROPSALINDIVIDUALQUERY,paramobj);
			}
			if(planCodeList!=null && planCodeList.size()!=0){
			JsonNode planCode = objectMapper.readTree(planCodeList.get(0).toString());
			String plan = planCode.get("planCode").asText();
			if(plan!=null){
				((ObjectNode)inputReqNode).put("planCode", plan);
			}
			log.debug("HDFC Ergo PlanCode Added in Request :  "+inputReqNode.toString());
			exchange.getIn().setBody(inputReqNode);
		
			}else{
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|"+"Plan Code Not found for HDFC Ergo Health : "+inputReqNode);
				throw new ExecutionTerminator();		
			}
		
	}catch(Exception e){
		log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|HDFCErgoHealthPlanCodeProcessor|",e);
		throw new ExecutionTerminator();
	}

	}

}
