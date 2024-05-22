package com.idep.proposal.carrier.req.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class StarHealthProposerDataProcessor implements Processor
{
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthProposerDataProcessor.class.getName());
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  
	  public void process(Exchange exchange)  throws Exception {
		  
	    try
	    {
	    	String inputReq = (String)exchange.getIn().getBody(String.class);
	        JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
	        int adult=0,child=0,schemeId=0;
	        for(JsonNode node : inputReqNode.get("insuredMembers"))
			{
	        	if(node.get("relationship").asText().equalsIgnoreCase("Self"))
	        	{
	        		((ObjectNode)node).put("isPersonalAccidentApplicable", "true");
	        	}
	        	else
	        	{
	        		((ObjectNode)node).put("isPersonalAccidentApplicable", "false");
	        	}
	        	
	        	JsonDocument defaultHealthParamDoc = serverConfig.getDocBYId("defaultHealthQuoteParam");
	        	
	        	if(node.has("dateOfBirth"))
	        	{
	        		
	        		long age = getAge(node.get("dateOfBirth").asText());
	        		JsonNode defaultHealthParamNode =  objectMapper.readTree(defaultHealthParamDoc.content().toString());
	        		/**
	        		 * Read default age for child from defaultHealthQuoteParam document
	        		 */
	        		long defaultChildAge = defaultHealthParamNode.get("ratingParam").get("maxAllowedChildAge").asLong();

	        		if(age <= defaultChildAge && node.get("relationshipCode").asText().equalsIgnoreCase("CH"))
	        		{
	        			child++;
	        		}
	        		else
	        		{
	        			adult++;
	        		}
	        	}
			}
	        ((ObjectNode)inputReqNode).put("insuredMembers",inputReqNode.get("insuredMembers"));
	        
	        JsonDocument schemeDoc= serverConfig.getDocBYId("HealthSchemeMappingConf-34");
	        
	        if(schemeDoc!=null){
	        	
	        	JsonNode schemeIds =  objectMapper.readTree(schemeDoc.content().toString());
	        	
	        	schemeId = schemeIds.get("schemeId").get(adult+""+child).asInt();
	        }
	        else
	        {
	        	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|Document not found|HealthSchemeMappingConf-34");
	        	throw new ExecutionTerminator();
	        }
	        ((ObjectNode)inputReqNode).put("schemeId", schemeId);
	        exchange.getIn().setBody(inputReqNode);
	        
	    }
	    catch(Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|StarHealthProposerDataProcessor|",e);
			throw new ExecutionTerminator();
	    }
}

	  public long getAge(String dobUI){
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		  Date dob = null;
			Date sysDate =new Date();
			long age=0;
			try {
				dob = format.parse(dobUI);
				sysDate = format.parse(format.format(sysDate));

				//in milliseconds
				long diff = sysDate.getTime() - dob.getTime();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				//System.out.print((diffDays/365) + " Year, ");
				 age= (diffDays/365);
				
			} catch (Exception e) {
				
			}

			
			return age;
	  }
	  
}
