package com.idep.profession.quote.DB;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.profession.quote.constants.ProfessionQuoteConstant;


public class ProfessionalQuoteDBStore implements Processor {

	
	Logger log = Logger.getLogger(ProfessionalQuoteDBStore.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService QuoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	SimpleDateFormat sysDateFormat =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			String profQuoteId =null;
			if(reqNode!=null){
				long quoteSeq = serverConfig.updateDBSequence("SEQPROFQUOTE");
		    	if(quoteSeq==-1)
		    	{
		    		quoteSeq=serverConfig.updateDBSequence("SEQPROFQUOTE");
		    		if(quoteSeq==-1)
		    		{	Thread.sleep(500);
		    		quoteSeq=this.serverConfig.updateDBSequence("SEQPROFQUOTE");
		    		}
		    	}
				profQuoteId = DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("profQuoteId").asText() +quoteSeq;
				JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
				 String encProfQuoteId = GenrateEncryptionKey.GetEncryptedKey(profQuoteId, keyConfigDoc.get("encryptionKey").asText());
				 log.info("Encrypted ProfQUOTE_ID :" + encProfQuoteId);
				((ObjectNode)reqNode).put("creationDate",sysDateFormat.format(new Date()));
				((ObjectNode)reqNode).put("QUOTE_ID",profQuoteId);
				((ObjectNode)reqNode).put(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID,encProfQuoteId);
				String doc_status =   QuoteData.createDocument(profQuoteId,JsonObject.fromJson(reqNode.toString()));
				log.info("Professional Quote Id Generated : "+profQuoteId+" doc_status : "+doc_status);
				if(doc_status!=null && doc_status.equalsIgnoreCase("doc_exist") ){
					 doc_status = QuoteData.replaceDocument(profQuoteId,JsonObject.fromJson(reqNode.toString()));
					 log.info("Professional Quote Id Updated  : "+profQuoteId+" doc_status : "+doc_status);
				}
				exchange.setProperty("PROF_QUOTE_ID",profQuoteId);
				exchange.setProperty(ProfessionQuoteConstant.ENCRYPT_PROF_QUOTE_ID,encProfQuoteId);
				exchange.setProperty(ProfessionQuoteConstant.PROFQUOTEREQ,reqNode);
				((ObjectNode)reqNode).put("PROF_QUOTE_ID", profQuoteId);
			}
			exchange.getIn().setBody(reqNode);
		}catch(Exception e){
			log.error("Unable to store request in DB :",e);
		}
		
	}

	
	
	
}
