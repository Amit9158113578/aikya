package com.idep.api.xpath.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class Test {
	
	private static final Configuration configuration = Configuration.builder()
		    .jsonProvider(new JacksonJsonNodeJsonProvider())
		    .mappingProvider(new JacksonMappingProvider())
		    .build();

		public void a_value_can_be_updated(){

		    String originalJson = "{\n"
		        + "\"session\":\n"
		        + "    {\n"
		        + "        \"name\":\"JSESSIONID\",\n"
		        + "        \"value\":\"5864FD56A1F84D5B0233E641B5D63B52\"\n"
		       
		        + "    },\n"
		        + "\"loginInfo\":\n"
		        + "    {\n"
		        + "        \"loginCount\":77,\n"
		        + "        \"previousLoginTime\":\"2014-12-02T11:11:58.561+0530\"\n"
		        + "    }\n"
		        + "}";

		    System.out.println("before : "+originalJson); 
		    
		   DocumentContext context =  JsonPath.using(configuration).parse(originalJson);

		   //JsonNode updatedJson = JsonPath.using(configuration).parse(originalJson).set("$.session.name", "MYSESSINID").json();
		   // update existing node
		   context.set("$.session.name", "SANDEEP");
		   
		   
		   
		   
		   //context.add("$.session.empno", "EMP1234");
		   // add new node
		   context.put("$.session", "empno", "EMP1234");
		   //context.delete("$.session.name"); it works
		   JsonNode updateddJson = context.set("$.loginInfo.loginCount", 1111).json();
		   if(updateddJson.findValue("loginCount")!=null)
		   System.out.println("found");
		   else
			   System.out.println("not found");
		   System.out.println(updateddJson.toString());
		   
		   JsonNode nd = context.read("$.session.name");
		   System.out.println("nd : "+nd);
		   
		   
		}
		
		public static void main(String[] args) {
			
			
			//Test t = new Test();
			//t.a_value_can_be_updated();
			
			String[] strArray = new String[5];
			
			System.out.println(strArray[0]);
			if(strArray[0]==null)
			{
				System.out.println("string is empty");
			}
			else
			{
				System.out.println("bad luck");
			}
			
		}

}
