package com.idep.policy.res.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;


public class TravelSoapResFormatter implements Processor {

	public TravelSoapResFormatter() 
	{		
	}
	
	Logger log = Logger.getLogger(TravelSoapResFormatter.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception 
	{
		String proposalResponse  = exchange.getIn().getBody(String.class);		
		HashMap<Object, Object> map = new HashMap<>();
		/**
		 * 
		 */
		if(proposalResponse.contains("&gt;") || proposalResponse.contains("&lt;"))
		{
			map.put("&gt;", ">");
			map.put("&lt;", "<");
			/*calling for replace &gt or & lt with < > special symbol*/
			proposalResponse=ReplaceSpeicalCharcter(proposalResponse, map);
			log.debug("SOAP Respone Connverted Successfuly : "+proposalResponse);
		}
		/*calling for removing header from Carrier response*/
		log.debug("proposalResponse in TravelSoapResFormatter :"+ proposalResponse);
		String tagName="WsResult";		
		String formatedReposne = getPureResponse(proposalResponse, tagName);
		log.debug("SOAP Respone Connverted Successfuly : "+formatedReposne);
		exchange.getIn().setBody(formatedReposne);
	}

public String ReplaceSpeicalCharcter(String data ,HashMap<Object,Object> replaceValue)
	{	
		for (Map.Entry<Object, Object> entry : replaceValue.entrySet()){
			data = data.replaceAll(entry.getKey().toString(), entry.getValue().toString());
		}
		return data;
	}	
	
	public String getPureResponse(String data, String tagName)
	{
		SoapConnector extService = new SoapConnector();
		log.debug("Tag Name : "+tagName);
		String formattedSoapRes = extService.getSoapResult(data,tagName);
		log.debug("Soap Response Processed : "+formattedSoapRes.toString());
		return formattedSoapRes;
	}
	
}
