package com.idep.dbaccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.Agency;
import com.idep.bean.Branch;
import com.idep.util.AgencyConstants;

public class AgencyAccess {
	static Properties props=new Properties();
	static InputStream in=UserAccess.class.getClassLoader().getResourceAsStream("sql.properties");
	public String getAgencyList(Connection con) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		String agencyList = new String();
        try
		{   props.load(in);
			PreparedStatement stmt = con.prepareStatement(props.getProperty("selectAgencyList"));	
			 ArrayNode agencyListNode = objectMapper.createArrayNode();
		        log.info("Prepared Query Statement -- "+stmt);
				ResultSet rs = stmt.executeQuery();
				log.info("Query ResultSet -- "+rs);
				
			while(rs.next())
			{
				ObjectNode agencyNode = objectMapper.createObjectNode();
				agencyNode.put("AgencyId", rs.getString("AgencyId"));
				agencyNode.put("AgencyName", rs.getString("AgencyName"));
				agencyNode.put("LOB", rs.getString("LOB"));
				agencyNode.put("Address", rs.getString("Address")+", "+rs.getString("City")+", "+rs.getString("State")+", "+rs.getString("PinCode"));
				agencyNode.put("ContactPerson", rs.getString("ContactPerson"));
				agencyNode.put("delFlg", rs.getString("DelFlg"));
				agencyNode.put("Type", rs.getString("Type"));
				agencyListNode.add(agencyNode);
			}		
			agencyList = objectMapper.writeValueAsString(agencyListNode);
			
		}catch (SQLException e)
		{		
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return agencyList;	
	}
	public String addBranch(Connection con,JsonNode branchDetails,String sessionId) throws SQLException
	{   
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		String status = new String();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		try
		{   props.load(in);
		int count =0;
		PreparedStatement BranchCounter = con.prepareStatement("SELECT Id FROM agencysystem.AgencyBranch where agencyId='"+branchDetails.get("agencyId").asText()+"'order by Id DESC limit 1");
		
		//isNewbranchstmt.setString(1, branch.getId());
		log.info("LastBranchId Query: "+BranchCounter);
		ResultSet TotalBranchCount = BranchCounter.executeQuery();
		
		if(TotalBranchCount.next())
		{    String lastId=TotalBranchCount.getString("Id");
		log.info("LastBranchId : "+lastId);
		  String[]  lastId_arr =lastId.split("-");
		 count=Integer.parseInt(lastId_arr[1].substring(6))+1;
		}else{
			count=101;
		}
		
				PreparedStatement branchstmt = con.prepareStatement(props.getProperty("insertAgencyBranch"));
			
				branchstmt.setString(1,branchDetails.get("agencyId").asText()+"-BRANCH"+count);
				branchstmt.setString(2,branchDetails.get("name").asText());
				branchstmt.setString(3,branchDetails.get("email").asText());
				branchstmt.setString(4,branchDetails.get("state").asText());
				branchstmt.setString(5,branchDetails.get("city").asText());
				branchstmt.setString(6,branchDetails.get("address").asText());
				branchstmt.setLong(7,branchDetails.get("pincode").longValue());
				branchstmt.setString(8,branchDetails.get("contactPerson").asText());
				branchstmt.setLong(9,branchDetails.get("mobile").longValue());
				branchstmt.setString(10,branchDetails.get("agencyId").asText());
				branchstmt.setString(11,"N");
				branchstmt.setString(12,branchDetails.get("branchlOB").asText());
				branchstmt.setString(13, "N");
				log.info("Branch Prepared Query Statement -- "+branchstmt);
			
			con.setAutoCommit(false);
		boolean resp=branchstmt.execute();
			log.info("resp : "+resp);
			ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"ADD",sessionId,branchDetails.get("agencyId").asText()+"-BRANCH"+count);
			status=AgencyConstants.BRANCH_ADD_SUCCESS;
		con.commit();
			
		}
		catch (Exception e)
		{		
			log.info("Error Occurred While Adding New Agency! Please Try Again");
			status=AgencyConstants.AGENCY_ADD_FAILURE;
			e.printStackTrace();
			con.rollback();
		}
		return status;	
	}
	public String addAgency(Connection con,Agency agency,ArrayList<Branch> branchList,String sessionId) throws SQLException
	{   
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		String status = new String();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		try
		{   props.load(in);
			PreparedStatement agencystmt = con.prepareStatement(props.getProperty("insertAgency"));
			con.setAutoCommit(false);
			int counter = MiscAccess.getCounterValue(con, "AGENCY");
			agency.setId("AGENCY"+counter);
			agencystmt.setString(1, agency.getId());
			agencystmt.setString(2, agency.getName());
			agencystmt.setString(3, agency.getType());
			agencystmt.setString(4, agency.getLob());
			agencystmt.setString(5, "N");
			log.info("Agency Prepared Query Statement -- "+agencystmt);
			agencystmt.execute();
			
			ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"ADD",sessionId,agency.getId());
	
			for(Branch branch : branchList)
			{
				PreparedStatement branchstmt = con.prepareStatement(props.getProperty("insertAgencyBranch"));
				String[] branchId =branch.getId().split("-");
				branchstmt.setString(1,agency.getId()+"-"+branchId[1]);
				branchstmt.setString(2, branch.getName());
				branchstmt.setString(3, branch.getEmail());
				branchstmt.setString(4, branch.getState());
				branchstmt.setString(5, branch.getCity());
				branchstmt.setString(6, branch.getAddress());
				branchstmt.setLong(7, branch.getPincode());
				branchstmt.setString(8, branch.getContactPerson());
				branchstmt.setLong(9, branch.getMobile());
				branchstmt.setString(10, agency.getId());
				branchstmt.setString(11, branch.getHeadOffice());
				branchstmt.setString(12, branch.getbranchlOB());
				branchstmt.setString(13, "N");
				log.info("Branch Prepared Query Statement -- "+branchstmt);
				branchstmt.execute();
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"ADD",sessionId,branch.getId());
			}
			status=AgencyConstants.AGENCY_ADD_SUCCESS;
			con.commit();
			
		}
		catch (Exception e)
		{		
			log.info("Error Occurred While Adding New Agency! Please Try Again");
			status=AgencyConstants.AGENCY_ADD_FAILURE;
			e.printStackTrace();
			con.rollback();
		}
		return status;	
	}
	public String fetchAgency(Connection con,String agencyId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		String agencydetails = new String();
		
		
		
		Agency agency = new Agency();
		ArrayList<Branch> branchList = new ArrayList<Branch>();
		try
		{	props.load(in);
		PreparedStatement agencystmt = con.prepareStatement(props.getProperty("selectAgency"));
		agencystmt.setString(1, agencyId);
        PreparedStatement branchstmt = con.prepareStatement(props.getProperty("selectAgencyBranch"));
        branchstmt.setString(1, agencyId);
		log.info("Agency Prepared Query Statement -- "+agencystmt);
		log.info("Branch Prepared Query Statement -- "+branchstmt);
		
		ResultSet agencyrs = agencystmt.executeQuery();
		ResultSet branchrs = branchstmt.executeQuery();
		
		log.info("Agency Query ResultSet -- "+agencyrs.toString());
		log.info("Branch Query ResultSet -- "+branchrs.toString());
			while(agencyrs.next())
			{
				agency.setId(agencyrs.getString("Id"));
				agency.setName(agencyrs.getString("Name"));
				agency.setType(agencyrs.getString("Type"));
				agency.setLob(agencyrs.getString("LOB"));
			}
			while(branchrs.next())
			{
				Branch branch = new Branch();
				branch.setId(branchrs.getString("Id"));
				branch.setName(branchrs.getString("Name"));
				branch.setEmail(branchrs.getString("Email"));
				branch.setState(branchrs.getString("State"));
				branch.setCity(branchrs.getString("City"));
				branch.setAddress(branchrs.getString("Address"));
				branch.setPincode(branchrs.getLong("PinCode"));
				branch.setContactPerson(branchrs.getString("ContactPerson"));
				branch.setMobile(branchrs.getLong("Mobile"));
				branch.setAgencyId(branchrs.getString("AgencyId"));
				branch.setHeadOffice(branchrs.getString("HeadOffice"));
				branch.setbranchlOB(branchrs.getString("LOB"));
				branchList.add(branch);
			}
			
			String agencydata = objectMapper.writeValueAsString(agency);
			String branchdata = objectMapper.writeValueAsString(branchList);
			log.info("Agency Query ResultSet after processing -- "+agencydata);
			log.info("Branch Query ResultSet after processing -- "+branchdata);
			JsonNode agencyNode = objectMapper.readTree(agencydata);
			JsonNode branchNode = objectMapper.readTree(branchdata);
			((ObjectNode)agencyNode).put("branchDetails", branchNode);
			
			agencydetails = objectMapper.writeValueAsString(agencyNode);
			
		}
		catch (SQLException e)
		{		
			log.info("Error occurred while fetching agency details from the database..!!");
			e.printStackTrace();
		}
		return agencydetails;
	}
	
	public String updateAgency(Connection con,Agency agency,ArrayList<Branch> branchList,String sessionId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String status = new String();
		try
		{       props.load(in);
		PreparedStatement agencystmt = con.prepareStatement(props.getProperty("updateAgency"));
				con.setAutoCommit(false);
				agencystmt.setString(1, agency.getId());
				agencystmt.setString(2, agency.getName());
				agencystmt.setString(3, agency.getType());
				agencystmt.setString(4, agency.getLob());
				agencystmt.setString(5, agency.getId());
				log.info("Agency Prepared Query Statement -- "+agencystmt);
				agencystmt.execute();
				
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"UPDATE",sessionId,agency.getId());
			
			for(Branch branch : branchList)
			{
					int count =0;
					PreparedStatement isNewbranchstmt = con.prepareStatement(props.getProperty("updateAgencyBranch"));
					isNewbranchstmt.setString(1, branch.getId());
					ResultSet isNewBranchrs = isNewbranchstmt.executeQuery();
					PreparedStatement branchstmt = null;
					while(isNewBranchrs.next())
					{
						count = isNewBranchrs.getInt("isNewBranch");
					}
					if(count == 0){
							branchstmt = con.prepareStatement(props.getProperty("updateByNewBranch"));branchstmt.setString(1,branch.getId());
							branchstmt.setString(2, branch.getName());
							branchstmt.setString(3, branch.getEmail());
							branchstmt.setString(4, branch.getState());
							branchstmt.setString(5, branch.getCity());
							branchstmt.setString(6, branch.getAddress());
							branchstmt.setLong(7, branch.getPincode());
							branchstmt.setString(8, branch.getContactPerson());
							branchstmt.setLong(9, branch.getMobile());
							branchstmt.setString(10, branch.getAgencyId());
							branchstmt.setString(11, branch.getHeadOffice());
							branchstmt.setString(12, branch.getbranchlOB());
							branchstmt.setString(13, "N");
					}else{
							branchstmt = con.prepareStatement(props.getProperty("updateBranch"));
							branchstmt.setString(1, branch.getId());
							branchstmt.setString(2, branch.getName());
							branchstmt.setString(3, branch.getEmail());
							branchstmt.setString(4, branch.getState());
							branchstmt.setString(5, branch.getCity());
							branchstmt.setString(6, branch.getAddress());
							branchstmt.setLong(7, branch.getPincode());
							branchstmt.setString(8, branch.getContactPerson());
							branchstmt.setLong(9, branch.getMobile());
							branchstmt.setString(10, branch.getAgencyId());
							branchstmt.setString(11, branch.getHeadOffice());
							branchstmt.setString(12, branch.getbranchlOB());
							branchstmt.setString(13, branch.getId());
					}
					log.info("Branch Prepared Query Statement -- "+branchstmt);
					branchstmt.execute();
					
					ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"UPDATE",sessionId,branch.getId());
			}
			con.commit();
			status=AgencyConstants.AGENCY_UPDATE_SUCCESS;
		}
		catch (Exception e)
		{		
			log.info("Error Occurred While Updating Agency! Please try Again.");
			e.printStackTrace();
			con.rollback();
		}
		return status;	
	}
	public String deleteAgency (Connection con,String agencyId,String sessionId) throws SQLException, IOException
	{
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		props.load(in);
		PreparedStatement agencystmt = con.prepareStatement(props.getProperty("updateAgencyForDelFlg"));
		agencystmt.setString(1, agencyId);
		log.info("Agency Prepared Query Statement -- "+agencystmt);
		String status = new String();
		PreparedStatement countbranchstmt = con.prepareStatement(props.getProperty("selectCountOfBranch"));
		countbranchstmt.setString(1, agencyId);
		ResultSet rs = countbranchstmt.executeQuery();
		int count = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		while(rs.next())
		{
			count = rs.getInt("COUNT");
		}
		if(count == 0)
		{
			try
			{
				con.setAutoCommit(false);
				agencystmt.execute();
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"DELETE",sessionId,agencyId);
				con.commit();
				status=AgencyConstants.AGENCY_DELETE_SUCCESS;
			}
			catch (SQLException e)
			{		
				con.rollback();
				log.info("Error occurred while deleting agency data in database..!!");
				status = AgencyConstants.AGENCY_DELETE_FAILURE;
				e.printStackTrace();
			}
		}
		else{
			log.info("Error occurred while deleting agency data in database..!!");
			status = AgencyConstants.AGENCY_DELETE_FAILURE;
		}
		return status;	
	}
	public String deleteAgencyBranch (Connection con,String branchId,String sessionId) throws Exception
	{
		Logger log = Logger.getLogger(AgencyAccess.class.getName());
		PreparedStatement branchstmt = con.prepareStatement(props.getProperty("updateAgencyBranchForDelFlg"));
		branchstmt.setString(1, branchId);
		log.info("Agency Branch Prepared Query Statement -- "+branchstmt);
		
		String status = new String();
		
		PreparedStatement countagentstmt = con.prepareStatement(props.getProperty("agentCount"));
		countagentstmt.setString(1, branchId);
		ResultSet rs = countagentstmt.executeQuery();
		int count = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		while(rs.next())
		{
			count = rs.getInt("COUNT");
		}
		if(count == 0)
		{
			try
			{
				con.setAutoCommit(false);
				branchstmt.execute();
				ActivityLogAccess.addActivityLog(con,sdf.format(timestamp).toString(),"DELETE",sessionId,branchId);
				con.commit();
				status=AgencyConstants.BRANCH_DELETE_SUCCESS;
			}
			catch (SQLException e)
			{		
				con.rollback();
				log.info("Error occurred while deleting agency data in database..!!");
				status = AgencyConstants.BRANCH_DELETE_FAILURE;
				e.printStackTrace();
			}
		}
		else{
			log.info("Error occurred while deleting agency data in database..!!");
			status =  AgencyConstants.BRANCH_DELETE_FAILURE;
			
			}
		return status;	
	}
}
