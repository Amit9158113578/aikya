package com.idep.offline.reminder.manual;

import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewalReminder.service.PolicyRenewalReminderServiceImpl;

public class RunOfflineRenewalManualReqProcessor implements Processor{
	static Logger log = Logger.getLogger(RunOfflineRenewalManualReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		log.info("In RunOfflineRenewalManualReqProcessor ");
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		String mobileNumbers = "";
		for (JsonNode mobileNumber : reqNode) {
			if(mobileNumber.has("mobile") && mobileNumber.get("mobile") != null && mobileNumber.get("mobile").asText().length() > 0){
				mobileNumbers += mobileNumber.get("mobile").toString()+",";
			}
		}
		log.info("String of mobiles :"+mobileNumbers.substring(0, mobileNumbers.length()-1));
		exchange.getIn().setHeader("mobileList",mobileNumbers.substring(0, mobileNumbers.length()-1));
	}
	public void prepareOfflineRenewalRequest(ArrayList<Map<String, Object>> arrayList) {
		JsonNode headerNode = objectMapper.createObjectNode();
		JsonNode finalNode = objectMapper.createObjectNode();

		((ObjectNode) headerNode).put("source","Renewal");
		((ObjectNode) headerNode).put("transactionName","offlineRenewalReminder");
		((ObjectNode) headerNode).put("deviceId","ABCD12345");

		for(Map<String, Object> map : arrayList){
			log.info("map :"+map);

			JsonNode bodyNode = objectMapper.createObjectNode();
			for (Map.Entry<String, Object> entry: map.entrySet()) {
				if(entry.getValue() !=null){
					((ObjectNode) bodyNode).put(entry.getKey(),entry.getValue().toString());
				}else{
					((ObjectNode) bodyNode).put(entry.getKey(),"");
				}
			}
			((ObjectNode) bodyNode).put("intervalDay","0");
			((ObjectNode) finalNode).put("header",headerNode);
			((ObjectNode) finalNode).put("body",bodyNode);

			log.info("final request :"+finalNode);
			PolicyRenewalReminderServiceImpl.runOfflineRenewal(finalNode);
		}

	}

}
