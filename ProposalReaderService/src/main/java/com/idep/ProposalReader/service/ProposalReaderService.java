/**
 * 
 */
package com.idep.ProposalReader.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

/**
 * @author sayli.boralkar
 *
 */
@Path("/calculate")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=1209600)

public class ProposalReaderService {
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/findproposaldata/integrate/invoke")
	public String FindProposalData(String s1)
	{
		
		return null;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/readproposaldetails/integrate/invoke")
	
	public String readProposalDetails(String request)
	{
		
		return null;
	}

}
