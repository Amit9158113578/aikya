package com.idep.proposal.data.commit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;


public class CarrierConfigFetchingURL implements Processor {


	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());


	@Override
	public void process(Exchange exchange) throws Exception 
	{

		String proposalRequest = exchange.getIn().getBody(String.class);
		JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
		int carrierID = reqNode.findValue(ProposalConstants.CARRIER_ID).intValue();
		log.info("carrier ID printed like this -----"+carrierID);
		String carrierspecificid=""+carrierID;

		String carrierName = serverConfig.getDocBYId(ProposalConstants.CARRIER_CONFIG).content().getString(carrierspecificid);
		log.info("carrierName printed lIke this:"+carrierName);


		JsonNode dataNode  = objectMapper.readTree(this.serverConfig.getDocBYId(ProposalConstants.CARRIER_SPECIFIC_CONFIGURATION+carrierName).content().toString());
		log.info("data Node printed "+dataNode);

		JsonNode carrierSpecificConfig = dataNode.get("proposal").get("life");

		log.info("URL---------:"+carrierSpecificConfig);

		String proposalReq = ProposalConstants.PROPOSALXPATHCONF_REQDOC;
		int carrierId =  reqNode.findValue(ProposalConstants.CARRIER_ID).intValue();
		int productId = reqNode.findValue(ProposalConstants.PRODUCT_ID).intValue();

		String name=this.serverConfig.getDocBYId(proposalReq+carrierId+"-"+productId).content().toString();

		JsonNode carrierNode= this.objectMapper.readTree(name);

		((ObjectNode) carrierNode).putAll((ObjectNode) carrierSpecificConfig);

		String documentId = ProposalConstants.PROPOSALXPATHCONF_REQDOC+carrierId+"-"+productId;

		serverConfig.replaceDocument(documentId , JsonObject.fromJson(carrierNode.toString()));

		log.info("carrierNode contains-----------------------"+carrierNode);

	}

}
