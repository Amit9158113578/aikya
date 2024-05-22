package com.idep.healthquote.form.req;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;

public class FormHealthQuoteRequest {
	
	 Logger log = Logger.getLogger(FormHealthQuoteRequest.class.getName());
	 ObjectMapper objectMapper = new ObjectMapper();
	 
	 public List<Map<String, Object>> getProductRatingDetails(JsonNode quoteParamNode, JsonNode ratingParamNode, CBService service)
			    throws Exception
			  {
			    List<Map<String, Object>> ratingList = null;
			    
			    String preExistingDisease = "N";
			    if (quoteParamNode.findValue("preExistingDisease").size() > 0) {
			      preExistingDisease = "Y";
			    }

			    JsonArray ratingParam = JsonArray.create();
			    if(ratingParamNode.has("criticalIllness")){
			    	ratingParam.add(ratingParamNode.get("criticalIllness").textValue());
			    }
			    if(ratingParamNode.has("organDonar")){
			    	ratingParam.add(ratingParamNode.get("organDonar").textValue());
			    }
			    if(ratingParamNode.has("isSpouse")){
			    	ratingParam.add(ratingParamNode.get("isSpouse").textValue());
			    }
			    ratingParam.add("T");
			    ratingParam.add(preExistingDisease);
			    if(ratingParamNode.has("isSelf")){
			    	ratingParam.add(ratingParamNode.get("isSelf").textValue());
			    }
			    if(quoteParamNode.has("selfAge")){
			    	ratingParam.add(quoteParamNode.get("selfAge").intValue());
			    }
			    if(ratingParamNode.has("spouseAge")){
			    	ratingParam.add(ratingParamNode.get("spouseAge").intValue());
			    }
			    if(quoteParamNode.has("totalCount")){
			    	ratingParam.add(quoteParamNode.get("totalCount").intValue());
			    }
			    ratingParam.add(ratingParamNode.get("minInsuredAge").intValue());
			    ratingParam.add(ratingParamNode.get("maxInsuredAge").intValue());
			    
			    
			    //ARRAY_AGG(p.products)[0]
			    String statement = HealthQuoteConstants.PRODUCTS_RATINGS_QUERY;
			    ratingList = service.executeParamArrQuery(statement, ratingParam);
			    
			    return ratingList;
		}
	 
	 public List<Map<String, Object>> getProductRatingDetails_old(JsonNode quoteParamNode, JsonNode ratingParamNode, CBService service)
			    throws Exception
			  {
		 List<Map<String, Object>> ratingList = null;
		    //JsonObject ratingParamObj = JsonObject.create();
		    String preExistingDisease = "N";
		    if (quoteParamNode.findValue("preExistingDisease").size() > 0) {
		      preExistingDisease = "Y";
		    }
		    
		    //JsonObject ratingParamObj = JsonObject.create();
		    //ARRAY_AGG(p.products)[0]
		    String statement = "select p.DefaultRiskType,p.products AS products from ProductData p where "
		      + "p.documentType='HealthCategoryRating' "
		      + " and p.businessLineId=4 "
		      + " and p.criticalIllness='" + ratingParamNode.findValue("criticalIllness").textValue() + "'" + 
		      " and p.organDonar='" + ratingParamNode.findValue("organDonar").textValue() + "'" + 
		      " and p.isSpouse='" + ratingParamNode.findValue("isSpouse").textValue() + "'" + 
		      " and p.isCities='T'" + 
		      " and p.preExistingDisease='" + preExistingDisease + "'" + 
		      " and p.isSelf='" + ratingParamNode.findValue("isSelf").textValue() + "'" + 
		      " and p.minSelf<=" + quoteParamNode.findValue("selfAge").intValue() + 
		      " and p.selfAge>=" + quoteParamNode.findValue("selfAge").intValue() + 
		      " and p.minSpouse<=" + ratingParamNode.findValue("spouseAge").intValue() + 
		      " and p.spouseAge>=" + ratingParamNode.findValue("spouseAge").intValue() + 
		      " and p.minMinInsuredAge<=" + ratingParamNode.findValue("minInsuredAge").intValue() + 
		      " and p.minMaxInsuredAge>=" + ratingParamNode.findValue("minInsuredAge").intValue() + 
		      " and p.minInsuredAge<=" + ratingParamNode.findValue("maxInsuredAge").intValue() + 
		      " and p.maxInsuredAge>=" + ratingParamNode.findValue("maxInsuredAge").intValue() + 
		      " and p.minTotalCount<=" + quoteParamNode.findValue("totalCount").intValue() + 
		      " and p.totalCount>=" + quoteParamNode.findValue("totalCount").intValue();
		    
		    this.log.info("Health Rating query :" + statement);
		    ratingList = service.executeQueryCouchDB(statement);
		    
		    //ratingList = service.executeQuery(statement); use parameterized query
		    
		    return ratingList;
		}
	 
	 
	  public List<Map<String, Object>> getProductDetails(JsonNode quoteParamNode,int carrierId, long minsumInsured, long maxsumInsured,CBService service)
			    throws Exception
			  {
			    this.log.info("get HealthProduct details");
			    
			    List<Map<String, Object>> productList = null;
			    String statement = null;
			    if (quoteParamNode.has("riders"))
			    {
			      String whereClause = "";
			      for (JsonNode jsonNode : quoteParamNode.findValue("riders"))
			      {
			        whereClause = whereClause + "b.riderId" + jsonNode.findValue("riderId") + " IS NOT MISSING and ";
			      }
			      whereClause = whereClause.substring(0, whereClause.length() - 4);
			      if (quoteParamNode.get("planType").textValue().equals("I"))
			      {
			        statement = 
			        
			            " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			          + " INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			          + " b.documentType='HealthPlan'"
			          + " and b.carrierId="+carrierId
			          + " and b.planType='I' "
			          + " and b.maxAllowedSumInsured >=" + minsumInsured 
			          + " and b.maxAllowedSumInsured >=" + maxsumInsured
			          + " and a.documentType='Carrier' " 
			          + " and b.riderApplicable='Y'"
			          + " and " + whereClause;
			        
			       
			      }
			      else if (quoteParamNode.get("planType").textValue().equals("F"))
			      {
			        /*statement = 
			        
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.Features FROM ProductData b "
			          + "INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			          + " b.documentType='HealthPlan' "
			          + " and b.planType='I' "
			          + " and b.maxAllowedSumInsured BETWEEN " + minsumInsured + " and " + maxsumInsured
			          + " and b.riderApplicable='Y'"
			          + " or b.documentType='HealthPlan' and b.planType='F' "
			          + " and b.maxAllowedAdult=" + quoteParamNode.get("adultCount").longValue() 
			          + " and b.maxAllowedChild=" + quoteParamNode.get("childCount").longValue() 
			          + " and b.maxAllowedSumInsured >= " + minsumInsured + " "
			          + " and b.riderApplicable='Y'"
			          + " and a.documentType='Carrier' "
			          + " and " + whereClause;
			          */
			    	  
			    	  statement = 
			    		        
			    	          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			    	          + "INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			    	         // + " b.documentType='HealthPlan' "
			    	         // + " and b.planType='I' "
			    	         // + " and b.maxAllowedSumInsured BETWEEN " + minsumInsured + " and " + maxsumInsured
			    	         // + " and b.riderApplicable='Y'"
			    	          + " b.documentType='HealthPlan'"
			    	          + " and b.carrierId="+carrierId
			    	          + " and b.planType='F' "
			    	          + " and b.maxAllowedAdult>=" + quoteParamNode.get("adultCount").longValue() 
			    	          + " and b.maxAllowedChild>=" + quoteParamNode.get("childCount").longValue() 
			    	          + " and b.minAllowedAdult<=" + quoteParamNode.get("adultCount").longValue() 
			    	          + " and b.minAllowedChild<=" + quoteParamNode.get("childCount").longValue()
			    	          + " and b.maxAllowedSumInsured >=" + minsumInsured
			    	          + " and b.maxAllowedSumInsured >=" + maxsumInsured
			    	          + " and b.riderApplicable='Y'"
			    	          + " and a.documentType='Carrier' "
			    	          + " and " + whereClause;
			        
			       
			      }
			      else
			      {
			        this.log.info(" invalid planType ");
			      }
			    }
			    else if (quoteParamNode.get("planType").textValue().equals("I"))
			    {
			      statement = 
			      
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			        + " INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			        + " b.documentType='HealthPlan'"
			        + " and b.carrierId="+carrierId
			        + " and b.planType='I' "
			        + " and b.maxAllowedSumInsured >=" + minsumInsured
			        + " and b.maxAllowedSumInsured >=" + maxsumInsured
			        + " and a.documentType='Carrier' "
			        + " and b.riderApplicable='N'";
			      
			      
			    }
			    else if (quoteParamNode.get("planType").textValue().equals("F"))
			    {
			      statement = 
			      
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
			        + " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
			      //  + " where b.documentType='HealthPlan' and b.planType='I' "
			      //  + " and b.maxAllowedSumInsured >= " + minsumInsured 
			      //  + " and b.riderApplicable='N'"
			      //  + " and a.documentType='Carrier' "
			        + " where b.documentType='HealthPlan'"
			        + " and b.carrierId="+carrierId
			        + " and b.planType='F' "
			        + " and b.riderApplicable='N'"
			        + " and b.maxAllowedAdult>=" + quoteParamNode.get("adultCount").longValue() 
			        + " and b.maxAllowedChild>=" + quoteParamNode.get("childCount").longValue()
			        + " and b.minAllowedAdult<=" + quoteParamNode.get("adultCount").longValue() 
			    	+ " and b.minAllowedChild<=" + quoteParamNode.get("childCount").longValue()
			        + " and b.maxAllowedSumInsured >=" + minsumInsured
			        + " and b.maxAllowedSumInsured >=" + maxsumInsured
			        + " and a.documentType='Carrier' " ;
			      
			    }
			    else
			    {
			      this.log.info(" invalid planType ");
			    }
			    
			    this.log.info("Health statement : " + statement);
			    productList = service.executeQueryCouchDB(statement);
			    //productList = service.executeQuery(statement);
			    this.log.info("Health statement result : " + productList.toString());
			    System.out.println("Health statement result : " + productList.toString());
			    
			    return productList;
			  }
	  
	  
	  public List<Map<String, Object>> getProducts(JsonNode quoteParamNode,int carrierId, long minsumInsured, long maxsumInsured,CBService service)
			    throws Exception
			  {
			    this.log.info("get HealthProduct details");
			    
			    List<Map<String, Object>> productList = null;
			    String statement = null;
			    JsonObject paramObj = JsonObject.create();
			    
			    if (quoteParamNode.has("riders"))
			    {
			      String whereClause = "";
			      for (JsonNode jsonNode : quoteParamNode.findValue("riders"))
			      {
			        whereClause = whereClause + "b.riderId" + jsonNode.findValue("riderId") + " IS NOT MISSING and ";
			      }
			      whereClause = whereClause.substring(0, whereClause.length() - 4);
			      if (quoteParamNode.get("planType").textValue().equals("I"))
			      {
			        statement = 
			        
			            " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			          + " INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			          + " b.documentType='HealthPlan'"
			          + " and b.carrierId="+carrierId
			          + " and b.planType='I' "
			          + " and b.maxAllowedSumInsured >=" + minsumInsured 
			          + " and b.maxAllowedSumInsured >=" + maxsumInsured
			          + " and a.documentType='Carrier' " 
			          + " and b.riderApplicable='Y'"
			          + " and " + whereClause;
			        
			       
			      }
			      else if (quoteParamNode.get("planType").textValue().equals("F"))
			      {
			        /*statement = 
			        
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.Features FROM ProductData b "
			          + "INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			          + " b.documentType='HealthPlan' "
			          + " and b.planType='I' "
			          + " and b.maxAllowedSumInsured BETWEEN " + minsumInsured + " and " + maxsumInsured
			          + " and b.riderApplicable='Y'"
			          + " or b.documentType='HealthPlan' and b.planType='F' "
			          + " and b.maxAllowedAdult=" + quoteParamNode.get("adultCount").longValue() 
			          + " and b.maxAllowedChild=" + quoteParamNode.get("childCount").longValue() 
			          + " and b.maxAllowedSumInsured >= " + minsumInsured + " "
			          + " and b.riderApplicable='Y'"
			          + " and a.documentType='Carrier' "
			          + " and " + whereClause;
			          */
			    	  
			    	  statement = 
			    		        
			    	          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			    	          + "INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			    	         // + " b.documentType='HealthPlan' "
			    	         // + " and b.planType='I' "
			    	         // + " and b.maxAllowedSumInsured BETWEEN " + minsumInsured + " and " + maxsumInsured
			    	         // + " and b.riderApplicable='Y'"
			    	          + " b.documentType='HealthPlan'"
			    	          + " and b.carrierId="+carrierId
			    	          + " and b.planType='F' "
			    	          + " and b.maxAllowedAdult>=" + quoteParamNode.get("adultCount").longValue() 
			    	          + " and b.maxAllowedChild>=" + quoteParamNode.get("childCount").longValue() 
			    	          + " and b.minAllowedAdult<=" + quoteParamNode.get("adultCount").longValue() 
			    	          + " and b.minAllowedChild<=" + quoteParamNode.get("childCount").longValue()
			    	          + " and b.maxAllowedSumInsured >=" + minsumInsured
			    	          + " and b.maxAllowedSumInsured >=" + maxsumInsured
			    	          + " and b.riderApplicable='Y'"
			    	          + " and a.documentType='Carrier' "
			    	          + " and " + whereClause;
			        
			       
			      }
			      else
			      {
			        this.log.info(" invalid planType ");
			      }
			    }
			    else if (quoteParamNode.get("planType").textValue().equals("I"))
			    {
			      statement = 
			      
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b "
			        + " INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
			        + " b.documentType='HealthPlan'"
			        + " and b.carrierId="+carrierId
			        + " and b.planType='I' "
			        + " and b.maxAllowedSumInsured >=" + minsumInsured
			        + " and b.maxAllowedSumInsured >=" + maxsumInsured
			        + " and a.documentType='Carrier' "
			        + " and b.riderApplicable='N'";
			      
			      
			    }
			    else if (quoteParamNode.get("planType").textValue().equals("F"))
			    {
			      statement = 
			      
			          " SELECT a.carrierName,a.insurerIndex,b.carrierId,b.planId,b.planType,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
			        + " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
			      //  + " where b.documentType='HealthPlan' and b.planType='I' "
			      //  + " and b.maxAllowedSumInsured >= " + minsumInsured 
			      //  + " and b.riderApplicable='N'"
			      //  + " and a.documentType='Carrier' "
			        + " where b.documentType='HealthPlan'"
			        + " and b.carrierId=$carrierId"
			        + " and b.planType='F' "
			        + " and b.riderApplicable='N'"
			        + " and b.maxAllowedAdult>=$maxAllowedAdult" 
			        + " and b.maxAllowedChild>=$maxAllowedChild" 
			        + " and b.minAllowedAdult<=$minAllowedAdult"
			    	+ " and b.minAllowedChild<=$minAllowedChild"
			        + " and b.maxAllowedSumInsured >=$minsumInsured" 
			        + " and b.maxAllowedSumInsured >=$maxsumInsured"
			        + " and a.documentType='Carrier' " ;
			      
			      paramObj.put("carrierId", carrierId);
			      paramObj.put("maxAllowedAdult", quoteParamNode.get("adultCount").longValue() );
			      paramObj.put("maxAllowedChild",  quoteParamNode.get("childCount").longValue());
			      paramObj.put("minAllowedAdult", quoteParamNode.get("adultCount").longValue() );
			      paramObj.put("minAllowedChild", quoteParamNode.get("childCount").longValue());
			      paramObj.put("minsumInsured", minsumInsured);
			      paramObj.put("maxsumInsured", maxsumInsured);
			      
			    }
			    else
			    {
			      this.log.info(" invalid planType ");
			    }
			    
			    this.log.info("Health statement : " + statement);
			    productList = service.executeParamQuery(statement,paramObj);
			    //productList = service.executeQuery(statement);
			    this.log.info("Health statement result : " + productList.toString());
			    System.out.println("Health statement result : " + productList.toString());
			    
			    return productList;
			  }
	  
	  public List<JsonObject> getCarrierProduct(JsonNode carrierProductNode,long minsumInsured, long maxsumInsured,CBService service)
			    throws Exception
			  {
			    
			    List<JsonObject> productList = null;
			    JsonArray paramObj = JsonArray.create();
			    productList = service.executeConfigParamArrQuery(HealthQuoteConstants.PRODUCTS_PLANRIDERS_QUERY, paramObj);
			    return productList;
			    
			  }
	  
	  public List<JsonObject> getAllProducts(JsonNode quoteParamNode,long minsumInsured, long maxsumInsured,CBService service)
			    throws Exception
			  {
			    
			    List<JsonObject> productList = null;
			    JsonArray paramObj = JsonArray.create();
			    
			    if (quoteParamNode.has("riders"))
			    {
			    	
			    }
			    else
			    {
				      if (quoteParamNode.get("planType").textValue().equals("F"))
				      {
				    	  String preExistingDisease = "N";
				    	  if (quoteParamNode.get("preExistingDisease").size() > 0) {
					  
						  preExistingDisease = "Y";
						  
					  }	
				      paramObj.add(preExistingDisease);
				      paramObj.add(quoteParamNode.get("adultCount").longValue());
				      paramObj.add(quoteParamNode.get("childCount").longValue());
				      paramObj.add(minsumInsured);
				      paramObj.add(maxsumInsured);
				      String familyQuery = HealthQuoteConstants.ALL_FAMILY_PRODUCTS_QUERY;
					  productList = service.executeConfigParamArrQuery(familyQuery, paramObj);
					  String indvQuery = HealthQuoteConstants.ALL_INDVIDUAL_PRODUCTS_QUERY;
					  List<JsonObject> indvproductList = null;
					  indvproductList = service.executeConfigParamArrQuery(indvQuery, paramObj);
					  productList.addAll(indvproductList);
				    }
				    else
				    {
				      this.log.error(" invalid planType ");
				    }
				      
			    }

			    
			    return productList;
			  }
	  
		}
