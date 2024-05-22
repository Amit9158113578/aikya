package com.idep.travelquote.res.transformer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class FutureGenResTransformer implements Processor {

	ObjectMapper objectMapper=new ObjectMapper();
	Logger log = Logger.getLogger(FutureGenResTransformer.class.getName());
	
	double sumInsured;
	double totalPremiumAmount;
	double serviceTaxAmount;
	double premiumWithServiceTax;
	//FutureGenResTransformer fgObject = new FutureGenResTransformer();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String carrierResponse = exchange.getIn().getBody(String.class);
			double uiSumInsured = 0;
			String inputContractTypeCode = null;
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			JsonNode uiInputRequest = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
			log.info("Ui Input Request to getting sumInsured as selected in UI"+ uiInputRequest);
			if(uiInputRequest.get("travelDetails").has("sumInsured") && uiInputRequest.get("productInfo").has("ContractType"))
			{
				log.info("UI input sumInsured :"+((ObjectNode)uiInputRequest).get("travelDetails").get("sumInsured").asDouble());
				uiSumInsured = ((ObjectNode)uiInputRequest).get("travelDetails").get("sumInsured").asDouble();
				inputContractTypeCode = ((ObjectNode)uiInputRequest).get("productInfo").get("ContractType").asText();
				log.info("uiSumInsured value: "+uiSumInsured);
				//uiSumInsured = 100000;
			}
			JsonNode policyNode =carrierResNode.get("Policy");
			//30000 hardcoded for testing purpose we will give UI sumInsured
			ObjectNode CarrierUiResponse= objectMapper.createObjectNode();
			
			if(uiInputRequest.get("travelDetails").has("tripType"))
			{
				if(uiInputRequest.get("travelDetails").get("tripType").asText().equalsIgnoreCase("Single"))
				{
					for(JsonNode node : policyNode)
					{
						double suminsuredamount = Double.parseDouble(node.get("SumInsured").asText().replaceAll(",", ""));
						String productContractTypeCode =  node.get("ContractType").asText();
						if(suminsuredamount == uiSumInsured && productContractTypeCode.equalsIgnoreCase(inputContractTypeCode))
						{			
							log.info("Risk Type Code matched with input product risk type");
							sumInsured = Double.parseDouble(node.findValue("SumInsured").asText().replaceAll(",", ""));	
							totalPremiumAmount = Double.parseDouble(node.findValue("ToatlPremiumAmount").asText().replaceAll(",", ""));	
							serviceTaxAmount = Double.parseDouble(node.findValue("ServiceTaxAmount").asText().replaceAll(",", ""));	
							premiumWithServiceTax = Double.parseDouble(node.findValue("PremiumWithServiceTax").asText().replaceAll(",", ""));	
							
							CarrierUiResponse.put("sumInsured", sumInsured);
							CarrierUiResponse.put("serviceTax", serviceTaxAmount);
							CarrierUiResponse.put("grossPremium", premiumWithServiceTax);
							CarrierUiResponse.put("netPremium", totalPremiumAmount);
							CarrierUiResponse.put("planName",node.findValue("PlanName"));
							CarrierUiResponse.put("planCode",node.findValue("PlanCode"));
							CarrierUiResponse.put("contractType",node.findValue("ContractType"));
							CarrierUiResponse.put("carrierProductName",node.findValue("ProductName"));
							CarrierUiResponse.put("coverageValue",node.findValue("RiskType"));
					
							((ObjectNode)carrierResNode).put("carrierResponse", CarrierUiResponse);
							break;
						}
					}
				}
				else if(uiInputRequest.get("travelDetails").get("tripType").asText().equalsIgnoreCase("Multi"))
				{
					log.info("policyNode for MultiTrip:::"+policyNode);
					for(JsonNode node : policyNode)
					{
						log.info("Multi Node info: "+node);
						log.info("Output Suminsured amount: "+node.get("PlanName"));
				
						double suminsuredamount = Double.parseDouble(node.get("SumInsured").asText().replaceAll(",", ""));
						String travelDays = node.get("PlanName").asText();
						String UItripDuration = uiInputRequest.get("travelDetails").get("tripDuration").asText();
						log.info("input suminsuredamount: "+suminsuredamount);                 
						String productRiskTypeCode =  node.get("ContractType").asText();
						String planName = node.get("PlanName").asText();
						if(UItripDuration.contains(UItripDuration))
						{
							log.info("MultiTrip TravelDays Matched successfully: ");
						}
						if(suminsuredamount == uiSumInsured && planName.contains(UItripDuration)
								&& productRiskTypeCode.equalsIgnoreCase(inputContractTypeCode))
						{	
							log.info("Condition Matched ::");
							sumInsured = Double.parseDouble(node.findValue("SumInsured").asText().replaceAll(",", ""));	
							totalPremiumAmount = Double.parseDouble(node.findValue("ToatlPremiumAmount").asText().replaceAll(",", ""));	
							serviceTaxAmount = Double.parseDouble(node.findValue("ServiceTaxAmount").asText().replaceAll(",", ""));	
							premiumWithServiceTax = Double.parseDouble(node.findValue("PremiumWithServiceTax").asText().replaceAll(",", ""));	
							
							CarrierUiResponse.put("sumInsured", sumInsured);
							CarrierUiResponse.put("serviceTax", serviceTaxAmount);
							CarrierUiResponse.put("grossPremium", premiumWithServiceTax);
							CarrierUiResponse.put("netPremium", totalPremiumAmount);
							CarrierUiResponse.put("planName",node.findValue("PlanName"));
							CarrierUiResponse.put("planCode",node.findValue("PlanCode"));
							CarrierUiResponse.put("contractType",node.findValue("ContractType"));
							CarrierUiResponse.put("carrierProductName",node.findValue("ProductName"));
							CarrierUiResponse.put("coverageValue",node.findValue("RiskType"));
							log.info("after adding values::::"+CarrierUiResponse);
							((ObjectNode)carrierResNode).put("carrierResponse", CarrierUiResponse);
							
						}
					}
				}
				
			}	
				
			String finalCarrierResponse = carrierResNode.toString();
			log.info("finalCarrierResponse :"+finalCarrierResponse);
			//exchange.getIn().setBody(finalCarrierResponse);
			 String sumInsured =carrierResNode.get("carrierResponse").get("sumInsured").asText();
	    	  String grossPremium =carrierResNode.get("carrierResponse").get("grossPremium").asText();
	    	  String netPremium =carrierResNode.get("carrierResponse").get("netPremium").asText();
	      
	    	  if(sumInsured.equalsIgnoreCase("")||grossPremium.equalsIgnoreCase("")||netPremium.equalsIgnoreCase("")){
	    		  throw new ExecutionTerminator();
	    	  }else{
	    		  exchange.getIn().setBody(finalCarrierResponse);			    
	    		  }
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.FUTUREGENRESTRANS+"|ERROR|"+" Exception at FutureGenResTransformer for response :",e);
			throw new ExecutionTerminator();
		}
	}
	
}
