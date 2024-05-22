package com.idep.service;

import javax.ws.rs.Consumes;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;


@Path("/AgencyService")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=1209600)
public class AgencyService {

	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/user/integrate/invoke")
	public Response getUserDetails(String UserId)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/login/integrate/invoke")
	public String validateUser(String credential)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/fetch/integrate/invoke")
	public String fetchDetails(String role)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/agency/integrate/invoke")
	public String agencyMaint(String input)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/agent/integrate/invoke")
	public String agentMaint(String input)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/activity/integrate/invoke")
	public String activityMaint(String input)
	{
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/forgotpass/integrate/invoke")
	public String forgotPass(String input)
	{
		return null;
	}
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/createtoken/integrate/invoke")
	public String createToken(String input)
	{
		return input;
	}
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/verifytoken/integrate/invoke")
	public String verifyToken(String input)
	{
		return input;
	}
	
	@OPTIONS
	@Path("{path : .*}")
	public Response options()
	{
		return 
	    
	    Response.ok("").header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization").header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD").header("Access-Control-Max-Age", "1209600").build();
	}
}
