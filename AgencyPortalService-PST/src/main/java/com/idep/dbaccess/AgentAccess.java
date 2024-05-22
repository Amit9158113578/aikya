package com.idep.dbaccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.UserProfile;
import com.idep.util.AgencyConstants;

public class AgentAccess {
	static Properties props=new Properties();
	static InputStream in=UserAccess.class.getClassLoader().getResourceAsStream("sql.properties");
	
	public String getAgentList(Connection con,String role,String user) throws SQLException, IOException
	{	
		Logger log = Logger.getLogger(AgentAccess.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayNode agentListNode = objectMapper.createArrayNode();
		ObjectNode lastAgentIdNode = objectMapper.createObjectNode();
		String agentList = new String();
		ResultSet rs = null;
		ResultSet lastagentrs = null;
		props.load(in);
		if(role.equals("NIBPL_ADMIN")){
			
			PreparedStatement stmt1 = con.prepareStatement(props.getProperty("AgentDetailsByNIBPL_ADMIN"));
			
			log.info("Prepared Query Statement -- "+stmt1);
			rs = stmt1.executeQuery();
			log.info("Query ResultSet -- "+rs);
		}
		if(role.equals("AGENCY_ADMIN")||role.equals("MANAGER")||role.equals("TEAM_LEAD")){
				
			PreparedStatement stmt2 = con.prepareStatement(props.getProperty("agentDetailsByAGENCY_ADMIN"));
			
			stmt2.setString(1, user);
			stmt2.setString(2,role);
			log.info("Prepared Query Statement -- "+stmt2);
			rs = stmt2.executeQuery();
			log.info("Query ResultSet -- "+rs);
		}
		PreparedStatement lastagentIdstmt = con.prepareStatement(props.getProperty("idFromAgencyPortal"));
		log.info("Prepared Query Statement -- "+lastagentIdstmt);
		lastagentrs = lastagentIdstmt.executeQuery();
		log.info("Query ResultSet -- "+lastagentrs);
		try
		{
			while(rs.next())
			{
				ObjectNode agentNode = objectMapper.createObjectNode();
				agentNode.put("Id", rs.getString("Id"));
				agentNode.put("Name", rs.getString("Name"));
				agentNode.put("AgencyName", rs.getString("AgencyName"));
				agentNode.put("Address", rs.getString("Address")+","+rs.getString("PinCode"));
				agentNode.put("Designation", rs.getString("Designation"));
				agentNode.put("BranchId", rs.getString("AgencyId"));
				agentNode.put("Mobile", rs.getString("Mobile"));
				 agentNode.put("Username", rs.getString("Username"));
				agentNode.put("delFlg", rs.getString("DelFlg"));
				agentListNode.add(agentNode);
			}
			while(lastagentrs.next())
			{
				lastAgentIdNode.put("lastAgentId", lastagentrs.getString("Id"));
			}
			lastAgentIdNode.put("agentList",agentListNode);
			agentList = objectMapper.writeValueAsString(lastAgentIdNode);
		}catch (SQLException e)
		{		
			log.info("Error occurred while processing AgentList resultset..!!");
			e.printStackTrace();
		}
		return agentList;	
	}
	
	public String addAgent(Connection con,UserProfile agent,String sessionId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgentAccess.class.getName());
		int count =0;
		String status = new String();
		props.load(in);
		if(agent.getDesignation().equals("AGENCY_ADMIN"))
		{
			
			log.info("agencyid"+agent.getAgencyId());
			String agencyId=agent.getAgencyId();
			String str=agencyId.substring(0,9);
			log.info("agencyid"+str);
			PreparedStatement admincount = con.prepareStatement(props.getProperty("agencyAdminCount"));
			admincount.setString(1,str+"%");
			try{
				ResultSet rs = admincount.executeQuery();
				while(rs.next())
				{
					count = rs.getInt("COUNT");
				}
			}catch(Exception e)
			{
				log.info("Error occurred while getting Agency Admin count..!!");
				e.printStackTrace();
			}
		}
		if(count==0)
		{
			PreparedStatement agentstmt = con.prepareStatement(props.getProperty("insertAgent"));
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			try {
				con.setAutoCommit(false);
				int counter = MiscAccess.getCounterValue(con, "AGENT");
				agent.setAgentId("AGENT"+counter);
				try
				{
					agentstmt.setString(1, agent.getAgentId());
					agentstmt.setString(2, agent.getName());
					agentstmt.setString(3, agent.getEmail());
					agentstmt.setLong(4, agent.getMobile());
					agentstmt.setString(5, agent.getAddress());
					agentstmt.setString(6, agent.getDesignation());
					agentstmt.setString(7, agent.getReportingTo());
					agentstmt.setString(8, "N");
					log.info("Agency Prepared Query Statement -- "+agentstmt);
					agentstmt.execute();
					
					ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"ADD",sessionId,agent.getAgentId());
				}
				catch (SQLException e)
				{
					log.info("Error occurred while inserting user profile data in database..!!");
					status= AgencyConstants.ADD_NEW_AGENT_FAILURE;
					e.printStackTrace();
					con.rollback();
				}
				try{
						String password = new String();
						password = MiscAccess.getEncryptedPassword(agent.getPass());
						PreparedStatement agentloginstmt = con.prepareStatement(props.getProperty("insertAgentIntoUser"));
						PreparedStatement getroleIdstmt = con.prepareStatement(props.getProperty("selectIdByRolename"));
						PreparedStatement getlobstmt = con.prepareStatement(props.getProperty("selectLOBById"));
						getlobstmt.setString(1, agent.getAgencyId());
						getroleIdstmt.setString(1, agent.getDesignation());
						log.info("LOB Statement Query :"+getlobstmt);
						log.info("RoleId Statement Query :"+getroleIdstmt);
						ResultSet roleId = getroleIdstmt.executeQuery();
						ResultSet lobrs = getlobstmt.executeQuery();
						long Id = 0;
						String lob = new String();
						while (roleId.next())
						{
							Id = roleId.getLong("Id");
						}
						while (lobrs.next())
						{
							lob = lobrs.getString("LOB");
						}
						
						log.info("User Login details : Id :"+agent.getAgentId()+" Username :"+ agent.getUsername()+" pass :"+ password+" role ID :"+Id+" AgencyId :"+agent.getAgencyId()+" LOB :"+lob);
						agentloginstmt.setString(1, agent.getAgentId());
						agentloginstmt.setString(2, agent.getUsername());
						agentloginstmt.setString(3, password);
						agentloginstmt.setString(4, "Y");
						agentloginstmt.setLong(5, Id);
						agentloginstmt.setString(6, agent.getAgencyId());
						agentloginstmt.setString(7, lob);
						agentloginstmt.setString(8, AgencyConstants.USER_DEFAULT_IMAGE);
						agentloginstmt.setString(9, "N");
						log.info("Agent login Prepared Query Statement -- "+agentloginstmt);
						agentloginstmt.execute();
						con.commit();
						status=AgencyConstants.AGENT_ADD_SUCCESS;
					}
					catch (SQLException e)
					{
						log.info("Error occurred while inserting user login data in database..!!");
						status=AgencyConstants.ADD_NEW_AGENT_FAILURE;
						e.printStackTrace();
						con.rollback();
					}
			}
			catch (Exception e)
			{		
				log.info("Error occurred while adding new agency..!!");
				status=AgencyConstants.ADD_NEW_AGENT_FAILURE;
				e.printStackTrace();
				con.rollback();
			}
			}else{
				log.info("Agency Admin Already exists for the Agency..!!");
				status=AgencyConstants.ADD_NEW_ADMIN_FAILURE;
		}
		return status;	
	}
	public UserProfile fetchAgent(Connection con,String agentId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgentAccess.class.getName());
		
		
		
		UserProfile agent = new UserProfile();
		try 
		{   props.load(in);
			PreparedStatement agentloginstmt = con.prepareStatement(props.getProperty("selectAgent"));
			agentloginstmt.setString(1, agentId);
	        PreparedStatement agentstmt = con.prepareStatement(props.getProperty("selectUserProfile"));
	        agentstmt.setString(1, agentId);
	        
	        PreparedStatement agencynamestmt = con.prepareStatement(props.getProperty("selectAgencyName"));
	        agencynamestmt.setString(1, agentId);
	        log.info("Agent Branch Name Prepared Query Statement -- "+agencynamestmt);
	        log.info("Agent Login Details Prepared Query Statement -- "+agentloginstmt);
			log.info("Agent Profile Details Prepared Query Statement -- "+agentstmt);
			
			ResultSet agentrs = agentloginstmt.executeQuery();
			ResultSet agentprofilers = agentstmt.executeQuery();
			ResultSet agencynamers = agencynamestmt.executeQuery();
			
			log.info("Agent login Query ResultSet -- "+agentrs.toString());
			log.info("Agent Profile Query ResultSet -- "+agentprofilers.toString());
			log.info("Agency name Query ResultSet -- "+agencynamers.toString());
			while(agentrs.next())
			{
				agent.setUsername(agentrs.getString("Username"));
				agent.setPass(agentrs.getString("Pass"));
				agent.setAgencyId(agentrs.getString("AgencyId"));
			}
			while(agentprofilers.next())
			{
				agent.setAgentId(agentprofilers.getString("Id"));
				agent.setName(agentprofilers.getString("Name"));
				agent.setEmail(agentprofilers.getString("Email"));
				agent.setMobile(agentprofilers.getLong("Mobile"));
				agent.setAddress(agentprofilers.getString("Address"));
				agent.setDesignation(agentprofilers.getString("Designation"));
				agent.setReportingTo(agentprofilers.getString("ReportingTo"));
			}
			while(agencynamers.next())
			{
				log.info("Agency Name : "+agencynamers.getString("AgencyName"));
				agent.setAgencyName(agencynamers.getString("AgencyName"));
			}
		}
		catch (SQLException e)
		{		
			log.info("Error occurred while fetching Agent details from the database..!!");
			e.printStackTrace();
		}
		return agent;
	}
	public String updateAgent(Connection con,UserProfile agent,String sessionId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgentAccess.class.getName());
		String status = new String();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		try {   
			    props.load(in);
			    PreparedStatement agentstmt = con.prepareStatement(props.getProperty("updateAgentProfile"));
			
				con.setAutoCommit(false);
				agentstmt.setString(1, agent.getAgentId());
				agentstmt.setString(2, agent.getName());
				agentstmt.setString(3, agent.getEmail());
				agentstmt.setLong(4, agent.getMobile());
				agentstmt.setString(5, agent.getAddress());
				agentstmt.setString(6, agent.getDesignation());
				agentstmt.setString(7, agent.getReportingTo());
				agentstmt.setString(8, agent.getAgentId());
				
				log.info("Agent Profile Prepared Query Statement -- "+agentstmt);
				agentstmt.execute();
				
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"UPDATE",sessionId,agent.getAgentId());
				PreparedStatement agentloginstmt = con.prepareStatement(props.getProperty("updateAgent"));
				PreparedStatement getroleIdstmt = con.prepareStatement(props.getProperty("selectIdByRolename"));
				PreparedStatement getlobstmt = con.prepareStatement(props.getProperty("selectLOBById"));
				getlobstmt.setString(1, agent.getAgencyId());
				getroleIdstmt.setString(1, agent.getDesignation());
				log.info("LOB Statement Query :"+getlobstmt);
				log.info("RoleId Statement Query :"+getroleIdstmt);
				ResultSet roleId = getroleIdstmt.executeQuery();
				ResultSet lobrs = getlobstmt.executeQuery();
				long Id = 0;
				String lob = new String();
				while (roleId.next())
				{
					Id = roleId.getLong("Id");
				}
				while (lobrs.next())
				{
					lob = lobrs.getString("LOB");
				}
				agentloginstmt.setString(1, agent.getUsername());
				agentloginstmt.setString(2, "Y");
				agentloginstmt.setLong(3, Id);
				agentloginstmt.setString(4, agent.getAgencyId());
				agentloginstmt.setString(5, lob);
				agentloginstmt.setString(6, agent.getAgentId());
				log.info("Agent login Prepared Query Statement -- "+agentloginstmt);
				agentloginstmt.execute();
				con.commit();
				status=AgencyConstants.AGENT_UPDATE_SUCCESS;
			}
			catch (SQLException e)
			{
				log.info("Error occurred while inserting user login data in database..!!");
				status=AgencyConstants.AGENT_UPDATE_FAILURE;
			e.printStackTrace();
				con.rollback();
			}
		return status;	
	}
	public String deleteAgent (Connection con,String agentId,String sessionId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgentAccess.class.getName());
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String status = new String();
		try
		{  props.load(in);
		PreparedStatement agentstmt = con.prepareStatement(props.getProperty("updateUserProfileById"));
		agentstmt.setString(1, agentId);
		PreparedStatement agentloginstmt = con.prepareStatement(props.getProperty("UpdateUserById"));
		agentloginstmt.setString(1, agentId);
		log.info("Agent Profile Prepared Query Statement -- "+agentstmt);
		log.info("Agent Login Prepared Query Statement -- "+agentloginstmt);
			con.setAutoCommit(false);
			agentstmt.execute();
			try
			{
				agentloginstmt.execute();
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"DELETE",sessionId,agentId);
				con.commit();
			}
			catch(SQLException e)
			{
				con.rollback();
				log.info("Error occurred while deleting agent login details from database..!!");
				status = AgencyConstants.AGENT_DELETE_FAILURE;
				e.printStackTrace();
			}
		}
		catch (SQLException e)
		{		
			con.rollback();
			log.info("Error occurred while deleting agent data from database..!!");
			status = AgencyConstants.AGENT_UPDATE_FAILURE;
			e.printStackTrace();
		}
		status = AgencyConstants.AGENT_DELETE_SUCCESS;
		return status;	
	}
}
