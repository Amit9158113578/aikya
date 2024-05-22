package com.idep.api.xpath.mapper;

import java.text.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.idep.api.function.library.DataFunctions;
import com.idep.api.xpath.util.XPathMapConstants;







import org.apache.log4j.Logger;

/**
 * XPathMapper API
 * @author sandeep.jadhav
 */
public class XPathMapper {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(XPathMapper.class);
	DataFunctions libFunc = new DataFunctions();
	Configuration configuration = Configuration.builder()
		    .jsonProvider(new JacksonJsonNodeJsonProvider())
		    .mappingProvider(new JacksonMappingProvider())
		    .build();
	
	public JsonNode updateRequest(ArrayNode masterFieldList, JsonNode inputRequest, JsonNode sampleRequest, JsonNode configurations) throws MapperException
	{
		DocumentContext context = null;
		DocumentContext inputReqContext = null;
		//Add debug statements for all input fields assuming data is huge in it.
		log.debug("Input field details: \nMaster Field List : "+masterFieldList.toString()+"\nInput Request : "+inputRequest.toString()+"\n Sample Request : "+sampleRequest.toString()+"\n Configurations : "+configurations.toString());
		context =  JsonPath.using(configuration).parse(sampleRequest.toString());
		inputReqContext = JsonPath.using(configuration).parse(inputRequest.toString());
		MapperFunctions mapperfunction = new MapperFunctions(); 
		
		for(JsonNode fieldArray : masterFieldList)
		{
			String inputField = fieldArray.get("field").textValue();
			//Pravin - If field list attribute is missing. Ex. DocumentType. 
			if(configurations.has(inputField) && configurations.get(inputField).has("fields"))
			{
			  JsonNode configNode = configurations.get(inputField);//selectFM
			  for(JsonNode currentNode : configNode.get("fields"))
			  {
					JsonNode srcFieldXpath = fieldArray.get("xpath");
					if(currentNode.get("source").textValue().equalsIgnoreCase("request"))
					{

						if(currentNode.get("type").textValue().equalsIgnoreCase("request"))
						{
							context=mapperfunction.requestProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("createpath"))
						{
							context=mapperfunction.createpathProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("default"))
						{
							context.set(currentNode.get("xpath").textValue(), currentNode.get("value")).json();
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("double"))
						{
							context=mapperfunction.doubleProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);							
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("String"))
						{
							context=mapperfunction.stringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("SYSDATE"))
						{
							// set current date in required date format
							try {
								context.set(currentNode.get("xpath").textValue(), libFunc.getSYSDate(currentNode.get("dateFormat").textValue())).json();
							} catch (ParseException e) {
								throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code :"+XPathMapConstants.JSONPARSEEXPCETION);
							}
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("DATE"))
						{
							context=mapperfunction.dateProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("BACKDATE"))
						{
							context=mapperfunction.backDateProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("POLICYSTARTDATE"))
						{
							context=mapperfunction.policyStartDateProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("POLICYENDDATE"))
						{
							context=mapperfunction.policyEndDateProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("STRINGTOINT"))
						{
							context=mapperfunction.stringtoIntProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("STRINGTOLONG"))
						{
							context=mapperfunction.stringtoLongProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("STRINGTODOUBLE"))
						{
							context=mapperfunction.stringtoDoubleProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("INTTOSTRING"))
						{
							context=mapperfunction.inttoStringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("LONGTOSTRING"))
						{
							context=mapperfunction.longtoStringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("DOUBLETOSTRING"))
						{
							context=mapperfunction.doubletoStringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("REMOVECHAR"))
						{
							context=mapperfunction.removeCharProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);	
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("CONCATSTRING"))
						{
							context=mapperfunction.concatStringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("GETSUBSTRING"))
						{
							context=mapperfunction.subStringProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("CONCATNUMBER"))
						{
							context=mapperfunction.concatNumberProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}else if(currentNode.get("type").textValue().equalsIgnoreCase("getAge"))
						{
							context=mapperfunction.getAge(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}else if(currentNode.get("type").textValue().equalsIgnoreCase("CONCATARRAYFIELD"))
						{
								/****
								 * concat array field by user defined concat operator/special symbol eg. insured member array dob concat by , symbol/operator 
								 * **/
							context=mapperfunction.concatArrayFieldProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("ARRAY"))
						{
							if(srcFieldXpath!=null) {
							JsonNode inputsrcFieldNode = inputReqContext.read(srcFieldXpath.textValue());
							
							if(inputsrcFieldNode.size()>0)
							{
								
							JsonNode targetReqNode = ((JsonNode)context.read(currentNode.get("xpath").textValue())).get(0);
							ArrayNode finalArray = objectMapper.createArrayNode();
							for(JsonNode inputNode : inputsrcFieldNode)
							{
								DocumentContext inputNodeContext = null;
								inputNodeContext =  JsonPath.using(configuration).parse(inputNode.toString());
								//Get first element of array
								DocumentContext targetReqContext = JsonPath.using(configuration).parse(targetReqNode.toString());
								
								for(JsonNode currentArrayNode : currentNode.get("arrayFields"))
								{
								  if(currentArrayNode.get("source").textValue().equalsIgnoreCase("request"))
								  {
									if(currentArrayNode.get("type").textValue().equalsIgnoreCase("request"))
									{
										if(currentArrayNode.has("requestxpath"))
										{
											targetReqContext=mapperfunction.requestProcessor(targetReqContext,currentArrayNode,inputReqContext,currentArrayNode.get("requestxpath"),currentArrayNode,inputNode,inputField);
										}
										else
										{
											targetReqContext=mapperfunction.requestProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
										}
										
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("createpath"))
									{
										targetReqContext=mapperfunction.createpathProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("default"))
									{
										targetReqContext.set(currentArrayNode.get("xpath").textValue(), currentArrayNode.get("value")).json();
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("double"))
									{
										targetReqContext=mapperfunction.doubleProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("String"))
									{
										targetReqContext=mapperfunction.stringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("SYSDATE"))
									{
										// set current date in required date format
										try {
											targetReqContext.set(currentArrayNode.get("xpath").textValue(), libFunc.getSYSDate(currentArrayNode.get("dateFormat").textValue())).json();
										} catch (ParseException e) {
											throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code :"+XPathMapConstants.JSONPARSEEXPCETION);
										}
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("DATE"))
									{
										targetReqContext=mapperfunction.dateProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("BACKDATE"))
									{
										targetReqContext=mapperfunction.backDateProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("STRINGTOINT"))
									{
										targetReqContext=mapperfunction.stringtoIntProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("STRINGTOLONG"))
									{
										targetReqContext=mapperfunction.stringtoLongProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("STRINGTODOUBLE"))
									{
										targetReqContext=mapperfunction.stringtoDoubleProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("INTTOSTRING"))
									{
										targetReqContext=mapperfunction.inttoStringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("LONGTOSTRING"))
									{
										targetReqContext=mapperfunction.longtoStringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("DOUBLETOSTRING"))
									{
										targetReqContext=mapperfunction.doubletoStringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("REMOVECHAR"))
									{
										targetReqContext=mapperfunction.removeCharProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);	
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("CONCATSTRING"))
									{
										targetReqContext=mapperfunction.concatStringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("GETSUBSTRING"))
									{
										targetReqContext=mapperfunction.subStringProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									else if(currentArrayNode.get("type").textValue().equalsIgnoreCase("CONCATNUMBER"))
									{
										targetReqContext=mapperfunction.concatNumberProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}else if(currentNode.get("type").textValue().equalsIgnoreCase("getAge"))
										{
										targetReqContext=mapperfunction.getAge(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
										}
									}
								
								else if(currentArrayNode.get("source").textValue().equalsIgnoreCase("requestmap")&&
										currentArrayNode.get("type").textValue().equalsIgnoreCase("request"))
									{/*
									log.info("currentArrayNode : "+currentArrayNode);
										targetReqContext=mapperfunction.requestMapProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);*/
									if(currentArrayNode.has("requestxpath"))
									{
										targetReqContext=mapperfunction.requestMapProcessor(targetReqContext,currentArrayNode,inputReqContext,currentArrayNode.get("requestxpath"),currentArrayNode,inputNode,inputField);
									}
									else
									{
										targetReqContext=mapperfunction.requestMapProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
									}
									}
									
								else if(currentArrayNode.get("source").textValue().equalsIgnoreCase("function"))
									{
										
										if(currentArrayNode.has("requestxpath"))
										{
											targetReqContext=mapperfunction.functionProcessor(targetReqContext,currentArrayNode,inputReqContext,currentArrayNode.get("requestxpath"),currentArrayNode,inputNode,inputField);
										}
										else
										{
											targetReqContext=mapperfunction.functionProcessor(targetReqContext,currentArrayNode,inputNodeContext,currentArrayNode.get("arrayFieldXpath"),currentArrayNode,inputNode,inputField);
										}
									}
								else{
										throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.FIELDNOTFOUNDEXCEPTION );							
									}
								}
						
								ObjectNode arrNode = objectMapper.createObjectNode();
								arrNode.putAll((ObjectNode)targetReqContext.json());
								finalArray.add(arrNode);
								
								
							}
							
							context.set(currentNode.get("xpath").textValue(),finalArray).json();
						}
						}
						
						
						else{
							
							log.error("provided array doest not contain any values hence not mapped");
						}
						}
					}
					
									
					
					else if(currentNode.get("source").textValue().equalsIgnoreCase("carrierRequest"))
					{
						if(currentNode.get("type").textValue().equalsIgnoreCase("carrierRequest"))
						{
							context=mapperfunction.carrierRequestProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("replaceKeyNode"))
						{
							context=mapperfunction.replaceKeyNodeProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField,objectMapper);
						}
						else if(currentNode.get("type").textValue().equalsIgnoreCase("singleKeyNode"))
						{
							context.set(currentNode.get("xpath").textValue(),((JsonNode)inputReqContext.read(currentNode.get("carrierNodeXpath").textValue()))).json();
						}else{
							throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.FIELDNOTFOUNDEXCEPTION );
						}
					}
					else if(currentNode.get("source").textValue().equalsIgnoreCase("requestmap")&&
							currentNode.get("type").textValue().equalsIgnoreCase("request"))
					{
						context=mapperfunction.requestMapProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
					}
					else if(currentNode.get("source").textValue().equalsIgnoreCase("function"))
					{
						context=mapperfunction.functionProcessor(context,currentNode,inputReqContext,srcFieldXpath,configNode,inputRequest,inputField);
					}else{
						throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.FIELDNOTFOUNDEXCEPTION );
					}
				}
			}
			else
			{
				// selected request attribute is not applicable as per configuration
			}
		}
		//this.log.info("Process complete successfully  : "+context.json().toString());
		return context.json();
	}
	
}