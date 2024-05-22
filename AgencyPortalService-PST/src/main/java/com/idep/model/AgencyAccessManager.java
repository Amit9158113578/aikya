package com.idep.model;

import java.sql.SQLException;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.idep.bean.*;
import com.idep.database.Database;
import com.idep.dbaccess.*;
import com.mysql.jdbc.Connection;

public class AgencyAccessManager {
	public String fetchAgencyList() throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String agencylist = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			agencylist = access.getAgencyList(con);	
			log.info("AgencyList in Accessmanager -- "+agencylist);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return agencylist;
	}
	
	public String addAgency(Agency agency, ArrayList<Branch> branchList, String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			status = access.addAgency(con,agency,branchList,sessionId);	
			log.info("addAgency status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	
	public String addBranch(JsonNode branchDetails, String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			status = access.addBranch(con,branchDetails,sessionId);	
			log.info("addBranch status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public String fetchAgency(String agencyId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String agencyDetails = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			agencyDetails = access.fetchAgency(con,agencyId);	
			log.info("fetchAgency status in Accessmanager -- "+agencyDetails);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return agencyDetails;
	}
	public String updateAgency(Agency agency, ArrayList<Branch> branchList,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			status = access.updateAgency(con,agency,branchList,sessionId);	
			log.info("updateAgency status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public String deleteAgency(String agencyId,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			status = access.deleteAgency(con,agencyId,sessionId);	
			log.info("deleteAgency status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public String deleteAgencyBranch(String branchId,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgencyAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgencyAccess access = new AgencyAccess();
			status = access.deleteAgencyBranch(con,branchId,sessionId);	
			log.info("deleteAgencyBranch status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{  
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
}
