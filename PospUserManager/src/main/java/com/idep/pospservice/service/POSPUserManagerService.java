package com.idep.pospservice.service;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/posp")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=1209600)
public class POSPUserManagerService {
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/adminProfileConfig/integrate/invoke")
	public String getAdminProfileConfig(String profileDetails)
	{
		return null;
	}
	
	
	@POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  @Path("/approve/admin/integrate/invoke")
	  public String  approveAdmin(String Request)
	  {
		  return null;
	  }
	
	@POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  @Path("/getscreenmenuconfig/integrate/invoke")
	  public String  getScreenMenuConfig(String Request)
	  {
		  return null;
	  }
	
	@POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  @Path("/createRoleConfig/integrate/invoke")
	  public String  createRoleConfig(String Request)
	  {
		  return null;
	  }
	
		@POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  @Path("/createGroupConfig/integrate/invoke")
	  public String  createGroupConfig(String Request)
	  {
		  return null;
	  }
	
		@POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  @Path("/getuserconfig/integrate/invoke")
	  public String  getConfigDoc(String Request)
	  {
		  return null;
	  }
	
		
		  @POST
		  @Consumes(MediaType.APPLICATION_JSON)
		  @Produces(MediaType.APPLICATION_JSON)
		  @Path("/addscreenconfig/integrate/invoke")
		  public String  addScreenConfig(String Request)
		  {
			  return null;
		  }
	

		  @POST
		  @Consumes(MediaType.APPLICATION_JSON)
		  @Produces(MediaType.APPLICATION_JSON)
		  @Path("/userappconfig/integrate/invoke")
		  public String  getUserAppConfig(String Request)
		  {
			  return null;
		  }
		  
		  
		  @POST
		  @Consumes(MediaType.APPLICATION_JSON)
		  @Produces(MediaType.APPLICATION_JSON)
		  @Path("/userlogout/integrate/invoke")
		  public String  UserLogout(String Request)
		  {
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
