 package com.idep.policy.res.processor;
 
 import com.idep.api.impl.SoapConnector;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class SOAPResultProcessor implements Processor {
   Logger log = Logger.getLogger(SOAPResultProcessor.class.getName());
   
   SoapConnector soapService = new SoapConnector();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String pdfResponse = (String)exchange.getIn().getBody(String.class);
       String pdfSoapResponse = this.soapService.retriveSoapResult(pdfResponse, "GetSignPolicyPDFResult");
       String formattedResponse = pdfSoapResponse.replaceAll("i:nil=\"true\"", "");
       exchange.getIn().setBody(formattedResponse);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("proper response not found from SOAPResultProcessor ");
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


