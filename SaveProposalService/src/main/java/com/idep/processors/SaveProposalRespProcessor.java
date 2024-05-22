package com.idep.processors;

/**
 * 
 * @author suraj.huljute
 * 30-NOV-2018
 * In this service we are 
 * 1.creating proposal
 * 2.updating proposal using proposalId
 * 3.forwarding same proposalId to respective service (like SubmitCarProposal)
 * 4.updating proposalId in CRM by putting request in leads queue.  
 */

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.util.SaveProposalConstants;

public class SaveProposalRespProcessor  implements Processor {
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(SaveProposalProcessor.class.getName());
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	CBService transServiceQuoteData =  CBInstanceProvider.getBucketInstance(SaveProposalConstants.QUOTE_BUCKET);
	DateFormat dateFormat = new SimpleDateFormat(SaveProposalConstants.DATE_FORMAT);
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static JsonNode sourceConfigNode = objectMapper.createObjectNode();
	JsonNode hostConfig = null;
	static CBService service = null;
	static JsonNode serviceConfigNode = null;
	static JsonNode leadProfileNode = null;
	static JsonNode WebHostConfigNode = null;
	static JsonNode leadConfigNode = null;
	static JsonNode keyConfigDoc=null;
	String encryptedProposalId =null;
      String encryptedQuoteId =null;
	String QUOTE_ID =null;
	static CBService policyTransService=null;
	String messageId=null;
	static
    {
    	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
    	service = CBInstanceProvider.getServerConfigInstance();
    	 policyTransService =  CBInstanceProvider.getPolicyTransInstance();
    	try
    	{    
    	      if (leadConfigNode == null) {
    	        leadConfigNode = objectMapper.readTree(((JsonObject)service.getDocBYId(SaveProposalConstants.LEAD_STAGES_CONF).content()).toString());
    	        }
    		 WebHostConfigNode = objectMapper.readTree(((JsonObject)service.getDocBYId(SaveProposalConstants.WEBHOSTCONFIG).content()).toString());
             serviceConfigNode= objectMapper.readTree(((JsonObject)service.getDocBYId(SaveProposalConstants.SERVICE_URL_CONFIG).content()).toString());
             docConfigNode = objectMapper.readTree(((JsonObject)serverConfigService.getDocBYId(SaveProposalConstants.LOG_CONFIGURATION).content()).toString());
             keyConfigDoc = objectMapper.readTree(((JsonObject)serverConfigService.getDocBYId(SaveProposalConstants.ENCRYPT_PRIVATE_CONF).content()).toString());
    	}
    	catch (Exception e)
    	{
    		log.info(SaveProposalConstants.ERR_FETCH_DOC + e);
    	}
   }
	public void process(Exchange exchange)throws Exception
	   { 
		log.info("Inside SaveProposalRespProcessor...........:");  
        try
        {
	          String proposalRequestData = (String)exchange.getIn().getBody(String.class);
	          String deviceId = (String)exchange.getIn().getHeader(SaveProposalConstants.DEVICE_ID);
	          JsonNode reqNode = objectMapper.readTree(proposalRequestData);
	          String businessLineId=reqNode.get(SaveProposalConstants.BUSINESS_LINE_ID).asText();
	          String LOB=docConfigNode.get(SaveProposalConstants.BUSINESS_LIST).get(businessLineId).asText();
	          
	          log.info("reqNode : "+reqNode);
	          String transactionName=null;
	          if(!reqNode.has(SaveProposalConstants.PROPOSALID)){
	    	    	 if(reqNode.has(SaveProposalConstants.BUSINESS_LINE_ID)){
	    	    		 String ProposalDocName="SEQ"+LOB.toUpperCase()+"PROPOSAL";
	    	    		 String proposalId=null;
		         try
		          {
		            synchronized (this)
		            {
		             log.info("ProposalDocName : "+ProposalDocName);	
		             long proposal_seq = this.serverConfig.updateDBSequence(ProposalDocName);
		             if(LOB.toUpperCase().equalsIgnoreCase(SaveProposalConstants.LOB_HEALTH.toUpperCase()) || LOB.toUpperCase().equalsIgnoreCase(SaveProposalConstants.LOB_TRAVEL.toUpperCase()))
		             proposalId = DocumentDataConfig.getConfigDocList().get(SaveProposalConstants.DOCUMENT_ID_CONFIG).get(LOB.toLowerCase()+"ProposalId").asText() + proposal_seq;
		             if(LOB.toUpperCase().equalsIgnoreCase(SaveProposalConstants.LOB_CAR.toUpperCase()) || LOB.toUpperCase().equalsIgnoreCase(SaveProposalConstants.LOB_BIKE.toUpperCase()))
                 	 proposalId = DocumentDataConfig.getConfigDocList().get(SaveProposalConstants.DOCUMENT_ID_CONFIG).get(LOB.toLowerCase()+SaveProposalConstants.PROPOSAL).asText() + proposal_seq;
		             log.info("proposalId : "+proposalId);
		             ((ObjectNode)reqNode).put(SaveProposalConstants.PROPOSALID, proposalId);
		             String documentId = LOB+"CarrierProposalRequest-" + proposalId;
		             this.log.info("proposal id generated carrierProposalRequest...........:" + documentId);
		             ((ObjectNode)reqNode).put(SaveProposalConstants.DOCUMENT_TYPE, LOB+"CarrierProposalRequest");
		             ((ObjectNode)reqNode).put(SaveProposalConstants.LOB,LOB);
		             JsonNode proposalRequest=objectMapper.createObjectNode();
		             JsonNode premiumDetails=null;
		             JsonNode proposerDetails=null;
		             JsonNode medicalQuestionarrie=null;
		             JsonNode declartionDetails=null;
		             JsonNode nomineeDetails=null;
		             JsonNode coverageDetails=null;
		             JsonNode insuredMembers=null;
		             JsonNode proposerInfo=null;
		             JsonNode previousPolicyDetails=null;
		             JsonNode socialStatusDetails=null;
		             JsonNode VehicleDetails=null;
		             if(LOB.equals(SaveProposalConstants.LOB_HEALTH)){
		             if(reqNode.has(SaveProposalConstants.MEDICAL_QUESTIONARRIE)){
		            	 medicalQuestionarrie=reqNode.get(SaveProposalConstants.MEDICAL_QUESTIONARRIE);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.MEDICAL_QUESTIONARRIE);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.MEDICAL_QUESTIONARRIE,medicalQuestionarrie);
			          }
		             if(reqNode.has(SaveProposalConstants.DECLARTION_DETAILS)){
		            	 declartionDetails=reqNode.get(SaveProposalConstants.DECLARTION_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.DECLARTION_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.DECLARTION_DETAILS,declartionDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.NOMINEE_DETAILS)){
		            	 nomineeDetails=reqNode.get(SaveProposalConstants.NOMINEE_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.NOMINEE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.NOMINEE_DETAILS,nomineeDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.COVERAGE_DETAILS)){
		            	 coverageDetails=reqNode.get(SaveProposalConstants.COVERAGE_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.COVERAGE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.COVERAGE_DETAILS,coverageDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.INSURED_MEMBERS)){
		            	 insuredMembers=reqNode.get(SaveProposalConstants.INSURED_MEMBERS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.INSURED_MEMBERS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.INSURED_MEMBERS,insuredMembers);
			          }
		             if(reqNode.has(SaveProposalConstants.PROPOSER_INFO)){
		            	 proposerInfo=reqNode.get(SaveProposalConstants.PROPOSER_INFO);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PROPOSER_INFO);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PROPOSER_INFO,proposerInfo);
			          }
		             if(reqNode.has(SaveProposalConstants.PREVIOUS_POLICY_DETAILS)){
		            	 previousPolicyDetails=reqNode.get(SaveProposalConstants.PREVIOUS_POLICY_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PREVIOUS_POLICY_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PREVIOUS_POLICY_DETAILS,previousPolicyDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.SOCIAL_STATUS_DETAILS)){
		            	 socialStatusDetails=reqNode.get(SaveProposalConstants.SOCIAL_STATUS_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.SOCIAL_STATUS_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.SOCIAL_STATUS_DETAILS,socialStatusDetails);
			          }
		             }
		             if(LOB.equals(SaveProposalConstants.LOB_BIKE) || LOB.equals(SaveProposalConstants.LOB_CAR))
		             if(reqNode.has(SaveProposalConstants.PREMIUM_DETAILS)){
			        	  premiumDetails=reqNode.get(SaveProposalConstants.PREMIUM_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PREMIUM_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PREMIUM_DETAILS,premiumDetails);
			          }
			          if(reqNode.has(SaveProposalConstants.PROPOSER_DETAILS)){
			        	  proposerDetails=reqNode.get(SaveProposalConstants.PROPOSER_DETAILS); 
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PROPOSER_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PROPOSER_DETAILS,proposerDetails);
				          }
			          if(reqNode.has(SaveProposalConstants.VEHICLE_DETAILS)){
			        	  VehicleDetails=reqNode.get(SaveProposalConstants.VEHICLE_DETAILS); 
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.VEHICLE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.VEHICLE_DETAILS,VehicleDetails);
				          }
			          
		             if(reqNode.has(SaveProposalConstants.INSURANCE_DETAILS)){
		            	  JsonNode insuranceDetails=reqNode.get(SaveProposalConstants.INSURANCE_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.INSURANCE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.INSURANCE_DETAILS,insuranceDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.PACOVER_DETAILS)){
		            	  JsonNode PACoverDetails=reqNode.get(SaveProposalConstants.PACOVER_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PACOVER_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PACOVER_DETAILS,PACoverDetails);
			          }
		             /*if(reqNode.has(SaveProposalConstants.MESSAGE_ID)){
		            	 messageId=reqNode.get(SaveProposalConstants.MESSAGE_ID).asText();
		            	 leadProfileNode=objectMapper.readTree(((JsonObject)policyTransService.getDocBYId(messageId).content()).toString());          	 
		            	 leadProfileNode=leadProfileNode.get("LeadDetails").get("firstActivity");
		
		             }*/
		             ((ObjectNode)reqNode).put(SaveProposalConstants.PROPOSALREQUEST,proposalRequest);
		             ((ObjectNode)reqNode).put(SaveProposalConstants.PROPOSALID,proposalId);
                     
		             encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, keyConfigDoc.get("encryptionKey").asText());
			  	      log.info("Encrypted Car ProposalId : " + encryptedProposalId);
			  	    ((ObjectNode)reqNode).put(SaveProposalConstants.ENCRYPT_PROPOSAL_ID,encryptedProposalId);
			  	    if(reqNode.has(SaveProposalConstants.QUOTE_ID)){
							QUOTE_ID = reqNode.findValue(SaveProposalConstants.QUOTE_ID).asText();
							JsonNode quoteIdDoc = objectMapper.readTree(((JsonObject)transServiceQuoteData.getDocBYId(QUOTE_ID).content()).toString());
							if(quoteIdDoc.findValue(SaveProposalConstants.ENCRYPT_QUOTE_ID) != null && !quoteIdDoc.get(SaveProposalConstants.ENCRYPT_QUOTE_ID).asText().isEmpty()){
								encryptedQuoteId = quoteIdDoc.findValue(SaveProposalConstants.ENCRYPT_QUOTE_ID).asText();
								log.info("encryptedQuoteId to store in Car ProposalId: "+encryptedQuoteId);
								 ((ObjectNode)reqNode).put(SaveProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
							}
							else{
								encryptedQuoteId = GenrateEncryptionKey.GetEncryptedKey(QUOTE_ID, keyConfigDoc.get("encryptionKey").asText());
							    log.info("Car encryptedQuoteId generated : " + encryptedQuoteId);
							    ((ObjectNode)reqNode).put(SaveProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
							}
						}
		             
		             String carrierRequesttNode = reqNode.toString();
		      	     JsonObject carrierRequestObject = JsonObject.fromJson(carrierRequesttNode);
			      	 try
			  	     {
			  	      Date currentDate = new Date();
			  	      carrierRequestObject.put(SaveProposalConstants.PROPOSAL_CREATED_DATE, this.dateFormat.format(currentDate));
			  	      String response= this.transService.createDocument(proposalId, carrierRequestObject);
			  	      log.info("response : "+response);
			  	     if(response.equals(SaveProposalConstants.DOC_CREATED)){
			  	    	 try
			  			 {		 
			  				/**
			  				 *  create producer template to send messages to LeadRequestQ
			  				 */
			  	    		 JsonNode QuoteDataNode=null;
			  	    		 ObjectNode leadDataNode = this.objectMapper.createObjectNode();
			  	    		try
			  	    	    {
			  	    			 JsonNode leadConfig = this.leadConfigNode.get("servicesList");
			  	    	      CBService policyTrans = CBInstanceProvider.getBucketInstance(SaveProposalConstants.QUOTE_DATA);
			  	    	      JsonDocument quoteDocument = policyTrans.getDocBYId(reqNode.get(SaveProposalConstants.QUOTE_ID).asText());
			  	    	    
			  	    	      if (quoteDocument != null)
			  	    	      {
			  	    	        QuoteDataNode = this.objectMapper.readTree(((JsonObject)quoteDocument.content()).toString());
			  	    	
			  	    	       if(LOB.equals(SaveProposalConstants.LOB_BIKE)||LOB.equals(SaveProposalConstants.LOB_CAR)){   
			  	    	    	 if(LOB.equals(SaveProposalConstants.LOB_CAR)){
			  	    	    		leadDataNode.put("carProposalStatus", leadConfig.get("submitCarProposal").get("proposalStage").asText());
			  	    	    		leadDataNode.put("transaction",leadConfig.get("saveProposal").get("proposalStage").asText());
			  	    	    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.CAR_QUOTE_REQUEST));
			  	    	    	 }
			  	    	       if(LOB.equals(SaveProposalConstants.LOB_BIKE)){  
			  	    	    		leadDataNode.put("bikeProposalStatus", leadConfig.get("submitBikeProposal").get("proposalStage").asText());
                                    leadDataNode.put("transaction",leadConfig.get("saveProposal").get("proposalStage").asText());				  	    	    	
				  	    	    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.BIKE_QUOTE_REQUEST));
			  	    	           }
			  	    	            if (leadDataNode.has(SaveProposalConstants.CARRIER_VEHICLE_INFO)) {
		  	    	    			leadDataNode.remove(SaveProposalConstants.CARRIER_VEHICLE_INFO);
				  	    	        }
				  	    	        if (leadDataNode.has(SaveProposalConstants.CARRIER_RTO_INFO)) {
				  	    	        	leadDataNode.remove(SaveProposalConstants.CARRIER_RTO_INFO);
				  	    	        }
				  	    	        if (leadDataNode.has(SaveProposalConstants.OCCUPATION_INFO)) {
				  	    	        	leadDataNode.remove(SaveProposalConstants.OCCUPATION_INFO);
				  	    	        }
			  	    	    	
			  	    	    }
			  	    	  if(LOB.equals(SaveProposalConstants.LOB_HEALTH)){   
			  	    		leadDataNode.put("healthProposalStatus", leadConfig.get("submitHealthProposal").get("proposalStage").asText());
			  	    		leadDataNode.put("transaction",leadConfig.get("saveProposal").get("proposalStage").asText());
			  	    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.QUOTE_REQUEST));
	  	    	    	   }
			  	    	  //commented because we are not implementing save proposal for life and travel
			  	    	/*if(LOB.equals(SaveProposalConstants.LOB_TRAVEL)){   
			  	    		QuoteDataReq = (ObjectNode)QuoteDataNode.get("quoteRequest");
			  	    	 	}
			  	    	if(LOB.equals(SaveProposalConstants.LOB_LIFE)){   
			  	    		QuoteDataReq = (ObjectNode)QuoteDataNode;
			  	    	 	}
			  	    	*/   
			  	    	  this.log.info(" Quote Request After: " + leadDataNode);
			  	    	      }
			  	    	    }
			  	    	    catch (Exception e)
			  	    	    {
			  	    	      this.log.error("Exception while fetching  quote document from DB ", e);
			  	    	    }
			  				CamelContext camelContext = exchange.getContext();
			  				ProducerTemplate template = camelContext.createProducerTemplate();
			  				String request = exchange.getIn().getBody().toString();
			  				JsonNode body =  objectMapper.readTree(request);
			  		
			  				 ((ObjectNode)body).put(SaveProposalConstants.QUOTE_ID,reqNode.get("QUOTE_ID"));
				  		     ((ObjectNode)body).put(SaveProposalConstants.LAST_VISITED_QUOTE_ID,reqNode.get("LastVisitedQuoteId"));
				  		   	 ((ObjectNode)body).put(SaveProposalConstants.COMMON_PROPOSAL_ID,proposalId);
				  		     ((ObjectNode)body).put(SaveProposalConstants.PROPOSALID,proposalId);
				  		     ((ObjectNode)body).put(SaveProposalConstants.LINE_OF_BUSINESS,businessLineId);
				  		     ((ObjectNode)body).put(SaveProposalConstants.MESSAGE_ID,reqNode.get("messageId"));
				  		     ((ObjectNode)body).put(SaveProposalConstants.MSG_ID_STATUS,"old");
				  		   ((ObjectNode)body).put(SaveProposalConstants.LEAD_STAGE,"PROPOSAL");
				  		     
				  		     ((ObjectNode)body).putAll((ObjectNode)leadDataNode);
				  		   		String uri = SaveProposalConstants.LEAD_REQ_Q;//?mapJmsMessage=false
			  					exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
			  					//template.send(uri, exchange); // put request in carrier quote request queue
			  					template.sendBody(uri, body.toString());
			  		   	 }
			  			 catch(Exception e)
			  			 {
			  				 log.error("unable to send message to LeadRequestQ : ",e);
			  			 }
 
			  	    	ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
				    	saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.SUCCESS_CODE);
				    	saveProposalResponse.put(SaveProposalConstants.RES_MSG,SaveProposalConstants.PROPOSAL_CREATE_SUCC);
				    	saveProposalResponse.put(SaveProposalConstants.RES_DATA,proposalId);
					    exchange.getIn().setBody(saveProposalResponse); 
			          }
			  	    }
			  	    catch (Exception e)
			  	    {
			  	      this.log.error("Failed to Create  "+LOB+"  CarrierProposalRequest Document  :  " + documentId, e);
			  	      throw new Exception("Failed to Create  "+LOB+"  CarrierProposalRequest Document  :  " + documentId);
			  	    }  	    
		            }
		          }
		          
		      catch (Exception e)
		      {
		            log.error("Exception while updating  "+LOB+"  proposal sequence in DB :", e);
		            throw new Exception("Exception while updating  "+LOB+"  proposal sequence in DB ");
		           
		      }
	          }else{
        	  throw new NullPointerException("businessLineId not found");
          }
	  }
      else{
    	  String proposalId=reqNode.get(SaveProposalConstants.PROPOSALID).asText();
    	  log.info("proposalId : "+proposalId);
    	  JsonNode QuoteDataNode=null;
    		 ObjectNode leadDataNode = this.objectMapper.createObjectNode();
    		 JsonDocument quoteDocument=null;
    		 try
    	    {
    			 JsonNode leadConfig = this.leadConfigNode.get("servicesList");
    	      CBService policyTrans = CBInstanceProvider.getBucketInstance(SaveProposalConstants.QUOTE_DATA);
    	      if(reqNode.has(SaveProposalConstants.QUOTE_ID)){
    	    	  quoteDocument = policyTrans.getDocBYId(reqNode.get(SaveProposalConstants.QUOTE_ID).asText());
    	      }
    	      if (quoteDocument != null)
    	      {
    	        QuoteDataNode = this.objectMapper.readTree(((JsonObject)quoteDocument.content()).toString());
    	
    	       if(LOB.equals(SaveProposalConstants.LOB_BIKE)||LOB.equals(SaveProposalConstants.LOB_CAR)){   
    	    	 if(LOB.equals(SaveProposalConstants.LOB_CAR)){
    	    		leadDataNode.put("carProposalStatus", leadConfig.get("submitCarProposal").get("proposalStage").asText());
    	    		leadDataNode.put("transaction",leadConfig.get("submitCarProposal").get("proposalStage").asText());
    	    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.CAR_QUOTE_REQUEST));
    	    	 }
    	       if(LOB.equals(SaveProposalConstants.LOB_BIKE)){  
    	    		leadDataNode.put("bikeProposalStatus", leadConfig.get("submitBikeProposal").get("proposalStage").asText());
                 leadDataNode.put("transaction",leadConfig.get("submitBikeProposal").get("proposalStage").asText());				  	    	    	
    	    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.BIKE_QUOTE_REQUEST));
    	           }
    	            if (leadDataNode.has(SaveProposalConstants.CARRIER_VEHICLE_INFO)) {
	    			leadDataNode.remove(SaveProposalConstants.CARRIER_VEHICLE_INFO);
  	    	        }
  	    	        if (leadDataNode.has(SaveProposalConstants.CARRIER_RTO_INFO)) {
  	    	        	leadDataNode.remove(SaveProposalConstants.CARRIER_RTO_INFO);
  	    	        }
  	    	        if (leadDataNode.has(SaveProposalConstants.OCCUPATION_INFO)) {
  	    	        	leadDataNode.remove(SaveProposalConstants.OCCUPATION_INFO);
  	    	        }
    	    	
    	    }
    	  if(LOB.equals(SaveProposalConstants.LOB_HEALTH)){   
    		leadDataNode.put("healthProposalStatus", leadConfig.get("submitHealthProposal").get("proposalStage").asText());
    		leadDataNode.put("transaction",leadConfig.get("submitHealthProposal").get("proposalStage").asText());
    		leadDataNode.putAll((ObjectNode)QuoteDataNode.get(SaveProposalConstants.QUOTE_REQUEST));
    	   }
    	  //commented because we are not implementing save proposal for life and travel
    	/*if(LOB.equals(SaveProposalConstants.LOB_TRAVEL)){   
    		QuoteDataReq = (ObjectNode)QuoteDataNode.get("quoteRequest");
    	 	}
    	if(LOB.equals(SaveProposalConstants.LOB_LIFE)){   
    		QuoteDataReq = (ObjectNode)QuoteDataNode;
    	 	}
    	*/   
    	  this.log.info(" Quote Request After: " + leadDataNode);
    	      }
    	    }
    	    catch (Exception e)
    	    {
    	      this.log.error("Exception while fetching  quote document from DB ", e);
    	    }
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String request = exchange.getIn().getBody().toString();
			JsonNode body =  objectMapper.readTree(request);
	
			 ((ObjectNode)body).put(SaveProposalConstants.QUOTE_ID,reqNode.get("QUOTE_ID"));
  		     ((ObjectNode)body).put(SaveProposalConstants.LAST_VISITED_QUOTE_ID,reqNode.get("LastVisitedQuoteId"));
  		   	 ((ObjectNode)body).put(SaveProposalConstants.COMMON_PROPOSAL_ID,proposalId);
  		     ((ObjectNode)body).put(SaveProposalConstants.PROPOSALID,proposalId);
  		     ((ObjectNode)body).put(SaveProposalConstants.LINE_OF_BUSINESS,businessLineId);
  		     ((ObjectNode)body).put(SaveProposalConstants.MESSAGE_ID,reqNode.get("messageId"));
  		     ((ObjectNode)body).put(SaveProposalConstants.MSG_ID_STATUS,"old");
  		     ((ObjectNode)body).put(SaveProposalConstants.LEAD_STAGE,"PROPOSAL");
  		     ((ObjectNode)body).putAll((ObjectNode)leadDataNode);
  		   		String uri = SaveProposalConstants.LEAD_REQ_Q;//?mapJmsMessage=false
				exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
				//template.send(uri, exchange); // put request in carrier quote request queue
				template.sendBody(uri, body.toString());
	
      	if(reqNode.has(SaveProposalConstants.IS_CLEARED)){
	    	String carrierRequesttNode = reqNode.toString();
	      	JsonObject carrierRequestObject = JsonObject.fromJson(carrierRequesttNode);
      		ObjectNode proposalData=objectMapper.createObjectNode();
      		((ObjectNode)proposalData).put(SaveProposalConstants.IS_CLEARED, true);
      		JsonObject proposaldoc=JsonObject.fromJson(proposalData.toString());
      		String response= this.transService.replaceDocument(proposalId, proposaldoc);
      	    log.info("clear response:"+response);
      	  String serviceURL=null;
      	    if(response.equals(SaveProposalConstants.DOC_REPLACED)){
 	      		if(LOB.equals(SaveProposalConstants.LOB_CAR)){
 		        	  transactionName=SaveProposalConstants.SUBMIT_CAR_PROPOSAL;
 		       }
 		             if(LOB.equals(SaveProposalConstants.LOB_BIKE)){
 		            	 transactionName=SaveProposalConstants.SUBMIT_BIKE_PROPOSAL;    
 		             }
 		              if(LOB.equals(SaveProposalConstants.LOB_HEALTH)){
 		            	 transactionName=SaveProposalConstants.SUBMIT_HEALTH_PROPOSAL;
 		              }
 		          //commented because we are not implementing save proposal for life and travel
 		        /*    if(LOB.equals(SaveProposalConstants.LOB_LIFE)){
		            	 transactionName=SaveProposalConstants.SUBMIT_LIFE_PROPOSAL;
		             }
 		           if(LOB.equals(SaveProposalConstants.LOB_TRAVEL)){
		            	 transactionName=SaveProposalConstants.SUBMIT_TRAVEL_PROPOSAL;
		              }
		            */
 		        ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
		    	saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.SUCCESS_CODE);
		    	saveProposalResponse.put(SaveProposalConstants.RES_MSG,SaveProposalConstants.PROPOSAL_UPDATE_SUCC);
		    	saveProposalResponse.put(SaveProposalConstants.RES_DATA,proposalId);
				DefaultHttpClient httpClient = new DefaultHttpClient();
		    	String proposalResponse = null;
		    	JsonNode proposalResponseNode = null;
		     	//serviceURL=serviceConfigNode.get(SaveProposalConstants.MASTER_SERVICE).asText().replaceAll(SaveProposalConstants.URL_REMOVAL,"");
		    	serviceURL=serviceConfigNode.get(transactionName).asText().replaceAll(SaveProposalConstants.URL_REMOVAL,"")+"/integrate/invoke";
		    	try
		    	{	HttpPost httpPost = new HttpPost(serviceURL);
		    	  /*  JsonNode header= objectMapper.createObjectNode();
		    	    JsonNode finalNode= objectMapper.createObjectNode();
		    	    ((ObjectNode)header).put(SaveProposalConstants.TRANSACTION_NAME,transactionName);
		    	    ((ObjectNode)header).put(SaveProposalConstants.ORIGIN, WebHostConfigNode.get("origin").asText());
				    ((ObjectNode)header).put(SaveProposalConstants.DEVICE_ID,deviceId);
		    	    ((ObjectNode)finalNode).put(SaveProposalConstants.BODY,reqNode );
		    	    ((ObjectNode)finalNode).put(SaveProposalConstants.HEADER,header);
		    	  */
		    	((ObjectNode)reqNode).put(SaveProposalConstants.DEVICE_ID,deviceId);
		    	
		    	StringEntity entity = new StringEntity(((ObjectNode)reqNode).toString());
		    	     entity.setContentType(SaveProposalConstants.APPLICATION_JSON);
		    	     httpPost.setEntity(entity);
		    	     httpPost.setHeader(SaveProposalConstants.ORIGIN, WebHostConfigNode.get("origin").asText());
		    	     httpPost.setHeader("deviceId", deviceId);
		    	     httpPost.setHeader("transactionName", transactionName);
				     HttpResponse SubmitDataResponse = httpClient.execute(httpPost);
		    	     this.log.info("Submit  Proposal Response Status Code : " + SubmitDataResponse.getStatusLine().getStatusCode());
		    	     proposalResponse = EntityUtils.toString(SubmitDataResponse.getEntity());
		    	     this.log.info("Response in JSON without slashremoval: " + proposalResponse);
		    	     this.log.info("Response in JSON : " + proposalResponse.replace("\\\"", "\""));
		    	     String SlashRemoval=null;
		    	     if(proposalResponse.contains("\\\"")){
		    	      SlashRemoval = proposalResponse.replace("\\\"", "\"");
		    	      proposalResponseNode = this.objectMapper.readTree(SlashRemoval.substring(1, SlashRemoval.length() - 1));	 
	    	         }else{
		    	    	 SlashRemoval=proposalResponse;
		    	    	 proposalResponseNode = this.objectMapper.readTree(SlashRemoval);	 
		    	     }
		    	     httpClient.getConnectionManager().shutdown();
		    	     exchange.getIn().setBody(proposalResponseNode);
                  }catch (ClientProtocolException e)
		    	    {
            this.log.info("Exception While Getting submit proposal Response  ", e);
            e.printStackTrace();
          }
          catch (IOException e)
          {
            this.log.info("Exception While  Getting submit proposal Response  ", e);
            e.printStackTrace();
          }
      	}
      	}else{

      		  JsonNode nominationDetaiils=null;
	    	  JsonNode premiumDetails=null;
	          JsonNode proposerDetails=null;
	          JsonNode insuranceDetails=null;
	          JsonNode appointeeDetails=null;
	          JsonNode proposalRequest=objectMapper.createObjectNode();
	          JsonNode medicalQuestionarrie=null;
	             JsonNode declartionDetails=null;
	             JsonNode nomineeDetails=null;
	             JsonNode coverageDetails=null;
	             JsonNode insuredMembers=null;
	             JsonNode proposerInfo=null;
	             JsonNode previousPolicyDetails=null;
	             JsonNode socialStatusDetails=null;
	             
	          if(!reqNode.has(SaveProposalConstants.PROPOSALREQUEST)){
	          if(LOB.equals(SaveProposalConstants.LOB_CAR)|| LOB.equals(SaveProposalConstants.LOB_BIKE)){
	          if(reqNode.has(SaveProposalConstants.PREMIUM_DETAILS)){
	        	  premiumDetails=reqNode.get(SaveProposalConstants.PREMIUM_DETAILS);
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PREMIUM_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PREMIUM_DETAILS,premiumDetails);
	          }
	          if(reqNode.has(SaveProposalConstants.PROPOSER_DETAILS)){
	        	  proposerDetails=reqNode.get(SaveProposalConstants.PROPOSER_DETAILS); 
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PROPOSER_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PROPOSER_DETAILS,proposerDetails);
		          }
	          if(reqNode.has(SaveProposalConstants.NOMINATION_DETAILS)){
	        	  nominationDetaiils=reqNode.get(SaveProposalConstants.NOMINATION_DETAILS);
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.NOMINATION_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.NOMINATION_DETAILS,nominationDetaiils);
	          }
              if(reqNode.has(SaveProposalConstants.INSURANCE_DETAILS)){
            	  insuranceDetails=reqNode.get(SaveProposalConstants.INSURANCE_DETAILS);
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.INSURANCE_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.INSURANCE_DETAILS,insuranceDetails);
	          }
              if(reqNode.has(SaveProposalConstants.APPOINTEE_DETAILS)){
            	  appointeeDetails=reqNode.get(SaveProposalConstants.APPOINTEE_DETAILS);
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.APPOINTEE_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.APPOINTEE_DETAILS,appointeeDetails);
	          }
              if(reqNode.has(SaveProposalConstants.PACOVER_DETAILS)){
            	  JsonNode PACoverDetails=reqNode.get(SaveProposalConstants.PACOVER_DETAILS);
	        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PACOVER_DETAILS);
	        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PACOVER_DETAILS,PACoverDetails);
	          }
              }
              if(LOB.equals(SaveProposalConstants.LOB_HEALTH)){
		             if(reqNode.has(SaveProposalConstants.MEDICAL_QUESTIONARRIE)){
		            	 medicalQuestionarrie=reqNode.get(SaveProposalConstants.MEDICAL_QUESTIONARRIE);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.MEDICAL_QUESTIONARRIE);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.MEDICAL_QUESTIONARRIE,medicalQuestionarrie);
			          }
		             if(reqNode.has(SaveProposalConstants.DECLARTION_DETAILS)){
		            	 declartionDetails=reqNode.get(SaveProposalConstants.DECLARTION_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.DECLARTION_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.DECLARTION_DETAILS,declartionDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.NOMINEE_DETAILS)){
		            	 nomineeDetails=reqNode.get(SaveProposalConstants.NOMINEE_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.NOMINEE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.NOMINEE_DETAILS,nomineeDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.COVERAGE_DETAILS)){
		            	 coverageDetails=reqNode.get(SaveProposalConstants.COVERAGE_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.COVERAGE_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.COVERAGE_DETAILS,coverageDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.INSURED_MEMBERS)){
		            	 insuredMembers=reqNode.get(SaveProposalConstants.INSURED_MEMBERS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.INSURED_MEMBERS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.INSURED_MEMBERS,insuredMembers);
			          }
		             if(reqNode.has(SaveProposalConstants.PROPOSER_INFO)){
		            	 proposerInfo=reqNode.get(SaveProposalConstants.PROPOSER_INFO);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PROPOSER_INFO);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PROPOSER_INFO,proposerInfo);
			          }
		             if(reqNode.has(SaveProposalConstants.PREVIOUS_POLICY_DETAILS)){
		            	 previousPolicyDetails=reqNode.get(SaveProposalConstants.PREVIOUS_POLICY_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.PREVIOUS_POLICY_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.PREVIOUS_POLICY_DETAILS,previousPolicyDetails);
			          }
		             if(reqNode.has(SaveProposalConstants.SOCIAL_STATUS_DETAILS)){
		            	 socialStatusDetails=reqNode.get(SaveProposalConstants.SOCIAL_STATUS_DETAILS);
			        	  ((ObjectNode)reqNode).remove(SaveProposalConstants.SOCIAL_STATUS_DETAILS);
			        	  ((ObjectNode)proposalRequest).put(SaveProposalConstants.SOCIAL_STATUS_DETAILS,socialStatusDetails);
			          }
		             }
		           
              ((ObjectNode)reqNode).put(SaveProposalConstants.PROPOSALREQUEST,proposalRequest);
	          }
          try
	  	     {
	    	  String carrierRequesttNode = reqNode.toString();
		      JsonObject carrierRequestObject = JsonObject.fromJson(carrierRequesttNode);
	  	      Date currentDate = new Date();
	  	      carrierRequestObject.put(SaveProposalConstants.PROPOSAL_CREATED_DATE, this.dateFormat.format(currentDate));
	  	      log.debug("carrierRequestObject"+carrierRequestObject);
	  	      String response= this.transService.replaceDocument(proposalId, carrierRequestObject);
	          log.info("response : "+response);
	          if(response.equals(SaveProposalConstants.DOC_REPLACED)){
	        	  ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
			    	saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.SUCCESS_CODE);
			    	saveProposalResponse.put(SaveProposalConstants.RES_MSG,SaveProposalConstants.PROPOSAL_UPDATE_SUCC);
			    	saveProposalResponse.put(SaveProposalConstants.RES_DATA,proposalId);
				    exchange.getIn().setBody(saveProposalResponse);
	          }
	  	    }
	  	    catch (Exception e)
	  	    {
	  	      this.log.error("Failed to update CarrierProposalRequest Document  :  " , e);
	  	      throw new Exception("Failed to update CarrierProposalRequest Document : "+proposalId);
	  	    }
      	}
      	}
   }
    catch(NullPointerException e){
    	ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
    	saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.FAILURE_CODE);
    	saveProposalResponse.put(SaveProposalConstants.RES_MSG,e.getMessage());
    	saveProposalResponse.put(SaveProposalConstants.RES_DATA,SaveProposalConstants.FAILURE);
	     exchange.getIn().setBody(saveProposalResponse);
        }
    catch(Exception e)
        {
    	ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
    	saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.FAILURE_CODE);
    	saveProposalResponse.put(SaveProposalConstants.RES_MSG,e.getMessage());
    	saveProposalResponse.put(SaveProposalConstants.RES_DATA,SaveProposalConstants.FAILURE);
	     exchange.getIn().setBody(saveProposalResponse); 
          
        }
        
    }
  }
	   

