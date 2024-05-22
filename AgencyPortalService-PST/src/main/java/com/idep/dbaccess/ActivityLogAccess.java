package com.idep.dbaccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.util.AgencyConstants;

public class ActivityLogAccess {
	static Properties props=new Properties();
	static InputStream in=UserAccess.class.getClassLoader().getResourceAsStream("sql.properties");
	public String getActivityLog(Connection con) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(ActivityLogAccess.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		String activityList = new String();
        
		try
		{ props.load(in);
		PreparedStatement stmt = con.prepareStatement(props.getProperty("getActivityLog"));
		 ArrayNode activityListNode = objectMapper.createArrayNode();
         log.info("Prepared Query Statement -- "+stmt);
		    ResultSet rs = stmt.executeQuery();
		    log.info("Query ResultSet -- "+rs);
			while(rs.next())
			{
				ObjectNode actvityNode = objectMapper.createObjectNode();
				actvityNode.put("Id", rs.getString("Id"));
				actvityNode.put("TimeStamp", rs.getString("TimeStamp"));
				actvityNode.put("Activity", rs.getString("Activity"));
				actvityNode.put("PerformedBy", rs.getString("PerformedBy"));
				actvityNode.put("ImpactOn", rs.getString("ImpactOn"));
				activityListNode.add(actvityNode);
			}		
			activityList = objectMapper.writeValueAsString(activityListNode);
			
		}catch (SQLException e)
		{		
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return activityList;	
	}
	public String deleteActivityLog(Connection con,int Id) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(ActivityLogAccess.class.getName());
		String status = new String();
       
		try
		{
			props.load(in);
			 PreparedStatement stmt = con.prepareStatement(props.getProperty("updateActivityLog"));	
		        stmt.setInt(1, Id);
		        log.info("Prepared Query Statement -- "+stmt);
			con.setAutoCommit(false);
			stmt.execute();
			con.commit();
			status=AgencyConstants.ACTIVITYLOG_DELETE_SUCCESS;
			
		}catch (SQLException e)
		{		
			con.rollback();
			status = AgencyConstants.ACTIVITYLOG_DELETE_FAILURE;
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return status;	
	}
	public static String addActivityLog(Connection con,String timestamp,String activity,String performedBy, String ImpactOn) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(ActivityLogAccess.class.getName());
		String status = new String();
		
		try
		{
			props.load(in);
	        PreparedStatement activitystmt = con.prepareStatement(props.getProperty("insertActivityLog"));	
        activitystmt.setString(1, timestamp);
        activitystmt.setString(2, activity);
        activitystmt.setString(3, performedBy);
        activitystmt.setString(4, ImpactOn);
        activitystmt.setString(5, "N");
        log.info("Prepared Query Statement -- "+activitystmt);
			con.setAutoCommit(false);
			activitystmt.execute();
			status=AgencyConstants.ACTIVITYLOG_ADD__SUCCESS;
			
		}catch (SQLException e)
		{		
			status = AgencyConstants.ACTIVITYLOG_ADD__FAILURE;
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return status;	
	}
}
