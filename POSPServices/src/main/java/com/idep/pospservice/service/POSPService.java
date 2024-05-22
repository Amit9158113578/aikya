package com.idep.pospservice.service;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
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
public class POSPService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/validate/otp/integrate/invoke")
	public String validateOTP(String mobileNumber)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getData/integrate/invoke")
	public String getData(String data)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/agentProfileConfig/integrate/invoke")
	public String getAgentProfileConfig(String profileDetails)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/userAuthentication/integrate/invoke")
	public String getUserAuthDetails(String profileDetails)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/adminlogin/integrate/invoke")
	public String admiLogin(String request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/uploaddoc/integrate/invoke")
	public String  uploadPospDocument(@Multipart(value="proofDocument") Attachment documentCntect,@Multipart(value="dataParam") String requestJson)
	{
		return null;
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/approve/agent/integrate/invoke")
	public String  approveAgent(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update/agent/integrate/invoke")
	public String  updateAgentDoc(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/fetch/leads/integrate/invoke")
	public String  fetchLeads(String Request)
	{
		return null;
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/fetch/proposals/integrate/invoke")
	public String  fetchProposals(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getadmindetails/integrate/invoke")
	public String  getAdminList(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getpospdocurl/integrate/invoke")
	public String  GenerateDocDownloadUrl(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/fetch/customerLeads/integrate/invoke")
	public String  fetchCustLeads(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getdashboardinfo/integrate/invoke")
	public String  getDashBoardInfo(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/createticket/integrate/invoke")
	public String createPOSPTicket(String request)
	{
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/gettickets/integrate/invoke")
	public String getPOSPTickets(String request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getmoodlesession/integrate/invoke")
	public String  getMoodleSession(String Request)
	{
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/updatePospTicket/integrate/invoke")
	public String updatePospTicket(String request)
	{
		return null;
	}
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getUserToken/integrate/invoke")
	public String getUserToken(String data)
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
