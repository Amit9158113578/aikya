package com.idep.model;

import java.sql.SQLException;


import org.apache.log4j.Logger;

import com.idep.database.Database;
import com.idep.dbaccess.*;
import com.mysql.jdbc.Connection;

public class MiscAccessManager
{
	public String getRole(long role) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(MiscAccessManager.class.getName());
		String roleName = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			MiscAccess access = new MiscAccess();
			roleName = access.getRole(con,role);	
			log.info("roleName in Accessmanager -- "+roleName);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return roleName;
	}

	public String resetPass(String email,long mobile, String newPass) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(MiscAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			MiscAccess access = new MiscAccess();
			status = access.resetPass(con,email,mobile,newPass);	
			log.info("reset pass status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public String fetchManagerTeamleadList(String agencyId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(MiscAccessManager.class.getName());
		String managerteamleadList = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			MiscAccess access = new MiscAccess();
			managerteamleadList = access.fetchManagerTeamleadList(con,agencyId);	
			log.info("reset pass status in Accessmanager -- "+managerteamleadList);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return managerteamleadList;
	}
	public String fetchRoleList() throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(MiscAccessManager.class.getName());
		String roleList = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			MiscAccess access = new MiscAccess();
			roleList = access.fetchroleList(con);	
			log.info("reset pass status in Accessmanager -- "+roleList);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return roleList;
	}
	public String fetchCustList(String username,int roleId,String agencyId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(MiscAccessManager.class.getName());
		String custList = new String();
		try
		{
			Connection crmCon = db.getCRMConnection();
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			MiscAccess access = new MiscAccess();
			custList = access.fetchCustList(crmCon, con, username,roleId,agencyId);	
			log.info("fetchCustList data in MiscAccessmanager -- "+custList);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return custList;
	}
}
