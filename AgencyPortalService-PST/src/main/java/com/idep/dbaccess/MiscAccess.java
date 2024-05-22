package com.idep.dbaccess;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.util.AgencyConstants;

public class MiscAccess {
	static Properties props = new Properties();
	JsonNode customerPolicyNode;
	static InputStream in =UserAccess.class.getClassLoader().getResourceAsStream("sql.properties");
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(MiscAccess.class.getName());
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static {
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		try {
			docConfigNode = objectMapper.readTree(((JsonObject) serverConfigService.getDocBYId("LogConfiguration").content()).toString());
		} catch(Exception e) {
			log.info("Failed to load Log Config Document" + e);
		}
	}

	public static String getEncryptedPassword(String password) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] bytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	public String getRole(Connection con, long roleId) throws SQLException {
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		String rolename = new String();
		try {
			props.load( in );
			PreparedStatement stmt = con.prepareStatement(props.getProperty("selectRoleName"));
			stmt.setLong(1, roleId);
			log.info("Prepared Query Statement -- " + stmt);
			ResultSet rs = stmt.executeQuery();
			log.info("Query ResultSet -- " + rs);
			while (rs.next()) {
				rolename = rs.getString("RoleName");
			}
		} catch(SQLException | IOException e) {
			log.info("Error occurred while processing RoleName resultset..!!");
			e.printStackTrace();
		}
		return rolename;
	}

	public String resetPass(Connection con, String email, long mobile, String newPass) throws SQLException,
	IOException {
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		String status = new String();
		props.load( in );
		PreparedStatement usercheckstmt = con.prepareStatement(props.getProperty("resetPass"));
		usercheckstmt.setString(1, email);
		usercheckstmt.setLong(2, mobile);
		int count = 0;
		log.info("Prepared Query Statement -- " + usercheckstmt);
		ResultSet rs = usercheckstmt.executeQuery();
		log.info("Query ResultSet -- " + rs);
		try {
			while (rs.next()) {
				count = rs.getInt("COUNT");
			}
			if (count > 0) {
				log.info("Value of Count " + count);
				String newpass = getEncryptedPassword(newPass);
				log.info("New Password :" + newPass);
				PreparedStatement updatepassstmt = con.prepareStatement(props.getProperty("updateUsername&Pass"));
				updatepassstmt.setString(1, newpass);
				updatepassstmt.setString(2, email);
				log.info("Update Password Query Statement --" + updatepassstmt);
				try {
					con.setAutoCommit(false);
					updatepassstmt.execute();
					log.info("New Password Updated.");
					con.commit();
					status = AgencyConstants.TRIGGER_EMAIL_SEND_MSG;
				} catch(Exception e) {
					con.rollback();
					status = AgencyConstants.TRIGGER_EMAIL_FAILURE_PASSWORD;
					log.info("Error occurred while updating password or sending mail. Transaction rolled back.");
					e.printStackTrace();
				}
			} else {
				status = AgencyConstants.TRIGGER_EMAIL_INVALID_DETAILS;
			}
		} catch(SQLException e) {
			con.rollback();
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return status;
	}

	public String fetchManagerTeamleadList(Connection con, String agencyId)
	throws SQLException,
	IOException {
		props.load( in );
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		String reportingToList = new String();
		ObjectMapper objectMapper = new ObjectMapper();
		PreparedStatement reportingtoliststmt = con.prepareStatement(props.getProperty("reportingToList"));
		ArrayNode reportingTolist = objectMapper.createArrayNode();
		reportingtoliststmt.setString(1, agencyId);
		log.info("Prepared Query Statement -- " + reportingtoliststmt);
		ResultSet rs = reportingtoliststmt.executeQuery();
		log.info("Query ResultSet -- " + rs);
		try {
			while (rs.next()) {
				ObjectNode reportingToNode = objectMapper.createObjectNode();
				reportingToNode.put("Id", rs.getString("Id"));
				reportingToNode.put("Name", rs.getString("Name"));
				reportingToNode.put("Designation", rs.getString("Designation"));
				reportingTolist.add(reportingToNode);
			}
			reportingToList = objectMapper.writeValueAsString(reportingTolist);

		} catch(SQLException e) {
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return reportingToList;
	}

	public String fetchroleList(Connection con) throws SQLException,
	IOException {
		props.load( in );
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		String roleList = new String();
		ObjectMapper objectMapper = new ObjectMapper();
		PreparedStatement roleListstmt = con.prepareStatement(props.getProperty("selectRoleList"));
		ArrayNode rolelist = objectMapper.createArrayNode();
		log.info("Prepared Query Statement -- " + roleListstmt);
		ResultSet rs = roleListstmt.executeQuery();
		log.info("Query ResultSet -- " + rs);
		try {
			while (rs.next()) {
				ObjectNode roleNode = objectMapper.createObjectNode();
				roleNode.put("id", rs.getString("Id"));
				roleNode.put("role", rs.getString("RoleName"));
				roleNode.put("value", rs.getString("Value"));
				rolelist.add(roleNode);
			}
			roleList = objectMapper.writeValueAsString(rolelist);

		} catch(SQLException e) {
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return roleList;
	}

	public static int getCounterValue(Connection con, String Id)
	throws SQLException,
	IOException {
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		int counter = 0;
		props.load( in );
		PreparedStatement countertstmt = con.prepareStatement(props.getProperty("getCounterValue"));
		countertstmt.setString(1, Id);
		log.info("Prepared Query Statement -- " + countertstmt);
		ResultSet rs = countertstmt.executeQuery();
		log.info("Query ResultSet -- " + rs);
		try {
			while (rs.next()) {
				counter = rs.getInt("count");
			}
		} catch(SQLException e) {
			log.info("Error occurred while processing resultset..!!");
			e.printStackTrace();
		}
		return counter;
	}

	public String fetchCustomers(String usernameList[], String agencyId)
	throws SQLException,
	IOException {
		String customerList_String = null;
		ArrayNode customerList_Array = null;
		ObjectMapper objectMapper = null;
		JsonNode policyNode = null;
		ObjectNode respCustomerNode = null;
		String LOB = null;
		String queryForFetchCustomers = "";
		List < Map < String,
		Object >> getCusomersProposals = null;
		String whereClause = null;
		try {
			queryForFetchCustomers = AgencyConstants.FETCH_CUSTOMER_QUERY;
			whereClause = "";
			for (int i = 0; i < usernameList.length; i++) {
				whereClause = whereClause + "p.documentType='userPolicyDetails' and pp.requestSource='agency' and pp.userName='" + usernameList[i] + "' OR ";
			}
			whereClause = whereClause.substring(0, whereClause.length() - 4);
			queryForFetchCustomers = queryForFetchCustomers + whereClause;
			this.log.info("Executing queryForFetchCustomers : " + queryForFetchCustomers);
			objectMapper = new ObjectMapper();
			customerList_Array = objectMapper.createArrayNode();
			customerPolicyNode = objectMapper.createObjectNode();
			this.log.info("Executing queryForFetchCustomers : " + queryForFetchCustomers);
			this.log.info("Response for Fetch Proposals : " + transService.executeQuery(queryForFetchCustomers));
			customerPolicyNode = objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchCustomers)));

			if (customerPolicyNode != null) {
				log.info(" Customer Policy Details :" + customerPolicyNode);
				policyNode = customerPolicyNode;
				for (JsonNode policyList: policyNode) {
					try {
						respCustomerNode = objectMapper.createObjectNode();
						if (policyList.has("Name")) respCustomerNode.put("name", policyList.get("Name"));						
						if (policyList.has("businessLineId")) LOB = docConfigNode.get("businessList").get(policyList.get("businessLineId").asText()).asText();
						if (LOB != null) respCustomerNode.put("lineof_business_c", LOB);
						if (policyList.has("mobile")) respCustomerNode.put("mobile_c", policyList.get("mobile"));													
						if (policyList.has("policyNo")) respCustomerNode.put("policynumber_c", policyList.get("policyNo"));
						if (policyList.has("agencyId")) respCustomerNode.put("agencyid_c", policyList.get("agencyId"));
						if (policyList.has("userName")) respCustomerNode.put("agentusername_c", policyList.get("userName"));
						if (policyList.has("emailId")) respCustomerNode.put("useremail_c", policyList.get("emailId"));
						if (policyList.has("policyIssueDate")){ 
							String date=policyList.get("policyIssueDate").asText();
							String date_array[]=date.split("/");
							date=date_array[2]+"/"+date_array[1]+"/"+date_array[0];
							respCustomerNode.put("policyIssueDate_c",date);
							};
						if (policyList.has("secretKey")) respCustomerNode.put("p_key", policyList.get("secretKey"));
						if (policyList.has("u_key")) respCustomerNode.put("u_key", policyList.get("u_key"));
					} catch(NullPointerException e) {
						log.info("NullPointerException : " + e.getMessage());
						e.printStackTrace();
					}
					customerList_Array.add(respCustomerNode);
					customerList_String = objectMapper.writeValueAsString(customerList_Array);
				}
				return customerList_String;
			}
		} catch(Exception e) {
			log.info("Error while executing fetch proposals query" + e);
		}
		return customerList_String;
	}

	public String fetchCustomersForAdmin(String roleName, String agencyId)
	throws SQLException,
	IOException {
		String customerList_String = null;
		ArrayNode customerList_Array = null;
		ObjectMapper objectMapper = null;
		ObjectNode respCustomerNode = null;
		String LOB = null;
		String queryForFetchCustomers = "";
		if (roleName.equalsIgnoreCase("NIBPL_ADMIN")) {
			queryForFetchCustomers = AgencyConstants.FETCH_CUSTOMER_BY_SU_ADMIN;
		} else {
			queryForFetchCustomers = AgencyConstants.FETCH_CUSTOMER_BY_AGENCY_ADMIN;
			queryForFetchCustomers = queryForFetchCustomers.replace("{1}", "'" + agencyId + "%'");
		}
		try {
			objectMapper = new ObjectMapper();
			customerList_Array = objectMapper.createArrayNode();
			customerPolicyNode = objectMapper.createObjectNode();
			this.log.info("Executing query For Fetch Customers : " + queryForFetchCustomers);
			this.log.info("Response for Fetch Customers : " + transService.executeQuery(queryForFetchCustomers));
			customerPolicyNode = objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchCustomers)));
			if (customerPolicyNode != null) {
				for (JsonNode policyList: customerPolicyNode) {
					try {
						respCustomerNode = objectMapper.createObjectNode();
						if (policyList.has("Name")) respCustomerNode.put("name", policyList.get("Name"));
						if (policyList.has("businessLineId")) LOB = docConfigNode.get("businessList").get(policyList.get("businessLineId").asText()).asText();
						if (LOB != null) respCustomerNode.put("lineof_business_c", LOB);
						if (policyList.has("mobile")) respCustomerNode.put("mobile_c", policyList.get("mobile"));									
						if (policyList.has("policyNo")) respCustomerNode.put("policynumber_c", policyList.get("policyNo"));
						if (policyList.has("agencyId")) respCustomerNode.put("agencyid_c", policyList.get("agencyId"));
						if (policyList.has("userName")) respCustomerNode.put("agentusername_c", policyList.get("userName"));
						if (policyList.has("emailId")) respCustomerNode.put("useremail_c", policyList.get("emailId"));
						if (policyList.has("policyIssueDate")){ 
							String date=policyList.get("policyIssueDate").asText();
							String date_array[]=date.split("/");
							date=date_array[2]+"/"+date_array[1]+"/"+date_array[0];
							respCustomerNode.put("policyIssueDate_c",date);
							};
						if (policyList.has("secretKey")) respCustomerNode.put("p_key", policyList.get("secretKey"));
						if (policyList.has("u_key")) respCustomerNode.put("u_key", policyList.get("u_key"));
					} catch(NullPointerException e) {
						log.info("NullPointerException : " + e.getMessage());
						e.printStackTrace();
					}
					customerList_Array.add(respCustomerNode);
					customerList_String = objectMapper.writeValueAsString(customerList_Array);
				}
				return customerList_String;
			}
		} catch(Exception e) {
			log.info("Error while executing fetch customers query" + e);
		}
		return customerList_String;
	}

	public String fetchCustList(Connection crmCon, Connection con, String username, int roleId, String agencyId) throws SQLException,
	IOException {
		String rolename = getRole(con, roleId);
		log.info("role : " + rolename);
		Logger log = Logger.getLogger(MiscAccess.class.getName());
		String customerList_String = new String();
		props.load( in );
		PreparedStatement custListstmt1 = null;
		String userNameList[];
		try {
			if (rolename.equals("NIBPL_ADMIN")) {
				customerList_String = fetchCustomersForAdmin(rolename, agencyId);
				return customerList_String;
			} else if (rolename.equals("AGENCY_ADMIN")) {
				String str = agencyId.substring(0, 9);
				log.info("agencyid" + str);
				customerList_String = fetchCustomersForAdmin(rolename, str);
				return customerList_String;
			} else if (rolename.equals("MANAGER")) {
				String fetchCustForManager = props.getProperty("fetchCustForManager");
				userNameList = null;
				custListstmt1 = con.prepareStatement(fetchCustForManager);
				custListstmt1.setString(1, username);
				custListstmt1.setString(2, username);
				log.info("custListstmt1 query" + custListstmt1);
				ResultSet rs1 = custListstmt1.executeQuery();
				int i = 0;
				if (rs1.last()) {
					userNameList = new String[rs1.getRow() + 1];
					userNameList[i] = username;
					i++;
					rs1.beforeFirst();
				} else {
					userNameList = new String[1];
					userNameList[0] = username;
				}
				while (rs1.next()) {
					userNameList[i] = rs1.getString("Email");
					log.info("userNameList[i]  " + userNameList[i]);
					i++;
				}
				String str = agencyId.substring(0, 9);
				log.info("agencyid" + str);
				customerList_String = fetchCustomers(userNameList, str);
				log.info("return custListtest :" + customerList_String);
				return customerList_String;
			} else if (rolename.equals("TEAM_LEAD")) {
				String fetchCustListForTeamLead = props.getProperty("fetchCustListForTeamLead");
				userNameList = null;
				custListstmt1 = con.prepareStatement(fetchCustListForTeamLead);
				custListstmt1.setString(1, username);
				log.info("fetchCustListForTeamLead query " + custListstmt1);
				ResultSet rs1 = custListstmt1.executeQuery();
				int i = 0;
				if (rs1.last()) {
					userNameList = new String[rs1.getRow() + 1];
					userNameList[i] = username;
					i++;
					rs1.beforeFirst();
				} else {
					userNameList = new String[1];
					userNameList[0] = username;
				}

				while (rs1.next()) {
					userNameList[i] = rs1.getString("Email");
					log.info("userNameList[i]  " + userNameList[i]);
					i++;
				}
				String str = agencyId.substring(0, 9);
				log.info("agencyid" + str);

				customerList_String = fetchCustomers(userNameList, str);
				log.info("Response customerList_String :" + customerList_String);
				return customerList_String;
			} else if (rolename.equals("AGENT")) {
				log.info("Agent inside" + username);
				userNameList = new String[] {
					username
				};
				customerList_String = fetchCustomers(userNameList, agencyId);
				log.info("return custListtest :" + customerList_String);
				return customerList_String;
			} else {
				log.info("INVALID");
			}
		} catch(NullPointerException e) {
			log.info(e.getMessage());
			e.printStackTrace();
		} catch(Exception e) {
			log.info("Error in preparedstatement..!!");
			e.printStackTrace();
		}

		return customerList_String;
	}

}