package com.idep.sugar.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/crmservice")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class SugarCRMService
{
	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/createlead/integrate/invoke")
	public String collectLeadData(String leadData)
	{
		return null;
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/ticket/createticket/integrate/invoke")
	public String collectTicketData(String ticketData)
	{
		return null;
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/read/proposal/integrate/invoke")
	public String getLeadForProposal(String Data)
	{
		return null;

	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/url/updateURL/integrate/invoke")
	public String collectURL(String Data)
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
