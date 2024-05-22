package com.idep.model;

import java.sql.SQLException;


import org.apache.log4j.Logger;

import com.idep.bean.User;
import com.idep.bean.UserProfile;
import com.idep.database.Database;
import com.idep.dbaccess.UserAccess;
import com.mysql.jdbc.Connection;

public class UserAccessManager {
	public UserProfile getUserDetails(String UserId) throws Exception
	{
		UserProfile userDetails = new UserProfile();
		Database db = new Database();
		Logger log = Logger.getLogger(UserAccessManager.class.getName());
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			UserAccess access = new UserAccess();
			userDetails = access.getUserDetails(con,UserId);	
			log.info("User Details List in Accessmanager -- "+userDetails);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return userDetails;
	}
	public String updateUserDetails (UserProfile user)throws Exception
	{
		String userDetails = new String();
		Database db = new Database();
		Logger log = Logger.getLogger(UserAccessManager.class.getName());
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			UserAccess access = new UserAccess();
			userDetails = access.updateUserDetails(con,user);	
			log.info("User Details List in Accessmanager -- "+userDetails);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return userDetails;
	}
	public User validateUser(String Username, String password) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(UserAccessManager.class.getName());
		User userlogindata = new User();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			UserAccess access = new UserAccess();
			userlogindata = access.getUserLoginDetails(con,Username,password);	
			log.info("userStatus in Accessmanager -- "+userlogindata);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return userlogindata;
	}

}
