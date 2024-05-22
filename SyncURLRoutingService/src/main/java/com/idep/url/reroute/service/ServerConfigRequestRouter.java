package com.idep.url.reroute.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=100000)
public class ServerConfigRequestRouter {
	
	 @GET
	   @Path("/{param1}")
	   @Produces(MediaType.APPLICATION_JSON)
	    public String singleParamGetRequest(String data)
	    {
	    	return null;
	    	
	    }
	   @GET
	   @Path("/{param1}/{param2}")
	   @Produces(MediaType.APPLICATION_JSON)
	    public String twoParamGetRequest(String data)
	    {
	    	return null;
	    	
	    }
	   
	   @POST
	   @Path("/{param1}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String singleParamPostRequest(String data)
	    {
	    	return null;
	    	
	    }
	   @POST
	   @Path("/{param1}/{param2}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String twoParamPostRequest(String data)
	    {
	    	return null;
	    	
	    }
	   
	   @PUT
	   @Path("/{param1}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String singleParamPutRequest(String data)
	    {
	    	return null;
	    	
	    }
	   @PUT
	   @Path("/{param1}/{param2}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String twoParamPutRequest(String data)
	    {
	    	return null;
	    	
	    }
	   @DELETE
	   @Path("/{param1}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String singleParamDeleteRequest(String data)
	    {
	    	return null;
	    	
	    }
	   
	   @DELETE
	   @Path("/{param1}/{param2}")
	   @Consumes(MediaType.APPLICATION_JSON)
	   @Produces(MediaType.APPLICATION_JSON)
	    public String twoParamDeleteRequest(String data)
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
	                .header("Access-Control-Max-Age", "10000")
	                .build();
	    }

}
