package com.idep.healthquote.queue.listener;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class CignaHealthRequestListener 
{
		static ObjectMapper objectMapper = new ObjectMapper();
		Logger log = Logger.getLogger(CignaHealthRequestListener.class.getName());	
		static CBService productService = CBInstanceProvider.getProductConfigInstance();
		static ObjectNode products = null;
		static String ALL_Health_SA_Details;
		static List<Map<String, Object>> healthSADetailsList;
		
		static
		{
			ALL_Health_SA_Details ="select ProductData.* from ProductData where documentType='HealthSADetails' and planId=13 and carrierId=35 and rate=10000";
			healthSADetailsList = productService.executeQuery(ALL_Health_SA_Details);
		}
		
				 
		public String findSADetails(int siRequest)
		{
			 String basePlanOptionCd = null;
			 try
			 {		
				 JsonNode healthSAListNode = objectMapper.readTree(objectMapper.writeValueAsString(healthSADetailsList));
				 
				 ArrayNode saDetailsArrNode = (ArrayNode)healthSAListNode;
				 for(JsonNode saDetailsNode :saDetailsArrNode)
				 {
					 log.info("SAnode : "+saDetailsNode);
					 //System.out.println("node : "+saDetailsNode.get("minSumInsured").doubleValue());
					 if(siRequest >= saDetailsNode.get("minSumInsured").doubleValue() && siRequest <= saDetailsNode.get("maxSumInsured").doubleValue())
					 {
						 System.out.println("BasePlan is : "+saDetailsNode.get("basePlanOptionCd").textValue());
						 basePlanOptionCd = saDetailsNode.get("basePlanOptionCd").textValue();
						 break;
					 }
				 }
			 }
			 catch(Exception e)
			 {
				 log.error(e);
			 } 
			
			 return basePlanOptionCd;		  
		}
		
		public String onMessage(Message message) {
			 
			 String queueMsg=null;
			 
			 try
			 {
				 if ((message instanceof TextMessage))
			      {
			        TextMessage text = (TextMessage)message;
			        queueMsg = text.getText();
			        JsonNode queueMsgNode = objectMapper.readTree(queueMsg);
			        log.info("p365msg: " +queueMsgNode.toString());
			        return queueMsgNode.toString();
			       
			      }
				 else
			      {
			        this.log.error("P365HealthReqQ  message is not an instance of TextMessage ");
			        return queueMsg;
			      }
				 
			 }
			 
			    catch (JMSException e)
			    {
			      this.log.error("JMSException at P365HealthReqQListener : ", e);
			      return queueMsg;
			    }
			    catch (JsonProcessingException e)
			    {
				  this.log.error("JsonProcessingException at P365HealthReqQListener : ", e);
			      return queueMsg;
			    }
			    catch (IOException e)
			    {
				  this.log.error("IOException at P365HealthReqQListener : ", e);
			      return queueMsg;
			    }
			 
		 }
		
	public static void main(String[] args) 
	{
		System.out.println("Enter SI value:");
		Scanner input = new Scanner(System.in); 
		int sumInsuredRequest = input.nextInt();
		CignaHealthRequestListener cignaHealthReq = new CignaHealthRequestListener();
		cignaHealthReq.findSADetails(sumInsuredRequest);
	}

}
