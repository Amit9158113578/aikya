package com.idep.service.vehicleinfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.activemq.filter.function.replaceFunction;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;
import com.sun.jersey.core.util.Base64;





public class ExtractResponseProcessor {
	//static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	public static void main(String[] args) {
		try{
			ObjectMapper mapper = new ObjectMapper();
			
			/*String csvFile = "E:/Profiles/pravin.jakhi/Documents/Dump-India.csv";
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = ",";
	        Date currentDate = new Date();
	        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			ObjectMapper objectmapper = new ObjectMapper();
			SoapConnector soapconnector = new SoapConnector();
			List<JsonDocument> documents = new ArrayList<>();
	        //System.out.println("csv file readin");
	        int index=0;
	        int counter = 0;
	        br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
            	if(line.trim().replaceAll("\\s+", "").replaceAll("\"", "").length()>9){
                // use comma as separator
                String[] carResposne = line.split(cvsSplitBy);
                String registrationNo= carResposne[0].toUpperCase().trim().replaceAll("\\s+", "");
            	//System.out.println("vehicle No :  : "+registrationNo);
            	if(carResposne[1]!=null && !carResposne[1].equalsIgnoreCase("") && carResposne[1].length()>10  ){
            		String Resposne=null;
            		if(ExtractResponseProcessor.decodeString(carResposne[1]).length()>165){
            			//Resposne=soapconnector.getSoapResult(ExtractResponseProcessor.decodeString(carResposne[1]), "vehicleJson");
            		System.out.println("CONVERT : "+ExtractResponseProcessor.decodeString("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTE2Ij8+PFZlaGljbGUgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeG1sbnM6eHNkPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+PHZlaGljbGVKc29uPnsiRGVzY3JpcHRpb24iOiJNQUhJTkRSQSBCT0xFUk8gU0xYIiwiUmVnaXN0cmF0aW9uWWVhciI6IjIwMDgiLCJDYXJNYWtlIjp7IkN1cnJlbnRUZXh0VmFsdWUiOiJNQUhJTkRSQSJ9LCJDYXJNb2RlbCI6eyJDdXJyZW50VGV4dFZhbHVlIjoiQk9MRVJPIn0sIkVuZ2luZVNpemUiOnsiQ3VycmVudFRleHRWYWx1ZSI6IjI1MjMifSwiTWFrZURlc2NyaXB0aW9uIjp7IkN1cnJlbnRUZXh0VmFsdWUiOiJNQUhJTkRSQSJ9LCJNb2RlbERlc2NyaXB0aW9uIjp7IkN1cnJlbnRUZXh0VmFsdWUiOiJCT0xFUk8ifSwiVmVjaGlsZUlkZW50aWZpY2F0aW9uTnVtYmVyIjoiTUExUFMyR0FLODJHMTM1MzciLCJOdW1iZXJPZlNlYXRzIjp7IkN1cnJlbnRUZXh0VmFsdWUiOiI3In0sIkNvbG91ciI6IkJMQUNLIiwiRW5naW5lTnVtYmVyIjoiR0E4NEczMzIyMCIsIkZ1ZWxUeXBlIjp7IkN1cnJlbnRUZXh0VmFsdWUiOiJESUVTRUwifSwiUmVnaXN0cmF0aW9uRGF0ZSI6IjI0LzIvMjAwOSIsIkxvY2F0aW9uIjoiUlRPLCBIT1NQRVQgIiwiSW1hZ2VVcmwiOiJodHRwOi8vaW4uY2FycmVnaXN0cmF0aW9uYXBpLmNvbS9pbWFnZS5hc3B4L0BUVUZJU1U1RVVrRWdRazlNUlZKUElGTk1XQT09In08L3ZlaGljbGVKc29uPjx2ZWhpY2xlRGF0YT48RGVzY3JpcHRpb24+TUFISU5EUkEgQk9MRVJPPC9EZXNjcmlwdGlvbj48UmVnaXN0cmF0aW9uWWVhcj4yMDA4PC9SZWdpc3RyYXRpb25ZZWFyPjxDYXJNYWtlPjxDdXJyZW50VGV4dFZhbHVlPk1BSElORFJBPC9DdXJyZW50VGV4dFZhbHVlPjwvQ2FyTWFrZT48Q2FyTW9kZWw+Qk9MRVJPPC9DYXJNb2RlbD48RW5naW5lU2l6ZT48Q3VycmVudFRleHRWYWx1ZT4yNTIzPC9DdXJyZW50VGV4dFZhbHVlPjwvRW5naW5lU2l6ZT48L3ZlaGljbGVEYXRhPjwvVmVoaWNsZT4="));            			Resposne=soapconnector.getSoapResult("", "vehicleJson");
            	JsonNode xmlResponse = null;//ExtractResponseProcessor.ExtractResposne(Resposne);
            	JsonNode finalRes = objectmapper.createObjectNode();
            	((ObjectNode)finalRes).put("carDetails", xmlResponse);
            	((ObjectNode)finalRes).put("documentType", "carRtoDetails");
            	((ObjectNode)finalRes).put("updatedDate", dateFormat.format(currentDate));
            	((ObjectNode)finalRes).put("registrationNo", registrationNo);
            	//System.out.println("Response  extract : "+index+" \t Res : "+finalRes);
            	index++;
            	counter++;
            	JsonObject docObj = JsonObject.fromJson(objectmapper.readTree(finalRes.toString()).toString());
            	
            	
            	documents.add(JsonDocument.create(registrationNo, docObj));
            	
            	//serverConfig.createDocument(registrationNo,docObj );
            	System.out.println("Document Added in List : "+registrationNo);
            		}
            	}else{
            		System.out.println("Response not extract : "+line);
            	}
            	
            	}
            }
            System.out.println("Document DB Upload Started :  ");
            serverConfig.bulkAsyncDocument(documents);
            System.out.println("Document DB Upload END ");*/
		/*String base64="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTE2Ij8+PFZlaGljbGUgeG1sbnM6eHNkPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSI+PHZlaGljbGVKc29uPnsiRGVzY3JpcHRpb24iOiJIeXVuZGFpIEdldHogLyBDbGljayAvIGkyMCIsIlJlZ2lzdHJhdGlvblllYXIiOiIyMDE3IiwiQ2FyTWFrZSI6eyJDdXJyZW50VGV4dFZhbHVlIjoiSHl1bmRhaSJ9LCJDYXJNb2RlbCI6eyJDdXJyZW50VGV4dFZhbHVlIjoiR2V0eiAvIENsaWNrIC8gaTIwIn0sIkVuZ2luZVNpemUiOnsiQ3VycmVudFRleHRWYWx1ZSI6Ik4vQSJ9LCJNYWtlRGVzY3JpcHRpb24iOnsiQ3VycmVudFRleHRWYWx1ZSI6Ikh5dW5kYWkifSwiTW9kZWxEZXNjcmlwdGlvbiI6eyJDdXJyZW50VGV4dFZhbHVlIjoiR2V0eiAvIENsaWNrIC8gaTIwIn0sIlZlY2hpbGVJZGVudGlmaWNhdGlvbk51bWJlciI6Ik1BTEJNNTFCTEhNNDAwMDAxIiwiTG9jYXRpb24iOiJQSU1QUkktQ0hJTkNIV0FEICwgTUFIQVJBU0hUUkEiLCJJbWFnZVVybCI6Imh0dHA6Ly9pbi5jYXJyZWdpc3RyYXRpb25hcGkuY29tL2ltYWdlLmFzcHgvQFNIbDFibVJoYVNCSFpYUjZJQzhnUTJ4cFkyc2dMeUJwTWpBPSJ9PC92ZWhpY2xlSnNvbj48dmVoaWNsZURhdGE+PERlc2NyaXB0aW9uPkh5dW5kYWkgR2V0eiAvIENsaWNrIC8gaTIwPC9EZXNjcmlwdGlvbj48UmVnaXN0cmF0aW9uWWVhcj4yMDE3PC9SZWdpc3RyYXRpb25ZZWFyPjxDYXJNYWtlPjxDdXJyZW50VGV4dFZhbHVlPkh5dW5kYWk8L0N1cnJlbnRUZXh0VmFsdWU+PC9DYXJNYWtlPjxDYXJNb2RlbD5HZXR6IC8gQ2xpY2sgLyBpMjA8L0Nhck1vZGVsPjxFbmdpbmVTaXplPjxDdXJyZW50VGV4dFZhbHVlPk4vQTwvQ3VycmVudFRleHRWYWx1ZT48L0VuZ2luZVNpemU+PC92ZWhpY2xlRGF0YT48L1ZlaGljbGU+";
		String xmlRes = ExtractResponseProcessor.decodeString(base64);
		String Resposne = soapconnector.getSoapResult(xmlRes, "vehicleJson");
		System.out.println("Extrat Resposne : "+Resposne);
		JsonNode response = objectmapper.readTree(Resposne);
		*/
            
            String vale ="316";
		JsonNode value = mapper.readTree(vale);
		
		System.out.println("VLUES DOUBLE : "+value.doubleValue());
            
		}catch(Exception e){
			System.out.println("Exception : ");
			e.printStackTrace();
		}
	}
	static String decodeString(String encoded) {
	    byte[] dataDec = Base64.decode(encoded.getBytes());
	    String decodedString = "";
	    try {
	        decodedString = new String(dataDec, "UTF-8");
	    } catch (Exception e) {
	        e.printStackTrace();

	    } finally {
	       // System.out.println("Converted : "+decodedString);
	        return(new String(decodedString));
	    }
	}
	
	
	public  JsonNode ExtractResposne(String xmlResposne){
		ObjectMapper objectmapper = new ObjectMapper();
		JsonNode finalResposne = objectmapper.createObjectNode();
		try {
			JsonNode inputReq= objectmapper.readTree(xmlResposne);
			if(inputReq.has(VehicleInfoConstant.DESCRIPTION)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.DESCRIPTION,inputReq.get(VehicleInfoConstant.DESCRIPTION).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.DESCRIPTION,"");
			}
			if(inputReq.has(VehicleInfoConstant.REGISTRATIONYEAR)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.REGISTRATIONYEAR,inputReq.get(VehicleInfoConstant.REGISTRATIONYEAR).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.REGISTRATIONYEAR,"");
			}
			if(inputReq.has(VehicleInfoConstant.CARMAKE)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.CARMAKE,inputReq.get(VehicleInfoConstant.CARMAKE).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.CARMAKE,"");
			}
			if(inputReq.has(VehicleInfoConstant.CARMODEL)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.CARMODEL,inputReq.get(VehicleInfoConstant.CARMODEL).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.CARMODEL,"");
			}
			if(inputReq.has(VehicleInfoConstant.ENGINESIZE)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.ENGINESIZE,inputReq.get(VehicleInfoConstant.ENGINESIZE).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.ENGINESIZE,"");
			}
			
			if(inputReq.has(VehicleInfoConstant.MAKEDESCRIPTION)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.MAKEDESCRIPTION,inputReq.get(VehicleInfoConstant.MAKEDESCRIPTION).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.MAKEDESCRIPTION,"");
			}
			
			if(inputReq.has(VehicleInfoConstant.MODELDESCRIPTION)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.MODELDESCRIPTION,inputReq.get(VehicleInfoConstant.MODELDESCRIPTION).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.MODELDESCRIPTION,"");
			}
			if(inputReq.has(VehicleInfoConstant.VEHICLEIDENTNUMBER)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.VEHICLEIDENTNUMBER,inputReq.get(VehicleInfoConstant.VEHICLEIDENTNUMBER).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.VEHICLEIDENTNUMBER,"");
			}
			if(inputReq.has(VehicleInfoConstant.NOOFSEATS)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.NOOFSEATS,inputReq.get(VehicleInfoConstant.NOOFSEATS).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.NOOFSEATS,"");
			}
			if(inputReq.has(VehicleInfoConstant.COLOUR)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.COLOUR,inputReq.get(VehicleInfoConstant.COLOUR).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.COLOUR,"");
			}
			if(inputReq.has(VehicleInfoConstant.ENGINENUMBER)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.ENGINENUMBER,inputReq.get(VehicleInfoConstant.ENGINENUMBER).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.ENGINENUMBER,"");
			}
			if(inputReq.has(VehicleInfoConstant.FUELTYPE)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.FUELTYPE,inputReq.get(VehicleInfoConstant.FUELTYPE).get("CurrentTextValue").asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.FUELTYPE,"");
			}
			if(inputReq.has(VehicleInfoConstant.REGDATE)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.REGDATE,inputReq.get(VehicleInfoConstant.REGDATE).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.REGDATE,"");
			}
			if(inputReq.has(VehicleInfoConstant.LOCATION)){
				((ObjectNode)finalResposne).put(VehicleInfoConstant.LOCATION,inputReq.get(VehicleInfoConstant.LOCATION).asText());
			}else{
				((ObjectNode)finalResposne).put(VehicleInfoConstant.LOCATION,"");
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		return finalResposne;
	}
	
}
