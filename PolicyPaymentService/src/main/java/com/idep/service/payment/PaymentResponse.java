 package com.idep.service.payment;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.OPTIONS;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
 
 @Path("/service")
 @CrossOriginResourceSharing(allowAllOrigins = true, allowCredentials = true, allowHeaders = {"origin", "content-type", "accept", "authorization"}, maxAge = 1209600)
 public class PaymentResponse {
   @POST
   @Consumes({"application/x-www-form-urlencoded"})
   @Path("/success")
   public Response paymentMasterResponseSuccessPOST(MultivaluedMap<String, String> formParams, @PathParam("carrier") String carrier, @PathParam("lob") String lob, @Context UriInfo info) {
     return null;
   }
   
   @POST
   @Consumes({"application/x-www-form-urlencoded"})
   @Path("/success/{carrier}/{lob}")
   public Response paymentResponseSuccessPOST(MultivaluedMap<String, String> formParams, @PathParam("carrier") String carrier, @PathParam("lob") String lob, @Context UriInfo info) {
     return null;
   }
   
   @GET
   @Path("/success")
   public String paymentMasterResponseSuccessGET(@Context UriInfo info, @PathParam("carrier") String carrier, @PathParam("lob") String lob) {
     return null;
   }
   
   @GET
   @Path("/success/{carrier}/{lob}")
   public String paymentResponseSuccessGET(@Context UriInfo info, @PathParam("carrier") String carrier, @PathParam("lob") String lob) {
     return null;
   }
   
   @POST
   @Consumes({"application/x-www-form-urlencoded"})
   @Path("/failure")
   public String paymentMasterResponseFailurePOST(MultivaluedMap<String, String> formParams, @PathParam("carrier") String carrier, @PathParam("lob") String lob, @Context UriInfo info) {
     return null;
   }
   
   @POST
   @Consumes({"application/x-www-form-urlencoded"})
   @Path("/failure/{carrier}/{lob}")
   public String paymentResponseFailurePOST(MultivaluedMap<String, String> formParams, @PathParam("carrier") String carrier, @PathParam("lob") String lob, @Context UriInfo info) {
     return null;
   }
   
   @GET
   @Path("/failure")
   public String paymentMasterResponseFailureGET(@Context UriInfo info, @PathParam("carrier") String carrier, @PathParam("lob") String lob) {
     return null;
   }
   
   @GET
   @Path("/failure/{carrier}/{lob}")
   public String paymentResponseFailureGET(@Context UriInfo info, @PathParam("carrier") String carrier, @PathParam("lob") String lob) {
     return null;
   }
   
   @OPTIONS
   @Path("{path : .*}")
   public Response options() {
     return Response.ok("")
       .header("Access-Control-Allow-Origin", "*")
       .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
       .header("Access-Control-Allow-Credentials", "true")
       .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
       .header("Access-Control-Max-Age", "1209600")
       .build();
   }
 }


