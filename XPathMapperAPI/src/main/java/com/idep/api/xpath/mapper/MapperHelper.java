package com.idep.api.xpath.mapper;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.DocumentContext;
import com.idep.api.function.library.DataFunctions;

/**
 * XPathMapper Helper
 * @author sandeep.jadhav
 */

public class MapperHelper {
	
	DataFunctions libFunc = new DataFunctions();
	Logger log = Logger.getLogger(MapperHelper.class);
	MapperFunctions mapperfunction = new MapperFunctions();
	ObjectMapper objectMapper = new ObjectMapper();
	
	public void dataConfigMapper(String reqAttr,String reqType,DocumentContext context,JsonNode inputReqNode,JsonNode currentNode,JsonNode configNode, JsonNode funcArrNode,JsonNode srcFieldXpath,DocumentContext inputReqContext)throws Exception
	{
		if(reqType.equalsIgnoreCase("String"))
		{
			String attrValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
			
			for(JsonNode node : funcArrNode)
			{
				if(attrValue.equals(node.get("inputValue").textValue()))
				{
					context.set(currentNode.get("xpath").textValue(), node.get("absoluteValue").textValue()).json();
					break;
				}
			}
		}
		
		else if(reqType.equalsIgnoreCase("long"))
		{
			long attrValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).longValue();
			
			for(JsonNode node : funcArrNode)
			{
				if(attrValue>=node.get("startValue").doubleValue() &&
						attrValue<=node.get("endValue").doubleValue())
				{
					context.set(currentNode.get("xpath").textValue(), node.get("absoluteValue").textValue()).json();
					break;
				}
			}
		}
		else if(reqType.equalsIgnoreCase("double"))
		{
			double attrValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).doubleValue();
			
			for(JsonNode node : funcArrNode)
			{
				if(attrValue>=node.get("startValue").doubleValue() &&
						attrValue<=node.get("endValue").doubleValue())
				{
					context.set(currentNode.get("xpath").textValue(), node.get("absoluteValue").textValue()).json();
					break;
				}
			}
		}
		
	}
	
	
	public void setRequestRiders(String reqAttr,String reqType,DocumentContext context,JsonNode inputReqNode,JsonNode currentNode,JsonNode configNode, JsonNode funcArrNode,JsonNode srcFieldXpath,DocumentContext inputReqContext)throws Exception
	{
		
		JsonNode riderInput = inputReqNode.get(configNode.get("reqNode").textValue()).get(reqAttr);
		
		try {
			
		if(riderInput!=null)
		{
			ArrayNode UIRiders = (ArrayNode)riderInput;
			for(JsonNode rider:UIRiders)
			{
				if(rider.has("riderType"))
				{
					if((rider.get("riderType").asText().equalsIgnoreCase("NA")) || (rider.get("riderType").asText().equalsIgnoreCase("implicit")))
					{
						continue;
					}

				}
				
				JsonNode configRiderNode = funcArrNode.get(rider.get("riderId").asText());
				log.debug("MAPPER HELPER configRiderNode : "+configRiderNode);
				for(JsonNode node : configRiderNode)
				{
					if(node.has("riderAmountAttribute"))
					{
						if(node.has("type"))
						{
							if(node.get("type").textValue().equals("request"))
							{
								if(node.has("sourcexpath"))
								{ 
									
										context.set(node.get("xpath").textValue(),((JsonNode)inputReqContext.read(node.get("sourcexpath").textValue()))).json();
								}
								else
								{
									context.set(node.get("xpath").textValue(), rider.get(node.get("riderAmountAttribute").textValue())).json();
								}
								
							}
							else if(node.get("type").textValue().equals("INTTOSTRING"))
							{
								int riderAmount = rider.get(node.get("riderAmountAttribute").textValue()).intValue();
								context.set(node.get("xpath").textValue(), libFunc.getIntASString(riderAmount)).json();
							}
							
						}
						else
						{
							context.set(node.get("xpath").textValue(), rider.get(node.get("riderAmountAttribute").textValue())).json();
						}
					}else if( node.has("source") && node.get("type").textValue().equals("request")){
						
						if(node.get("type").textValue().equals("request") &&node.get("source").textValue().equals("requestmap")){
							srcFieldXpath=node.get("sourcexpath");
							mapperfunction.requestMapProcessor(context,node,inputReqContext,srcFieldXpath,configNode,inputReqNode,"");
						}
					}else
					{
						context.set(node.get("xpath").textValue(), node.get("value")).json();
					}
				}
				
			}
		}
		
		
		}
		catch(Exception e)
		{
			log.error("Exception at setRequestRiders : ", e);
		}
		
	}
	
	
	
	public void setHealthDiseaseQuestions(String reqAttr,String reqType,DocumentContext context,JsonNode inputReqNode,JsonNode currentNode,JsonNode configNode, JsonNode funcArrNode,JsonNode srcFieldXpath,DocumentContext inputReqContext)throws Exception
	{
		
		try {
			
			log.debug("setHealthDiseaseQuestions reqAttr : "+reqAttr);
			JsonNode diseaseQuestionList = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue()));
			log.debug("diseaseQuestionList : "+diseaseQuestionList);
			String DiseaseConcatString="";
			//ObjectNode diseaseQuestionListNode = objectMapper.createObjectNode();
			for(JsonNode diseaseQuesNode : diseaseQuestionList)
			{
				log.debug("setHealthDiseaseQuestions diseaseQuesNode : "+diseaseQuesNode);
				if(diseaseQuesNode.has("applicable"))
				{
					if(diseaseQuesNode.get("applicable").asText().equalsIgnoreCase("true")){
					if(funcArrNode.has("default"))
					{
						JsonNode defaultConfigNode = funcArrNode.get("default");
						if(defaultConfigNode!=null)
						{
							for(JsonNode disNode : defaultConfigNode)
							{
								if(disNode.has("value"))
								{
									context.set(disNode.get("xpath").textValue(), disNode.get("value")).json();
								}
								
							}
						}
					}
					
					JsonNode diseaseConfigNode = funcArrNode.get(diseaseQuesNode.get(currentNode.get("searchField").asText()).asText());
					log.debug("diseaseConfigNode : "+diseaseConfigNode);
					if(diseaseConfigNode!=null)
					{
						for(JsonNode disNode : diseaseConfigNode)
						{
							if(disNode.has("value"))
							{
								if(disNode.has("concatType")){
									
										DiseaseConcatString=DiseaseConcatString+disNode.get("value").asText()+disNode.get("concatType").asText();
									
								}else{
									context.set(disNode.get("xpath").textValue(), disNode.get("value")).json();	
								}
							}
							else if(disNode.has("source"))
							{
								if(disNode.get("source").asText().equalsIgnoreCase("request")&&
										disNode.get("type").asText().equalsIgnoreCase("request"))
								{
									context.set(disNode.get("xpath").textValue(),diseaseQuesNode.get(disNode.get("field").asText()) ).json();
								}
								else if(disNode.get("source").asText().equalsIgnoreCase("request")&&
										disNode.get("type").asText().equalsIgnoreCase("DATE"))
								{
									context.set(disNode.get("xpath").textValue(),libFunc.getFormattedDate(diseaseQuesNode.get(disNode.get("field").asText()).asText(),disNode.get("dateFormat").textValue())).json();
								}else if(disNode.get("source").asText().equalsIgnoreCase("requestmap")&&
										disNode.get("type").asText().equalsIgnoreCase("request"))
								{
									/**
									 * here key is value of diseaseQuesNode.get(disNode.get("field").asText())
									 * */
									String key =  diseaseQuesNode.get(disNode.get("field").asText()).asText();
									if(disNode.has("mappingConfig")){
									context.set(disNode.get("xpath").textValue(),disNode.get("mappingConfig").get(key).asText()).json();
									}
									
								}
							}
							
							
						}
					}
					}
				}
				
			}
			if(DiseaseConcatString.length()>0){
				if(funcArrNode.has("xpath")){
					log.debug("Health Concated disease  : "+DiseaseConcatString);
				context.set(funcArrNode.get("xpath").asText(),DiseaseConcatString.substring(0, (DiseaseConcatString.length()-1))).json();
				}
			}
		}
		catch(Exception e)
		{
			log.error("Exception at setHealthDiseaseQuestions : ", e);
		}
		
	}
	
	public void setHealthRider(String reqAttr,String reqType,DocumentContext context,JsonNode inputReqNode,JsonNode currentNode,JsonNode configNode, JsonNode funcArrNode,JsonNode srcFieldXpath,DocumentContext inputReqContext)throws Exception
	{
		
		try {
			
			log.debug("setHealthRider reqAttr : "+reqAttr);
			ArrayNode inputReq = ((ArrayNode)inputReqContext.read(srcFieldXpath.asText()));
			//log.debug("inputReqNode inputReqNode setHealthRider :  : "+inputReqContext.read(srcFieldXpath.asText()));
			if(inputReq.size()>0){
				JsonNode riderList = inputReqContext.read(srcFieldXpath.asText());
			//JsonNode riderList = inputReqNode.get(srcFieldXpath.asText());
			log.debug("RiderList : "+riderList); 
			String DiseaseConcatString="";
			//ObjectNode diseaseQuestionListNode = objectMapper.createObjectNode();
			if(riderList.size()>0)
			{
			for(JsonNode riderNode : riderList)
			{
				JsonNode riderConfigNode = funcArrNode.get(riderNode.get(currentNode.get("searchField").asText()).asText());
				log.debug("diseaseConfigNode : "+riderConfigNode);
				if(riderConfigNode!=null)
				{
					for(JsonNode riderConfNode : riderConfigNode)
					{
						if(riderConfNode.has("source"))
						{
							if(riderConfNode.get("source").asText().equalsIgnoreCase("request")&&
									riderConfNode.get("type").asText().equalsIgnoreCase("default"))
							{
								log.debug("riderConfNode.get(xpath).textValue() : "+riderConfNode.get("xpath").textValue());
								context.set(riderConfNode.get("xpath").textValue(),riderConfNode.get("value").textValue()).json();
							}
						}
						
						}
					}
			}
			}
			}//if has condition	
		}catch(Exception e)
		{
			log.error("Exception at setHealthRider : ", e);
		}
	}
	
	
	
}