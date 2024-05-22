package com.idep.policy.payment.res.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


public class PaymentResponseValidator implements Processor{

	Logger log =  Logger.getLogger(PaymentResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	static String ExceptionHandlerQ = "ExceptionHandlerQ";

	@Override
	public void process(Exchange exchange) throws Exception {
		CamelContext camelContext = exchange.getContext();
		ProducerTemplate template = camelContext.createProducerTemplate();
		JsonNode inputReqNode = null;
		try{
			log.debug("PaymentResponseValidator ");
			String inputReq = exchange.getIn().getBody(String.class);
			inputReqNode = objectMapper.readTree(inputReq.toString());
			log.debug("PaymentResponseValidator inputReqNode: "+inputReqNode);
			if(inputReqNode.get(ProposalConstants.TRANSSTATUSINFO).get(ProposalConstants.TRANSSTATUSCODE).asInt()==0)
			{
				log.debug("PaymentResponseValidator transaction status is 0 ");
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|"+"Payment response received - PaymentFail : "+inputReqNode.toString());
				throw new ExecutionTerminator();				
			}
			else
			{
				exchange.getIn().setBody(inputReqNode);
			}

		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PAYMENTRES+"|FAIL|",e);


			String trace = "Error in Class :"+PaymentResponseValidator.class+"   Line Number :"+Thread.currentThread().getStackTrace()[0].getLineNumber();
			log.info("Erroror messgaes PaymentResponseValidator"+PaymentResponseValidator.class+"    "+Thread.currentThread().getStackTrace()[0].getLineNumber());
			String uri = "activemq:queue:" + ExceptionHandlerQ;
			((ObjectNode) inputReqNode).put("transactionName","PaymentResponseValidator");
			((ObjectNode) inputReqNode).put("Exception",e.toString());
			((ObjectNode) inputReqNode).put("ExceptionMessage",trace);
			exchange.getIn().setBody(inputReqNode.toString());
			log.info("sending to exception handler queue"+inputReqNode);
			exchange.setPattern(ExchangePattern.InOnly);
			template.send(uri, exchange);

			throw new ExecutionTerminator();
		}
	}
}
