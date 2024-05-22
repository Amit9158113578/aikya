package com.idep.travelquote.res.transformer;



import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class BhartiAxaSoapResFormatter implements Processor 
{
	Logger log = Logger.getLogger(BhartiAxaSoapResFormatter.class.getName());
	public void process(Exchange exchange) throws Exception 
	{	
		String formatedReposne=null;
		String tagName="processTPRequestResponse";
		try{
			String proposalResponse  = exchange.getIn().getBody(String.class);
			HashMap<Object, Object> map = new HashMap<>();

			if(proposalResponse.contains("xmlns:SOAP") || proposalResponse.contains("xmlns"))
			{
				map.put(" xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\"", "");
				map.put(" xmlns=\"http://schemas.cordys.com/bagi/tparty/core/bpm/1.0\"", "");
				map.put(" xmlns=\"http://schemas.cordys.com/default\"", "");
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
