package com.idep.createPolicyDoc;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.summaryUtils.ExecutionTerminator;

public class CreatePolicyDoc {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(PolicyRequest.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService proposalService = CBInstanceProvider.getPolicyTransInstance();
	PolicyRequest qc = new PolicyRequest();

	public static String createPolicyDoc(JsonObject reqNode){
		JsonNode documentSEQ = null;
		String docId =null;
		try {
			documentSEQ = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("SEQPAYMENTRES").content()).toString());
			
			long updateDBSequence = serverConfig.updateDBSequence("SEQPAYMENTRES");
			if (updateDBSequence == -1L)
			{
				updateDBSequence = serverConfig.updateDBSequence("SEQPAYMENTRES");
				if (updateDBSequence == -1L)
				{
					updateDBSequence = serverConfig.updateDBSequence("SEQPAYMENTRES");
					if (updateDBSequence == -1L)
					{
						Thread.sleep(500L);
						updateDBSequence = serverConfig.updateDBSequence("SEQPAYMENTRES");
						if (updateDBSequence == -1L) {
							throw new ExecutionTerminator();
						}
					}
				}
			}
			
			 docId = "PAYRES-"+updateDBSequence;
			String doc_status = proposalService.createDocument(docId, reqNode);
			//log.info("document creation status" + doc_status);
		}
		catch(Exception e){
			log.error("Error in CreatePolicyDoc Class",e);
		}
		return docId;
	}

}
