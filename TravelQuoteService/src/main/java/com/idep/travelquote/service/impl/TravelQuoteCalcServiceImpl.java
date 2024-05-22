package com.idep.travelquote.service.impl;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelQuoteCalcServiceImpl{
	Logger log = Logger.getLogger(TravelQuoteCalcServiceImpl.class);
	ObjectMapper objectMapper = new ObjectMapper();
	public String calculateQuote(String quotedata){
		try{
		JsonNode uiInputReqNode = objectMapper.readTree(quotedata);
		log.info("quoteData recieved from UI : " + uiInputReqNode );
		}catch(Exception ex){
			log.info("Read tree Exception : " , ex);
		}
		return quotedata;
	}
}