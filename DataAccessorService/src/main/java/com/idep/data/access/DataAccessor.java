package com.idep.data.access;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
/**
 * 
 * @author sandeep.jadhav
 * web methods configuration
 */
@Path("/operations")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class DataAccessor
{
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/rcouchdb/integrate/invoke")
  public String readCouchDB(String data)
  {
    System.out.println("this is interfacing class");
    return null;
  }
  
  @POST
  @Produces({"application/json"})
  @Path("/wcouchdb/integrate/invoke")
  public String writeCouchDB(String data)
  {
    return null;
  }
  
  @POST
  @Produces({"application/json"})
  @Path("/mcouchdb/integrate/invoke")
  public String couchDBOperation(String data)
  {
    return null;
  }
  
  @POST
  @Produces({"application/json"})
  @Path("/wadminappcouchdb/integrate/invoke")
  public String writeAdminAppDB(String data)
  {
    return null;
  }
  
  @POST
  @Produces({"application/json"})
  @Path("/radminappcouchdb/integrate/invoke")
  public String readAdminAppDB(String data)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/productreadcouchdb/integrate/invoke")
  public String readProductData(String data)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/get/idvrange/integrate/invoke")
  public String getIDVRange(String data)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/get/policytransdata/integrate/invoke")
  public String readPolicyTransData(String data)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/get/quotereqresdata/integrate/invoke")
  public String readQuoteData(String data)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/get/productplanriders/integrate/invoke")
  public String getProductPlanRiders(String input)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/get/getPopularRTO/integrate/invoke")
  public String getPopularRTO(String input)
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
