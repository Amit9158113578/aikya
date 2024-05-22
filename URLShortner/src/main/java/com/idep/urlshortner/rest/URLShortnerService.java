package com.idep.urlshortner.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/urlshortner")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class URLShortnerService {
	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/getshorturl/integrate/invoke")
	public String getShortURL(String leadData){
		return null;
	}

}
