package com.idep.geolocator.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.geolocator.util.GeoLocatorConstants;

public class GeoLocator implements Processor{
	Logger log = Logger.getLogger(GeoLocator.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		log.info("Inside P365 Geo-Locator Service.");
		String geoLocatorRequest = null;
		URL vurll;
		JsonNode geoLocatorInfo;
		
		try{
			geoLocatorRequest = exchange.getIn().getBody(String.class);
			geoLocatorInfo = this.objectMapper.readTree(geoLocatorRequest);
			
			String latitude = geoLocatorInfo.get(GeoLocatorConstants.LATITUDE).asText();
			String longitude = geoLocatorInfo.get(GeoLocatorConstants.LONGITUDE).asText();
			
			String endPoint = GeoLocatorConstants.GEOAPI;
			endPoint = endPoint.replace(GeoLocatorConstants.LATITUDE, latitude);
			endPoint = endPoint.replace(GeoLocatorConstants.LONGITUDE, longitude);
			
			vurll = new URL(endPoint);
			HttpURLConnection conn= (HttpURLConnection) vurll.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			StringBuffer buffer = new StringBuffer();
			
			while((output = br.readLine()) != null){
				buffer.append(output);
			}
			
			JsonNode geoLocationInfo = this.objectMapper.readTree(buffer.toString());
			
			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(GeoLocatorConstants.RES_CODE, 1000);
			finalresultNode.put(GeoLocatorConstants.RES_MSG, "sucess");
			finalresultNode.put(GeoLocatorConstants.RES_DATA, geoLocationInfo);
			
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(finalresultNode.toString()));
		}catch (MalformedURLException e){
			e.printStackTrace();
		}catch (ProtocolException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}