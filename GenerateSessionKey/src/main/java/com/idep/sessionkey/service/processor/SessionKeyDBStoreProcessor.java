package com.idep.sessionkey.service.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sessionkey.service.util.GenrateSessionKeyConstant;

public class SessionKeyDBStoreProcessor  implements Processor{
	
	Logger log = Logger.getLogger(SessionKeyDBStoreProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService service= CBInstanceProvider.getServerConfigInstance();
	CBService sessionKey= CBInstanceProvider.getBucketInstance("SessionKey");
	JsonNode configDoc =null;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String inputReq = exchange.getIn().getBody(String.class);
		JsonNode inputReqNode =objectMapper.readTree(inputReq);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		try{
			
        	Date currentDate = new Date();
        	
        	JsonObject docObj = JsonObject.fromJson(inputReq);
        	
        	if(inputReqNode.has("sessionId")){
        	docObj.put("sessionCreatedDate", dateFormat.format(currentDate));
    		docObj.put("documentType", "webSessionKey");  	
    		docObj.put("sessionId", inputReqNode.get("sessionId").asText());  	
        	/*	sessionKey.createAsyncDocument(inputReqNode.findValue("sessionId").asText(), docObj);
        		log.info("Session Document Created : "+inputReqNode.findValue("sessionId").asText());*/

    		try
			{
    			String sessionkey_docId=null;
				synchronized(this)
				{
					// get session sequence from database
					long sessionkey_seq = this.service.updateDBSequence(GenrateSessionKeyConstant.SEQDOCkEY);
					sessionkey_docId = service.getDocBYId(GenrateSessionKeyConstant.DOCID_CONFIG).content().getString(GenrateSessionKeyConstant.SEQNAME)+sessionkey_seq;
					docObj.put(GenrateSessionKeyConstant.DOCID,sessionkey_docId);
				}
				sessionKey.createAsyncDocument(sessionkey_docId, docObj);
				log.info("sessionId doc created : "+sessionkey_docId);
			}catch(Exception e)
				{
					this.log.error("ProposalReqProcessor : Exception while updating proposal sequence in DB");
				}

        	}else{
        		log.error("Fail to create Session  Document  because session id not found in request ");
        	}
    	 }
    	catch(Exception e)
    	{
    		log.error("Fail to create Session  Document  :  ",e);
    		
    	}
	}

}
