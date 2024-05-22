package com.idep.insassessment.invoke;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.insassessment.InputRequestParsing;

public class invoker {
	//	static InputRequestParsing reqParsing = new InputRequestParsing();
	ObjectMapper objectMapper = new ObjectMapper();



	public static void main(String args[]) 
	{
		//InputRequestParsing reqParsing = new InputRequestParsing();

		Logger log = Logger.getLogger(invoker.class.getName());
		String request = "{\"quoteParam\":{\"addressRisk\":\"Low\",\"gender\":\"male\",\"bikeInfo\":{\"bikeCCRange\":\"110\",\"bikeIDV\":24250,\"bikeNcbPercentage\":50,\"bikeVehicleAgeRange\":4.37},\"vehicleAgeRange\":\"2\",\"insuranceType\":\"All\",\"dailyActivity\":\"Adequte\",\"smoking\":\"Non-Smoker\",\"familyHistory\":\"High\",\"vehicleCCRange\":\"125\",\"carInfo\":{\"carNcbPercentage\":\"\",\"carVehicleAgeRange\":\"\",\"carCCRange\":\"\",\"carIDV\":\"\"},\"homeStatus\":\"Owned\",\"alcoholConsumption\":\"LowConsumption\",\"professionId\":\"1\",\"familyMember\":\"3\",\"profession\":\"Doctor\",\"employmentType\":\"\",\"professionCode\":\"DR\",\"vehicleInfo\":\"1\",\"weight\":\"Normal Weight\",\"driving_Travel\":\"Low\",\"computerActivity\":\"Low\",\"spouseOccupation\":\"HouseWife\",\"driver\":\"SelfDriver\",\"annualIncomeAmt\":\"500000\",\"financialLiability\":\"Medium\",\"specialization\":\"Opthalmologist\",\"clinic\":\"\",\"age\":\"63\"}}{\"quoteParam\":{\"addressRisk\":\"Low\",\"gender\":\"male\",\"bikeInfo\":{\"bikeCCRange\":\"110\",\"bikeIDV\":24250,\"bikeNcbPercentage\":50,\"bikeVehicleAgeRange\":4.37},\"vehicleAgeRange\":\"2\",\"insuranceType\":\"All\",\"dailyActivity\":\"Adequte\",\"smoking\":\"Non-Smoker\",\"familyHistory\":\"High\",\"vehicleCCRange\":\"125\",\"carInfo\":{\"carNcbPercentage\":\"\",\"carVehicleAgeRange\":\"\",\"carCCRange\":\"\",\"carIDV\":\"\"},\"homeStatus\":\"Owned\",\"alcoholConsumption\":\"LowConsumption\",\"professionId\":\"1\",\"familyMember\":\"3\",\"profession\":\"Doctor\",\"employmentType\":\"\",\"professionCode\":\"DR\",\"vehicleInfo\":\"1\",\"weight\":\"Normal Weight\",\"driving_Travel\":\"Low\",\"computerActivity\":\"Low\",\"spouseOccupation\":\"HouseWife\",\"driver\":\"SelfDriver\",\"annualIncomeAmt\":\"1800000\",\"financialLiability\":\"Medium\",\"specialization\":\"Opthalmologist\",\"clinic\":\"\",\"age\":\"63\"}}";
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode response = objectMapper.createObjectNode();
		invoker invoke = new invoker();
		try {
			//invoke.invoke(request);
			JsonNode inputRequestNode = objectMapper.readTree(request);
			invoke.invoke(inputRequestNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public ObjectNode invoke(JsonNode request) throws JsonProcessingException, IOException{
		InputRequestParsing reqParsing = new InputRequestParsing();
		ObjectNode response = objectMapper.createObjectNode();
		//System.out.println("Request:::::: "+request);
		response = reqParsing.inputRequestParsing(request);
		//System.out.println("Response:::::: "+response);
		return response;
	}
}