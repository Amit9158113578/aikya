package com.idep.proposal.dbstore.processor;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class CarrierProposalRequestDBStore  implements Processor
{
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(CarrierProposalRequestDBStore.class.getName());
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
  
  public void process(Exchange exchange)
    throws Exception
  {
            String carrierProposalRequest = exchange.getIn().getBody(String.class);
            String carrierProposalRequestFormatted = "\"" + carrierProposalRequest + "\"";  
            log.debug("carrierProposalRequestFormatted: "+carrierProposalRequestFormatted);
            JsonNode InputRequest = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
            log.debug("InputRequest at CarrierProposalRequestDBStore "+InputRequest);
		    String documentId = ProposalConstants.TRAVELRIERPROPREQUEST+"-"+InputRequest.get(ProposalConstants.PROPOSAL_ID).textValue();
		    this.log.debug("proposal id generated carrierProposalRequest...........:" + documentId);
		    ObjectNode requestNode = this.objectMapper.createObjectNode();
		    requestNode.put(ProposalConstants.REQUEST, carrierProposalRequestFormatted);
		    requestNode.put(ProposalConstants.PROPOSAL_ID, InputRequest.get(ProposalConstants.PROPOSAL_ID).textValue());
		    requestNode.put(ProposalConstants.CARRIER_ID, InputRequest.get(ProposalConstants.CARRIER_ID).asText());
		    //requestNode.put(ProposalConstants.PRODUCT_ID, InputRequest.get(ProposalConstants.PRODUCT_ID).asText());
		    requestNode.put(ProposalConstants.PLAN_ID, InputRequest.get(ProposalConstants.PLAN_ID).asText());
		    //requestNode.put(ProposalConstants.DOCUMENT_TYPE,ProposalConstants.TRAVELRIERPROPREQUEST);
		    
		    String carrierRequestNode = requestNode.toString();
		    JsonObject carrierRequestObject = JsonObject.fromJson(carrierRequestNode);
		    try
		    {
		       Date currentDate = new Date();
		      carrierRequestObject.put(ProposalConstants.PROPOSALCREATEDATE, this.dateFormat.format(currentDate));
		      this.transService.createDocument(documentId, carrierRequestObject);
		    }
		    catch (Exception e)
		    {
		      this.log.error("Failed to Create Travel CarrierProposalRequest Document  :  " + documentId, e);
		      throw new ExecutionTerminator();
		    }
     }
}
