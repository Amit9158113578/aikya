
package com.idep.proposal.impl.service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.carrier.res.processor.BhartiAxaSoapResFormatter;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalAgeCalculator implements Processor 
{

	ObjectMapper objectMapper=new ObjectMapper();
	Logger log = Logger.getLogger(TravelProposalAgeCalculator.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String MemberDOB ="";
			String uiInputreq = (String)exchange.getIn().getBody(String.class);
			JsonNode uiInputRequest = objectMapper.readTree(uiInputreq);
			log.info("Ui Input Request to getting sumInsured as selected in UI"+ uiInputRequest);
			long memberAge=0;
			if(uiInputRequest.has("travellerDetails"))
			{
				ArrayNode travellerDetailsNode =(ArrayNode) uiInputRequest.get("travellerDetails");
				for(JsonNode node : travellerDetailsNode){
					log.info("UI Member Age  :"+((ObjectNode)node).get("dateOfBirth").asText());
					MemberDOB = node.get("dateOfBirth").asText();
					memberAge = getAge(MemberDOB);
					((ObjectNode)node).put("memberAge", memberAge);
					log.info("uiSumInsured value: "+MemberDOB);
				//uiSumInsured = 100000;
				}
			}
			
			
		
			log.info("final Input Request BhartiAxa:"+uiInputRequest);
			
			exchange.getIn().setBody(uiInputRequest);
		}
		catch(Exception e)
		{
			log.error("Exception at TravelProposalReqPostProcessor : ", e);
		}
	}
	 public long getAge(String date)
			    throws ParseException
			  {
			    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			    Date dob = null;
			    Date sysDate = new Date();
			    long age = 0L;
			    try
			    {
			      dob = format.parse(date);
			      sysDate = format.parse(format.format(sysDate));
			      
			      long diff = sysDate.getTime() - dob.getTime();
			      long diffDays = diff / 86400000L;
			      
			      age = diffDays / 365L;
			    }
			    catch (Exception e)
			    {
			      e.printStackTrace();
			    }
			    return age;
			  }
}
