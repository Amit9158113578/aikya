 package com.idep.policy.document.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
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
 import net.sf.jasperreports.engine.DefaultJasperReportsContext;
 import net.sf.jasperreports.engine.JRDataSource;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRPropertiesUtil;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.JasperReportsContext;
 import net.sf.jasperreports.engine.data.JsonDataSource;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
 
 public class PolicyDocJasperProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(PolicyDocJasperProcessor.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   CBService policyTranscation = CBInstanceProvider.getPolicyTransInstance();
   
   public void process(Exchange exchange) throws Exception {
     String reportFileName = null;
     try {
       exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1L);
       ExtendedJsonNode pdfConfigNode = new ExtendedJsonNode(Utils.mapper.readTree(exchange.getProperty("carrierReqMapConf").toString()));
       String reqJSON = exchange.getIn().getBody().toString();
       ExtendedJsonNode reqNode = new ExtendedJsonNode(Utils.mapper.readTree(reqJSON));
       this.log.info("jasper report list node " + reqNode);
       ExtendedJsonNode policyDetails = reqNode.get("policyDetails");
       ExtendedJsonNode premiumODTableDataArray = policyDetails.get("premiumODTableData");
       String ownDamage = premiumODTableDataArray.get(0).getKey("name");
       Font font = new Font(ownDamage, 1, 8);
       policyDetails.get("premiumODTableData").get(0).put("name", font.getName());
       if (policyDetails.get("premiumLiabilityTableData").get(2).asDouble("value") == 0.0D) {
         policyDetails.get("premiumLiabilityTableData").get(2).put("name", "");
         policyDetails.get("premiumLiabilityTableData").get(2).put("value", "");
         policyDetails.putInt("limitsOfLibilityOwnerDriver", 0);
       } 
       if (!policyDetails.has("limitsOfLibilityOwnerDriver"))
         policyDetails.put("limitsOfLibilityOwnerDriver", "15,00,000"); 
       if (policyDetails.asInt("PACoverForAmt") > 0 && policyDetails.asInt("PACoverAmt") > 0) {
         ArrayNode premiumLiabilityTableData = policyDetails.get("premiumLiabilityTableData").iterator();
         ObjectNode createObjectNode = Utils.mapper.createObjectNode();
         createObjectNode.put("name", "Unnamed PA cover for driver Rs " + policyDetails.asInt("PACoverForAmt") + " per person");
         createObjectNode.put("value", policyDetails.asInt("PACoverAmt"));
         premiumLiabilityTableData.insert(3, (JsonNode)createObjectNode);
         ArrayNode premiumODTableData = policyDetails.get("premiumODTableData").iterator();
         ObjectNode premiumOD = Utils.mapper.createObjectNode();
         premiumOD.put("name", "");
         premiumOD.put("value", "");
         premiumODTableData.insert(3, (JsonNode)premiumOD);
       } 
       if (policyDetails.asInt("PACoverAmt") > 0 && policyDetails.asInt("PACoverForAmt") > 0) {
         reportFileName = pdfConfigNode.get("policyDocumentConfig").getKey("jasperFileLocationforPACover");
       } else {
         reportFileName = pdfConfigNode.get("policyDocumentConfig").getKey("jasperFileLocation");
       } 
       if (reqNode.findValue("insuranceType").textValue().equals("new")) {
         reportFileName = pdfConfigNode.get("policyDocumentConfig").getKey("jasperFileLocationnew");
         String tpPeriodOfInsuranceTo = calculateTpDate(policyDetails.getKey("periodOfInsuranceFrom"));
         policyDetails.put("tpPeriodOfInsuranceTo", tpPeriodOfInsuranceTo);
         if (policyDetails.asInt("limitsOfLibilityOwnerDriver") > 0) {
           String PACoverEndDate = calculatePADate(policyDetails.getKey("periodOfInsuranceFrom"), policyDetails.asInt("paPeriodOfInsuranceTo"));
           policyDetails.put("paPeriodOfInsuranceTo", PACoverEndDate);
           policyDetails.put("paPeriodOfInsuranceFrom", policyDetails.getKey("periodOfInsuranceFrom"));
           policyDetails.put("paCoverLable", "Compulsory PA for Owner- Driver:");
           policyDetails.put("paFrom", "From:");
           policyDetails.put("paTo", "To:");
           policyDetails.put("midnight", "Midnight");
         } else {
           policyDetails.remove("paPeriodOfInsuranceTo");
         } 
         updatePolicyEndDateForNewVehicle(tpPeriodOfInsuranceTo, policyDetails.getKey("proposalId"));
       } 
       ByteArrayInputStream iStream = new ByteArrayInputStream(reqNode.toString().getBytes());
       String outFileName = String.valueOf(String.valueOf(pdfConfigNode.get("policyDocumentConfig").getKey("outputFilePath"))) + policyDetails.getKey("policyNumber").replace("/", "");
       JsonDataSource jsonDataSource = new JsonDataSource(iStream, "policyDetails");
       HashMap<String, Object> hm = new HashMap<>();
       ArrayNode reportParamConfig = pdfConfigNode.get("policyDocumentConfig").get("reportParamConfig").iterator();
       for (JsonNode param : reportParamConfig)
         hm.put(param.get("paramKey").asText(), param.get("paramValue").asText()); 
       DefaultJasperReportsContext defaultJasperReportsContext = DefaultJasperReportsContext.getInstance();
       JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance((JasperReportsContext)defaultJasperReportsContext);
       
       jrPropertiesUtil.setProperty("net.sf.jasperreports.default.pdf.font.name", "Helvetica");
       jrPropertiesUtil.setProperty("net.sf.jasperreports.default.pdf.encoding", "UTF-8");
       jrPropertiesUtil.setProperty("net.sf.jasperreports.default.pdf.embedded", "false");
       JasperReport jasperReport = JasperCompileManager.compileReport(reportFileName);
       ImageIO.scanForPlugins();
       JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hm, (JRDataSource)jsonDataSource);
       File file = File.createTempFile("output.", ".pdf");
       JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(file));
       Path path = Paths.get(file.getPath(), new String[0]);
       byte[] data = Files.readAllBytes(path);
       String base64string = Base64.encodeBase64String(data);
       FileOutputStream fos = new FileOutputStream(String.valueOf(String.valueOf(outFileName)) + "." + pdfConfigNode.get("policyDocumentConfig").getKey("outputFileExtension"));
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
       exchange.getIn().setBody(Utils.mapper.writeValueAsString(pdfSignReq));
     } catch (JRException e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYDOCJASPPRO|ERROR|policy doc jasper processing failed :", (Throwable)e);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYDOCJASPPRO|ERROR|policy doc jasper processing failed :", e);
     } 
   }
   
   public String calculateTpDate(String tpStartDate) throws ParseException {
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     Date tpstartdate = dateFormat.parse(tpStartDate);
     Calendar.getInstance().setTime(tpstartdate);
     Calendar cal = new GregorianCalendar();
     cal.setTime(tpstartdate);
     cal.add(1, 5);
     Date nextYear = cal.getTime();
     Calendar.getInstance().setTime(nextYear);
     cal.add(7, -1);
     nextYear = cal.getTime();
     String tpPolicyEndDate = dateFormat.format(nextYear);
     return tpPolicyEndDate;
   }
   
   public String calculatePADate(String tpStartDate, int paCoverAge) throws ParseException {
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     Date tpstartdate = dateFormat.parse(tpStartDate);
     Calendar.getInstance().setTime(tpstartdate);
     Calendar cal = new GregorianCalendar();
     cal.setTime(tpstartdate);
     if (paCoverAge == 5) {
       cal.add(1, 5);
     } else {
       cal.add(1, 1);
     } 
     Date nextYear = cal.getTime();
     Calendar.getInstance().setTime(nextYear);
     cal.add(7, -1);
     nextYear = cal.getTime();
     String paPolicyEndDate = dateFormat.format(nextYear);
     return paPolicyEndDate;
   }
   
   public String updatePolicyEndDateForNewVehicle(String tpEndDate, String proposalId) throws JsonProcessingException, IOException {
     JsonDocument docBYId = this.policyTranscation.getDocBYId(proposalId);
     JsonNode proposalDocNode = Utils.mapper.readTree(((JsonObject)docBYId.content()).toString());
     ((ObjectNode)proposalDocNode.get("proposalRequest").get("insuranceDetails")).put("policyEndDate", tpEndDate);
     JsonObject pbquoterequestdb = JsonObject.fromJson(proposalDocNode.toString());
     String docstatus = this.policyTranscation.replaceDocument(proposalId, pbquoterequestdb);
     this.log.info("policyEndDate updated successfully :" + docstatus);
     return docstatus;
   }
 }


