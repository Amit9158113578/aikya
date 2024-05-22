package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;






import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class ABHIProposalPolicyProcessor implements Processor {

	Logger log = Logger.getLogger(ABHIProposalPolicyProcessor.class);
	 ObjectMapper objectMapper = new ObjectMapper();
	 CBService policyTrans =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
			try{
				
				String inputReq = exchange.getIn().getBody(String.class);
				JsonNode inputReqNode = objectMapper.readTree(inputReq);
				JsonObject carrierPropRes = policyTrans.getDocBYId(inputReqNode.get("transactionStausInfo").get("proposalId").asText()).content();
				
				if(carrierPropRes!=null){
					
					JsonNode healthProposalRes =  objectMapper.readTree(carrierPropRes.get("healthProposalResponse").toString());
					
					JsonNode paymentRes =  objectMapper.readTree(carrierPropRes.get("paymentResponse").toString());
					
					JsonObject paymentResDoc = policyTrans.getDocBYId(paymentRes.get("apPreferId").asText()).content();	
					
					JsonNode clientResponse =  objectMapper.readTree(paymentResDoc.get("clientResponse").toString());
					String txnRefNo= clientResponse.get("TxRefNo").asText();
					
					String quotationNoDocumentId = healthProposalRes.get("carrierQuoteId").asText();
					JsonDocument proposalReq = policyTrans.getDocBYId(quotationNoDocumentId);
					if(proposalReq == null)
					{
						JsonDocument proposalIDDocumentInPT = policyTrans.getDocBYId(healthProposalRes.get("proposalId").asText());
						JsonNode dataDoc = objectMapper.readTree(proposalIDDocumentInPT.content().toString());
						JsonObject quoteIdDoc = policyTrans.getDocBYId(dataDoc.get("ABHIQuoteId").asText()).content();
				
						String Doc_status = policyTrans.createDocument(quotationNoDocumentId, quoteIdDoc);
						proposalReq = policyTrans.getDocBYId(quotationNoDocumentId);
						JsonNode proposalReqNode =  objectMapper.readTree(proposalReq.content().toString());
						JsonNode receiptCreation = proposalReqNode.get("carrierProposalRequest").get("ReceiptCreation");
						((ObjectNode)receiptCreation).put("instrumentNumber", txnRefNo);
						log.debug("Final Primium Amount : "+healthProposalRes.get("finalPremium"));
						((ObjectNode)receiptCreation).put("collectionAmount",healthProposalRes.get("finalPremium").asText());
						((ObjectNode)proposalReqNode.get("carrierProposalRequest").get("PolicyCreationRequest")).put("IsPayment","1");
						
						((ObjectNode)proposalReqNode).put("requestType", "HealthPolicyRequest");
						((ObjectNode)proposalReqNode).put("carrierId", carrierPropRes.getInt("carrierId"));
						((ObjectNode)proposalReqNode).put("planId", carrierPropRes.getInt("productId"));
						((ObjectNode)proposalReqNode.get("carrierProposalRequest")).put("ReceiptCreation",receiptCreation);
						log.debug("ABHI Carrier Health Proposal Res : "+healthProposalRes);
						exchange.getIn().setBody(proposalReqNode);
						
					}else{
						
						proposalReq = policyTrans.getDocBYId(quotationNoDocumentId);
					JsonNode proposalReqNode =  objectMapper.readTree(proposalReq.toString());
					
					JsonNode receiptCreation = proposalReqNode.get("carrierProposalRequest").get("ReceiptCreation");
					
					((ObjectNode)receiptCreation).put("instrumentNumber", txnRefNo);
					log.debug("Final Primium Amount : "+healthProposalRes.get("finalPremium"));
					((ObjectNode)receiptCreation).put("collectionAmount",healthProposalRes.get("finalPremium").asText());
					((ObjectNode)proposalReqNode.get("carrierProposalRequest").get("PolicyCreationRequest")).put("IsPayment","1");
					
					((ObjectNode)proposalReqNode).put("requestType", "HealthPolicyRequest");
					((ObjectNode)proposalReqNode).put("carrierId", carrierPropRes.getInt("carrierId"));
					((ObjectNode)proposalReqNode).put("planId", carrierPropRes.getInt("productId"));
					((ObjectNode)proposalReqNode.get("carrierProposalRequest")).put("ReceiptCreation",receiptCreation);
					log.debug("ABHI Carrier Health Proposal Res : "+healthProposalRes);
					exchange.getIn().setBody(proposalReqNode);
					}
				}else{
					
					log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIProposalPolicyProcessor: Proposal Document not Present |"+inputReqNode);
				}
			}catch(Exception e){
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIProposalPolicyProcessor|",e);
			}

	}

}
