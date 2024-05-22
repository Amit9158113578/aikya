 package com.idep.profession.DBCache;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 
 
 
 public class PBJDBCacheConfigProcessor
 {
   static ObjectNode pincodeList = null;
   static {
     Logger log = Logger.getLogger(PBJDBCacheConfigProcessor.class.getName());
     
     try {
       ObjectMapper objectMapper = new ObjectMapper();
       pincodeList = objectMapper.createObjectNode();
       CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
       
       log.info("Pincode Data Caching  processing Started for PBJ Request");
       
       log.info("PBJ Request Pincode Data Caching  process Completed ");
     } catch (Exception e) {
       log.error("unable to fech pincode  List PBJ : ", e);
     } 
   }
 
   
   public static ObjectNode getPincodeList() {
     return pincodeList;
   }
 }