package com.idep.lifequote.util;

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