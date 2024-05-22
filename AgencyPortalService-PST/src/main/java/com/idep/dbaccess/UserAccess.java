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

import com.idep.bean.User;
import com.idep.bean.UserProfile;
import com.idep.util.AgencyConstants;

public class UserAccess {
	Properties props=new Properties();
	InputStream in=UserAccess.class.getClassLoader().getResourceAsStream("sql.properties");
	public UserProfile getUserDetails(Connection con, String UserId) throws SQLException, IOException,Exception
	{   
		Logger log = Logger.getLogger(UserAccess.class.getName());
		UserProfile userObj = new UserProfile();
		try {
			props.load(in);
			PreparedStatement stmt = con.prepareStatement(props.getProperty("getUserProfile"));
			stmt.setString(1, UserId);
			log.info("Prepared Query Statement -- "+stmt);
			ResultSet rs = stmt.executeQuery();
			log.info("Query ResultSet -- "+rs);
			while(rs.next())
			{
				userObj.setUsername(rs.getString("Username"));
				userObj.setAgentId(rs.getString("Id"));
				userObj.setPass(rs.getString("Pass"));
				userObj.setName(rs.getString("Name"));
				userObj.setEmail(rs.getString("Email"));
				userObj.setMobile(rs.getLong("Mobile"));
				userObj.setAgencyId(rs.getString("AgencyId"));
				userObj.setAgencyName(rs.getString("AgencyName"));
				userObj.setAddress(rs.getString("Address"));
				userObj.setDesignation(rs.getString("Designation"));
			}
		} catch (SQLException e)
		{		
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return userObj;		
	}
	public String updateUserDetails(Connection con, UserProfile user) throws SQLException,IOException
	{   
		Logger log = Logger.getLogger(UserAccess.class.getName());
		String status = new String();
		
		try
		{  
			props.load(in);
	        PreparedStatement UserProfilestmt = con.prepareStatement(props.getProperty("updateUserProfile"));
			
			con.setAutoCommit(false);
			try
			{
				UserProfilestmt.setString(1, user.getName());
				UserProfilestmt.setString(2, user.getEmail());
				UserProfilestmt.setLong(3, user.getMobile());
				UserProfilestmt.setString(4, user.getAddress());
				UserProfilestmt.setString(5, user.getAgentId());
				log.info("Agency Prepared Query Statement -- "+UserProfilestmt);
				UserProfilestmt.execute();
			}
			catch (SQLException e)
			{
				log.info("Error occurred while updating user Profile data in database..!!");
				status=AgencyConstants.USER_UPDATE_FAILURE;
				e.printStackTrace();
				con.rollback();
			}
			try{
				props.load(in);
				PreparedStatement userstmt = con.prepareStatement(props.getProperty("updateUser"));
				String pass = new String();
				pass = MiscAccess.getEncryptedPassword(user.getPass());
				userstmt.setString(1, user.getUsername());
				userstmt.setString(2, pass);
				userstmt.setString(3, user.getUserImage());
				userstmt.setString(4, user.getAgentId());
				log.info("User Login Prepared Query Statement -- "+userstmt);
				userstmt.execute();
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"UPDATE",user.getAgentId(),user.getAgentId());
			}
			catch (SQLException e)
			{
				log.info("Error occurred while updating User login data in database..!!");
				status=AgencyConstants.USER_UPDATE_FAILURE;
				e.printStackTrace();
				con.rollback();
			}
			con.commit();
			status=AgencyConstants.USER_UPDATE_SUCCESS;
		}
		catch (Exception e)
		{		
			log.info("Error occurred while updating User Details..!!");
			e.printStackTrace();
			con.rollback();
		}
		return status;
	}
	
	public User getUserLoginDetails(Connection con, String Username, String pass) throws Exception
	{
		Logger log = Logger.getLogger(UserAccess.class.getName());
		User user = new User();
		String password = new String();
		password = MiscAccess.getEncryptedPassword(pass);
		try
		{
			props.load(in);
			PreparedStatement stmt = con.prepareStatement(props.getProperty("selectUser"));
			PreparedStatement getlobstmt = con.prepareStatement(props.getProperty("selectLOBById"));
			
			stmt.setString(1, Username);
	        stmt.setString(2, password);
	        log.info("Prepared Query Statement -- "+stmt);
		    
	        ResultSet rs = stmt.executeQuery();
	        log.info("Query ResultSet -- "+rs);
	       
	        while(rs.next())
			{
				user.setAgencyId(rs.getString("AgencyId"));
				user.setIsActive(rs.getString("IsActive"));
				user.setUsername(rs.getString("Username"));
				user.setRoleId(rs.getLong("RoleId"));
				user.setPass(rs.getString("Pass"));
				//String lob=rs.getString("LOB");
				getlobstmt.setString(1, rs.getString("AgencyId"));
				log.info("LOB Statement Query :"+getlobstmt);
				ResultSet lobrs = getlobstmt.executeQuery();
				long Id = 0;
				String lob = new String();
				while (lobrs.next())
				{
					lob = lobrs.getString("LOB");
				}
				log.info("lob : "+lob);
				String arr[] =lob.split(", ");
				String final_lob="";
				for(int i=0;i<arr.length;i++){
				if(arr[i].equals("Car"))
				{ 
					final_lob=final_lob+"3"+",";
				}else if(arr[i].equals("Bike"))
				{ final_lob=final_lob+"2"+",";
					
				}else if(arr[i].equals("Health"))
				{final_lob=final_lob+"4"+",";
					
				}else if(arr[i].equals("Life"))
				{final_lob=final_lob+"1"+",";
					
				}else if(arr[i].equals("Travel"))
				{final_lob=final_lob+"5"+",";
				
				}
				else if(rs.getString("LOB").equals("All"))
				{final_lob=final_lob+"0"+",";
					
				}
				}
				if(final_lob.lastIndexOf(",")!=-1){
				final_lob=final_lob.substring(0,final_lob.lastIndexOf(","));
				}
				log.info("lob : "+final_lob);
				
				user.setLob(final_lob);
				
user.setUserImage(rs.getString("UserImage"));
				
			}
	        if(user.getIsActive()==null){
	        	throw new Exception(AgencyConstants.INVALID_LOGIN);
	        }
	        
		}catch (SQLException e)
		{		
			log.info("Error occurred while processing login resultset..!!");
			e.printStackTrace();
			e.getMessage();
		}
		return user;	
	}
	
}
