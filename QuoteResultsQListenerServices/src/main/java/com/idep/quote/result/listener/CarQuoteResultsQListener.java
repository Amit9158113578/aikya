package com.idep.quote.result.listener;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.idep.queue.util.QueueConstants;

public class CarQuoteResultsQListener
{
  Logger log = Logger.getLogger(CarQuoteResultsQListener.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService transService = CBInstanceProvider.getBucketInstance("QuoteData");
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
  
  JsonObject idvConfig = null;
  JsonNode reqInfo;
  
  public String onMessage(Message message)
  {
    try
    {
      synchronized (this)
      {
        if ((message instanceof TextMessage))
        {
          if (this.idvConfig == null) {
            this.idvConfig = ((JsonObject)this.serverConfig.getDocBYId("CarrierIDVCalcConfig").content());
          }
          TextMessage text = (TextMessage)message;
          this.reqInfo = this.objectMapper.readTree(text.getText());
          JsonObject docObj = JsonObject.fromJson(text.getText());
          if (docObj.getString("QUOTE_ID").equals("ERROR"))
          {
            this.log.info("error message ignored by CarQuoteResultsQListener");
          }
          else
          {
        	
            String docId = docObj.getString("QUOTE_ID");
            String encQuoteId = docObj.getString(QueueConstants.ENCRYPT_QUOTE_ID);    
            /*String messageId = docObj.getString(QueueConstants.MESSAGE_ID);*/
            JsonDocument document = this.transService.getDocBYId(docId);
            
            if (document == null)
            {
              JsonObject quoteResult = JsonObject.create();
              if(docObj.containsKey(QueueConstants.MESSAGE_ID) && docObj.get(QueueConstants.MESSAGE_ID) != null ){
		        	String messageId = docObj.getString(QueueConstants.MESSAGE_ID);
		        	quoteResult.put(QueueConstants.MESSAGE_ID,messageId) ;
		      }
              quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
              log.info("Encrypted QUOTE_ID :"+encQuoteId);
              quoteResult.put(QueueConstants.ENCRYPT_QUOTE_ID, encQuoteId);
              quoteResult.put("carQuoteRequest", docObj.getObject("carQuoteRequest"));
              quoteResult.put("documentType", "carQuoteResults");
              quoteResult.put("businessLineId", 3);
              quoteResult.put("quoteCreatedDate", this.dateFormat.format(new Date()));
              
              JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
              try
              {
                double idv = carrierQuoteResponse.getDouble("insuredDeclareValue").doubleValue();
                double minIdvValue = 0.0D;
                double maxIdvValue = 0.0D;
                String carrierId = carrierQuoteResponse.get("carrierId").toString();
                String productId = carrierQuoteResponse.get("productId").toString();

                if (this.idvConfig.containsKey(carrierId))
                {
                  JsonObject idvCalcConfig = this.idvConfig.getObject(carrierId);
                  JsonObject idvCalcPolicyType = idvCalcConfig.getObject(carrierQuoteResponse.getString("policyType"));
                  int minidvPerc = idvCalcPolicyType.getInt("minusIDVPercVehicle").intValue();
                  int maxidvPerc = idvCalcPolicyType.getInt("plusIDVPercVehicle").intValue();
                  
                  minIdvValue = (100 + minidvPerc) * idv / 100.0D;
                  maxIdvValue = (100 + maxidvPerc) * idv / 100.0D;
                }
                else
                {
                  minIdvValue = carrierQuoteResponse.getDouble("minIdvValue").doubleValue();
                  maxIdvValue = carrierQuoteResponse.getDouble("maxIdvValue").doubleValue();
                }
                carrierQuoteResponse.put("minIdvValue", minIdvValue);
                carrierQuoteResponse.put("maxIdvValue", maxIdvValue);
                
                quoteResult.put("lowestIDV", minIdvValue);
                quoteResult.put("highestIDV", maxIdvValue);
                
                JsonArray carQuoteResultsObj = JsonArray.create();
                carQuoteResultsObj.add(carrierQuoteResponse);
                quoteResult.put("carQuoteResponse", carQuoteResultsObj);
                
                quoteResult.put("carrierTransformedReq", JsonObject.create().put(productId, docObj.getObject("carrierTransformedReq")));
                String doc_status = this.transService.createDocument(docId, quoteResult);
                this.log.info("Car Quote results document : " + docId + " doc_status : " + doc_status);
              }
              catch (Exception e)
              {
                this.log.error("Exception at CarQuoteResultsQListener : ", e);
              }
            }
            else
            {
              JsonObject carQuoteData = (JsonObject)document.content();
              
              JsonArray carQuoteResArr = carQuoteData.getArray("carQuoteResponse");
              try
              {
                JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
                
                double idv = carrierQuoteResponse.getDouble("insuredDeclareValue").doubleValue();
                double minIdvValue = 0.0D;
                double maxIdvValue = 0.0D;
                String carrierId = carrierQuoteResponse.getInt("carrierId").toString();
                String productId = carrierQuoteResponse.get("productId").toString();

                if (this.idvConfig.containsKey(carrierId))
                {
                  JsonObject idvCalcConfig = this.idvConfig.getObject(carrierId);
                  JsonObject idvCalcPolicyType = idvCalcConfig.getObject(carrierQuoteResponse.getString("policyType"));
                  int minidvPerc = idvCalcPolicyType.getInt("minusIDVPercVehicle").intValue();
                  int maxidvPerc = idvCalcPolicyType.getInt("plusIDVPercVehicle").intValue();
                  
                  minIdvValue = (100 + minidvPerc) * idv / 100.0D;
                  maxIdvValue = (100 + maxidvPerc) * idv / 100.0D;
                }
                else
                {
                  minIdvValue = carrierQuoteResponse.getDouble("minIdvValue").doubleValue();
                  maxIdvValue = carrierQuoteResponse.getDouble("maxIdvValue").doubleValue();
                }
                if (minIdvValue <= carQuoteData.getDouble("lowestIDV").doubleValue()) {
                  carQuoteData.put("lowestIDV", minIdvValue);
                }
                if (maxIdvValue >= carQuoteData.getDouble("highestIDV").doubleValue()) {
                  carQuoteData.put("highestIDV", maxIdvValue);
                }
                carrierQuoteResponse.put("minIdvValue", minIdvValue);
                carrierQuoteResponse.put("maxIdvValue", maxIdvValue);
                
                JsonArray carQuoteResultsObj = JsonArray.create();
                carQuoteResultsObj.add(carrierQuoteResponse);
                carQuoteResArr.add(carrierQuoteResponse);
                carQuoteData.put("carQuoteResponse", carQuoteResArr);
                
                JsonObject carrierTransReqObj = carQuoteData.getObject("carrierTransformedReq").put(productId, docObj.getObject("carrierTransformedReq"));
                carQuoteData.put("carrierTransformedReq", carrierTransReqObj);
                String doc_status = this.transService.replaceDocument(docId, carQuoteData);
                this.log.info("Car Quote results document updated : " + docId + "status : " + doc_status);
              }
              catch (Exception e)
              {
                this.log.error("Exception at CarQuoteResultsQListener : ", e);
              }
            }
          }
        }
      }
    }
    catch (JMSException e)
    {
      this.log.error("JMSException at CarQuoteResultsQListener: ", e);
    }
    catch (Exception e)
    {
      this.log.error("Exception at CarQuoteResultsQListener: ", e);
    }
    return this.reqInfo.toString();
  }
}
