package com.idep.service.vehicleinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class GetOurMasterData implements Processor {

	Logger log = Logger.getLogger(GetOurMasterData.class.getName());
	 ObjectMapper objectMapper = new ObjectMapper();
	static CBService service = CBInstanceProvider.getServerConfigInstance();
	static ArrayNode P365CarMasterArray;
	static ArrayNode P365BikeMasterArray;

	
	static {
		Logger slog = Logger.getLogger(GetOurMasterData.class.getName());
		ObjectMapper staticOM = new ObjectMapper();
		JsonNode RegistrationMasterAPIConfig = null;
		try {
			 RegistrationMasterAPIConfig = staticOM.readTree(service.getDocBYId("RegistrationMasterAPIConfig").content().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String P365CarQuery = RegistrationMasterAPIConfig.get("P365CarQuery").textValue();
		String P365BikeQuery = RegistrationMasterAPIConfig.get("P365BikeQuery").textValue();
		List<Map<String, Object>> executeQueryCar = service.executeQuery(P365CarQuery);
		P365CarMasterArray = staticOM.convertValue(executeQueryCar ,ArrayNode.class);
		List<Map<String, Object>> executeQuery = service.executeQuery(P365BikeQuery);
		P365BikeMasterArray = staticOM.convertValue(executeQuery ,ArrayNode.class);
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		
		
		try{
			String input = exchange.getIn().getBody(String.class);
			
			JsonNode carInfo = objectMapper.readTree(input);
			String carMake = null;
			String carModel = null;
			String description = null;
			String fuelType = null;
			String cubicCapacity = null;
			String registrationYear = null;
			String registrationDate = null;
			String variant=null;
			JsonNode UIResponse = objectMapper.createObjectNode();
			JsonNode Response = null;
			log.debug("carInfo : "+carInfo);
			if(carInfo.has("carDetails")){
				carMake = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.CARMAKE).asText();
				carModel = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.CARMODEL).asText();
				description = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.DESCRIPTION).asText();
				fuelType = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.FUELTYPE).asText();
				cubicCapacity = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.ENGINESIZE).asText();
				registrationYear = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.REGISTRATIONYEAR).asText();
				registrationDate = carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.REGDATE).asText();
			}else{
				carMake = carInfo.get(VehicleInfoConstant.CARMAKE).asText();
				carModel = carInfo.get(VehicleInfoConstant.CARMODEL).asText();
				description = carInfo.get(VehicleInfoConstant.DESCRIPTION).asText();
				fuelType = carInfo.get(VehicleInfoConstant.FUELTYPE).asText();
				cubicCapacity = carInfo.get(VehicleInfoConstant.ENGINESIZE).asText();
				registrationYear = carInfo.get(VehicleInfoConstant.REGISTRATIONYEAR).asText();
				registrationDate = carInfo.get(VehicleInfoConstant.REGDATE).asText();
			}
		
			if(description!=null && description.length()>0){
			   variant=description;
			   log.info("variant :"+variant);
				 variant = variant.replaceAll(carMake, "").replaceAll(carModel, "").replaceAll("/", "").replaceAll(fuelType, "").replaceAll(cubicCapacity, "").trim();
				log.info("After replace variant :"+variant);
				 if(variant.length()>0){
				
				}else{
					variant=carModel;
					log.info("registration api car model assign to variant :"+variant);
				}
			}
			if(fuelType==null || fuelType.equalsIgnoreCase(""))
			{
				fuelType="PETROL";
			}
			
			JsonNode RegistrationMasterAPIConfig = objectMapper.readTree(service.getDocBYId("RegistrationMasterAPIConfig").content().toString());
			
			
			String lob = exchange.getProperty("lob").toString();
			if(lob.equals("car"))
			{
			if(RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.MAKE).has(carMake))
			{
				carMake=RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.MAKE).get(carMake).textValue();
				log.info("Replace car make :"+carMake);
			}
			if(RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.MODEL).has(carModel))
			{
				carModel=RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.MODEL).get(carModel).textValue();
			    log.info("Replace car model :"+carModel);
			}
			if(RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.FUELTYPE).has(fuelType))
			{
				fuelType=RegistrationMasterAPIConfig.get("Car").get(VehicleInfoConstant.FUELTYPE).get(fuelType).textValue();
			    log.info("Replace car fuelType :"+fuelType);
			}
			
			for (JsonNode p365Node : P365CarMasterArray) 
				
			{
				String p365make = p365Node.get(VehicleInfoConstant.MAKE).textValue();
				String p365mode = p365Node.get(VehicleInfoConstant.MODEL).textValue();
				String p365FuelType = p365Node.get(VehicleInfoConstant.UFUELTYPE).textValue();
                if(carMake.toLowerCase().equalsIgnoreCase(p365make.toLowerCase()) && carModel.toLowerCase().equalsIgnoreCase(p365mode.toLowerCase())&& fuelType.toLowerCase().equalsIgnoreCase(p365FuelType.toLowerCase()))
               {
            	   log.info("car make and model match case :"+carMake+" :"+carModel+" :"+fuelType);
            	   JsonNode variantArrayNode = p365Node.get(VehicleInfoConstant.VARIANT);
            	   for (JsonNode variantNode : variantArrayNode) {
					if(variantNode.get(VehicleInfoConstant.VARIANT).textValue().toLowerCase().equalsIgnoreCase(variant.toLowerCase()))
					{
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANTID, variantNode.get(VehicleInfoConstant.VARIANTID).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANT, variantNode.get(VehicleInfoConstant.VARIANT).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.DISPLAYVEHICLE, variantNode.get(VehicleInfoConstant.DISPLAYVEHICLE).asText());
					}
					if(variantNode.get(VehicleInfoConstant.P365MODEL_VARIANT).textValue().toLowerCase().equalsIgnoreCase(variant.toLowerCase()))
					{
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANTID, variantNode.get(VehicleInfoConstant.VARIANTID).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANT, variantNode.get(VehicleInfoConstant.VARIANT).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.DISPLAYVEHICLE, variantNode.get(VehicleInfoConstant.DISPLAYVEHICLE).asText());
					}
				  }
            	   if(!UIResponse.has(VehicleInfoConstant.VARIANTID) &&!UIResponse.has((VehicleInfoConstant.VARIANT)))
            	   {
            		   log.info("registration API and P365 Master variant not match API Variant :"+variant);
            		   exchange.setProperty("p365CarVariantMaster", variantArrayNode);
            	   }
            	   ((ObjectNode)UIResponse).put(VehicleInfoConstant.MODEL, p365Node.get(VehicleInfoConstant.MODEL).asText());
				   ((ObjectNode)UIResponse).put(VehicleInfoConstant.UMAKE, p365Node.get(VehicleInfoConstant.MAKE).asText());
				   ((ObjectNode)UIResponse).put(VehicleInfoConstant.UFUELTYPE, p365Node.get(VehicleInfoConstant.UFUELTYPE).asText());
				   ((ObjectNode)UIResponse).put("registrationYear", registrationYear);
				   ((ObjectNode)UIResponse).put("registrationDate", registrationDate);
				    Response=objectMapper.createObjectNode();
				   ((ObjectNode)Response).put("documentType",VehicleInfoConstant.DOCUMENT_TYPE);
				   ((ObjectNode)Response).put(VehicleInfoConstant.UIRESPONSE, UIResponse);
				   ((ObjectNode)Response).put(VehicleInfoConstant.CAR_REG_API_RESPONSE, carInfo);
					exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1000);
               }
			}
			log.info("car output :"+Response);
		}
		if(lob.equals("bike"))
		{
			if(RegistrationMasterAPIConfig.get("Bike").get(VehicleInfoConstant.MAKE).has(carMake))
			{
				carMake=RegistrationMasterAPIConfig.get("Bike").get(VehicleInfoConstant.MAKE).get(carMake).textValue();
				log.info("Replace bike make :"+carMake);
			}
			if(RegistrationMasterAPIConfig.get("Bike").get(VehicleInfoConstant.MODEL).has(carModel))
			{
				carModel=RegistrationMasterAPIConfig.get("Bike").get(VehicleInfoConstant.MODEL).get(carModel).textValue();
			    log.info("Replace bike model :"+carModel);
			}
			for (JsonNode p365Node : P365BikeMasterArray) 
				
			{
				String p365make = p365Node.get(VehicleInfoConstant.MAKE).textValue();
				String p365mode = p365Node.get(VehicleInfoConstant.MODEL).textValue();
                if(carMake.toLowerCase().equalsIgnoreCase(p365make.toLowerCase()))
               {
            	   JsonNode variantArrayNode = p365Node.get(VehicleInfoConstant.VARIANT);
            	   for (JsonNode variantNode : variantArrayNode) {
					if(carModel.toLowerCase().equalsIgnoreCase(p365mode.toLowerCase()) && variantNode.get(VehicleInfoConstant.VARIANT).textValue().toLowerCase().equalsIgnoreCase(variant.toLowerCase()))
					{
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANTID, variantNode.get(VehicleInfoConstant.VARIANTID).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANT, variantNode.get(VehicleInfoConstant.VARIANT).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.DISPLAYVEHICLE, variantNode.get(VehicleInfoConstant.DISPLAYVEHICLE).asText());
					}
					if(variantNode.get(VehicleInfoConstant.P365MODEL_VARIANT).textValue().toLowerCase().equalsIgnoreCase(carModel.toLowerCase()))
					{
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANTID, variantNode.get(VehicleInfoConstant.VARIANTID).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANT, variantNode.get(VehicleInfoConstant.VARIANT).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.DISPLAYVEHICLE, variantNode.get(VehicleInfoConstant.DISPLAYVEHICLE).asText());
					}
					 description= description.replace(carInfo.get(VehicleInfoConstant.CARDETAILS).get(VehicleInfoConstant.CARMAKE).asText(), carMake).replace("/", " ");
					if(variantNode.get(VehicleInfoConstant.P365MAKE_MODEL_VARIANT).textValue().toLowerCase().equalsIgnoreCase(description.toLowerCase()))
					{
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANTID, variantNode.get(VehicleInfoConstant.VARIANTID).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.VARIANT, variantNode.get(VehicleInfoConstant.VARIANT).asText());
						((ObjectNode)UIResponse).put(VehicleInfoConstant.DISPLAYVEHICLE, variantNode.get(VehicleInfoConstant.DISPLAYVEHICLE).asText());
					}
				  }
            	   if(!UIResponse.has(VehicleInfoConstant.VARIANTID) &&!UIResponse.has((VehicleInfoConstant.VARIANT)))
            	   {
            		   log.info("registration API and P365 Master variant not match API Variant for Bike :"+variant);
            		   exchange.setProperty("p365BikeVariantMaster", variantArrayNode);
            	   }
				   ((ObjectNode)UIResponse).put(VehicleInfoConstant.UMAKE, p365Node.get(VehicleInfoConstant.MAKE).asText());
				   ((ObjectNode)UIResponse).put("registrationYear", registrationYear);
				   ((ObjectNode)UIResponse).put("registrationDate", registrationDate);
				    Response=objectMapper.createObjectNode();
				   ((ObjectNode)Response).put("documentType",VehicleInfoConstant.DOCUMENT_TYPE);
				   ((ObjectNode)Response).put(VehicleInfoConstant.UIRESPONSE, UIResponse);
				   ((ObjectNode)Response).put(VehicleInfoConstant.CAR_REG_API_RESPONSE, carInfo);
					exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1000);
               }
			}
			log.info("bike output :"+Response);
		}
			if(Response==null)
             {
          	     log.error("vehicle make and model details not found in P365 master for Make :"+carMake+" Model :"+carModel+" fuelType :"+fuelType);
          		 exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1001);
          		 Response=objectMapper.createObjectNode();
          		((ObjectNode)Response).put(VehicleInfoConstant.CAR_REG_API_RESPONSE, carInfo);
  				((ObjectNode)Response).put("message","vehicle details not found");
             }
			 exchange.getIn().setBody(Response);
		}catch(Exception e){
			ObjectNode responseNode = objectMapper.createObjectNode();
			exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1001);
			  responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, VehicleInfoConstant.VEHICLE_RES_FAILED_CODE);
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "failure");
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, "Vehilce Details not found");
				exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
			log.error("Error at GetOurMasterData :",e);			
		}
		
		

	}

}
