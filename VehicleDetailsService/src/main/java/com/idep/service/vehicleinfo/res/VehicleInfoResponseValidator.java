package com.idep.service.vehicleinfo.res;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.ExtractResponseProcessor;
import com.idep.service.vehicleinfo.processor.VehicleInfoReqProcessor;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

/**
 * @author pravin.jakhi
 *  this class validate the response success or fail.
 *  if success then extracting and store in DB and also sending to UI 
 */
public class VehicleInfoResponseValidator implements Processor {
	
	Logger log = Logger.getLogger(VehicleInfoResponseValidator.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService policy = CBInstanceProvider.getPolicyTransInstance();
	SoapConnector soapconnector = new SoapConnector();
	ExtractResponseProcessor extractRes = new ExtractResponseProcessor();
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			
		//String serviceRes = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><CheckIndiaResponse xmlns=\"http://regcheck.org.uk\"><CheckIndiaResult><vehicleJson>{\"Description\":\"MARUTI ALTO 800 LXI\",\"RegistrationYear\":\"2015\",\"CarMake\":{\"CurrentTextValue\":\"MARUTI\"},\"CarModel\":{\"CurrentTextValue\":\"ALTO 800\"},\"EngineSize\":{\"CurrentTextValue\":\"796\"},\"MakeDescription\":{\"CurrentTextValue\":\"MARUTI\"},\"ModelDescription\":{\"CurrentTextValue\":\"ALTO 800\"},\"VechileIdentificationNumber\":\"MA3EUA61S00663588EF\",\"NumberOfSeats\":{\"CurrentTextValue\":\"5\"},\"Colour\":\"\",\"EngineNumber\":\"F8DN5444586\",\"FuelType\":{\"CurrentTextValue\":\"Petrol\"},\"RegistrationDate\":\"22/07/2015\",\"Location\":\"\",\"ImageUrl\":\"http://in.carregistrationapi.com/image.aspx/@TUFSVVRJIEFMVE8gODAwIExYSQ==\\\"}</vehicleJson><vehicleData><Description>MARUTI ALTO 800 LXI</Description><RegistrationYear>2015</RegistrationYear><CarMake><CurrentTextValue>MARUTI</CurrentTextValue></CarMake><CarModel>ALTO 800</CarModel><EngineSize><CurrentTextValue>796</CurrentTextValue></EngineSize></vehicleData></CheckIndiaResult></CheckIndiaResponse></soap:Body></soap:Envelope>";
			String serviceRes =exchange.getIn().getBody(String.class);
		String Response = soapconnector.getSoapResult(serviceRes, "vehicleJson");
		log.info("xml Vehicle Info response : "+Response);
		JsonNode response = extractRes.ExtractResposne(Response);
		JsonNode finalRes = objectMapper.createObjectNode();
		log.info("Exctracted Vehicle  response Added in ObejctNode : "+response);
		
		/**
		 * 
		 * set mapper response document to extract data from response
		 * */
		exchange.getIn().setHeader("carrierReqMapConf", VehicleInfoConstant.VEHICLERESCONFIGDOCUMENT);
		exchange.setProperty("carrierReqMapConf", VehicleInfoConstant.VEHICLERESCONFIGDOCUMENT);
		((ObjectNode)finalRes).put(VehicleInfoConstant.CARDETAILS, response);
		((ObjectNode)finalRes).put(VehicleInfoConstant.VEHICALENO, exchange.getProperty(VehicleInfoConstant.VEHICALENUMBER).toString());
		//((ObjectNode)finalRes).put("documentType",VehicleInfoConstant.DOCUMENT_TYPE);
		((ObjectNode)finalRes).put(VehicleInfoConstant.VEHICALENO, exchange.getProperty(VehicleInfoConstant.VEHICALENUMBER).toString());
		exchange.getIn().setBody(finalRes);
		}catch(Exception e){
			ObjectNode responseNode = objectMapper.createObjectNode();
			exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1001);
			  responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, VehicleInfoConstant.VEHICLE_RES_FAILED_CODE);
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "failure");
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, exchange.getIn().getBody(String.class));
				exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
			log.error("unable to validate vehicle info response : ",e);	
		}
	}
}
