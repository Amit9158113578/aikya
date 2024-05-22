package com.idep.model;

import java.sql.SQLException;


import org.apache.log4j.Logger;

import com.idep.database.Database;
import com.idep.dbaccess.ActivityLogAccess;
import com.mysql.jdbc.Connection;

public class ActivityLogAccessManager {
	public String getActivityLog() throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(ActivityLogAccessManager.class.getName());
		String userData = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			ActivityLogAccess access = new ActivityLogAccess();
			userData = access.getActivityLog(con);	
			log.info("getActivityLog status in Accessmanager -- "+userData);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return userData;
	}
	public String deleteActivityLog(int Id) throws Exception
	{
		Database db = new Database();
		Logger log = Logger.getLogger(ActivityLogAccessManager.class.getName());
		String userData = new String();
		try
		{
			Connection con = db.getAgencyConnection();
			log.info("Connection Created..!!!");
			ActivityLogAccess access = new ActivityLogAccess();
			userData = access.deleteActivityLog(con,Id);	
			log.info("deleteActivityLog status in Accessmanager -- "+userData);
		}
		catch(SQLException e)
		{
			log.info("Error in AccessManager!!");
			e.printStackTrace();
		}
		return userData;
	}
}
