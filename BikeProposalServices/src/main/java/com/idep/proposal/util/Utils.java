 package com.idep.proposal.util;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 
 public class Utils {
   public static final ObjectMapper mapper = new ObjectMapper();
   
   public static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   public static CBService policyTrans = CBInstanceProvider.getPolicyTransInstance();
 }


