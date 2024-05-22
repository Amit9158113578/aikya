package com.idep.quote.result.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.queue.util.QueueConstants;

/**
 * 
 * store travel quote results in database
 */
public class TravelQuoteResultsQListener {

	Logger log = Logger.getLogger(TravelQuoteResultsQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService = CBInstanceProvider.getBucketInstance(QueueConstants.QUOTE_BUCKET);
	CBService serverConfig= CBInstanceProvider.getServerConfigInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	JsonNode reqInfo;
public String onMessage(Message message) 
{
    try
    {
    
      synchronized (this) {
    	  
      if (message instanceof TextMessage)
      {
        TextMessage text = (TextMessage)message;
        reqInfo=objectMapper.readTree(text.getText());
        JsonObject docObj = JsonObject.fromJson(text.getText());
        if(docObj.getString("QUOTE_ID").equals("ERROR"))
        {
        	this.log.info("error message ignored by TravelQuoteResultsQListener");
        }
        else
        {
	        String docId=docObj.getString("QUOTE_ID");
	        JsonDocument document = this.transService.getDocBYId(docId);
	        String encQuoteId = docObj.getString(QueueConstants.ENCRYPT_QUOTE_ID);
	        /**
	         * check whether document is already created in database
	         */
	        if(document==null)
	        {
	        	JsonObject quoteResult = JsonObject.create();
	        	log.info("docObj in  TravelQuoteResultsQListener: "+docObj);
	        	log.info("Q1id"+docObj.getString("QUOTE_ID"));
		        quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
		        quoteResult.put(QueueConstants.ENCRYPT_QUOTE_ID,encQuoteId) ;
		        log.info("quoteRequest: "+docObj.getObject("quoteInputRequest"));
		        quoteResult.put("quoteRequest", docObj.getObject("quoteInputRequest"));
		        quoteResult.put("documentType", "travelQuoteResults");
		        quoteResult.put("businessLineId", 5);
		        quoteResult.put("quoteCreatedDate",dateFormat.format(new Date()));
		        
		        // get carrier quote response
		        JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
		       log.info("carrierQuoteResponse 2"+carrierQuoteResponse);
		        JsonArray travelQuoteResultsObj = JsonArray.create();
		        travelQuoteResultsObj.add(carrierQuoteResponse);
		        log.info("travelQuoteResultsObj info: "+travelQuoteResultsObj);
		        quoteResult.put("quoteResponse", travelQuoteResultsObj);
		        log.info(message);
		        String planId = ((Integer)carrierQuoteResponse.getInt("planId")).toString();
		        log.info("planId "+planId);
		        // get transformed request sent to carrier and store in database
		        //quoteResult.put("carrierTransformedReq",JsonObject.create().put(planId,docObj.getObject("carrierTransformedReq")));
		        log.info("carrierTransformedReq info4: "+docObj.getObject("carrierTransformedReq"));
		        quoteResult.put("carrierTransformedReq", docObj.getObject("carrierTransformedReq"));
		        log.info("Final quoteResult: "+quoteResult);
	        	String doc_status = transService.createDocument(docId, quoteResult);
	        	this.log.info("Travel Quote results document created : "+docId + " doc_status : "+doc_status);
	        	
	       }
	        else
	        {
	        	log.info("In else part: ");
	        	JsonObject travelQuoteData = document.content();
	        	// load existing car quote results
	        	JsonArray travelQuoteResArr = travelQuoteData.getArray("quoteResponse");
	        	
	        	// get carrier quote response
		        JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
		       
		        JsonArray travelQuoteResultsObj = JsonArray.create();
		        travelQuoteResultsObj.add(carrierQuoteResponse);

		        travelQuoteResArr.add(carrierQuoteResponse);
		        travelQuoteData.put("quoteResponse", travelQuoteResArr);
	        	
	        	// get transformed request node and update
	        	 String planId = ((Integer)carrierQuoteResponse.getInt("planId")).toString();
	        	JsonObject carrierTransReqObj = travelQuoteData.getObject("carrierTransformedReq")
	        			.put(planId, docObj.getObject("carrierTransformedReq"));
	        	travelQuoteData.put("carrierTransformedReq", carrierTransReqObj);
	        	
	        	String doc_status = transService.replaceDocument(docId, travelQuoteData);
	        	this.log.info("Travel Quote results document updated : "+docId+ "status : "+doc_status);
	      
	        }
        } 
      }
      else
      {
    	  this.log.error("provided travel quote message is not TextMessage");
      }
      
     }
      
	}
    catch (JMSException e)
    {
    	this.log.error("TravelQuoteResultsQListener JMSException : ",e);
    }
    catch (Exception e)
    {
     	this.log.error("TravelQuoteResultsQListener Exception : "+e);
    }
    return  reqInfo.toString();
    
}


}



