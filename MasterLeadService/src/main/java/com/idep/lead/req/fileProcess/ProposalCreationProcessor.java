package com.idep.lead.req.fileProcess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.sugar.util.SugarCRMConstants;


public class ProposalCreationProcessor implements Processor {
	CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	ObjectMapper mapper=new ObjectMapper();
	Logger log =Logger.getLogger(ProposalCreationProcessor.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		try{
			
				String request = exchange.getIn().getBody().toString();
				JsonNode requestNode = mapper.readTree(request);
				if(requestNode.has("proposalId"))
				{
					JsonDocument ProposalDoc = policyTransaction.getDocBYId(requestNode.get("proposalId").textValue());
					if(ProposalDoc!=null)
					{
						 JsonNode proposalNode = mapper.readTree(ProposalDoc.content().toString());
						 requestNode = proposalNode.get("proposalRequest");
						 exchange.getIn().setHeader("requiredMapping", "N");
					}
				}
				if(requestNode.has("messageId") && requestNode.get("messageId").textValue()!=null)
				{
					JsonDocument leadJsonDoc = policyTransaction.getDocBYId("LeadProfile-"+requestNode.findValue("messageId").textValue());
					if(leadJsonDoc!=null)
					{
						  JsonNode leadRequestNode = mapper.readTree(leadJsonDoc.content().toString());
						  if(leadRequestNode.get("LeadDetails").has("lastActivity"))
						  {
							  requestNode = leadRequestNode.get("LeadDetails").get("lastActivity");
							  if(!requestNode.has("carInfo"))
							  {
								  ((ObjectNode)requestNode).put("carInfo", requestNode.get("vehicleInfo"));
								  ((ObjectNode)requestNode).remove("vehicleInfo");
							  }
						  }
						  else
						  {
							  requestNode = leadRequestNode.get("LeadDetails").get("firstActivity");
						  }
						  if(leadRequestNode.has("latestQUOTE_ID"))
						  {
							  ((ObjectNode)requestNode).put("QUOTE_ID",leadRequestNode.findValue("latestQUOTE_ID").textValue());
						  }
						 ((ObjectNode)requestNode).put("requestType", "SaveProposalRequest");
						 ((ObjectNode)requestNode).put("businessLine", "Car");
					exchange.getIn().setHeader("requiredMapping", "Y");
					  }
					{
						log.error("lead document not found in bucket :"+"LeadProfile-"+requestNode.findValue("messageId").textValue());
					}
				}
				
				else
				{
					log.error("Message Id not found in request :");
				}
				exchange.getIn().setBody(requestNode);	
		
		}
		catch(NullPointerException e)
		{
			log.error(ProposalCreationProcessor.class+"-"+Thread.currentThread().getStackTrace()[1].getMethodName() +"-"+Thread.currentThread().getStackTrace()[1].getLineNumber()+"Null pointer exception in ProposalCreationProcessor",e);
		}
		catch(Exception e)
		{
			log.error(ProposalCreationProcessor.class+"-"+Thread.currentThread().getStackTrace()[1].getMethodName() +"-"+Thread.currentThread().getStackTrace()[1].getLineNumber()+"exception in ProposalCreationProcessor",e);

		}
		
	}

}
