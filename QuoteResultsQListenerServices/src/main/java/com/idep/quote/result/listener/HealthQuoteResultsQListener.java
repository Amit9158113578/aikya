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
 * @author sandeep.jadhav
 * store health quote results in database
 */
public class HealthQuoteResultsQListener {

	Logger log = Logger.getLogger(HealthQuoteResultsQListener.class.getName());
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
						this.log.info("error message ignored by HealthQuoteResultsQListener");
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
							quoteResult.put("QUOTE_ID", docObj.getString("QUOTE_ID"));
							quoteResult.put(QueueConstants.ENCRYPT_QUOTE_ID,encQuoteId) ;
							quoteResult.put("quoteRequest", docObj.getObject("quoteRequest"));
							quoteResult.put("documentType", "healthQuoteResults");
							quoteResult.put("businessLineId", 4);
							quoteResult.put("quoteCreatedDate",dateFormat.format(new Date()));

							// get carrier quote response
							JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

							JsonArray healthQuoteResultsObj = JsonArray.create();
							healthQuoteResultsObj.add(carrierQuoteResponse);
							quoteResult.put("quoteResponse", healthQuoteResultsObj);

							String planId = ((Integer)carrierQuoteResponse.getInt("planId")).toString();
							// get transformed request sent to carrier and store in database
							quoteResult.put("carrierTransformedReq",JsonObject.create().put(planId,docObj.getObject("carrierTransformedReq")));

							String doc_status = transService.createDocument(docId, quoteResult);
							this.log.info("Health Quote results document created : "+docId + " doc_status : "+doc_status);

						}
						else
						{
							JsonObject healthQuoteData = document.content();
							// load existing car quote results
							JsonArray carQuoteResArr = healthQuoteData.getArray("quoteResponse");

							// get carrier quote response
							JsonObject carrierQuoteResponse = docObj.getObject("carrierQuoteResponse");

							JsonArray healthQuoteResultsObj = JsonArray.create();
							healthQuoteResultsObj.add(carrierQuoteResponse);

							carQuoteResArr.add(carrierQuoteResponse);
							healthQuoteData.put("quoteResponse", carQuoteResArr);

							// get transformed request node and update
							String planId = ((Integer)carrierQuoteResponse.getInt("planId")).toString();
							JsonObject carrierTransReqObj = healthQuoteData.getObject("carrierTransformedReq")
									.put(planId, docObj.getObject("carrierTransformedReq"));
							healthQuoteData.put("carrierTransformedReq", carrierTransReqObj);

							String doc_status = transService.replaceDocument(docId, healthQuoteData);
							this.log.info("Health Quote results document updated : "+docId+ "status : "+doc_status);

						}
					} 
				}
				else
				{
					this.log.error("provided health quote message is not TextMessage");
				}

			}

		}
		catch (JMSException e)
		{
			this.log.error("HealthQuoteResultsQListener JMSException : ",e);
		}
		catch (Exception e)
		{
			this.log.error("HealthQuoteResultsQListener Exception : "+e);
		}
		return  reqInfo.toString();

	}


}


