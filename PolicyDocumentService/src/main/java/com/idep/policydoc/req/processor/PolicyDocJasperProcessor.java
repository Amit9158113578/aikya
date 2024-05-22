package com.idep.policydoc.req.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.util.JRProperties;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policydoc.util.PolicyDocViewConstants;

/**
 * 
 * @author sandeep.jadhav
 * This class creates PDF file from jasper
 */
@SuppressWarnings("deprecation")
public class PolicyDocJasperProcessor implements Processor {

	Logger log = Logger.getLogger(PolicyDocJasperProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {

		
		
		 try 
	     {
			 /**
			 * spool strategy changed to allow large size PDF stream in response
			 * -1 used to disable overflow to disk, use RAM instead
			 */
			 String reportFileName=null;
			 exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1);
			 
			 String reqJSON = exchange.getIn().getBody().toString();
			 JsonNode reqNode = objectMapper.readTree(reqJSON);
			 JsonNode reqDataNode = reqNode.get("data");
			 JsonNode pdfConfigNode = this.objectMapper.readTree(serverConfig.getDocBYId("PolicyPDFConfig-"+reqDataNode.findValue("carrierId").asInt()+"-"+reqDataNode.findValue("productId").asInt()+"-"+reqDataNode.findValue("businessLineId").asInt()).content().toString());
			 if(reqDataNode.findValue("insuranceType").textValue().equals("new"))
			 {
				   reportFileName =pdfConfigNode.get("jasperFileLocationnew").asText();
				   String tpPeriodOfInsuranceTo = calculateTpDate(reqDataNode.get("policyDetails").get("periodOfInsuranceFrom").textValue());
				   ((ObjectNode)reqDataNode.get("policyDetails")).put("tpPeriodOfInsuranceTo", tpPeriodOfInsuranceTo);
				   reqDataNode = calculateIDVForNewVehicle(reqDataNode);
				   log.info("reqDataNode : "+reqDataNode);

				   
			 }
			 else
			 {
				  reportFileName =pdfConfigNode.get("jasperFileLocationrenew").asText();
				 log.info("jasper report FileName : "+reportFileName);
			 }
			 if(reqDataNode.get("policyDetails").get("premiumLiabilityTableData").get(2).get("value").asDouble()==0.00)
			 {
				 ((ObjectNode)reqDataNode.get("policyDetails").get("premiumLiabilityTableData").get(2)).put("name", "");
				 ((ObjectNode)reqDataNode.get("policyDetails").get("premiumLiabilityTableData").get(2)).put("value", "");
				 ((ObjectNode)reqDataNode.get("policyDetails")).put("limitsOfLibilityOwnerDriver", 0);
			 }
			 	if(!reqDataNode.get("policyDetails").has("limitsOfLibilityOwnerDriver"))
			 	{
			 		((ObjectNode)reqDataNode.get("policyDetails")).put("limitsOfLibilityOwnerDriver", "15,00,000"); 
			 	}
			 ByteArrayInputStream iStream = new ByteArrayInputStream(reqDataNode.toString().getBytes());
			 String outFileName    =pdfConfigNode.get("outputFilePath").asText()+reqDataNode.findValue("policyNumber").asText().replace("/", "");
			 log.info("jasper outFileName : "+outFileName);
			 JsonDataSource jsonDataSource = new JsonDataSource(iStream,"policyDetails");
	     
	         
		     /**
		      * Set all parameters required to run jasper report
		      * eg $P{KOTAKLOGO}
		      */
			 HashMap<String,Object> hm = new HashMap<String,Object>();
			 for(JsonNode param : pdfConfigNode.get("reportParamConfig"))
			 {
				 hm.put(param.get("paramKey").asText(), param.get("paramValue").asText());
			 }
		 
			 log.info("jasper report Config Param "+hm.toString());
			 /**
			  * set below properties for bold font to be appear while extracting PDF
			  */
			 JRProperties.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
	    	 JRProperties.setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
	    	 JRProperties.setProperty("net.sf.jasperreports.default.pdf.embedded", "false");
	    	 
			 JasperReport jasperReport = JasperCompileManager.compileReport(reportFileName);
			 log.info("jasper report compilation complated");
	    	 ImageIO.scanForPlugins();
	    	 JasperPrint  jasperPrint = JasperFillManager.fillReport(jasperReport, hm, jsonDataSource);
	    	 log.info("jasper report data printing completed");
	    	 
	    	     	 
	    	 File file = File.createTempFile("output.", ".pdf");
	         JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(file));
	       
	         Path path = Paths.get(file.getPath());
	         byte[] data = Files.readAllBytes(path);
	         String base64string =  Base64.encodeBase64String(data);
	         
	         // decode base64 string and create pdf to verify
	         FileOutputStream fos = new FileOutputStream(outFileName+"."+pdfConfigNode.get("outputFileExtension").asText());
	         fos.write(Base64.decodeBase64(base64string));
	         fos.close();
	         /**
	          * remove existing path and URI
	          */
	    	 exchange.getIn().removeHeader(PolicyDocViewConstants.CAMEL_HTTP_PATH);
	 		 exchange.getIn().removeHeader(PolicyDocViewConstants.CAMEL_HTTP_URI);
	 		 
	         JsonNode userPolicyKeyNode = exchange.getProperty("userPolicyKeys", JsonNode.class);
	         exchange.setProperty("pdfFileLocation", outFileName);
	    	 ObjectNode pdfSignReq = objectMapper.createObjectNode();
	    	 pdfSignReq.put("uKey", userPolicyKeyNode.get("uKey").textValue());
	    	 pdfSignReq.put("pKey", userPolicyKeyNode.get("pKey").textValue());
	    	 pdfSignReq.put("policyDocStream", base64string);
	    	 
	    	 // wait until user profile gets updated in the background
	    	 Thread.sleep(4000);
	    	 exchange.getIn().setHeader("pdfSignURL", pdfConfigNode.get("URL").asText());
	    	 log.info(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.SERVICEINVOKE+"|SUCCESS|"+"carrier policy request service invoked : "+pdfSignReq);
	    	 exchange.getIn().setBody(objectMapper.writeValueAsString(pdfSignReq));
	    	
	       
	    } catch (JRException ex) 
	     {
	       log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCJASPPRO+"|ERROR|"+"Error on Policy Doc Jasper Processor:",ex);
	       
	     } catch(Exception ex)
	     {
	    	 log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCJASPPRO+"|ERROR|"+"Error on Policy Doc Jasper Processor:",ex);
	     }
		
	}
	
	public String calculateTpDate(String tpStartDate) throws ParseException
	{
	       SimpleDateFormat dateFormat = new SimpleDateFormat(PolicyDocViewConstants.SERVICE_DATE_FORMAT);
		   Date tpstartdate = dateFormat.parse(tpStartDate);
	       Calendar.getInstance().setTime(tpstartdate);
		   Calendar cal = new GregorianCalendar();
		   cal.setTime(tpstartdate);
		   cal.add(Calendar.YEAR, 3); // to get previous year add -1
		   Date nextYear = cal.getTime();
		   Calendar.getInstance().setTime(nextYear);
		   cal.add(Calendar.DAY_OF_WEEK, -1);		  
		   nextYear = cal.getTime();
		   String tpPolicyEndDate = dateFormat.format(nextYear);
		   return tpPolicyEndDate;
	}
	
	//calculate IDV and total IDV values for kotak car new business policy pdf generation
	public JsonNode calculateIDVForNewVehicle(JsonNode reqDataNode)
	{
		long secondtotalValueVehicle = 0;
		long thiredtotalValueVehicle = 0;
		
		JsonNode policyDetails = reqDataNode.get("policyDetails");
		int idv = policyDetails.get("idv").asInt();
		int secondYearIdv=(int)(idv*84.21/100);
		int thiredYearIdv=(int)(idv*73.684/100);
		((ObjectNode)policyDetails).put("secondYearIdv", secondYearIdv);
		((ObjectNode)policyDetails).put("thiredYearIdv", thiredYearIdv);
		secondtotalValueVehicle = secondtotalValueVehicle + secondYearIdv;
		thiredtotalValueVehicle = thiredtotalValueVehicle + thiredYearIdv;

		if(policyDetails.get("nonEleAccessoriesSI").asInt()>0)
		  {
				int nonEleAccessoriesSI = policyDetails.get("nonEleAccessoriesSI").asInt();
				int secondnonEleAccessoriesSI=(int)(nonEleAccessoriesSI*84.21/100);
				int thirednonEleAccessoriesSI=(int)(nonEleAccessoriesSI*73.684/100);
				((ObjectNode)policyDetails).put("secondnonEleAccessoriesSI", secondnonEleAccessoriesSI);
				((ObjectNode)policyDetails).put("thirednonEleAccessoriesSI", thirednonEleAccessoriesSI);
				 secondtotalValueVehicle = secondtotalValueVehicle+secondnonEleAccessoriesSI;
				 thiredtotalValueVehicle = thiredtotalValueVehicle+thirednonEleAccessoriesSI;
		  }
		 else
		  {
				((ObjectNode)policyDetails).put("secondnonEleAccessoriesSI", 0);
				((ObjectNode)policyDetails).put("thirednonEleAccessoriesSI", 0);
		  }
		 if(policyDetails.get("eleAccessoriesSI").asInt()>0)
		  {
				int eleAccessoriesSI = policyDetails.get("eleAccessoriesSI").asInt();
				int secondeleAccessoriesSI=(int)(eleAccessoriesSI*84.21/100);
				int thiredeleAccessoriesSI=(int)(eleAccessoriesSI*73.684/100);
				((ObjectNode)policyDetails).put("secondeleAccessoriesSI", secondeleAccessoriesSI);
				((ObjectNode)policyDetails).put("thiredeleAccessoriesSI", thiredeleAccessoriesSI);
				 secondtotalValueVehicle = secondtotalValueVehicle+secondeleAccessoriesSI;
				 thiredtotalValueVehicle = thiredtotalValueVehicle+thiredeleAccessoriesSI;
		  }
		  else
		   {
				 ((ObjectNode)policyDetails).put("secondeleAccessoriesSI", 0);
			     ((ObjectNode)policyDetails).put("thiredeleAccessoriesSI", 0);
		   }
		  if(policyDetails.get("cngLpgKitSI").asInt()>0)
		   {
				int cngLpgKitSI = policyDetails.get("cngLpgKitSI").asInt();
				int secondCngLpgKitSI=(int)(cngLpgKitSI*84.21/100);
				int thiredCngLpgKitSI=(int)(cngLpgKitSI*73.684/100);
				((ObjectNode)policyDetails).put("secondCngLpgKitSI", secondCngLpgKitSI);
				((ObjectNode)policyDetails).put("thiredCngLpgKitSI", thiredCngLpgKitSI);
				 secondtotalValueVehicle = secondtotalValueVehicle+secondCngLpgKitSI;
				 thiredtotalValueVehicle = thiredtotalValueVehicle+thiredCngLpgKitSI;
		   }
	      else
		   {
			    ((ObjectNode)policyDetails).put("secondCngLpgKitSI", 0);
				((ObjectNode)policyDetails).put("thiredCngLpgKitSI", 0);  
		    }
		
		((ObjectNode)policyDetails).put("secondTotalValueVehicle", secondtotalValueVehicle);
		((ObjectNode)policyDetails).put("thiredTotalValueVehicle", thiredtotalValueVehicle);
    	 return reqDataNode;
	}
	

}
