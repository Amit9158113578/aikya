package com.idep.travelquote.req.transformer;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class TataAIGSoapReqProcessor implements Processor{
	Logger log = Logger.getLogger(TataAIGSoapReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	SimpleDateFormat dateFormat = new SimpleDateFormat(TravelQuoteConstants.SERVICE_DATE_FORMAT);
	DateFormat UIDDateFormate = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			String input = exchange.getIn().getBody().toString();
			JsonNode inputReqNode = this.objectMapper.readTree(input);
			log.debug("inputReqNode::::::::::::::::"+inputReqNode);

			JsonNode productInfoNode = inputReqNode.get(TravelQuoteConstants.PRODUCT_INFO);
			log.debug("productInfoNode:::::::::::"+productInfoNode);

			String docsearchinDb=(TravelQuoteConstants.CARRIER_QUOTE_REQUEST+productInfoNode.get(TravelQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(TravelQuoteConstants.DROOLS_PLANID).intValue()
					).toString();

			log.debug("docsearchindDb:::::::::::::"+docsearchinDb);

			JsonNode carReqConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.CARRIER_QUOTE_REQUEST+productInfoNode.get(TravelQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(TravelQuoteConstants.DROOLS_PLANID).intValue()
					).content().toString());
			log.debug("TravelReqConfigNode::::::::::::"+carReqConfigNode);

			// set Unique UID form Request
			Date currentDate = new Date();
			String UID = UIDDateFormate.format(currentDate);

			((ObjectNode)inputReqNode).put("UID", UID);

			/**
			 * set request configuration document id TravelQuoteRequest
			 * 
			 */
			exchange.setProperty(TravelQuoteConstants.CARRIER_REQ_MAP_CONF,carReqConfigNode);
			exchange.setProperty(TravelQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(inputReqNode));

			exchange.getIn().setHeader(TravelQuoteConstants.REQUESTFLAG, TravelQuoteConstants.TRUE);

			exchange.getIn().setBody(this.objectMapper.writeValueAsString(inputReqNode));
		}catch(Exception e){
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.TATAAIGSOAPREQPROCESS+"|ERROR|"+" Error AT FutureGeneraliSOAPReqProcessor :",e);
			throw new ExecutionTerminator();
		}
	}

}

