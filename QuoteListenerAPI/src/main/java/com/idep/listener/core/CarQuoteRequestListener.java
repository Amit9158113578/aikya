package com.idep.listener.core;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.listener.services.ICarProductValidation;
import com.idep.listener.services.ICarQuoteRequestListener;
import com.idep.listener.utils.QuoteListenerConstants;

public class CarQuoteRequestListener implements ICarQuoteRequestListener{

	protected ProductMetaData productMetaData =  new ProductMetaData();
	protected CarProductValidation carProductValidation = new CarProductValidation();
	JsonObject vehicleDetails  = null;
	JsonObject occupationDetails = null;
	JsonObject rtoDetails = null;
	JsonObject exShowroomPriceDetails = null;
	protected ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarQuoteRequestListener.class.getName());
	
	@Override
	public ObjectNode preProcessing(ObjectNode input, String carrierId) throws JsonProcessingException, IOException, ParseException 
	{
		String modelCode=null;
		//set vehicle details.
		String variantId = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_VARIANTID).asText();
		log.info("variant Id :"+variantId+"carrierId :"+carrierId);
		vehicleDetails = productMetaData.getVehicleDetails(variantId, carrierId);
		if(vehicleDetails == null)
			return null;
		JsonNode vehicleDetailsNode = objectMapper.readTree(vehicleDetails.toString());
		//ObjectNode vehicleNode = objectMapper.createObjectNode();
		//log.info("Vehicle node details :"+vehicleDetailsNode);
		input.put(QuoteListenerConstants.DB_VEHICLEINFONODE, vehicleDetailsNode);
		
		//Get the details of Occupation Mapping
		String occupationId = input.get(QuoteListenerConstants.ND_QUOTEPARM).get(QuoteListenerConstants.OCCUPATION_ID).asText();
		occupationDetails = productMetaData.getOccupationDetails(occupationId, carrierId);
		JsonNode occupationDetailsNode = objectMapper.createObjectNode();
		if(occupationDetails != null)
		{
			occupationDetailsNode = objectMapper.readTree(occupationDetails.toString());
			input.put(QuoteListenerConstants.ND_OCCUPATIONINFO, occupationDetailsNode);
		}
		else
		{
			((ObjectNode)occupationDetailsNode).put("occupationCode", 0);
			input.put(QuoteListenerConstants.ND_OCCUPATIONINFO, occupationDetailsNode);
		}
		//log.info("Occupation Node Details : "+occupationDetailsNode);
		
		//Get the Details of RTO Mapping
		String rtoCode = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.RTO_CODE).asText();
		String businessLineId = input.get(QuoteListenerConstants.ND_QUOTEPARM).get(QuoteListenerConstants.UI_QUOTETYPE).asText();
		rtoDetails = productMetaData.getRTODetails(rtoCode , carrierId, businessLineId);
		JsonNode rtoDetailsNode = objectMapper.createObjectNode();
		if(rtoDetails != null)
		{
			
			rtoDetailsNode = objectMapper.readTree(rtoDetails.toString());
			input.put(QuoteListenerConstants.CARRIER_RTO_INFO, rtoDetailsNode);
		}
		//log.info("RTO Node Details : "+rtoDetailsNode);
		//Get the details of Ex-Showroom Price
		if(vehicleDetailsNode.has("modelCode"))
		{
			modelCode = vehicleDetailsNode.get("modelCode").asText();
		}
		if(rtoDetailsNode.has("stateGroupId"))
		{
			String stateGroupId = rtoDetailsNode.get("stateGroupId").asText();
			exShowroomPriceDetails = productMetaData.getExshowroomPriceDetails(carrierId, businessLineId, modelCode, stateGroupId);
			JsonNode exShowroomDetailsNode = objectMapper.createObjectNode();
			if(exShowroomPriceDetails != null)
			{
				exShowroomDetailsNode=objectMapper.readTree(exShowroomPriceDetails.toString());
				input.put(QuoteListenerConstants.CARRIER_EXSHOWROOM_INFO, exShowroomDetailsNode);
			}
			//log.info("EX-Showroom Price Node Details : "+exShowroomDetailsNode);
			//System.out.println("EX-Showroom Price Node Details : "+exShowroomDetailsNode);

		}
				
		//Checking blocked Riders
		if( input.get(QuoteListenerConstants.ND_QUOTEPARM).has(QuoteListenerConstants.ND_QUOTEPARM_RIDERS))
		{
			//log.info("isRiderBlocked Method initiated !! ");
			input = carProductValidation.isRiderBlocked(input, vehicleDetailsNode, rtoDetailsNode);
		}
		ObjectNode inputNode = calculateVehicleAge(input);
		
		return inputNode;
	}
	
	@Override
	public ObjectNode process(ObjectNode input, JsonObject vehicleDetails,String carrierId) throws JsonProcessingException, IOException{
		return input;
	}
	@Override
	public String onMessage(Message message) throws ParseException //Message message
	{
		//log.info("Quote Listener as picked up message");
		ObjectNode responseNode = objectMapper.createObjectNode();
		ObjectNode errorNode = objectMapper.createObjectNode();
		responseNode.put(QuoteListenerConstants.QUOTE_RES_CODE, QuoteListenerConstants.VALIDATIONFAILEDCODE);
		responseNode.put(QuoteListenerConstants.QUOTE_RES_MSG, QuoteListenerConstants.MSG_VALIDATIONFAIL);
		responseNode.put(QuoteListenerConstants.QUOTE_RES_DATA,errorNode);
		ObjectNode quoteRequest = null; 
		ObjectNode requestNode = null;
		ObjectNode inputNode = null;
		String carrierId = null;
		try
		 {
			
			 if ((message instanceof TextMessage))
		     {
		        TextMessage text = (TextMessage)message;
		        String request = text.getText();
		        inputNode = (ObjectNode)objectMapper.readTree(request);
		        log.debug("message details :"+inputNode);
		        requestNode = (ObjectNode)inputNode.get(QuoteListenerConstants.ND_INPUTMESSAGE);
		        responseNode.put(QuoteListenerConstants.CORRELATION_ID, inputNode.get(QuoteListenerConstants.CORRELATION_ID).asText());
		        responseNode.put(QuoteListenerConstants.QUOTE_ID, inputNode.get(QuoteListenerConstants.QUOTE_ID).asText());
		        log.debug("Actual data node :"+requestNode);
		        carrierId = requestNode.get(QuoteListenerConstants.UI_PRODUCTINFO).get(QuoteListenerConstants.UI_CARRIERID).toString();
		     }
		      log.info("Preprocssing is started using carrierId :"+carrierId);
			requestNode = preProcessing(requestNode, carrierId);
			if(requestNode == null)
			{
				log.info("Preprocessing is failed, returing validation failed message");
				return responseNode.toString();
			}
			//log.info("validation is started");
			if(validate(requestNode, carrierId))
			{	
				quoteRequest = process(requestNode, vehicleDetails, carrierId);
				if(quoteRequest == null)
				{
					log.info("Processing for this request is failed. returning validation failed message.");
					return responseNode.toString();
				}
				responseNode.put(QuoteListenerConstants.QUOTE_RES_CODE, QuoteListenerConstants.SUCCESSCODE);
				responseNode.put(QuoteListenerConstants.QUOTE_RES_MSG, QuoteListenerConstants.MSG_SUCCESS);
				responseNode.put(QuoteListenerConstants.QUOTE_RES_DATA,quoteRequest);
			}
			else
			{
				log.info("validation is failed, returning validation failed message.");
				return responseNode.toString();
			}
		}
		catch (JMSException e)
	    {
	    	log.info("JMX Exception occured, returning validation failed message");
			return responseNode.toString();
	    }
	    catch (JsonProcessingException e)
	    {
	    	log.info("Unable to process Json, returning validation failed message");
	    	return responseNode.toString();
	    }
	    catch (IOException e)
	    {
	    	log.info("IO exception, returning validation failed message");
	    	return responseNode.toString();
	    }
		
		return responseNode.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean validate(ObjectNode input, String carrierId) 
	{
		boolean validationResult = true;
		String productId = input.get(QuoteListenerConstants.UI_PRODUCTINFO).get(QuoteListenerConstants.UI_PRODUCTID).toString();
		ICarProductValidation carProdValidation = new CarProductValidation();
		JsonObject validationDetails = productMetaData.getCarValidationDetails(carrierId, productId);
		if(validationDetails == null)
		{
			log.info("No validations available for this product in the system");
			return true;
		}
		List<Map<String,Object>> validationList  = (List<Map<String,Object>>)validationDetails.toMap().get(QuoteListenerConstants.DB_VALIDATION);
		for(Map<String, Object> validation : validationList)
		{
			Integer validationId = (Integer)validation.get(QuoteListenerConstants.DB_VALIDATIONID);
			if(validationId == QuoteListenerConstants.V_BLOCKEVEHICLEBYRTO)
			{
				String businessLineId=QuoteListenerConstants.BUSINESSSLINEID;
				String rtoCode = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_RTOCODE).asText();
				JsonObject rtoDetails = productMetaData.getRTODetails(rtoCode, carrierId, businessLineId);
				if(carProdValidation.isVarientBlockedByRTO(vehicleDetails, rtoDetails))
				{
					log.info("vehicle Blocked by RTO");
					validationResult = false;
					break;
				}
			}
			else if(validationId == QuoteListenerConstants.V_BLOCKEDVEHICLE)
			{
				String variantId = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_VARIANTID).asText();
				if(carProdValidation.isVehicleBlocked(variantId, carrierId))
				{
					log.info("vehicle "+variantId+" is blocked by insurer :"+carrierId);
					validationResult = false;
					break;
				}

			}
			/*else if(validationId == QuoteListenerConstants.V_BLOCKEDRTO)
			{
				String rtoCode = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_RTOCODE).asText();
				if(carProdValidation.isRTOBlocked(rtoCode, carrierId))
				{
					log.info("RTO "+rtoCode+" is blocked by insurer :"+carrierId);
					validationResult = false;
					break;
				}
			}*/
		}
		return validationResult;
	}
	
	public ObjectNode calculateVehicleAge(ObjectNode input) throws ParseException
	{
		String monthStr = null;
		String yearStr = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String dateOfVehicleReg = input.get("vehicleInfo").get("dateOfRegistration").asText();
		Date vehicleRegDate = formatter.parse(dateOfVehicleReg);
		Date today = new Date();
		long diff = today.getTime() - vehicleRegDate.getTime();
		double n = Math.round(diff / 1000 );
		//double noDays = Math.floor(n/86400);
		int noMonth = (int) Math.floor(n/2629743);
		int noYear = (int) (n/31556926);
		int month_n = noMonth%12;
		if(Integer.toString(month_n).length()==1 )
		{
			monthStr = "0"+month_n;
		}else
		{
			monthStr=Integer.toString(month_n);
		}
		yearStr = noYear+"."+monthStr;
		double years = Double.parseDouble(yearStr);
		ObjectNode vehicleDetails = (ObjectNode) input.get("vehicleInfo");
		vehicleDetails.put("vehicleAgeinYears",years);
		vehicleDetails.put("vehicleAgeinMonths", noMonth);
		input.put("vehicleInfo",vehicleDetails);
		return input;
	}
	
	public static void main(String[] args) throws JsonProcessingException, IOException, ParseException {
		//String message = "{\"quoteParam\":{\"ncb\":25,\"personAge\":46,\"userIdv\":0,\"isAntiTheftDevice\":\"Y\",\"occupationClass\":1,\"deductible\":15000,\"isARAIMember\":\"Y\",\"policyType\":\"renew\",\"zone\":\"ZoneA\",\"cubicCapacity\":\"1298\",\"showRoomPrice\":\"780000\",\"fuelType\":\"PETROL\",\"documentType\":\"QuoteRequest\",\"quoteType\":3,\"vehicleAge\":2.45,\"policyExpiredAge\":0.0547945205479452,\"riders\":[{\"riderId\":6,\"riderName\":\"Zero Depreciation cover\",\"riderAmount\":0},{\"riderId\":37,\"riderName\":\"NCB Protection\",\"riderAmount\":0},{\"riderId\":8,\"riderName\":\"Engine Protector\",\"riderAmount\":10000,\"seatingCapacity\":5},{\"riderId\":10,\"riderName\":\"Invoice Cover\",\"riderAmount\":10000}],\"isRiderSelected\":\"Y\"},\"vehicleInfo\":{\"registrationPlace\":\"MH-12 Pune\",\"isCostal\":\"N\",\"model\":\"SWIFT DZIRE\",\"IDV\":0,\"name\":\"MARUTI\",\"fuel\":\"PETROL\",\"variant\":\"VXI\",\"previousClaim\":\"false\",\"earthQuakeArea\":\"N\",\"isAutoAssociation\":\"N\",\"regYear\":\"2013\",\"variantId\":\"CarVarientId-189\",\"seatingCapacity\":\"5\",\"RTOCode\":\"MH12\",\"dateOfRegistration\":\"01/07/2016\",\"PreviousPolicyExpiryDate\":\"11/12/2016\"},\"hitType\":\"pageview\",\"productInfo\":{\"plusAllowedOldVehicleIDV\":10,\"plusAllowedNewVehicleIDV\":5,\"carrierName\":\"Kotak Mahindra General Insurance Co. Ltd.\",\"productId\":8,\"maxAllowedIDV\":5000000.0,\"insurerIndex\":3.7,\"riderDetails\":[{\"allowedVehicleAge\":2.75,\"riderId\":6,\"dependant\":[{\"allowedVehicleAge\":7.75,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":2.75,\"riderId\":24,\"riderName\":\"Consumables cover\"}],\"riderName\":\"Zero Depreciation cover\"},{\"allowedVehicleAge\":2.75,\"riderId\":8,\"dependant\":[{\"allowedVehicleAge\":7.75,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":2.75,\"riderId\":24,\"riderName\":\"Consumables cover\"},{\"allowedVehicleAge\":2.75,\"riderId\":6,\"riderName\":\"Zero Depreciation cover\"}],\"riderName\":\"Engine Protector\"},{\"allowedVehicleAge\":7.75,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":1,\"riderId\":10,\"dependant\":[{\"allowedVehicleAge\":7.75,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":2.75,\"riderId\":24,\"riderName\":\"Consumables cover\"},{\"allowedVehicleAge\":2.75,\"riderId\":6,\"riderName\":\"Zero Depreciation cover\"}],\"riderName\":\"Invoice Cover\"},{\"allowedVehicleAge\":7.75,\"riderId\":20,\"riderName\":\"Driver Accident Cover\"},{\"allowedVehicleAge\":7.75,\"riderId\":21,\"riderName\":\"Passanger Accident Cover\"},{\"allowedVehicleAge\":2.75,\"riderId\":24,\"dependant\":[{\"allowedVehicleAge\":7.75,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":2.75,\"riderId\":6,\"riderName\":\"Zero Depreciation cover\"}],\"riderName\":\"Consumables cover\"},{\"allowedVehicleAge\":7.75,\"riderId\":25,\"riderName\":\"Electrical Accessories cover\"},{\"allowedVehicleAge\":7.75,\"riderId\":30,\"riderName\":\"Non-Electrical Accessories cover\"},{\"allowedVehicleAge\":7.75,\"riderId\":35,\"riderName\":\"LPG/CNG Kit Rider\"}],\"insuranceType\":1,\"minusAllowedNewVehicleIDV\":5,\"carrierId\":53,\"maxAllowedVehicleAge\":8,\"minusAllowedOldVehicleIDV\":10}}";
		//log.info("Response :"+new CarQuoteRequestListener().onMessage(message));
		String message = "{\"quoteParam\":{\"personAge\":46,\"occupationClass\":1,\"isAntiTheftDevice\":\"Y\",\"occupationId\":1,\"zone\":\"ZoneA\",\"policyType\":\"renew\",\"riders\":[{\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\",\"riderAmount\":0},{\"riderId\":6,\"riderName\":\"Zero Depreciation cover\",\"riderAmount\":0},{\"riderId\":8,\"riderName\":\"Engine Protector\",\"riderAmount\":10000,\"seatingCapacity\":5},{\"riderId\":10,\"riderName\":\"Invoice Cover\",\"riderAmount\":10000}],\"deductible\":5000,\"isARAIMember\":\"Y\",\"userIdv\":0,\"ncb\":25,\"cubicCapacity\":\"1120\",\"showRoomPrice\":\"631473\",\"policyExpiredAge\":0.0547945205479452,\"fuelType\":\"DIESEL\",\"vehicleAge\":3.05,\"documentType\":\"QuoteRequest\",\"quoteType\":3},\"vehicleInfo\":{\"earthQuakeArea\":\"N\",\"regYear\":\"2013\",\"previousClaim\":\"false\",\"fuel\":\"DIESEL\",\"isAutoAssociation\":\"N\",\"name\":\"HYUNDAI MOTORS\",\"variant\":\"2nd Gen 1.1 U2 CRDi 5-Speed Manual Prime (M) Sportz Edition\",\"registrationPlace\":\"MH-01 MumbaiTardeo\",\"IDV\":0,\"model\":\"GRANDE i10\",\"isCostal\":\"N\",\"variantId\":\"CarVarientId-169\",\"seatingCapacity\":5,\"RTOCode\":\"TN29\",\"PreviousPolicyExpiryDate\":\"28/12/2016\",\"dateOfRegistration\":\"01/07/2013\"},\"hitType\":\"pageview\",\"productInfo\":{\"productId\":10,\"maxAllowedIDV\":500000,\"insuranceType\":1,\"minusAllowedNewVehicleIDV\":10,\"productName\":\"Private Car Insurance\",\"maxAllowedVehicleAge\":10,\"plusAllowedOldVehicleIDV\":10,\"plusAllowedNewVehicleIDV\":10,\"carrierName\":\"HDFC ERGO General Insurance Company\",\"insurerIndex\":4.33078256713485,\"riderDetails\":[{\"allowedVehicleAge\":10,\"riderId\":9,\"riderName\":\"24X7 Road Side Assistance\"},{\"allowedVehicleAge\":10,\"riderId\":6,\"planType\":\"Silver\",\"riderName\":\"Zero Depreciation cover\"},{\"allowedVehicleAge\":10,\"riderId\":36,\"planType\":\"Gold\",\"riderName\":\"DownTime Protection\"},{\"allowedVehicleAge\":10,\"riderId\":7,\"planType\":\"Platinum\",\"riderName\":\"NCB Protection\"},{\"allowedVehicleAge\":10,\"riderId\":8,\"planType\":\"Platinum\",\"riderName\":\"Engine Protector\"},{\"allowedVehicleAge\":3,\"riderId\":10,\"riderName\":\"Invoice Cover\"},{\"allowedVehicleAge\":10,\"riderId\":24,\"planType\":\"Titanium\",\"riderName\":\"Consumables cover\"},{\"allowedVehicleAge\":10,\"riderId\":20,\"riderName\":\"Driver Accident Cover\"},{\"allowedVehicleAge\":10,\"riderId\":21,\"riderName\":\"Passanger Accident Cover\"},{\"allowedVehicleAge\":10,\"riderId\":35,\"riderName\":\"LPG-CNG Kit\"},{\"allowedVehicleAge\":10,\"riderId\":25,\"riderName\":\"Electrical Accessories cover\"},{\"allowedVehicleAge\":10,\"riderId\":30,\"riderName\":\"Non-Electrical Accessories cover\"}],\"carrierId\":28,\"minusAllowedOldVehicleIDV\":10},\"carrierVehicleInfo\":{\"makeCode\":49,\"modelCode\":28788,\"variantCode\":null,\"cubicCapacity\":1197,\"seatingCapacity\":5,\"make\":\"HYUNDAI\",\"model\":\"GRAND I10\",\"variant\":\"1.2 PRIME SPORTZ EDITION\",\"vehicleBlocked\":null,\"fuelType\":\"PETROL\",\"blockedRiders\":\"10,9\"},\"occupationInfo\":{\"occupationCode\":0},\"carrierRTOInfo\":{\"State_Code\":14,\"rtoLocationCode\":10406,\"ExShowRoom_Mapping_Location\":\"MUMBAI\",\"documentType\":\"RTOMapping\",\"Registration_Zone\":\"A\",\"State_Name\":\"MAHARASHTRA\",\"rtoLocation\":\"MUMBAI\",\"blockedRiders\":\"\",\"carrierId\":47,\"Rate_Mapping_Location\":\"MUMBAI\"}}";
		String carrierId = "47";
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode input = (ObjectNode) objectMapper.readTree(message);
		CarQuoteRequestListener list = new CarQuoteRequestListener();
		list.preProcessing(input, carrierId);
	}
}
