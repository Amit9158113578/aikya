package com.idep.xpath.mapper.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.api.xpath.mapper.XPathMapper;
import com.idep.xpath.mapper.util.MapperConstants;

/**
 * The p365xpathmapper producer.
 * @author sandeep.jadhav
 */
public class p365xpathmapperProducer extends DefaultProducer {

	XPathMapper xpathMapper =  new XPathMapper();
	Logger log = Logger.getLogger(p365xpathmapperProducer.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode responseConfigNode = null;
	JsonNode errorNode=null;
	
	@SuppressWarnings("unused")
	private p365xpathmapperEndpoint endpoint;

    public p365xpathmapperProducer(p365xpathmapperEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
    	
    	try
    	{
    	
    	if(responseConfigNode==null)
    	{
    		this.responseConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(MapperConstants.RESPONSE_MESSAGES).content().toString());
    	}
    	
    	
    	String inputReq = exchange.getIn().getBody().toString();
    	JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
    	/**
    	 *  get request type
    	 */
    	String requestType = inputReqNode.findValue(MapperConstants.REQUEST_TYPE).asText();
    	JsonNode reqTypeConfigNode=null;
    	JsonDocument reqTypeConfigDoc = this.serverConfig.getDocBYId(requestType);
    	
    	if(reqTypeConfigDoc!=null){
    		reqTypeConfigNode = this.objectMapper.readTree(reqTypeConfigDoc.content().toString());
    	}else{
    		log.error("requestType Configuration Document not found  :  "+requestType);	
    	}

    	/**
    	 *  get fields to form document id for carrier request
    	 */
    	ArrayNode documentIdFields = (ArrayNode)reqTypeConfigNode.get(MapperConstants.DOCUMENTID_FIELDS);
    	String docId = "";
    	if(exchange.getIn().getHeaders().containsKey("documentId"))
    	{
    		docId = exchange.getIn().getHeader("documentId").toString();
    	}
    	else
    	{
	    	docId = requestType;
	    	for(int i=0;i<documentIdFields.size();i++)
	    	{
	    		docId = docId +"-"+ inputReqNode.findValue(documentIdFields.get(i).textValue()).asText();
	    	}
	    	log.debug("p365xpathmapperProducer docId :  "+docId);
    	}
    	log.debug("request type : "+requestType+" mapper docId : "+docId);
    	/**
    	 * clear all existing headers before consuming web service and set all required headers using configuration
    	 */
    	exchange.getIn().setHeaders(new HashMap<String,Object>());
    	/**
    	 *  load required input parameters
    	 */
    	log.debug("inputReqFields reqTypeConfigNode.get(MapperConstants.INPUTREQFIELDS) : "+reqTypeConfigNode.get(MapperConstants.INPUTREQFIELDS));
    	ArrayNode inputReqFields = (ArrayNode)reqTypeConfigNode.get(MapperConstants.INPUTREQFIELDS);
    	
    	JsonNode carrierConfigNode=null;
    	JsonDocument carrierConfigDoc =this.serverConfig.getDocBYId(docId);
    	if(carrierConfigDoc!=null){
    	carrierConfigNode= this.objectMapper.readTree(carrierConfigDoc.content().toString());
    	}else{
    		log.error("Carrier Mapping Request configuration Document not found  :  "+docId);	
    	}
    	JsonNode sampleReqNode =null;
    	JsonDocument carrierSampleDoc =this.serverConfig.getDocBYId(carrierConfigNode.get(MapperConstants.SAMPLE_REQUESTID).textValue());
    	if(carrierSampleDoc!=null){
    	sampleReqNode =  this.objectMapper.readTree(carrierSampleDoc.content().toString());
    	}else{
    		log.error("Carrier Mapping Sample Request configuration Document not found  :  "+carrierConfigNode.get(MapperConstants.SAMPLE_REQUESTID).textValue());
    	}
    	/**
    	 *  call xpath mapper API
    	 */
    	JsonNode mapperResponseNode =  xpathMapper.updateRequest(inputReqFields,inputReqNode, sampleReqNode, carrierConfigNode);

    	if(mapperResponseNode!=null)
    	{
	    	/* set success response */
	    	exchange.getIn().setHeader(MapperConstants.CARRIER_URL_CODE,carrierConfigNode.get(MapperConstants.URL).textValue());
	    	exchange.getIn().removeHeader(MapperConstants.CAMEL_HTTP_PATH);
			exchange.getIn().removeHeader(MapperConstants.CAMEL_HTTP_URI);
			
			if(carrierConfigNode.has("webserviceType"))
			{
				exchange.getIn().setHeader("webserviceType",carrierConfigNode.get("webserviceType").textValue());
			}
			/**
			 * set all headers required to consume a web service
			 */
			if(carrierConfigNode.has("requestHeaders"))
			{
				@SuppressWarnings("unchecked")
				Map<Object,Object> headerMap = this.objectMapper.convertValue(carrierConfigNode.get("requestHeaders"), Map.class);
				
				for (Map.Entry<Object,Object> entry : headerMap.entrySet()) {
				    
					exchange.getIn().setHeader(entry.getKey().toString(),entry.getValue().toString());
				}
			}
			else
			{
				exchange.getIn().setHeader(Exchange.CONTENT_TYPE,carrierConfigNode.get(MapperConstants.HTTP_CONTENT_TYPE).textValue());
				exchange.getIn().setHeader(Exchange.HTTP_METHOD,carrierConfigNode.get(MapperConstants.HTTP_METHOD).textValue());
				exchange.getIn().setHeader(Exchange.ACCEPT_CONTENT_TYPE,carrierConfigNode.get(MapperConstants.CAMEL_ACCEPT_CONTENT_TYPE).textValue());
			}
			
			exchange.getIn().setHeader(MapperConstants.MAPPER_REQ_FLAG_CODE, MapperConstants.MAPPER_REQSUCC_FLAG_RES);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(mapperResponseNode));
    	}
    	else
    	{
    		/* set error response */
    		exchange.getIn().setHeader(MapperConstants.MAPPER_REQ_FLAG_CODE, MapperConstants.MAPPER_REQERR_FLAG_RES);
		    ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(MapperConstants.RESPONSE_CODE, this.responseConfigNode.get(MapperConstants.ERRORCODE).intValue());
		    objectNode.put(MapperConstants.MESSAGE, this.responseConfigNode.get(MapperConstants.ERRORMESSAGES).textValue());
		    objectNode.put(MapperConstants.DATA, this.errorNode);
		    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
    	}
    	}
    	catch(Exception e)
    	{
    		log.error("Exception at p365xpathmapperProducer : ",e);
    	}
    }
}

