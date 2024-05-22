package com.idep.proposal.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ProposalAddressProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(ProposalAddressProcessor.class.getName());
  CBService service = CBInstanceProvider.getServerConfigInstance();
  JsonNode addressValidationConfig = null;
  
  public void process(Exchange exchange)
    throws Exception
  {
	  		try
		{
				
	  		String proposalRequest = exchange.getIn().getBody(String.class);

		    JsonNode proposalReqNode = this.objectMapper.readTree(proposalRequest);
		    //JsonNode proposerInfo = proposalReqNode.get("proposerInfo");
		    
		    //log.info("Request to ProposalAddressProcessor"+proposalReqNode);
		   // log.info("ProposerInfo node ProposalAddressProcessor"+proposerInfo);

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
		    	if(carrierId!=null && maxLengthNode!=null)
		    	{
		    		carrierAddrLength = maxLengthNode.get(carrierId.asText()+"-2").asInt();
		    		log.info("carrierAddrLength "+ carrierAddrLength);
		    	}
		    }
		    catch(Exception e)
		    {
		    	log.error("unable to get the vehicle reg address max length for carrier. hence used default value 30");
		    }
		    
		    
		    /**
		     * process proposer address details
		     */
		    log.info("carrierAddrLength : "+carrierAddrLength);
		    if(proposalReqNode.get("addressDetails")!=null)
		    {
		    	
		    	String proposerAddress  = proposalReqNode.get("addressDetails").get("communicationAddress").get("doorNo").asText().concat(proposalReqNode.get("addressDetails").get("communicationAddress").get("address").asText());
		    	log.debug("Proposal contactInfo address : "+proposerAddress);
			    /**
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	
			    	((ObjectNode)proposalReqNode.get("addressDetails").get("communicationAddress")).put("addressLine1",proposalReqNode.get("addressDetails").get("communicationAddress").get("doorNo").asText());
			    	((ObjectNode)proposalReqNode.get("addressDetails").get("communicationAddress")).put("addressLine2",proposalReqNode.get("addressDetails").get("communicationAddress").get("address").asText());
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("addressDetails").get("communicationAddress")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    	
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("addressDetails").get("communicationAddress")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check proposer address details in input request");
		    }
		    
		    /**
		     * code for permanentAddress Address breaking in 4 address lines
		     * **/
		    log.info("carrierAddrLength : "+carrierAddrLength);
		    if(proposalReqNode.get("addressDetails")!=null)
		    {
		    	
		    	String proposerAddress  = proposalReqNode.get("addressDetails").get("permanentAddress").get("doorNo").asText().concat(proposalReqNode.get("addressDetails").get("permanentAddress").get("address").asText());
		    	log.debug("Proposal contactInfo address : "+proposerAddress);
			    /**
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	
			    	((ObjectNode)proposalReqNode.get("addressDetails").get("permanentAddress")).put("addressLine1",proposalReqNode.get("addressDetails").get("permanentAddress").get("doorNo").asText());
			    	((ObjectNode)proposalReqNode.get("addressDetails").get("permanentAddress")).put("addressLine2",proposalReqNode.get("addressDetails").get("permanentAddress").get("address").asText());
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("addressDetails").get("permanentAddress")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    	
				    
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("addressDetails").get("permanentAddress")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check proposer address details in input request");
		    }
		    
		    
		    
		    /**
		     * logic for nomineeAddress Address breaking in 4 address lines 
		     * **/
		    log.info("carrierAddrLength : "+carrierAddrLength);
		    if(proposalReqNode.get("nominationDetails")!=null)
		    {
		    
		    	//log.info("doorNo:"+proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("doorNo").asText());
		    	//log.info("address:"+proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("address").asText());
		    	String proposerAddress  = proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("doorNo").asText().concat(proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("address").asText());
		    	log.debug("Proposal nominee address : "+proposerAddress);
			    /**
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	
			    	((ObjectNode)proposalReqNode.get("nominationDetails").get("nomineeAddressDetails")).put("addressLine1",proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("doorNo").asText());
			    	((ObjectNode)proposalReqNode.get("nominationDetails").get("nomineeAddressDetails")).put("addressLine2",proposalReqNode.get("nominationDetails").get("nomineeAddressDetails").get("address").asText());
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("nominationDetails").get("nomineeAddressDetails")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    	
				    
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("nominationDetails").get("nomineeAddressDetails")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check nominee address details in input request");
		    }
		    
	
		    
		    

		    /**
		     * logic for appointee Address breaking in 4 address lines 
		     * **/
		    /**/
		    
		    log.info("carrierAddrLength : "+carrierAddrLength);
		    if(proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")!=null)
		    {
		    
		    	String proposerAddress  = proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("doorNo").asText().concat(proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("address").asText());
		    	log.info("Proposal appointee address : "+proposerAddress);
			    /**
			     * .nominationDetails.appointeeDetails.appointeeAddressDetails
			     * if entered address is less than limit specified in configuration
			     */
			    if(proposerAddress.length()<=carrierAddrLength){
	                 log.info("nominationDetails.appointeeDetails.appointeeAddressDetails.doorNo ");                                                                           //.nominationDetails.appointeeDetails.appointeeAddressDetails.doorNo
			    	((ObjectNode)proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")).put("addressLine1",proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("doorNo").asText());
			    	((ObjectNode)proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")).put("addressLine2",proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails").get("address").asText());
			    	
	
			    }
			    else
			    {
			    	int start=0;
				    int end =carrierAddrLength;
				    int i=0;
				    for (i=1;i<=(proposerAddress.length()/carrierAddrLength);i++){
	
				    	((ObjectNode)proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")).put("addressLine"+i,proposerAddress.substring(start, end));
				    	start=end;
				    	end=end+carrierAddrLength;
				    	
				    
				    }
				    if (proposerAddress.length() % carrierAddrLength > 0) {
				        ((ObjectNode)proposalReqNode.get("nominationDetails").get("appointeeDetails").get("appointeeAddressDetails")).put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength));
				      }
			    }
		    }
		    else
		    {
		    	log.error("please check appointee address details in input request");
		    }
	
		    
		    
		    
		    
		    
		    
	    
		    
		    log.info("proposalReqNode outside the address procssor : "+proposalReqNode);
		    exchange.getIn().setBody(proposalReqNode);
		    exchange.setProperty(ProposalConstants.PROPOSAL_ID,proposalReqNode.findValue(ProposalConstants.PROPOSAL_ID) );
		    
		}
		catch(NullPointerException e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.LIFEPROADDRESSPRO+"|ERROR|"+"Address fields seems to be missing in proposerDetails : ", e);
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.LIFEPROADDRESSPRO+"|ERROR|"+"Exception at ProposalAddressProcessor : ", e);
		}
	
	  
	
}
}