package com.idep.geolocator.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GeoLocatorService {
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getgeolocation/integrate/invoke")
	public String geolocatorRequestPOST(String formParams){
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
