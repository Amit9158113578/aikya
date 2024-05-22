package com.idep.travelquote.util;

import java.util.UUID;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class CorrelationKeyGenerator{
	public UUID getUniqueKey(){
		UUID uniqueKey = UUID.randomUUID();
		return uniqueKey;
	}
}