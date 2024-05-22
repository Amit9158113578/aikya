/**
 * @author Pravin.Jakhi
 */
package com.idep.proposal.carrier.req.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class StarHealthGetCityProcessor implements Processor {

	 ObjectMapper objectMapper = new ObjectMapper();
	  static Logger log = Logger.getLogger(StarHealthGetCityProcessor.class.getName());
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			
			String input = exchange.getIn().getBody(String.class);
			JsonNode inputReq = objectMapper.readTree(input);
			JsonNode proposerInfo = inputReq.get("proposerInfo");
			String pinCode = proposerInfo.get("contactInfo").get("pincode").asText();
			String permanentPinCode = null;
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|INIT|StarHealthGetCityProcessor Get AreaId process Started|");
			if(proposerInfo.has("permanentAddress")){
				permanentPinCode = proposerInfo.get("permanentAddress").get("pincode").asText();
			}
			JsonNode ConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("HealthProposalRequest-"+inputReq.get("carrierId").asText()+"-"+inputReq.get("planId").asText()).content().toString());
			JsonNode ServiceConfigNode = ConfigDoc.get("getCityDetailsServiceConfig");
		     String  serviceURL = ServiceConfigNode.get("serviceURL").asText();
		      ObjectNode paramNode1 = objectMapper.createObjectNode();
		      paramNode1.put("pincode", pinCode);
		      ArrayNode paramList = objectMapper.createArrayNode();
		      paramList.add(paramNode1);
		      /*
		       * Calling genrateServiceURL method for generating dynamic url as per request param.
		       * for GET method
		       * */
		      serviceURL= genrateServiceURL(serviceURL,paramList,ServiceConfigNode);
		      log.debug("genrated serviceURL  For Star Health GetCityId : "+serviceURL);
		      /*
		       * Calling getServiceResponse method for calling GET method using HttpConnection class  
		       * for GET method
		       * */
		      ObjectNode serviceRes = getServiceResponse(serviceURL);
		  		JsonNode result = objectMapper.readTree(serviceRes.get("result").asText());
		  		log.info("Star Health Get City Details Service Resposne : "+result);
		  		
		  		if(result.has("error")){
		  			JsonNode response_result = result.get("error");
		  			
		  			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|StarHealthGetCityProcessor|"+result);
		  			
		  			ObjectNode obj = this.objectMapper.createObjectNode();
		  			obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
					obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
					obj.put(ProposalConstants.PROPOSAL_RES_DATA, response_result);
					exchange.getIn().setBody(obj);
		  			throw new ExecutionTerminator();
		  		}
		  		
		  		
		  		ArrayNode cityList=(ArrayNode)result.get("city");
		  		String city_id=cityList.get(0).get("city_id").asText();
		  		ObjectNode paramNode = objectMapper.createObjectNode();
		  		paramNode.put("pincode", pinCode);
		  		paramNode.put("city_id", city_id);
		  		 ArrayNode paramList1 = objectMapper.createArrayNode();
			      paramList1.add(paramNode);
		  		JsonNode getAreaIdConfigNode = ConfigDoc.get("getAreaIdServiceConfig");
		  		serviceURL = getAreaIdConfigNode.get("serviceURL").asText();
		  		serviceURL= genrateServiceURL(serviceURL,paramList1,getAreaIdConfigNode);
			      log.debug("genrated Service Url  For Star Health GetAreaId Service URL : "+serviceURL);
			      ObjectNode serviceRes1 = getServiceResponse(serviceURL);
			      JsonNode areaServiceRes = objectMapper.readTree(serviceRes1.get("result").asText());
			      ArrayNode areaList=(ArrayNode)areaServiceRes.get("area");  
			  String areaId=areaList.get(0).get("areaID").asText();
			  log.debug("get AreaId details : "+serviceRes1+"\tareaId "+areaId);
			  ((ObjectNode)inputReq).put("commuAreaId", areaId);
			  
			  if(pinCode.equalsIgnoreCase(permanentPinCode)){
				  ((ObjectNode)inputReq).put("permanentAreaId", areaId);
			  }else{
				   ServiceConfigNode = ConfigDoc.get("getCityDetailsServiceConfig");
				       serviceURL = ServiceConfigNode.get("serviceURL").asText();
				       paramNode1 = objectMapper.createObjectNode();
				      paramNode1.put("pincode", permanentPinCode);
				       paramList = objectMapper.createArrayNode();
				      paramList.add(paramNode1);
				      /*
				       * Calling genrateServiceURL method for generating dynamic url as per request param.
				       * for GET method
				       * */
				      serviceURL= genrateServiceURL(serviceURL,paramList,ServiceConfigNode);
				      log.debug("genrated serviceURL  For Star Health GetCityId : "+serviceURL);
				      /*
				       * Calling getServiceResponse method for calling GET method using HttpConnection class  
				       * for GET method
				       * */
				       serviceRes = getServiceResponse(serviceURL);
				  		 result = objectMapper.readTree(serviceRes.get("result").asText());
				  		 cityList=(ArrayNode)result.get("city");
				  		 city_id=cityList.get(0).get("city_id").asText();
				  		 paramNode = objectMapper.createObjectNode();
				  		paramNode.put("pincode", permanentPinCode);
				  		paramNode.put("city_id", city_id);
				  		  paramList1 = objectMapper.createArrayNode();
					      paramList1.add(paramNode);
				  		 getAreaIdConfigNode = ConfigDoc.get("getAreaIdServiceConfig");
				  		serviceURL = getAreaIdConfigNode.get("serviceURL").asText();
				  		serviceURL= genrateServiceURL(serviceURL,paramList1,getAreaIdConfigNode);
					      log.debug("genrated Service Url  For Star Health GetAreaId Service URL : "+serviceURL);
					       serviceRes1 = getServiceResponse(serviceURL);
					       areaServiceRes = objectMapper.readTree(serviceRes1.get("result").asText());
					       areaList=(ArrayNode)areaServiceRes.get("area");  
					   areaId=areaList.get(0).get("areaID").asText();
					  log.info("get AreaId details : "+serviceRes1+"\tareaId "+areaId);
					  ((ObjectNode)inputReq).put("permanentAreaId", areaId);
			  }
			  
			  log.debug("Final inputReq Star Health Get City Details : "+inputReq);
			  log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|SUCCESS|StarHealthGetCityProcessor AreaId process completed|");
			exchange.getIn().setBody(inputReq);
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|StarHealthGetCityProcessor|",e);
		}
	}
	
	
	public String genrateServiceURL(String url,ArrayNode paramList, JsonNode ServiceConfigNode){
		
		 if(ServiceConfigNode.has("fieldNameReplacement"))
			{
				for(JsonNode fieldName : ServiceConfigNode.get("fieldNameReplacement"))
				{
					if(fieldName.has("deafultValue") && fieldName.get("deafultValue").asText().equalsIgnoreCase("Y") ){
						url = url.replace(fieldName.get("destFieldName").asText(), fieldName.get("value").asText());
						}else{
							for(JsonNode data : paramList){
							url = url.replace(fieldName.get("destFieldName").asText(), data.get(fieldName.get("sourceFieldName").asText()).asText() );
							}
						}
				}
		}
				
		
		return url;
	}
	
	
	public ObjectNode getServiceResponse(String serviceURL){
		String response="";
		 HttpURLConnection httpConn=null;
		 ObjectNode serviceRes = objectMapper.createObjectNode();
		try{
		URL url = new URL(serviceURL);
	      
		httpConn = (HttpURLConnection)url.openConnection();
	      int responseCode = httpConn.getResponseCode();
		  
	      log.info("Service responseCode : "+responseCode);
	      if(responseCode==200){
	      BufferedReader br = new BufferedReader(new InputStreamReader(
	  			(httpConn.getInputStream())));
	     
	  		String output="";
	  		while ((output = br.readLine()) != null) {
	  			serviceRes.put("result",output);
	  		}
	  		httpConn.disconnect();
	      }else{
	    	  log.info("Error while feteching error response from : "+serviceURL);
	    	  BufferedReader error = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
	  	     
	  	  		String output="";
	  	  		while ((output = error.readLine()) != null) {
	  	  			serviceRes.put("result",output);
	  	  		}
	   	  	httpConn.disconnect();
	   	  	
	      }
		}catch(Exception e){
			
			log.error("getServiceResponse : ",e);
		}finally{
			
			if(httpConn!=null){
				httpConn.disconnect();
				
			}
		}
		
		return serviceRes;
	}
	

}
