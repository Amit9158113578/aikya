package com.idep.proposal.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author yogesh.shisode
 * @version 1.0
 * @since   23-MAR-2018
 */
public class LifePolicyCreatorImpl {
	ObjectMapper objectMapper = new ObjectMapper();

	public String createLifePolicy(String policy){
		JsonNode reqNode =null;
		try {
			reqNode = this.objectMapper.readTree(policy);
			return reqNode.toString();
		}catch(Exception e){
			return reqNode.toString();
		}
	}

	public String sendMessage(String proposal){
		return proposal;
	}
}