 package com.idep.Insuranceapi.service;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
 
 
 @Path("/request")
 @CrossOriginResourceSharing(allowAllOrigins = true, allowCredentials = true, allowHeaders = {"origin", "content-type", "accept", "authorization"}, maxAge = 1209600)
 public class InsuranceService
 {
   @POST
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @Path("/integrate/invoke")
   public String calculateBikeQuote(String quoteparam) {
     return null;
   }
 }


