package com.idep.restapi.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.utils.EncrypDecryptOperation;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;
import com.nimbusds.jose.util.StandardCharset;
import com.nimbusds.jwt.SignedJWT;

public class RESTResponseFormatter implements Processor {
  String docId = "";
  
  Logger log = Logger.getLogger(RESTResponseFormatter.class.getName());
  
  public void process(Exchange exchange) {
    int carrierId = 0;
	int productId;
    try {
      String resBody = (String)exchange.getIn().getBody(String.class);
      this.log.info("request to RESTResponseFormatter :" + resBody);
      if (exchange.getProperty("inputRequest") != null) {
        JsonNode inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
        JsonNode configurations = null;
        String docId = null;
        int lob = inputRequest.findValue("lob").asInt();
        carrierId = inputRequest.findValue("carrierId").asInt();
        if (lob == 4) {
			productId = inputRequest.findValue("planId").asInt();
		} else {
			productId = inputRequest.findValue("productId").asInt();
		}
        String stage = inputRequest.findValue("stage").asText();
        JsonNode policyType = inputRequest.findValue("policyType");
        if (policyType != null) {
          docId = "RESTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-" + policyType.asText();
          configurations = RestAPIConstants.objectMapper.readTree(((JsonObject)RestAPIConstants.serverConfig.getDocBYId(docId).content()).toString());
          this.log.info("rest Response document :" + docId);
        } else {
          docId = "RESTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
          configurations = RestAPIConstants.objectMapper.readTree(((JsonObject)RestAPIConstants.serverConfig.getDocBYId(docId).content()).toString());
          this.log.info("rest Response document :" + docId);
        } 
        if (configurations != null) {
          exchange.getIn().setHeader("configDocumentFound", "True");
          if (configurations.has("restDataType") && configurations.get("restDataType").asText().equalsIgnoreCase("XML")) {
            exchange.getIn().setHeader("RestDataType", "XML");
            if (configurations.has("replaceTags")) {
              JsonNode replaceTags = RestAPIConstants.objectMapper.readTree(configurations.get("replaceTags").toString());
              for (JsonNode replaceTag : replaceTags)
                resBody = resBody.replaceAll(replaceTag.get("replaceTo").asText(), replaceTag.get("replaceWith").asText()); 
            } 
            String tagName = configurations.get("responseTagName").asText();
            this.log.info("tagName :" + tagName);
            this.log.info("response after replacing characters :" + resBody);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(new InputSource(new StringReader(resBody)));
            Node nodeList = document.getElementsByTagName(tagName).item(0);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(nodeList);
            StringWriter outWriter = new StringWriter();
            StreamResult result = new StreamResult(outWriter);
            transformer.transform(source, result);
            StringBuffer sb = outWriter.getBuffer();
            String finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
            finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            exchange.getIn().setBody(finalstring);
            exchange.setProperty("RESTXMLConfiguration", configurations);
          } else {
            exchange.getIn().setHeader("RestDataType", "JSON");
            if (configurations.has("convertToJson") && configurations.get("convertToJson").asText().equalsIgnoreCase("False")) {
              this.log.info("validation convert to json  : false");
              ObjectNode data = RestAPIConstants.objectMapper.createObjectNode();
              data.put("data", resBody);
              exchange.getIn().setHeader("carrierResponse", "Success");
              this.log.info("carrier response  success");
              exchange.getIn().setBody(data);
            } else {
              JsonNode resNode = RestAPIConstants.objectMapper.readTree(resBody);
             if (configurations.has(RestAPIConstants.DECRYPT_PAYLOAD) && configurations.get(RestAPIConstants.DECRYPT_PAYLOAD).has(RestAPIConstants.SECRET_KEY))
              {
            	  String decrypt = EncrypDecryptOperation.decrypt(configurations.get(RestAPIConstants.DECRYPT_PAYLOAD).get(RestAPIConstants.SECRET_KEY).asText(), resNode.get(configurations.get(RestAPIConstants.DECRYPT_PAYLOAD).get(RestAPIConstants.REQUEST_KEY).asText()).toString(),configurations.get(RestAPIConstants.DECRYPT_PAYLOAD).get(RestAPIConstants.IV).asText());
            	  ((ObjectNode) resNode).put(configurations.get(RestAPIConstants.DECRYPT_PAYLOAD).get(RestAPIConstants.REQUEST_KEY).asText(), RestAPIConstants.objectMapper.readTree(decrypt));
              }
              
              if (configurations.has("validateResponseNode")) {
                JsonNode validations = configurations.get("validateResponseNode");
                this.log.info("validation node : " + validations);
                if (validations.has("successResponseKey") && validations.has("successResponseValue")) {
                  this.log.info("inside validate response node :"+resNode);
                  if (!resNode.findValue(validations.get("successResponseKey").asText()).isNull()) {
                	    if (configurations.get("carrierId").asInt() == 52 && validateBajajResponse(configurations, resNode, validations))
						{
							exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponseWithData(carrierId, resNode));
							exchange.getIn().setHeader("carrierResponse", "Failure");
						    throw new Exception("Invalid response !");
						}
						
                    if (validations.has("checkSuccessResponse") && validations.get("checkSuccessResponse").asText().equalsIgnoreCase("Y")) {
                      if (resNode.findValue(validations.get("successResponseKey").asText()).asText().equalsIgnoreCase(validations.get("successResponseValue").asText())) {
                        if (validations.has("maxAllowedIDV"))
                          if (validateMaxVehicleIDV(resNode.findValue(validations.get("IDVClientTag").asText()).asDouble(), validations.get("maxAllowedIDV").asDouble())) {
                            exchange.getIn().setHeader("carrierResponse", "Success");
                          } else {
                            exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponseWithData(carrierId, resNode));
                            exchange.getIn().setHeader("carrierResponse", "Failure");
                            throw new Exception();
                          }  
                        if (validations.has("maxAllowedCubicCapacity")) {
                          this.log.info("test if string comming here :1");
                          if (validateMaxVehicleIDV(resNode.findValue(validations.get("CubicCapacityClientTag").asText()).asDouble(), validations.get("maxAllowedCubicCapacity").asDouble())) {
                            this.log.info("test if string comming here :2");
                            exchange.getIn().setHeader("carrierResponse", "Success");
                          } else {
                            this.log.info("test if string comming here :3");
                            exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponseWithData(carrierId, resNode));
                            exchange.getIn().setHeader("carrierResponse", "Failure");
                            throw new Exception();
                          } 
                        } else {
                          exchange.getIn().setHeader("carrierResponse", "Success");
                        } 
                      } else {
                        exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponseWithData(carrierId, resNode));
                        exchange.getIn().setHeader("carrierResponse", "Failure");
                      } 
                    } else {
                      exchange.getIn().setHeader("carrierResponse", "Success");
                      this.log.info("carrier response  success");
                    } 
                    if(configurations.has("EncryptKYCDetails") && configurations.get("stage").textValue().equals("KYCAUTHZ") && configurations.get("carrierId").asInt()==29)
					{
						String token =resNode.findValue(validations.get("successResponseKey").asText()).asText();
						resNode=parseJWTForICICI(token, inputRequest,resNode);
						log.info("ICICI KYC response :"+resNode);
					}
                    
                    exchange.getIn().setBody(resNode);
                  } else {
                    exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponseWithData(carrierId, resNode));
                    exchange.getIn().setHeader("carrierResponse", "Failure");
                    this.log.info("carrier response  failure");
                    exchange.getIn().setBody(ResponseMessageProcessor.returnNotValidcarrierResponse(carrierId, resNode));
                  } 
                } else if (validations.has("droolResponse") && validations.get("droolResponse").asText().equalsIgnoreCase("True")) {
                  this.log.info("inside response validation");
                  if (resNode.findValue(validations.get("droolResponseValidateKey").asText()) != null) {
                    String droolRes = resNode.findValue(validations.get("droolResponseValidateKey").asText()).asText();
                    JsonNode droolResNode = RestAPIConstants.objectMapper.readTree(droolRes);
                    exchange.getIn().setHeader("carrierResponse", "Success");
                    this.log.info("carrier response  success");
                    exchange.getIn().setBody(droolResNode.findValue(validations.get("droolResponseNode").asText()));
                  } else {
                    exchange.getIn().setBody(ResponseMessageProcessor.returnNotValidcarrierResponse(carrierId, resNode));
                    exchange.getIn().setHeader("carrierResponse", "Failure");
                    this.log.info("carrier response  failure");
                  } 
                } 
              } else {
                this.log.info("carrier doesn't contains validate response node");
                exchange.getIn().setHeader("carrierResponse", "Success");
                this.log.info("carrier response  success");
                exchange.getIn().setBody(resNode);
              } 
            } 
          } 
        } else {
          this.log.error("configDoc is not found : " + docId);
          exchange.getIn().setHeader("configDocumentFound", "False");
          exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
        } 
      } 
    } catch (Exception e) {
      this.log.error("Exception in RESTResponseFormatter processor : " + ExceptionUtils.getFullStackTrace(e));
      exchange.getIn().setHeader("carrierResponse", "Failure");
      e.printStackTrace();
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
    } 
  }
  
  public boolean validateMaxVehicleIDV(double vehicleIDV, double MAXVehicleIDV) {
    if (vehicleIDV <= MAXVehicleIDV)
      return true; 
    return false;
  }
  public JsonNode parseJWTForICICI(String jwtToken, JsonNode requestNode,JsonNode resNode) throws Exception {
	    try {
	        SignedJWT decodedJWT = SignedJWT.parse(jwtToken);
	        JsonNode readTree = RestAPIConstants.objectMapper.readTree(decodedJWT.getPayload().toString());
	        String pbk = readTree.get("pbk").asText();
	        String publicKey=pbk.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
	        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
		    Cipher encrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
		    	encrypt.init(Cipher.ENCRYPT_MODE, pubKey);
		     byte[] dobencrypted = encrypt.doFinal(requestNode.findValue("dateOfBirth").asText().getBytes(StandardCharset.UTF_8));
		     String dob = new String(Base64.getEncoder().encode(dobencrypted));
		     byte[] pancardencrypted = encrypt.doFinal(requestNode.findValue("panNumber").asText().getBytes(StandardCharset.UTF_8));
		     String pancard = new String(Base64.getEncoder().encode(pancardencrypted));
		     ((ObjectNode)resNode).put("encryptedDOB", dob);
		     ((ObjectNode)resNode).put("encryptedPAN", pancard);
		     return resNode;
	    } catch (ParseException e) {
	        throw new Exception("Invalid token!");
	    }
	}
  
	public boolean validateBajajResponse(JsonNode configurations, JsonNode resNode, JsonNode validations) {
		if (configurations.get("stage").textValue().equals("Quote")
				&& resNode.findValue(validations.get("successResponseKey").asText()).textValue().equals("null")) {
				return true;
		}

		if (configurations.get("stage").textValue().equals("Proposal")
				&& resNode.findValue(validations.get("successResponseKey").asText()).size() > 0) {
				return true;
		}

		return false;

	}
}
