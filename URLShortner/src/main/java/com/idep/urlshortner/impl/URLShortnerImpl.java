package com.idep.urlshortner.impl;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class URLShortnerImpl {
	Logger log = Logger.getLogger(URLShortnerImpl.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	public String getShortURL(String request){
		return request;
	} 
}
