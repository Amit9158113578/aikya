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
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.queue.util.QueueConstants;
/**
 * 
 * @author sandeep.jadhav
 * store bike quote results in database
 */
public class BikeQuoteResultsQListener {
	
	Logger log = Logger.getLogger(BikeQuoteResultsQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService = CBInstanceProvider.getBucketInstance(QueueConstants.QUOTE_BUCKET);
	CBService serverConfig= CBInstanceProvider.getServerConfigInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	JsonObject idvConfig= null;
	 JsonNode reqInfo;
	 
	public String onMessage(Message message) 
	{
	    try
	    {
	    
	      synchronized (this) {
	    		
	      if (message instanceof TextMessage)
	      {
	    	if(this.idvConfig==null)
	    	{
	    		this.idvConfig = this.serverConfig.getDocBYId("BikeCarrierIDVCalcConfig").content();
	    	}
	    
	        TextMessage text = (TextMessage)message;
	        reqInfo=objectMapper.readTree(text.getText());
	        JsonObject docObj = JsonObject.fromJson(text.getText());
	        
	        if(docObj.getString("QUOTE_ID").equals("ERROR"))
	        {
	        	this.log.info("error message ignored by BikeQuoteResultsQListener");
	        }
	        else
	        {
		        String docId=docObj.getString("QUOTE_ID");
		        String encQuoteId = docObj.getString(QueueConstants.ENCRYPT_QUOTE_ID);
		        
		        JsonDocument document = this.transService.getDocBYId(docId);
		        if(document==null)
		        {
		        	JsonObject quoteResult = JsonObject.create();
		        	if(docObj.containsKey(QueueConstants.MESSAGE_ID) && docObj.get(QueueConstants.MESSAGE_ID) != null ){
			        	String messageId = docObj.getString(QueueConstants.MESSAGE_ID);
			        	quoteResult.put(QueueConstants.MESSAGE_ID,messageId) ;
			        }
			        quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
		            quoteResult.put(QueueConstants.ENCRYPT_QUOTE_ID,encQuoteId) ;
			        quoteResult.put("bikeQuoteRequest", docObj.getObject("quoteInputRequest"));
			        quoteResult.put("documentType", "bikeQuoteResults");
			        quoteResult.put("businessLineId", 2);
			        quoteResult.put("quoteCreatedDate",dateFormat.format(new Date()));
			        
			        // get carrier quote response
			        JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
			        try {
			        // find IDV value and set min and max idv values
			        double idv = carrierQuoteResponse.getDouble("insuredDeclareValue");
			        double minIdvValue = 0;
			        double maxIdvValue = 0;
			        
			        String carrierId = ((Integer)carrierQuoteResponse.getInt("carrierId")).toString();
			        String productId = ((Integer)carrierQuoteResponse.getInt("productId")).toString();

			        if(this.idvConfig.containsKey(carrierId))
			        {
				        JsonObject idvCalcConfig = this.idvConfig.getObject(carrierId);
				        JsonObject idvCalcPolicyType = idvCalcConfig.getObject(carrierQuoteResponse.getString("policyType"));
				        int minidvPerc = idvCalcPolicyType.getInt("minusIDVPercVehicle");
				        int maxidvPerc = idvCalcPolicyType.getInt("plusIDVPercVehicle");
				        
				        minIdvValue = ((100-minidvPerc)*idv)/100.0;
				        maxIdvValue = ((100+maxidvPerc)*idv)/100.0;
			        }
			        else
			        {
			        	 minIdvValue = carrierQuoteResponse.getDouble("minIdvValue");
				         maxIdvValue = carrierQuoteResponse.getDouble("maxIdvValue");
			        }
				        
				    carrierQuoteResponse.put("minIdvValue", minIdvValue);
				    carrierQuoteResponse.put("maxIdvValue", maxIdvValue);
				        
				    // set lowest IDV and highest IDV
				    quoteResult.put("lowestIDV", minIdvValue);
				    quoteResult.put("highestIDV", maxIdvValue);
				        
				    //carrierQuoteResponse.put("minIdvPercentage", idvCalcPolicyType.getInt("minusIDVPercVehicle"));
				    //carrierQuoteResponse.put("maxIdvPercentage", idvCalcPolicyType.getInt("plusIDVPercVehicle"));
			        
				        
			        JsonArray carQuoteResultsObj = JsonArray.create();
			        carQuoteResultsObj.add(carrierQuoteResponse);
			        quoteResult.put("bikeQuoteResponse", carQuoteResultsObj);
			        // get transformed request sent to carrier and store in database
			        quoteResult.put("carrierTransformedReq",JsonObject.create().put(productId,docObj.getObject("carrierTransformedReq")));
			        
		        	String doc_status = transService.createDocument(docId, quoteResult);
		        	this.log.info("Bike Quote results document : "+docId + " doc_status : "+doc_status);
		        	
			       }
			       catch(Exception e )
			       {
			        	this.log.error("Exception at BikeQuoteResultsQListener : ",e);
			       }
		       	
		       }
		        else
		        {
		        	JsonObject carQuoteData = document.content();
		        	// load existing car quote results
		        	JsonArray carQuoteResArr = carQuoteData.getArray("bikeQuoteResponse");
		        	try {
		        	// get carrier quote response
			        JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
			       
			        // find IDV value and set min and max idv values
			        double idv = carrierQuoteResponse.getDouble("insuredDeclareValue");
			        double minIdvValue = 0;
			        double maxIdvValue = 0;
			        String carrierId = ((Integer)carrierQuoteResponse.getInt("carrierId")).toString();
			        String productId = ((Integer)carrierQuoteResponse.getInt("productId")).toString();

			        if(this.idvConfig.containsKey(carrierId))
			        {
				        JsonObject idvCalcConfig = this.idvConfig.getObject(carrierId);
				        JsonObject idvCalcPolicyType = idvCalcConfig.getObject(carrierQuoteResponse.getString("policyType"));
				        int minidvPerc = idvCalcPolicyType.getInt("minusIDVPercVehicle");
				        int maxidvPerc = idvCalcPolicyType.getInt("plusIDVPercVehicle");
				        
				         minIdvValue = ((100-minidvPerc)*idv)/100.0;
				         maxIdvValue = ((100+maxidvPerc)*idv)/100.0;
			        }
			        else
			        {
			        	 minIdvValue = carrierQuoteResponse.getDouble("minIdvValue");
				         maxIdvValue = carrierQuoteResponse.getDouble("maxIdvValue");
			        }
			        
			        // set lowest and highest IDV values
			        if(minIdvValue<=carQuoteData.getDouble("lowestIDV"))
			        {
			        	carQuoteData.put("lowestIDV", minIdvValue);
			        }
			        if(maxIdvValue>=carQuoteData.getDouble("highestIDV"))
			        {
			        	carQuoteData.put("highestIDV", maxIdvValue);
			        }
			        
			        
			        carrierQuoteResponse.put("minIdvValue", minIdvValue);
			        carrierQuoteResponse.put("maxIdvValue", maxIdvValue);
			        
			        //carrierQuoteResponse.put("minIdvPercentage", idvCalcPolicyType.getInt("minusIDVPercVehicle"));
			        //carrierQuoteResponse.put("maxIdvPercentage", idvCalcPolicyType.getInt("plusIDVPercVehicle"));
			        
			        JsonArray carQuoteResultsObj = JsonArray.create();
			        carQuoteResultsObj.add(carrierQuoteResponse);
	
			        carQuoteResArr.add(carrierQuoteResponse);
		        	carQuoteData.put("bikeQuoteResponse", carQuoteResArr);
		        	
		        	// get transformed request node and update
		        	JsonObject carrierTransReqObj = carQuoteData.getObject("carrierTransformedReq")
		        			.put(productId, docObj.getObject("carrierTransformedReq"));
		        	carQuoteData.put("carrierTransformedReq", carrierTransReqObj);
		        	
		        	String doc_status = transService.replaceDocument(docId, carQuoteData);
		        	this.log.info("Bike Quote results document updated : "+docId+ "status : "+doc_status);
		        }
		        catch(Exception e)
		        {
		        		this.log.error("Exception at BikeQuoteResultsQListener : ",e);
		        }
		      
		    }
	      } 
	    }
	    }
	      
	    }
	    catch (JMSException e)
	    {
	    	this.log.error("JMSException at BikeQuoteResultsQListener : ",e);
	    }
	    catch (Exception e)
	    {
	    	this.log.error("Exception at BikeQuoteResultsQListener : ",e);
	    }
	    
	    return reqInfo.toString();
	}


}
