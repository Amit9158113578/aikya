package com.idep.healthquote.req.processor;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ProductValidationImpl {
	
	Logger log = Logger.getLogger(ProductValidationImpl.class.getName());
	public long sumInsured = 0;
	
	public boolean validateProduct(JsonNode product,JsonNode quoteParamNode,ObjectNode sumInsuredNode,JsonNode diseaseList,JsonNode HDFCPinCodeList)
	{
		boolean validProductFlag = false;
		
		try
		{
				// validate family product
				if(quoteParamNode.get("planType").textValue().equals("F"))
				{
					if(product.get("planType").textValue().equals("F"))
					{
						validProductFlag = validateFamilyProduct(product,quoteParamNode,sumInsuredNode,diseaseList,HDFCPinCodeList);
						return validProductFlag;
					}
					else
					{
						return validProductFlag;
					}
				}
				// validate individual product
				if(quoteParamNode.get("planType").textValue().equals("I"))
				{
					if(product.get("planType").textValue().equals("I"))
					{
						validProductFlag = validateIndividualProduct(product,quoteParamNode,sumInsuredNode,diseaseList,HDFCPinCodeList);
						return validProductFlag;
					}
					else
					{
						return validProductFlag;
					}
				}
				else
				{
					return validProductFlag;
				}
				
		}
		catch(Exception e)
		{
			log.error("Exception at ProductValidationImpl : ", e);
			return validProductFlag;
			
		}
		
		
	}
	
	public boolean validateFamilyProduct(JsonNode product,JsonNode quoteParamNode,ObjectNode sumInsuredNode,JsonNode diseaseList,JsonNode HDFCPinCodeList)
	{
		boolean validProductFlag = false;
		//validate pre-existing disease
		if(diseaseList.size()>0 && product.has("Disease"))
		{
			validProductFlag = validateDiseaseList(diseaseList, product);
			log.info("disease check : "+validProductFlag);
		}
		else
		{
			validProductFlag = true;
		}
		
		if(product.has("allowedMaxChildAge")){
			if(quoteParamNode.has("dependent")){
				ArrayNode dependentList= (ArrayNode)quoteParamNode.get("dependent");
				for(JsonNode dependent : dependentList){
					if(dependent.has("relationShip")){
						if(dependent.get("relationShip").asText().equalsIgnoreCase("CH")){
				boolean flag = validateChildAge(dependent.get("age").intValue(),product.get("allowedMaxChildAge").intValue());
								if(!flag){
									validProductFlag=false;
									break;
								}
						}
					}//relationShip if condition end 
			}//for loop end 
		}
		}else
		{
			validProductFlag = true;
		}
		
		if(validProductFlag)
		{
			
			/**
			 * replace by default selfage to max age present in quote request 
			 * 
			 * */
			int maxAge = findMaxAge(quoteParamNode);
			if(maxAge>0){
				((ObjectNode)quoteParamNode).put("selfAge", maxAge);
			}
			validProductFlag = validateAge(quoteParamNode.get("selfAge").intValue(),product.get("entryAge").intValue());
			
			/**
			 * validate self max age 
			 * */
			if(validProductFlag){
				if(product.has("selfMaxAge")){
					validProductFlag = validateMaxAge(quoteParamNode.get("selfAge").intValue(),product.get("selfMaxAge").intValue());
					
				}
			}
			
			if(validProductFlag){
				
				if (product.has("isPinCodeTest") && product.get("isPinCodeTest").asText().equalsIgnoreCase("Y")){
					validProductFlag = validatePinCode(HDFCPinCodeList,product);
					if (validProductFlag){
						String pinCode = HDFCPinCodeList.get("pinCode").asText();
				    	JsonNode delhiNCRCodes = HDFCPinCodeList.get("DelhiNCR");
				    	if(delhiNCRCodes.has(pinCode) && sumInsuredNode.get(HealthQuoteConstants.MIN_SUM_INSURED).asLong() <= 400000 ){
				    		log.info("input entered is of delhiNCRCodes and has Sum Insured < 400000 for carrierId :"+product.get("carrierId")+",planId :"+product.get("planId"));
				    		validProductFlag = false;
				    	}
				    	else{
				    		ArrayNode dependentList= (ArrayNode)quoteParamNode.get("dependent");
				    		int count = 0;
							for(JsonNode dependent : dependentList){
								if(dependent.get("age").asInt() >= 18){
									count++; 
								}
							}
							if(count == 1 && sumInsuredNode.get(HealthQuoteConstants.MIN_SUM_INSURED).asLong() == 300000){
								log.info("input entered has only one adult and has Sum Insured = 300000 for carrierId :"+product.get("carrierId")+",planId :"+product.get("planId"));
								validProductFlag = false;
							}
				    	}
					}
				}
			}
			
			
			if(validProductFlag){
				//if rider is selected then validation will happen according to the specific flags.
				if(quoteParamNode.has("riders") && quoteParamNode.get("riders").size() > 0){
					validProductFlag = validateRider(product,quoteParamNode);
					log.info("value of carrierId :"+product.get("carrierId")+",planId :"+product.get("planId")+",childPlanId :"+product.get("childPlanId"));
				}
				//otherwise all base plans will be displayed having "isBase" flag.
				else{
					validProductFlag = validateDefaultPlan(product,quoteParamNode);
				}
			}
			
			
			
			if(validProductFlag)
			{
				
				sumInsured = validateSumInsured(product.get("minAllowedSumInsured").longValue(),
						product.get("maxAllowedSumInsured").longValue(),sumInsuredNode);
				
				if(sumInsured > 0)
				{
					validProductFlag = validateAdultCount(product.get("minAllowedAdult").intValue(),
														  product.get("maxAllowedAdult").intValue(),
										                  quoteParamNode.get("adultCount").intValue());
					if(validProductFlag)
					{
						validProductFlag = validateChildCount(product.get("minAllowedChild").intValue(),
								  product.get("maxAllowedChild").intValue(),
				                  quoteParamNode.get("childCount").intValue());
						
						if(!validProductFlag)
						{
							log.error("validateChildCount failed for family plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
						}
					}
					else
					{
						log.error("validateAdultCount failed for family plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
					}
				}
				else
				{
					validProductFlag =false;
					log.error("validateSumInsured failed for family plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
				}
				
				//return validProductFlag;
			}
		
			else
			{
				log.error("validateAge failed for family plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
				//return validProductFlag;
			}
		}
		else
		{
			log.error("Pre-Existing disease check failed for family plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
		}
		return validProductFlag;
	}
	
	public boolean validateIndividualProduct(JsonNode product,JsonNode quoteParamNode,ObjectNode sumInsuredNode,JsonNode diseaseList,JsonNode HDFCPinCodeList)
	{
		boolean validProductFlag = false;
		
		//validate pre-existing disease
		if(diseaseList!=null && product.has("Disease"))
		{
				validProductFlag = validateDiseaseList(diseaseList, product);
				log.info("disease check : "+validProductFlag);
		}
		else
		{
				validProductFlag = true;
		}
			
		if(validProductFlag)
		{
			/**
			 * replace by default selfage to max age present in quote request 
			 * 
			 * */
			int maxAge = findMaxAge(quoteParamNode);
			if(maxAge>0){
				((ObjectNode)quoteParamNode).put("selfAge", maxAge);
			}
			validProductFlag = validateAge(quoteParamNode.get("selfAge").intValue(),product.get("entryAge").intValue());
			/**
			 * validate self max age 
			 * */
			if(validProductFlag){
				if(product.has("selfMaxAge")){
					validProductFlag = validateMaxAge(quoteParamNode.get("selfAge").intValue(),product.get("selfMaxAge").intValue());
					
				}
			}
			
			if(validProductFlag){
				if (product.has("isPinCodeTest") && product.get("isPinCodeTest").asText().equalsIgnoreCase("Y")){
					validProductFlag = validatePinCode(HDFCPinCodeList,product);
				}
			}
			
			
			if(validProductFlag){
				//if rider is selected then validation will happen according to the specific flags
				if(quoteParamNode.has("riders") && quoteParamNode.get("riders").size() > 0){
					validProductFlag = validateRider(product,quoteParamNode);
					log.info("value of carrierId :"+product.get("carrierId")+",planId :"+product.get("planId")+",childPlanId :"+product.get("childPlanId"));
				}
				//otherwise all base plans will be displayed having "isBase" flag.
				else{
					validProductFlag = validateDefaultPlan(product,quoteParamNode);
				}
			}
			
			
			if(validProductFlag)
			{
				
				sumInsured = validateSumInsured(product.get("minAllowedSumInsured").longValue(),
						product.get("maxAllowedSumInsured").longValue(),sumInsuredNode);
				
				if(sumInsured == 0)
				{
					validProductFlag = false;
					log.error("validateSumInsured failed for Individual plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
				}
				
				//return validProductFlag;
			}
			else
			{
				log.error("validateAge failed for Individual plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
				//return validProductFlag;
			}
		}
		else
		{
			log.error("Pre-Existing disease check failed for Individual plan id :"+product.get("planId").intValue()+" : child planid :"+product.get("childPlanId").intValue());
		}
		
		return validProductFlag;
	}
	
  public boolean validateAge(int selfAge ,int entryAge)
   {
	   if(selfAge>=entryAge)
	   {
		   return true;
	   }
	   else
	   {
		   return false;
	   }
   }
  public boolean validateChildAge(int childAge ,int entryAge)
  {
	   if(childAge>entryAge)
	   {
		   return false;
	   }
	   else
	   {
		   return true;
	   }
  }

  public boolean validateMaxAge(int age ,int maxAge)
  {
	   if(age>maxAge)
	   {
		   return false;
	   }
	   else
	   {
		   return true;
	   }
  }
   public long validateSumInsured(long minSumInsured,long maxSumInsured,ObjectNode sumInsuredNode)
   {
	   //if(sumInsuredNode.get("minsumInsured").longValue()>=minSumInsured &&
		//  sumInsuredNode.get("maxsumInsured").longValue()<=maxSumInsured)
	   
	   if(sumInsuredNode.get("minsumInsured").longValue()>=minSumInsured &&
				  sumInsuredNode.get("minsumInsured").longValue()<=maxSumInsured)
	   {
		   return sumInsuredNode.get("minsumInsured").longValue();
	   }
	   /*else if(sumInsuredNode.get("maxsumInsured").longValue()>minSumInsured &&
				  sumInsuredNode.get("maxsumInsured").longValue()<=maxSumInsured)
	   {
		   return sumInsuredNode.get("maxsumInsured").longValue();
	   }*/
	   else
	   {
		   return 0;
	   }
   }
   
   public boolean validateAdultCount(int minAllowedAdult,int maxAllowedAdult,int adultCount)
   {
	   if(minAllowedAdult<=adultCount&&maxAllowedAdult>=adultCount)
	   {
		   return true;
	   }
	   else
	   {
		   return false;
	   }
   }
   
   public boolean validateChildCount(int minAllowedChild,int maxAllowedChild,int childCount)
   {
	   if(minAllowedChild<=childCount&&maxAllowedChild>=childCount)
	   {
		   return true;
	   }
	   else
	   {
		   return false;
	   }
   }
   
   public boolean validateDiseaseList(JsonNode diseaseList,JsonNode product)
   {
	   boolean flag = true;
	   		int DiseaseCount=0;
			for(JsonNode diseaseNode : product.get("Disease"))
			{
				   if(diseaseList.hasNonNull(diseaseNode.get("DiseaseId").asText()))
				   {
					   //flag = true;
					   //return flag; // return true when one of the disease id found
					   DiseaseCount++;
				   }/*
				   else
				   {
					   //flag = false;
				   }*/
			}
			log.info("DiseaseCount For Product Validation: "+DiseaseCount+"\t diseaseList : "+diseaseList);
			if(DiseaseCount==diseaseList.size()){
				flag = true;
			}else{
				flag = false;
			}
			
		return flag;	   
   }

   public int findMaxAge(JsonNode quoteParam){
	   try{
		   ArrayNode dependent = (ArrayNode)quoteParam.get("dependent");
		   int maxAge=0;
		   if(dependent!=null && dependent.size()>0){
			   maxAge=dependent.get(0).get("age").asInt();
			   for(JsonNode member : dependent){
				   if(maxAge<member.get("age").asInt()){
					   maxAge=member.get("age").asInt();
				   }
			   }
			   return maxAge;
		   }
	   }catch(Exception e){
		   log.info("Error at findMaxAge ",e);
	   }
	   return 0;
   }
   
   public boolean validateRider(JsonNode product,JsonNode quoteParamNode){
		boolean validProductFlag = false;
		int count =0;
		JsonNode uiRiders1 =  quoteParamNode.get("riders");
		if(product.has("riderFilterCategory") ){
			if( product.get("riderFilterCategory").has("requiredRiders") && (!product.get("riderFilterCategory").get("requiredRiders").isNull())){
				log.info("product in requiredRiders : "+product);
				for(JsonNode uRider:uiRiders1){
					for(JsonNode requiredRiders:product.get("riderFilterCategory").get("requiredRiders")){
						if(uRider.get("riderId").asText().equalsIgnoreCase(requiredRiders.asText())){
							count++;
							break;
						}
					}
					
				}
				if(count==product.get("riderFilterCategory").get("requiredRiders").size()){
					validProductFlag = true;
					log.info("requiredRiders size : "+product.get("riderFilterCategory").get("requiredRiders").size()+" count : "+count+" are same hence product validated.");

				}else
				{
					validProductFlag = false;
					log.info("false required rider not present in request for plan "+product.get("carrierId").asText()+"-"+product.get("planId").asText()+"  childPlanId :- "+product.get("childPlanId"));
					return validProductFlag;
				}
			}
			count=0;
			for(JsonNode fromUIRiders1 : uiRiders1){
				JsonNode planRiders =  product.get("riderFilterCategory");
				String riderId = fromUIRiders1.get("riderId").asText();
				if(!planRiders.isNull()&& planRiders.has(riderId)){
					if(planRiders.get(riderId).get("active").asText().equalsIgnoreCase("Y")){
						log.info("inside rider validation");
						if(fromUIRiders1.has("value") && (planRiders.get(riderId).has("matchValue1") ||planRiders.get(riderId).has("matchValue1"))){
							String value = fromUIRiders1.get("value").asText();
							if(planRiders.get(riderId).get("matchValue1").asText().equalsIgnoreCase(value)|| planRiders.get(riderId).get("matchValue2").asText().equalsIgnoreCase(value)){
								validProductFlag = true;
								count++;
							}else{
								validProductFlag = false;
								log.info("rider match values does not matched for product : "+product.get("carrierId").asText()+"-"+product.get("planId").asText()+" childPlanId : "+product.get("childPlanId").asText());
								return validProductFlag;
							}

						}else{
							validProductFlag = true;
							count++;
						}
					}
				}else{
					validProductFlag = false;
					return validProductFlag;
				}
			}
			if(count==uiRiders1.size()){
				validProductFlag = true;
				log.info("rider validation true since count match "+product.get("carrierId").asText()+"-"+product.get("planId").asText()+" childPlanId : "+product.get("childPlanId").asText());
			}else{
				validProductFlag = true;
				log.info("rider validation false since count match "+product.get("carrierId").asText()+"-"+product.get("planId").asText()+" childPlanId : "+product.get("childPlanId").asText());
			}
		}
			
		log.info("rider validation "+validProductFlag+ " for "+product.get("carrierId").asText()+"-"+product.get("planId").asText()+" childPlanId : "+product.get("childPlanId").asText());
		return validProductFlag;
	}
   public boolean validateDefaultPlan(JsonNode product,JsonNode quoteParamNode){
	   boolean validProductFlag = false;
		//add "isDefault " for default plans.
		if(product.has("isDefault") && product.get("isDefault").asText().equalsIgnoreCase("Y")){
			validProductFlag = true;
		}
		return validProductFlag;
	}
   //validation for PinCode, add isPinCodeTest flag in your Product document implemented in HDFC Plans
   public boolean validatePinCode(JsonNode HDFCPinCodeList,JsonNode product){
   	boolean validProductFlag = true;
   	String pinCode = HDFCPinCodeList.get("pinCode").asText();
   	JsonNode blockedCodes = HDFCPinCodeList.get("BlockedCodes");
   	if (blockedCodes.has(pinCode)){
		log.info("PinCode entered is in block codes :"+pinCode+",for carrierId :"+product.get("carrierId")+",planId :"+product.get("planId"));
   		validProductFlag = false;
   	}
   	return validProductFlag;
   }
}
