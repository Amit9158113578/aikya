package com.idep.webservice.consume.service;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class WebServiceInvoke {
	
	Logger log = Logger.getLogger(WebServiceInvoke.class.getName());
	static JsonNode timeOutConfigNode = null;
	
	static
	{
		try
		{
			ObjectMapper objectMapper = new ObjectMapper();
			CBService service = CBInstanceProvider.getServerConfigInstance();
			timeOutConfigNode = objectMapper.readTree(service.getDocBYId("TimeOutConfig").content().toString());
		}
		catch(Exception e)
		{
			//
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public String sendRESTHTTPRequest(String request,String url,Map<String,Object>reqHeaders)throws SocketException,SocketTimeoutException,Exception
    {
		HttpClient httpclient = HttpClientBuilder.create().build();
		String serviceResponse = null;
		
	    try 
	    {
		    HttpParams httpParameters = new BasicHttpParams();
		    /**
		     *  Set the timeout in milliseconds until a connection is established.
		     */
		    int timeoutConnection = timeOutConfigNode.get("connectTimeout").asInt();//20000;
		    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		    /**
		     *  Set the default socket timeout (SO_TIMEOUT)
		     *  in milliseconds which is the timeout for waiting for data.
		     */
		    int timeoutSocket = timeOutConfigNode.get("socketTimeout").asInt();//90000;
		    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		    //httpclient = new DefaultHttpClient(httpParameters);
		    
		    HttpPost httppost = new HttpPost(url);
		    httppost.setParams(httpParameters);
		    /**
		     *  set headers
		     */
		    for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
		    	
		    	//log.debug("headerkey : "+entry.getKey()+" : headervalue : "+entry.getValue().toString());
		    	httppost.setHeader(entry.getKey(), entry.getValue().toString());
		    }
		    
	        @SuppressWarnings("deprecation")
			HttpEntity entity = new StringEntity(request,HTTP.UTF_8);
	        httppost.setEntity(entity);
	        HttpResponse response = httpclient.execute(httppost);// calling server
	        HttpEntity r_entity = response.getEntity();  //get response
	        
	        Header[] headers = response.getAllHeaders();
            for(Header h:headers){
                log.debug("Reponse Header : "+h.getName() + " : " + h.getValue());
            }
            
	        serviceResponse = EntityUtils.toString(r_entity);
	        log.debug("REST service response : "+serviceResponse);
	        httpclient.getConnectionManager().shutdown(); //shut down the connection
	        
	        return serviceResponse;
      
	  }
	    catch (Exception e) {
        
	    	log.error("Exception while invoking REST webservice : ", e);
        	httpclient.getConnectionManager().shutdown(); //shut down the connection
        	throw e;
        	
	    }
	    finally
    	{
    		httpclient.getConnectionManager().shutdown(); //shut down the connection
    	}
    
    }
    
    @SuppressWarnings("deprecation")
	public String sendHTTPSOAPRequest(String request,String url,Map<String,Object>reqHeaders)throws SocketException,SocketTimeoutException,Exception
    {
    	HttpClient httpclient = HttpClientBuilder.create().build();
    	String serviceResponse = null;
    	
    	try {
    		
	        HttpParams httpParameters = new BasicHttpParams();
	        /**
	         *  Set the timeout in milliseconds until a connection is established.
	         */
	        int timeoutConnection = timeOutConfigNode.get("connectTimeout").asInt();//20000;
	        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	        /**
	         *  Set the default socket timeout (SO_TIMEOUT)
	         *  in milliseconds which is the timeout for waiting for data.
	         */
	        int timeoutSocket = timeOutConfigNode.get("socketTimeout").asInt();//90000;
	        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	        //httpclient = new DefaultHttpClient(httpParameters);
	        
	        HttpPost httppost = new HttpPost(url);
	        /**
	         *  set headers
	         */
		    for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
		    	
		    	//log.debug("headerkey : "+entry.getKey()+" : headervalue : "+entry.getValue().toString());
		    	httppost.setHeader(entry.getKey(), entry.getValue().toString());
		    }
	        
      
            @SuppressWarnings("deprecation")
			HttpEntity entity = new StringEntity(request,HTTP.UTF_8);
            httppost.setEntity(entity);
            System.out.println("Complete Request :"+EntityUtils.toString(entity));
            HttpResponse response = httpclient.execute(httppost);// calling server
            HttpEntity r_entity = response.getEntity();  //get response
            Header[] headers = response.getAllHeaders();
            for(Header h:headers){
                log.debug("Reponse Header : "+h.getName() + " : " + h.getValue());
            }
            
            
            serviceResponse = EntityUtils.toString(r_entity);
            log.debug("SOAP service response : "+serviceResponse);
            
            httpclient.getConnectionManager().shutdown(); //shut down the connection
            
            return serviceResponse;
          
        } catch (Exception e) {
        	
        	log.error("Exception while invoking SOAP webservice : ", e);
        	httpclient.getConnectionManager().shutdown(); //shut down the connection
        	throw e;
        }
    	finally
    	{
    		httpclient.getConnectionManager().shutdown(); //shut down the connection
    	}

        
    }
    public String sendGETHTTPRequest(String request,String url,Map<String,Object>reqHeaders)throws SocketException,SocketTimeoutException,Exception
    {
		HttpClient httpclient = HttpClientBuilder.create().build();
		String serviceResponse = null;
		
	    try 
	    {
		    HttpParams httpParameters = new BasicHttpParams();
		    /**
		     *  Set the timeout in milliseconds until a connection is established.
		     */
		    int timeoutConnection = timeOutConfigNode.get("connectTimeout").asInt();//20000;
		    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		    /**
		     *  Set the default socket timeout (SO_TIMEOUT)
		     *  in milliseconds which is the timeout for waiting for data.
		     */
		    int timeoutSocket = timeOutConfigNode.get("socketTimeout").asInt();//90000;
		    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		    //httpclient = new DefaultHttpClient(httpParameters);
		    
		   
		    
		    //HttpPost httppost = new HttpPost(url);
		    HttpGet httpget=new HttpGet(url);

		    
		   		    /**
		     *  set headers
		     */
		    for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
		    	
		    	this.log.debug("hkey : " + (String)entry.getKey() + " : hvalue : " + entry.getValue().toString());
		        httpget.setHeader((String)entry.getKey(), entry.getValue().toString());
		      
		    }
		    
	        
	        HttpResponse response = httpclient.execute(httpget);// calling server
	        HttpEntity r_entity = response.getEntity();  //get response
	        
	        Header[] headers = response.getAllHeaders();
            for(Header h:headers){
                log.debug("Reponse Header : "+h.getName() + " : " + h.getValue());
            }
            
	        serviceResponse = EntityUtils.toString(r_entity);
	        log.info("GET service response : "+serviceResponse);
	        httpclient.getConnectionManager().shutdown(); //shut down the connection
	        
	        return serviceResponse;
      
	  }
	    catch (Exception e) {
        
	    	log.error("Exception while invoking REST webservice : ", e);
        	httpclient.getConnectionManager().shutdown(); //shut down the connection
        	throw e;
        	
	    }
	    finally
    	{
    		httpclient.getConnectionManager().shutdown(); //shut down the connection
    	}
    
    }
    @SuppressWarnings("deprecation")
	public String sendHTTPPUTRequest(String request,String url,Map<String,Object>reqHeaders)throws SocketException,SocketTimeoutException,Exception
    {
    	HttpClient httpclient = HttpClientBuilder.create().build();
    	String serviceResponse = null;
    	
    	try {
	        HttpParams httpParameters = new BasicHttpParams();
	        /**
	         *  Set the timeout in milliseconds until a connection is established.
	         */
	        int timeoutConnection = timeOutConfigNode.get("connectTimeout").asInt();//20000;
	        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	        /**
	         *  Set the default socket timeout (SO_TIMEOUT)
	         *  in milliseconds which is the timeout for waiting for data.
	         */
	        int timeoutSocket = timeOutConfigNode.get("socketTimeout").asInt();//90000;
	        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	        //httpclient = new DefaultHttpClient(httpParameters);
	        
	        HttpPut httpput = new HttpPut(url);
	        /**
	         *  set headers
	         */
		    for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
		    	
		    	//log.debug("headerkey : "+entry.getKey()+" : headervalue : "+entry.getValue().toString());
		    	httpput.setHeader(entry.getKey(), entry.getValue().toString());
		    }
	        
      
            @SuppressWarnings("deprecation")
			HttpEntity entity = new StringEntity(request,HTTP.UTF_8);
            httpput.setEntity(entity);
            System.out.println("Complete Request :"+EntityUtils.toString(entity));
            HttpResponse response = httpclient.execute(httpput);// calling server
            HttpEntity r_entity = response.getEntity();  //get response
            Header[] headers = response.getAllHeaders();
            for(Header h:headers){
                log.debug("Reponse Header : "+h.getName() + " : " + h.getValue());
            }
            
            serviceResponse = EntityUtils.toString(r_entity);
            log.debug("SOAP service response : "+serviceResponse);
            
            httpclient.getConnectionManager().shutdown(); //shut down the connection
            
            return serviceResponse;
          
        } catch (Exception e) {
        	
        	log.error("Exception while invoking SOAP webservice : ", e);
        	httpclient.getConnectionManager().shutdown(); //shut down the connection
        	throw e;
        }
    	finally
    	{
    		httpclient.getConnectionManager().shutdown(); //shut down the connection
    	}

        
    }

}
