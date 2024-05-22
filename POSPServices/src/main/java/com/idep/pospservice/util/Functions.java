package com.idep.pospservice.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Functions {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(Functions.class.getName());

	public static String getDate(){
		SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		return dateformat.format(date);

	}

	public static String getDateAndTime(){
		SimpleDateFormat datetimeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		return datetimeformat.format(date);		
	}

	public String getFileExtenesion(String filename){
		try{
			String fileName = filename;
			if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
				return fileName.substring(fileName.lastIndexOf(".")+1);
			else return "";
		}catch(Exception e){
			log.error("unable to get file extension : "+filename);
		}
		return null;
	}


	public String CreateFile(String filename,String absoulteLoc ,InputStream fileContent){
		OutputStream outputStream = null;
		try{

			getFileExtenesion(filename);
			File file = new File(absoulteLoc+filename);
			if(file.createNewFile()){
				outputStream = new FileOutputStream(file);
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = fileContent.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				log.info("new file Created : "+file.getAbsolutePath());
			}else{
				log.info("File Already Exisits : "+file.getAbsolutePath());
			}
			return file.getAbsolutePath();
		}catch(Exception e){
			log.error("Unable to Process request ",e); 
		}finally{
			if(outputStream!=null){
				try{
					outputStream.close();
				}catch(Exception e){
					log.error("Unable to close OUTPUTSTRAM in CreateFile()");
				}

			}


		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static ObjectNode filterMapData(ObjectNode dataNode,JsonNode reqNode,JsonNode reqConfig)
	{

		Map<String, String> reqProcNodeMap = null;
		try {
			reqProcNodeMap = objectMapper.readValue(reqConfig.toString(),Map.class);
		} catch (IOException e) {
			log.error("error while mapping configuration value",e);
		}

		try
		{
			for (Map.Entry<String, String> field : reqProcNodeMap.entrySet())
			{
				if(reqNode.findValue(field.getKey()) != null){
					if (reqNode.findValue(field.getKey()).isTextual()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).textValue());
					} else if (reqNode.findValue(field.getKey()).isInt()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).intValue());
					} else if (reqNode.findValue(field.getKey()).isLong()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).longValue());
					} else if (reqNode.findValue(field.getKey()).isDouble()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).doubleValue());
					} else if (reqNode.findValue(field.getKey()).isBoolean()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).booleanValue());
					} else if (reqNode.findValue(field.getKey()).isFloat()) {
						dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).floatValue());
					}
				}
			}
		}
		catch(NullPointerException e)
		{
			log.error("error while preparing ObjectNode",e);
		}
		return dataNode;
	}

}
