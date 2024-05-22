package com.idep.proposal.data.commit;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ProposalReqDBStore
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
  
  public void process(Exchange exchange)
    throws Exception
  {
    String doc_status = null;
    try
    {
      String proposalRequest = (String)exchange.getIn().getBody(String.class);
      
      this.log.info("proposalRequest in ProposalReqDBStore: " + proposalRequest);
      JsonObject docObj = JsonObject.fromJson(proposalRequest);
      //this.log.info("The doc obj is" + docObj);
      
      JsonDocument proposalDoc = null;
      String docId = docObj.getString("proposalId");
      docObj.put(ProposalConstants.ENCRYPT_PROPOSAL_ID, exchange.getProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID));
      docObj.put("proposalId", docId);
      
      this.log.info("proposalRequest body set as: " + docObj);
      
      exchange.getIn().setBody(docObj);
      try
      {
        proposalDoc = this.transService.getDocBYId(docId);
      }
      catch (Exception e)
      {
        this.log.error("failed to read proposal document from DB :" + docId);
        throw new ExecutionTerminator();
      }
      if (proposalDoc == null)
      {
        try
        {
          Date currentDate = new Date();
          docObj.put("proposalCreatedDate", this.dateFormat.format(currentDate));
          this.transService.createDocument(docId, docObj);
        }
        catch (Exception e)
        {
          this.log.error("Failed to Create car proposal Document  :  " + docId, e);
          throw new ExecutionTerminator();
        }
      }
      else
      {
        Date currentDate = new Date();
        docObj.put("updatedDate", this.dateFormat.format(currentDate));
        
        JsonObject documentContent = ((JsonObject)proposalDoc.content()).put("lifeProposalResponse", docObj);
        if (docObj.containsKey("proposalStatus")) {
          documentContent.put("proposalStatus", docObj.getString("proposalStatus"));
        }
        try
        {
          this.log.info("docId inside try" + docId);
          this.log.info("documentContent inside try" + documentContent);
          this.transService.replaceDocument(docId, documentContent);
        }
        catch (Exception e)
        {
          this.log.error("Failed to Update life proposal Document  :  " + docId, e);
          throw new ExecutionTerminator();
        }
      }
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "LIFEPRODBSTORE" + "|ERROR|" + "life proposal db stored failed :" + doc_status, e);
      throw new ExecutionTerminator();
    }
  }
}
