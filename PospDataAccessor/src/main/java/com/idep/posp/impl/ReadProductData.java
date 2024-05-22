package com.idep.posp.impl;


import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class ReadProductData implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(ReadProductData.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	CBService productData = CBInstanceProvider.getProductConfigInstance();
	public void process(Exchange exchange) throws Exception {

		try{
			List<JsonObject> queryOutputList=null;
			JsonArray paramobj = JsonArray.create();
			String request =  exchange.getIn().getBody(String.class);

			JsonNode reqNode = objectMapper.readTree(request);
			JsonNode queryConfig =  objectMapper.readTree(PospData.getDocBYId("PospSearchProductConfig").content().toString());
			log.info("Request Node : "+reqNode);
			log.info("queryConfig Node : "+queryConfig);
			if(reqNode.has("documentType")){
				String documentType=reqNode.get("documentType").asText();
				log.info("queryConfig.get(documentType) : "+queryConfig.get(documentType));
				if (queryConfig.get(documentType).has("parameters")) {
					for (int i = 0; i < queryConfig.get(documentType).get("parameters").size(); i++) {
						paramobj.add(queryConfig.get(documentType)
								.get("parameters").get(i));
					}
					log.info(queryConfig.get(documentType).asText()+ " Query Param Object set : " + paramobj);
					log.info(queryConfig.get(documentType).asText()+ " Query  : "+ queryConfig.get(documentType).get("query").asText());
					queryOutputList = productData.executeConfigParamArrQuery(queryConfig.get(documentType).get("query").asText(),paramobj);
					log.info(" QUERY OUTPUT : " + queryOutputList);
					exchange.getIn().setBody(objectMapper.readTree(queryOutputList.get(0).toString()));
				} else {
					JsonNode queryOutput = objectMapper
							.readTree(objectMapper.writeValueAsString(productData
									.executeQuery(queryConfig
											.get(documentType).get("query")
											.asText())));
					log.info("Documeent type Query : "
							+ queryConfig.get(documentType));
					log.info("Query output : "
							+ queryConfig.get(documentType)
							+ "\n output : " + queryOutput);
					exchange.getIn().setBody(queryOutput);
				}
			}
		}catch(Exception e){
			log.error("unable to load ProductData Document : "+exchange.getIn().getBody());			
		}
	}
}