package com.idep.healthquote.test;

import java.util.HashMap;
import java.util.Map;

import com.idep.api.impl.SoapConnector;

public class SoapResponseTest {

	public void process() throws Exception {

		try {
		  String request = "<compute><WSQuotationListIO><listofquotationTO><addOnName/><agentId>1601491-01</agentId><campaignCd>Brkonlin01</campaignCd><caseType/><channelId>350</channelId><inwardSubTypeCd>PROPOSALDOCUMENT</inwardSubTypeCd><inwardTypeCd>NEWBUSINESS</inwardTypeCd><noOfAdults>1</noOfAdults><noOfKids>0</noOfKids><parentProductId>NULL</parentProductId><parentProductVersion>1</parentProductVersion><planID/><planName/><policyNum/><policyServicingBranch/><policyType>INDIVIDUAL</policyType><ppmcFl/><productFamilyCd>HEALTH</productFamilyCd><productId>RPLS01SB01</productId><productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd><quotationChangeDOList><alterationType/></quotationChangeDOList><quotationChargeDOList><chargeClassCd/></quotationChargeDOList><quotationDt>17/11/2016</quotationDt><quotationProductDOList><paymentFrequencyCd/><payoutOption/><productId>RPLS01SB01</productId><productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd><productTypeCd>SUBPLAN</productTypeCd><quotationProductAddOnDOList><productId/><productPlanOptionCd/><sumInsured/></quotationProductAddOnDOList><quotationProductBenefitDOList><amount/><benefitId/><benefitTypeCd/><productId/></quotationProductBenefitDOList><quotationProductChargeDOList><chargeAmount>0</chargeAmount><chargeClassCd/><chargePercentage>0</chargePercentage></quotationProductChargeDOList><quotationProductInsuredDOList><chewTobaccoCd>NO</chewTobaccoCd><cityCd>PUNE</cityCd><consumeAlcoholCd>NO</consumeAlcoholCd><customerId/><dob>05/11/1997</dob><emailAddress/><genderCd>FEMALE</genderCd><insuredTypeCd>PRIMARY</insuredTypeCd><issueAge>0</issueAge><mobileNum/><ppmcFl/><ppmcSetName/><productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd><quotationProductInsuredBenefitDOList><amount>0</amount><benefitId/><benefitTypeCd/><productId/></quotationProductInsuredBenefitDOList><refGuid/><relationCd>SELF</relationCd><smokerStatusCd>NO</smokerStatusCd><uwFl/><zoneCd>ZONE1</zoneCd></quotationProductInsuredDOList><reducingBalanceSI/><zoneCd>ZONE1</zoneCd></quotationProductDOList><quoteId/><quoteTypeCd>PORTAL</quoteTypeCd><riderName/><saveFl>YES</saveFl><tenure>1</tenure><totPremium>0</totPremium><uwFl/></listofquotationTO></WSQuotationListIO></compute>";
		  //exchange.getIn().removeHeader("CamelHttpPath");
		  //exchange.getIn().removeHeader("CamelHttpUri");
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		  System.out.println("modified XML request : "+request);
	      Map<String, String> tnsMap =  new HashMap<String,String>();
		  tnsMap.put("parentTns", "http://syminterface.insurance.symbiosys.c2lbiz.com");
		  SoapConnector  soapService = new SoapConnector();
		  //String response  = soapService.prepareSoapRequest("compute", "http://syminterface.insurance.symbiosys.c2lbiz.com", tnsMap);
		  String response  = soapService.prepareSoapRequest("compute", "WSQuotationListIO", request, tnsMap);
		  System.out.println("final SoapResponse : "+response);
		  //response without errorlist
		  //String response = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sym=\"http://syminterface.insurance.symbiosys.c2lbiz.com\" xmlns:xsd=\"http://io.syminterface.insurance.symbiosys.c2lbiz.com/xsd\" xmlns:xsd1=\"http://transferobjects.syminterface.insurance.symbiosys.c2lbiz.com/xsd\">    <soap:Header/>    <soap:Body>       <sym:compute>          <!--Optional:-->          <sym:WSQuotationListIO>       <!--Zero or more repetitions:-->     <listofquotationTO>                <addOnName/>                <agentId>1601491-01</agentId>                <campaignCd>Brkonlin01</campaignCd>                <caseType/>                <channelId>350</channelId>                <inwardSubTypeCd>PROPOSALDOCUMENT</inwardSubTypeCd>                <inwardTypeCd>NEWBUSINESS</inwardTypeCd>                <noOfAdults>1</noOfAdults>                <noOfKids>0</noOfKids>                <parentProductId>NULL</parentProductId>                <parentProductVersion>1</parentProductVersion>                <planID/>                <planName/>                <policyNum/>                <policyServicingBranch/>                <policyType>INDIVIDUAL</policyType>                <ppmcFl/>                <productFamilyCd>HEALTH</productFamilyCd>                <productId>RACC01SB01</productId>                <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                <quotationChangeDOList>                   <alterationType/>                </quotationChangeDOList>                <quotationChargeDOList>                   <chargeClassCd/>                </quotationChargeDOList>                <quotationDt>17/11/2016</quotationDt>                <quotationProductDOList>                   <paymentFrequencyCd/>                   <payoutOption/>                   <productId>RACC01SB01</productId>                   <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                   <productTypeCd>SUBPLAN</productTypeCd>                   <quotationProductAddOnDOList>                      <productId/>                      <productPlanOptionCd/>                      <sumInsured/>                   </quotationProductAddOnDOList>                   <quotationProductBenefitDOList>                      <amount/>                      <benefitId/>                      <benefitTypeCd/>                      <productId/>                   </quotationProductBenefitDOList>                   <quotationProductChargeDOList>                      <chargeAmount>0</chargeAmount>                      <chargeClassCd/>                      <chargePercentage>0</chargePercentage>                   </quotationProductChargeDOList>                   <quotationProductInsuredDOList>                      <chewTobaccoCd>NO</chewTobaccoCd>                      <cityCd>Mumbai</cityCd>                      <consumeAlcoholCd>NO</consumeAlcoholCd>                      <customerId/>                      <dob>05/11/1997</dob>                      <emailAddress/>                      <genderCd>FEMALE</genderCd>                      <insuredTypeCd>PRIMARY</insuredTypeCd>                      <issueAge>0</issueAge>                      <mobileNum/>                      <ppmcFl/>                      <ppmcSetName/>                      <productPlanOptionCd>IN-PLS5.5-HMB2K</productPlanOptionCd>                      <quotationProductInsuredBenefitDOList>                         <amount>0</amount>                         <benefitId/>                         <benefitTypeCd/>                         <productId/>                      </quotationProductInsuredBenefitDOList>                      <refGuid/>                      <relationCd>SELF</relationCd>                      <smokerStatusCd>NO</smokerStatusCd>                      <uwFl/>                      <zoneCd>ZONE1</zoneCd>                   </quotationProductInsuredDOList>                   <reducingBalanceSI/>                   <zoneCd>ZONE1</zoneCd>                </quotationProductDOList>                <quoteId/>                <quoteTypeCd>PORTAL</quoteTypeCd>                <riderName/>                <saveFl>YES</saveFl>                <tenure>1</tenure>                <totPremium>0</totPremium>                <uwFl/>             </listofquotationTO>          </sym:WSQuotationListIO>       </sym:compute>    </soap:Body> </soap:Envelope>";	
		}
		catch(Exception e)
		{
			System.out.println("Exception at SOAPRequestFormatter : ");
		}
	}
		  
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SoapResponseTest soapTest = new SoapResponseTest();
		soapTest.process();
	}

}
