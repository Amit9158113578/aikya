package com.idep.PBQ.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/***
 * 
 * @author kuldeep.patil
 *
 */
public class PBQuoteDBSender implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PBQuoteDBSender.class.getName());
	CBService transService = CBInstanceProvider.getBucketInstance("QuoteData");
	SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public void process(Exchange exchange){
		 
		 try {
			 
			 String message = exchange.getIn().getBody().toString();
			 JsonNode quoteResponseNode = objectMapper.readTree(exchange.getProperty("PBQResponse").toString());
			 if(quoteResponseNode.get("responseCode").intValue()==1000)
			 {
				  JsonNode pbquoterequestnode = objectMapper.readTree(exchange.getProperty("PBQuoteRequest").toString());
		    	  ArrayNode pbqdataNode =(ArrayNode)quoteResponseNode.get("data");
		    	  /***
		    	   * Below QUOTE_ID means ProfQuoteDocument ID, Reading from DB and updating lob QuoteId for reference
		    	   * **/
		    	  JsonDocument document = this.transService.getDocBYId(pbquoterequestnode.get("PROF_QUOTE_ID").asText());
                  try{
                	  log.info("PBQ journey Service Professioal Quote Document : "+document);
				    	  if(document!=null)
		                  {
		                	  String quoteid = quoteResponseNode.get("QUOTE_ID").textValue();
		                	 JsonNode profQuoteDoc = objectMapper.readTree(document.content().toString());
		                	 ArrayNode lobQuoteNode = null;
		                	 if(profQuoteDoc.has("lobQuoteId")){
		                		 lobQuoteNode = (ArrayNode)profQuoteDoc.get("lobQuoteId");
		                	 }else{
		                		 lobQuoteNode=objectMapper.createArrayNode();
		                	 }
		    		    	   /***
		    			    	* updating QUOTE_ID lob wise in Professional Base Quote Id for CRM & for reference 
		    			    	**/
		                	 ObjectNode quoteData = objectMapper.createObjectNode();
		                	 if(quoteResponseNode.has("businessLineId")){
		                		 quoteData.put("businessLineId", quoteResponseNode.get("businessLineId").asInt());
		                		 quoteData.put("QUOTE_ID",quoteid);
		                		 lobQuoteNode.add(quoteData);
		                		 /*if(quoteResponseNode.get("businessLineId").asText().equalsIgnoreCase("3"))
			    		    	   {
		                			  ((ObjectNode)lobQuoteNode).put("CAR_QUOTE_ID", quoteid);
		                			
		                			 
			    		    	   }else if(quoteResponseNode.get("businessLineId").asText().equalsIgnoreCase("2"))
			    		    	   {
			                			  ((ObjectNode)lobQuoteNode).put("BIKE_QUOTE_ID", quoteid);
			    		    	   }else if(quoteResponseNode.get("businessLineId").asText().equalsIgnoreCase("4"))
			    		    	   {
			                			  ((ObjectNode)lobQuoteNode).put("HEALTH_QUOTE_ID", quoteid);
			    		    	   }else  if(quoteResponseNode.get("businessLineId").asText().equalsIgnoreCase("1"))
			    		    	   {
			                			  ((ObjectNode)lobQuoteNode).put("LIFE_QUOTE_ID", quoteid);
			    		    	   }else  if(quoteResponseNode.get("businessLineId").asText().equalsIgnoreCase("5"))
			    		    	   {
			                			  ((ObjectNode)lobQuoteNode).put("TRAVEL_QUOTE_ID", quoteid);
			    		    	   }
			    		    */
		                		 
		                		 ((ObjectNode)profQuoteDoc).put("lobQuoteId", lobQuoteNode);
		                		 ((ObjectNode)profQuoteDoc).put("lastUpddatedDate",sysDateFormat.format(new Date()));
		                	 
		                		 	   JsonObject pbquoterequestdb = JsonObject.fromJson(profQuoteDoc.toString());
			    		    	   String docstatus = this.transService.replaceDocument(pbquoterequestnode.get("PROF_QUOTE_ID").asText(),pbquoterequestdb);
			    		    	   this.log.info("Professional Quote results document updated : " + pbquoterequestnode.get("PROF_QUOTE_ID").asText() + " status : " + docstatus);
		                	 	}else{
		                	 		log.error("Profession Quote ID  Document not updtaed : due to businessLineId not found in resonse : "+pbquoterequestnode.get("PROF_QUOTE_ID").asText());
		                	 	}
		                	 }
		                	
		                  else
		                  {
		                	   log.error("professional base quoteId not found in QuoteData bucket :"+pbquoterequestnode.get("QUOTE_ID").asText());
		                	  new Exception();
		                  }
                  }catch(Exception e)
                  {
                	  log.error("error at replace professional quote id in quotedata bucket :",e);
                	  new Exception();
                  }
			 }
			 else
			 {
				  this.log.info("Professional Quote response not found : "+quoteResponseNode);
				  new Exception();
			 }
		 }
		 catch(Exception e)
		 {
			 log.error("error in PBQuoteDBSender processor :",e);
			 new Exception();
		 }
	 }
}