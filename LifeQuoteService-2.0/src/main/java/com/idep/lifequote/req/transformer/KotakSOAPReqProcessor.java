package com.idep.lifequote.req.transformer;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;


import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.lifequote.exception.processor.ExecutionTerminator;
import com.idep.lifequote.util.LifeQuoteConstants;
//import com.idep.Lifequote.util.LifeQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class KotakSOAPReqProcessor implements Processor{
	Logger log = Logger.getLogger(KotakSOAPReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			String input = exchange.getIn().getBody().toString();
			JsonNode inputReqNode = this.objectMapper.readTree(input);
			log.info("inputReqNode: "+inputReqNode);
			
			int policyTerm=inputReqNode.get("quoteParam").get("policyTerm").asInt();
			
			exchange.setProperty("carrierId", inputReqNode.get("productInfo").get("carrierId"));
			exchange.setProperty("productId", inputReqNode.get("productInfo").get("productId"));
			exchange.setProperty("productInfo", inputReqNode.get("productInfo"));
				
		      
		      //log.info("PolicyTerm Validation Started for Kotak : "+policyTerm);
		      //this.log.debug("Life ProductInfo: " + exchange.getProperty("productInfo"));
		      
			if(inputReqNode.get("productInfo").has("minTermLimit")){
				      int minPolicyTerm= inputReqNode.get("productInfo").get("minTermLimit").asInt();
				      //log.info("minPolicyTerm Cheking : "+minPolicyTerm);
				    //checking Validaton for policyterm and setting flag accordingly
					if(policyTerm>minPolicyTerm)
					{
					 ((ObjectNode)inputReqNode).put("CoverageTill75","Y");
					}else{
						((ObjectNode)inputReqNode).put("CoverageTill75","N");
					}
			}else{
				log.error(LifeQuoteConstants.KOTAKLIFESOAPREQPROCESS+"|ERROR|unable to process request :minTermLimit not found ");
				throw new ExecutionTerminator();				
			}
			//Taking ProductInfo from UI
			//JsonNode productInfoNode = inputReqNode.get("productInfo");
			
			                                                           
			/*JsonNode carReqConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.CARRIER_QUOTE_REQUEST+productInfoNode.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(LifeQuoteConstants.PRODUCTID).intValue()
					).content().toString());
			*/
			
			
			/*  //setting config document
		      JsonNode carReqConfigNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("LifeQuoteRequest-" + productInfoNode.get("carrierId").intValue() + "-" + productInfoNode.get("productId").intValue()).content()).toString());
		      
			
			log.info("carReqConfigNode: "+carReqConfigNode);

			*//**
			 * set request configuration document id LifeQuoteRequest
			 * 
			 *//*
			
			exchange.getIn().setHeader("reqFlag", "True");
			exchange.setProperty(LifeQuoteConstants.CARRIER_REQ_MAP_CONF,carReqConfigNode);
			log.info("property set1"+LifeQuoteConstants.CARRIER_REQ_MAP_CONF+" "+carReqConfigNode);
*/
			//log.info("KOTAK SOAP REQ node genrated : "+inputReqNode);
			exchange.setProperty("inputRequest", inputReqNode);
			//exchange.getIn().setHeader("reqFlag", "True");
			exchange.setProperty(LifeQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(inputReqNode));

			//log.info("KOTAK SOAP REQ PROCESS COMPLTED : "+inputReqNode);
			exchange.getIn().setHeader(LifeQuoteConstants.REQUESTFLAG, LifeQuoteConstants.TRUE);

			exchange.getIn().setBody(this.objectMapper.writeValueAsString(inputReqNode));
		}catch(Exception e){
			log.error(LifeQuoteConstants.KOTAKLIFESOAPREQPROCESS+"|ERROR|Error AT KotakSOAPReqProcessor :",e);
			throw new ExecutionTerminator();
		}
	}

}

