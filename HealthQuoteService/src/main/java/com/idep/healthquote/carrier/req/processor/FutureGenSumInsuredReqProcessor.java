package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class FutureGenSumInsuredReqProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FutureGenSumInsuredReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	@Override
	public void process(Exchange exchange) throws Exception
	{
		try{
		String inputReq = exchange.getIn().getBody(String.class);
		JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
		log.info("inputReqNode:"+inputReqNode);
		JsonNode QuoteConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("HealthQuoteRequest-"+inputReqNode.get("productInfo").get("carrierId")+"-"+inputReqNode.get("productInfo").get("planId")).content().toString());
		
		int bmiValue, smokingValue, bmismokeValue;
		
		for(JsonNode insuredMember : inputReqNode.get("personalInfo").get("selectedFamilyMembers"))
		{
			bmiValue = 0;
			smokingValue = 0;
			bmismokeValue = 0;
			if(insuredMember.has("bmi"))
			{
				String insuredMemberBmi = ((ObjectNode)insuredMember).get("bmi").asText();
				log.info("insuredMemberBmi:"+insuredMemberBmi);
				if(QuoteConfigDoc.has("bmiConfig") )
				{
					if(QuoteConfigDoc.get("bmiConfig").has(insuredMemberBmi))
					{
						bmiValue = QuoteConfigDoc.get("bmiConfig").get(insuredMemberBmi).asInt();
						log.info("bmiValue:" +bmiValue);
					}
				}
			}
			
			if(insuredMember.has("isSmoking"))
			{
				String insuredMemberSmoking = ((ObjectNode)insuredMember).get("isSmoking").asText();
				if(QuoteConfigDoc.has("smokingConfig") )
				{
					if(QuoteConfigDoc.get("smokingConfig").has(insuredMemberSmoking))
					{
					smokingValue = QuoteConfigDoc.get("smokingConfig").get(insuredMemberSmoking).asInt();
					log.info("smokingValue:" +smokingValue);
					}
				}
			}
			
			bmismokeValue = bmiValue + smokingValue;
			log.info("bmismokeValue:" +bmismokeValue);
			((ObjectNode)insuredMember).put("bmismokeValue", bmismokeValue);
			((ObjectNode)insuredMember).put("sumInsured",inputReqNode.get("productInfo").get("sumInsured").asInt());
			log.info("insuredMember sumInsured:" +insuredMember.get("sumInsured"));
		}
		
		
		
		log.info("SumInsured updated in property request : "+inputReqNode);
		
		 exchange.getIn().setBody(inputReqNode);
		}
		
		catch(Exception e)
		{
		log.error("Exception at FutureGenSumInsuredReqProcessor : ",e);	
		}
	}

}
