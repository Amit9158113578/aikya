 package com.idep.soap.service;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.jolt.soap.rest.DataCacher;
 import com.idep.soap.util.ResponseMessageProcessor;
 import com.idep.soap.util.SoapUtils;
 import java.io.ByteArrayOutputStream;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import javax.xml.namespace.QName;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPBodyElement;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.soap.SOAPPart;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 import org.w3c.dom.CDATASection;
 
 public class SOAPRequestFormatter implements Processor {
   Logger log = Logger.getLogger(SOAPRequestFormatter.class.getName());
   
   DataCacher dataCacher = new DataCacher();
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       String requestBody = (String)exchange.getIn().getBody();
       if (exchange.getProperty("inputRequest") != null) {
         JsonNode inputRequest = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
         JsonNode configurations = null;
         String docId = null;
         int lob = inputRequest.findValue("lob").asInt();
         this.carrierId = inputRequest.findValue("carrierId").asInt();
         int productId = inputRequest.findValue("productId").asInt();
         String stage = inputRequest.findValue("stage").asText();
         JsonNode policyType = inputRequest.findValue("policyType");
         if (policyType != null) {
           docId = "SOAPRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType.asText();
           configurations = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.serverConfig.getDocBYId(docId).content()).toString());
         } else {
           docId = "SOAPRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId;
           configurations = this.dataCacher.docCache(docId);
         } 
         if (configurations != null) {
           String requestFormed = createSOAPRequest(configurations, requestBody);
           requestFormed = requestFormed.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").replace("<o>", "").replace("</o>", "");
           if (configurations.has("ReplaceData")) {
             
             JsonNode ReplaceData = SoapUtils.objectMapper.readTree(configurations.get("ReplaceData").toString());
             requestFormed = requestFormed.replace(ReplaceData.get(0).get("old").asText(), ReplaceData.get(0).get("new").asText()).replace(ReplaceData.get(1).get("old").asText(), ReplaceData.get(1).get("new").asText()).replace(ReplaceData.get(2).get("old").asText(), ReplaceData.get(2).get("new").asText()).replace(ReplaceData.get(3).get("old").asText(), ReplaceData.get(3).get("new").asText());
           } 
           
           this.log.info("requestFormed after replacing:" + requestFormed);
           ObjectNode object = SoapUtils.objectMapper.createObjectNode();
           object.put("carrierData", requestFormed);
           object.put("url", configurations.get("URL"));
           object.put("headers", configurations.get("headers"));
           object.put("inputRequest", inputRequest);
           object.put("subStage", "Request");
           exchange.getIn().setBody(object.toString());
           exchange.getIn().setHeader("configDocumentFound", "True");
         } else {
           this.log.error("Configuration Document not found for docId :" + docId);
           exchange.getIn().setHeader("configDocumentFound", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnConfigDocResponse("Configuration Document Not Found, DocId : SOAPRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType, this.carrierId));
         } 
       } else {
         this.log.error("InputRequest property is not set");
         exchange.getIn().setHeader("configDocumentFound", "False");
         exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.carrierId));
       } 
     } catch (Exception e) {
       this.log.error("Exception in SOAPRequestFormatter", e);
       exchange.getIn().setHeader("configDocumentFound", "False");
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.carrierId));
     } 
   }
   
   public String createSOAPRequest(JsonNode configDoc, String request) throws Exception {
     MessageFactory factory = MessageFactory.newInstance();
     SOAPMessage message = factory.createMessage();
     SOAPPart soapPart = message.getSOAPPart();
     String myNamespace = configDoc.get("tnsName").asText();
     String myNamespaceURI = configDoc.get("tnsURL").asText();
     SOAPEnvelope envelope = soapPart.getEnvelope();
     envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
     QName bodyName = null;
     SOAPBody soapBody = envelope.getBody();
     if (configDoc.has("nodeName")) {
       bodyName = new QName(configDoc.get("nodeUrlName").asText(), configDoc.get("nodeName").asText(), configDoc.get("nodeNsName").asText());
       SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);
       if (configDoc.has("appendInputRequest"))
         if (configDoc.has("appendCDATA")) {
           CDATASection cdata = bodyElement.getOwnerDocument().createCDATASection(request);
           bodyElement.appendChild(cdata);
           this.log.info("soapBodyElem :" + bodyElement);
         } else {
           bodyElement.addTextNode(request);
         }  
       if (configDoc.has("parameters")) {
         JsonNode parameters = SoapUtils.objectMapper.readTree(configDoc.get("parameters").toString());
         for (JsonNode params : parameters) {
           SOAPElement soapBodyElem = bodyElement.addChildElement(params.get("nodeName").asText(), params.get("nodeNsName").asText(), params.get("nodeUrlName").asText());
           if (params.has("defaultValue"))
             soapBodyElem.addTextNode(params.get("defaultValue").asText()); 
           if (params.has("appendInputRequest"))
             if (params.has("appendCDATA")) {
               CDATASection cdata = soapBodyElem.getOwnerDocument().createCDATASection(request);
               soapBodyElem.appendChild(cdata);
             } else {
               soapBodyElem.addTextNode(request);
             }  
           this.log.info("params :" + params);
           if (params.has("parameters")) {
             JsonNode parameter = SoapUtils.objectMapper.readTree(params.get("parameters").toString());
             this.log.info("parameter :" + parameter);
             for (JsonNode param : parameter) {
               this.log.info("param :" + param);
               SOAPElement soapBodyElem2 = soapBodyElem.addChildElement(param.get("nodeName").asText(), param.get("nodeNsName").asText(), param.get("nodeUrlName").asText());
               if (param.has("defaultValue"))
                 soapBodyElem2.addTextNode(param.get("defaultValue").asText()); 
               if (param.has("appendInputRequest"))
                 if (param.has("appendCDATA")) {
                   CDATASection cdata = soapBodyElem2.getOwnerDocument().createCDATASection(request);
                   soapBodyElem2.appendChild(cdata);
                 } else {
                   soapBodyElem2.addTextNode(request);
                 }  
               if (param.has("parameters")) {
                 JsonNode childParameters = SoapUtils.objectMapper.readTree(param.get("parameters").toString());
                 this.log.info("childParameters :" + childParameters);
                 for (JsonNode childParams : childParameters) {
                   SOAPElement soapBodyElem3 = soapBodyElem2.addChildElement(childParams.get("nodeName").asText(), childParams.get("nodeNsName").asText(), childParams.get("nodeUrlName").asText());
                   if (childParams.has("defaultValue"))
                     soapBodyElem3.addTextNode(childParams.get("defaultValue").asText()); 
                   if (childParams.has("appendInputRequest"))
                     if (childParams.has("appendCDATA")) {
                       CDATASection cdata = soapBodyElem3.getOwnerDocument().createCDATASection(request);
                       soapBodyElem3.appendChild(cdata);
                     } else {
                       soapBodyElem3.addTextNode(request);
                     }  
                   if (childParams.has("parameters")) {
                     JsonNode childParameter = SoapUtils.objectMapper.readTree(childParams.get("parameters").toString());
                     this.log.info("childParameter :" + childParameter);
                     for (JsonNode childParam : childParameter) {
                       SOAPElement soapBodyElem4 = soapBodyElem3.addChildElement(childParam.get("nodeName").asText());
                       if (childParam.has("defaultValue"))
                         soapBodyElem4.addTextNode(childParam.get("defaultValue").asText()); 
                       if (childParam.has("appendInputRequest")) {
                         if (childParam.has("appendCDATA")) {
                           CDATASection cdata = soapBodyElem4.getOwnerDocument().createCDATASection(request);
                           soapBodyElem4.appendChild(cdata);
                           this.log.info("soapBodyElem4 :" + soapBodyElem3);
                           continue;
                         } 
                         soapBodyElem4.addTextNode(request);
                       } 
                     } 
                   } 
                 } 
               } 
             } 
           } 
         } 
       } 
     } 
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     message.writeTo(baos);
     String requestFormed = baos.toString();
     requestFormed = requestFormed.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
     
     if (configDoc.has("CDATASection")) {
       String beforeThisTag = configDoc.get("CDATASection").get("beforeThisTag").asText();
       String afterThisTag = configDoc.get("CDATASection").get("afterThisTag").asText();
       requestFormed = requestFormed.replace(beforeThisTag, "<![CDATA[" + beforeThisTag);
       requestFormed = requestFormed.replace(afterThisTag, String.valueOf(String.valueOf(afterThisTag)) + "]]>");
     } 
     requestFormed = requestFormed.replaceAll("&#xD;", "");
     this.log.info("Request before clientReqAttrList :" + requestFormed);
     if (configDoc.has("clientReqAttrList")) {
       JsonNode clientReqAttrList = configDoc.get("clientReqAttrList");
       ObjectNode objectNode = SoapUtils.objectMapper.createObjectNode();
       for (JsonNode list : clientReqAttrList)
         objectNode.put(list.get("appKey").asText(), list.get("clientKey").asText()); 
       Map<String, String> ClientReqAttrList = (Map<String, String>)SoapUtils.objectMapper.readValue(objectNode.toString(), Map.class);
       Set<Map.Entry<String, String>> clientEntrySet = ClientReqAttrList.entrySet();
       Iterator<Map.Entry<String, String>> clientItr = clientEntrySet.iterator();
       while (clientItr.hasNext()) {
         Map.Entry<String, String> clientEntry = clientItr.next();
         String key = ((String)clientEntry.getKey()).trim();
         String value = ((String)clientEntry.getValue()).trim();
         requestFormed = requestFormed.replaceAll(key, value);
       } 
     } 
     this.log.info("Request Formed :" + requestFormed);
     return requestFormed;
   }
 }


