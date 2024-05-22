package com.idep.imatLead.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class iMATLeadProcessor implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(iMATLeadProcessor.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	PrepareiMATLead imatLead = new PrepareiMATLead();
		
	@Override
	public void process(Exchange exchange) throws Exception {
		{
			String leadresponseNode = null;
			try
			{
				JsonNode request = objectMapper.readTree((String)exchange.getIn().getBody(String.class));
				log.info("iMAT LEAD Request recived for Processing : " + request);
				
					leadresponseNode = imatLead.prepareLead(request);
				
				ObjectNode responseNode = objectMapper.createObjectNode();
				if ((leadresponseNode != null ) && (leadresponseNode.contains("201")))
				{
					responseNode.put("responseCode", "1000");
					responseNode.put("message", "success");
					log.info("iMAT LEAD response generated for : " + leadresponseNode);
					responseNode.put("data", leadresponseNode);
				}
				else
				{
					responseNode.put("responseCode", "1001");
					responseNode.put("message", "failure");
					responseNode.put("data", "");
					log.error("iMAT LEAD response not found : " + request);
				}
				log.info("in get iMAT LEAD responsssseeee" + responseNode);
				exchange.getIn().setBody(responseNode);
			}
			catch (Exception e)
			{
				log.error("unable to process iMAT LEAD  request : ", e);
			}
		}

	}
}