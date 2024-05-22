package com.idep.leadutility.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/leadutility")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)

public class LeadUtilityService {
	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/importleads/integrate/invoke")
	public String importLeads(String string)
	{
		return null;
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/getLeadFormData/integrate/invoke")
	public String getLeadFormData(String string)
	{
		return null;
	}
}
