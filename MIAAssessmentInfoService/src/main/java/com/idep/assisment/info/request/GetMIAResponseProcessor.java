package com.idep.assisment.info.request;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.geostatistics.summaryUtils.DBConnection;
import com.idep.assisment.info.request.ExecutionTerminator;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class GetMIAResponseProcessor
{
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetMIAResponseProcessor.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode questionRes = objectMapper.createObjectNode();
	static JsonNode GeoStatisticQueryConfigNode = null;
	static JsonNode MIAAssessmentResponseMsg = null;
	static JsonNode MIAAssessmentResponse = null;
	static List<Map<String, Object>> QueryExcuteState = null;
	static String returnValue = null;
	JsonNode valuereplaceNode = objectMapper.createObjectNode();
	static ArrayNode Output = objectMapper.createArrayNode();
	static ArrayNode OutputQuery = objectMapper.createArrayNode();

	static
	{
		try
		{
			GeoStatisticQueryConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("GeoStatisticQueryConfiguration").content()).toString());
			MIAAssessmentResponseMsg = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("MIAAssessmentResponseMsg-DR").content()).toString());
			MIAAssessmentResponse = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("MIAAssessmentResponse").content()).toString());
		}
		catch (Exception e)
		{
			log.info("ALLL Documents Not Found" + e);
			e.printStackTrace();
		}
		try
		{
			JsonNode QueryConfigNode = objectMapper.readTree(GeoStatisticQueryConfigNode.toString());
			if (QueryConfigNode != null)
			{
				log.info("All query DOcument Load : " + QueryConfigNode);
				ArrayNode queries = (ArrayNode)QueryConfigNode.get("listQueries");
				log.info("All query DOcument Load Query Batch extracted : : " + queries);
				for (JsonNode query : queries)
				{
					if (query.has("defaultValue"))
					{
						((ObjectNode)query).put("value", query.get("defaultValue"));
					}
					else
					{
						String Query = query.get("Query").asText();
						QueryExcuteState = DBConnection.getServerConfigInstance().executeQuery(Query);
						if (QueryExcuteState.size() > 0) {
							((ObjectNode)query).put("value", ((Map)QueryExcuteState.get(0)).get("$1").toString());
						}
					}
					Output.add(query);
				}
				OutputQuery.addAll(Output);
			}
		}
		catch (Exception e)
		{
			log.error("Document found but not able to fire query on couchbase : ", e);
		}
	}

	public String getMiaSummaryConfig(JsonNode request)
			throws JsonProcessingException, IOException, ExecutionTerminator  {
		try{
			for(int i=0;i<MIAAssessmentResponse.get("QuestionCode").size();i++){
				if(request.get("currentQuestionCode").asText().equalsIgnoreCase(MIAAssessmentResponse.get("QuestionCode").get(i).findValue("questionCode").asText())){
					String currentQuestionCode =  request.get("currentQuestionCode").asText();	
					int quesCode = currentQuestionCode.length() - 3;
					currentQuestionCode = currentQuestionCode.replace(currentQuestionCode.substring(0,quesCode), "DR");
					((ObjectNode) request).put("currentQuestionCode",currentQuestionCode);
				}
			}
			for (JsonNode queryRes : OutputQuery)
			{
				if (request.get("currentQuestionCode").asText().equalsIgnoreCase(queryRes.get("questionId").asText())) {
					if (queryRes.has("reqParam"))
					{
						if (request.findValue(queryRes.get("reqParam").asText()) != null)
						{
							if (request.findValue(queryRes.get("reqParam").asText()).asText().equalsIgnoreCase(queryRes.get("validationKey").asText()))
							{				
								String outputValue = generateOutputInWords(queryRes.get("value").asInt());
								String res = generateResponse(outputValue, queryRes, request);
								log.info("Question Code Procesing Completed : "+queryRes.get("questionId").asText()+" msg :"+  res );
								return res;
							}
						}
						else
						{					
							String outputValue = generateOutputInWords(queryRes.get("value").asInt());
							String res = generateResponse(outputValue, queryRes, request);
							log.info("Question Code Procesing Completed : "+queryRes.get("questionId").asText()+" msg :"+  res );
							return res;
						}
					}
					else
					{
						String outputValue = generateOutputInWords(queryRes.get("value").asInt());
						String res = generateResponse(outputValue, queryRes, request);
						log.info("Question Code Procesing Completed: "+queryRes.get("questionId").asText()+" msg :"+  res );
						return res;
					}
				}
			}
		}
		catch(Exception e){
			log.error("Exception : ",e);
			throw new ExecutionTerminator();
		}
		return null;
	}
	public String generateResponse(String input, JsonNode queryRes, JsonNode request)
	{
		String replaceKey = MIAAssessmentResponseMsg.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText();
		String msg = MIAAssessmentResponseMsg.get(request.get("currentQuestionCode").asText()).get("msg").asText();
		msg = msg.replaceAll("<" + replaceKey + ">", input);
		if (MIAAssessmentResponseMsg.get(request.get("currentQuestionCode").asText()).get("reqReplaceKeys") != null)
		{
			ArrayNode reqReplaceKeys = (ArrayNode)MIAAssessmentResponseMsg.get(request.get("currentQuestionCode").asText()).get("reqReplaceKeys");
			for (JsonNode reqParam : reqReplaceKeys)
			{
				if (reqParam.has("reqNode"))
				{		msg = msg.replaceAll("<" + reqParam.get("reqParam").asText() + ">", request.get(reqParam.get("reqNode").asText()).findValue(reqParam.get("reqParam").asText()).asText());

				return  msg;
				}
				else if (reqParam.has("defaultValue"))
				{
					return  msg;
				}
			}
		}
		else
		{
			return msg;
		}
		log.info("generateResponse Process Complted for : " + this.questionRes);
		return null;
	}

	public String generateOutputInWords(int input)
	{
		String replaceValue = null;
		double dividedValue = Math.round(input / 100000);

		if ((dividedValue >= 1.0D) && (dividedValue <= 99.0D))
		{
			replaceValue = dividedValue + " Lakh";
		}
		else if ((dividedValue >= 100.0D) && (dividedValue <= 999.0D))
		{
			double dividedValueC = Math.round(input / 10000000);	
			replaceValue = dividedValueC + " Crore";
		}
		else if ((dividedValue >= 1000.0D) && (dividedValue <= 9999.0D))
		{
			double dividedValueCR = Math.round(input / 10000000);	
			replaceValue = dividedValueCR + " Crore";
		}
		else
		{
			replaceValue = Integer.toString(input);
		}
		return replaceValue;
	}
	/*
	public static void main (String args[]) throws ExecutionTerminator{
		System.out.println("hello in main method...");	
		GetMIAResponseProcessor qc = new GetMIAResponseProcessor();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode data;
		try {
			data = objectMapper.readTree("{\"professionId\": 2,\"profession\": \"IT, ITES & Tech Startup\",\"professionCode\": \"IT\",\"commonInfo\": {	\"familyHistory\": [],	\"address\": {		\"streetDetails\": \"\",		\"city\": \"\",		\"state\": \"\",		\"pincode\": \"\"	},	\"termsCondition\": true,	\"gender\": \"Male\",	\"specialization\": \"Design & Creatives\",	\"professionalLiability\": \"Own Consulting\",	\"homeStatus\": \"Owned\",	\"clinicStatus\": \"Owned\",	\"smoking\": false},\"currentQuestionCode\": \"IT001\",\"requestSource\": \"web\",\"leadSource\": {	}}");
			qc.getMiaSummaryConfig(data);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
