package com.idep.healthquote.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class MapperHelper {
	
	
	public void findsumInsured(long sumInsured,String clientAttr,ObjectNode clientReqNode, ArrayNode arrNode)
	{

		for(JsonNode node : arrNode)
		{
			if(sumInsured>=node.get(HealthQuoteConstants.START_VALUE).longValue()&&
			   sumInsured<=node.get(HealthQuoteConstants.END_VALUE).longValue())
			{
				clientReqNode.put(clientAttr, node.get(HealthQuoteConstants.SUM_INSURED_ID).intValue());
				break;
			}
		}
		
	}

}
