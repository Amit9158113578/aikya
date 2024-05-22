package com.idep.healthquote.res.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;


public class CalculateDOBMinMaxProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CalculateDOBMinMaxProcessor.class.getName());
	CBService serverConfig = null;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String response = exchange.getIn().getBody(String.class);
			JsonNode responseNode = objectMapper.readTree(response);
			
			JsonNode inputReqNode= objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.UI_QUOTEREQUEST).toString());
			
			ArrayNode selectedFamilyMembers= (ArrayNode)inputReqNode.get("personalInfo").get("selectedFamilyMembers");
			for(JsonNode memeber : selectedFamilyMembers ){
				String minDOB=getMinDate(memeber.get("age").asInt());
				String maxDOB=getMaxDate(memeber.get("age").asInt());
				((ObjectNode)memeber).put("minDOB", minDOB);
				((ObjectNode)memeber).put("maxDOB", maxDOB);
			}
			if(responseNode.has(HealthQuoteConstants.CARRIER_REQUEST_FORM)){
			((ObjectNode)responseNode.get(HealthQuoteConstants.CARRIER_REQUEST_FORM)).put("selectedFamilyMembers",selectedFamilyMembers);
			}else{
				((ObjectNode)responseNode).put("selectedFamilyMembers",selectedFamilyMembers);
			}
			//log.info("min and max DOB addded in response : "+responseNode);
			exchange.getIn().setBody(responseNode);	
		}catch(Exception e){
			log.error("error at CalculateDOBMinMaxProcessor :",e);
		}
	}
	
	
	public  String getMaxDate(int age){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date sysDate = new Date();
		try {	
		Calendar cal = Calendar.getInstance();
		String currentDate = formatter.format(sysDate);
		cal.setTime(formatter.parse(currentDate));
		cal.add(Calendar.YEAR, -age);
		Date minDOB = cal.getTime();
		System.out.println("MAX DOB Date : "+formatter.format(minDOB));
		return formatter.format(minDOB);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * if above return fail to calculate minimum DOB then it will return sysDate 
		 * */
		return formatter.format(sysDate);
	}
	
	public String getMinDate(int age){
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date sysDate = new Date();
		try {	
		Calendar cal = Calendar.getInstance();
		String currentDate = formatter.format(sysDate);
		cal.setTime(formatter.parse(currentDate));
		cal.add(Calendar.YEAR, -(age+1));
		cal.add(Calendar.DATE, +1);
		Date minDOB = cal.getTime();
		System.out.println("Min DOB : "+formatter.format(minDOB));
		return formatter.format(minDOB);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * if above return fail to calculate minimum DOB then it will return sysDate 
		 * */
		return formatter.format(sysDate);
	}
}
