/**
 * 
 */
package com.idep.assisment.info.request;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.geostatistics.restService.GeoStatisticsProcessor;
/**
 * @author pravin.jakhi
 *
 */
public class GetProfessionQuestionConfiguration {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetProfessionQuestionConfiguration.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode questionRes = objectMapper.createObjectNode();
	static ObjectNode miaDocConfigList = null;
	static
	{
		miaDocConfigList = objectMapper.createObjectNode();
		log.info("Product Config Data loading initiated");
		try
		{
			JsonNode docConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("MIADocConfigList").content()).toString());
			log.info("docConfigNode"+docConfigNode);
			Map<String, Object> configMap = (Map)objectMapper.readValue(docConfigNode.get("documentList").toString(), Map.class);
			log.info("configMap"+configMap);
			for (Map.Entry<String, Object> entry : configMap.entrySet())
			{
				Map<String, String> confMap = (Map)objectMapper.readValue(objectMapper.writeValueAsString(entry.getValue()), Map.class);
				ObjectNode docListNode = objectMapper.createObjectNode();
				for (Map.Entry<String, String> docConfig : confMap.entrySet()) {
					if (((String)docConfig.getValue()).equalsIgnoreCase("Y")) {
						docListNode.put((String)docConfig.getKey(), objectMapper.readTree(((JsonObject)serverConfig.getDocBYId((String)docConfig.getKey()).content()).toString()));
					}
				}
				miaDocConfigList.put((String)entry.getKey(), docListNode);
				log.info("miaDocConfigList..."+miaDocConfigList);
			}
			log.info("Product Config Data loading completed");
		}
		catch (Exception e)
		{
			log.error("failed to setup product configuration documents : ", e);
		}
	}

	////enddddd

	public JsonNode getProfessionQuestionConfig(JsonNode request){
		JsonNode assistmentConfig=null;

		try{
			GeoStatisticsProcessor GSA = new GeoStatisticsProcessor(); 
			log.info("request in MIA serVice"+request);

			if(request.has("professionCode"))
			{ 
				log.info("miaDocConfigList : "+miaDocConfigList);
				//assistmentConfig = objectMapper.readTree(serverConfig.getDocBYId("MIAAssessmentConfig-"+request.get("professionCode").asText()).content().toString());
				String docId = "MIAAssessmentConfig-"+request.get("professionCode").asText();
				
				assistmentConfig = miaDocConfigList.get("miaScreenConfig").get(docId);
				log.info("docmune for configg..."+assistmentConfig);
			}
			else
			{
				log.error("unable to Assessment config document from DB : MIAAssessmentConfig-<profesionCode> : "+request);
			}
			JsonNode formedReq = generateRequest(request,assistmentConfig);
			log.info("Formed request : "+formedReq);
			Method methodq = Class.forName(GeoStatisticsProcessor.class.getName()).getMethod(formedReq.get("methodName").asText(), new Class[] {String.class});
			String methodOutput  =(String) methodq.invoke(GSA, objectMapper.writeValueAsString(formedReq));
			String msg=null;
			ArrayNode output = (ArrayNode)objectMapper.readTree(methodOutput);
			ArrayNode resParam = (ArrayNode)assistmentConfig.get("questionList").get(request.get("currentQuestionCode").asText()).get("responseParam");
			log.info("Method Output : "+output);

			//JsonDocument msgConfigDoc =serverConfig.getDocBYId("MIAAssessmentResponseMsg-"+request.get("professionCode").asText());
			JsonNode msgConfig = miaDocConfigList.get("miaScreenConfig").get("MIAAssessmentResponseMsg-"+request.get("professionCode").asText());
			log.info("docmune for responseeee..."+msgConfig);
			if(msgConfig!=null){
				log.info("questionRes.msgConfigDoc"+msgConfig);
				//JsonNode msgConfig = objectMapper.readTree(msgConfigDoc.content().toString());

				log.info("msgConfigDoc msgConfig"+msgConfig);
				if(msgConfig.has(request.get("currentQuestionCode").asText())){
					log.info("request.get(currentQuestionCode).asText()"+request.get("currentQuestionCode").asText());
					log.info("output size...."+output.size());
					if(output.size()==1){
						log.info("outtttputttt..."+output.findValue(msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText()).asInt());
						int outputValue = output.findValue(msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText()).asInt();
						double dividedValue = Math.round((outputValue / 100000));
						if( dividedValue >= 1 &&  dividedValue <=99 )
						{

							log.info("in lakh else if....");
							log.info("in lakh if else...."+dividedValue);
							JsonNode replaceNode = output.get(0);
							log.info("replaceNode else"+replaceNode);
							log.info("replace keys from document.."+msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText());
							((ObjectNode)replaceNode).put(msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText(), dividedValue+" Lakh");
							log.info("replaceNode after putting divied value else"+replaceNode);
							output.remove(0);
							output.add(replaceNode);
							log.info("output after adding else"+output);						}
						else if (dividedValue >= 100 &&  dividedValue <=999) 
						{   
							double dividedValueC = Math.round((outputValue / 10000000));
							log.info("in lakh else if....");
							log.info("in lakh if else...."+dividedValueC);
							JsonNode replaceNode = output.get(0);
							log.info("replaceNode else"+replaceNode);
							log.info("replace keys from document.."+msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText());
							((ObjectNode)replaceNode).put(msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText(), dividedValueC+" Crore");
							log.info("replaceNode after putting divied value else"+replaceNode);

							output.remove(0);
							output.add(replaceNode);
							log.info("output after adding else"+output);
						}
						else if (dividedValue >= 1000 &&  dividedValue <=9999) 
						{
							double dividedValueCR = Math.round((outputValue / 10000000));
							log.info("in lakh else if....");
							log.info("in lakh if else...."+dividedValueCR);
							JsonNode replaceNode = output.get(0);
							log.info("replaceNode else"+replaceNode);
							log.info("replace keys from document.."+msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText());
							((ObjectNode)replaceNode).put(msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText(), dividedValueCR+" Crore");
							log.info("replaceNode after putting divied value else"+replaceNode);

							output.remove(0);
							output.add(replaceNode);
							log.info("output after adding else"+output);
						}
						else
						{
							log.info("Value smaller than 100,000 OR  bigger than 10,00,00,000");
						}
						String replaceKey = msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText();
						msg = msgConfig.get(request.get("currentQuestionCode").asText()).get("msg").asText();
						msg = msg.replaceAll("<"+replaceKey+">", output.get(0).get(replaceKey).asText());
						log.info("msggg"+msg);

						((ObjectNode)questionRes).put("Data", msg);
						log.info("questionRes with data"+questionRes);
						if(msgConfig.get(request.get("currentQuestionCode").asText()).get("reqReplaceKeys") != null)
							//if(questionRes.get("Data").asText().contains("<"))
						{
							String a = questionRes.get("Data").asText();
							//ArrayNode reqReplaceKeys = (ArrayNode)msgConfigDoc.get("questionList").get(input.get("currentQuestionCode").asText()).get("reqReplaceKeys");
							ArrayNode reqReplaceKeys = (ArrayNode)msgConfig.get(request.get("currentQuestionCode").asText()).get("reqReplaceKeys");
							for(JsonNode reqParam : reqReplaceKeys){
								if(reqParam.has("reqNode")){
									log.info("rreqParam"+reqParam);
									log.info("rreqParam"+reqReplaceKeys);
									log.info("key to be rplace"+reqParam.get("reqParam").asText());
									log.info("value to be replace"+formedReq.get(reqParam.get("reqParam").asText()).asText());
									a = a.replaceAll("<"+reqParam.get("reqParam").asText()+">",formedReq.get(reqParam.get("reqParam").asText()).asText());

									log.info("rreqParam......replaced to a"+a);
									((ObjectNode)questionRes).put("Data", a);
									log.info("returnnnnnn valeeee"+questionRes);
								}
								log.info("returnnnnnn valeeee questionRes"+questionRes);
							}
						}
						return questionRes;
						//((ObjectNode) questionRes).put(resParam.get(0).get("resFieldName").asText(),output.get(0).get(resParam.get(0).get("resFieldName").asText()).asText());
					}
					else if(output.size()>1){
						log.info("replace keys has two values to be replace..."+output);
						/*for(JsonNode repalce : output)
						{
							String replaceKey = msgConfig.get(request.get("currentQuestionCode").asText()).get("replacesKeys").get(0).asText();
							msg = msgConfig.get(request.get("currentQuestionCode").asText()).get("msg").asText();
							msg = msg.replaceAll("<"+replaceKey+">", repalce.get(replaceKey).asText());
							//msg = msg.replaceAll("<"+replaceKey+">", output.get(0).get(replaceKey).asText());
							((ObjectNode)questionRes).put("Data", msg);
							if(questionRes.get("Data").asText().contains("<"))
							{
								String a = questionRes.get("Data").asText();

								a = a.	replaceAll("", formedReq.get("").asText());
							}
							return questionRes;
						}
						 */
					}
				}
				log.info("Generated msg : "+msg);
			}
		}catch(Exception e){
			log.error("unable to process Request in GetProfessionQuestionConfguration : ",e);
		}
		return questionRes;
	}
	public JsonNode generateRequest(JsonNode input,JsonNode config){
		ObjectNode requestNode = objectMapper.createObjectNode();
		try{
			if(input.has("currentQuestionCode")){
				ArrayNode paramList = (ArrayNode)config.get("questionList").get(input.get("currentQuestionCode").asText()).get("paramList");
				for(JsonNode reqParam : paramList){
					if(reqParam.has("resParam")){
						if(reqParam.has("reqNode")){
							requestNode.put(reqParam.get("resParam").asText(), input.get(reqParam.get("reqNode").asText()).findValue(reqParam.get("reqParam").asText()).asText());
						}else{
							requestNode.put(reqParam.get("resParam").asText(), input.get(reqParam.get("reqParam").asText()).asText());
						}
					}else{
						if(reqParam.has("reqNode")){
							requestNode.put(reqParam.get("reqParam").asText(),input.get(reqParam.get("reqNode").asText()).findValue(reqParam.get("reqParam").asText()).asText());
						}else{
							requestNode.put(reqParam.get("reqParam").asText(), input.get(reqParam.get("reqParam").asText()).asText());
						}
						log.info("requestNode"+requestNode);
					}
				}
				requestNode.put("methodName", config.get("questionList").get(input.get("currentQuestionCode").asText()).get("methodName").asText());
				log.info("requestNode with all values"+requestNode);
			}
		}catch(Exception e){
			log.error("unable to generate mia Assessment request : ",e);
		}
		return requestNode;
	}
	public static void main(String[] args) {

		GetProfessionQuestionConfiguration qc = new GetProfessionQuestionConfiguration();

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode data;
		try {
			data = objectMapper.readTree("{\"professionCode\":\"DR\",\"state\":\"Maharashtra\",\"currentQuestionCode\":\"DR001\"}");
			qc.getProfessionQuestionConfig(data);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
