package com.idep.healthquote.res.processor;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ErrorXMLReader {

	Logger log = Logger.getLogger(ErrorXMLReader.class.getName());
	public List readXMLerror(String xmlResposne,String StartTag,String validateTag){
		List<String> errList= new ArrayList<>();


		try{
			log.info("ErrorXMLReader Foud in ErrorXMLReader  :StartTag "+StartTag+" "+validateTag);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			factory.setNamespaceAware(true);
			DocumentBuilder builder;  
			builder = factory.newDocumentBuilder();
			Document document = builder.parse( new InputSource( new StringReader( xmlResposne ) ) );
			document.getDocumentElement().normalize();
			int errorCount=0;
			NodeList nList = document.getDocumentElement().getElementsByTagNameNS("*", StartTag);

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node Nnode =nList.item(temp);
				if(Nnode.hasChildNodes()){
					nList = Nnode.getChildNodes();
					for(int i=0 ;i<nList.getLength();i++){
						Node list = nList.item(i);

						if(list.getNodeName().endsWith(validateTag)){
							if(list.getNodeType() == Node.ELEMENT_NODE ){

								Element eElement = (Element) list;

								if(eElement.hasChildNodes()){

									NodeList childNode = eElement.getChildNodes();
									for(int child=0;child<childNode.getLength();child++){
										Node childlist = childNode.item(child);

										if(childlist.getTextContent().length()>0 && childlist.getTextContent()!=null){
											errorCount++;
											errList.add(childlist.getTextContent());
											log.debug("Error Foud in response  :"+childlist.getTextContent());
										}
									}
								}
							}
						}
					}
				}
			}
			if(errorCount>0){
				//System.out.println("Error List : "+errList.toString());
				log.info("Error found : "+errList.toString());
			}else{
				log.info("NO Error List Found");
				System.out.println("NO Error List Found : ");
			}
			log.info("ErrorXMLReader Execution complate  ");
		}catch(Exception e){
			log.info("Exception found in ErrorXMLReader : ",e);
		}




		return errList;
	} 
	public HashSet validateStartTagXML(String xmlResposne,String StartTag){
		HashSet<String> prefixList = new HashSet<String>();


		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
			factory.setNamespaceAware(true);
			DocumentBuilder builder;  
			builder = factory.newDocumentBuilder();
			Document document = builder.parse( new InputSource( new StringReader( xmlResposne ) ) );
			document.getDocumentElement().normalize();
			// List<String> tagCode= new ArrayList<>();

			NodeList nList = document.getDocumentElement().getElementsByTagNameNS("*", StartTag);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node Nnode =nList.item(temp);
				//prefixList.add(Nnode.getPrefix());
				if(Nnode.hasChildNodes()){
					nList = Nnode.getChildNodes();
					for(int i=0 ;i<nList.getLength();i++){
						Node list = nList.item(i);
						prefixList.add(list.getPrefix());

						if(list.getNodeType() == Node.ELEMENT_NODE ){

							Element eElement = (Element) list;

							if(eElement.hasChildNodes()){

								NodeList childNode = eElement.getChildNodes();
								for(int child=0;child<childNode.getLength();child++){
									Node childlist = childNode.item(child);
									prefixList.add(childlist.getPrefix());
									log.debug("node Name :"+childlist.getNodeName());
								}
							}
						}

					}
				}
			}
			log.info("prefix list created successfully : "+prefixList);
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
		}catch(Exception e){
			log.error("Exception found in  ErrorXMLReader : ",e);
		}
		return prefixList;
	}

}