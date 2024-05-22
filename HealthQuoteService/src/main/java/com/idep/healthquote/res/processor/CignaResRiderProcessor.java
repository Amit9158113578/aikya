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
public class CignaResRiderProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaResRiderProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode resNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode uiReqNode = objectMapper.readTree(exchange.getProperty("HealthProductUIReq").toString());
		
			ArrayNode uiReqRiders = null; 
			if(resNode.get("carrierRequestForm").has("carrierResRider")){
			
				if(uiReqNode!=null){
					log.debug("UI Request  for cigna Rider processing  : "+uiReqNode);
					if(uiReqNode.has("productInfo")){
						if(uiReqNode.get("productInfo").has("riderList")){
							uiReqRiders= (ArrayNode)uiReqNode.get("productInfo").get("riderList");	
						}
					}
				}
				if(!resNode.get("carrierRequestForm").get("carrierResRider").isNull()){
					ArrayNode carrierResRider = null;
					if(resNode.has("carrierRequestForm")){
						if(resNode.get("carrierRequestForm").get("carrierResRider").isArray()){
						carrierResRider=(ArrayNode)resNode.get("carrierRequestForm").get("carrierResRider");
						}else{
							carrierResRider = objectMapper.createArrayNode();
									carrierResRider.add(resNode.get("carrierRequestForm").get("carrierResRider"));
						}
					}else{
						carrierResRider=(ArrayNode)resNode.get("carrierResRider");
						if(resNode.get("carrierResRider").isArray()){
							carrierResRider=(ArrayNode)resNode.get("carrierResRider");
							}else{
								carrierResRider = objectMapper.createArrayNode();
										carrierResRider.add(resNode.get("carrierResRider"));
							}
					}
				
				ArrayNode riderList = objectMapper.createArrayNode();
				if(carrierResRider.size()>0){
					for(JsonNode riders : carrierResRider){
						if(riders.has("productId")){
							for(JsonNode uiRider : uiReqRiders){
								if(uiRider.has("clientRiderCode")){
									if(uiRider.get("clientRiderCode").asText().equalsIgnoreCase(riders.get("productId").asText())&&  !riders.get("benefitTypeCd").asText().equalsIgnoreCase("UWLOAD")){										
										((ObjectNode)uiRider).put("riderPremiumAmount", riders.get("amount").asText());
										riderList.add(uiRider);
										break;
									}
									
								}
							}
						}
					}//for loop end for CarrierRider list	
				}
				log.debug("Cigna CarrierRider List genrated on base on carrierResponse  :"+riderList );
				
				((ObjectNode)resNode.get("carrierRequestForm")).put("riderList",riderList);
				/***
					removing carrierResRider from CarrierResponse - same contain present in riderList  
				**/
				((ObjectNode)resNode.get("carrierRequestForm")).remove("carrierResRider");
				}
			}	
			log.info("Cigna CarrierRider  carrierResponse  processed :"+resNode );
			exchange.getIn().setBody(resNode);
		}catch(Exception e){
			log.error("unableto proocess cigna rider : ",e);
		}
	}		

	
	
	
	
	
}
