package com.idep.imatUpdate.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResponseProcessor implements Processor {
	static Logger log = Logger.getLogger(iMATUpdateRenewalLeadProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static String reqNode = null;
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			reqNode = (String) exchange.getIn().getBody(String.class);

			log.info("MESSAGE in iMATUpdateRenewalLeadProcessor : " + reqNode);
			ObjectNode responseNode = objectMapper.createObjectNode();
			if ((reqNode != null ) && (reqNode.contains("201")))
			{
				responseNode.put("responseCode", "1000");
				responseNode.put("message", "success");
				log.info("iMAT LEAD response generated for : " + reqNode);
				responseNode.put("data", reqNode);
			}
			else
			{
				responseNode.put("responseCode", "1001");
				responseNode.put("message", "failure");
				responseNode.put("data", "");
				log.error("iMAT LEAD response not found : " + reqNode);
			}
			log.info("in get iMAT LEAD responsssseeee" + responseNode);
			exchange.getIn().setBody(responseNode);
		}

		catch(Exception e){
			log.error("error in response processor",e);
		}
		
	}

	
}
