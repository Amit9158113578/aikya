package com.idep.smsemail.request.bean;

import java.util.HashMap;


public class MessageDetails {
	
	private String username;
	private String funcType;
	HashMap<String,String> paramMap;
	
	
	public HashMap<String, String> getParamMap() {
		return paramMap;
	}
	public void setParamMap(HashMap<String, String> paramMap) {
		this.paramMap = paramMap;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFuncType() {
		return funcType;
	}
	public void setFuncType(String funcType) {
		this.funcType = funcType;
	}
	
	
	
	
	
	

}
