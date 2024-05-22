package com.idep.sugarcrm.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sugarcrm.util.SugarCRMConstants;

public class SugarCRMModuleServices {


	SugarCRMGatewayImpl sugarCRMService = SugarCRMGatewayImpl.getSugarCRMInstance();
	Logger log = Logger.getLogger(SugarCRMModuleServices.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * It return json data of selected fields
	 * @param query
	 * @param selected_fields
	 * @return
	 */
	public ObjectNode getLeadData(String query,String selected_fields) {
		ObjectNode leadData = objectMapper.createObjectNode();
		log.info("Selected Fields :"+selected_fields);
		try {
			leadData=sugarCRMService.getSelectedFieldData(SugarSession.sessionId,SugarCRMConstants.LEAD_MODULE,query,selected_fields);
		} catch (Exception e) {
			log.error("Error at getting lead data");
			leadData=null;
		}
		return leadData;
	}

	public ObjectNode getModuleData(String query,String moduleName,String selected_fields) {
		ObjectNode moduleData = objectMapper.createObjectNode();
		log.info("Selected Fields :"+selected_fields);
		try {
			moduleData=sugarCRMService.getSelectedFieldData(SugarSession.sessionId,moduleName,query,selected_fields);
		} catch (Exception e) {
			log.error("Error at getting module data");
			moduleData=null;
		}
		return moduleData;
	}

	/**
	 * create lead record and return lead id
	 * @param data
	 * @return leadId
	 */

	public String createLead(ObjectNode data)
	{

		//String sessionId = SugarSession.sessionId;//"";
		String leadId = "";	
		try
		{	
			leadId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.LEAD_MODULE, data);
			log.info("Lead Created Successfuly, Lead Id :"+leadId);

		}
		catch(Exception e)
		{
			leadId = "";
			log.error("Exception while creating lead record : ",e);
		}
		return leadId;
	}


	// For creating record in crm, like email sent record.
	public String createModuleRecord(ObjectNode data, String moduleName)
	{

		String recordId = "";	
		try
		{	
			recordId = sugarCRMService.createRecord(SugarSession.sessionId, moduleName, data);
			log.info(moduleName+"Record Created Successfuly, Id :"+recordId);

		}
		catch(Exception e)
		{
			recordId = "";
			log.error("Exception while creating record : ",e);
		}
		return recordId;
	}
	// Update record , Using for email update
	public String updateModuleRecord(ObjectNode data, String moduleName, String recordId)
	{
		String recordid = "";
		try
		{
			recordid = sugarCRMService.updateRecord(SugarSession.sessionId, moduleName, recordId ,data);
			log.info("Record Updated Successfuly,  Id :"+recordid);
		}
		catch(Exception e)
		{
			recordid = "";
			log.error("Exception while updating lead record : ",e);
		}

		return recordid;
	}

	public String findModuleRecord(String query, String moduleName ,String select_fields)
	{
		String recordId = "";
		try
		{
			recordId = sugarCRMService.getModuleData(SugarSession.sessionId, moduleName ,query,select_fields);
			log.info("Record Found With  ID: "+recordId);

		}
		catch(Exception e)
		{
			recordId = "";
			log.error("Exception while finding lead record : ",e);
		}

		return recordId;
	}

	public String createTickets(ObjectNode data)
	{	
		String ticketId = "";

		try
		{
			ticketId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.TICKET_MODULE, data);
			log.info("Ticket Created Successfuly, Ticket Id :"+ticketId);
		}
		catch(Exception e)
		{
			ticketId = "";
			log.error("Exception while creating Ticket record : ",e);
		}
		return ticketId;
	}

	public String createCustomer(ObjectNode data)
	{
		String customerId = "";

		try
		{

			customerId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_MODULE, data);
			log.info("customer Created Successfuly, customer Id :"+customerId);

		}
		catch(Exception e)
		{
			customerId = "";
			log.error("Exception while creating customer record : ",e);
		}

		return customerId;
	}

	public String createPolicy(ObjectNode data)
	{
		String policyId = "";

		try
		{

			policyId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.POLICY_MODULE, data);
			log.info("customer Created Successfuly, customer Id :"+policyId);

		}
		catch(Exception e)
		{
			policyId = "";
			log.error("Exception while creating customer record : ",e);
		}

		return policyId;
	}



	public String createCustomerInteraction(ObjectNode data)
	{
		String customerInteractionId = "";
		try
		{
			customerInteractionId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_INTERACTION_MODULE, data);
			log.info("Customer InteractionId Created Successfuly, Ticket Id :"+customerInteractionId);
		}
		catch(Exception e)
		{
			customerInteractionId = "";
			log.error("Exception while creating Customer Interaction record : ",e);
		}


		return customerInteractionId;
	}


	/**
	 * update lead record
	 * @param data
	 * @param recordId
	 * @return
	 */
	public String updateLead(ObjectNode data,String recordId)
	{
		log.info("WriteUpdateLead Record Id: "+recordId);
		String leadId = "";

		try
		{
			leadId = sugarCRMService.updateRecord(SugarSession.sessionId, SugarCRMConstants.LEAD_MODULE, recordId,data);
			log.info("lead Updated Successfuly, Lead Id :"+leadId);
		}
		catch(Exception e)
		{
			leadId = "";
			log.error("Exception while updating lead record : ",e);
		}

		return leadId;
	}

	public String updateTicket(ObjectNode data,String recordId)
	{

		String ticketId = "";

		try
		{
			ticketId = sugarCRMService.updateRecord(SugarSession.sessionId, SugarCRMConstants.TICKET_MODULE, recordId,data);
			log.info("Ticket Updated Successfuly, Ticket Id: "+ticketId);
		}
		catch(Exception e)
		{
			ticketId = "";
			log.error("Exception while updating ticket record : ",e);
		}

		return ticketId;
	}


	public String deleteLead(String leadId)
	{
		try
		{
			leadId = sugarCRMService.deleteRecord(SugarSession.sessionId, SugarCRMConstants.LEAD_MODULE, leadId);
			log.info("Lead Deleted Successfuly, Lead Id: "+leadId);

		}
		catch(Exception e)
		{
			leadId = "";
			log.error("Exception while deleting lead record : ",e);
		}
		return leadId;
	}


	public String findLead(String query, String select_fields)
	{
		String leadId = "";
		try
		{
			leadId = sugarCRMService.getModuleData(SugarSession.sessionId, SugarCRMConstants.LEAD_MODULE,query,select_fields);
			log.info("Lead Found With lead ID: "+leadId);

		}
		catch(Exception e)
		{
			leadId = "";
			log.error("Exception while finding lead record : ",e);
		}

		return leadId;
	}

	public String findCustomerInteraction(String query, String select_fields)
	{
		log.info("Now Finding Customer Interaction");
		String customerInteractionId = "";

		try
		{
			customerInteractionId = sugarCRMService.getModuleData(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_INTERACTION_MODULE,query,select_fields);
			log.info("Customer Interaction Id Found with, Customer InteractionId Id: "+customerInteractionId);
		}
		catch(Exception e)
		{
			customerInteractionId = "";
			log.error("Exception while finding Customer Interaction record : ",e);
		}	

		return customerInteractionId;
	}

	public String findCustomer(String query, String select_fields)
	{
		String customerId = "";

		try
		{
			customerId = sugarCRMService.getModuleData(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_MODULE,query,select_fields);
			log.info("Customer Found with, Customer  Id: "+customerId);
		}
		catch(Exception e)
		{
			customerId = "";
			log.error("Exception while finding Customer  record : ",e);
		}	

		return customerId;
	}

	public String createProspect(ObjectNode data)
	{
		String prospectId = "";

		try
		{

			prospectId = sugarCRMService.createRecord(SugarSession.sessionId, SugarCRMConstants.PROSPECT_MODULE, data);		
			log.info("Prospect Created Successfuly, Prospect Id: "+prospectId);
		}
		catch(Exception e)
		{
			prospectId = "";
			log.error("Exception while creating Prospect record : ",e);
		}

		return prospectId;
	}

	public String updateProspect(ObjectNode data,String recordId)
	{

		String prospectId = "";

		try
		{
			prospectId = sugarCRMService.updateRecord(SugarSession.sessionId,SugarCRMConstants.PROSPECT_MODULE,recordId,data);
			log.info("Prospect Updated Successfuly, Prospect Id: "+prospectId);
		}
		catch(Exception e)
		{
			prospectId = "";
			log.error("Exception while updating Prospect record : ",e);
		}

		return prospectId;
	}

	public String findProspect(String query, String select_fields)
	{

		String prospectId = "";

		try
		{

			prospectId = sugarCRMService.getModuleData(SugarSession.sessionId, SugarCRMConstants.PROSPECT_MODULE,query,select_fields);
			log.info("Prospect Found with ID: "+prospectId);
		}
		catch(Exception e)
		{
			prospectId = "";
			log.error("Exception while finding Prospect record : ",e);
		}
		return prospectId;
	}

	public String deleteProspect(String prospectId)
	{


		try
		{
			prospectId = sugarCRMService.deleteRecord(SugarSession.sessionId, SugarCRMConstants.PROSPECT_MODULE, prospectId);
			log.info("Prospect deleted Successfuly, Prospect Id: "+prospectId);

		}
		catch(Exception e)
		{
			prospectId = "";
			log.error("Exception while deleting prospect record : ",e);
		}

		return prospectId;
	}

	public String createLeadProspectRelation(String prospectId,String leadId)
	{
		String status = "";

		try
		{
			status = sugarCRMService.createRelation(SugarSession.sessionId, SugarCRMConstants.PROSPECT_MODULE, prospectId, SugarCRMConstants.PROSPECT_LEAD_REL, leadId, 0);
			log.info("Lead-Prospect Interaction Relationship :"+status);
			return status;
		}
		catch(Exception e)
		{
			log.error("Exception while creating Lead-Prospect relation : ",e);
			status = "";
		}
		return status;

	}

	public String createModuleRelation(String parentId, String childId, String parentModuleName, String relationshipName)
	{
		String status = "";

		try
		{
			status = sugarCRMService.createRelation(SugarSession.sessionId,parentModuleName , parentId, relationshipName, childId, 0);
			log.info(relationshipName+" Relationship Status:"+status);
			return status;
		}
		catch(Exception e)
		{
			log.error("Exception while "+relationshipName+" relation : ",e);
			status = "";
		}
		return status;

	}

	public String createCustomerPolicyRelation(String policyId,String customerId)
	{
		String status = "";
		try
		{
			status = sugarCRMService.createRelation(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_MODULE, policyId, SugarCRMConstants.CUST_POLICY_REL, customerId, 0);
			log.info("Customer-Policy Relationship :"+status);
			return status;
		}
		catch(Exception e)
		{
			log.error("Exception while creating Customer-Policy relation : ",e);
			status = "";
		}
		return status;

	}

	public String createTicketCustomerInteractionRelation(String customerInteractionId,String ticketId)
	{
		String status = "";

		try
		{
			status = sugarCRMService.createRelation(SugarSession.sessionId, SugarCRMConstants.CUSTOMER_INTERACTION_MODULE, customerInteractionId, SugarCRMConstants.CUSTOMERINTERACTION_TICKET_REL, ticketId, 0);
			log.info("Ticket-Customer Interaction Relationship :"+status);
			return status;
		}
		catch(Exception e)
		{
			log.error("Exception while creating Ticket-Customer relation : ",e);
			status = "";
		}

		return status;

	}

	public void writeUpdateLead(ObjectNode data,String recordId){
		BufferedWriter writer =null;
		Properties property = new Properties();
		SimpleDateFormat simple = new SimpleDateFormat("dd-MM-yyyy");
		try {
			InputStream inputStream = new FileInputStream(System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
			property.load(inputStream);	
			String fileName = "lms_updatelead_request_"+simple.format(new Date())+property.getProperty("nodeName")+".txt";
			File file = new File( property.getProperty("lms_leadfile_path")+fileName);


			if(!file.exists()){
				file.createNewFile();
				log.info("update lead file created  : "+fileName);	
			}

			FileWriter fw = new FileWriter(file,true);
			writer=new BufferedWriter(fw);
			writer.write("\n".concat(recordId+" "+data.toString()));
			log.info("update lead requtes added in file : "+fileName);
		}catch(Exception e){
			log.error("error at  Lead writting in file ",e);
		}finally{
			if(writer!=null){
				try{
					writer.close();
				}catch(Exception e){
					log.error("failed to close writer connection",e);		
				}
			}
		}



	}


}
