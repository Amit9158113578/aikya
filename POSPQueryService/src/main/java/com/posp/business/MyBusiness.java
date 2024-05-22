package com.posp.business;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.posp.response.PospQueryResponse;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import org.apache.log4j.Logger;


public class MyBusiness {

	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(MyBusiness.class.getName());
	  CBService service = null;
	  CBService PospData = null; 
	  JsonNode responseConfigNode;
	  JsonNode proposalConfigNode;
	  CBService productservice = null;
	  JsonNode searchConfigNode;
	  JsonNode errorNode;
	  CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	  public String businessOperation(String appData)
	  {	
		String customerList_String=null;
		ArrayNode customerList_Array=null ;
		ArrayNode newPolicies_Array=null ;
		ArrayNode ReNewPolicies_Array=null ;
		ObjectMapper objectMapper=null;
		ObjectNode respCustomerNode=null;
		ObjectNode respNewCustomerNode=null;
		ObjectNode respReNewCustomerNode=null;
		ObjectNode ReNewPoliciesNode=null;
        ObjectNode NewPoliciesNode=null;	 
        ObjectNode PoliciesNode=null;
		JsonNode responseNode =null;
		JsonNode customerPolicyNode;
		String requestSource=null;
		JsonNode agentBusinessNode=null;
		String  lob=null;
		String  businessLineId=null;
		String[] lob_arr=null;
		String[] businessLineId_arr=null;
		String query=null;
		String queryForFetchSaleSummary=null;
		String replaceField=null;
		String[] replaceBy_arr=null;
		String replaceBy=null;
		String selectField=null;
		String whereClause=null;
		String userName=null;
		try{
			  if (this.service == null || this.PospData == null)
		      {try{
		    	this.PospData = CBInstanceProvider.getBucketInstance("PospData");
		        this.log.info("ServerConfig bucket instance created");
		        this.service = CBInstanceProvider.getServerConfigInstance();
		        this.responseConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("ResponseMessages").content()).toString());
		        this.searchConfigNode = this.objectMapper.readTree(((JsonObject)this.PospData.getDocBYId("POSPSearchQueryConfig").content()).toString());
		      }catch(Exception e){
		    	  throw new NullPointerException("Unable to fetch ResponseMessages/POSPSearchQueryConfig documents");
		      }
		      }
	 	objectMapper = new ObjectMapper();
		customerList_Array = objectMapper.createArrayNode();
		customerPolicyNode = objectMapper.createObjectNode();
		JsonNode inputdataNode = this.objectMapper.readTree(appData);
		if(inputdataNode.has("operation")){
			if(inputdataNode.get("operation").textValue().equals("getLeadStageSummary")){
			    if(inputdataNode.has("userName") && inputdataNode.has("requestSource")){
			    userName = inputdataNode.get("userName").textValue();
				requestSource=inputdataNode.get("requestSource").textValue();
				if(this.searchConfigNode.has("getBusinessAgent")){
				agentBusinessNode = this.searchConfigNode.get("getBusinessAgent");
				if(this.searchConfigNode.has("leadStages"))
				lob = this.searchConfigNode.get("leadStages").asText();
				if(this.searchConfigNode.has("leadStages"))
				businessLineId = this.searchConfigNode.get("leadStages").asText();
				if(lob!=null)
				lob_arr=lob.split(",");
				if(businessLineId!=null)
				businessLineId_arr=businessLineId.split(",");
				query=agentBusinessNode.get("queryForLeadStageSummary").asText();
				if(agentBusinessNode.has("selectFieldForAgentLeadStageReport"))
				selectField=agentBusinessNode.get("selectFieldForAgentLeadStageReport").asText();
				if(agentBusinessNode.has("whereClauseForAgentLeadStageReport"))
				whereClause=agentBusinessNode.get("whereClauseForAgentLeadStageReport").asText();
				query=query.replace("selectField", selectField);
				query=query.replace("whereClause", whereClause);
				if(agentBusinessNode.has("replaceFieldForAgentLeadStageReport"))
	            replaceField=agentBusinessNode.get("replaceFieldForAgentLeadStageReport").asText();
				if(agentBusinessNode.has("replaceByForAgentLeadStageReport"))
				replaceBy=agentBusinessNode.get("replaceByForAgentLeadStageReport").asText();
	            replaceBy_arr=replaceBy.split(",");
	            queryForFetchSaleSummary="";
			for(int i=0;i<lob_arr.length;i++){
				queryForFetchSaleSummary=queryForFetchSaleSummary+" "+query.replace("{1}",requestSource);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{2}",userName);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{3}","'"+businessLineId_arr[i]+"'");
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace(replaceField, replaceBy_arr[i]);
			}
			queryForFetchSaleSummary =queryForFetchSaleSummary.substring(0, queryForFetchSaleSummary.length() - 5);
			this.log.info("Executing query For Fetch Sale Summary : "+queryForFetchSaleSummary);
		  	customerPolicyNode =objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchSaleSummary)));
		  	this.log.info("Response for Fetch Sale Summary : "+customerPolicyNode);
		  	int totalPolicy=0;
		  	if(customerPolicyNode!=null){ 
				for(JsonNode policyList:customerPolicyNode){
		        try{
		        respCustomerNode = objectMapper.createObjectNode();
		        if(policyList.has("QUOTE")){
		         respCustomerNode.put("count",policyList.get("QUOTE"));
		         if(Integer.parseInt(policyList.get("QUOTE").asText())!=0){
		         totalPolicy=totalPolicy+Integer.parseInt(policyList.get("QUOTE").asText());
		         }
		         respCustomerNode.put("lob","QUOTES SEEN");
		        }
		        if(policyList.has("PROPOSAL")){
		        	respCustomerNode.put("count",policyList.get("PROPOSAL"));
		            if(Integer.parseInt(policyList.get("PROPOSAL").asText())!=0){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("PROPOSAL").asText());
		            }
		            respCustomerNode.put("lob","PROPOSAL SUBMITTED");
		        }
		        if(policyList.has("PAYINIT")){
		        	respCustomerNode.put("count",policyList.get("PAYINIT"));
		        	if(Integer.parseInt(policyList.get("PAYINIT").asText())!=0){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("PAYINIT").asText());
		            }
		        	respCustomerNode.put("lob","PAYMENT INITIATION");
		        }  
		        if(policyList.has("PAYSUCC")){
		        	respCustomerNode.put("count",policyList.get("PAYSUCC"));
		        	if(Integer.parseInt(policyList.get("PAYSUCC").asText())!=0){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("PAYSUCC").asText());
		            }
		        	respCustomerNode.put("lob","PAYMENT SUCCESS");
		        }
		        }catch(NullPointerException e){
		        	throw new Exception(e.getMessage());
		        }
					customerList_Array.add(respCustomerNode);
			    } 
				  respCustomerNode = objectMapper.createObjectNode();
				  respCustomerNode.put("lob","TOTAL");
				  respCustomerNode.put("count",totalPolicy);
				  customerList_Array.add(respCustomerNode);
				  customerList_String = objectMapper.writeValueAsString(customerList_Array);
				  responseNode = this.objectMapper.readTree(customerList_String);  
				  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
				}
			 	  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
			    }else{
					throw new NullPointerException("getBusiness Not found in POSPSearchQueryConfig");
				}
			    }else{
				throw new NullPointerException("requestSource/userName Not Present in request");
			}	
			}
			else if(inputdataNode.get("operation").textValue().equals("getSaleSummary")){
			    if(inputdataNode.has("userName") && inputdataNode.has("requestSource")){
			    userName = inputdataNode.get("userName").textValue();
				requestSource=inputdataNode.get("requestSource").textValue();
				if(this.searchConfigNode.has("getBusinessAgent")){
				agentBusinessNode = this.searchConfigNode.get("getBusinessAgent");
				if(this.searchConfigNode.has("lob"))
				lob = this.searchConfigNode.get("lob").asText();
				if(this.searchConfigNode.has("businessLineId"))
				businessLineId = this.searchConfigNode.get("businessLineId").asText();
				if(lob!=null)
				lob_arr=lob.split(",");
				if(businessLineId!=null)
				businessLineId_arr=businessLineId.split(",");
				query=agentBusinessNode.get("queryForSale").asText();
				if(agentBusinessNode.has("selectFieldForAgentSaleSummary"))
				selectField=agentBusinessNode.get("selectFieldForAgentSaleSummary").asText();
				if(agentBusinessNode.has("whereClauseForAgentSaleSummary"))
				whereClause=agentBusinessNode.get("whereClauseForAgentSaleSummary").asText();
				query=query.replace("selectField", selectField);
				query=query.replace("whereClause", whereClause);
				if(agentBusinessNode.has("replaceFieldForAgentSaleSummary"))
	            replaceField=agentBusinessNode.get("replaceFieldForAgentSaleSummary").asText();
				if(agentBusinessNode.has("replaceByForAgentSaleSummary"))
				replaceBy=agentBusinessNode.get("replaceByForAgentSaleSummary").asText();
	            replaceBy_arr=replaceBy.split(",");
	            queryForFetchSaleSummary="";
			for(int i=0;i<lob_arr.length;i++){
				queryForFetchSaleSummary=queryForFetchSaleSummary+" "+query.replace("{1}",requestSource);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{2}",userName);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{3}",businessLineId_arr[i]);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace(replaceField, replaceBy_arr[i]);
			}
			queryForFetchSaleSummary =queryForFetchSaleSummary.substring(0, queryForFetchSaleSummary.length() - 5);
			this.log.info("Executing query For Fetch Sale Summary : "+queryForFetchSaleSummary);
		  	customerPolicyNode =objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchSaleSummary)));
		  	this.log.info("Response for Fetch Sale Summary : "+customerPolicyNode);
		  	int totalPolicy=0;
		  	float totalPremiumOfAll=0;
		  	if(customerPolicyNode!=null){ 
				for(JsonNode policyList:customerPolicyNode){
		        try{
		        respCustomerNode = objectMapper.createObjectNode();
		        if(policyList.has("lifeCount")){
		         respCustomerNode.put("count",policyList.get("lifeCount"));
		         if(Integer.parseInt(policyList.get("lifeCount").asText())!=0 && policyList.get("totalPremium").asText()!=null){
		         totalPolicy=totalPolicy+Integer.parseInt(policyList.get("lifeCount").asText());
		         totalPremiumOfAll=totalPremiumOfAll+Float.parseFloat(policyList.get("totalPremium").asText());
		         respCustomerNode.put("totalPremium",policyList.get("totalPremium"));
		         }else{
		       	   respCustomerNode.put("totalPremium",0);
		          }
		         respCustomerNode.put("lob","life");
		        }
		        if(policyList.has("bikeCount")){
		        	respCustomerNode.put("count",policyList.get("bikeCount"));
		            if(Integer.parseInt(policyList.get("bikeCount").asText())!=0 && policyList.get("totalPremium").asText()!=null){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("bikeCount").asText());
		            totalPremiumOfAll=totalPremiumOfAll+Float.parseFloat(policyList.get("totalPremium").asText());
		            respCustomerNode.put("totalPremium",policyList.get("totalPremium"));
		            }else{
		         	   respCustomerNode.put("totalPremium",0);
		            }
		            respCustomerNode.put("lob","bike");
		        }
		        if(policyList.has("carCount")){
		        	respCustomerNode.put("count",policyList.get("carCount"));
		        	if(Integer.parseInt(policyList.get("carCount").asText())!=0 && policyList.get("totalPremium").asText()!=null){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("carCount").asText());
		            totalPremiumOfAll=totalPremiumOfAll+Float.parseFloat(policyList.get("totalPremium").asText());
		            respCustomerNode.put("totalPremium",policyList.get("totalPremium"));
		            }else{
		        	   respCustomerNode.put("totalPremium",0);
		           }
		        	respCustomerNode.put("lob","car");
		        }  
		        if(policyList.has("healthCount")){
		        	respCustomerNode.put("count",policyList.get("healthCount"));
		        	if(Integer.parseInt(policyList.get("healthCount").asText())!=0 && policyList.get("totalPremium").asText()!=null){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("healthCount").asText());
		            totalPremiumOfAll=totalPremiumOfAll+Float.parseFloat(policyList.get("totalPremium").asText());
		            respCustomerNode.put("totalPremium",policyList.get("totalPremium"));
		            }else{
		          	   respCustomerNode.put("totalPremium",0);
		             }
		        	respCustomerNode.put("lob","health");
		        }
		        if(policyList.has("travelCount")){
		        	respCustomerNode.put("count",policyList.get("travelCount"));
		        	if(Integer.parseInt(policyList.get("travelCount").asText())!=0 && policyList.get("totalPremium").asText()!=null){
		            totalPolicy=totalPolicy+Integer.parseInt(policyList.get("travelCount").asText());
		            totalPremiumOfAll=totalPremiumOfAll+Float.parseFloat(policyList.get("totalPremium").asText());
		            respCustomerNode.put("totalPremium",policyList.get("totalPremium"));
		            }else{
		          	   respCustomerNode.put("totalPremium",0);
		             }
		        	respCustomerNode.put("lob","travel");
		        }
		        }catch(NullPointerException e){
		        	throw new Exception(e.getMessage());
		        }
					customerList_Array.add(respCustomerNode);
			    } 
				  respCustomerNode = objectMapper.createObjectNode();
				  respCustomerNode.put("lob","total");
				  respCustomerNode.put("count",totalPolicy);
				  respCustomerNode.put("totalPremium",totalPremiumOfAll);
				  customerList_Array.add(respCustomerNode);
				  customerList_String = objectMapper.writeValueAsString(customerList_Array);
				  responseNode = this.objectMapper.readTree(customerList_String);  
				  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
				}
			 	  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
			    }else{
					throw new NullPointerException("getBusiness Not found in POSPSearchQueryConfig");
				}
			    }else{
				throw new NullPointerException("requestSource/userName Not Present in request");
			}	
			}
			else if(inputdataNode.get("operation").textValue().equals("getSaleDetailReport")){
			    if(inputdataNode.has("userName") && inputdataNode.has("requestSource")){
			    userName = inputdataNode.get("userName").textValue();
				requestSource=inputdataNode.get("requestSource").textValue();
				if(this.searchConfigNode.has("getBusinessAgent")){
				agentBusinessNode = this.searchConfigNode.get("getBusinessAgent");
				if(this.searchConfigNode.has("lob"))
				lob = this.searchConfigNode.get("lob").asText();
				if(this.searchConfigNode.has("businessLineId"))
				businessLineId = this.searchConfigNode.get("businessLineId").asText();
				if(lob!=null)
				lob_arr=lob.split(",");
				if(businessLineId!=null)
				businessLineId_arr=businessLineId.split(",");
				queryForFetchSaleSummary=agentBusinessNode.get("queryForSale").asText();
				if(agentBusinessNode.has("selectFieldForAgentSaleDetailReport"))
				selectField=agentBusinessNode.get("selectFieldForAgentSaleDetailReport").asText();
				if(agentBusinessNode.has("whereClauseForAgentSaleDetailReport"))
				whereClause=agentBusinessNode.get("whereClauseForAgentSaleDetailReport").asText();
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("selectField", selectField);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("whereClause", whereClause);
		        queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{1}",requestSource);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{2}",userName);
			this.log.info("Executing query For Fetch Sale Summary : "+queryForFetchSaleSummary);
		  	customerPolicyNode =objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchSaleSummary)));
		  	this.log.info("Response for Fetch Sale Summary : "+customerPolicyNode);
		  	if(customerPolicyNode!=null){ 
		  		for(JsonNode policyList:customerPolicyNode){
			        try{
			        respCustomerNode = objectMapper.createObjectNode();
			        if(policyList.has("businessLineId")){
			         for(int i=0;i<businessLineId_arr.length;i++){
			        	 if(businessLineId_arr[i].equalsIgnoreCase(policyList.get("businessLineId").asText())){
			        		 respCustomerNode=(ObjectNode)policyList;                     
			        		 respCustomerNode.put("lob",lob_arr[i]);
			        	 }
			         }
			        }
			        }catch(NullPointerException e){
			        	throw new Exception(e.getMessage());
			        }
						customerList_Array.add(respCustomerNode);
				    }
		  		 customerList_String = objectMapper.writeValueAsString(customerList_Array);
				 responseNode = this.objectMapper.readTree(customerList_String); 
				  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
				}
			 	  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
			    }else{
					throw new NullPointerException("getBusiness Not found in POSPSearchQueryConfig");
				}
			    }else{
				throw new NullPointerException("requestSource/userName Not Present in request");
			}
			       
			}else if(inputdataNode.get("operation").textValue().equals("getNewRenewalReport")){
				if(inputdataNode.has("userName") && inputdataNode.has("requestSource")){
				newPolicies_Array=objectMapper.createArrayNode();
				ReNewPolicies_Array=objectMapper.createArrayNode();
				userName = inputdataNode.get("userName").textValue();
				requestSource=inputdataNode.get("requestSource").textValue();
				if(this.searchConfigNode.has("getBusinessAgent")){
				agentBusinessNode = this.searchConfigNode.get("getBusinessAgent");
				if(this.searchConfigNode.has("lob"))
				lob = this.searchConfigNode.get("lob").asText();
				if(this.searchConfigNode.has("businessLineId"))
				businessLineId = this.searchConfigNode.get("businessLineId").asText();
				if(lob!=null)
				lob_arr=lob.split(",");
				if(businessLineId!=null)
				businessLineId_arr=businessLineId.split(",");
			    queryForFetchSaleSummary=agentBusinessNode.get("queryForSale").asText();
				if(agentBusinessNode.has("selectFieldForAgentSaleDetailReport"))
				selectField=agentBusinessNode.get("selectFieldForAgentSaleDetailReport").asText();
				if(agentBusinessNode.has("whereClauseForAgentSaleDetailReport"))
				whereClause=agentBusinessNode.get("whereClauseForAgentSaleDetailReport").asText();
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("selectField", selectField);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("whereClause", whereClause);
		        queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{1}",requestSource);
				queryForFetchSaleSummary=queryForFetchSaleSummary.replace("{2}",userName);
			this.log.info("Executing query For Fetch Sale Summary : "+queryForFetchSaleSummary);
			customerPolicyNode =objectMapper.readTree(objectMapper.writeValueAsString(transService.executeQuery(queryForFetchSaleSummary)));
		  	this.log.info("Response for Fetch Sale Summary : "+customerPolicyNode);
		  	int renewPolicyCount=0;
		  	int newPolicyCount=0;
		  	float renewPolicyPremium=0;
		  	float newPolicyPremium=0;
		  	String resp_arrray[]=new String[3]; 
		  	if(customerPolicyNode!=null){ 
		  		for(JsonNode policyList:customerPolicyNode){
			        try{
			        ReNewPoliciesNode=objectMapper.createObjectNode();
					NewPoliciesNode=objectMapper.createObjectNode();
					PoliciesNode=objectMapper.createObjectNode();
					respReNewCustomerNode = objectMapper.createObjectNode();
			        respNewCustomerNode = objectMapper.createObjectNode();
			        respCustomerNode = objectMapper.createObjectNode();
			        if(policyList.has("proposalId")){
			        	proposalConfigNode=this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(policyList.get("proposalId").asText()).content()).toString());
			        	log.info("proposalConfigNode : "+proposalConfigNode);
			        	String insuranceType=proposalConfigNode.get("proposalRequest").get("insuranceDetails").get("insuranceType").asText();
			            float Premium=proposalConfigNode.get("totalPremium").asInt(); 
			        	log.info(Premium+" "+insuranceType);
			            if(insuranceType.equalsIgnoreCase("renew")){
			                 renewPolicyCount++; 
			                 renewPolicyPremium=renewPolicyPremium+Premium;
			                 if(policyList.has("businessLineId")){
						         for(int i=0;i<businessLineId_arr.length;i++){
						        	 if(businessLineId_arr[i].equalsIgnoreCase(policyList.get("businessLineId").asText())){
						        		 ReNewPoliciesNode=(ObjectNode)policyList;                     
						        		 ReNewPoliciesNode.put("lob",lob_arr[i]);
						        	 }
						         }
			                 }
			                 ReNewPolicies_Array.add(ReNewPoliciesNode);
			            }else{
			            	newPolicyPremium=newPolicyPremium+Premium;
			            	newPolicyCount++;
			            	  if(policyList.has("businessLineId")){
							         for(int i=0;i<businessLineId_arr.length;i++){
							        	 if(businessLineId_arr[i].equalsIgnoreCase(policyList.get("businessLineId").asText())){
							        		 NewPoliciesNode=(ObjectNode)policyList;                     
							        		 NewPoliciesNode.put("lob",lob_arr[i]);
							        	 }
							         }
			            }
			            	  newPolicies_Array.add(NewPoliciesNode);
			            }
			        }
			                 
			        }catch(NullPointerException e){
			        	throw new Exception(e.getMessage());
			        }
			        
					}
		  		respNewCustomerNode.put("count",newPolicyCount);
		  		respNewCustomerNode.put("totalPremium",newPolicyPremium);
		  		respNewCustomerNode.put("label","New policies");
		  		respReNewCustomerNode.put("count",renewPolicyCount);
		  		respReNewCustomerNode.put("totalPremium",renewPolicyPremium);
		  		respReNewCustomerNode.put("label","Renewal policies");
		  		respCustomerNode.put("count",newPolicyCount+renewPolicyCount);
		  		respCustomerNode.put("totalPremium",newPolicyPremium+renewPolicyPremium);
		  		respCustomerNode.put("label","Total policies");
		  		customerList_Array.add(respNewCustomerNode);
            	customerList_Array.add(respReNewCustomerNode);
            	customerList_Array.add(respCustomerNode);
            	
            	
            	String newPoliciesList_String = objectMapper.writeValueAsString(newPolicies_Array);
            	String ReNewPoliciesList_String = objectMapper.writeValueAsString(ReNewPolicies_Array);
            	customerList_String = objectMapper.writeValueAsString(customerList_Array);
            	PoliciesNode.put("newPolicies",this.objectMapper.readTree(newPoliciesList_String));
            	PoliciesNode.put("reNewPolicies",this.objectMapper.readTree(ReNewPoliciesList_String));
            	PoliciesNode.put("policiesSummary",this.objectMapper.readTree(customerList_String));
            	 responseNode=PoliciesNode; 
				  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
				}
			 	  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
			    }else{
					throw new NullPointerException("getBusiness Not found in POSPSearchQueryConfig");
				}
			    }else{
				throw new NullPointerException("requestSource/userName Not Present in request");
			}
			}
			}else	{
				throw new NullPointerException("operation not present in request");
		  	}
		  }catch(NullPointerException e){
			  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), e.getMessage(),errorNode);
		  }catch(Exception e){
			  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), this.responseConfigNode.findValue("errorMessage").textValue(),errorNode);
		  }
		  return PospQueryResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(),responseNode);
	  }
	}
