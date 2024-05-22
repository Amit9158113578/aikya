package com.lead.upload.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.opencsv.CSVWriter;

public class ImportLeadFormat 
{
	static ObjectMapper objectMapper = new ObjectMapper(); 
	static JsonNode configNode = null;
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static Logger log = Logger.getLogger(ImportLeadFormat.class.getName());
	static {
		if(configNode == null){
			try {
				configNode = objectMapper.readTree(serverConfig.getDocBYId("LeadUtilityConfig").content().toString());
			} catch (IOException e) {
				log.error("error at loading LeadUtilityConfig"+e);
				e.printStackTrace();
			} 
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void jsonToCSVFormat(String jsonString) throws NumberFormatException,Exception {
		Pattern pattern = Pattern.compile("[1-9][0-9]{9}");
		log.info("Req at jsonToCSVFormat :"+jsonString);
		List<String[]> csvList = new ArrayList<String[]>(); 
		JsonNode jsonNode = objectMapper.readTree(jsonString);
		log.info("Converted To JSON :"+jsonNode);
		String vendorName = null;
		// data
		if(jsonNode.has("vendorName") && jsonNode.get("vendorName") != null){
			vendorName = jsonNode.get("vendorName").asText();
			log.info("vendor Name :"+vendorName);
		}else{
			log.info("vendor name not found");
			return;
		}
		if(configNode.get("vendorsConfig").has(vendorName)){
			JsonNode vendor = configNode.get("vendorsConfig").get(vendorName);
			log.info("vendor "+vendor);
			List<String> headerList = new ArrayList<String>();
			List<String> valueList = null;
			// Headers
			Iterator Headeriterator = vendor.getFields();
			while(Headeriterator.hasNext()){
				Map.Entry e = (Entry<String, JsonNode>)Headeriterator.next();
				if(!(e.getValue() instanceof ArrayNode)){
					headerList.add(e.getValue().toString().substring(1,e.getValue().toString().length()-1));
				}else{
					headerList.add(e.getKey().toString());
				}
			}
			log.info("headerList "+headerList);
			csvList.add(headerList.toArray(new String[0]));
			log.info("csvList1 "+csvList.toString());
			for (JsonNode lead : jsonNode.get("leadList")) {
				log.info("############################################################");
				log.info("lead info :"+lead);
				if(lead.has("mobile")){
					Matcher matcher = pattern.matcher(lead.get("mobile").asText());
					if(!(matcher.find() && matcher.group().equals(lead.get("mobile").asText()))){
						log.info("Number :"+lead.get("mobile").asText()+" is not valid");
						throw new NumberFormatException();
					}
				}
				Iterator valueIterator = vendor.getFields();
				valueList = new ArrayList<String>();
				while(valueIterator.hasNext()){
					Map.Entry entry = (Entry<String, JsonNode>)valueIterator.next();
					log.info("entry :"+entry.toString());
					if(entry.getValue() instanceof ArrayNode){
						/*
						ArrayNode arrayNode= (ArrayNode) entry.getValue();
						String additionalInfo = "";
						for(JsonNode additionalInfoNode: arrayNode){
							if(additionalInfo.length()> 0){
								additionalInfo += ",";
							}
							if(lead.has(additionalInfoNode.get("key").asText())){
								additionalInfo += additionalInfoNode.get("label").asText()+lead.get(additionalInfoNode.get("key").asText()).asText();
							}
						}
						log.info("additionalInfo :"+additionalInfo);
						valueList.add(additionalInfo);
						*/
					}else{
						if(lead.has(entry.getKey().toString())){
							log.info("Got value :"+lead.get(entry.getKey().toString().replaceAll("^\"|$\"", "")).asText());
							valueList.add(lead.get(entry.getKey().toString().replaceAll("^\"|$\"", "")).asText());
						}else{
							log.info("not found :"+entry.getKey());
							valueList.add("");
						}
					}
					log.info("valueList :"+valueList.toString());
				}
				log.info("valueList len:"+valueList.size());
				csvList.add(valueList.toArray(new String[0]));
				log.info("csvList2 "+csvList.toString());
				for (String string : valueList) {
					log.info("ddsdsd :"+string);
				}
			}
		}else{
			log.info("In else config not");
		}
		writeCSV(csvList);
	}
	public void writeCSV(List<String[]> data){
		File csvFile = new File("/home/devmaster/jboss-fuse-6.2.1.redhat-084/leadImport.csv");
		//File csvFile = new File("lm.csv");
		try {
			FileWriter fileWriter = new FileWriter(csvFile);
			CSVWriter csvWriter = new CSVWriter(fileWriter);
			csvWriter.writeAll(data);
			csvWriter.close();
		} catch (Exception e) {
			log.info("Error at writing csv");
		}
	}

	public static void main( String[] args ) throws  IOException {
		ImportLeadFormat importLead = new ImportLeadFormat();
		//importLead.jsonToCSVFormat(objectMapper.readTree(new File("testJson.json")).toString());
	}
}
