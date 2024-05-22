package com.idep.model;

import java.sql.SQLException;


import org.apache.log4j.Logger;

import com.idep.bean.UserProfile;
import com.idep.database.Database;
import com.idep.dbaccess.AgentAccess;
import com.mysql.jdbc.Connection;

public class AgentAccessManager {
	public String fetchAgentList(String role,String user) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgentAccessManager.class.getName());
		String agentlist = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgentAccess access = new AgentAccess();
			agentlist = access.getAgentList(con,role,user);	
			log.info("AgentList in Accessmanager -- "+agentlist);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return agentlist;
	}
	public String addAgent(UserProfile agent,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgentAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgentAccess access = new AgentAccess();
			status = access.addAgent(con,agent,sessionId);	
			log.info("addAgent status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public UserProfile fetchAgent(String agentId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgentAccessManager.class.getName());
		UserProfile agent = new UserProfile();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgentAccess access = new AgentAccess();
			agent = access.fetchAgent(con,agentId);	
			log.info("fetchAgent status in Accessmanager -- "+agent);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return agent;
	}
	public String updateAgent(UserProfile agent,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgentAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgentAccess access = new AgentAccess();
			status = access.updateAgent(con,agent,sessionId);	
			log.info("updateAgent status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
	public String deleteAgent(String agentId,String sessionId) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(AgentAccessManager.class.getName());
		String status = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			AgentAccess access = new AgentAccess();
			status = access.deleteAgent(con,agentId,sessionId);	
			log.info("deleteAgent status in Accessmanager -- "+status);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return status;
	}
}
