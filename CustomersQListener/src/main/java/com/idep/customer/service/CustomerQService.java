package com.idep.customer.service;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.log4j.Logger;

@Path("/customerqueueservice")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class CustomerQService
{
	Logger log = Logger.getLogger(CustomerQService.class.getName());
	
	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@Path("/createcustomer/integrate/invoke")
	public String collectCustomerData(String customerData)
	{
		return customerData;
	}
}
