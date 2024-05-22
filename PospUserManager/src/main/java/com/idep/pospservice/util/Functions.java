package com.idep.pospservice.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Functions {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(Functions.class.getName());
	
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

}
