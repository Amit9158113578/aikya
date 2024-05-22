package com.idep.service.vehicleinfo.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class DBStoreReqResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(DBStoreReqResProcessor.class.getName());
	CBService policyTransaction =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonObject docObj = JsonObject.fromJson(objectMapper.readTree(inputReq).toString());
			JsonDocument carDocument=null;
	        String docId=exchange.getProperty(VehicleInfoConstant.VEHICALENUMBER).toString();
	        try{
	        	carDocument  =  policyTransaction.getDocBYId(docId);
	        }catch(Exception e){
	        	log.error("Failed to read Car RTO Resposne Document  :  "+docId,e);
	        }
	        if(carDocument == null)
	        {
	        	/**
	        	 * create Car RTo details  document using  registrationNumber
	        	 *
	        	 **/
	        	try{
	        		
	        		Date currentDate = new Date();
	        		docObj.put("responseRecivedDate", dateFormat.format(currentDate));
	        		policyTransaction.createDocument(docId, docObj);
	        		log.info(" Car RTO Resposne document   Crerated :  "+docId);
	        	}catch(Exception e){
	        		log.error("Failed to Create Car RTO Resposne Document  :  "+docId,e);
	        	}
	        }
	        else
	        {
	        	try{
	        		/****
	        		 * Adding current date and time
	        		 * */
	        		Date currentDate = new Date();
	        		docObj.put("responseUpdatedDate", dateFormat.format(currentDate));
	        		/**
		        	 * update Car RTo details  document if already exist
		        	 */
	        		JsonObject documentContent = carDocument.content().put(VehicleInfoConstant.UIRESPONSE,docObj.get(VehicleInfoConstant.UIRESPONSE));
	        		JsonObject documentContent2 = documentContent.put("registrationAPIResponse",docObj.get("registrationAPIResponse"));
	        		String doc_status = policyTransaction.replaceDocument(docId, documentContent2);
	 	       		this.log.info("Car RTO Resposne document updated : "+docId+" : "+doc_status);
	        	}catch(Exception e){
	        		log.error("Failed to Update Car RTO Resposne Document  :  "+docId,e);
	        	}
	        }
		}
		catch(Exception e)
		{
			log.error("Exception while creating or updating Car RTO Resposne : ",e);
		}
		
	}

}
