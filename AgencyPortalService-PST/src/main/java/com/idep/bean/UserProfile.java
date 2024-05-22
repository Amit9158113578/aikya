package com.idep.bean;


public class UserProfile {
	private String name;
	private String agentId;
	private String agencyName;
	private String agencyId;
	private String email;
	private long mobile;
	private String address;
	private String username;
	private String pass;
	private String designation;
	private String reportingTo;
	private String userImage;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public String getAgencyName() {
		return agencyName;
	}
	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}
	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public long getMobile() {
		return mobile;
	}
	public void setMobile(long mobile) {
		this.mobile = mobile;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getReportingTo() {
		return reportingTo;
	}
	public void setReportingTo(String reportingTo) {
		this.reportingTo = reportingTo;
	}
	public String getUserImage() {
		return userImage;
	}
	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}
	@Override
	public String toString() {
		return "UserProfile [name=" + name + ", agentId=" + agentId
				+ ", agencyName=" + agencyName + ", agencyId=" + agencyId
				+ ", email=" + email + ", mobile=" + mobile + ", address="
				+ address + ", username=" + username + ", pass=" + pass
				+ ", designation=" + designation + ", reportingTo="
				+ reportingTo + ", userImage=" + userImage + "]";
	}
}
