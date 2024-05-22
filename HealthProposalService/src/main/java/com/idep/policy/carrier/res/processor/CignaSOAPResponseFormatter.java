package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CignaSOAPResponseFormatter  implements Processor {
	
	Logger log = Logger.getLogger(CignaSOAPResponseFormatter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		SoapConnector  soapService = new SoapConnector();
		try
		{
			String healthResponse  = exchange.getIn().getBody(String.class);
			String startTagName = null;
			String endTagName = null;
			String apppendTagName  = null;
			String removeAttrList[] = null;
			log.debug("generatePolicyNumberResponse : "+healthResponse);
			
			//String s = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns:computeResponse xmlns:ns=\"http://syminterface.insurance.symbiosys.c2lbiz.com\"><ns:return xmlns:ax242=\"http://io.syminterface.insurance.symbiosys.c2lbiz.com/xsd\" xmlns:ax239=\"http://text.java/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ax243=\"http://transferobjects.syminterface.insurance.symbiosys.c2lbiz.com/xsd\" xsi:type=\"ax242:WSQuotationDataIO\"><ax242:errorList xsi:type=\"ax243:WSIntExceptionTO\"><ax243:errActualMessage>?</ax243:errActualMessage><ax243:errDescription>?</ax243:errDescription><ax243:errField>?</ax243:errField><ax243:errProcessStatusCd>?</ax243:errProcessStatusCd><ax243:errRecord>?</ax243:errRecord><ax243:errSourceCd>?</ax243:errSourceCd><ax243:exceptionCatgCd>?</ax243:exceptionCatgCd><ax243:exceptionCd>?</ax243:exceptionCd></ax242:errorList><ax242:listofquotationTO xsi:type=\"ax243:WSQuotationTO\"><ax243:actualCummulativeBonus xsi:nil=\"true\" /><ax243:agentId>1601491-01</ax243:agentId><ax243:baseModalPremium xsi:nil=\"true\" /><ax243:campaignCd>Brkonlin01</ax243:campaignCd><ax243:caseType>STP</ax243:caseType><ax243:channelId>350</ax243:channelId><ax243:cummulativeBonusAmt xsi:nil=\"true\" /><ax243:cummulativeBonusPerc xsi:nil=\"true\" /><ax243:initialPremium>0.0</ax243:initialPremium><ax243:inwardSubTypeCd>PROPOSALDOCUMENT</ax243:inwardSubTypeCd><ax243:inwardTypeCd>NEWBUSINESS</ax243:inwardTypeCd><ax243:modalEduCess xsi:nil=\"true\" /><ax243:modalHigherEduCess xsi:nil=\"true\" /><ax243:modalPremium>0.0</ax243:modalPremium><ax243:modalServiceTax xsi:nil=\"true\" /><ax243:noOfAdults>1</ax243:noOfAdults><ax243:noOfKids>0</ax243:noOfKids><ax243:parentProductId>NULL</ax243:parentProductId><ax243:parentProductVersion>1</ax243:parentProductVersion><ax243:policyNum></ax243:policyNum><ax243:policyType>INDIVIDUAL</ax243:policyType><ax243:ppmcFl>NO</ax243:ppmcFl><ax243:premiumSuspenseAmount xsi:nil=\"true\" /><ax243:premiumToBeCollected xsi:nil=\"true\" /><ax243:prevCummulativeBonus xsi:nil=\"true\" /><ax243:productFamilyCd>HEALTH</ax243:productFamilyCd><ax243:productId>RACC01SB01</ax243:productId><ax243:productPlanOptionCd>IN-PLS5.5-HMB2K</ax243:productPlanOptionCd><ax243:quotationChangeDOList xsi:type=\"ax243:WSQuotationChangeTO\"><ax243:alterationType></ax243:alterationType></ax243:quotationChangeDOList><ax243:quotationChargeDOList xsi:type=\"ax243:WSQuotationChargeTO\"><ax243:chargeAmount>0.0</ax243:chargeAmount><ax243:chargeClassCd>SERVICETAX</ax243:chargeClassCd><ax243:chargePercentage xsi:nil=\"true\" /></ax243:quotationChargeDOList><ax243:quotationChargeDOList xsi:type=\"ax243:WSQuotationChargeTO\"><ax243:chargeAmount>0.0</ax243:chargeAmount><ax243:chargeClassCd>EDUCESS</ax243:chargeClassCd><ax243:chargePercentage xsi:nil=\"true\" /></ax243:quotationChargeDOList><ax243:quotationChargeDOList xsi:type=\"ax243:WSQuotationChargeTO\"><ax243:chargeAmount>0.0</ax243:chargeAmount><ax243:chargeClassCd>HIGHEREDUCESS</ax243:chargeClassCd><ax243:chargePercentage xsi:nil=\"true\" /></ax243:quotationChargeDOList><ax243:quotationDt>17/11/2016</ax243:quotationDt><ax243:quotationProductDOList xsi:type=\"ax243:WSQuotationProductTO\"><ax243:basePremium>0.0</ax243:basePremium><ax243:discount>0.0</ax243:discount><ax243:extraPremium>0.0</ax243:extraPremium><ax243:paymentFrequencyCd></ax243:paymentFrequencyCd><ax243:payoutOption></ax243:payoutOption><ax243:productId>RACC01SB01</ax243:productId><ax243:productPlanOptionCd>IN-PLS5.5-HMB2K</ax243:productPlanOptionCd><ax243:productTypeCd>SUBPLAN</ax243:productTypeCd><ax243:productVersion xsi:nil=\"true\" /><ax243:quotationProductBenefitDOList xsi:type=\"ax243:WSQuotationProductBenefitTO\"><ax243:amount>0.0</ax243:amount><ax243:benefitId>RACCONLINE</ax243:benefitId><ax243:benefitTypeCd>DISCOUNT</ax243:benefitTypeCd><ax243:productId>RACC01SB01</ax243:productId></ax243:quotationProductBenefitDOList><ax243:quotationProductChargeDOList xsi:type=\"ax243:WSQuotationProductChargeTO\"><ax243:chargeAmount>0.0</ax243:chargeAmount><ax243:chargeClassCd></ax243:chargeClassCd><ax243:chargePercentage>0.0</ax243:chargePercentage></ax243:quotationProductChargeDOList><ax243:quotationProductInsuredDOList xsi:type=\"ax243:WSQuotationProductInsuredTO\"><ax243:actualCummulativeBonus xsi:nil=\"true\" /><ax243:bmi xsi:nil=\"true\" /><ax243:chewTobaccoCd>NO</ax243:chewTobaccoCd><ax243:cityCd>Mumbai</ax243:cityCd><ax243:consumeAlcoholCd>NO</ax243:consumeAlcoholCd><ax243:cummulativeBonusAmt xsi:nil=\"true\" /><ax243:cummulativeBonusPerc xsi:nil=\"true\" /><ax243:customerId></ax243:customerId><ax243:discount>0.0</ax243:discount><ax243:dob>05/11/1997</ax243:dob><ax243:emailAddress></ax243:emailAddress><ax243:extraPremium xsi:nil=\"true\" /><ax243:genderCd>FEMALE</ax243:genderCd><ax243:height xsi:nil=\"true\" /><ax243:insuredTypeCd>PRIMARY</ax243:insuredTypeCd><ax243:issueAge>19</ax243:issueAge><ax243:mobileNum xsi:nil=\"true\" /><ax243:modalPremium>0.0</ax243:modalPremium><ax243:ppmcFl>NO</ax243:ppmcFl><ax243:ppmcSetName xsi:nil=\"true\" /><ax243:prevCummulativeBonus xsi:nil=\"true\" /><ax243:productPlanOptionCd>IN-PLS5.5-HMB2K</ax243:productPlanOptionCd><ax243:refGuid></ax243:refGuid><ax243:relationCd>SELF</ax243:relationCd><ax243:smokerStatusCd>NO</ax243:smokerStatusCd><ax243:sumInsured xsi:nil=\"true\" /><ax243:uwFl>NO</ax243:uwFl><ax243:uwLoadingAmount xsi:nil=\"true\" /><ax243:uwLoadingPerc xsi:nil=\"true\" /><ax243:weight xsi:nil=\"true\" /><ax243:zoneCd>ZONE1</ax243:zoneCd></ax243:quotationProductInsuredDOList><ax243:reducingBalanceSI></ax243:reducingBalanceSI><ax243:sumInsured xsi:nil=\"true\" /><ax243:zoneCd>ZONE1</ax243:zoneCd></ax243:quotationProductDOList><ax243:quoteId>S107206127</ax243:quoteId><ax243:quoteTypeCd>PORTAL</ax243:quoteTypeCd><ax243:renewalYear xsi:nil=\"true\" /><ax243:saveFl>YES</ax243:saveFl><ax243:tenure>1</ax243:tenure><ax243:totPremium>0.0</ax243:totPremium><ax243:totRenewalLoadingAmount xsi:nil=\"true\" /><ax243:totRenewalLoadingInclOfTaxes xsi:nil=\"true\" /><ax243:totUWLoadingAmount xsi:nil=\"true\" /><ax243:totUWLoadingInclOfTaxes xsi:nil=\"true\" /><ax243:uwFl>NO</ax243:uwFl><ax243:uwLoadingPerc xsi:nil=\"true\" /></ax242:listofquotationTO></ns:return></ns:computeResponse></soapenv:Body></soapenv:Envelope>";
			//String soapResponse = soapService.retriveSoapResult(quoteResponse, "computeResponse");
			//String soapResponse = soapService.getSoapResult(s, "ax242:listofquotationTO");
					
			JsonNode resConfigData = exchange.getProperty("cignaSoapReqConfDocument",JsonNode.class);
			 if (resConfigData.has("carrierSOAPConfig"))
			  {
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("startTagName"))
				  {
					  startTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("startTagName").asText();
					  log.debug("startTagName : "+startTagName);
				  }
				  
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("endTagName"))
				  {
					  endTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("endTagName").asText();
					  log.debug("endTagName : "+endTagName);
				  }
				  
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("apppendTagName"))
				  {
					  apppendTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("apppendTagName").asText();
					  log.debug("apppendTagName : "+apppendTagName);
				  }
				  
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("removeAttrList"))
				  {
					  JsonNode attrList = resConfigData.get("carrierSOAPConfig").get("resConfig").get("removeAttrList");
					  removeAttrList = objectMapper.readValue(attrList.toString(), String[].class);
				  }
			  }
			 
			String soapResponse = soapService.getSoapResult(healthResponse, startTagName, endTagName, removeAttrList, apppendTagName);
			log.debug("soapResponse : "+soapResponse);
			exchange.getIn().setBody(soapResponse);
			
			
		}
		
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaSOAPResponseFormatter|",e);
			throw new ExecutionTerminator();
		}
	}

}

