package com.idep.data.searchconfig.cache;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Test {

	public static void main(String[] args) {

		ObjectMapper objMapper = new ObjectMapper();
		ObjectNode obj = objMapper.createObjectNode();
		obj.put("city", "chennai");
		HashMap<String,Object> hs =  new HashMap<String,Object>();
		hs.put("TN01", obj);
		
		String search = "TN";
		
		for(Map.Entry<String,Object> entrySet : hs.entrySet())
		{
			if(entrySet.getKey().contains(search))
			{
				System.out.println("true");
			}
		}
		
	}

}
