package com.idep.couchbase.api.util;

/*
* This is a bean class to set couchbase config parameters
* 
* @author  Sandeep Jadhav
* @version 1.0
* @since   2016-01-04
*/
public class Database {
	
	private String bucket;
	private String password;

	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
