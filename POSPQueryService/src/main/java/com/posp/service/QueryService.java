package com.posp.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/query")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class QueryService
{
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/getApplicationData/integrate/invoke")
  public String getApplicationData(String appData)
  {
    return null;
  }
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/businessOperation/integrate/invoke")
  public String businessOperation(String appData)
  {
    return null;
  }
  @OPTIONS
  @Path("{path : .*}")
  public Response options()
  {
    return 
    
      Response.ok("").header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").header("Access-Control-Max-Age", "1209600").build();
  }
}
