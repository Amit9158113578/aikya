package com.idep.lead.req.fileProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class LeadReqStoreFileProcessor implements Processor{

	Logger log = Logger.getLogger(LeadReqStoreFileProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		BufferedWriter writer =null;
		Properties property = new Properties();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode reqNode=null;
		String inputReq=null;
		SimpleDateFormat simple = new SimpleDateFormat("dd-MM-yyyy");
		try{

			inputReq = exchange.getIn().getBody().toString();
			reqNode = objectMapper.readTree(inputReq);
			log.info("Printing : "+reqNode);
			if(reqNode.has("msgIdStatus")){
				log.info("as new"+reqNode.get("msgIdStatus").asText());
			}
			
			if( reqNode.has("msgIdStatus") && reqNode.get("msgIdStatus").asText().equalsIgnoreCase("new")){
				log.info("In Writting req");
				InputStream inputStream = new FileInputStream(System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
				property.load(inputStream);	
				String fileName = "lmslead_request_"+simple.format(new Date())+property.getProperty("nodeName")+".txt";
				File file = new File( property.getProperty("lms_leadfile_path")+fileName);

				if(!file.exists()){
					file.createNewFile();
					log.info("lead requtes file created  : "+fileName);	
				}

				FileWriter fw = new FileWriter(file,true);

				writer=new BufferedWriter(fw);
				writer.write("\n".concat(inputReq));
			}else{
				log.info("Request Not written since msgIdStatus is old");
			}

		}catch(Exception e){
			log.error("error at  Lead writting inn file "+exchange.getIn().getBody(String.class));
			log.error("error at  Lead writting inn file ",e);
		}finally{
			if(writer!=null){
				try{
					writer.close();
				}catch(Exception e){
					log.error("failed to close writer connection",e);		
				}
			}
		}
	}

}
