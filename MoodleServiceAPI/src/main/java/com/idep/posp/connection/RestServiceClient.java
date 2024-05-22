package com.idep.posp.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

public class RestServiceClient{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(RestServiceClient.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode moodleConfig =null;
	
	
	public String CallRestService(JsonNode request){
		try{
			String serviceUrl=null;
			String methodType="POST";
			String urlParam = null;
		log.info("request reciveedat restClient : "+request);
			if(request.has("urlConfigDocId")){
				/**
				 * generate url using confguration eg. http://192.18.0.10/elearing/rest/service.php?wstoken=<wstoken>&functionname=<functionnme>
				 * below method replace url parameter from config  
				 * **/
				serviceUrl=urlGenrater(request.get("urlConfigDocId").asText(),request);
			}else{
				if(request.has("serviceURL")){
					serviceUrl=  request.get("serviceURL").asText(); 
				}else if(request.has("URL")){
					serviceUrl=  request.get("URL").asText(); 
				}else if(request.has("url")){
					serviceUrl=  request.get("url").asText(); 
				}else{
					log.error("service URL not found in reques : "+request);
				}
			}
			log.info("gennarted URL : "+serviceUrl);
			if(request.has("methodType")){
				methodType = request.get("methodType").asText().toUpperCase();
			}
			JsonNode restClientConfig= objectMapper.readTree(pospData.getDocBYId(request.get("ConfigDocId").asText()).content().toString());
			if(restClientConfig.has("parameters")){
				String reqNode = null;
				if(restClientConfig.has("reqNode")){
					reqNode = restClientConfig.get("reqNode").asText();
				}
				
				for(JsonNode data : restClientConfig.get("parameters")){
					log.info("Data Node : "+data);
					/**
					 * 
					 * Generate request url using below method 
					 * data = config document 
					 * request = passed by previous service or Request
					 * **/
					urlParam =generateRequest(data,request,urlParam);
				}
			}
		
			log.info("gennarted urlParam  : "+urlParam);
			  HttpURLConnection con = (HttpURLConnection) new URL(serviceUrl).openConnection();
			  con.setRequestMethod(methodType);
		        con.setRequestProperty("Content-Type",
		           "application/x-www-form-urlencoded");
		        con.setRequestProperty("Content-Language", "en-US");
		        con.setDoOutput(true);
		        con.setUseCaches (false);
		        con.setDoInput(true);
		        DataOutputStream wr = new DataOutputStream (
		                  con.getOutputStream ());
		        wr.writeBytes (urlParam);
		        wr.flush ();
		        wr.close ();

		        //Get Response
		        InputStream is =con.getInputStream();
		        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		        String line;
		        StringBuilder response = new StringBuilder();
		        while((line = rd.readLine()) != null) {
		            response.append(line);
		            response.append('\r');
		        }
		        rd.close();
		        System.out.println("RES : "+response.toString());
			return response.toString();
		}catch(Exception e){
			log.error("uable to genarte reques usinf rest client : ",e);
				e.printStackTrace();
		}
		return null;
	}
	
	
	public String  urlGenrater(String configdocId,JsonNode request){
		
		try{
			if(pospData!=null){
				moodleConfig = objectMapper.readTree(pospData.getDocBYId(configdocId).content().toString());
			}else{
				log.error("unable to read CB  document : "+configdocId); 
			}
			log.info("urlGenrater()  request Recived : "+request);
			String serviceUrl= moodleConfig.get("serviceUrl").asText();
			if(request.has("functionName")){
				log.info("request in functionName : "+request.findValue("functionName").asText());
			serviceUrl = serviceUrl.replace("<wstoken>", moodleConfig.get("wstoken").asText())
						.replace("<wsfunction_name>", moodleConfig.get("functionList").get(request.findValue("functionName").asText()).asText());
			}else{
				log.info("unable to find wstoken/functionName in property :"+request);
			}
			return serviceUrl;
		}catch(Exception e){
			log.error("unable to genrate url urlGenrater(): ",e); 
		}
		return null;
	}


	public String generateRequest(JsonNode data,JsonNode request,String urlParam){
		try{
		if(data.has("defaultValue")){
			if(urlParam!=null){
				urlParam = urlParam +"&"+data.get("requestKey").asText()+"="+data.get("defaultValue").asText();
				}else{
					urlParam = data.get("requestKey").asText()+"="+data.get("defaultValue").asText();
				}
		}else if(data.has("reqNode")){
			String reqNode = data.get("reqNode").asText();
			if(data.has("case") && data.get("case").asText().equalsIgnoreCase("lower")){
				if(urlParam!=null && data.get("case").asText().equalsIgnoreCase("lower")){
					urlParam = urlParam+"&"+data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText().toLowerCase();
					}else{
						urlParam=data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText().toLowerCase();
					}
			}else{
				if(data.has("case")){
				if(urlParam!=null && data.get("case").asText().equalsIgnoreCase("lower")){
					urlParam = urlParam+"&"+data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText().toLowerCase();
					}else{
						urlParam=data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText().toUpperCase();
					}
				}else{
					if(urlParam!=null){
						urlParam = urlParam+"&"+data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText();
						}else{
							urlParam=data.get("requestKey").asText()+"="+request.get(reqNode).get(data.get("keyName").asText()).asText();
						}
				}
			}
			
		}else{
			if(data.has("case")){
					if(urlParam!=null && data.get("case").asText().equalsIgnoreCase("lower")){
					urlParam = urlParam +"&"+data.get("requestKey").asText()+"="+request.get(data.get("keyName").asText()).asText().toLowerCase();
					}else{
						urlParam=data.get("requestKey").asText()+"="+URLEncoder.encode(request.get(data.get("keyName").asText()).asText().toLowerCase(), "UTF-8");
					}
			}else{
				if(urlParam!=null){
					urlParam = urlParam +"&"+data.get("requestKey").asText()+"="+request.get(data.get("keyName").asText()).asText();
					}else{
						urlParam=data.get("requestKey").asText()+"="+URLEncoder.encode(request.get(data.get("keyName").asText()).asText(), "UTF-8");
					}
			}
		}
	return urlParam;	
	
	}catch(Exception e){
		
		log.error("unale to genrate url param : ",e);
	}
		return urlParam;
  }		
}
