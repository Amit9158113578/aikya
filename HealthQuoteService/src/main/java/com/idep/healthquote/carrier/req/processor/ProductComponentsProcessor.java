package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.couchbase.api.impl.CBService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;


public class ProductComponentsProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProductComponentsProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			String inputReq= exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			JsonNode prodComponent = inputReqNode.get("QuotationRequest").get("insuredDetail").get("insuredList").get(0).get("productComponents");
			log.debug("ProductComponentsProcessor : prodComponent : "+prodComponent);
			log.debug("ProductComponentsProcessor Quote Request : prodComponent : "+exchange.getProperty("carrierInputRequest"));
			ArrayNode tempComponent = objectMapper.createArrayNode();
			
			JsonNode quoteReq = objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
			log.debug("quoteReq.get(productInfo).get(carrierId quoteReq.get(productInfo).get(planId)).content().toString() "+quoteReq.get("productInfo").get("carrierId")+"-"+quoteReq.get("productInfo").get("planId"));
			JsonNode QuoteConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("HealthQuoteRequest-"+quoteReq.get("productInfo").get("carrierId")+"-"+quoteReq.get("productInfo").get("planId")).content().toString());
			log.debug("QuoteConfigDoc Read : "+QuoteConfigDoc);
			log.debug("QUote Request :  "+quoteReq);
			ArrayNode SelectedFamilyMem = (ArrayNode)quoteReq.get("personalInfo").get("selectedFamilyMembers");
			int index=0;
			int memberNo=0;
			ArrayNode rider= null;
			if(quoteReq.get("quoteParam").has("riders")){
			rider= (ArrayNode)quoteReq.get("quoteParam").get("riders");
			}
			for(JsonNode data : prodComponent){
				for(JsonNode node : data.get("productComponentsData"))
				{
					ObjectNode zoneNode = objectMapper.createObjectNode();
					zoneNode.put("memberno",++index );
					zoneNode.put("productComponentName", "zone");
					zoneNode.put("productComponentValue",inputReqNode.get("QuotationRequest").get("proposer").get("contact").get("zone").asText() );
					tempComponent.add(zoneNode);
					if(node.has("productComponentName") && node.get("productComponentName").asText().equalsIgnoreCase("SumInsured")){
						((ObjectNode)node).put("memberno",index );
						log.debug(" node.get(productComponentName).get(productComponentName) : "+node.get("productComponentName").get("memberno"));
					}
					tempComponent.add(node);
				}
				
				 
			/*log.info("rider for ABHI : "+rider);
				}*/
			}
			int count=0;
			/***
			 * 
			 * reading Insured Array and adding memberno field value by sequence
			 * 
			 * */
			ArrayNode insured= (ArrayNode) inputReqNode.get("QuotationRequest").get("insuredDetail").get("insuredList").get(0).get("Insured");
			
			for(JsonNode member : insured ){
					++count;
				((ObjectNode)member).put("memberno", count);
				//++memberNo;
				if(rider!=null && rider.size()>0){ 
					for(JsonNode UIRider : rider){
						
						log.info("QuoteConfigDoc : "+QuoteConfigDoc);
						if(QuoteConfigDoc.has(UIRider.get("riderId").asText())){
							
							JsonNode RoomrentList = QuoteConfigDoc.get(UIRider.get("riderId").asText());
							if(RoomrentList.has(UIRider.get("value").asText())){
							ObjectNode roomRent = objectMapper.createObjectNode();
							
							roomRent.put("memberno", count);
							roomRent.put("productComponentName", "RoomCategory");
							roomRent.put("productComponentValue", RoomrentList.get(UIRider.get("value").asText()));
							tempComponent.add(roomRent);
							}
						}
					}
				}
			}
			log.debug("MemberNo Added in Insured Array : "+inputReqNode.get("QuotationRequest").get("insuredDetail").get("insuredList").get(0).get("Insured"));
			count=0;
			for(JsonNode member : SelectedFamilyMem){
				
				log.debug("member SelectedFamilyMem : "+member);
				ArrayNode diease = (ArrayNode)member.get("dieaseDetails");
				log.debug("(diease - ABHi class) "+diease);
				++count;
				if(diease.size()==0){
					ObjectNode diseaseNode = objectMapper.createObjectNode();
					diseaseNode.put("memberno",count );
					diseaseNode.put("productComponentName", "Conditions");
					diseaseNode.put("productComponentValue",QuoteConfigDoc.get("dieaseList").get("default"));
					tempComponent.add(diseaseNode);
				}else{
					int dieaseCount=0;
					for(JsonNode list : diease){
						
						ObjectNode diseaseNode = objectMapper.createObjectNode();
						log.info("list.get(dieaseName.asText() "+list.get("dieaseCode").asText());
						if(QuoteConfigDoc.has("dieaseList")){
							log.debug(QuoteConfigDoc.get("dieaseList").get(list.get("dieaseCode").asText()));
							if(QuoteConfigDoc.get("dieaseList").has(list.get("dieaseCode").asText())){
								++dieaseCount;
							diseaseNode.put("memberno", count);
							diseaseNode.put("productComponentName", "Conditions");
							diseaseNode.put("productComponentValue",QuoteConfigDoc.get("dieaseList").get(list.get("dieaseCode").asText()));
						}
						tempComponent.add(diseaseNode);
						}
					}
					if(dieaseCount==0){
						log.debug(" QuoteConfigDoc.get(dieaseList) : "+QuoteConfigDoc.get("dieaseList"));
						ObjectNode diseaseNode = objectMapper.createObjectNode();
						diseaseNode.put("memberno",count );
						diseaseNode.put("productComponentName", "Conditions");
						diseaseNode.put("productComponentValue",QuoteConfigDoc.get("dieaseList").get("default"));
						tempComponent.add(diseaseNode);	
					}
				}
			}
			((ObjectNode)inputReqNode.get("QuotationRequest").get("insuredDetail").get("insuredList").get(0)).put("productComponents", tempComponent);
			log.debug(" ProductComponentsProcessor Data comeplete productComponentsData orgnised ---------- : "+tempComponent);
			exchange.getIn().setBody(inputReqNode);
		}catch(Exception e){
			log.error("Exception at ProductComponentsProcessor : ",e);
			throw new ExecutionTerminator();
		}

	}

}
