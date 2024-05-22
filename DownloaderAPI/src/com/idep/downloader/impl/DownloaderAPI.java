/**
 * 
 */
package com.idep.downloader.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.idep.downloader.IDownloader;

/**
 * @author vipin.patil
 *
 * May 24, 2017
 */
public class DownloaderAPI implements IDownloader{

	Logger log = Logger.getLogger(DownloaderAPI.class.getName());
	private static final int BUFFER_SIZE = 4096;
	@Override
	public String downloadFile(String saveDirectory, String URLString) {
		// TODO Auto-generated method stub
		log.info("Download File Method");
		try
		{
			log.info("Connection URL : "+URLString);
			URL url = new URL(URLString);
		
	    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	    int responseCode = httpConn.getResponseCode();
	    String fileName = "";
	    // always check HTTP response code first
	    log.info("Reponse Code : "+responseCode);
	    if (responseCode == 200) {
	        String disposition = httpConn.getHeaderField("Content-Disposition");
	        String contentType = httpConn.getContentType();
	        int contentLength = httpConn.getContentLength();

	        if (disposition != null) {
	            // extracts file name from header field
	            int index = disposition.indexOf("filename=");
	            if (index > 0) {
	                fileName = disposition.substring(index + 9,
	                        disposition.length());
	            }
	        } else {
	            // extracts file name from URL
	            fileName = URLString.substring(URLString.lastIndexOf("/") + 1,
	            		URLString.length());
	        }

	        log.info("Content-Type = " + contentType);
	        log.info("Content-Disposition = " + disposition);
	        log.info("Content-Length = " + contentLength);
	        log.info("fileName = " + fileName);
	        // opens input stream from the HTTP connection
	        InputStream inputStream = httpConn.getInputStream();
	        String saveFilePath = saveDirectory + File.separator + fileName;
	        /**
	         * For Cigna added code to remove " " from downloaded filename
	         */
	        log.info("fileName before contains doublequote : "+fileName);
	        if(fileName.charAt(0)== '"'){
	        	 fileName = fileName.substring(1,fileName.length()-1);
	        	 log.info("fileName after doublequote remove : "+fileName);
	             saveFilePath = saveDirectory +  File.separator  + fileName;
	        }

	        log.info("File Path:"+saveFilePath);
	        // opens an output stream to save into file
	        FileOutputStream outputStream = new FileOutputStream(saveFilePath);
	        int bytesRead = -1;
	        byte[] buffer = new byte[BUFFER_SIZE];
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outputStream.write(buffer, 0, bytesRead);
	        }

	        outputStream.close();
	        inputStream.close();
	        saveDirectory = saveDirectory+"/"+fileName.trim();
	        log.info("File downloaded");	        
	    } else {
	        log.info("No file to download. Server replied HTTP code: " + responseCode);
	    }
	    httpConn.disconnect();
		return saveDirectory;
		}
		catch(Exception e)
		{
			return null;
		}
		
	}

	}
