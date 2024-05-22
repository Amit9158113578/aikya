package com.idep.PBQ.Common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
/***
 * 
 * @author kuldeep.patil
 *
 */
public class PBQuoteResponseProcessor implements Processor {

	CBService cbService =CBInstanceProvider.getServerConfigInstance();
	JsonNode lobRequestNode;
	ObjectMapper mapper=new ObjectMapper();
	Logger log=Logger.getLogger(PBQuoteResponseProcessor.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String request = exchange.getIn().getBody().toString();
			String pbquoterequeststr = exchange.getProperty("PBQuoteRequest").toString();
			JsonNode pbquoterequestnode = mapper.readTree(pbquoterequeststr);
			String replace = request.replace("\"}\"", "\"}").replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}");
		       JsonNode quoteresponse = mapper.readTree(replace);
		       
		       if(lobRequestNode==null)
		       {
		    	   lobRequestNode=mapper.readTree(cbService.getDocBYId("LobQuoteRequestConfig").content().toString());
		       }   
		         JsonNode lobRequestArray = lobRequestNode.get("lobRequest");
		         for (JsonNode lobRequest : lobRequestArray) {
		    	   if(quoteresponse.has("businessLineId"))
		    	   {
		    		   if(quoteresponse.get("businessLineId").asInt()==lobRequest.get("lob").asInt())
		    		   {
		    			   ((ObjectNode)quoteresponse).put(lobRequest.get("requestNode").textValue(), mapper.readTree(exchange.getProperty("LobQuoteRequest").toString()));
		    		   }
		    	   }
		    	   else
			        {
			        	log.error("businessLineId not found in lob quote response :"+quoteresponse);
			        }
		      	}
	    		((ObjectNode)quoteresponse).put("messageId", pbquoterequestnode.get("ProfmessageId").asText());
		         exchange.setProperty("PBQResponse", quoteresponse);
	    	    exchange.getIn().setHeader("JMSCorrelationID", pbquoterequestnode.get("ProfmessageId").asText());
    		  	exchange.getIn().setBody(quoteresponse.toString());
		}catch(Exception e)
		{
			log.error("error found at PBQuoteResponseProcessor processor :",e);
			e.printStackTrace();
 		     new Exception();
		}
	}
}
