 package com.idep.policy.carrier.req.processor;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.api.impl.SoapConnector;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 import org.dom4j.CDATA;
 import org.dom4j.DocumentHelper;
 
 public class KotakProposalSender implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(KotakProposalSender.class.getName());
   
   SoapConnector extService = new SoapConnector();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String inputReq = exchange.getIn().getBody().toString();
       String soapRequest = "";
       soapRequest = createCDATARequest(inputReq);
       exchange.getIn().setHeader("content-type", "text/xml");
       String soapHeader = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">    <soapenv:Header/>    <soapenv:Body>       <tem:SavePartner_integration>          <tem:Partner_integrationXML>";
       String soapFooter = "</tem:Partner_integrationXML> </tem:SavePartner_integration>    </soapenv:Body> </soapenv:Envelope>";
       soapRequest = soapHeader.concat(soapRequest).concat(soapFooter);
       exchange.getIn().setBody(soapRequest);
       this.log.info(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "SERVICEINVOKE" + "|SUCCESS|" + "carrier proposal request service invoked : " + soapRequest);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "KOTAKPROSENDER" + "|ERROR|" + "Exception at kotakProposalSender:", e);
     } 
   }
   
   public String createCDATARequest(String request) {
     try {
       request = request.substring(38, request.length());
       CDATA cdata = DocumentHelper.createCDATA(request);
       return cdata.asXML();
     } catch (Exception e) {
       this.log.error("Error at createCDATARequest method at KotakProposalSender : " + request, e);
       return null;
     } 
   }
 }


