package com.idep.data.xmlreader;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
/**
 * 
 * @author sandeep.jadhav
 * sample code to read xml file
 */
public class ReadXMLFile
{
  private static final String XML_FILE = "Couchbase-config.xml";
  static InputStream inputStream = null;
  static HashMap<String, String> xmldata = new HashMap<String, String>();
  
  static
  {
    try
    {
      inputStream = 
        ReadXMLFile.class.getClassLoader().getResourceAsStream(XML_FILE);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(inputStream);
      
      doc.getDocumentElement().normalize();
      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      NodeList nList = doc.getElementsByTagName("entity");
      for (int temp = 0; temp < nList.getLength(); temp++)
      {
        Node nNode = nList.item(temp);
        Element eElement = (Element)nNode;
        xmldata.put(eElement.getAttribute("name"), eElement.getAttribute("value"));
        System.out.println("ENTITY NAME " + eElement.getAttribute("name"));
        System.out.println("ENTITY VALUE: " + eElement.getAttribute("value"));
      }
    }
    catch (SAXParseException e)
    {
      e.printStackTrace();
      System.out.println("invalid xml file");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("exception occurred");
    }
  }
  
  public HashMap<String, String> getXMLAsHashMap()
  {
    return xmldata;
  }
}
