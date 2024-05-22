package com.idep.travelquote.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class TravelCountryCacheProcessor
{
  static ObjectNode productDataConfigList = null;
  
  static
  {
    ObjectMapper objectMapper = new ObjectMapper();
    Logger log = Logger.getLogger(TravelProductRequestProcessor.class.getName());
    CBService serverConfig = null;
    CBService ProductDataService = null;
    JsonNode docConfigNode = null;
    productDataConfigList = objectMapper.createObjectNode();
    try
    {
      if (serverConfig == null)
      {
        serverConfig = CBInstanceProvider.getServerConfigInstance();
        docConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("TravelDataCacheList").content()).toString());
        log.info("Country List Document Cached : TravelDataCacheList");
      }
      if (ProductDataService == null) {
        ProductDataService = CBInstanceProvider.getProductConfigInstance();
      }
      Map<String, Object> configMap = (Map)objectMapper.readValue(docConfigNode.get("documentList").toString(), Map.class);
      System.out.println("Config Map  : " + configMap);
      for (Map.Entry<String, Object> entry : configMap.entrySet())
      {
        Map<String, String> confMap = (Map)objectMapper.readValue(objectMapper.writeValueAsString(entry.getValue()), Map.class);
        
        ObjectNode docListNode = objectMapper.createObjectNode();
        for (Map.Entry<String, String> docConfig : confMap.entrySet()) {
          if (((String)docConfig.getValue()).equalsIgnoreCase("Y"))
          {
            System.out.println("docConfig.getKey() : " + (String)docConfig.getKey());
            JsonDocument Document = ProductDataService.getDocBYId((String)docConfig.getKey());
            if (Document != null) {
              docListNode.put((String)docConfig.getKey(), objectMapper.readTree(((JsonObject)Document.content()).toString()));
            } else {
              System.out.println("Document not found : " + (String)docConfig.getKey());
            }
          }
          else
          {
            log.error("Carrier Country List Document not  Cached : " + ((String)docConfig.getKey()).toString());
          }
        }
        productDataConfigList.put((String)entry.getKey(), docListNode);
      }
    }
    catch (Exception e)
    {
      log.error("unable to cache Country List : ", e);
    }
  }
  
  public static ObjectNode getAllCarrierCountryList()
  {
    System.out.println("Country List Cached ");
    
    return productDataConfigList;
  }
  
  public static void main(String[] args)
  {
    ObjectMapper objectMapper1 = new ObjectMapper();
    try
    {
      String str = "\"TravelContinentList\":\"[{\"continent\":\"Africa\",\"countryCode\":45,\"displayField\":\"Algeria\",\"country\":\"ALGERIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":46,\"displayField\":\"Angola\",\"country\":\"ANGOLA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":47,\"displayField\":\"Benin\",\"country\":\"BENIN\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":48,\"displayField\":\"Botswana\",\"country\":\"BOTSWANA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":49,\"displayField\":\"Burkina\",\"country\":\"BURKINA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":50,\"displayField\":\"Burundi\",\"country\":\"BURUNDI\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":51,\"displayField\":\"Cameroon\",\"country\":\"CAMEROON\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":52,\"displayField\":\"Cape Verde\",\"country\":\"CAPE VERDE\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":53,\"displayField\":\"Central African Republic\",\"country\":\"CENTRAL AFRICAN REPUBLIC\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":54,\"displayField\":\"Chad\",\"country\":\"CHAD\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":55,\"displayField\":\"Comoros\",\"country\":\"COMOROS\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":56,\"displayField\":\"Congo\",\"country\":\"CONGO\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":57,\"displayField\":\"Congo, Democratic Republic\",\"country\":\"CONGO, DEMOCRATIC REPUBLIC\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":58,\"displayField\":\"Djibouti\",\"country\":\"DJIBOUTI\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":59,\"displayField\":\"Egypt\",\"country\":\"EGYPT\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":60,\"displayField\":\"Equatorial Guinea\",\"country\":\"EQUATORIAL GUINEA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":61,\"displayField\":\"Eritrea\",\"country\":\"ERITREA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":62,\"displayField\":\"Ethiopia\",\"country\":\"ETHIOPIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":63,\"displayField\":\"Gabon\",\"country\":\"GABON\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":64,\"displayField\":\"Gambia\",\"country\":\"GAMBIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":65,\"displayField\":\"Ghana\",\"country\":\"GHANA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":66,\"displayField\":\"Guinea\",\"country\":\"GUINEA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":67,\"displayField\":\"Guinea-Bissau\",\"country\":\"GUINEA-BISSAU\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":68,\"displayField\":\"Ivory Coast\",\"country\":\"IVORY COAST\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":69,\"displayField\":\"Kenya\",\"country\":\"KENYA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":70,\"displayField\":\"Lesotho\",\"country\":\"LESOTHO\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":71,\"displayField\":\"Liberia\",\"country\":\"LIBERIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":72,\"displayField\":\"Libya\",\"country\":\"LIBYA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":73,\"displayField\":\"Madagascar\",\"country\":\"MADAGASCAR\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":74,\"displayField\":\"Malawi\",\"country\":\"MALAWI\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":75,\"displayField\":\"Mali\",\"country\":\"MALI\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":76,\"displayField\":\"Mauritania\",\"country\":\"MAURITANIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":77,\"displayField\":\"Mauritius\",\"country\":\"MAURITIUS\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":78,\"displayField\":\"Morocco\",\"country\":\"MOROCCO\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":79,\"displayField\":\"Mozambique\",\"country\":\"MOZAMBIQUE\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":80,\"displayField\":\"Namibia\",\"country\":\"NAMIBIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":81,\"displayField\":\"Niger\",\"country\":\"NIGER\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":82,\"displayField\":\"Nigeria\",\"country\":\"NIGERIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":83,\"displayField\":\"Rwanda\",\"country\":\"RWANDA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":84,\"displayField\":\"Sao Tome and Principe\",\"country\":\"SAO TOME AND PRINCIPE\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":85,\"displayField\":\"Senegal\",\"country\":\"SENEGAL\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":86,\"displayField\":\"Seychelles\",\"country\":\"SEYCHELLES\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":87,\"displayField\":\"Sierra Leone\",\"country\":\"SIERRA LEONE\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":88,\"displayField\":\"Somalia\",\"country\":\"SOMALIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":89,\"displayField\":\"South Africa\",\"country\":\"SOUTH AFRICA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":90,\"displayField\":\"South Sudan\",\"country\":\"SOUTH SUDAN\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":91,\"displayField\":\"Sudan\",\"country\":\"SUDAN\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":92,\"displayField\":\"Swaziland\",\"country\":\"SWAZILAND\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":93,\"displayField\":\"Tanzania\",\"country\":\"TANZANIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":94,\"displayField\":\"Togo\",\"country\":\"TOGO\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":95,\"displayField\":\"Tunisia\",\"country\":\"TUNISIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":96,\"displayField\":\"Uganda\",\"country\":\"UGANDA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":97,\"displayField\":\"Zambia\",\"country\":\"ZAMBIA\",\"continentCode\":2},{\"continent\":\"Africa\",\"countryCode\":98,\"displayField\":\"Zimbabwe\",\"country\":\"ZIMBABWE\",\"continentCode\":2}]";
      ArrayNode json = (ArrayNode)objectMapper1.readTree(str);
      System.out.println("JNSO L: " + json);
      if ((!json.has("countryCode")) || (json.findValue("countryCode").asInt() != 5)) {
        System.out.println("not found");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
