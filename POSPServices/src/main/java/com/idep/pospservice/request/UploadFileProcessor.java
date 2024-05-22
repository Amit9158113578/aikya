package com.idep.pospservice.request;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.Functions;
import com.idep.services.impl.UploadDocument;

/***
 * this Processor  is  used for upload images/png from client side file to server dir . 
 * 
 * ***/

public class UploadFileProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UploadFileProcessor.class.getName());
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	Functions pcf =  new Functions();
	UploadDocument ud = new UploadDocument();
	DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
	static JsonDocument jsonDocument;
	static{
		jsonDocument = PospData.getDocBYId("PospContentManagementConfig");	
	}
	
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			log.info("class in exchnage bod recived Processing  started ");
			Message inMessage = exchange.getIn();    
			String fileLocation =null;
			if(jsonDocument==null){
				jsonDocument = PospData.getDocBYId("PospContentManagementConfig");	
			}
			JsonNode configDoc = objectMapper.readTree(jsonDocument.content().toString());
			log.info("claas in exchnage bod recived : "+inMessage);		
			/***
			 * Reading imge/pdf/png  from request
			 * */
			Attachment profDoc =  (Attachment) inMessage.getBody(ArrayList.class).get(0);
			/***
			 * Reading Json request from body 
			 * */
			JsonNode reqNode =  objectMapper.readTree(inMessage.getBody(ArrayList.class).get(1).toString());
			InputStream io =   profDoc.getDataHandler().getInputStream();
			
			if (jsonDocument != null) {
				Date date = new Date();
				log.info("File server Creation location : "+configDoc.get("documentTempGenerLoc").asText());
				fileLocation =  pcf.CreateFile(dateFormat.format(date).toString()+profDoc.getDataHandler().getName(), configDoc.get("documentTempGenerLoc").asText(),io);
			}
			/*exchange.getIn().setBody("Succeess : "+reqNode+"  FileLocation : "+fileLocation);*/
			JsonNode docProperties = objectMapper.createObjectNode();
			if(reqNode.has("agentId")){
				String agetId = reqNode.get("agentId").asText();
				((ObjectNode)docProperties).put("description", agetId);
				((ObjectNode)docProperties).put("agentId", agetId);		
			}else{
				log.error("AgentId field not foud in request "+reqNode);
			}
			if(reqNode.has("documentType")){
				((ObjectNode)docProperties).put("documentType", reqNode.get("documentType").asText());
				
				
			}else{
				log.error("Request in documentType not found "+reqNode);
			}
			/**
			 * sending file details to ECMS APi for upload file in alfresco POSP folder.
			 * **/
			log.info("docProperties updating list  : "+docProperties);		
			String fileDownloadUrl = ud.uploadPospDocument(fileLocation, docProperties);

			((ObjectNode)reqNode).put("docDownloadUrl", fileDownloadUrl);
			
			JsonNode resNode = objectMapper.createObjectNode();
			((ObjectNode)resNode).put(reqNode.get("documentType").asText(), reqNode);
			((ObjectNode)resNode).put("documentType", reqNode.get("documentType").asText());
			//((ObjectNode)resNode).put("documentType",reqNode.get("documentType").asText());
			exchange.getIn().setBody(resNode);
		}catch(Exception e){
			log.error("unable to process request / file upload : ",e);
		}


	}





	/*public static void main(String[] args) {
			UploadFileProcessor up =  new UploadFileProcessor();
			System.out.println(up.getFileExtenesion("abc.png"));



	//System.out.println("Date format : "+dateFormat.format(date).toString());

		}
	 */
}

