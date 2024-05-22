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
 * store life quote results in database
 */
public class LifeQuoteResultsQListener {
	Logger log = Logger.getLogger(LifeQuoteResultsQListener.class.getName());
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

				String docId=docObj.getString("QUOTE_ID");
				JsonDocument document = this.transService.getDocBYId(docId);
				String encQuoteId = docObj.getString(QueueConstants.ENCRYPT_QUOTE_ID);
				if(document==null){
					synchronized(this){
						JsonObject quoteResult = JsonObject.create();
						quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
						quoteResult.put(QueueConstants.ENCRYPT_QUOTE_ID,encQuoteId) ;
						quoteResult.put("lifeQuoteRequest", docObj.getObject("lifeQuoteRequest"));
						quoteResult.put("documentType", "lifeQuoteResults");
						quoteResult.put("businessLineId", 1);
						quoteResult.put("quoteCreatedDate",dateFormat.format(new Date()));
						// get carrier quote response
						JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

						JsonArray lifeQuoteResultsObj = JsonArray.create();
						lifeQuoteResultsObj.add(carrierQuoteResponse);
						quoteResult.put("lifeQuoteResponse", lifeQuoteResultsObj);

						String doc_status = transService.createAsyncDocument(docId, quoteResult);
						this.log.debug("Life Quote results document : "+docId + " doc_status : "+doc_status);
					}
				}else{
					synchronized(this){
						JsonObject lifeQuoteData = document.content();
						// load existing car quote results
						JsonArray carQuoteResArr = lifeQuoteData.getArray("lifeQuoteResponse");
						try{
							// get carrier quote response
							JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

							carQuoteResArr.add(carrierQuoteResponse);
							lifeQuoteData.put("lifeQuoteResponse", carQuoteResArr);

							String doc_status = transService.replaceDocument(docId, lifeQuoteData);
							this.log.debug("Life Quote results document updated : "+docId+ "status : "+doc_status);
						}catch(Exception e){
							this.log.error("Exception at LifeQuoteResultsQListener listener : ",e);
						}
					}
				}
			}else{
				this.log.error("provided message is not TextMessage");
			}
		}catch (JMSException e){
			this.log.error("JMSException at LifeQuoteResultsQListener : ",e);
		}catch (Exception e){
			this.log.error("Exception at LifeQuoteResultsQListener : ",e);
		}
		return reqInfo.toString();
	}
	
	
}