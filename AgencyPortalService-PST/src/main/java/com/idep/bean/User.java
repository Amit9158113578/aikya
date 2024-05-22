package com.idep.bean;

public class User {
		
		private String Id;
		private String username;
		private String pass;
		private String isActive;
		private long roleId;
		private String agencyId;
		private String lob;
		private String userImage;
		
		public String getId() {
			return Id;
		}
		public void setId(String id) {
			Id = id;
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
		public String getIsActive() {
			return isActive;
		}
		public void setIsActive(String isActive) {
			this.isActive = isActive;
		}
		public long getRoleId() {
			return roleId;
		}
		public void setRoleId(long roleId) {
			this.roleId = roleId;
		}
		public String getAgencyId() {
			return agencyId;
		}
		public void setAgencyId(String agencyId) {
			this.agencyId = agencyId;
		}
		public String getLob() {
			return lob;
		}
		public void setLob(String lob) {
			this.lob = lob;
		}
		public String getUserImage() {
			return userImage;
		}
		public void setUserImage(String userImage) {
			this.userImage = userImage;
		}

}
