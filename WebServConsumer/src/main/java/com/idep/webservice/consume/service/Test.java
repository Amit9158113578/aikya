package com.idep.webservice.consume.service;

import java.util.HashMap;
import java.util.Map;

public class Test {

	public static void main(String[] args) {

		
				WebServiceInvoke invoke = new WebServiceInvoke();
				//invoke.sendHTTPSOAPRequest(request, url, reqHeaders)
				String request = "<soapenv:Envelopexmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"xmlns:tem=\"http://tempuri.org/\"><soapenv:Header/><soapenv:Body><tem:GetSignPolicyPDF><tem:strUserId>?</tem:strUserId><tem:strPassword>?</tem:strPassword><tem:strProdType>?</tem:strProdType><tem:strPolicyNo>?</tem:strPolicyNo><tem:strPolicyIssueTime>?</tem:strPolicyIssueTime><tem:strTransactionID>?</tem:strTransactionID><tem:strTransactionRef>?</tem:strTransactionRef><tem:strCustomerName>?</tem:strCustomerName><tem:strPolicyPDF>cid:4139203583</tem:strPolicyPDF></tem:GetSignPolicyPDF></soapenv:Body></soapenv:Envelope>";
				String url = "https://kgipass.kotakmahindrageneralinsurance.co.in/GCIntegrationServices/PartnerIntegrationService.svc";
				//String url = "http://14.141.253.242/GCIntegrationServices/PartnerIntegrationService.svc";
				Map<String,Object> headers = new HashMap<String,Object>();
				
				headers.put("soapaction", "http://tempuri.org/IPartnerIntegrationService/GetSignPolicyPDF");
				//headers.put("Content-Type", "text/xml");
				//headers.put("Host", "kgipass.kotakmahindrageneralinsurance.co.in");
				//headers.put("Accept-Encoding", "gzip,deflate");
				
				
				
				try
				{
					String res = invoke.sendHTTPSOAPRequest(request, url, headers);
					System.out.println("service response : "+res);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		
	}

}
