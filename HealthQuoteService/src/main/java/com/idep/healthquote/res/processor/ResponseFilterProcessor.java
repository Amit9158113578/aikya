package com.idep.healthquote.res.processor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author pravin.jakhi
 *
 */
public class ResponseFilterProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ResponseFilterProcessor.class.getName());	
	
	@Override
	public void process(Exchange exchange) throws Exception {
try{
	
	JsonNode resNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
	JsonNode CarrierRiders = objectMapper.readTree(exchange.getProperty("carrierRiders").toString());
	JsonNode UIReq = objectMapper.readTree(exchange.getProperty("HealthProductUIReq").toString());
	double claimIndex =0.0;
	double hospitalIndex =0.0;
	log.info("UIReq in productInfo  "+UIReq);
	if(UIReq!=null){
		
		ArrayNode prductRider = (ArrayNode)UIReq.get("productInfo").get("riderList");
		
		log.info("UI Request for Respoonse Filter : "+UIReq);
		log.info("Response Filter processing request  : "+resNode);
		log.info("Carrier Riders  : "+CarrierRiders);
		if(UIReq.get("productInfo").has("claimIndex")){
			claimIndex = 	UIReq.get("productInfo").get("claimIndex").asDouble();
		}
		if(UIReq.get("productInfo").has("hospitalIndex")){
			hospitalIndex = 	UIReq.get("productInfo").get("hospitalIndex").asDouble();
		}
		if(UIReq.get("quoteParam").has("riders")){
			if(resNode.has("carrierRequestForm")){
				
				if(resNode.get("carrierRequestForm").has("riderList")){
					
					/***
					 * response rider list compare with system rider and adding category & subCategory field in response
					 * */
					log.debug("Carrier Request carrierRequestForm  rider  List full resNode: "+resNode);
					ArrayNode riderList = (ArrayNode)resNode.get("carrierRequestForm").get("riderList");
					log.debug("CarrerRequestt Form rider  List : "+riderList);
					for(JsonNode resRider :  riderList){
						
						for(JsonNode prodrider : prductRider ){
							/**
							 * validating rider id if match then addding category and subCategory in rider response
							 * **/
							if(prodrider.get("riderId").asText().equalsIgnoreCase(resRider.get("riderId").asText())){
								if(prodrider.has("category")){
									((ObjectNode)resRider).put("category",prodrider.get("category").asText());
								}
								if(prodrider.has("subCategory")){
									((ObjectNode)resRider).put("subCategory",prodrider.get("subCategory").asText());
								}
							}
						}
					}
					((ObjectNode)resNode.get("carrierRequestForm")).put("riderList",riderList);
					((ObjectNode)resNode.get("carrierRequestForm")).put("claimIndex",claimIndex);
					((ObjectNode)resNode.get("carrierRequestForm")).put("hospitalIndex",hospitalIndex);
					log.info("final rider list in ResponseFilterProcessor :"+riderList);
				}
			}else if (resNode.has("riderList")) {
				//JsonNode riderData = 
				ArrayNode riderList = (ArrayNode)resNode.get("riderList");
				for(JsonNode resRider :  riderList){
					
					for(JsonNode prodrider : prductRider ){
						if(prodrider.get("riderId").asText().equalsIgnoreCase(resRider.get("riderId").asText())){
							if(prodrider.has("category")){
								((ObjectNode)resRider).put("category",prodrider.get("category").asText());
							}
							if(prodrider.has("subCategory")){
								((ObjectNode)resRider).put("subCategory",prodrider.get("subCategory").asText());
							}
						}
					}
				}
					((ObjectNode)resNode).put("riderList",riderList);
					((ObjectNode)resNode).put("claimIndex",claimIndex);
					((ObjectNode)resNode).put("hospitalIndex",hospitalIndex);
			}
		}
		if(resNode.has("carrierRequestForm"))
		{
			((ObjectNode)resNode.get("carrierRequestForm")).put("claimIndex",claimIndex);
			((ObjectNode)resNode.get("carrierRequestForm")).put("hospitalIndex",hospitalIndex);
		}
		else
		{
		((ObjectNode)resNode).put("claimIndex",claimIndex);
		((ObjectNode)resNode).put("hospitalIndex",hospitalIndex);
		}
	}
	log.info("Response filter vaidation completed  : "+resNode);
	
	
	exchange.getIn().setBody(resNode);
	
}catch(Exception e){
	log.error("unable to process request : ",e);
}


		
		
	}

}
