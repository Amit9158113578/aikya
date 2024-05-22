/**
 * 
 */
package com.idep.listener.core;

import java.io.IOException;
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
import com.idep.listener.services.IBikeProductValidation;
import com.idep.listener.services.IBikeQuoteRequestListener;
import com.idep.listener.utils.QuoteListenerConstants;

/**
 * @author vipin.patil
 *
 */
public class BikeQuoteRequestListener implements IBikeQuoteRequestListener{
	
	protected ProductMetaData productMetaData =  new ProductMetaData();
	protected BikeProductValidation bikeProductValidation = new BikeProductValidation();
	
	
	
	JsonObject vehicleDetails  = null;
	JsonObject occupationDetails = null;
	JsonObject rtoDetails = null;
	JsonObject exShowroomPriceDetails = null;
	protected ObjectMapper objectMapper = new ObjectMapper();
	JsonNode occupationDetailsNode = objectMapper.createObjectNode();
	Logger log = Logger.getLogger(BikeQuoteRequestListener.class.getName());
	@Override
	public ObjectNode preProcessing(ObjectNode input, String carrierId)
			throws JsonProcessingException, IOException {
		String modelCode=null;
		String variantId = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_VARIANTID).asText();
		//log.info("variant Id :"+variantId);
		//log.info("carrierId :"+carrierId);
		vehicleDetails = productMetaData.getVehicleDetails(variantId, carrierId);
		if(vehicleDetails == null)
			return null;
		JsonNode vehicleDetailsNode = objectMapper.readTree(vehicleDetails.toString());
		//ObjectNode vehicleNode = objectMapper.createObjectNode();
		
		//log.info("Vehicle node details :"+vehicleDetailsNode);
		
		input.put(QuoteListenerConstants.DB_VEHICLEINFONODE, vehicleDetailsNode);
		
		//Get the details of Occupation Mapping
		if(input.get(QuoteListenerConstants.ND_QUOTEPARM).has(QuoteListenerConstants.OCCUPATION_ID))
		{
		String occupationId = input.get(QuoteListenerConstants.ND_QUOTEPARM).get(QuoteListenerConstants.OCCUPATION_ID).asText();
		occupationDetails = productMetaData.getOccupationDetails(occupationId, carrierId);
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
		}
		JsonNode exShowroomDetailsNode = objectMapper.createObjectNode();
		if(exShowroomPriceDetails != null)
		{
			exShowroomDetailsNode=objectMapper.readTree(exShowroomPriceDetails.toString());
			input.put(QuoteListenerConstants.CARRIER_EXSHOWROOM_INFO, exShowroomDetailsNode);
		}
		//log.info("EX-Showroom Price Node Details : "+exShowroomDetailsNode);
		
		if( input.get(QuoteListenerConstants.ND_QUOTEPARM).has(QuoteListenerConstants.ND_QUOTEPARM_RIDERS))
		{
			//log.info("isRiderBlocked Method initiated !! ");
			input = bikeProductValidation.isRiderBlocked(input, vehicleDetailsNode, rtoDetailsNode);
		}
		
		return input;
	}
	
	
	@Override
	public boolean validate(ObjectNode input, String carrierId) {
		boolean validationResult = true;
		String productId = input.get(QuoteListenerConstants.UI_PRODUCTINFO).get(QuoteListenerConstants.UI_PRODUCTID).toString();
		IBikeProductValidation bikeProdValidation = new BikeProductValidation();
		
		JsonObject validationDetails = productMetaData.getValidationDetails(carrierId, productId);
		//comment after disc
		if(validationDetails == null)
		{
			log.info("No validations available for this product in the system");
			return true;
		}
		@SuppressWarnings("unchecked")
		
		List<Map<String,Object>> validationList  = (List<Map<String,Object>>)validationDetails.toMap().get(QuoteListenerConstants.DB_VALIDATION);
		for(Map<String, Object> validation : validationList)
		{
			Integer validationId = (Integer)validation.get(QuoteListenerConstants.DB_VALIDATIONID);
			if(validationId == QuoteListenerConstants.V_BLOCKEDVEHICLE)
			{
				String variantId = input.get(QuoteListenerConstants.ND_VEHICLEINFO).get(QuoteListenerConstants.UI_VARIANTID).asText();
				if(bikeProdValidation.isVehicleBlocked(vehicleDetails,variantId, carrierId))
				{
					log.info("vehicle "+variantId+" is blocked by insurer :"+carrierId);
					validationResult = false;
					break;
				}
				
				String TXT_SEGMENTTYPE = input.get(QuoteListenerConstants.DB_VEHICLEINFONODE).get("TXT_SEGMENTTYPE").textValue();
				String isBlocked = rtoDetails.get("isBlocked").toString();
				String isAllowed = rtoDetails.get("isAllowed").toString();
				if(bikeProdValidation.isSegmentTypeBlock(isBlocked, TXT_SEGMENTTYPE, isAllowed))
				{
					log.info("SEGMENT TYPE "+TXT_SEGMENTTYPE+" is blocked by insurer :"+carrierId);
					validationResult = false;
					break;
				}
			}
			else if(validationId == QuoteListenerConstants.V_BLOCKEDRTO)
			{
				if(input.get(QuoteListenerConstants.CARRIER_RTO_INFO).has("isBlocked"))
				{
					if(input.get(QuoteListenerConstants.CARRIER_RTO_INFO).get("isBlocked").textValue().equals("Y"))
					{
						log.info("RTO "+input.get(QuoteListenerConstants.CARRIER_RTO_INFO).get("regisCode").toString()+" is blocked by insurer :"+carrierId);
						validationResult = false;
					}
				}
			}
		}
		if( input.get(QuoteListenerConstants.ND_VEHICLEINFO).has("previousPolicyExpired"))
		{
			if(input.get(QuoteListenerConstants.ND_VEHICLEINFO).get("previousPolicyExpired").textValue().equals("Y"))
			{
			//log.info("isRiderBlocked Method initiated !! ");
				if(input.get(QuoteListenerConstants.CARRIER_RTO_INFO).has("isPolicyExpired"))
				{
					if(!input.get(QuoteListenerConstants.CARRIER_RTO_INFO).get("isPolicyExpired").isNull() || input.get(QuoteListenerConstants.CARRIER_RTO_INFO).get("isPolicyExpired").textValue()!="")
					{
					   if(input.get(QuoteListenerConstants.CARRIER_RTO_INFO).get("isPolicyExpired").textValue().equals("Y"))
					     {
					    	log.info("Kotak Bike is policy Expired Validation :");
					    	validationResult = false;
					     }
					}
				}
			}
			
		}
		return validationResult;
	}
	
	@Override
	public ObjectNode process(ObjectNode input, JsonObject vehicleDetails, String carrierId) throws JsonProcessingException, IOException {
		return input;
	}
	@Override
	public String onMessage(Message message) {
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
		        //log.info("message details :"+inputNode);
		        requestNode = (ObjectNode)inputNode.get(QuoteListenerConstants.ND_INPUTMESSAGE);
		        responseNode.put(QuoteListenerConstants.CORRELATION_ID, inputNode.get(QuoteListenerConstants.CORRELATION_ID).asText());
		        responseNode.put(QuoteListenerConstants.QUOTE_ID, inputNode.get(QuoteListenerConstants.QUOTE_ID).asText());
		        //log.info("Actual data node :"+requestNode);
		        carrierId = requestNode.get(QuoteListenerConstants.UI_PRODUCTINFO).get(QuoteListenerConstants.UI_CARRIERID).toString();
		     }
			//log.info("Preprocssing is started");
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
	    	log.error("JMX Exception occured, returning validation failed message");
			return responseNode.toString();
	    }
	    catch (JsonProcessingException e)
	    {
	    	log.error("Unable to process Json, returning validation failed message");
	    	return responseNode.toString();
	    }
	    catch (IOException e)
	    {
	    	log.error("IO exception, returning validation failed message");
	    	return responseNode.toString();
	    }
		
		return responseNode.toString();
	}

}
