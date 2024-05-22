package com.idep.restapi.request;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.log4j.Logger;

public class ConfigurationLoaderProcessor implements Processor {
	String docId = null;

	Logger log = Logger.getLogger(ConfigurationLoaderProcessor.class.getName());

	public void process(Exchange exchange) {
		int carrierId = 0;
		try {
			int productId;
			String request = (String) exchange.getIn().getBody(String.class);
			JsonNode reqNode = RestAPIConstants.objectMapper.readTree(request);
			JsonNode loadedDoc = null;
			this.log.info("reqNode :"+reqNode);
			if (exchange.getProperty("inputRequest") == null) {
				this.log.info("inputRequest property is null :"+reqNode);
				int lob = reqNode.findValue("lob").asInt();
				carrierId = reqNode.findValue("carrierId").asInt();
				if (carrierId == 28) {
					if(reqNode.get("request").has("vehicleInfo")) {
			          this.log.info("PreviousPolicyExpiryDate : " + reqNode.findValue("PreviousPolicyExpiryDate").asText());
			          this.log.info("planType : " + reqNode.findValue("planType").asText());
			          this.log.info("policyType : " + reqNode.findValue("policyType").asText());
			          if (!reqNode.findValue("policyType").asText().equals("new")) {
			            String dateString = reqNode.findValue("PreviousPolicyExpiryDate").asText();
			            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			            LocalDate previousPolicyExpiryDate = LocalDate.parse(dateString, formatter);
			            LocalDate currentDate = LocalDate.now();
			            if (previousPolicyExpiryDate.isEqual(currentDate)) {
			              this.log.info("Previous Policy Expiry Date is the current date :" + currentDate);
			            } else if (!reqNode.findValue("planType").asText().equals("TP")) {
			              ((ObjectNode)reqNode.get("request").get("vehicleInfo")).put("PreviousPolicyExpiryDate", "");
			            } 
			          } 
			          this.log.info("PreviousPolicyExpiryDate : " + reqNode.findValue("PreviousPolicyExpiryDate").asText());
			        } 
				}
				if (lob == 4) {
					productId = reqNode.findValue("planId").asInt();
				} else {
					productId = reqNode.findValue("productId").asInt();
				}
				String stage = reqNode.findValue("stage").asText();
				JsonNode policyType = reqNode.findValue("policyType");
				if (policyType != null) {
					this.docId = "JOLTRequest-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-"
							+ policyType.asText();
					log.info("document id :"+docId);
					loadedDoc = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(this.docId).content()).toString());
				} else {
					this.docId = "JOLTRequest-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
					log.info("document id :"+docId);
					loadedDoc = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(this.docId).content()).toString());
				}
				if (loadedDoc != null) {
					ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
					object.put("inputRequest", reqNode.get("request"));
					object.put("configuration", loadedDoc.get("configuration"));
					exchange.setProperty("inputRequest", reqNode);
					exchange.getIn().setHeader("documentFound", "True");
					exchange.getIn().setBody(object);
				} else {
					this.log.error("Configuration Document Not Found for docId :" + this.docId);
					exchange.getIn().setHeader("documentFound", "False");
					exchange.getIn()
							.setBody(ResponseMessageProcessor.returnConfigDocResponse(
									"Configuration Document Not Found, DocId : JOLTRequest-" + stage + "-" + lob + "-"
											+ carrierId + "-" + productId + "-" + policyType,
									carrierId));
				}
			} else {
				JsonNode inputRequest = RestAPIConstants.objectMapper
						.readTree(exchange.getProperty("inputRequest").toString());
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
					this.docId = "JOLTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-"
							+ policyType.asText().replace("/", "");
					loadedDoc = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(this.docId).content()).toString());
				} else {
					this.docId = "JOLTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
					loadedDoc = RestAPIConstants.objectMapper.readTree(
							((JsonObject) RestAPIConstants.serverConfig.getDocBYId(this.docId).content()).toString());
				}
				if (loadedDoc != null) {
					ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
					((ObjectNode) inputRequest).put("carrierResponse", reqNode);
					object.put("inputRequest", inputRequest);
					object.put("configuration", loadedDoc.get("configuration"));
					exchange.getIn().setHeader("documentFound", "True");
					exchange.getIn().setBody(object);
				} else {
					this.log.error("Configuration Document Not Found for docId :" + this.docId);
					exchange.getIn().setHeader("documentFound", "False");
					exchange.getIn()
							.setBody(
									ResponseMessageProcessor.returnConfigDocResponse(
											"Configuration Document Not Found, DocId : JOLTResponse-" + stage + "-"
													+ lob + "-" + carrierId + "-" + productId + "-" + policyType,
											carrierId));
				}
			}
		} catch (NullPointerException e) {
			this.log.info(e);
			this.log.error("Exception at ConfigurationLoaderProcessor : " + e);
			exchange.getIn().setHeader("documentFound", "False");
			exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
		} catch (Exception e) {
			this.log.info(e);
			this.log.error("Exception at ConfigurationLoaderProcessor : " + e);
			exchange.getIn().setHeader("documentFound", "False");
			exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
		}
	}
}
