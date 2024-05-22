package com.idep.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/message")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class SMSEmailService
{
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/forgotuser/integrate/invoke")
  public String forgotUsername(String mobileNumber)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/login/integrate/invoke")
  public String loginRequest(String msgdetails)
  {
    return null;
  }
 
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/sms/integrate/invoke")
  public String sendSMSRequest(String msgdetails)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/email/integrate/invoke")
  public String sendEmailRequest(String emaildetails)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/notification/integrate/invoke")
  public String sendNotifications(String notifdetails)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/viewreport/integrate/invoke")
  public String viewReport(String reportDetails)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/contactUs/integrate/invoke")
  public String contactUSInfo(String userdetails)
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
