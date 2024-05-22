package com.idep.profession.quote.req.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.profession.quote.util.CorrelationKeyGenerator;
import com.idep.profession.quote.constants.ProfessionQuoteConstant;


/**
 * 
 * @author pranjal.dutta
 * @date 11-01-2019
 *
 */
public class ProfessionRequestProcessor implements Processor {

	ObjectMapper mapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProfessionRequestProcessor.class);
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService QuoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	JsonNode p365QuoteReqQ = null;
	JsonNode p365QuoteResQ = null;
	JsonNode errorNode = null;
	
	JsonNode riskDetails = null;
	JsonNode lobDetails = null;
	JsonNode productDetails = null;
	
	@Override
	public void process(Exchange exchange) throws Exception {
	try{
	    CamelContext camelContext = exchange.getContext();
	    ProducerTemplate template = camelContext.createProducerTemplate();
		JsonNode response =mapper.readTree(exchange.getIn().getBody(String.class));
		String profQuoteId =null; 
		log.info(" Risk Assisment  Response Recived :  "+response);

		JsonNode requestNode = mapper.readTree(exchange.getProperty(ProfessionQuoteConstant.PROFQUOTEREQ).toString());
		p365QuoteReqQ = mapper.readTree(serverConfig.getDocBYId(ProfessionQuoteConstant.P365QUOTEREQCONF).content().toString());
		p365QuoteResQ = mapper.readTree(serverConfig.getDocBYId(ProfessionQuoteConstant.P365QUOTERESCONF).content().toString());
		
		
		if(requestNode.has(ProfessionQuoteConstant.PROF_QUOTE_ID)){
			profQuoteId= requestNode.get(ProfessionQuoteConstant.PROF_QUOTE_ID).asText();
		}else{
			profQuoteId= exchange.getProperty(ProfessionQuoteConstant.PROF_QUOTE_ID).toString();
		}
		
		ArrayNode reqQListNode = this.mapper.createArrayNode();	
		/**
		 * reading UI quote Request from Property 
		 * **/
		String ProfessionQuoteRequest= exchange.getProperty(ProfessionQuoteConstant.PROFQUOTEREQ).toString();
		JsonNode ProfReqNode = null;
		
		if(ProfessionQuoteRequest!=null){
			
			ProfReqNode = mapper.readTree(ProfessionQuoteRequest);
		}
		
		//JsonNode response = new RiskAssessment().getAlgResponse(requestNode);	
		//reading by default document for testing purpose 
		//JsonNode response =  mapper.readTree(serverConfig.getDocBYId("SamplePBAlgorithmResponse").content().toString());
		//((ObjectNode) requestNode).put("productAnalysis", response.get("algResponse").get("productAnalysis"));
	/*	ArrayNode algResponse = null;
		algResponse =(ArrayNode)response.get("algResponse");//(ArrayNode) requestNode.get("algResponse");
		log.info("Algorithm Response :  "+algResponse);
		*/
		
		riskDetails = response.get("algResponse").get("riskAnalysis");
		lobDetails = response.get("algResponse").get("insuranceAnalysis");
		productDetails = response.get("algResponse").get("productAnalysis");
		((ObjectNode) requestNode).put("productAnalysis",productDetails);
		
		log.info("Product List Node : "+productDetails);
		log.info("RiskAnalysis Node : "+riskDetails);
		log.info("LOB Analysis Node : "+lobDetails);
		  if(requestNode.has("RISK_QUOTE_ID")){
			  ((ObjectNode) requestNode).put("RISK_QUOTE_ID", requestNode.get("RISK_QUOTE_ID").asText());
          	
          }else{
        	  if(exchange.getProperty("RISK_QUOTE_ID")!=null){
        	  ((ObjectNode) requestNode).put("RISK_QUOTE_ID",exchange.getProperty("RISK_QUOTE_ID").toString());
        	  }
          }
		for (JsonNode lobNode : productDetails) {
		{	
			try{
				String lobReqQName = p365QuoteReqQ.get(lobNode.get("lob").asText()).asText();
	            String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
	            ((ObjectNode) requestNode).put("productAnalysis", lobNode);
	            ((ObjectNode) requestNode).put("ProfmessageId", correlationKey);
	            ((ObjectNode) requestNode).put("lob", lobNode.get("lob").asText());
	            ((ObjectNode) requestNode).put(ProfessionQuoteConstant.PROF_QUOTE_ID, profQuoteId);
	            log.info("Current LOB :"+lobNode.get("lob").asText());
	            log.info("Request Q :"+lobReqQName);
	            log.info("ProfmessageId :"+correlationKey);
	          
	            if (lobReqQName != null)
	            {
	            	boolean productValidate =true;
	            	if(lobNode.get("lob").asText().equalsIgnoreCase("car")){
	            		if(ProfReqNode.has("carInfo")){
	            			if(ProfReqNode.get("carInfo").has("variantId") ){
	            				if(ProfReqNode.get("carInfo").get("variantId").asText().length() ==0 ){
	            					productValidate =false;	
	            				}
	            			}else{
	            				productValidate =false;
	            			}
	            		}
	            	}
	            	if(lobNode.get("lob").asText().equalsIgnoreCase("bike")){
	            		if(ProfReqNode.has("bikeInfo")){
	            			if(ProfReqNode.get("bikeInfo").has("variantId") ){
	            				if(ProfReqNode.get("bikeInfo").get("variantId").asText().length() ==0 ){
	            					productValidate =false;	
	            				}
	            			}else{
	            				productValidate =false;
	            			}
	            		}
	            	}

	            	if(productValidate){
	              String uri = "activemq:queue:" + lobReqQName;
	              exchange.getIn().setBody(requestNode.toString());
	              exchange.setPattern(ExchangePattern.InOnly);
	              template.send(uri, exchange);
	              
	              ObjectNode resultNode = mapper.createObjectNode();
	              resultNode.put("lob", lobNode.get("lob").asText());
	              resultNode.put("messageId", correlationKey);
	              resultNode.put("qname",p365QuoteResQ.get(lobNode.get("lob").asText()).asText());
	              resultNode.put("PROF_QUOTE_ID",profQuoteId);
	              if(exchange.getProperty(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID) != null){
	            	  log.info("encProfQuoteId :"+exchange.getProperty(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID));
	            	  resultNode.put(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID,exchange.getProperty(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID).toString());
	              }
	              resultNode.put("status",0);
	              log.info("Added Result Node :"+resultNode);
	              reqQListNode.add(resultNode);
	            	}
	            }
			}
			catch(Exception e)
			{
				log.error("Error found in ProfessionsQuoteDecisionProcessor :",e);
				new Exception();
			}
		}
		
        ArrayNode resultnode = mapper.createArrayNode();
        resultnode.add(reqQListNode);
        resultnode.add(riskDetails);   
        resultnode.add(lobDetails);
        resultnode.add(productDetails);

		if (reqQListNode.size() > 0)
	    {
	      log.info("Final Result Node :"+reqQListNode);
		  ObjectNode finalresultNode = this.mapper.createObjectNode();
	      finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").asInt());
	      finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
	      finalresultNode.put("data", resultnode);
	      log.info("Final Result Node with status:"+reqQListNode);
	      exchange.getIn().setBody(finalresultNode);
	   }
	}
	}
    catch (Exception e)
    {
      this.log.error("Exception at ProfessionRequestProcessor ", e);
      ObjectNode finalresultNode = this.mapper.createObjectNode();
      finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
      finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
      finalresultNode.put("data", this.errorNode);
      exchange.getIn().setBody(finalresultNode);
    }
	}
}
