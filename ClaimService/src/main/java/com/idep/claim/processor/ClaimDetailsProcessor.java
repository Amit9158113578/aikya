/*
 * Author:  	Nikita Jain
 * Date  :		23/1/18
 * Description:		This processor will show claim labels and respective URL in UI 
 * 					depending on selection of insurance type and insurance company.
 */

package com.idep.claim.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.node.ObjectNode;
import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.claim.util.ServiceConstant;

public class ClaimDetailsProcessor implements Processor {

	ObjectMapper objectMapper=new ObjectMapper();
	Logger log =Logger.getLogger(ClaimDetailsProcessor.class.getName());
	//CBService is couchbase service
	CBService service=CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		JsonNode response =null;

		try{
			String UIcarrierName =null;
			String DBcarrierName =null;
			String request = exchange.getIn().getBody(String.class);
			JsonNode requestNode = objectMapper.readTree(request);
			log.info("claim request node details :"+requestNode);

			JsonDocument CarrierNameDetails=service.getDocBYId(ServiceConstant.CARRIERNAMEDETAILS);
			if(CarrierNameDetails!=null)
			{
				JsonNode carrierNameDetails=objectMapper.readTree(CarrierNameDetails.content().toString());

				if(requestNode.has(ServiceConstant.CARRIERNAME))
				{
					UIcarrierName = requestNode.get(ServiceConstant.CARRIERNAME).asText().trim();
					DBcarrierName = carrierNameDetails.get(UIcarrierName).asText();
				}
				else
				{
					log.info("carrier name not found :");
					new Exception();
				}


				if(requestNode.has(ServiceConstant.CARRIERID) && requestNode.get(ServiceConstant.CARRIERID).asText()!=null)
				{
					String documentId= ServiceConstant.CLAIMDETAILS+requestNode.get(ServiceConstant.CARRIERID).asText();
					//log.info("document id"+documentId);
					//get document from couchbase
					JsonDocument ClaimDetails = service.getDocBYId(documentId);
					if(ClaimDetails==null)
					{
						log.info("Claim Details Document Not Found :"+ClaimDetails);
						new Exception();
					}
					else{				
						if(requestNode.has(ServiceConstant.CARRIERNAME) && requestNode.get(ServiceConstant.CARRIERNAME ).asText()!=null)
						{		//claimDetails will have all content of document
							JsonNode claimDetails = objectMapper.readTree(ClaimDetails.content().toString());
							ArrayNode carrierNameNode=(ArrayNode)claimDetails.get(DBcarrierName);
							for(JsonNode carrier :carrierNameNode)
							{
								response = carrier.get(requestNode.get(ServiceConstant.INSURANCETYPE).asText());
								if(response!=null)
								{
									break; 
								}
							}
						}
						else
						{
							log.info("carrier name not found input details :"+requestNode);
							new Exception();
						}
					}
					exchange.getIn().setBody(objectMapper.writeValueAsString(response));
				}
			}else
			{
				log.error("can not read carrier name details document :"+CarrierNameDetails);
			}

		}catch(Exception e)
		{
			log.error("Document not found Exception Occured:",e);
			new Exception();
		}
	}

}
