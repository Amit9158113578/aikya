/**
 * 
 */
package com.idep.listener.core;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.listener.services.IBikeProductValidation;
import com.idep.listener.utils.QuoteListenerConstants;

/**
 * @author vipin.patil
 *
 */
public class BikeProductValidation implements IBikeProductValidation{
	
	Logger log = Logger.getLogger(BikeProductValidation.class.getName());
	ProductMetaData productMetaData = new ProductMetaData();

	@Override
	public boolean isRTOBlocked(String rtoCode, String carrierId) {
		JsonObject rtoDetails = productMetaData.getBlockedRTODetails(rtoCode);
		if(rtoDetails != null  && rtoDetails.containsKey(carrierId))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean isVehicleBlocked(JsonObject vehicleDetails,
			String variantId, String carrierId) {
		if(vehicleDetails == null)
		{
			vehicleDetails = productMetaData.getVehicleDetails(variantId, carrierId);
		}
		if(vehicleDetails.containsKey(QuoteListenerConstants.VEHICLEBLOCKEDKEY))
		{
			if(vehicleDetails.get(QuoteListenerConstants.VEHICLEBLOCKEDKEY).toString().equalsIgnoreCase("Y"))
			{
				return true;
			}
		}
	     return false;
	}

	@Override
	public ObjectNode isRiderBlocked(ObjectNode input,
			JsonNode vehicleDetailsNode, JsonNode rtoDetailsNode) {
		// TODO Auto-generated method stub
		//log.info("Inside isRiderBlocked method");
		String blockedRider = "";
		String rtoBlockedRiderIds = "";
		String vehicleBlockedRiderIds = "";
		if(vehicleDetailsNode.has(QuoteListenerConstants.BLOCK_RIDERS) && vehicleDetailsNode.get(QuoteListenerConstants.BLOCK_RIDERS).asText().trim().length() >= 1)
		{
			vehicleBlockedRiderIds = vehicleDetailsNode.get(QuoteListenerConstants.BLOCK_RIDERS).asText();
			blockedRider = vehicleBlockedRiderIds+",";
			//log.info("Vehicle Blocked Riders : "+vehicleBlockedRiderIds);
		}
		if(rtoDetailsNode!=null)
		{
			if(rtoDetailsNode.has(QuoteListenerConstants.BLOCK_RIDERS) && rtoDetailsNode.get(QuoteListenerConstants.BLOCK_RIDERS).asText().trim().length() >= 1)
			{
				rtoBlockedRiderIds = rtoDetailsNode.get(QuoteListenerConstants.BLOCK_RIDERS).asText();
				blockedRider = blockedRider+rtoBlockedRiderIds;
				//log.info("RTO Blocked Riders : "+rtoBlockedRiderIds);
			}
		}
		//log.info("Blocked Riders : "+blockedRider);
		if(blockedRider.equals(""))
		{
			return input;
		}
		if(blockedRider.endsWith(","))
		{
			blockedRider = blockedRider.substring(0, blockedRider.length() - 1);
		}
		String[] blockedRiderIds = blockedRider.split(",");
		ArrayNode requestedRiders = (ArrayNode)input.get(QuoteListenerConstants.ND_QUOTEPARM).get(QuoteListenerConstants.ND_QUOTEPARM_RIDERS);
		for(int j=0;j<requestedRiders.size();j++)
		{
			JsonNode rider = requestedRiders.get(j);
			for(String riderId : blockedRiderIds)
			{
				if(rider.get(QuoteListenerConstants.ND_QUOTEPARM_RIDER_ID).toString().equals(riderId))
				{
					//log.info("Remove Riders : "+riderId);
					requestedRiders.remove(j);
				}
			}
		}
		//log.info("Input Request : "+input);
		return input;
	}
	


	@Override
	public boolean isSegmentTypeBlock(String isBlocked, String segemntType,
			String isAllow) {
		// TODO Auto-generated method stub
		if(isBlocked.equals("N") && !isAllow.equals("All"))
		{
			String moped = isAllow.substring(8, 13);
			String scooter = isAllow.substring(0, 7);
			if(!segemntType.contains(scooter) && !segemntType.contains(moped))
		      {
				return true;
		      }
	     }
		return false;
		
	}
	 public String validateBikeProduct(JsonNode product, JsonNode quoteParamNode, JsonNode vehicleParamNode) {
		    String validProductFlag = "failure";
		    try {
		      validProductFlag = validateVehicleAge(quoteParamNode.get("vehicleAge").doubleValue(), product.get("maxAllowedVehicleAge").doubleValue());
		      if (validProductFlag.equalsIgnoreCase("success")) {
		        if (product.has("policyTypeSupport"))
		          validProductFlag = validatePolicyType(quoteParamNode.get("policyType").asText(), product.get("policyTypeSupport")); 
		        if (validProductFlag.equalsIgnoreCase("success"))
		          if (vehicleParamNode.has("previousPolicyExpired"))
		            validProductFlag = isExpiredPolicyAllowed(vehicleParamNode.get("previousPolicyExpired").textValue(), product.get("isExpiredPolicyAllowed").textValue());  
		      } else {
		        return "vehicle age is not supported :";
		      } 
		      return validProductFlag;
		    } catch (Exception e) {
		      e.printStackTrace();
		      return validProductFlag;
		    } 
		  }
	 public String validateVehicleAge(double vehicleAge, double maxAllowedVehicleAge) {
		    if (vehicleAge <= maxAllowedVehicleAge)
		      return "success"; 
		    return "calculate quote failure for insurer";
		  }
		  
		  public String validatePolicyType(String policyType, JsonNode policyTypeSupport) {
		    String flag = "calculate quote failure for insurer";
		    for (JsonNode prodPolicyType : policyTypeSupport) {
		      if (prodPolicyType.get("policyType").asText().equalsIgnoreCase(policyType)) {
		        flag = "success";
		        break;
		      } 
		      flag = "calculate quote failure for insurer";
		    } 
		    return flag;
		  }
		  
		  public String isExpiredPolicyAllowed(String userSelectedFlag, String productExpiredPolicyFlag) {
			    if (productExpiredPolicyFlag.equals("N")) {
			      if (userSelectedFlag.equals(productExpiredPolicyFlag))
			        return "success"; 
			      return "calculate quote failure for insurer";
			    } 
			    return "success";
			  }
}

