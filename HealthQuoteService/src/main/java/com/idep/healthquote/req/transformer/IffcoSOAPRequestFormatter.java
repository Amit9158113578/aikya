package com.idep.healthquote.req.transformer;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.healthquote.util.HealthQuoteConstants;

public class IffcoSOAPRequestFormatter implements Processor
{
	
	  Logger log = Logger.getLogger(IffcoSOAPRequestFormatter.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  SoapConnector  soapService = new SoapConnector();
	  
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
		  String methodName	= null;
		  String methodParam = null;
		  String request  = exchange.getIn().getBody(String.class);
		  exchange.getIn().removeHeader("CamelHttpPath");
		  exchange.getIn().removeHeader("CamelHttpUri");
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		  log.info("modified XML request : "+request);
		  String response = "";
		  
		  JsonNode configData = exchange.getProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,JsonNode.class);
		  log.info("configData : "+configData);
		  /**
		   * Carrier Quote configuration information
		   */
		  if (configData.has("carrierSOAPConfig"))
		  {
			  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodName"))
			  {
				  methodName = configData.get("carrierSOAPConfig").get("reqConfig").get("methodName").asText();
				  log.info("methodName : "+methodName);
			  }
			  
			  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodParam"))
			  {
				  methodParam = configData.get("carrierSOAPConfig").get("reqConfig").get("methodParam").asText();
				  log.info("methodParam : "+methodParam);
			  }
			   
			  if(configData.get("carrierSOAPConfig").get("reqConfig").has("schemaLocMap"))
			  {
				  JsonNode schemaLocMap = configData.get("carrierSOAPConfig").get("reqConfig").get("schemaLocMap");
				  log.info("schemaLocMap : "+schemaLocMap);
				  @SuppressWarnings("unchecked")
				  Map<String,String> schemaMap = objectMapper.readValue(schemaLocMap.toString(), Map.class);
				  
				  log.info("map from soap config : "+schemaMap);
				   response  = soapService.prepareSoapRequest(methodName, methodParam, request, schemaMap);
				   response = response.replaceAll("<ihpPolicy>", "")
					  .replaceAll("</ihpPolicy>", "")
					  .replaceAll("<fhpPolicy>", "")
					  .replaceAll("</fhpPolicy>", "")
					  .replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>","");
					//String response = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sym=\"http://syminterface.insurance.symbiosys.c2lbiz.com\" xmlns:xsd=\"http://io.syminterface.insurance.symbiosys.c2lbiz.com/xsd\" xmlns:xsd1=\"http://transferobjects.syminterface.insurance.symbiosys.c2lbiz.com/xsd\">    <soap:Header/>    <soap:Body>       <sym:compute>          <!--Optional:-->          <sym:WSQuotationListIO>       <!--Zero or more repetitions:-->     <listofquotationTO>                <addOnName/>                <agentId>1601491-01</agentId>                <campaignCd>Brkonlin01</campaignCd>                <caseType/>                <channelId>350</channelId>                <inwardSubTypeCd>PROPOSALDOCUMENT</inwardSubTypeCd>                <inwardTypeCd>NEWBUSINESS</inwardTypeCd>                <noOfAdults>1</noOfAdults>                <noOfKids>0</noOfKids>                <parentProductId>NULL</parentProductId>                <parentProductVersion>1</parentProductVersion>                <planID/>                <planName/>                <policyNum/>                <policyServicingBranch/>                <policyType>INDIVIDUAL</policyType>                <ppmcFl/>                <productFamilyCd>HEALTH</productFamilyCd>                <productId>RACC01SB01</productId>                <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                <quotationChangeDOList>                   <alterationType/>                </quotationChangeDOList>                <quotationChargeDOList>                   <chargeClassCd/>                </quotationChargeDOList>                <quotationDt>17/11/2016</quotationDt>                <quotationProductDOList>                   <paymentFrequencyCd/>                   <payoutOption/>                   <productId>RACC01SB01</productId>                   <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                   <productTypeCd>SUBPLAN</productTypeCd>                   <quotationProductAddOnDOList>                      <productId/>                      <productPlanOptionCd/>                      <sumInsured/>                   </quotationProductAddOnDOList>                   <quotationProductBenefitDOList>                      <amount/>                      <benefitId/>                      <benefitTypeCd/>                      <productId/>                   </quotationProductBenefitDOList>                   <quotationProductChargeDOList>                      <chargeAmount>0</chargeAmount>                      <chargeClassCd/>                      <chargePercentage>0</chargePercentage>                   </quotationProductChargeDOList>                   <quotationProductInsuredDOList>                      <chewTobaccoCd>NO</chewTobaccoCd>                      <cityCd>Mumbai</cityCd>                      <consumeAlcoholCd>NO</consumeAlcoholCd>                      <customerId/>                      <dob>05/11/1997</dob>                      <emailAddress/>                      <genderCd>FEMALE</genderCd>                      <insuredTypeCd>PRIMARY</insuredTypeCd>                      <issueAge>0</issueAge>                      <mobileNum/>                      <ppmcFl/>                      <ppmcSetName/>                      <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                      <quotationProductInsuredBenefitDOList>                         <amount>0</amount>                         <benefitId/>                         <benefitTypeCd/>                         <productId/>                      </quotationProductInsuredBenefitDOList>                      <refGuid/>                      <relationCd>SELF</relationCd>                      <smokerStatusCd>NO</smokerStatusCd>                      <uwFl/>                      <zoneCd>ZONE1</zoneCd>                   </quotationProductInsuredDOList>                   <reducingBalanceSI/>                   <zoneCd>ZONE1</zoneCd>                </quotationProductDOList>                <quoteId/>                <quoteTypeCd>PORTAL</quoteTypeCd>                <riderName/>                <saveFl>YES</saveFl>                <tenure>1</tenure>                <totPremium>0</totPremium>                <uwFl/>             </listofquotationTO>          </sym:WSQuotationListIO>       </sym:compute>    </soap:Body> </soap:Envelope>";
					  log.info("final SoapResponse : "+response); 
					  log.info("headers list : "+exchange.getIn().getHeaders());
				  exchange.getIn().setHeader("soapaction", "");
				  
			  }
		  }		 
		  exchange.getIn().setBody(response);	
		}
		catch(Exception e)
		{
			log.error("Exception at SOAPRequestFormatter : "+e);
		}
		  
	}

}