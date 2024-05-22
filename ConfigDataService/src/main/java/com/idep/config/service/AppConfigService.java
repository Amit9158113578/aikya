package com.idep.config.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/config")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class AppConfigService
{
  @GET
  @Produces({"application/json"})
  @Path("/getconfigdata")
  public String getConfigGETData(@Context UriInfo info)
  {
    return null;
  }
  
  @GET
  @Produces({"application/json"})
  @Path("/getaddress")
  public String getAddressDetails(@Context UriInfo info)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/getdata/integrate/invoke")
  public String getConfigData(String riderparam)
  {
    return null;
  }
  
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/getappdata/integrate/invoke")
  public String getApplicationData(String appData)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/getview/integrate/invoke")
  public String getViewData(String viewname)
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
