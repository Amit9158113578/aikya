package com.idep.PBQService;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;



@Path("/calculate")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=1209600)
public class PBQuoteService {

	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/pbqlifequote/integrate/invoke")
	public String calculatePBQLifeQuote(String quoteparam)
	{
		return null;
	}

	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/pbqcarquote/integrate/invoke")
	public String calculatePBQCarQuote(String quoteparam)
	{
		return null;
	}
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/pbqbikequote/integrate/invoke")
	public String calculatePBQBikeQuote(String quoteparam)
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
