/**
 * 
 */
package com.idep.insurancerecommender.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

/**
 * @author deepak.surapaneni
 *
 */
@Path("/predict")
@CrossOriginResourceSharing(allowAllOrigins = true,allowCredentials=true,allowHeaders={"origin", "content-type", "accept", "authorization"},
maxAge=1209600)
public class InsuranceRecService {
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/getInsuranceRecommendation")
	public String getFeatureWeights(String quoteparam)
	{
		return null;
	}

}
