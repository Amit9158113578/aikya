package com.idep.popsp.ticket.dbstore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.POSPServiceConstant;

public class TicketDBStore implements Processor {
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  static ObjectMapper objectMapper = new ObjectMapper();
	  static Logger log = Logger.getLogger(TicketDBStore.class.getName());
	  CBService transService = CBInstanceProvider.getBucketInstance("PospData");
	  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	  
	  public void process(Exchange exchange)
	    throws Exception
	  {
		  		String status = null;
	            String carrierProposalData = (String)exchange.getIn().getBody(String.class);
			    String documentId = DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.DOCID_CONFIG).get("pospTicketId").asText()+this.serverConfig.updateDBSequence("SEQPOSPTICKET");
			    JsonObject carrierRequestObject = JsonObject.fromJson(carrierProposalData);
			    try
			    {
			       Date currentDate = new Date();
			       carrierRequestObject.put(POSPServiceConstant.POSP_TICKET_CREATED_DATE, this.dateFormat.format(currentDate));
			       carrierRequestObject.put(POSPServiceConstant.POSPTICKETID,documentId );
			       status = this.transService.createDocument(documentId, carrierRequestObject);
			       log.info("posp document created status :"+status + " \t "+documentId);
			      ObjectNode resNode = objectMapper.createObjectNode();
			      
			      resNode.put("status", status);
			      resNode.put("stage", "created");
			      resNode.put("ticketId", documentId);
			      exchange.getIn().setBody(resNode);
			    }
			    catch (Exception e)
			    {
			      log.error("Failed to Create posp ticket Document  :  " + documentId, e);
			      status = POSPServiceConstant.TICKET_DOC_NOT_CREATED;
			    }
			    
	     }
}
