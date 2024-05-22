package com.idep.lifequote.req.transformer;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.lifequote.exception.processor.ExecutionTerminator;
import com.idep.lifequote.req.transformer.XMLCharEmitter;
import com.idep.lifequote.util.LifeQuoteConstants;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLCharEmitter implements Processor{
	
	Logger log = Logger.getLogger(XMLCharEmitter.class.getName());
		ObjectMapper objectMapper =  new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String request  = exchange.getIn().getBody(String.class);
		  
		  JsonNode InputReqNode  = objectMapper.readTree(request.toString());
		  //String modifiedRequest = request.replaceAll("/r/n","");
		  //modifiedRequest = modifiedRequest.replaceAll("/r/n","");
		  log.info("input XML request in XMLCharEmitter before Convert to XML : "+InputReqNode.get("CreateQuoteResponse").get("Response").asText());
		  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			factory.setNamespaceAware(true);
		    DocumentBuilder builder;  
		    builder = factory.newDocumentBuilder();
		    
		    if(InputReqNode.get("CreateQuoteResponse").has("QuotationNumber")){
		    /**
		     * if response in found QuotationNumber then setting in property for future use
		     * **/
		    	
		    	exchange.setProperty(LifeQuoteConstants.QuotationNumber, InputReqNode.get("CreateQuoteResponse").get("QuotationNumber"));
		    }
		    
		    
		    Document document = builder.parse( new InputSource( new StringReader( InputReqNode.get("CreateQuoteResponse").get("Response").asText() ) ) );
			//log.info("Passing the xml :"+document);
		    
		    
		    TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

			//System.out.println("converted XMl : "+output);
			log.debug("modified XML request in XMLCharEmitter : "+output);
		  	exchange.getIn().setBody(output);			
		}
		catch(Exception e)
		{
			 log.error(exchange.getProperty(LifeQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"NullPointerException at XMLCharEmitter  : ",e);
			 throw new ExecutionTerminator();
		}
		  


}
	

}
