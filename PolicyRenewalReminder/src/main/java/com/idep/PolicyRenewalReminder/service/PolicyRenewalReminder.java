package com.idep.PolicyRenewalReminder.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

@Path("/policyrenewal")
@CrossOriginResourceSharing(allowAllOrigins=true, allowCredentials=true, allowHeaders={"origin", "content-type", "accept", "authorization"}, maxAge=1209600)
public class PolicyRenewalReminder
{
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/renewalpolicyemail/integrate/invoke")
  public String sendRenewalEmail(String s1)
  {
    return null;
  }

  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/renewalpolicydata/integrate/invoke")
  public String getProposalData(String s1)
  {
    return null;
  }

  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/offlinepolicyrenewal/integrate/invoke")
  public String offlineRenewalReminder(String s1)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/readEmailStatus/integrate/invoke")
  public String getEmailStatus(String s1)
  {
    return null;
  }
  
  @POST
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @Path("/runOfflineRenewalManual/integrate/invoke")
  public String runOfflineRenewal(String data)
  {
    return null;
  }
  
}


