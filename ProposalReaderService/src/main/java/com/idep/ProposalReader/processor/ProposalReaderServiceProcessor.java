/**
 * 
 */
package com.idep.ProposalReader.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.ProposalReader.util.ProposalReaderConstant;

/**
 * @author sayli.boralkar
 *
 */
public class ProposalReaderServiceProcessor implements Processor {
	
	Logger log = Logger.getLogger(ProposalReaderServiceProcessor.class);
	ObjectMapper objectMapper =new ObjectMapper();
	
	
	CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {		
		String PID=null;
	try{
			this.policyTransaction = CBInstanceProvider.getPolicyTransInstance(); //getting PolicyTransaction instance
			this.log.info("Policy transaction is loading....");
			String request = exchange.getIn().getBody(String.class);	//requesting for data 
			log.info("ProposalReaderServiceProcessor Request "+request);	//print request data
			
			
			JsonNode requestNode = objectMapper.readTree(request);  //conversion string to JsonNode
		//	List<JsonObject> proposaldata= null;
			
			ObjectNode objn = objectMapper.createObjectNode();
			/*
			ObjectNode queryConfigNode = objectMapper.createObjectNode();
			
			queryConfigNode.put("2", ProposalReaderConstant.PROPOSALQUERY1);
			queryConfigNode.put("3", ProposalReaderConstant.PROPOSALQUERY);
			queryConfigNode.put("4", ProposalReaderConstant.PROPOSALQUERY2);*/
		
			if(requestNode.has(ProposalReaderConstant.PROPOSALID))
			{
				JsonArray proposaalarrdata = JsonArray.create();
			    PID = requestNode.get("proposalId").asText();
				String LOB = requestNode.get(ProposalReaderConstant.BUSINID).asText();
				proposaalarrdata.add(PID);
				proposaalarrdata.add(LOB);
				this.log.info(" proposal reader node  "+proposaalarrdata);
			//	proposaldata = policyTransaction.executeConfigParamArrQuery(queryConfigNode.get(requestNode.get("businessLineId").asText()).asText(), proposaalarrdata); 
			    JsonDocument proposalIdDoc = policyTransaction.getDocBYId(PID);
				this.log.info("getting proposal document"+proposalIdDoc);
				if(proposalIdDoc!=null)
				{
					JsonNode proposaldata = objectMapper.readTree(proposalIdDoc.content().toString());
					ObjectNode data = objectMapper.createObjectNode();
					data.set("PolicyTransaction", proposaldata);
					/* if above condition satisfied then success response come in header */
					this.log.info("Proposal Id Document  : "+proposaldata);
					objn.put("responsecode", 1000);
					objn.put("message", "expected record found");
					objn.put("data",data);
					exchange.getIn().setBody(objectMapper.writeValueAsString(objn));
				}
				else
				{
					
				      JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
				      String decryptedProposalId = GenrateEncryptionKey.GetPlainText(PID, keyConfigDoc.get("encryptionKey").asText());
				      log.info("decryptedProposalId :" + decryptedProposalId);
				      JsonDocument decryptedProposalIdDoc = policyTransaction.getDocBYId(decryptedProposalId);
				      if(decryptedProposalIdDoc!=null)
						{
							JsonNode proposaldata = objectMapper.readTree(decryptedProposalIdDoc.content().toString());
							ObjectNode data = objectMapper.createObjectNode();
							data.set("PolicyTransaction", proposaldata);
							/* if above condition satisfied then success response come in header */
							this.log.info("Proposal Id Document  : "+proposaldata);
							objn.put("responsecode", 1000);
							objn.put("message", "expected record found");
							objn.put("data",data);
							exchange.getIn().setBody(objectMapper.writeValueAsString(objn));
						}
				      else{
					/* if above condition fail then below response comes in header  */
					this.log.info("Proposal Id Document Not Found : "+PID);
					objn.put("responsecode", 1002);
					objn.put("message", "expected record not found");
					objn.put("data", "");
					exchange.getIn().setBody(objectMapper.writeValueAsString(objn));
				      }

				}
			}					
		}
	  catch(Exception e)
		{
		  this.log.error(" unable to read proposal document:  "+PID);
		 }
	}
	
	}


