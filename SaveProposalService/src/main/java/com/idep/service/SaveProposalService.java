package com.idep.service;

import javax.ws.rs.Consumes;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;


@Path("/service")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class SaveProposalService {
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/saveProposal/integrate/invoke")
	public String saveProposal(String input){
		return null;
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/submitProposal/integrate/invoke")
	public String submitProposal(String input){
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
