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
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.queue.util.QueueConstants;

public class CriticalIllnessQuoteResultsQListener {
	Logger log = Logger.getLogger(CriticalIllnessQuoteResultsQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService = CBInstanceProvider.getBucketInstance(QueueConstants.QUOTE_BUCKET);
	CBService serverConfig= CBInstanceProvider.getServerConfigInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	JsonNode reqInfo;
	public String onMessage(Message message){
		try{
			if(message instanceof TextMessage){
				TextMessage text = (TextMessage)message;
				 reqInfo=objectMapper.readTree(text.getText());
				JsonObject docObj = JsonObject.fromJson(text.getText());
				log.info("In CriticalIllnessQuoteResultsQListener : "+ docObj);
				String docId=docObj.getString("QUOTE_ID");
				JsonDocument document = this.transService.getDocBYId(docId);

				if(document==null){
					synchronized(this){
						JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
						JsonObject quoteResult = JsonObject.create();
						quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
						log.info("Encrypted QUOTE_ID :"+GenrateEncryptionKey.GetEncryptedKey(docId, keyConfigDoc.get("encryptionKey").asText()));
			            quoteResult.put("encryptedQuoteId", GenrateEncryptionKey.GetEncryptedKey(docId, keyConfigDoc.get("encryptionKey").asText()));
						quoteResult.put("criticalIllnessQuoteRequest", docObj.getObject("criticalillnessQuoteRequest"));
						quoteResult.put("documentType", "criticalIllnessQuoteResults");
						quoteResult.put("businessLineId", 6);
						quoteResult.put("quoteCreatedDate",dateFormat.format(new Date()));
						// get carrier quote response
						JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

						JsonArray criticalIllnessQuoteResultsObj = JsonArray.create();
						criticalIllnessQuoteResultsObj.add(carrierQuoteResponse);
						quoteResult.put("criticalIllnessQuoteResponse", criticalIllnessQuoteResultsObj);

						String doc_status = transService.createAsyncDocument(docId, quoteResult);
						this.log.info("criticalIllness Quote results document : "+docId + " doc_status : "+doc_status);
					}
				}else{
					synchronized(this){
						JsonObject criticalIllnessQuoteData = document.content();
						// load existing car quote results
						JsonArray carQuoteResArr = criticalIllnessQuoteData.getArray("criticalIllnessQuoteResponse");
						try{
							// get carrier quote response
							JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

							carQuoteResArr.add(carrierQuoteResponse);
							criticalIllnessQuoteData.put("criticalIllnessQuoteResponse", carQuoteResArr);

							String doc_status = transService.replaceDocument(docId, criticalIllnessQuoteData);
							this.log.info("criticalIllness Quote results document updated : "+docId+ "status : "+doc_status);
						}catch(Exception e){
							this.log.error("Exception at CriticalIllnessQuoteResultsQListener listener : ",e);
						}
					}
				}
			}else{
				this.log.error("provided message is not TextMessage");
			}
		}catch (JMSException e){
			this.log.error("JMSException at CriticalIllnessQuoteResultsQListener : ",e);
		}catch (Exception e){
			this.log.error("Exception at CriticalIllnessQuoteResultsQListener : ",e);
		}
		return reqInfo.toString();
	}
	
}
     