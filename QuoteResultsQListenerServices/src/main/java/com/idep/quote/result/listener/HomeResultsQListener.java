package com.idep.quote.result.listener;


import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.encryption.session.GenrateEncryptionKey;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

public class HomeResultsQListener
{
  Logger log = Logger.getLogger(HomeResultsQListener.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService transService = CBInstanceProvider.getBucketInstance("QuoteData");
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
  JsonNode reqInfo;
  
  public String onMessage(Message message)
  {
    try
    {
      if ((message instanceof TextMessage))
      {
        TextMessage text = (TextMessage)message;
        this.reqInfo = this.objectMapper.readTree(text.getText());
        JsonObject docObj = JsonObject.fromJson(text.getText());
        
        this.log.info("inside HomeResultQListner");
        String docId = docObj.getString("QUOTE_ID");
        JsonDocument document = this.transService.getDocBYId(docId);
        this.log.info("fetching document:" + document);
        if (document == null) {
          synchronized (this)
          {
        	JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
            JsonObject quoteResult = JsonObject.create();
            quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
            log.info("Encrypted QUOTE_ID :"+GenrateEncryptionKey.GetEncryptedKey(docId, keyConfigDoc.get("encryptionKey").asText()));
            quoteResult.put("encryptedQuoteId", GenrateEncryptionKey.GetEncryptedKey(docId, keyConfigDoc.get("encryptionKey").asText()));
            quoteResult.put("homeQuoteRequest", docObj.getObject("homeQuoteRequest"));
            quoteResult.put("documentType", "homeQuoteResults");
            quoteResult.put("businessLineId", 7);
            quoteResult.put("quoteCreatedDate", this.dateFormat.format(new Date()));
            
            JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
            
            JsonArray homeQuoteResultsObj = JsonArray.create();
            homeQuoteResultsObj.add(carrierQuoteResponse);
            quoteResult.put("homeQuoteResponse", homeQuoteResultsObj);
            
            String doc_status = this.transService.createAsyncDocument(docId, quoteResult);
            this.log.info("home Quote results document : " + docId + " doc_status : " + doc_status);
          }
        }
        synchronized (this)
        {
          JsonObject homeQuoteData = (JsonObject)document.content();
          
          JsonArray carQuoteResArr = homeQuoteData.getArray("homeQuoteResponse");
          try
          {
            JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");
            
            carQuoteResArr.add(carrierQuoteResponse);
            homeQuoteData.put("homeQuoteResponse", carQuoteResArr);
            
            String doc_status = this.transService.replaceDocument(docId, homeQuoteData);
            this.log.info("home Quote results document updated : " + docId + "status : " + doc_status);
          }
          catch (Exception e)
          {
            this.log.error("Exception at HomeResultsQListener listener : ", e);
          }
        }
      }
      this.log.error("provided message is not TextMessage");
    }
    catch (JMSException e)
    {
      this.log.error("JMSException at HomeResultsQListener : ", e);
    }
    catch (Exception e)
    {
      this.log.error("Exception at HomeResultsQListener : ", e);
    }
    return this.reqInfo.toString();
  }
}
