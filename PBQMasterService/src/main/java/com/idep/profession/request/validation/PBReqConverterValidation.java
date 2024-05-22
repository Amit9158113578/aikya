package com.idep.profession.request.validation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.profession.quote.constants.ProfessionQuoteConstant;

/**
 * @author pravin.jakhi
 *
 */
public class PBReqConverterValidation implements Processor {

		ObjectMapper objectMapper = new ObjectMapper();
		Logger log = Logger.getLogger(PBReqConverterValidation.class);
		CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
		 Calendar calender = new GregorianCalendar();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
		//	exchange.setProperty("PBJQuoteRequest", request);
			
			JsonDocument paramCalclauteConfig = serverConfig.getDocBYId("RecommendReqParamConfig");
			if(paramCalclauteConfig!=null){
				
				JsonNode paramNode = objectMapper.readTree(paramCalclauteConfig.content().get("calcluateParam").toString());
				ArrayNode ParamList = (ArrayNode)paramNode;
				log.info("PARAMList for configuration:  "+ParamList);
				for(JsonNode param : ParamList){
						if(param.has("requestParam")){
						String requestParam = param.get("requestParam").asText();
							if(param.has("requestNode")){
								if(!param.get("requestNode").asText().equalsIgnoreCase("NA") || !param.get("requestNode").asText().equalsIgnoreCase("")){
								if(param.has("action")){
									if(param.get("action").asText().equalsIgnoreCase("count")){
										if(request.get(param.get("requestNode").asText()).get(requestParam).isArray()){
											((ObjectNode)request.get(param.get("requestNode").asText())).put(param.get("responseParam").asText(),request.get(param.get("requestNode").asText()).get(requestParam).size());
											log.info("values added for this config : reqNode :  "+param.get("requestNode").asText()+" resParam : "+param.get("responseParam").asText());
										}else{
											((ObjectNode)request.get(param.get("requestNode").asText())).put(param.get("responseParam").asText(),request.get(param.get("requestNode").asText()).get(requestParam).size());	
										}
									}
								}
							}else{
								if(param.has("action")){
									if(param.get("action").asText().equalsIgnoreCase("count")){
										if(request.get(requestParam).isArray()){
											((ObjectNode)request).put(param.get("responseParam").asText(),request.get(requestParam).size());
											log.info("values added for this config : reqNode :  "+param.get("requestNode").asText()+" resParam : "+param.get("responseParam").asText());
										}else{
											((ObjectNode)request).put(param.get("responseParam").asText(),request.get(requestParam).size());	
										}
									}
								}
							}
						}
					}
				}
				
				
			}else{
				log.error("unable to fetch RecommendReqParamConfig from DB");
			}
			int vehicleCount=0;
			if(request.has("carInfo")){
				if(request.get("carInfo").has("variantId")){
					vehicleCount=vehicleCount+1;
				}
				if(request.get("carInfo").has("registrationYear")){
					log.info("CarInfo _> registrationYear found :  "+request.get("carInfo").get("registrationYear").asInt());
					((ObjectNode)request.get("carInfo")).put("dateOfRegistration",getDateOfRegistrationYear(request.get("carInfo").get("registrationYear").asInt()));
					// exchange.setProperty(ProfessionQuoteConstant.PROFQUOTEREQ,request);
				}
				if(request.get("carInfo").has("registrationPlace") || request.get("carInfo").has("registrationNumber")){
					 ((ObjectNode)request.get("carInfo")).put("RTOCode",createRTOCodeForMotor(request.get("carInfo")));
					  exchange.setProperty(ProfessionQuoteConstant.PROFQUOTEREQ,request);
				}
			}
			if(request.has("bikeInfo")){
				if(request.get("bikeInfo").has("variantId")){
					vehicleCount=vehicleCount+1;
				}
				if(request.get("bikeInfo").has("registrationYear")){
					((ObjectNode)request.get("bikeInfo")).put("dateOfRegistration",getDateOfRegistrationYear(request.get("bikeInfo").get("registrationYear").asInt()));
					 //exchange.setProperty(ProfessionQuoteConstant.PROFQUOTEREQ,request);
				}
				if(request.get("bikeInfo").has("registrationPlace") || request.get("bikeInfo").has("registrationNumber")){
					 ((ObjectNode)request.get("bikeInfo")).put("RTOCode",createRTOCodeForMotor(request.get("bikeInfo")));
					  exchange.setProperty(ProfessionQuoteConstant.PROFQUOTEREQ,request);
				}
			}
			((ObjectNode)request.get("commonInfo")).put("vehicleInfo",vehicleCount);
			if(request.has("professionCode")){
				JsonDocument defaultDoc = serverConfig.getDocBYId("defaultProfessionRecommendRequest-"+request.get("professionCode").asText());
				if(defaultDoc!=null){
				((ObjectNode)request).put("defaultRequest",objectMapper.readTree(defaultDoc.content().toString()));
				}
			}else{
				log.error("PBJ requuest in Profession CODE NOT found");
			}
			
			
			
			((ObjectNode)request).put("requestType","ProfessionRecommendRequest");
			
			
			
			
			request = calculateVehicleAgeAndExshwroomDetails(request);
				
			log.info("updated reqest PBJ recommend : "+request);
		
			exchange.getIn().setBody(request);
			
		}catch(Exception e){
			log.error("unable to valdate PB recommender request : ",e);
		}
	}	
	
	public JsonNode calculateVehicleAgeAndExshwroomDetails(JsonNode requestNode)
	{
		
		try{
			
			if(requestNode.has("carInfo"))
			{
				try{
						if(requestNode.get("carInfo").has("registrationYear") && requestNode.get("carInfo").has("variantId"))
						{
							String variantId = requestNode.get("carInfo").get("variantId").textValue();
							JsonDocument docBYId = serverConfig.getDocBYId(variantId);
							if(docBYId!=null)
							{
								JsonNode variantIdDocNode = objectMapper.readTree(docBYId.content().toString());
								if(variantIdDocNode.has("exShowroomPrice"))
							     {
									int registrationYear = requestNode.get("carInfo").get("registrationYear").asInt();
									double vehicleAge = calculateVehicleAge(registrationYear);
									long idv = getValidateIDVPrice(vehicleAge, variantIdDocNode);
									int ncbPercentage = calculateNCBPercentage(vehicleAge);
									((ObjectNode)requestNode.get("carInfo")).put("carIDV",idv);
									((ObjectNode)requestNode.get("carInfo")).put("carVehicleAgeRange", vehicleAge);
									((ObjectNode)requestNode.get("carInfo")).put("carNcbPercentage", ncbPercentage);
									((ObjectNode)requestNode.get("carInfo")).put("carCCRange", variantIdDocNode.get("cubicCapacity"));
								 }
								else
								{
									log.info("car exShowRoomPrice not found for selected model.Please check document :"+variantId);
								}
							}
							else
							{
								log.info("car variantId document not found in serverConfig bucket :"+variantId);
							}
						}
						else
						{
							log.info("car registrationYear and variantId not found in carInfo :"+requestNode.get("carInfo"));
						}
				}catch(Exception e)
				{
					log.error("exception in calculate car information :",e);
					e.printStackTrace();
				}
					
			}
			else
			{
				log.info("car info not found in requestNode :"+requestNode);
			}
			if(requestNode.has("bikeInfo"))
			{
				try{
					  if(requestNode.get("bikeInfo").has("registrationYear") && requestNode.get("bikeInfo").has("variantId"))
						{
							String variantId = requestNode.get("bikeInfo").get("variantId").textValue();
							JsonDocument docBYId = serverConfig.getDocBYId(variantId);
							if(docBYId!=null)
							{
								JsonNode variantIdDocNode = objectMapper.readTree(docBYId.content().toString());
								if(variantIdDocNode.has("exShowroomPrice"))
							     {
									int registrationYear = requestNode.get("bikeInfo").get("registrationYear").asInt();
									double vehicleAge = calculateVehicleAge(registrationYear);
									long idv = getValidateIDVPrice(vehicleAge, variantIdDocNode);
									int ncbPercentage = calculateNCBPercentage(vehicleAge);
									((ObjectNode)requestNode.get("bikeInfo")).put("bikeIDV", idv);
									((ObjectNode)requestNode.get("bikeInfo")).put("bikeVehicleAgeRange", vehicleAge);
									((ObjectNode)requestNode.get("bikeInfo")).put("bikeNcbPercentage", ncbPercentage);
									((ObjectNode)requestNode.get("bikeInfo")).put("bikeCCRange", variantIdDocNode.get("cubicCapacity"));

								 }
								else
								{
									log.info("bike exShowRoomPrice not found for selected model.Please check document :"+variantId);
								}
							}
							else
							{
								log.info("bike variantId document not found in serverConfig bucket :"+variantId);
							}
						}
						else
						{
							log.info("bike registrationYear and variantId not found in carInfo :"+requestNode.get("carInfo"));
						}
				}catch(Exception e)
				{
					log.error("exception in calculate bike information");
					e.printStackTrace();
				}
			}
			else
			{
				log.info("bike info not found in requestNode :"+requestNode);
			}
		}
		catch(Exception e)
		{
			log.info("Exception Found  CalculateVehicleAgeDepRateAndNCBPer Method In PBReqConverterValidation Processor :",e);
		    e.printStackTrace();
		}
   return requestNode;
		
	}
	 public double calculateVehicleAge(int registrationYear)  
	 {
		 double age=0.0;
		 String dateOfRegistration=null;
		  try{
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				    Calendar cal = new GregorianCalendar();
				    int month = cal.get(Calendar.MONTH)+1;
				    if(month>6)
				    {
				    	 dateOfRegistration="01/07/"+registrationYear;
				    }
				    else
				    {
				    	 dateOfRegistration="01/01/"+registrationYear;
				    }
					String startDateAddingDays = sdf.format(cal.getTime());
					age=(double)Math.abs((sdf.parse(startDateAddingDays).getTime()-sdf.parse(dateOfRegistration).getTime())/(1000*60*60*24))/365;
					String stringage  = String.format("%.2f", age);
					age= Double.parseDouble(stringage);
					return age;
		     }
		 catch(Exception e)
		 {
			 log.error("unable to calculate kotak car vehicle age  :"+dateOfRegistration);
		 }
		return age;
	 }
	 
	
	 static JsonNode idvConfigNode=null;
	 static
     {
		
		 ObjectMapper objectMapper2 = new ObjectMapper();
	     CBService staticServiceConfig = CBInstanceProvider.getServerConfigInstance();
	     
		 try {
			idvConfigNode  = objectMapper2.readTree(staticServiceConfig.getDocBYId("P365IDVCalcConfig").content().toString());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
	 public long getValidateIDVPrice(double vehicleAge,JsonNode exshowroompricenode)
	 {
		 
		    
			String idvCalVehicleAge = "";
			for(JsonNode node : idvConfigNode.get("P365").get("idvCalculationDetails"))
			{
					if(vehicleAge>=node.get("startValue").doubleValue()&&
							vehicleAge<=node.get("endValue").doubleValue())
					{
						
						idvCalVehicleAge = node.get("absoluteValue").textValue();
						break;
					}
			}
			JsonNode carrierEXSHOWROOMInfo = null;
			try {
				carrierEXSHOWROOMInfo = this.objectMapper.readTree(exshowroompricenode.toString());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long exshowroomPriceConfigValue = carrierEXSHOWROOMInfo.get(idvCalVehicleAge).longValue();
			
			if(exshowroomPriceConfigValue>0 || exshowroomPriceConfigValue>0.0)
			{
				return exshowroomPriceConfigValue;
			}
		return 0;
		 
	 }
	 public int calculateNCBPercentage(double vehicleAge)
	 {
		 int ncbPercentage = 0;
		 for(JsonNode node : idvConfigNode.get("P365").get("NCBPercentageDetails"))
			{
					if(vehicleAge>=node.get("startValue").doubleValue()&&
							vehicleAge<=node.get("endValue").doubleValue())
					{
						
						ncbPercentage = node.get("absoluteValue").asInt();
						break;
						
					}
			}
		 return ncbPercentage;
	 }
	 
	 
	 public String getDateOfRegistrationYear(int registrationYear)  
	 {
		 String dateOfRegistration=null;
		  try{				   
				    int month = calender.get(Calendar.MONTH)+1;
				    int year = calender.get(calender.YEAR);
				    
				    if(year==registrationYear){
					    if(month>6)
					    {
					    	 dateOfRegistration="01/07/"+registrationYear;
					    }
					    else
					    {
					    	 dateOfRegistration="01/01/"+registrationYear;
					    }
				    }else{
				    	 dateOfRegistration="01/07/"+registrationYear;
				    }
					return dateOfRegistration;
		  }catch(Exception e)
		 {
			 log.error("unable to send date of registration  :"+dateOfRegistration);
		 }
		return null;
	 }
	 
 public String createRTOCodeForMotor(JsonNode infoNode )
 {
	 String RTOCode="";
	 if(infoNode.has("registrationNumber"))
	 {
		 String registrationNumber =  new PBReqConverterValidation().validateRegistrationNumber(infoNode.get("registrationNumber").textValue());
		 RTOCode = registrationNumber.substring(0, 4);
	     return RTOCode;
	 }
	 else
	 {
		 String registrationPlace = infoNode.get("registrationPlace").textValue();
		 registrationPlace = registrationPlace.replaceAll("-", "");
		 RTOCode = registrationPlace.substring(0, 4);
	     return RTOCode;
	 }
 }
 
 
 public String validateRegistrationNumber(String registrationNumber)
	{
		    
		    Pattern compile = Pattern.compile(ProfessionQuoteConstant.PATTERN);
		    if(compile.matcher(registrationNumber).matches())
		    {
		    	registrationNumber = new StringBuffer(registrationNumber).insert(registrationNumber.length()-8, "0").toString();
		    	return registrationNumber;
		    }
		 return registrationNumber;
	}
}