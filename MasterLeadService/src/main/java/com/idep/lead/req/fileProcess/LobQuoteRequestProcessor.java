package com.idep.lead.req.fileProcess;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.profession.request.validation.PBReqConverterValidation;
import com.idep.sugar.impl.rest.ExecutionTerminator;

/****
 * 
 * @author kuldeep.patil
 * @Date 03-06-2019
 *
 */

public class LobQuoteRequestProcessor implements Processor {

	ObjectMapper mapper =new ObjectMapper();
	static CBService serverConfig;
	Logger log=Logger.getLogger(LobQuoteRequestProcessor.class);
	static JsonNode ApplicableLobDocConfig=null;
    static ArrayNode P365CarMasterArray;
    static ArrayNode P365BikeMasterArray;
	static
	{
		ObjectMapper mapper =new ObjectMapper();
		serverConfig=CBInstanceProvider.getServerConfigInstance();
		try {
			ApplicableLobDocConfig = mapper.readTree(serverConfig.getDocBYId("ApplicableLobDocConfig").content().toString());
			List<Map<String, Object>> carexecuteQuery = serverConfig.executeQuery(ApplicableLobDocConfig.get("Car").get("Query").textValue());
			P365CarMasterArray = (ArrayNode)mapper.convertValue(carexecuteQuery, ArrayNode.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try
		{
			String requestQName=null;
			CamelContext context = exchange.getContext();
			ProducerTemplate template = context.createProducerTemplate();
			String reqString = exchange.getIn().getBody().toString();
			JsonNode requestNode = mapper.readTree(reqString);
			if(requestNode.has("calcQuote"))
			{
				
				if(requestNode.get("calcQuote").asBoolean())
				{
				    ((ObjectNode)requestNode).put("lob",ApplicableLobDocConfig.get("lob").get(requestNode.findValue("quoteType").asText()).textValue());
				    requestNode=new LobQuoteRequestProcessor().getVariantDetails(requestNode);
				    requestQName = ApplicableLobDocConfig.get(requestNode.findValue("quoteType").asText()).textValue();
					if(requestQName!=null)
					{
						log.info("queue request :"+requestNode);
						String uri = "activemq:queue:"+requestQName;
						exchange.getIn().setBody(requestNode.toString());
						exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
			            template.send(uri, exchange);
					}
					else
					{
						log.error("LOB QName not found for quoteType :"+requestNode.findValue("quoteType").asText());
					}
				}
			}
			else
			{
				log.error("calcQuote flage not found in leadRequest :"+requestNode);
			}
			
		}
		catch(NullPointerException e)
		{
			log.error("NullPointerException in LobQuoteRequestProcessor :",e);
			log.error(Thread.currentThread().getStackTrace()[0].getMethodName()+""+Thread.currentThread().getStackTrace()[1].getLineNumber()+"NullPointerException in LobQuoteRequestProcessor");
			new ExecutionTerminator();
		}
		catch(Exception e)
		{
			log.error("Exception in LobQuoteRequestProcessor :",e);
			log.error(Thread.currentThread().getStackTrace()[0].getMethodName()+""+Thread.currentThread().getStackTrace()[1].getLineNumber()+"Exception in LobQuoteRequestProcessor");
			new ExecutionTerminator();
		}
		
	}
	
	public JsonNode getVariantDetails(JsonNode requestNode) throws Exception
	{
	  try{
			   log.info("requestNode in getVariantDetails :"+requestNode);
			   JsonNode lobInfo = requestNode.get(requestNode.get("lob").textValue().toLowerCase()+"Info");
               String RTOCode = new PBReqConverterValidation().createRTOCodeForMotor(lobInfo);
		      ((ObjectNode)lobInfo).put("RTOCode",RTOCode);
	          ((ObjectNode)lobInfo).put("registrationPlace",lobInfo.get("registrationNumber").textValue());
		      if(requestNode.get("lob").textValue().equals("Car"))
		       {
		         if(!lobInfo.has("variantId"))
			     {
				   log.info("query response :"+P365CarMasterArray);
			       for (JsonNode jsonNode : P365CarMasterArray) {
			    	if(jsonNode.get("make").asText().toLowerCase().contains(lobInfo.get("make").textValue().toLowerCase()))
			    	{
			    		if(jsonNode.get("model").asText().toLowerCase().contains(lobInfo.get("model").textValue().toLowerCase()))
				    	{
			    			if(jsonNode.get("variant").textValue().toLowerCase().equals(lobInfo.get("variant").textValue().toLowerCase()))
					    	{
					    		((ObjectNode)requestNode).put("matchVariant",true);
					    		((ObjectNode)lobInfo).put("variantId", jsonNode.get("variantId").textValue());
					    		((ObjectNode)requestNode).put(requestNode.get("lob").textValue().toLowerCase()+"Info", lobInfo);
					    		 return requestNode;
					    	}
			    			else
			    			{
			    		    	((ObjectNode)requestNode).put("matchVariant",true);
			    				((ObjectNode)lobInfo).put("variantId", jsonNode.get("variantId").textValue());
					    		 return requestNode;
			    			}
				    	}
			    	}
				  }
			       if(!requestNode.has("matchVariant"))
				    {
				    	((ObjectNode)requestNode).put("matchVariant",false);
				    	((ObjectNode)lobInfo).put("variantId", "CarVarientId-411");
				    	((ObjectNode)requestNode).put("P365VariantDetails",P365CarMasterArray);
				    }
			      }
		         else
				   {
					   ((ObjectNode)requestNode).put("matchVariant",true);
					    return requestNode;
				   }
		    	}
		     try{
				    if(requestNode.get("lob").textValue().equals("Bike"))
				    {
					   if(!lobInfo.has("variantId"))
					   {
						   String make = null;
						   if(lobInfo.has("make") && lobInfo.has("model") && lobInfo.has("variant") )
						   {
							   if(ApplicableLobDocConfig.get("replaceMake").has(lobInfo.get("make").textValue()))
							    {
								   make= ApplicableLobDocConfig.get("replaceMake").get(lobInfo.get("make").textValue()).textValue();
							    }
							    else
							     {
								   make=lobInfo.get("make").asText();
							    }
							   String Query = ApplicableLobDocConfig.get("Bike").get("Query").textValue();
							   Query= Query+make+"' AND model='"+lobInfo.get("model").asText()+"' AND variant='"+lobInfo.get("variant").asText()+"'";
							   log.info("select Query :"+Query);
							   List<Map<String, Object>> allvariantResult = serverConfig.executeQuery(Query);
							   log.info("all variant Result :"+allvariantResult);
							   if(allvariantResult.size()>0)
							   {
								   ((ObjectNode)requestNode).put("matchVariant",true);
						    		((ObjectNode)lobInfo).put("variantId", allvariantResult.get(0).get("variantId").toString());
						    		((ObjectNode)requestNode).put(requestNode.get("lob").asText().toLowerCase()+"Info", lobInfo);
						    		 return requestNode;
							   }
							   else
							   {
								   String makeAndModel = ApplicableLobDocConfig.get("Bike").get("Query").textValue();
								   makeAndModel= makeAndModel+make+"' AND model='"+lobInfo.get("model").asText()+"'";
								   List<Map<String, Object>> allmakeandmodelResult = serverConfig.executeQuery(makeAndModel);
								   log.info("all make and model Result :"+allvariantResult);
								   if(allmakeandmodelResult.size()>0)
								   {
									   for (Map<String, Object> master : allmakeandmodelResult) {
										   if(lobInfo.get("variant").asText().contains(((String)master.get("variant"))))
										   {
											     log.info("variant match using contains method:"+((String)master.get("variant")));
											    ((ObjectNode)requestNode).put("matchVariant",true);
									    		((ObjectNode)lobInfo).put("variantId", ((String)master.get("variantId")));
									    		((ObjectNode)requestNode).put(requestNode.get("lob").asText().toLowerCase()+"Info", lobInfo);
									    		 return requestNode;
										   }
									}
									    ((ObjectNode)requestNode).put("matchVariant",true);
							    		((ObjectNode)lobInfo).put("variantId", allmakeandmodelResult.get(0).get("variantId").toString());
							    		((ObjectNode)requestNode).put(requestNode.get("lob").asText().toLowerCase()+"Info", lobInfo);
							    		 return requestNode;
								   }
								   else
								   {
									    ((ObjectNode)requestNode).put("matchVariant",false);
								    	((ObjectNode)lobInfo).put("variantId", "BikeVarientId-824");
								    	((ObjectNode)requestNode).put("P365VariantDetails",P365BikeMasterArray);
								   }
							   
							   }
						   }
					     }
					   else
					   {
						   ((ObjectNode)requestNode).put("matchVariant",true);
						    return requestNode;
					   }
					   }
		    }
		    catch(Exception e)
		    {
		    	log.error("Exception in get Bike Variant Details method:",e);
				throw new Exception();
		    }
		    //make model suppose not match default variant set for RAMP Integration.
		       log.info("response requestNode :"+requestNode);
	          return requestNode;
		}
		catch(NullPointerException e)
		{
			log.error("Null Exception in getVariantDetails method:",e);
			throw new Exception();
		}
		catch(Exception e)
		{
			log.error("Exception in getVariantDetails method :",e);
			throw new Exception();
		}
	}
	

}
