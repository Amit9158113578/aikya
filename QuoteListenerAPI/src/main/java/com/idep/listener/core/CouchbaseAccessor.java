package com.idep.listener.core;

import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;



public class CouchbaseAccessor{

	private static final CBService serverConfigInstance = CBInstanceProvider.getServerConfigInstance();
	private static final CBService productDataInstance = CBInstanceProvider.getProductConfigInstance();
	
	public static CBService getServerConfigInstance() {
		return serverConfigInstance;
	}
	
	public static CBService getProductDataInstance() {
		return productDataInstance;
	}

}
