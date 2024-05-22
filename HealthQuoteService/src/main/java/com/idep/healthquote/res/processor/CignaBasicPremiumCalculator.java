package com.idep.healthquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class CignaBasicPremiumCalculator implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaBasicPremiumCalculator.class.getName());

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		double basicPremium =0;
		try
		{
			ArrayNode riderNode = objectMapper.createArrayNode();
			String carrierResponse = exchange.getIn().getBody(String.class);
			String clientRiderCode ="";
			JsonNode UIReq = objectMapper.readTree(exchange.getProperty("HealthProductUIReq").toString());
			ArrayNode prductRider = (ArrayNode)UIReq.get("productInfo").get("riderList");
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			if(carrierResNode.get("quoteResult").get("quotationProductDOList").isArray()){
				ArrayNode ProductListNode = (ArrayNode) carrierResNode.get("quoteResult").get("quotationProductDOList");
				for(JsonNode node:ProductListNode ){
					basicPremium = basicPremium+ node.get("basePremium").asDouble();
						for(JsonNode prodRider:prductRider){
							prodRider.get("clientRiderCode").asText().equalsIgnoreCase(node.get("productId").asText());
							clientRiderCode=prodRider.get("clientRiderCode").asText();
							break;
						}
					if(node.has("productId")&& !clientRiderCode.isEmpty() && node.get("productId").asText().equalsIgnoreCase(clientRiderCode)&& node.get("basePremium").asDouble()>0){
						ObjectNode CriticalIllnessNode = objectMapper.createObjectNode();
						CriticalIllnessNode.put("amount", node.get("basePremium").asText());
						CriticalIllnessNode.put("benefitId", "PREMIUM");
						CriticalIllnessNode.put("benefitTypeCd", "RIDER");
						CriticalIllnessNode.put("productId",  node.get("productId").asText());
						riderNode.add(CriticalIllnessNode);
						log.info("critical Illness Rider Added : "+CriticalIllnessNode);
						
					}
					if(node.get("quotationProductInsuredDOList").isArray()){
						ArrayNode insuredArray =(ArrayNode)node.get("quotationProductInsuredDOList");
						for(JsonNode member:insuredArray){
							if(member.has("quotationProductInsuredBenefitDOList")){
								if(member.get("quotationProductInsuredBenefitDOList").isArray()){
									ArrayNode addonNode = (ArrayNode) member.get("quotationProductInsuredBenefitDOList");
									for(JsonNode childNode : addonNode){
										if(childNode.has("amount")){
											basicPremium = basicPremium + childNode.get("amount").asDouble();
											riderNode.add(childNode);
										}
									}
								}
								else{
									basicPremium = basicPremium +member.get("quotationProductInsuredBenefitDOList").get("amount").asDouble();
									riderNode.add(member.get("quotationProductInsuredBenefitDOList"));
								}
							}

						}

					}else{
						if(node.get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").isArray()){
							ArrayNode addonNode = (ArrayNode) node.get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList");
							for(JsonNode childNode : addonNode){
								if(childNode.has("amount")){
									basicPremium = basicPremium + childNode.get("amount").asDouble();
									riderNode.add(childNode);
								}
							}

						}else{
							basicPremium = basicPremium +node.get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").get("amount").asDouble();
							riderNode.add(node.get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList"));
						}
					}

				}


			}else{
				basicPremium = basicPremium+ carrierResNode.get("quoteResult").get("quotationProductDOList").get("basePremium").asDouble();
				if(carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").isArray()){
					ArrayNode insuredMembers = (ArrayNode) carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList");
					for(JsonNode member:insuredMembers){
						if(member.has("quotationProductInsuredBenefitDOList")){
							if(member.get("quotationProductInsuredBenefitDOList").isArray()){
								ArrayNode addonNode = (ArrayNode) member.get("quotationProductInsuredBenefitDOList");
								for(JsonNode childNode : addonNode){
									if(childNode.has("amount")){
										basicPremium = basicPremium + childNode.get("amount").asDouble();
										riderNode.add(childNode);
									}
								}
							}
							else{
								basicPremium = basicPremium +member.get("quotationProductInsuredBenefitDOList").get("amount").asDouble();
								riderNode.add(member.get("quotationProductInsuredBenefitDOList"));
							}
						}

					}

				}else{
					if(carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").has("quotationProductInsuredBenefitDOList")){
						if(carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").isArray()){
							ArrayNode addonNode = (ArrayNode) carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList");
							for(JsonNode childNode : addonNode){
								if(childNode.has("amount")){
									basicPremium = basicPremium + childNode.get("amount").asDouble();
									riderNode.add(childNode);
								}
							}

						}else{
							basicPremium = basicPremium +carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").get("amount").asDouble();
							riderNode.add(carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList"));
						}
					}
				}
				/*if(carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").isArray()){
					ArrayNode addonNode = (ArrayNode) carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList");
					for(JsonNode node : addonNode){
						if(node.has("amount")){
							basicPremium = basicPremium + node.get("amount").asDouble();
						}
					}

				}else{
					basicPremium = basicPremium +carrierResNode.get("quoteResult").get("quotationProductDOList").get("quotationProductInsuredDOList").get("quotationProductInsuredBenefitDOList").get("amount").asDouble();
				}*/
			}
			this.log.info("basic Premium changed to : "+basicPremium );
			this.log.info("Carrier complete RiderNode is : "+riderNode );
			exchange.setProperty("carrierRiders", riderNode);
			((ObjectNode)carrierResNode.get("quoteResult")).put("basicPremium", String.valueOf(basicPremium) );
			((ObjectNode)carrierResNode.get("quoteResult")).put("carrierResRiders", riderNode);
			exchange.getIn().setBody(carrierResNode);
		}
		catch(Exception e)
		{
			this.log.error("Exception at CignaBasicPremiumCalculator : ", e);
			throw new ExecutionTerminator();
		}

	}


}
