package com.idep.travelquote.res.transformer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class BhartiAxaResTransformer implements Processor {

	ObjectMapper objectMapper=new ObjectMapper();
	Logger log = Logger.getLogger(BhartiAxaResTransformer.class.getName());
	
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
			JsonNode carrierTransformedReq=(JsonNode) exchange.getProperty("carrierTransformedReq");
			log.debug("carrierTransformedReq BhartiAxa"+ carrierTransformedReq);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			JsonNode uiInputRequest = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
			log.debug("Ui Input Request to getting sumInsured as selected in UI"+ uiInputRequest);
			if(uiInputRequest.get("travelDetails").has("sumInsured"))
			{
				log.debug("UI input sumInsured :"+((ObjectNode)uiInputRequest).get("travelDetails").get("sumInsured").asDouble());
				uiSumInsured = ((ObjectNode)uiInputRequest).get("travelDetails").get("sumInsured").asDouble();
				log.debug("uiSumInsured value: "+uiSumInsured);
				//uiSumInsured = 100000;
			}
			JsonNode policyNode =carrierResNode.get("response").get("PremiumSet");
			//30000 hardcoded for testing purpose we will give UI sumInsured
			ObjectNode CarrierUiResponse= objectMapper.createObjectNode();
			for(JsonNode node : policyNode)
			{
				log.debug("Node info: "+node);
				log.debug("Output Suminsured amount: "+node.get(0).get(0).get("SumInsured").doubleValue());
				double suminsuredamount = Double.parseDouble(node.get(0).get(0).get("SumInsured").asText().replaceAll(",", ""));
				log.info("input suminsuredamount: "+suminsuredamount);
				//testing purpose hard coded                     
				//log.info("carrierResNode.Policy : "+ (fgObject.CommaRemover(node.get("SumInsured").toString())));
				if(suminsuredamount == uiSumInsured)
				{			
					log.debug("Node info after comparision: "+node);
					sumInsured = Double.parseDouble(node.get(0).get(0).get("SumInsured").asText().replaceAll(",", ""));	
					totalPremiumAmount = Double.parseDouble(node.get(0).get(0).get("Premium").asText().replaceAll(",", ""));	
					serviceTaxAmount = Double.parseDouble(node.get(0).get(0).get("ServiceTax").asText().replaceAll(",", ""));	
					premiumWithServiceTax = Double.parseDouble(node.get(0).get(0).get("PremiumPayable").asText().replaceAll(",", ""));	
					log.debug("TotalPremiumAmount::::::"+totalPremiumAmount);
					
					CarrierUiResponse.put("sumInsured", sumInsured);
					CarrierUiResponse.put("serviceTax", serviceTaxAmount);
					CarrierUiResponse.put("grossPremium", premiumWithServiceTax);
					CarrierUiResponse.put("netPremium", totalPremiumAmount);
					CarrierUiResponse.put("planName",node.get(0).get(0).get("PlanName").asText());
					CarrierUiResponse.put("planCode",node.get(0).get(0).get("PlanId").asText());
					CarrierUiResponse.put("OrderNo",carrierResNode.get("response").findValue("OrderNo"));
					CarrierUiResponse.put("QuoteNo",carrierResNode.get("response").findValue("QuoteNo"));
					CarrierUiResponse.put("TPClientRefNo",carrierTransformedReq.get("Session").get("Client").findValue("TPClientRefNo"));
					CarrierUiResponse.put("FamilyType",carrierTransformedReq.get("Session").get("Travel").findValue("FamilyType"));
					CarrierUiResponse.put("multiTripDuration",carrierTransformedReq.get("Session").get("Travel").findValue("MaxPerTripDuration"));
					((ObjectNode)carrierResNode).put("carrierResponse", CarrierUiResponse);
					break;
				}
			}
			String finalCarrierResponse = carrierResNode.toString();
			log.debug("finalCarrierResponse BhartiAxa:"+finalCarrierResponse);
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
