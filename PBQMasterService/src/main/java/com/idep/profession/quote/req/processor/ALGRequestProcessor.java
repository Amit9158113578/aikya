package com.idep.profession.quote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.insassessment.invoke.invoker;

public class ALGRequestProcessor implements Processor
{
  ObjectMapper mapper = new ObjectMapper();
  Logger log = Logger.getLogger(ALGRequestProcessor.class);
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
@Override
	public void process(Exchange exchange) throws Exception {
	 try{
		  String requestRiskAnalysis = exchange.getIn().getBody(String.class); 
				  
				  
			/*	  JsonNode responseNode =  mapper.readTree(serverConfig.getDocBYId("SamplePBAlgorithmResponse").content().toString());
		  
				  log.info("Sending generated RiskAnalaysis Request : "+requestRiskAnalysis);
				  ProfessionalRecomService prs = new ProfessionalRecomService();
				  String RiskResponse = prs.getInputRequest(requestRiskAnalysis);

					  
				  //log.info("RiskAnayalsis Response recived : "+RiskResponse);	  
				  exchange.getIn().setBody(responseNode);
		 
		  */
		  invoker invok =new invoker();
		  ObjectNode response = invoker.invoke(mapper.readTree(requestRiskAnalysis));
		  exchange.getIn().setBody(response);
		  
	  }catch(Exception e){
		 log.error("unable to get Recommender response : ",e); 
	  }
	
	}
  

  /*public static void main(String[] args) {
	  try{
		  String requestRiskAnalysis = "{ 	\"quoteParam\": { 		\"gender\": \"Male\", 		\"addressRisk\": \"HighRisk\", 		\"bikeInfo\": { 			 \"variantId\": \"BikeVarientId-45\", 			  \"registrationYear\": \"2018\",      			  \"registrationPlace\": \"MH-12 Pune\", 			  \"bikeAge\": \"3\", 			  \"bikeCC\": \"150\", 			  \"bikeIDV\":\"68000\" 		}, 		\"insuranceType\": \"All\", 		\"dailyActivity\": \"Adequte\", 		\"familyHistory \": \"High\", 		\"smoking\": false, 		\"carInfo\": { 			  \"registrationYear\": \"2015\",       \"variantId\": \"CarVarientId-411\"       \"carAge\": \"4\",       \"carCC\": \"800\",       \"carIDV\":\"290000\" 		}, 		\"homeStatus\": \"Owned\", 		\"alcoholConsumption\": \"LowConsumption\", 		\"familyMember\": \"1\", 		\"profession\": \"Doctor\", 		\"employmentType\": \"Employee\", 		\"professionCode\": \"DR\", 		\"vehicleInfo\": 2, 		\"weight\": \"Normal Weight\", 		\"driving_Travel\": \"LowRisk\", 		\"computerActivity\": \"Low\", 		\"spouseOccupation\": \"\", 		\"driver\": \"SelfDriver\", 		\"clinicStatus\": \"Owned\", 		\"annualIncomeAmt\": \"500000\", 		\"financialLiability\": \"Medium\", 		\"specialization\": \"General Physician\", 		\"age\": 29 	} }";
	  ProfessionalRecomService prs = new ProfessionalRecomService();
	  System.out.println(prs.getInputRequest(requestRiskAnalysis));
	  }catch(Exception e){
		  System.out.println("failed : ");
		  e.printStackTrace();
	  }
}*/
}
