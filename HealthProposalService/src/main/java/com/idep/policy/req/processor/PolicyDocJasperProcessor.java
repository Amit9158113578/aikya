package com.idep.policy.req.processor;


import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.jms.ObjectMessage;

import net.sf.jasperreports.engine.JRDataSource;
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

public class PolicyDocJasperProcessor implements Processor {
  Logger log = Logger.getLogger(PolicyDocJasperProcessor.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  CBService policyTranscation = CBInstanceProvider.getPolicyTransInstance();
  
  public void process(Exchange exchange) throws Exception {
    String reportFileName = null;
    try {
      //exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1L);
      //String reqJSON = exchange.getIn().getBody().toString();
      String reqJSON ="{\"premiumODTableData\":{\"name\":\"Own Damage\",\"value\":\"1536\"}}";
      JsonNode reqNode = objectMapper.readTree(reqJSON);
      this.log.info("jasper report list node " + reqNode);
  
     
      ByteArrayInputStream iStream = new ByteArrayInputStream(reqNode.toString().getBytes());
      log.info("iStream :"+iStream);
      String outFileName = "/home/prodmaster/jasper/123";
      JsonDataSource jsonDataSource = new JsonDataSource(iStream, "premiumODTableData");
      log.info("jsonDataSource :"+jsonDataSource);
      HashMap<String, Object> hm = new HashMap<>();
      hm.put("KOTAKLOGO", "/home/prodmaster/jasper/img/KotakLogo.jpg"); 
     /* ArrayNode reportParamConfig = pdfConfigNode.get("policyDocumentConfig").get("reportParamConfig").iterator();
      for (JsonNode param : reportParamConfig)
        hm.put(param.get("paramKey").asText(), param.get("paramValue").asText()); */
      reportFileName= "/home/prodmaster/jasper/Blank_A4.jrxml";
      log.info("reportFileName :"+reportFileName);
      //JRProperties.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
     // JRProperties.setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
     // JRProperties.setProperty("net.sf.jasperreports.default.pdf.embedded", "false");
      log.info("compiling report ");
      JasperReport jasperReport = JasperCompileManager.compileReport(reportFileName);
      //ImageIO.scanForPlugins();
      log.info("jasperReport "+jasperReport);

      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hm, (JRDataSource)jsonDataSource);
      log.info("jasperPrint "+jasperPrint);
	  JasperExportManager.exportReportToPdfFile(jasperPrint, "/home/prodmaster/jasper/Blank_A4.pdf");
	    
	  /*File file = File.createTempFile("output.", ".pdf");
      JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(file));
      Path path = Paths.get(file.getPath(), new String[0]);
      byte[] data = Files.readAllBytes(path);
      String base64string = Base64.encodeBase64String(data);
      log.info("base64string :"+base64string);
      FileOutputStream fos = new FileOutputStream(String.valueOf(outFileName) + "." + "pdf");
      fos.write(Base64.decodeBase64(base64string));
      fos.close();
      JsonNode userPolicyKeyNode = (JsonNode)exchange.getProperty("userPolicyKeys", JsonNode.class);
      exchange.setProperty("pdfFileLocation", outFileName);
      ObjectNode pdfSignReq = Utils.mapper.createObjectNode();
      pdfSignReq.put("uKey", userPolicyKeyNode.findValue("uKey").textValue());
      pdfSignReq.put("pKey", userPolicyKeyNode.findValue("pKey").textValue());
      pdfSignReq.put("policyDocStream", base64string);
      Thread.sleep(4000L);
      exchange.getIn().setHeader("pdfSignURL", pdfConfigNode.getKey("URL"));
      this.log.info("PDF sign webservice URL : " + pdfConfigNode.getKey("URL"));
      exchange.getIn().setBody(Utils.mapper.writeValueAsString(pdfSignReq));*/
    } catch (JRException e) {
      this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "POLICYDOCJASPPRO" + "|ERROR|" + "policy doc jasper processing failed :", (Throwable)e);
    } catch (Exception e) {
      this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "POLICYDOCJASPPRO" + "|ERROR|" + "policy doc jasper processing failed :", e);
    } 
  }
  public void DocJaper(){
	  
  }
  
  public static void main(String[] args) throws JsonProcessingException, IOException {
	
	String sample = "{\"policyDetails\":{\"RTOLocation\":\"HUNGUND\",\"compulsoryDeductibles\":100,\"nonEleAccessoriesSI\":0,\"periodOfInsuranceFrom\":\"28/02/2021\",\"insurerPincode\":\"587120\",\"policyNumber\":\"PTT/0225200536\",\"policyProductType\":\"Comprehensive Policy\",\"insuranceType\":\"renew\",\"imtCodes\":\"22\",\"insurerDistrict\":\"\",\"receiverCountry\":\"INDIA\",\"custCareEmailId\":\"carekotak.com\",\"krishiKalyanCess\":0.5,\"insurerEmailId\":\"pmsantugmail.com\",\"PACoverAmt\":0,\"variant\":\"SPOKE-SELF START\",\"registrationDate\":\"12/06/2017\",\"totalPackage\":0,\"swachhBharatCess\":0.5,\"periodOfInsuranceTo\":\"27/02/2022\",\"model\":\"HF DELUXE\",\"paPeriodOfInsuranceTo\":\"\",\"additionalExcess\":0,\"insurerCountry\":\"INDIA\",\"engineNo\":\"HA11EPH4A08235\",\"totalValueVehicle\":27070,\"SACCode\":997134,\"basicTPPDPremium\":752,\"productId\":6,\"insurerCompanyAddress\":\"8th Floor, Kotak Infinity, Building  No. 21 Infinity Park, Off Western Express Highway General AK Vaidya Marg, Malad(E) Mumbai - 400 0097 , India\",\"insurerState\":\"KARNATAKA\",\"dateOfLetter\":\"27-Dec-2021\",\"ncbPercentage\":\"\",\"rejectPolicyPeriod\":\"15 days\",\"totalLiabilityPremium\":752,\"premiumTaxTableData\":[{\"name\":\"Taxable Value Of Services (A+B)\",\"value\":1286.0},{\"name\":\"IGST 18\",\"value\":231.0},{\"name\":\"Total Premium\",\"value\":1517}],\"receiverAddress\":\"1305/A AIHOLLIYAVARA ONI WARD NO 03 KAMATAGIHUNGUND - 587120KARNATAKA(17)IndiaContact Details 9845996711\",\"chassisNumber\":\"MBLHAR051H4A08082\",\"eleAccessoriesSI\":0,\"swachhBharatCessAmt\":0,\"policyProductName\":\"Kotak Car Secure\",\"cngLpgKitSI\":0,\"vehicleRegCity\":\"HUNGUND\",\"serviceTaxAmt\":231,\"receiverState\":\"KARNATAKA\",\"idv\":27070,\"totalOwnDamagePremium\":0,\"salutation\":\"Mr\",\"insurerCompanyWebsite\":\"www.kotakgeneralinsurance .com\",\"totalDeductibles\":100,\"insurerAddress\":\"#1305/A AIHOLLIYAVARA ONI WARD NO 03 KAMATAGIHUNGUND - 587120KARNATAKA(17)IndiaContact Details 9845996711\",\"businessLineId\":2,\"insurerPhoneNumber\":\"NA\",\"financeInstitution\":\"ABC\",\"discountList\":[{\"discountId\":2,\"type\":\"NCB Discount\",\"discountAmount\":0}],\"tollFreeNumber\":\"1800  266 4545\",\"description\":\"Motor vehicle insurance services\",\"PACoverForAmt\":0,\"TransactionNo\":\"12369376149\",\"receiverContactNumber\":\"9845996711\",\"policySignedDate\":\"28/02/2021\",\"proposalId\":\"PROP000B124384\",\"manufacturer\":\"HERO MOTOCORP\",\"trailer\":0,\"insurerMobileNumber\":\"9845996711\",\"receiverPincode\":\"587120\",\"tpPeriodOfInsuranceTo\":\"\",\"receiverDistrict\":\"\",\"basicOwnDamageAmt\":159,\"voluntaryDeductibleSI\":0,\"insurerCompanyName\":\"Kotak Mahindra General Insurance Company Limited\",\"UINs\":\"IRDAN152P0008V01201617\",\"geographicalArea\":\"INDIA\",\"yearOfManufacture\":0,\"referenceNo\":\"GEN/WEL/SG/0008.1/\",\"receiverName\":\"SANTOSH G SHIRUR\",\"policySignedPlace\":\"HUNGUND\",\"premiumLiabilityTableData\":[{\"name\":\"Liability\",\"value\":\"\"},{\"name\":\"Basic TP Including TPPD Premium\",\"value\":752},{\"name\":\"PA Cover for Owner Driver of Rs.15,00,000\",\"value\":\"375\"},{\"name\":\"Total Liability Premium (B)\",\"value\":1127.0}],\"totalPremium\":\"1517.00\",\"cubicCapacity\":97,\"premiumODTableData\":[{\"name\":\"Own Damage\",\"value\":\"\"},{\"name\":\"Basic Own Damage\",\"value\":159},{\"name\":\"Bonus Percent   0\",\"value\":0},{\"name\":\"Total Own Damage Premium (A)\",\"value\":159.0}],\"insurerName\":\"SANTOSH G SHIRUR\",\"registrationNumber\":\"KA29ED3048\",\"voluntaryDeductibleDepreciationCover\":100,\"krishiKalyanCessAmt\":0,\"policyIssueDate\":\"25/02/2021\",\"coverNoteNo\":\"NA \",\"hypothecatedTo\":\"NA\",\"policyIssuingOfficeAddress\":\"415-418 , Saurabh Hall, Opp. Jahangir Hospital, Sasun Road, Near Pune Rly Stn, Pune 411 001.\",\"carrierId\":53,\"legalLiabilityPaidDriver\":\"\",\"policySignAddress\":\"Kotak Mahindra General Insurance Company Limited,27 BKC,C27,G Block,Kurla Complex Bandra(East),Mumbai 400051\",\"seatingCapacity\":2}}";
	ObjectMapper obj = new ObjectMapper();
	JsonNode node = obj.readTree(sample);
	System.out.println(node);
	String reportFileName = "Kotak_New_Bike.jrxml";
	try{
	ByteArrayInputStream iStream = new ByteArrayInputStream(node.toString().getBytes());
    String outFileName = "fileName.pdf";
    JsonDataSource jsonDataSource = new JsonDataSource(iStream, "policyDetails");
	System.out.println(jsonDataSource);
    HashMap<String, Object> hm = new HashMap<>();
    JRProperties.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
    JRProperties.setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
    JRProperties.setProperty("net.sf.jasperreports.default.pdf.embedded", "false");
    JasperReport jasperReport = JasperCompileManager.compileReport(reportFileName);
    ImageIO.scanForPlugins();
    System.out.println(jasperReport);
    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hm, (JRDataSource)jsonDataSource);
    File file = File.createTempFile("output.", ".pdf");
    JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(file));
    Path path = Paths.get(file.getPath(), new String[0]);
    byte[] data = Files.readAllBytes(path);
	}
	catch(Exception e){
		System.out.println(e);

	}
  }
}
