package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class FutureGenInsuredPolicyReqProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FutureGenInsuredPolicyReqProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	public void process(Exchange exchange) throws Exception
	{
		try{
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
			JsonNode ProposalConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("HealthPolicyRequest-"+inputReqNode.get("carrierId")+"-"+inputReqNode.get("planId")).content().toString());
			log.info("ProposalConfigDoc:"+ProposalConfigDoc);
			int bmiValue, smokingValue, bmismokeValue;

			for(JsonNode insuredMember : inputReqNode.get("insuredMembers"))
			{
				bmiValue = 0;
				smokingValue = 0;
				bmismokeValue = 0;
				if(insuredMember.has("bmi"))
				{
					String insuredMemberBmi = ((ObjectNode)insuredMember).get("bmi").asText();
					log.info("proposalInsuredMemberBmi:"+insuredMemberBmi);
					if(ProposalConfigDoc.has("bmiConfig") )
					{
						if(ProposalConfigDoc.get("bmiConfig").has(insuredMemberBmi))
						{
							bmiValue = ProposalConfigDoc.get("bmiConfig").get(insuredMemberBmi).asInt();
							log.info("bmiValue:" +bmiValue);
						}
					}
				}

				if(insuredMember.has("isSmoking"))
				{
					String insuredMemberSmoking = ((ObjectNode)insuredMember).get("isSmoking").asText();
					if(ProposalConfigDoc.has("smokingConfig") )
					{
						if(ProposalConfigDoc.get("smokingConfig").has(insuredMemberSmoking))
						{
							smokingValue = ProposalConfigDoc.get("smokingConfig").get(insuredMemberSmoking).asInt();
							log.info("smokingValue:" +smokingValue);
						}
					}
				}

				bmismokeValue = bmiValue + smokingValue;
				log.info("proposalbmismokeValue:" +bmismokeValue);
				((ObjectNode)insuredMember).put("bmismokeValue", bmismokeValue);

			}

			log.info("proposal request updated in property request : "+inputReqNode);

			exchange.getIn().setBody(inputReqNode);
		}

		catch(Exception e)
		{
			log.error("Exception at FutureGenSumInsuredReqPolicyProcessor : ",e);	
		}
	}

}
