package com.idep.policy.req.processor;

import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


import com.idep.api.ISoapConnector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class NIAApprovalPROPOSALFORMATTER implements Processor{
		 Logger log = Logger.getLogger(NIAApprovalPROPOSALFORMATTER.class.getName());
		  ObjectMapper objectMapper = new ObjectMapper();
		  SoapConnector  soapService = new SoapConnector();
		  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
		@Override
		public void process(Exchange exchange) throws Exception {

			try {
			  String methodName	= null;
			  String methodParam = null;
			  String request  = exchange.getIn().getBody(String.class);
			  exchange.getIn().removeHeader("CamelHttpPath");
			  exchange.getIn().removeHeader("CamelHttpUri");
			  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
			  String response = "";
			 
			  JsonDocument postProposalConfigDoc = serverConfig.getDocBYId("PostHealthProposalRequest-25");
			  JsonNode configData1 =objectMapper.readTree(postProposalConfigDoc.content().toString());
			  log.info("niaapprovalproposal CONFIG "+configData1);
			 
			  if(postProposalConfigDoc!=null){
			  JsonNode configData =objectMapper.readTree(postProposalConfigDoc.content().toString());
			  /**
			   * Carrier Quote configuration information
			   */
			 
			  if (configData.has("carrierSOAPConfig"))
			  {
				  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodName"))
				  {
					  methodName = configData.get("carrierSOAPConfig").get("reqConfig").get("methodName").asText();
					  log.debug("methodName : "+methodName);
				  }
				  
				  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodParam"))
				  {
					  methodParam = configData.get("carrierSOAPConfig").get("reqConfig").get("methodParam").asText();
					  log.debug("methodParam : "+methodParam);
					  
				  }
				   
				  if(configData.get("carrierSOAPConfig").get("reqConfig").has("schemaLocMap"))
				  {
					  JsonNode schemaLocMap = configData.get("carrierSOAPConfig").get("reqConfig").get("schemaLocMap");
					  
					  @SuppressWarnings("unchecked")
					  
					  Map<String,String> schemaMap = objectMapper.readValue(schemaLocMap.toString(), Map.class);
					  
					  log.debug("map from soap config : "+schemaMap);
					   //response  = soapService.prepareSoapRequest(methodName, methodParam, request, schemaMap);
						//String response = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sym=\"http://syminterface.insurance.symbiosys.c2lbiz.com\" xmlns:xsd=\"http://io.syminterface.insurance.symbiosys.c2lbiz.com/xsd\" xmlns:xsd1=\"http://transferobjects.syminterface.insurance.symbiosys.c2lbiz.com/xsd\">    <soap:Header/>    <soap:Body>       <sym:compute>          <!--Optional:-->          <sym:WSQuotationListIO>       <!--Zero or more repetitions:-->     <listofquotationTO>                <addOnName/>                <agentId>1601491-01</agentId>                <campaignCd>Brkonlin01</campaignCd>                <caseType/>                <channelId>350</channelId>                <inwardSubTypeCd>PROPOSALDOCUMENT</inwardSubTypeCd>                <inwardTypeCd>NEWBUSINESS</inwardTypeCd>                <noOfAdults>1</noOfAdults>                <noOfKids>0</noOfKids>                <parentProductId>NULL</parentProductId>                <parentProductVersion>1</parentProductVersion>                <planID/>                <planName/>                <policyNum/>                <policyServicingBranch/>                <policyType>INDIVIDUAL</policyType>                <ppmcFl/>                <productFamilyCd>HEALTH</productFamilyCd>                <productId>RACC01SB01</productId>                <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                <quotationChangeDOList>                   <alterationType/>                </quotationChangeDOList>                <quotationChargeDOList>                   <chargeClassCd/>                </quotationChargeDOList>                <quotationDt>17/11/2016</quotationDt>                <quotationProductDOList>                   <paymentFrequencyCd/>                   <payoutOption/>                   <productId>RACC01SB01</productId>                   <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                   <productTypeCd>SUBPLAN</productTypeCd>                   <quotationProductAddOnDOList>                      <productId/>                      <productPlanOptionCd/>                      <sumInsured/>                   </quotationProductAddOnDOList>                   <quotationProductBenefitDOList>                      <amount/>                      <benefitId/>                      <benefitTypeCd/>                      <productId/>                   </quotationProductBenefitDOList>                   <quotationProductChargeDOList>                      <chargeAmount>0</chargeAmount>                      <chargeClassCd/>                      <chargePercentage>0</chargePercentage>                   </quotationProductChargeDOList>                   <quotationProductInsuredDOList>                      <chewTobaccoCd>NO</chewTobaccoCd>                      <cityCd>Mumbai</cityCd>                      <consumeAlcoholCd>NO</consumeAlcoholCd>                      <customerId/>                      <dob>05/11/1997</dob>                      <emailAddress/>                      <genderCd>FEMALE</genderCd>                      <insuredTypeCd>PRIMARY</insuredTypeCd>                      <issueAge>0</issueAge>                      <mobileNum/>                      <ppmcFl/>                      <ppmcSetName/>                      <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                      <quotationProductInsuredBenefitDOList>                         <amount>0</amount>                         <benefitId/>                         <benefitTypeCd/>                         <productId/>                      </quotationProductInsuredBenefitDOList>                      <refGuid/>                      <relationCd>SELF</relationCd>                      <smokerStatusCd>NO</smokerStatusCd>                      <uwFl/>                      <zoneCd>ZONE1</zoneCd>                   </quotationProductInsuredDOList>                   <reducingBalanceSI/>                   <zoneCd>ZONE1</zoneCd>                </quotationProductDOList>                <quoteId/>                <quoteTypeCd>PORTAL</quoteTypeCd>                <riderName/>                <saveFl>YES</saveFl>                <tenure>1</tenure>                <totPremium>0</totPremium>                <uwFl/>             </listofquotationTO>          </sym:WSQuotationListIO>       </sym:compute>    </soap:Body> </soap:Envelope>";
					 
							    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
							    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
							    
							    Document document = dBuilder.parse(new InputSource(new StringReader(request)));
							    Set<Map.Entry<String, String>> tnsEntrySet = schemaMap.entrySet();
							    Iterator<Map.Entry<String, String>> tnsItr = tnsEntrySet.iterator();
							    while (tnsItr.hasNext())
							    {
							      Map.Entry<String, String> tnsEntry = (Map.Entry)tnsItr.next();
							      String key = ((String)tnsEntry.getKey()).trim();
							      if (!key.equalsIgnoreCase("parentTns"))
							      {
							        Element element = (Element)document.getElementsByTagName(key).item(0);
							        element.setAttribute("xmlns", ((String)tnsEntry.getValue()).trim());
							      }
							    }
							    TransformerFactory transformerFactory = TransformerFactory.newInstance();
							    Transformer transformer = transformerFactory.newTransformer();
							    DOMSource source = new DOMSource(document);
							    StringWriter outWriter = new StringWriter();
							    StreamResult result = new StreamResult(outWriter);
							    transformer.transform(source, result);
							    StringBuffer sb = outWriter.getBuffer();
							    
							    String finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
							    finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
							    Map<String, Object> paramMap = new HashMap();
							    paramMap.put(methodParam, finalstring);
							    response =  soapService. prepareSoapRequest(methodName, ((String)schemaMap.get("parentTns")).toString().trim(), paramMap);
							    response = StringEscapeUtils.unescapeXml(response);
							    
							    log.info("response"+response);
							    
					  exchange.getIn().setHeader("soapaction", "");
					  
				  }
				  if(configData.get("carrierSOAPConfig").get("reqConfig").has("removeAttrList")){
					  JsonNode removeAttrList = configData.get("carrierSOAPConfig").get("reqConfig").get("removeAttrList");
				   Iterator<String> keyList = removeAttrList.fieldNames();
				   while(keyList.hasNext()){
					   String key = keyList.next();
					   response= response.replaceAll(key, removeAttrList.get(key).asText());
					   log.info("NIA Values Replaced : Key : "+key);
				   }
				   log.info("Final Resposne : "+response);
				  } 
			  }
			  /*Map<String, String> schemaMap =  new HashMap<String,String>();
			  schemaMap.put("parentTns", "http://syminterface.insurance.symbiosys.c2lbiz.com");
			 */
			 
			  exchange.getIn().setBody(response);
			  }else{
				  
				  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NIaSOAPRequestFormatter|Unable to read DOC PostHealthProposal-25");  
			  }
			}
			
			
			catch(Exception e)
			{
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NIaSOAPRequestFormatter|",e);
				throw new ExecutionTerminator();
			}
			  
		}



}

	
	
	
	
	

