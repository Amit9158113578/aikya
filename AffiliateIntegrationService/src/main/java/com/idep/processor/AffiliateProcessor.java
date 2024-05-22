package com.idep.processor;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.dbaccess.AffiliateAccess;
import com.idep.util.AffilationConstants;

public class AffiliateProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AffiliateProcessor.class.getName());
	public void process(Exchange exchange)
	  {
	    try
	    {      
	        String input = exchange.getIn().getBody().toString();
	        ObjectNode reqNodeData = (ObjectNode) this.objectMapper.readTree(input);
	        String action=null;
	        if(reqNodeData.has("action"))
	        {
	        	action = reqNodeData.get("action").asText();
	        	reqNodeData.remove("action");
	        }else{
	        	log.error(AffilationConstants.ACTION_NOT_FOUND);
	        }
            if(action.equals("CREATE"))
	        {
	        String response = new AffiliateAccess().createAffiliate(reqNodeData);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	       // response==AffilationConstants.DB_ACCESS_FAILURE_RESPONSE ||response==null
	           if(response.equals("doc_replaced"))
		        {  log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
		        finalresultNode.put("responseCode",AffilationConstants.SUCCESS_CODE);
		        finalresultNode.put("message", "success");
		        finalresultNode.put("data",response);
	        	exchange.getIn().setBody(finalresultNode);
		        }
		        else
		        {log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
		        finalresultNode.put("responseCode",AffilationConstants.FAILURE_CODE);
		        finalresultNode.put("message",AffilationConstants.AFFILIATE_CREATION_FAILURE);
		        finalresultNode.put("data",response);
	        	exchange.getIn().setBody(finalresultNode);   
		        	
		        }
		    //exchange.getIn().setBody(finalresultNode);
	        }
	       else if(action.equals("DELETE"))
 	       {
	    	String response = new AffiliateAccess().deleteAffiliate(reqNodeData);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	        //response==AffilationConstants.DB_ACCESS_FAILURE_RESPONSE||response==null
	        if(response.equals("doc_replaced"))
	        {   log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
	        finalresultNode.put("responseCode", AffilationConstants.SUCCESS_CODE);
	        finalresultNode.put("message", "success");
	        finalresultNode.put("data",response);
        	exchange.getIn().setBody(finalresultNode);	   
        	}
	        else
	        {   log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
	        finalresultNode.put("responseCode",AffilationConstants.FAILURE_CODE);
	        finalresultNode.put("message",AffilationConstants.AFFILIATE_DELETION_FAILURE);
	        finalresultNode.put("data",response);
        	exchange.getIn().setBody(finalresultNode);
	        }
	  //  exchange.getIn().setBody(finalresultNode);
  	       }
	       else if(action.equals("UPDATE"))
 	       {String  response=null;
	    	response = new AffiliateAccess().updateAffiliate(reqNodeData);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
        	if(response.equals("doc_replaced"))
	        {   log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
	        finalresultNode.put("responseCode", AffilationConstants.SUCCESS_CODE);
	        finalresultNode.put("message", "success");
	        finalresultNode.put("data",response);
        	exchange.getIn().setBody(finalresultNode);
	        }
        	else
	        {   log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
	        finalresultNode.put("responseCode",AffilationConstants.FAILURE_CODE);
	        finalresultNode.put("message",AffilationConstants.AFFILIATE_UPDATE_FAILURE);
	        finalresultNode.put("data",response);
        	exchange.getIn().setBody(finalresultNode);
        		
	        }
	   // exchange.getIn().setBody(finalresultNode);
	       }
           else if(action.equals("FETCH"))
  	       {String failure_response=null; 
        	JsonNode response=null;
        	response = new AffiliateAccess().fetchAffiliate(reqNodeData.get("deviceId").asText());
        	if(response.has("response")){
        		failure_response=response.get("response").asText();
        	}
 	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
         	if(failure_response==null){
 	         log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
	        finalresultNode.put("responseCode", AffilationConstants.SUCCESS_CODE);
	        finalresultNode.put("message", "success");
	        finalresultNode.put("data",response);
        	exchange.getIn().setBody(finalresultNode);
 	    	 
 	        }
 	        else
 	        {  log.info(AffilationConstants.RESPONSE_IN_PROCESSOR);
		        finalresultNode.put("responseCode",AffilationConstants.FAILURE_CODE);
		        finalresultNode.put("message",AffilationConstants.AFFILIATE_FETCH_FAILURE);
		        finalresultNode.put("data",failure_response);
	        	exchange.getIn().setBody(finalresultNode);
	         
 	        }
 	    exchange.getIn().setBody(finalresultNode);
   	       }
	        
	     }
	    catch (Exception e)
	    {
		      this.log.error(AffilationConstants.EXCEPTION_IN_PROCESSOR, e);
		      ObjectNode finalresultNode = this.objectMapper.createObjectNode();  
		      finalresultNode.put("responseCode",AffilationConstants.FAILURE_CODE);
		      finalresultNode.put("message", "Failure");
		      finalresultNode.put("data", e.getMessage());
		      exchange.getIn().setBody(finalresultNode);
	    }
	  }

}
