package com.idep.travelquote.res.transformer;



import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class FutureSoapResFormatter implements Processor 
{
	Logger log = Logger.getLogger(FutureSoapResFormatter.class.getName());
	public void process(Exchange exchange) throws Exception 
	{	
		String formatedReposne=null;
		String tagName="Root";
		try{
			String proposalResponse  = exchange.getIn().getBody(String.class);
			HashMap<Object, Object> map = new HashMap<>();

			if(proposalResponse.contains("&gt;") || proposalResponse.contains("&lt;"))
			{
				map.put("&gt;", ">");
				map.put("&lt;", "<");
				/*calling for replace &gt or & lt with < > special symbol*/
				proposalResponse=ReplaceSpeicalCharcter(proposalResponse, map);
			}
			/*calling for removing header from Carrier response*/

			formatedReposne = getPureResponse(proposalResponse, tagName);
			formatedReposne=formatedReposne.replaceAll("&#xD;", "");
			exchange.getIn().setBody(formatedReposne);

		}catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.FUTUREGENSOAPRESFORM+"|ERROR|"+" Exception at FutureGenResFormatter for response :"+formatedReposne,e);
			throw new ExecutionTerminator();
		}
	}

	public String ReplaceSpeicalCharcter(String data ,HashMap<Object,Object> replaceValue){

		for (Map.Entry<Object, Object> entry : replaceValue.entrySet()){
			data = data.replaceAll(entry.getKey().toString(), entry.getValue().toString());
		}
		return data;
	}


	public String getPureResponse(String data, String tagName){
		SoapConnector extService = new SoapConnector();
		String formattedSoapRes = extService.getSoapResult(data,tagName);
		return formattedSoapRes;
	}

}
