package com.idep.policy.pdfres.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.pdf.util.PolicyPDFConstants;

/**
 * 
 * @author sandeep.jadhav
 * Create response to generate base 64 PDF string
 */
public class PolicyPDFResProcessor implements Processor {
	
	Logger log = Logger.getLogger(PolicyPDFResProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			/**
			 * spool strategy changed to allow large size PDF stream in response
			 * -1 used to disable overflow to disk, use RAM instead
			 */
			exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1);
			String pdfResponse  = exchange.getIn().getBody(String.class);
			JsonNode pdfResNode = this.objectMapper.readTree(pdfResponse);
			log.debug("final carrier pdfResponse : "+pdfResponse);
			
			ObjectNode finalResponse = this.objectMapper.createObjectNode();
			ObjectNode obj = this.objectMapper.createObjectNode();
			String pdfString = "data:application/pdf;base64,";
			
			if(pdfResNode.get("GetSignPolicyPDFResult").hasNonNull("PolicyPDF"))
			{
				/**
				 * get PDF contents from carrier response
				 */
				finalResponse.put("signedPDF", pdfString.concat(pdfResNode.get("GetSignPolicyPDFResult").get("PolicyPDF").asText()));
				obj.put(PolicyPDFConstants.PROPOSAL_RES_CODE, 1000);
				obj.put(PolicyPDFConstants.PROPOSAL_RES_MSG, "success");
				obj.put(PolicyPDFConstants.PROPOSAL_RES_DATA, finalResponse);
				log.info("Policy PDF digital sign process completed successfully");
				
				
				
			}
			else
			{
				finalResponse.put("signedPDF", "NA");
				obj.put(PolicyPDFConstants.PROPOSAL_RES_CODE, 1050);
				obj.put(PolicyPDFConstants.PROPOSAL_RES_MSG, "PDF cannot be signed as of now, please try later");
				obj.put(PolicyPDFConstants.PROPOSAL_RES_DATA, finalResponse);
			}
		
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
		}
		
		catch(Exception e)
		{
			log.error("Policy PDF digital sign process could not be completed ",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(PolicyPDFConstants.PROPOSAL_RES_CODE, 1002);
			obj.put(PolicyPDFConstants.PROPOSAL_RES_MSG, "server error, please try later");
			obj.put(PolicyPDFConstants.PROPOSAL_RES_DATA, errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
		}
	}
}
