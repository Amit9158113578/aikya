
package com.idep.policydoc.res.processor;

import java.io.IOException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.policydoc.util.PolicyDocViewConstants;

/**
* @author  Sandeep Jadhav
* @version 1.0
* @since   25-OCT-2016
* prepare Data Source required by jasper to created PDF file
*/
public class PolicyDocViewerResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PolicyDocViewerResProcessor.class.getName());
	JsonNode errorNode=null;
	CBService service =  null;
	JsonNode responseConfigNode = null;
	JsonNode riderDisLabelConfigNode = null;
	JsonNode policyNodeMappingConfig =  null;
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try {
			
			if(this.service==null)
		    {
		    	this.service =  CBInstanceProvider.getServerConfigInstance();
		    	this.policyNodeMappingConfig = this.objectMapper.readTree(this.service.getDocBYId("PolicyDocResponseGroupConfig").content().toString());
		    	this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(PolicyDocViewConstants.RESPONSE_MSG).content().toString());
		    	this.riderDisLabelConfigNode = this.objectMapper.readTree(this.service.getDocBYId("RiderDiscountDisplayLabelConfig").content().toString());
		    }
			
			String policydocData = exchange.getIn().getBody(String.class);
			JsonNode policydocDataNode = this.objectMapper.readTree(policydocData);
			
			/**
			 *  default rows
			 */
			int odTableRows = 3;
			int liabilityTableRows = 3;
			
			/**
			 *  calculate total premium
			 */
			long totalPremium = policydocDataNode.get("totalPremium").asLong();
			/**
			 *  calculate service tax, swachh bharat cess and krishi kalyan cess
			 */
			((ObjectNode)policydocDataNode).put("serviceTaxAmt", policydocDataNode.get("serviceTax").doubleValue());
		//	((ObjectNode)policydocDataNode).put("swachhBharatCessAmt", (Math.round(totalPremium*(policydocDataNode.get("swachhBharatCess").doubleValue()))/100.0));
			//((ObjectNode)policydocDataNode).put("krishiKalyanCessAmt", (Math.round(totalPremium*(policydocDataNode.get("krishiKalyanCess").doubleValue()))/100.0));
			
			/**
			 *  calculate vehicle total value
			 */
			long totalValueVehicle = 0;
			totalValueVehicle = totalValueVehicle + policydocDataNode.get("idv").longValue();
			
			if(policydocDataNode.hasNonNull("nonEleAccessoriesSI"))
			{
				totalValueVehicle = totalValueVehicle+policydocDataNode.get("nonEleAccessoriesSI").longValue();
			}
			else
			{
				((ObjectNode)policydocDataNode).put("nonEleAccessoriesSI", 0);
			}
			if(policydocDataNode.hasNonNull("eleAccessoriesSI"))
			{
				totalValueVehicle = totalValueVehicle+policydocDataNode.get("eleAccessoriesSI").longValue();
			}
			else
			{
				((ObjectNode)policydocDataNode).put("eleAccessoriesSI", 0);
			}
			
			if(policydocDataNode.hasNonNull("cngLpgKitSI"))
			{
				totalValueVehicle = totalValueVehicle+policydocDataNode.get("cngLpgKitSI").longValue();
			}
			else
			{
				((ObjectNode)policydocDataNode).put("cngLpgKitSI", 0);
			}
			
			((ObjectNode)policydocDataNode).put("totalValueVehicle", totalValueVehicle);
			
			/**
			 *  calculate total Deductibles
			 */
			if(policydocDataNode.hasNonNull("voluntaryDeductibleSI"))
			{
				((ObjectNode)policydocDataNode).put("totalDeductibles", (policydocDataNode.get("voluntaryDeductibleSI").asInt()+policydocDataNode.get("compulsoryDeductibles").asInt()));
			}
			else
			{
				((ObjectNode)policydocDataNode).put("totalDeductibles", (policydocDataNode.get("compulsoryDeductibles").asInt()));
			}
			/**
			 *  group all selected riders
			 */
			log.info("policydocDataNode :"+policydocDataNode);
			JsonNode ridersList = policydocDataNode.get("ridersList");
			ArrayNode odRiderNodeArr = null;
			ArrayNode liabilityRiderNodeArr = null;
			double totalOdRidersValue = 0;
			double totalLiabilityRidersValue = 0;
			if(ridersList!=null)
			{
				odRiderNodeArr = this.objectMapper.createArrayNode();
				liabilityRiderNodeArr = this.objectMapper.createArrayNode();
				totalOdRidersValue = 0;
				totalLiabilityRidersValue = 0;
				JsonNode legalLibRiderArr = riderDisLabelConfigNode.get("legalLiabilityRiders");
				JsonNode ridersLabel = this.riderDisLabelConfigNode.get("ridersLabel");
				for(JsonNode riders : ridersList)
				{
				  if(!riders.get("riderType").asText().equalsIgnoreCase("NA"))
				  {	
					if(riders.has("dependant"))
					{
						for(JsonNode depRiders : riders.get("dependant"))
						{
							if(depRiders.hasNonNull("riderValue"))
							{
								JsonNode rLabel = ridersLabel.get(depRiders.get("riderId").asText());
								String label = rLabel.get("label").textValue();
								if(rLabel.has("placeholder"))
								{
									if(rLabel.get("type").textValue().equals("Long"))
									{
										label =	label.replaceAll("%"+rLabel.get("placeholder").textValue()+"%", ((Long)policydocDataNode.get(rLabel.get("placeholder").textValue()).longValue()).toString());
									}
									else if(rLabel.get("type").textValue().equals("String"))
									{
										label =	label.replaceAll("%"+rLabel.get("placeholder").textValue()+"%", policydocDataNode.get(rLabel.get("placeholder").textValue()).textValue());
									}
								}
								if(legalLibRiderArr.has(depRiders.get("riderId").asText()))
								{
									
									ObjectNode liabilityRiderNode = this.objectMapper.createObjectNode();
									liabilityRiderNode.put("name",label);
									liabilityRiderNode.put("value", String.format("%.2f", depRiders.get("riderValue").doubleValue()));
									liabilityRiderNodeArr.add(liabilityRiderNode);
									
									totalLiabilityRidersValue = totalLiabilityRidersValue+depRiders.get("riderValue").doubleValue();
									
									
									if(depRiders.get("riderId").intValue()==20)
									{
										ObjectNode llDriverNode = this.objectMapper.createObjectNode();
										llDriverNode.put("name", "Legal Liability to Paid Driver");
										llDriverNode.put("value", String.format("%.2f", policydocDataNode.get("llDriverCover").doubleValue()));
										liabilityRiderNodeArr.add(llDriverNode);
										totalLiabilityRidersValue = totalLiabilityRidersValue+policydocDataNode.get("llDriverCover").doubleValue();
									}
								}
								else
								{
									ObjectNode odRiderNode = this.objectMapper.createObjectNode();
									odRiderNode.put("name",label);
									odRiderNode.put("value", String.format("%.2f", depRiders.get("riderValue").doubleValue()));
									odRiderNodeArr.add(odRiderNode);
									
									
									if(depRiders.get("riderId").intValue()==35)
									{
										ObjectNode lpgCNGKitNode = this.objectMapper.createObjectNode();
										lpgCNGKitNode.put("name", "LPG/CNG Kit");
										lpgCNGKitNode.put("value", String.format("%.2f", policydocDataNode.get("liabilityForBiFuel").doubleValue()));
										liabilityRiderNodeArr.add(lpgCNGKitNode);
										totalLiabilityRidersValue = totalLiabilityRidersValue+policydocDataNode.get("liabilityForBiFuel").doubleValue();
									}
									
									
									totalOdRidersValue = totalOdRidersValue+depRiders.get("riderValue").doubleValue();
								}
								
							}
						}
						/* this is for only zero dep because of zero dep is dependent rider
						e.g 24*7 rodeside and consumable is working zero dep not added under premium.*/
						
							ObjectNode odRiderNode = this.objectMapper.createObjectNode();
							JsonNode rLabel = ridersLabel.get(riders.get("riderId").asText());
							String label = rLabel.get("label").textValue();
							odRiderNode.put("name",label);
							odRiderNode.put("value", String.format("%.2f", riders.get("riderValue").doubleValue()));
							odRiderNodeArr.add(odRiderNode);
							totalOdRidersValue = totalOdRidersValue+riders.get("riderValue").doubleValue();
					    }
					else
					{
						if(riders.hasNonNull("riderValue"))
						{
							JsonNode rLabel = ridersLabel.get(((Integer)riders.get("riderId").intValue()).toString());
							if(rLabel!=null)
							{
								String label = rLabel.get("label").textValue();
								if(rLabel.has("placeholder"))
								{
									if(rLabel.get("type").textValue().equals("Long"))
									{
										label =	label.replaceAll("%"+rLabel.get("placeholder").textValue()+"%", ((Long)policydocDataNode.get(rLabel.get("placeholder").textValue()).longValue()).toString());
									}
									else if(rLabel.get("type").textValue().equals("String"))
									{
										label =	label.replaceAll("%"+rLabel.get("placeholder").textValue()+"%", policydocDataNode.get(rLabel.get("placeholder").textValue()).textValue());
									}
								}
								// below condition added to categorize riders as odriders and liability riders
								if(legalLibRiderArr.has(((Integer)riders.get("riderId").intValue()).toString()))
								{
									ObjectNode liabilityRiderNode = this.objectMapper.createObjectNode();
									liabilityRiderNode.put("name",label);
									liabilityRiderNode.put("value", String.format("%.2f", riders.get("riderValue").doubleValue()));
									liabilityRiderNodeArr.add(liabilityRiderNode);
									
									if(riders.get("riderId").intValue()==20)
									{
										ObjectNode llDriverNode = this.objectMapper.createObjectNode();
										llDriverNode.put("name", "Legal Liability to Paid Driver");
										llDriverNode.put("value", String.format("%.2f", policydocDataNode.get("llDriverCover").doubleValue()));
										liabilityRiderNodeArr.add(llDriverNode);
										totalLiabilityRidersValue = totalLiabilityRidersValue+policydocDataNode.get("llDriverCover").doubleValue();
									}
									totalLiabilityRidersValue = totalLiabilityRidersValue+riders.get("riderValue").doubleValue();
									
								}
								else
								{
									ObjectNode odRiderNode = this.objectMapper.createObjectNode();
									odRiderNode.put("name",label);
									odRiderNode.put("value", String.format("%.2f", riders.get("riderValue").doubleValue()));
									odRiderNodeArr.add(odRiderNode);
									
									if(riders.get("riderId").intValue()==35)
									{
										ObjectNode lpgCNGKitNode = this.objectMapper.createObjectNode();
										lpgCNGKitNode.put("name", "LPG/CNG Kit");
										lpgCNGKitNode.put("value", String.format("%.2f", policydocDataNode.get("liabilityForBiFuel").doubleValue()));
										liabilityRiderNodeArr.add(lpgCNGKitNode);
										totalLiabilityRidersValue = totalLiabilityRidersValue+policydocDataNode.get("liabilityForBiFuel").doubleValue();
									}
									
									totalOdRidersValue = totalOdRidersValue+riders.get("riderValue").doubleValue();
								}
							
							}
						}
					}
				  }
				}
				
				 ObjectNode ridersObjNode = this.objectMapper.createObjectNode();
				 ridersObjNode.put("totalOdRidersValue", totalOdRidersValue);
				 ridersObjNode.put("totalLiabilityRidersValue", totalLiabilityRidersValue);
				 ridersObjNode.put("odRiders", odRiderNodeArr);
				 ridersObjNode.put("liabilityRiders", liabilityRiderNodeArr);
				((ObjectNode)policydocDataNode).put("ridersList",ridersObjNode);
			}
			
			// group discount
			JsonNode discountList = policydocDataNode.get("discountList");
			ArrayNode discountObjArr = null; 
			if(discountList!=null)
			{
				discountObjArr = this.objectMapper.createArrayNode();
				double totalDiscountValue = 0;
				JsonNode discountLabel = this.riderDisLabelConfigNode.get("discountsLabel");
				for(JsonNode discountNode : discountList)
				{
					
					String discountId = discountNode.get("discountId").asText();
					
					JsonNode disLabel = discountLabel.get(discountId);
					StringBuffer label = new StringBuffer();
					label.append(disLabel.get("label").textValue());
					if(disLabel.has("placeholder"))
					{
						if(disLabel.get("type").textValue().equals("String"))
						{
							label.append(" ").append(policydocDataNode.get(disLabel.get("placeholder").textValue()).textValue());
							label.append(disLabel.get("concatChar").textValue());
						}
						else if(disLabel.get("type").textValue().equals("Long"))
						{
							label.append(" ").append(policydocDataNode.get(disLabel.get("placeholder").textValue()).longValue());
						}
						else if(disLabel.get("type").textValue().equals("Double"))
						{
							label.append(" ").append(policydocDataNode.get(disLabel.get("placeholder").textValue()).doubleValue());
						}
						else
						{
							label.append(" ").append(policydocDataNode.get(disLabel.get("placeholder").textValue()));
						}
					}
					
					// to display on policy PDF
					ObjectNode discountDataNode  = objectMapper.createObjectNode();
					discountDataNode.put("name", label.toString());
					discountDataNode.put("value", String.format("%.2f", discountNode.get("discountAmount").doubleValue()));
					discountObjArr.add(discountDataNode);
					
					totalDiscountValue = totalDiscountValue+discountNode.get("discountAmount").doubleValue();
				}
				
				ObjectNode discountObjNode = this.objectMapper.createObjectNode();
				discountObjNode.put("totalDiscountValue", totalDiscountValue);
				discountObjNode.put("discounts", discountObjArr);
				((ObjectNode)policydocDataNode).put("discountList",discountObjNode);
			}
			
			/**
			 *  calculate total package amount
			 */
			calcuateTotalPkgAmt(policydocDataNode);
			
			/**
			 * calculate blank rows
			 * this function is added to make sure table looks good on signed PDF file by creating empty rows
			 */
			JsonNode blankRowsNode = calcBlankRows(odTableRows,liabilityTableRows,discountObjArr,odRiderNodeArr,liabilityRiderNodeArr);
			/**
			 * prepare Own Damage premium table data set
			 */
			calcpremiumODTableDataSet(policydocDataNode,discountObjArr,odRiderNodeArr,liabilityRiderNodeArr,blankRowsNode.get("odTableRows").asInt(),blankRowsNode.get("liabilityTableRows").asInt());
			/**
			 * prepare Liability premium table data set
			 */
			calcpremiumLiabilityTableDataSet(policydocDataNode,liabilityRiderNodeArr,discountObjArr,odRiderNodeArr,blankRowsNode.get("odTableRows").asInt(),blankRowsNode.get("liabilityTableRows").asInt());
			/**
			 * calculate all Taxes applicable
			 */
			calcpremiumTaxTableData(policydocDataNode);
			
			// send formatted response
			ObjectNode policyObj = this.objectMapper.createObjectNode();
			JsonNode policyConfigNode = policyNodeMappingConfig.get("nodeConfig");
			for(JsonNode policyNode : policyConfigNode)
			{
				
				ObjectNode policyFieldsNode = this.objectMapper.createObjectNode();
				ArrayNode fieldsArray = (ArrayNode)policyNode.get("nodeValues");
				for(JsonNode node : fieldsArray)
				{
					
					policyFieldsNode.put(node.get("nodeKey").textValue(), policydocDataNode.get(node.get("nodeKey").textValue()));
					
				}
				
				policyObj.put(policyNode.get("nodeName").textValue(), policyFieldsNode);
			}
			
			log.info("Data Source Preparation completed : "+policyObj);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_CODE, this.responseConfigNode.get(PolicyDocViewConstants.SUCC_CONFIG_CODE).intValue());
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_MSG, this.responseConfigNode.get(PolicyDocViewConstants.SUCC_CONFIG_MSG).textValue());
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_DATA, policyObj);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCVIEWRESHANDL+"|ERROR|"+"Exception at PolicyDocViewerResProcessor:",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_CODE, this.responseConfigNode.get(PolicyDocViewConstants.ERROR_CONFIG_CODE).intValue());
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_MSG, this.responseConfigNode.get(PolicyDocViewConstants.ERROR_CONFIG_MSG).textValue());
			obj.put(PolicyDocViewConstants.PROPOSAL_RES_DATA, errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
		}
		 
	}
	
	
	public void calcuateTotalPkgAmt(JsonNode policydocDataNode)
	{
		double tatakPkgAmt = 0.0;
		double totalLiabilityPremium = 0.0;
		double totalOwnDamagePremium = 0.0;
		
		tatakPkgAmt = tatakPkgAmt+ policydocDataNode.get("basicOwnDamageAmt").asDouble()+
					  policydocDataNode.get("basicTPPDPremium").asDouble();
		totalLiabilityPremium = totalLiabilityPremium + policydocDataNode.get("basicTPPDPremium").asDouble();
		totalOwnDamagePremium = totalOwnDamagePremium + policydocDataNode.get("basicOwnDamageAmt").asDouble();
		
		if(policydocDataNode.has("PACoverAmt"))
		{
			tatakPkgAmt =  tatakPkgAmt + policydocDataNode.get("PACoverAmt").asDouble();
			totalLiabilityPremium = totalLiabilityPremium + policydocDataNode.get("PACoverAmt").asDouble();
		}
		
		if(policydocDataNode.has("ridersList"))
		{
			
			if(policydocDataNode.get("ridersList").has("totalOdRidersValue"))
			{
				tatakPkgAmt =  tatakPkgAmt + policydocDataNode.get("ridersList").get("totalOdRidersValue").asDouble();
				totalOwnDamagePremium = totalOwnDamagePremium + policydocDataNode.get("ridersList").get("totalOdRidersValue").asDouble();
			}
			if(policydocDataNode.get("ridersList").has("totalLiabilityRidersValue"))
			{
				tatakPkgAmt =  tatakPkgAmt + policydocDataNode.get("ridersList").get("totalLiabilityRidersValue").asDouble();
				totalLiabilityPremium = totalLiabilityPremium + policydocDataNode.get("ridersList").get("totalLiabilityRidersValue").asDouble();
			}
		}
		if(policydocDataNode.has("discountList"))
		{
			tatakPkgAmt =  tatakPkgAmt - policydocDataNode.get("discountList").get("totalDiscountValue").asDouble();
			totalOwnDamagePremium =  totalOwnDamagePremium - policydocDataNode.get("discountList").get("totalDiscountValue").asDouble();
		}
		
		((ObjectNode)policydocDataNode).put("totalPackage",Math.round(tatakPkgAmt*100.0)/100.0);// to restrict to 2 digits
		((ObjectNode)policydocDataNode).put("totalLiabilityPremium",Math.round(totalLiabilityPremium*100.0)/100.0);
		((ObjectNode)policydocDataNode).put("totalOwnDamagePremium",Math.round(totalOwnDamagePremium*100.0)/100.0);
	
	}
	
	public ObjectNode calcBlankRows(int odTableRows,int liabilityTableRows,ArrayNode discountObjArr,ArrayNode odRiderNodeArr,ArrayNode liabilityRiderNodeArr)
	{
		ObjectNode blankRowsNode = objectMapper.createObjectNode();
		if(discountObjArr!=null)
		{
			odTableRows = odTableRows + discountObjArr.size();
		}
		if(odRiderNodeArr!=null)
		{
			odTableRows = odTableRows + odRiderNodeArr.size();
		}
		if(liabilityRiderNodeArr!=null)
		{
			liabilityTableRows = liabilityTableRows + liabilityRiderNodeArr.size();
		}
		
		blankRowsNode.put("odTableRows", odTableRows);
		blankRowsNode.put("liabilityTableRows", liabilityTableRows);
		
		return blankRowsNode;
	}
	public void calcpremiumODTableDataSet(JsonNode policydocDataNode,ArrayNode discountObjArr,ArrayNode odRiderNodeArr,ArrayNode liabilityRiderNodeArr,int odTableRows,int liabilityTableRows)
	{
		ArrayNode premiumODTableDataSet = objectMapper.createArrayNode();
		ObjectNode odTableNode = objectMapper.createObjectNode();
		odTableNode.put("name","Own Damage");
		odTableNode.put("value", "");
		premiumODTableDataSet.add(odTableNode);
		
		ObjectNode basicOwnDamageNode = objectMapper.createObjectNode();
		basicOwnDamageNode.put("name","Basic Own Damage");
		basicOwnDamageNode.put("value", String.format("%.2f", policydocDataNode.get("basicOwnDamageAmt").asDouble()));
		premiumODTableDataSet.add(basicOwnDamageNode);
		
		
		// add od riders
		if(odRiderNodeArr!=null)
		{
			premiumODTableDataSet.addAll(odRiderNodeArr);
		}
		
		
		
		
		if(discountObjArr!=null)
		{
			// add discount details
			ObjectNode discountNode = objectMapper.createObjectNode();
			discountNode.put("name","Less :");
			discountNode.put("value", "");
			premiumODTableDataSet.add(discountNode);
			
			// add discount array list
			premiumODTableDataSet.addAll(discountObjArr);
			
		}
		
		// this is added to make OD and liability table rows equal
		if(liabilityTableRows>odTableRows)
		{
			for(int i=1;i<=(liabilityTableRows-odTableRows);i++)
			{
				ObjectNode blankNode = objectMapper.createObjectNode();
				blankNode.put("name","");
				blankNode.put("value", "");
				premiumODTableDataSet.add(blankNode);
			}
		}
		ObjectNode totalODPremiumNode = objectMapper.createObjectNode();
		totalODPremiumNode.put("name","Total Own Damage Premium (A)");
		totalODPremiumNode.put("value",  String.format("%.2f", policydocDataNode.get("totalOwnDamagePremium").asDouble()));
		premiumODTableDataSet.add(totalODPremiumNode);
		
		
		((ObjectNode)policydocDataNode).put("premiumODTableData",premiumODTableDataSet);
		
	}
	
	public void calcpremiumLiabilityTableDataSet(JsonNode policydocDataNode,ArrayNode liabilityRiderNodeArr,ArrayNode discountObjArr,ArrayNode odRiderNodeArr,int odTableRows,int liabilityTableRows)
	{
		
		ArrayNode premiumLiabTableDataSet = objectMapper.createArrayNode();
		
		ObjectNode liabilityTableNode = objectMapper.createObjectNode();
		liabilityTableNode.put("name","Liability");
		liabilityTableNode.put("value", "");
		premiumLiabTableDataSet.add(liabilityTableNode);
		
		
		ObjectNode basicTPPremiumNode = objectMapper.createObjectNode();
		basicTPPremiumNode.put("name","Basic TP Including TPPD Premium");
		basicTPPremiumNode.put("value", String.format("%.2f", policydocDataNode.get("basicTPPDPremium").asDouble()));
		premiumLiabTableDataSet.add(basicTPPremiumNode);
		
		if(policydocDataNode.has("PACoverAmt"))
		{
			ObjectNode paCoverOwnerDriverNode = objectMapper.createObjectNode();
			paCoverOwnerDriverNode.put("name","PA Cover for Owner Driver of Rs.1500000");
			paCoverOwnerDriverNode.put("value", String.format("%.2f", policydocDataNode.get("PACoverAmt").asDouble()));
			premiumLiabTableDataSet.add(paCoverOwnerDriverNode);
		}
	
		
		
		if(liabilityRiderNodeArr!=null)
		{
			premiumLiabTableDataSet.addAll(liabilityRiderNodeArr);
		}
		
		
		// this is added to make OD and liability table rows equal
		if(odTableRows>liabilityTableRows)
		{
			for(int i=1;i<=(odTableRows-liabilityTableRows);i++)
			{
				ObjectNode blankNode = objectMapper.createObjectNode();
				blankNode.put("name","");
				blankNode.put("value", "");
				premiumLiabTableDataSet.add(blankNode);
			}
		}
		
		ObjectNode totalLiabilityPremiumNode = objectMapper.createObjectNode();
		totalLiabilityPremiumNode.put("name","Total Liability Premium (B)");
		totalLiabilityPremiumNode.put("value",  String.format("%.2f", policydocDataNode.get("totalLiabilityPremium").asDouble()));
		premiumLiabTableDataSet.add(totalLiabilityPremiumNode);
		
		((ObjectNode)policydocDataNode).put("premiumLiabilityTableData",premiumLiabTableDataSet);
	}
	
	public void calcpremiumTaxTableData(JsonNode policydocDataNode) throws JsonProcessingException, IOException
	{
		ArrayNode premiumTaxTableDataSet = objectMapper.createArrayNode();
		
		//totalPackage
		ObjectNode totalPkgNode = objectMapper.createObjectNode();
		totalPkgNode.put("name","Taxable Value of Service (A+B)");
		totalPkgNode.put("value", String.format("%.2f", policydocDataNode.get("totalPackage").asDouble()));
		premiumTaxTableDataSet.add(totalPkgNode);
		
		double serviceTax = policydocDataNode.get("serviceTaxAmt").asDouble();
		log.info("policydocDataNode"+policydocDataNode);
		log.info("service tax amount :"+serviceTax);

		 String gstDocumentId=PolicyDocViewConstants.GSTCONF+policydocDataNode.get(PolicyDocViewConstants.CARRIER_ID).asText();
		 JsonNode gstConfigDoc = objectMapper.readTree(service.getDocBYId(gstDocumentId).content().toString());
		 
		 double sgst = serviceTax/2;
	 	 double cgst = serviceTax-sgst;
	 	 
	 	 ObjectNode cgsNode = objectMapper.createObjectNode();
	 	 ObjectNode gstNode = objectMapper.createObjectNode();
	 	 
		 if(gstConfigDoc!=null)
		 {
			if(gstConfigDoc.has(policydocDataNode.get("receiverState").textValue()))
			{
				if(gstConfigDoc.get(policydocDataNode.get("receiverState").textValue()).asText().equalsIgnoreCase("CGST"))
				{
					
					//CSGT
					//SGST
					
					 cgsNode.put("name", "CGST @ 9%");
				 	 cgsNode.put("value", cgst);
				 	premiumTaxTableDataSet.add(cgsNode);
				 	  
				     gstNode.put("name", "SGST @ 9%");
				 	 gstNode.put("value", sgst);
				 	 premiumTaxTableDataSet.add(gstNode);
				}
				else
				{
					//CSGT
					//UGST
					 cgsNode.put("name", "CGST @ 9%");
				 	 cgsNode.put("value", cgst);
				 	premiumTaxTableDataSet.add(cgsNode);
				 	 
					 gstNode.put("name", "UGST @ 9%");
					 gstNode.put("value", sgst);
					 premiumTaxTableDataSet.add(gstNode);
					 
				}
			}
			else
			{
				//IGST
				 gstNode.put("name", "IGST @ 18%");
				 gstNode.put("value", serviceTax);
				 premiumTaxTableDataSet.add(gstNode);
			}
		 }
		
		ObjectNode totalPremiumNode = objectMapper.createObjectNode();
		totalPremiumNode.put("name","Total Premium (in Rs)");
		totalPremiumNode.put("value", String.format("%.2f", policydocDataNode.get("totalPremium").asDouble()));
		premiumTaxTableDataSet.add(totalPremiumNode);
		
		
		((ObjectNode)policydocDataNode).put("premiumTaxTableData",premiumTaxTableDataSet);
	}

}
