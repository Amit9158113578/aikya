package com.idep.policy.carrier.res.processor;

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

public class NIAApprovalProposalSOAPResFormatter implements Processor{

	Logger log = Logger.getLogger(NIAApprovalProposalSOAPResFormatter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		SoapConnector  soapService = new SoapConnector();
		try
		{
			String healthResponse  = exchange.getIn().getBody(String.class);
			String startTagName = null;
			String endTagName = null;
			String apppendTagName  = null;
			String removeAttrList[] = null;
			
			
			 JsonDocument postProposalConfigDoc = serverConfig.getDocBYId("PostHealthProposalRequest-25");
			 
			  if(postProposalConfigDoc!=null){
			  JsonNode resConfigData =objectMapper.readTree(postProposalConfigDoc.content().toString());
			  /**
			   * Carrier Quote configuration information
			   */
			  
			  log.info("niaapprovalRESOPANSEproposal CONFIG "+resConfigData);
			
			
					
			//JsonNode resConfigData = exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG,JsonNode.class);
			 if (resConfigData.has("carrierSOAPConfig"))
			  {
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("startTagName"))
				  {
					  startTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("startTagName").asText();
				
				  }
				  
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("endTagName"))
				  {
					  endTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("endTagName").asText();
				
				  }
				  
				  if(resConfigData.get("carrierSOAPConfig").get("resConfig").has("apppendTagName"))
				  {
					  apppendTagName = resConfigData.get("carrierSOAPConfig").get("resConfig").get("apppendTagName").asText();
					
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
			
	//		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><OrderNo>VDJD697929</OrderNo><QuoteNo>QRN201701280000054</QuoteNo><PremiumSet><Cover><Type>Basic</Type><Name>CarDamage</Name><Desc>Car Damage</Desc><Premium>717718</Premium><ExtraDetails><BreakUp><BasicOD>717718.16</BasicOD><Accessory>0</Accessory><NonElecAccessory>0</NonElecAccessory><BiFuel>0</BiFuel><ODDeductible>0</ODDeductible><NCB>0</NCB><AntiTheft>0</AntiTheft></BreakUp><Deductible>0</Deductible></ExtraDetails></Cover><Cover><Type>Basic</Type><Name>ThirdPartyLiability</Name><Desc>Third Party Liability</Desc><Premium>6214</Premium><ExtraDetails><BreakUp><TP>6164</TP><TPPD>0</TPPD><LLDriver>50</LLDriver><TPBiFuel>0</TPBiFuel></BreakUp></ExtraDetails></Cover><Cover><Type>Basic</Type><Name>PAOwnerDriver</Name><Desc>PA Owner Driver</Desc><Premium>100</Premium><ExtraDetails/></Cover><Cover><Type>Basic</Type><Name>PAFamily</Name><Desc>PA Family</Desc><Premium>0</Premium><ExtraDetails><PAFamilySI>0</PAFamilySI></ExtraDetails></Cover><ServiceTax>108604.8</ServiceTax><Discount>0</Discount><Cover><Type>Addon</Type><Name>AMBC</Name><Desc>AMBULANCE CHARGES COVER</Desc><Premium>100.0</Premium><SumInsured>5000</SumInsured><ExtraDetails><PLAN_ID>2</PLAN_ID><PLAN_CODE>S2</PLAN_CODE><RATEPER/><RATEFLAG/><FLATAMT>100.00</FLATAMT><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>CONC</Name><Desc>CONSUMABLES COVER</Desc><Premium>31295.850000000002</Premium><SumInsured/><ExtraDetails><PLAN_ID>11</PLAN_ID><PLAN_CODE>CN</PLAN_CODE><RATEPER>0.15</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>HOSP</Name><Desc>HOSPITAL CASH COVER</Desc><Premium>200.0</Premium><SumInsured>1000</SumInsured><ExtraDetails><PLAN_ID>12</PLAN_ID><PLAN_CODE>HA</PLAN_CODE><RATEPER/><RATEFLAG/><FLATAMT>100.00</FLATAMT><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>HYLC</Name><Desc>HYDROSTATIC LOCK COVER</Desc><Premium>35468.630000000005</Premium><SumInsured/><ExtraDetails><PLAN_ID>13</PLAN_ID><PLAN_CODE>CX</PLAN_CODE><RATEPER>0.17</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>INPC</Name><Desc>INVOICE PRICE COVER</Desc><Premium>41727.8</Premium><SumInsured/><ExtraDetails><PLAN_ID>33</PLAN_ID><PLAN_CODE>DI</PLAN_CODE><RATEPER>0.20</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>KEYC</Name><Desc>KEY REPLACEMENT COVER</Desc><Premium>33382.24</Premium><SumInsured/><ExtraDetails><PLAN_ID>35</PLAN_ID><PLAN_CODE>KD</PLAN_CODE><RATEPER>0.16</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>MEDI</Name><Desc>MEDICAL EXPENSES REIMBURSEMENT</Desc><Premium>150.0</Premium><SumInsured>10000</SumInsured><ExtraDetails><PLAN_ID>55</PLAN_ID><PLAN_CODE>MA</PLAN_CODE><RATEPER/><RATEFLAG/><FLATAMT>75.00</FLATAMT><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>NCBS</Name><Desc>NO CLAIM BONUS SAME SLAB</Desc><Premium>25036.679999999997</Premium><SumInsured/><ExtraDetails><PLAN_ID>56</PLAN_ID><PLAN_CODE>HY</PLAN_CODE><RATEPER>0.12</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>RSAC</Name><Desc>ROAD SIDE ASSISTANCE</Desc><Premium>375.0</Premium><SumInsured/><ExtraDetails><PLAN_ID>69</PLAN_ID><PLAN_CODE>R1</PLAN_CODE><RATEPER/><RATEFLAG/><FLATAMT>375.00</FLATAMT><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>EGBP</Name><Desc>ENGINE &amp; GEAR BOX PROTECTION</Desc><Premium>41727.8</Premium><SumInsured/><ExtraDetails><PLAN_ID>74</PLAN_ID><PLAN_CODE>DI</PLAN_CODE><RATEPER>0.20</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover><Cover><Type>Addon</Type><Name>DEPC</Name><Desc>DEPRECIATION COVER</Desc><Premium>58418.920000000006</Premium><SumInsured/><ExtraDetails><PLAN_ID>496</PLAN_ID><PLAN_CODE>GD</PLAN_CODE><RATEPER>0.28</RATEPER><RATEFLAG/><FLATAMT/><FLATFLAG/><TEXT3/></ExtraDetails></Cover></PremiumSet><SessionData><ID>148558132164317783248692</ID><Channel>gaadiMtr</Channel><UserAgentID>2C000024</UserAgentID><Source>2C000024</Source><AgentNumber>2C000024</AgentNumber><DealerId>616</DealerId></SessionData></response>";
			//exchange.getIn().setBody(s);
			
		}
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|NIAAPPROVALEDRESSOAPResponseFormatter|",e);
			throw new ExecutionTerminator();
		}
	}
	
	
	
}
