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
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
/**
 * @author shweta.joshi
 */
public class CarrierProposalResponseDBStore  implements Processor
{
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(CarrierProposalResponseDBStore.class.getName());
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
  
  public void process(Exchange exchange)
    throws Exception
  {
             String proposalResponse = (String)exchange.getIn().getBody(String.class);
             log.debug("proposalResponse in CarrierProposalResponseDBStore: "+proposalResponse);
             HashMap<Object, Object> map = new HashMap<>();
             if(proposalResponse.contains("&gt;") || proposalResponse.contains("&lt;"))
     		{
     			map.put("&gt;", ">");
     			map.put("&lt;", "<");
     			proposalResponse=ReplaceSpeicalCharcter(proposalResponse, map);
     		}
            JsonNode InputRequest = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
            log.debug("InputRequest at CarrierProposalResponseDBStore "+InputRequest);
		    String documentId = ProposalConstants.TRAVELRIERPROPRESPONSE+"-"+InputRequest.get(ProposalConstants.PROPOSAL_ID).textValue();
		    this.log.debug("proposal id generated CarrierProposalResponseDBStore...........:" + documentId);
		    ObjectNode responseNode = this.objectMapper.createObjectNode();
		    responseNode.put(ProposalConstants.RESPONSE, proposalResponse);		    
		    responseNode.put(ProposalConstants.PROPOSAL_ID, InputRequest.get(ProposalConstants.PROPOSAL_ID).textValue());
		    responseNode.put(ProposalConstants.CARRIER_ID, InputRequest.get(ProposalConstants.CARRIER_ID).asText());
		    //responseNode.put(ProposalConstants.PRODUCT_ID, InputRequest.get(ProposalConstants.PRODUCT_ID).asText());
		    responseNode.put(ProposalConstants.PLAN_ID, InputRequest.get(ProposalConstants.PLAN_ID).asText());
		    //responseNode.put(ProposalConstants.DOCUMENT_TYPE,ProposalConstants.TRAVELRIERPROPRESPONSE);
		    String carrierResponseNode = responseNode.toString();
		    JsonObject carrierResponseObject = JsonObject.fromJson(carrierResponseNode);
		    try
		    {
		       Date currentDate = new Date();
		       carrierResponseObject.put(ProposalConstants.PROPOSALCREATEDATE, this.dateFormat.format(currentDate));
		      this.transService.createDocument(documentId, carrierResponseObject);
		      log.debug("carrierResponseObject:  "+carrierResponseObject);
		    }
		    catch (Exception e)
		    {
		      this.log.error("Failed to Create Travel CarrierProposalResponse Document  :  " + documentId, e);
		      throw new ExecutionTerminator();
		    }
     }
  
  public String ReplaceSpeicalCharcter(String data ,HashMap<Object,Object> replaceValue){
		
		for (Map.Entry<Object, Object> entry : replaceValue.entrySet()){
			data = data.replaceAll(entry.getKey().toString(), entry.getValue().toString());
		}
		return data;
	}
}
