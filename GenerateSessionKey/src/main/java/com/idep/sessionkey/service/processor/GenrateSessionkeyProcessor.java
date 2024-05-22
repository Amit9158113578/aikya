package com.idep.sessionkey.service.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.sessionkey.service.util.GenrateSessionKeyConstant;


public class GenrateSessionkeyProcessor implements Processor {

	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GenrateSessionkeyProcessor.class.getName());
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	JsonNode keyConfigDoc = null;
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			JsonNode inputReq=objectMapper.readTree(exchange.getIn().getBody(String.class));
			
			
			if(inputReq.has("documentType") && inputReq.get("documentType").asText().equalsIgnoreCase("getSession")){
				
			
			JsonNode responseNode = objectMapper.createObjectNode();
				keyConfigDoc = objectMapper.readTree(serverConfigService.getDocBYId("encryptionPrivateKeyConfig").content().toString());
								 
				 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYhh:mm:ss");
					/**
					 * 
					 * 
					 * */
					String genratedValue=simpleDateFormat.format(new Date())+keyConfigDoc.get(GenrateSessionKeyConstant.CONCATKEY).asText()+generateRandomPin();
					log.info("genrated value : "+genratedValue);
					String sessionId =GenrateEncryptionKey.GetEncryptedKey(genratedValue, keyConfigDoc.get(GenrateSessionKeyConstant.ENCKEY).asText());
					log.info("genrated SessionId : "+sessionId);
			((ObjectNode)responseNode).put("sessionId", sessionId);
			exchange.getIn().setBody(responseNode);
			}else{
				log.error("unable to genarte session key documentType not found ");
			}
		}catch(Exception e){
			log.error("unable to genarte session key : ",e);
		}
		
	}
	
/*	public static void main(String[] args) {
		
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMYYhh:mm:ss");
		
		String currentDate= simpleDateFormat.format(new Date());
		String genratedValue=currentDate+"_policies365_"+generateRandomPin();
		String enc= GenrateEncryptionKey.GetEncryptedKey(genratedValue, "G54tFnxOmx5eL1QORCs5g0h@$#tuYfV_");
		System.out.println("ECN : "+enc);
		System.out.println("Dec : "+GenrateEncryptionKey.GetPlainText(enc, "G54tFnxOmx5eL1QORCs5g0h@$#tuYfV_"));
	}*/
	
	public static synchronized  long generateRandomPin(){

	    int START =110;
	    //int END = Integer.parseInt("9999999999");
	    //long END = Integer.parseInt("9999999999");
	    long END = 99999999L;

	    Random random = new Random(System.nanoTime());
	    return createRandomInteger(START, END, random);
	}
	
	private static long createRandomInteger(int aStart, long aEnd, Random aRandom){
	    if ( aStart > aEnd ) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = aEnd - (long)aStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    long fraction = (long)(range * aRandom.nextDouble());
	    long randomNumber =  fraction + (long)aStart;    
	    System.out.println("Genrtaed ranodm Nmber : "+randomNumber);
	    return randomNumber;
	  }

	
	
}
