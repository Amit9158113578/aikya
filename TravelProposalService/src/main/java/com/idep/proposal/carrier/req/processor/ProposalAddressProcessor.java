package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

public class ProposalAddressProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalAddressProcessor.class.getName());
	CBService service = CBInstanceProvider.getServerConfigInstance();
	JsonNode addressValidationConfig = null;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try
		{
			String proposalRequest = exchange.getIn().getBody(String.class);

		    JsonNode proposalReqNode = this.objectMapper.readTree(proposalRequest);
		    JsonNode proposerInfo = proposalReqNode.get("proposerDetails");
		    int carrierAddrLength = 30;
		    
		    try
		    {
		    	if(addressValidationConfig == null)
		    	{
		    		JsonDocument addressConfigDocument = service.getDocBYId("ProposerAddressConfig");
		    		if(addressConfigDocument!=null)
		    		{
		    			addressValidationConfig = objectMapper.readTree(addressConfigDocument.content().toString());
		    		}
		    		else
		    		{
		    			log.error("ProposerAddressConfig document not found in DB");
		    		}
		    	}
		    	
		    	JsonNode maxLengthNode = addressValidationConfig.get("proposerAddressValidation").get("maxlength");
		    	JsonNode carrierId = proposalReqNode.findValue("carrierId");
		    	JsonNode businessLineId = proposalReqNode.findValue("businessLineId");
		    	if(carrierId!=null && maxLengthNode!=null)
		    	{
		    		carrierAddrLength = maxLengthNode.get(carrierId.asText()+"-"+businessLineId).asInt();
		    	}
		    }
		    catch(Exception e)
		    {
		    	log.error("unable to get the Travel reg address max length for carrier. hence used default value 30");
		    }
		    
		    
		    /**
		     * process proposer address details
		     */
		    log.info("carrierAddrLength : "+carrierAddrLength);
		    if(proposerInfo.get("communicationAddress")!=null)
		    {
		    	
		    	String proposerAddress  = proposerInfo.get("communicationAddress").get("houseNo").asText()+", ".concat(proposerInfo.get("communicationAddress").get("addressLine").asText());
		    	log.debug("Proposal contactInfo address : "+proposerAddress);
			    /**
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	
			    	((ObjectNode)proposalReqNode.get("proposerDetails").get("communicationAddress")).put("addressLine1",proposerInfo.get("communicationAddress").get("houseNo").asText());
			    	((ObjectNode)proposalReqNode.get("proposerDetails").get("communicationAddress")).put("addressLine2",proposerInfo.get("communicationAddress").get("addressLine").asText());
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("proposerDetails").get("communicationAddress")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("proposerDetails").get("communicationAddress")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check proposer address details in input request");
		    }
		    
		    /**
		     * permanentAddress Address break in addressLine1 to til n  
		     * **/
		    
		    if(proposerInfo.get("permanantAddress")!=null)
		    {
		    	String proposerAddress  = proposerInfo.get("permanantAddress").get("houseNo").asText()+", ".concat(proposerInfo.get("permanantAddress").get("addressLine").asText());
		    	log.debug("Proposal permanentAddress address : "+proposerAddress);
			    /**
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	
			    	((ObjectNode)proposalReqNode.get("proposerDetails").get("permanantAddress")).put("addressLine1", proposerInfo.get("permanantAddress").get("houseNo").asText());
			    	((ObjectNode)proposalReqNode.get("proposerDetails").get("permanantAddress")).put("addressLine2",proposerInfo.get("permanantAddress").get("addressLine").asText());
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("proposerDetails").get("permanantAddress")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("proposerDetails").get("permanantAddress")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check proposer address details in input request");
		    }
		    
		    
		    log.debug("proposalReqNode : "+proposalReqNode);
		    exchange.getIn().setBody(proposalReqNode);
		    exchange.setProperty(ProposalConstants.PROPOSAL_ID,proposalReqNode.findValue(ProposalConstants.PROPOSAL_ID) );
		    
		}
		catch(NullPointerException e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPROADDRESSPRO+"|ERROR|"+"Address fields seems to be missing in proposerDetails : ", e);
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPROADDRESSPRO+"|ERROR|"+"Exception at ProposalAddressProcessor : ", e);
		}
	}
	
	
	

}
