package com.idep.api.xpath.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.function.library.DataFunctions;
import com.idep.api.xpath.util.XPathMapConstants;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;

/*
 * @author pravin.jakhi
 * 
*/

public class MapperFunctions {

	DataFunctions libFunc = new DataFunctions();
	Logger log = Logger.getLogger(MapperFunctions.class);
	
	DocumentContext requestProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		if (srcFieldXpath != null && currentNode != null
				&& inputReqContext != null) {
			context.set(
					currentNode.get("xpath").textValue(),
					((JsonNode) inputReqContext.read(srcFieldXpath.textValue())))
					.json();
		} else {
			if (inputReqNode.findValue(configNode.get("reqNode").textValue())
					.has(inputField)) {
				context.set(
						currentNode.get("xpath").textValue(),
						inputReqNode.findValue(
								configNode.get("reqNode").textValue()).get(
								inputField)).json();
	}
		}
		
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}

		return context;
	}
	 
	DocumentContext createpathProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		if( srcFieldXpath != null)
		{
			String inputStr = currentNode.get("xpath").textValue();
			inputStr = inputStr.replace(".", ",");
			String path [] = inputStr.split(",");
			String xpath = "";
			for(int i=0;i<(path.length-1);i++)
			{
				xpath = xpath.concat(path[i]).concat(".");
			}
			if(currentNode.has("value"))
			{
				context.put(xpath.substring(0, (xpath.length()-1)), path[(path.length-1)], currentNode.get("value")).json();
			}else
			{
				if((JsonNode)inputReqContext.read(srcFieldXpath.textValue())!=null)
				{
					context.put(xpath.substring(0, (xpath.length()-1)), path[(path.length-1)], ((JsonNode)inputReqContext.read(srcFieldXpath.textValue()))).json();
				}
				
			}
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext doubleProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		double value = 0;
		if( srcFieldXpath != null)
		{
			value = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).doubleValue();
		}else
		{
			if(inputReqNode.findValue(configNode.get("reqNode").textValue()).has(inputField))
			{
				value = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).doubleValue();
			}
		}
		
		if(value!=0)
		{
			if(currentNode.has("roundOff"))
			{
				context.set(currentNode.get("xpath").textValue(), Math.round(value)).json();
			}else
			{
				context.set(currentNode.get("xpath").textValue(), value).json();
			}
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	
	DocumentContext stringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String str = "";
		if( srcFieldXpath != null)
		{
			str = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}else
		{
			str = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		if(currentNode.has("prefix"))
		{
			str = currentNode.get("prefix").textValue().concat(str);
			context.set(currentNode.get("xpath").textValue(),str).json();
		}else
		{
			context.set(currentNode.get("xpath").textValue(),str).json();
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext dateProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) throws MapperException {
		try {
		String date = "";
		if( srcFieldXpath != null)
		{
			date = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}else
		{
			date = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// convert provided date to required date format
			context.set(currentNode.get("xpath").textValue(), libFunc.getFormattedDate(date,currentNode.get("dateFormat").textValue())).json();
		} catch (ParseException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"- Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.JSONPARSEEXPCETION );
		}
		catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext backDateProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField)throws MapperException {
		try {
			String date = "";
		if( srcFieldXpath != null)
		{
			date = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}else
		{
			date = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// provide back date based on given input date and no of days
			context.set(currentNode.get("xpath").textValue(), libFunc.getBackDate(date,currentNode.get("dateFormat").textValue())).json();
		} catch (ParseException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.JSONPARSEEXPCETION );
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext policyStartDateProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField)throws MapperException {
		try {
			String date = "";
			date = libFunc.getSYSDate(currentNode.get("dateFormat").textValue());
		// provide Start date based on given input date and no of days
			context.set(currentNode.get("xpath").textValue(), libFunc.getPolicyStartDate(date,currentNode.get("dateFormat").textValue())).json();
		} catch (ParseException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.JSONPARSEEXPCETION );
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext policyEndDateProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField)throws MapperException {
		try {
			String date = "";
			date = libFunc.getSYSDate(currentNode.get("dateFormat").textValue());
		// provide End date based on given input date and no of days
			context.set(currentNode.get("xpath").textValue(), libFunc.getPolicyEndDate(date,currentNode.get("dateFormat").textValue())).json();
		} catch (ParseException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.JSONPARSEEXPCETION );
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}

	DocumentContext stringtoIntProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String value = "";
		if( srcFieldXpath != null)
		{
			value = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}
		else
		{
			value = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// convert string value to integer
		context.set(currentNode.get("xpath").textValue(), libFunc.getStringAsInt(value)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext stringtoLongProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String value = "";
		if( srcFieldXpath != null)
		{
			value = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}
		else
		{
			value = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// convert string value to long
		context.set(currentNode.get("xpath").textValue(), libFunc.getStringAsLong(value)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext stringtoDoubleProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String strValue = "";
		if( srcFieldXpath != null)
		{
			strValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}
		else
		{
			strValue = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// convert string value to double
		double value = libFunc.getStringAsDouble(strValue);
		if(currentNode.has("roundOff"))
		{
			context.set(currentNode.get("xpath").textValue(),Math.round(value)).json();
		}
		else
		{
			context.set(currentNode.get("xpath").textValue(),value).json();
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext inttoStringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		int intValue = 0;
		if( srcFieldXpath != null)
		{
			intValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).intValue();
		}
		else
		{
			intValue = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).intValue();
		}
		// convert integer value to string
		context.set(currentNode.get("xpath").textValue(), libFunc.getIntASString(intValue)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext longtoStringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		long longValue = 0;
		if( srcFieldXpath != null)
		{
			longValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).longValue();
		}
		else
		{
			longValue = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).longValue();
		}
		// convert long value to string
		context.set(currentNode.get("xpath").textValue(), libFunc.getLongASString(longValue)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext doubletoStringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		double doubleValue = 0;
		if( srcFieldXpath != null)
		{
			doubleValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).doubleValue();
		}
		else
		{
			doubleValue = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).doubleValue();
		}
		// convert double value to string
		if(currentNode.has("roundOff"))
		{
			doubleValue =  Math.round(doubleValue);
			context.set(currentNode.get("xpath").textValue(), libFunc.getDoubleASString(doubleValue)).json();
		}
		else
		{
			context.set(currentNode.get("xpath").textValue(), libFunc.getDoubleASString(doubleValue)).json();
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext removeCharProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String strValue = "";
		if( srcFieldXpath != null)
		{
			strValue = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}
		else
		{
			strValue = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		strValue = libFunc.removeChar(strValue, currentNode.get("char").textValue());
		context.set(currentNode.get("xpath").textValue(), strValue).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext concatStringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) throws MapperException {
		try{
		String[] concatAttributes = currentNode.get("concatAttributes").textValue().split(",");
		String[] strArray = new String[concatAttributes.length];
		String concatBy = " ";
		if(currentNode.has("concatBy"))
		{
		   concatBy = currentNode.get("concatBy").asText();
		}
		
		if(configNode.has("reqNode"))
		 {
			for(int i=0;i<strArray.length;i++)
			{
				if(inputReqNode.findValue(configNode.get("reqNode").textValue()).has(concatAttributes[i]))
				{
					strArray[i] = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(concatAttributes[i]).textValue();
				}	
			}
			if(inputReqNode.findValue(configNode.get("reqNode").textValue()).has(concatAttributes[0]))
			{
				String str = libFunc.concatString(strArray,concatBy);
				context.set(currentNode.get("xpath").textValue(),str.substring(0, (str.length()-1))).json();
			}
		 }
		else
		{
			for(int i=0;i<strArray.length;i++)
			{
				try {
					
					if(((JsonNode)inputReqContext.read(concatAttributes[i])).isTextual())
					{
						strArray[i] = ((JsonNode)inputReqContext.read(concatAttributes[i])).textValue();
					}
					else if(((JsonNode)inputReqContext.read(concatAttributes[i])).isDouble())
					{
						strArray[i] = ((Number)((JsonNode)inputReqContext.read(concatAttributes[i])).doubleValue()).toString();
					}
					else if(((JsonNode)inputReqContext.read(concatAttributes[i])).isLong())
					{
						strArray[i] = ((Number)((JsonNode)inputReqContext.read(concatAttributes[i])).longValue()).toString();
					}
					else if(((JsonNode)inputReqContext.read(concatAttributes[i])).isInt())
					{
						strArray[i] = ((Number)((JsonNode)inputReqContext.read(concatAttributes[i])).intValue()).toString();
					}
				
				}
				catch(PathNotFoundException e)
				{
					//intentionally this exception being handled. if field is missing will concatenate below string mentioned. 
					strArray[i] = "";
					log.error("path not found for concatenation : "+concatAttributes[i]);
				}
				catch(Exception e)
				{
					log.error("Exception while concatenating : "+concatAttributes[i]);
				}
			}
			if(!(strArray[0] == null))
			{
				String str = libFunc.concatString(strArray,concatBy);
				context.set(currentNode.get("xpath").textValue(),str.substring(0, (str.length()-1))).json();
			}
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	
	DocumentContext subStringProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) throws MapperException {
		
		try{
			
			String str = "";
			
			str = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
			if(str.length()<=currentNode.get("endIndex").asInt())
			{
				context.set(currentNode.get("xpath").textValue(),str).json();
			}
			else
			{
				context.set(currentNode.get("xpath").textValue(),str.substring(0, (currentNode.get("endIndex").asInt()))).json();
			}
			
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext concatNumberProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String[] concatAttributes = currentNode.get("concatAttributes").textValue().split(",");
		Double[] strArray = new Double[concatAttributes.length];
		
			for(int i=0;i<strArray.length;i++)
			{
				try{
				strArray[i] = ((JsonNode)inputReqContext.read(concatAttributes[i])).doubleValue();
				}catch(PathNotFoundException e){
					//intentionally this exception being handled. if field is missing will concatenate below string mentioned.
					strArray[i]=0.0;
				}
			}
			
			if(!(strArray[0] == null))
			{
				double num = libFunc.concatNumber(strArray);
				if(currentNode.has("roundOff"))
				{
					context.set(currentNode.get("xpath").textValue(),Math.round(num)).json();
				}
				else
				{
					context.set(currentNode.get("xpath").textValue(),num).json();
				}
			}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext requestMapProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
		String nodeKey = "";
		if( srcFieldXpath != null)
		{
			nodeKey = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).asText();
		}
		else
		{
			nodeKey = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).asText();
		}

		context.set(currentNode.get("xpath").textValue(),currentNode.get("mappingConfig").get(nodeKey)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext carrierRequestProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) {
		try{
			JsonNode carrierReqNode = null;
		
		if(currentNode.has("carrierNodeXpath"))
		{
			carrierReqNode = ((JsonNode)inputReqContext.read(currentNode.get("carrierNodeXpath").textValue()));
			context.set(currentNode.get("xpath").textValue(),carrierReqNode).json();
		}
		else
		{
			if( srcFieldXpath != null)
			{
				carrierReqNode = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue()));
			}
			else
			{
				carrierReqNode = inputReqNode.findValue(currentNode.get("reqNode").textValue()).get(currentNode.get("carrierReqNode").textValue());
			}
			
			context.set(currentNode.get("xpath").textValue(),carrierReqNode).json();
		}
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext replaceKeyNodeProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField,ObjectMapper objectMapper) {
		try{
		JsonNode carrierInputReq = inputReqNode.findValue(currentNode.get("reqNode").textValue()).get(currentNode.get("carrierReqNode").textValue());
		ObjectNode sampleReqInputNode = context.read(currentNode.get("xpath").textValue());
		
		@SuppressWarnings("unchecked")
		Map<String,Object> carrierReqMap = objectMapper.convertValue(carrierInputReq, Map.class);
		@SuppressWarnings("unchecked")
		Map<String,Object> sampleReqMap = objectMapper.convertValue(sampleReqInputNode, Map.class);
		for(Map.Entry<String, Object> map: carrierReqMap.entrySet())
		{
			if(sampleReqInputNode.has(map.getKey()))
			{
				sampleReqMap.put(map.getKey(),map.getValue());
			}
		}
		context.set(currentNode.get("xpath").textValue(),objectMapper.convertValue(sampleReqMap, JsonNode.class)).json();
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext functionProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) throws MapperException {
		
		Method method;
					try {
						method = Class.forName(XPathMapConstants.MAPPER_HELPER_CLASS).getMethod(currentNode.get("functionName").textValue(),String.class,String.class,DocumentContext.class,JsonNode.class,JsonNode.class,JsonNode.class,JsonNode.class,JsonNode.class,DocumentContext.class);
					} catch (NoSuchMethodException e1) {
						throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.NOSUCHMETHODEXCEPTION );
					} catch (SecurityException e1) {
						throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.SECURITYEXCEPTION );
					} catch (ClassNotFoundException e1) {
						throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.CLASSNOTFOUNDEXCEPTION );
					}
		MapperHelper helper = new MapperHelper();
		//pass values
		try {
			
				method.invoke(helper, inputField,currentNode.get("type").textValue(),context,inputReqNode,currentNode,configNode,currentNode.get(currentNode.get("functionNode").textValue()),srcFieldXpath,inputReqContext);
		} catch (IllegalAccessException e){
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.ILLEGALACCESSEXCEPTION );
		}catch(IllegalArgumentException e){
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.ILLEGALARGUMENTEXCEPTION );		
		}catch (InvocationTargetException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"Configuration not found for type request field : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.INVOCATIONTARGETEXCEPTION );
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;	
	}
	
	DocumentContext getAge(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField)throws MapperException {
		try {
			String date = "";
		if( srcFieldXpath != null)
		{
			date = ((JsonNode)inputReqContext.read(srcFieldXpath.textValue())).textValue();
		}else
		{
			date = inputReqNode.findValue(configNode.get("reqNode").textValue()).get(inputField).textValue();
		}
		// provide age  based on given input date
			context.set(currentNode.get("xpath").textValue(), libFunc.getAge(date)).json();
		} catch (ParseException e) {
			throw new MapperException(XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Unable to parse date : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.JSONPARSEEXPCETION );
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	DocumentContext concatArrayFieldProcessor(DocumentContext context,
			JsonNode currentNode, DocumentContext inputReqContext,
			JsonNode srcFieldXpath, JsonNode configNode, JsonNode inputReqNode,
			String inputField) throws MapperException {
		try{
			String concatBy=" ";
		ArrayNode inputArray  = (ArrayNode)inputReqContext.read(currentNode.get("arrayxpath").asText());
		String 	arrayFieldValue[] = null ;
		/***
		 * current node in checking concatBy present or not, if not present then concat by space by default
		 * **/
		if(currentNode.has("concatBy"))
		{
		   concatBy = currentNode.get("concatBy").asText();
		}
		if(currentNode.has("default")){
			if(inputArray !=null && inputArray.size()>0){
				arrayFieldValue= new String[inputArray.size()];
				for(int i=0;i<inputArray.size();i++)
				{
						arrayFieldValue[i]=currentNode.get("default").asText();
				}
			}//inputArray size if condition END
			
		}
		/***
		 * concat node by checking searchArrayFieldName and searchArrayFieldValue present in inputArray or not, if not present do not concat node 
		 * **/
		else if(currentNode.has("searchArrayFieldName") && currentNode.has("searchArrayFieldValue"))
		{
			String str = null;
			String concateArrayField = null;
			if(inputArray !=null && inputArray.size()>0){
 				for(int i=0;i<inputArray.size();i++)
				{
					if(inputArray.get(i).has(currentNode.get("concatArrayFieldName").asText()) && inputArray.get(i).has(currentNode.get("searchArrayFieldName").asText())){
						if(inputArray.get(i).get(currentNode.get("searchArrayFieldName").asText()).asText().equalsIgnoreCase(currentNode.get("searchArrayFieldValue").asText()))
						{
							concateArrayField = inputArray.get(i).get(currentNode.get("concatArrayFieldName").asText()).asText();
							if(str!=null)
							{
								str = str.concat(concatBy).concat(concateArrayField);
							}
							else
							{
								str = concateArrayField;
								//first time when str is null, initialize str with first concate node
							}
						}
					}
				}
				context.set(currentNode.get("xpath").textValue(),str.substring(0, (str.length()-1))).json();
			}//inputArray size if condition END
		}
		
		else if(currentNode.has("concatArrayFieldName"))
		{
			if(inputArray !=null && inputArray.size()>0){
				arrayFieldValue= new String[inputArray.size()];
				for(int i=0;i<inputArray.size();i++)
				{
					if(inputArray.get(i).has(currentNode.get("concatArrayFieldName").asText())){					
						arrayFieldValue[i]=inputArray.get(i).get(currentNode.get("concatArrayFieldName").asText()).asText();
					}
				}
			}//inputArray size if condition END
		}
		if(arrayFieldValue!=null){
			String str = libFunc.concatString(arrayFieldValue,concatBy);
			context.set(currentNode.get("xpath").textValue(),str.substring(0, (str.length()-1))).json();
		}
		
		
		
		/*String[] strArray = new String[concatAttributes.length];
		String concatBy = " ";
			if(!(strArray[0] == null))
			{
				String str = libFunc.concatString(strArray,concatBy);
				context.set(currentNode.get("xpath").textValue(),str.substring(0, (str.length()-1))).json();
			}
		*/
			
			
			
		}catch(PathNotFoundException e){
			log.error (XPathMapper.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+" - Path not found for : "+currentNode.get("xpath").textValue()+" Error code : "+XPathMapConstants.PATHNOTFOUNDEXCEPTION);
		}
		return context;
	}
	
	
	
	
}
