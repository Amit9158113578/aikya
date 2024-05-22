 package com.idep.policy.document.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.api.impl.SoapConnector;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PolicyPDFSignReqProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(PolicyPDFSignReqProcessor.class.getName());
   
   CBService service = CBInstanceProvider.getServerConfigInstance();
   
   public void process(Exchange exchange) throws Exception {
     try {
       this.log.info("PolicyPDFSignReqProcessor invoked");
       String inputReq = exchange.getIn().getBody().toString();
       SoapConnector soapService = new SoapConnector();
       JsonNode reqNode = this.objectMapper.readTree(inputReq);
       LinkedHashMap<String, Object> orderedReq = new LinkedHashMap<>();
       LinkedHashMap<String, Object> sampleMap = new LinkedHashMap<>();
       sampleMap.put("strUserId", "");
       sampleMap.put("strPassword", "");
       sampleMap.put("strProdType", "");
       sampleMap.put("strPolicyNo", "");
       sampleMap.put("strPolicyIssueTime", "");
       sampleMap.put("strTransactionID", "");
       sampleMap.put("strTransactionRef", "");
       sampleMap.put("strCustomerName", "");
       sampleMap.put("strPolicyPDF", "");
       for (Map.Entry<String, Object> entry : sampleMap.entrySet())
         orderedReq.put(entry.getKey(), reqNode.get(entry.getKey()).textValue()); 
       String soapRequest = soapService.prepareSoapRequest("GetSignPolicyPDF", "http://tempuri.org/", orderedReq);
       this.log.info(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "SERVICEINVOKE|SUCCESS|carrier service invoked : " + soapRequest);
       exchange.getIn().setBody(soapRequest);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "SERVICEINVOKE|ERROR|error carrier service invoked :", e);
     } 
   }
 }


