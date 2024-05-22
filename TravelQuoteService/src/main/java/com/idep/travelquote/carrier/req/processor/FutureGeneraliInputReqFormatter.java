package com.idep.travelquote.carrier.req.processor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;


import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.DateFormatter;
import com.idep.travelquote.util.TravelQuoteConstants;

public class FutureGeneraliInputReqFormatter implements Processor{
	Logger log = Logger.getLogger(FutureGeneraliInputReqFormatter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	SimpleDateFormat dateFormat = new SimpleDateFormat(TravelQuoteConstants.SERVICE_DATE_FORMAT);
	DateFormat UIDDateFormate = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	DateFormatter libFunc = new DateFormatter();
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			String input = exchange.getIn().getBody().toString();
			JsonNode inputReqNode = this.objectMapper.readTree(input);
			log.info("inputReqNode::::::::::::::::"+inputReqNode);
			String startDate =  inputReqNode.findValue("startdate").asText();
			String endDate = inputReqNode.findValue("enddate").asText();
			String memberDOB; 
			long numberOfDays = libFunc.getDaysDifference(startDate,endDate);
			log.info("numberOfDays calculated: "+numberOfDays);
			((ObjectNode)inputReqNode).put("policyTerm", numberOfDays);
			JsonNode travellers = inputReqNode.get("quoteParam").get("travellers");
			for(JsonNode member : travellers)
			{
				int memberage = member.get("age").asInt();
				memberDOB = libFunc.calculateDOB(memberage);
				log.info("Caculated DOB is : "+memberDOB);
				((ObjectNode)member).put("memberDOB",memberDOB );
			}
			
			log.info("inputReqNode in FutureInputProcessor: "+inputReqNode);
			exchange.setProperty(TravelQuoteConstants.UI_QUOTEREQUEST, inputReqNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(inputReqNode));
		}catch(Exception e){
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.FUTUREGENSOAPREQPROCESS+"|ERROR|"+" Error AT FutureGeneraliSOAPReqProcessor :",e);
			throw new ExecutionTerminator();
		}
	}

}


