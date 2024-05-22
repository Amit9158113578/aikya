package com.idep.restapi.service;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.utils.CreateJWTToken;
import com.idep.restapi.utils.EncrypDecryptOperation;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;
import com.nimbusds.jose.util.StandardCharset;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.Cipher;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class RESTRequestFormatter implements Processor {
	String docId = "";

	String requestURL = "";

	Logger log = Logger.getLogger(RESTRequestFormatter.class.getName());

	public void process(Exchange exchange) {
		int carrierId = 0;
		int productId;
		try {
			Object joltResponse = exchange.getIn().getBody();
			JsonNode joltResNode = RestAPIConstants.objectMapper.readTree(joltResponse.toString());
			if (exchange.getProperty("inputRequest") != null) {
				JsonNode inputRequest = RestAPIConstants.objectMapper
						.readTree(exchange.getProperty("inputRequest").toString());
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
					docId = "RESTRequest-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-"
							+ policyType.asText();
					this.log.info("docId : " + docId);
					configurations = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(docId).content()).toString());
				} else {
					docId = "RESTRequest-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
					configurations = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(docId).content()).toString());
				}
				
				if (configurations != null) {
					if (configurations.has("URL"))
						this.requestURL = configurations.get("URL").asText();
					if (configurations.has(RestAPIConstants.REST_DATA_TYPE_SMALL)
							&& configurations.get(RestAPIConstants.REST_DATA_TYPE_SMALL).asText().equalsIgnoreCase("XML")) {
						exchange.getIn().setHeader(RestAPIConstants.REST_DATA_TYPE, "XML");
						exchange.setProperty("RESTXMLConfiguration", configurations);
						if (configurations.has(RestAPIConstants.BY_PASS_URL)
								&& configurations.get(RestAPIConstants.BY_PASS_URL).asText().equalsIgnoreCase("true")) {
							exchange.getIn().setHeader(RestAPIConstants.BY_PASS_URL, "True");
						} else {
							exchange.getIn().setHeader(RestAPIConstants.BY_PASS_URL, "False");
						}
						if (configurations.has(RestAPIConstants.IS_REQUEST_TYPE_GET)
								&& configurations.get(RestAPIConstants.IS_REQUEST_TYPE_GET).asText().equalsIgnoreCase("Y")) {
							exchange.getIn().setHeader(RestAPIConstants.IS_REQUEST_TYPE_GET, "True");
							 if(configurations.has(RestAPIConstants.IS_METHOD_TYPE_GET) && configurations.get(RestAPIConstants.IS_METHOD_TYPE_GET).asText().equalsIgnoreCase("GET"))
							  {
							 	exchange.getIn().setHeader(RestAPIConstants.IS_METHOD_TYPE_GET, "True");
							   }
							 else
							 {
								 exchange.getIn().setHeader(RestAPIConstants.IS_METHOD_TYPE_GET, "False");
							 }
							 
						} else {
							exchange.getIn().setHeader(RestAPIConstants.IS_REQUEST_TYPE_GET, "False");
						}
						exchange.getIn().setBody(joltResNode.toString());
					} else {
						ObjectNode objectNode1 = null;
						exchange.getIn().setHeader(RestAPIConstants.REST_DATA_TYPE, "JSON");
						if (configurations.has(RestAPIConstants.BY_PASS_URL)
								&& configurations.get(RestAPIConstants.BY_PASS_URL).asText().equalsIgnoreCase("true")) {
							exchange.getIn().setHeader(RestAPIConstants.BY_PASS_URL, "True");
						} else {
							exchange.getIn().setHeader(RestAPIConstants.BY_PASS_URL, "False");
						}
						if (configurations.has(RestAPIConstants.IS_REQUEST_TYPE_GET)
								&& configurations.get(RestAPIConstants.IS_REQUEST_TYPE_GET).asText().equalsIgnoreCase("Y")) {
							exchange.getIn().setHeader(RestAPIConstants.IS_REQUEST_TYPE_GET, "True");
							 if(configurations.has(RestAPIConstants.IS_METHOD_TYPE_GET) && configurations.get(RestAPIConstants.IS_METHOD_TYPE_GET).asText().equalsIgnoreCase("GET"))
							  {
							 	exchange.getIn().setHeader(RestAPIConstants.IS_METHOD_TYPE_GET, "True");
							   }
							 else
							 {
								 exchange.getIn().setHeader(RestAPIConstants.IS_METHOD_TYPE_GET, "False");
							 }
						} else {
							exchange.getIn().setHeader(RestAPIConstants.IS_REQUEST_TYPE_GET, "False");
						}
						if (configurations.has(RestAPIConstants.CHANG_URL)) {
							this.requestURL = this.requestURL.replace(
									configurations.get(RestAPIConstants.CHANG_URL).get("replaceField").asText(),
									joltResNode.findValue(configurations.get(RestAPIConstants.CHANG_URL).get("replaceValue").asText())
											.asText());
							this.log.info("request URL changed : " + this.requestURL + "replace Field : "
									+ configurations.get(RestAPIConstants.CHANG_URL).get("replaceField").asText() + " replace Value : "
									+ joltResNode
											.findValue(configurations.get(RestAPIConstants.CHANG_URL).get("replaceValue").asText())
											.asText());
						}
						if (configurations.has("fieldNameReplacement")) {
							ArrayNode fieldNameRepArray = (ArrayNode) configurations.get("fieldNameReplacement");
							for (JsonNode fieldNameRep : fieldNameRepArray) {
								this.log.info("FieldReplacement tag : " + fieldNameRep.get("destFieldName").asText());
								this.log.info("FieldReplacement Value tag : "
										+ joltResNode.findValue(fieldNameRep.get("sourceFieldName").asText()).asText());
								this.requestURL = this.requestURL.replace(fieldNameRep.get("destFieldName").asText(),
										joltResNode.findValue(fieldNameRep.get("sourceFieldName").asText()).asText());
								this.log.info("replace download url :" + this.requestURL);
							}
						}
						
						if (configurations.has("headers")) {
							JsonNode headers = configurations.get("headers");
							if (configurations.has("dynamicTransactionID")) {
								String TranIDDoc = null;
								JsonNode TranIDconfigurations = null;
								if (configurations.has("dynamicTransactionID")) {
									TranIDDoc = "TransactionID-" + carrierId;
									this.log.info("TranIDDoc : " + TranIDDoc);
									TranIDconfigurations = RestAPIConstants.objectMapper.readTree(
											((JsonObject) RestAPIConstants.serverConfig.getDocBYId(TranIDDoc).content())
													.toString());

									((ObjectNode) headers).put(
											configurations.get("dynamicTransactionID").get("headerName").asText(),
											getTransactionID(TranIDDoc, TranIDconfigurations));

								}

							}
							if (configurations.has("dynamicHeaders")) {
								if (configurations.get("dynamicHeaders").get("headerName").asText()
										.equalsIgnoreCase("PRODUCT_CODE")) {
									this.log.info(
											"Putting Dynamic Headers node2 : " + inputRequest.get("request").findValue(
													configurations.get("dynamicHeaders").get("inputField").asText()));
									String pNameValuesString = configurations.get("dynamicHeaders").get("P_NAME")
											.asText();

									String[] pNameArray = pNameValuesString.split(",");

									boolean matchFound = false;
									for (String pName : pNameArray) {
										if (inputRequest.get("request")
												.get(configurations.get("dynamicHeaders").get("headerSearchNode")
														.asText())
												.findValue(
														configurations.get("dynamicHeaders").get("inputField").asText())
												.asText().equalsIgnoreCase(pName.trim())) {
											((ObjectNode) headers).put(
													configurations.get("dynamicHeaders").get("headerName").asText(),
													configurations.get("dynamicHeaders").get("P_CODE").asText());
											matchFound = true;
											break;
										}
									}

									if (!matchFound) {
										((ObjectNode) headers).put(
												configurations.get("dynamicHeaders").get("headerName").asText(),
												configurations.get("dynamicHeaders").get("DEFAULT_CODE").asText());
									}
								} else {
									this.log.info("Putting Dynamic Headers node2 : " + inputRequest.get("request").get(
											configurations.get("dynamicHeaders").get("headerSearchNode").asText()));
									((ObjectNode) headers).put(
											configurations.get("dynamicHeaders").get("headerName").asText(),
											inputRequest.get("request")
													.get(configurations.get("dynamicHeaders").get("headerSearchNode")
															.asText())
													.findValue(configurations.get("dynamicHeaders").get("headerName")
															.asText())
													.asText());
								}
							}
							
							
							
							
							
								if (configurations.has("multipleDynamicHeaders")) {									
								JsonNode multipleDynamicHeaderNode = configurations.get("multipleDynamicHeaders");

							    if (multipleDynamicHeaderNode != null && multipleDynamicHeaderNode.isArray()) {
							        ArrayNode multipleDynamicHeaders = (ArrayNode) multipleDynamicHeaderNode;
							        
							        for (JsonNode headerConfig : multipleDynamicHeaders) {
							            String headerName = headerConfig.get("headerName").asText();
							            String headerSearchNode = headerConfig.get("headerSearchNode").asText();

							             this.log.info("Inside Multiple Dynamic Headers For Node : " + inputRequest.get("request").get(headerSearchNode));
							             
							                String headerValue = inputRequest.get("request").get(headerSearchNode).findValue(headerName).asText();
							                
							                ((ObjectNode) headers).put(headerName, headerValue);   
							        }
							    }
							}
							
							objectNode1 = (ObjectNode)headers;
							
						} 
						else {
							objectNode1 = RestAPIConstants.objectMapper.createObjectNode();
							objectNode1.put("Content-Type", "application/json");
							objectNode1.put("CamelHttpMethod", "POST");
							objectNode1.put("CamelAcceptContentType", "application/json");
						}
						
						if(configurations.has(RestAPIConstants.GENERATE_JWT_TOKEN) && configurations.get(RestAPIConstants.GENERATE_JWT_TOKEN).has(RestAPIConstants.SECRET_KEY))
						{
							String createFreshJWTToken = CreateJWTToken.createFreshJWTToken(configurations.get(RestAPIConstants.GENERATE_JWT_TOKEN).get(RestAPIConstants.SECRET_KEY).asText());
						    objectNode1.put(configurations.get(RestAPIConstants.GENERATE_JWT_TOKEN).get(RestAPIConstants.AUTH_KEY).asText(),createFreshJWTToken);
						}
						
						if(configurations.has(RestAPIConstants.GENERATE_UUID) && configurations.get(RestAPIConstants.GENERATE_UUID).has(RestAPIConstants.KEY))
						{
						    objectNode1.put(configurations.get(RestAPIConstants.GENERATE_UUID).get(RestAPIConstants.KEY).asText(),UUID.randomUUID().toString());

						}
						
						if(configurations.has(RestAPIConstants.ENCRYPT_PAYLOAD) && configurations.get(RestAPIConstants.ENCRYPT_PAYLOAD).has(RestAPIConstants.SECRET_KEY))
						{
							   String encrypt = EncrypDecryptOperation.encrypt(configurations.get(RestAPIConstants.ENCRYPT_PAYLOAD).get(RestAPIConstants.SECRET_KEY).asText(),joltResNode.get(configurations.get(RestAPIConstants.ENCRYPT_PAYLOAD).get(RestAPIConstants.REQUEST_KEY).asText()).toString(),configurations.get(RestAPIConstants.ENCRYPT_PAYLOAD).get(RestAPIConstants.IV).asText());
                               ((ObjectNode) joltResNode).put(configurations.get(RestAPIConstants.ENCRYPT_PAYLOAD).get(RestAPIConstants.REQUEST_KEY).asText(), encrypt);
						}
						
					
						ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
						object.put("carrierData", joltResNode);
						object.put("subStage", "Request");
						object.put("inputRequest", inputRequest);
						object.put("url", this.requestURL);
						object.put("headers", (JsonNode) objectNode1);
						exchange.getIn().setBody(object.toString());
					}
					exchange.getIn().setHeader("configDocumentFound", "True");
				} else {
					this.log.error("Configuration Document not found for docId :" + docId);
					exchange.getIn().setHeader("configDocumentFound", "False");
					exchange.getIn()
							.setBody(ResponseMessageProcessor.returnConfigDocResponse(
									"Configuration Document Not Found, DocId : RESTRequest-" + stage + "-" + lob + "-"
											+ carrierId + "-" + productId + "-" + policyType,
									carrierId));
				}
			} else {
				this.log.error("InputRequest property is not set");
				exchange.getIn().setHeader("configDocumentFound", "False");
				exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
			}
		} catch (Exception e) {
			this.log.error("Exception in RESTRequestFormatter", e);
			exchange.getIn().setHeader("configDocumentFound", "False");
			exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
		}
	}
	
	private String getTransactionID(String TranIDDoc, JsonNode TranIDconfigurations) {
	    ObjectMapper objectMapper = new ObjectMapper();
	    String updatedTrnsNo = null;
	    try {
	      HashMap<Object, Object> parameters = (HashMap<Object, Object>)objectMapper.convertValue(TranIDconfigurations, HashMap.class);
	      String trnsNo = (String)parameters.get("trnsNo");
	      updatedTrnsNo = updateTrnsNo(trnsNo);
	      parameters.put("trnsNo", updatedTrnsNo);
	      String doc_updated = RestAPIConstants.serverConfig.updateDocument(TranIDDoc, parameters);
	      this.log.info("doc_updated :" + doc_updated);
	    } catch (Exception e) {
	      e.printStackTrace();
	      this.log.error("Exception updateTransactionIDDoc in RESTRequestFormatter", e);
	    } 
	    this.log.info("updatedTrnsNo:" + updatedTrnsNo);
	    return updatedTrnsNo;
	  }
	  
	  private String updateTrnsNo(String trnsNo) {
	    String prefix = trnsNo.substring(0, 4);
	    String yy = trnsNo.substring(4, 6);
	    String mm = trnsNo.substring(6, 8);
	    String dd = trnsNo.substring(8, 10);
	    String incrementalNo = trnsNo.substring(10);
	    int incremental = Integer.parseInt(incrementalNo) + 1;
	    String updatedIncrementalNo = String.format("%05d", new Object[] { Integer.valueOf(incremental) });
	    LocalDate currentDate = LocalDate.now();
	    String currentYY = String.valueOf(currentDate.getYear()).substring(2);
	    String currentMM = String.format("%02d", new Object[] { Integer.valueOf(currentDate.getMonthValue()) });
	    String currentDD = String.format("%02d", new Object[] { Integer.valueOf(currentDate.getDayOfMonth()) });
	    String updatedTrnsNo = prefix + currentYY + currentMM + currentDD + updatedIncrementalNo;
	    return updatedTrnsNo;
	  }
	
}
