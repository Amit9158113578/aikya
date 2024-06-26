 package com.idep.soap.util;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 
 public class SoapUtils {
   public static final ObjectMapper objectMapper = new ObjectMapper();
   
   public static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   public static CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
 }


