package com.idep.sugarcrm.req.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugar.impl.rest.ExecutionTerminator;
import com.idep.sugar.util.SugarCRMConstants;


public class LeadMessageProcessor implements Processor
{

	static Logger log = Logger.getLogger(LeadMessageProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService transService = CBInstanceProvider.getPolicyTransInstance();
	static CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	SimpleDateFormat leaddate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	JsonNode P365IntegrationList = null;
	//SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyyhh24mmssSSS",Locale.ENGLISH);


	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception
	{

		try
		{

			/**
			 * read input request
			 */
			String request = exchange.getIn().getBody().toString();
			JsonNode reqNode = objectMapper.readTree(request);
			String deviceId=exchange.getIn().getHeader("deviceId").toString();
			JsonNode docLeadProfile = null;
			((ObjectNode)reqNode).put("deviceId", deviceId);
			log.info("Req node in LeadMessageProcessor :"+reqNode);
			P365IntegrationList = objectMapper.readTree(serverConfig.getDocBYId(SugarCRMConstants.P365_IntegrationList).content().toString());
			if(P365IntegrationList.has(deviceId)){
				JsonNode docDeviceIdNode = P365IntegrationList.get(deviceId);
				if(docDeviceIdNode.has("isActive") && docDeviceIdNode.get("isActive").asText().equalsIgnoreCase("Y")){
					if(reqNode.has("messageId")&&reqNode.get("messageId")!=null && reqNode.get("messageId").asText().length()>0)
					{
						((ObjectNode)reqNode).put("msgIdStatus", "old");
					}
					else
					{
						String mobileNumber = reqNode.get("contactInfo").get("mobileNumber").asText();
						JsonArray arr = JsonArray.create();
						arr.add(mobileNumber);
						log.info("Query :"+SugarCRMConstants.LEAD_PROFILE_QUERY + mobileNumber+"'");
						log.info("Params passed :"+arr);
						//List<JsonObject> messageIdReturned = transService.executeConfigParamArrQuery(SugarCRMConstants.LEAD_PROFILE_QUERY, arr);
						List<Map<String, Object>> messageIdReturned = transService.executeQuery(SugarCRMConstants.LEAD_PROFILE_QUERY + mobileNumber+"' order by messageId desc limit 1");
						log.info("messageIdReturned from  Query :"+messageIdReturned);
						if(messageIdReturned != null && messageIdReturned.size() > 0 && messageIdReturned.get(0).get("messageId") !=null && !messageIdReturned.get(0).get("messageId").toString().isEmpty()){
							log.info("messageIdReturned :"+ messageIdReturned.get(0).get("messageId").toString());
							((ObjectNode)reqNode).put("messageId", messageIdReturned.get(0).get("messageId").toString());
							((ObjectNode)reqNode.get("contactInfo")).put("messageId", messageIdReturned.get(0).get("messageId").toString());
							((ObjectNode)reqNode).put("leadExist", "Y");
						}
						else{
							synchronized (this) {
								JsonNode requestSourceConfig = P365IntegrationList.get(reqNode.get("requestSource").asText());
								long seq = 0;

								try
								{
									seq = serverConfig.updateDBSequence(requestSourceConfig.get("leadSeqDoc").asText());
									//seq = serverConfig.updateDBSequence("SEQLEADREQ");

									JsonNode seqObject = P365IntegrationList.get(deviceId);
									long seq1 = seqObject.get("leadCount").longValue();
									((ObjectNode) seqObject).put("leadCount", (seq1 + 1));
									((ObjectNode)P365IntegrationList).set(deviceId, seqObject);
									Map<String, String> mapData = objectMapper.convertValue(P365IntegrationList, Map.class) ;
									JsonObject deviceIdNode  = JsonObject.from(mapData);

									String docStatus = serverConfig.replaceDocument(SugarCRMConstants.P365_IntegrationList, deviceIdNode);
									if (docStatus.equals("doc_replaced"))
									{
										log.info("leadCount updated in P365IntegrationList :" + (seq1 + 1));

									}
									else
									{
										seq1 = -1;
										log.error("unable to update document P365IntegrationList");
									}

									if(seq==-1)
									{
										log.error("failed to update lead sequence hence retrying...1");
										Thread.sleep(3000);
										seq = serverConfig.updateDBSequence(requestSourceConfig.get("leadSeqDoc").asText());
									}
									if(seq==-1)
									{
										log.error("failed to update lead sequence hence retrying...2");
										Thread.sleep(3000);
										seq = serverConfig.updateDBSequence(requestSourceConfig.get("leadSeqDoc").asText());
									}
								}
								catch(InterruptedException e)
								{
									log.error("failed to update lead sequence hence retrying...",e);
									seq = serverConfig.updateDBSequence(requestSourceConfig.get("leadSeqDoc").asText());
								}
								if(seq==-1)
								{
									log.error("unable to update LEAD sequence , hence terminated");
									throw new ExecutionTerminator();
								}
								//String messageId = "LEADMSGID"+seq;
								String messageId = requestSourceConfig.get("leadSeqFormat").asText()+seq;
								log.info("Message Id Created: "+messageId);
								/**
								 * getting messageId fields from UI in contactInfo node hence replacing with generated message id
								 */


								docDeviceIdNode.get("leadCount").asLong();
								if( !reqNode.has("isChat")){
									((ObjectNode)reqNode.get("contactInfo")).put("messageId", messageId);
								}
								((ObjectNode)reqNode).put("messageId", messageId);
								((ObjectNode)reqNode.get("contactInfo")).put("messageId", messageId);
							}
						}	

					}
					//adding messageId in QuoteId Docs 
					if(reqNode.has("QUOTE_ID") && reqNode.get("QUOTE_ID") != null ){
						log.info("node coming :"+reqNode); 
						String QUOTE_ID = reqNode.get("QUOTE_ID").asText();
						if(quoteData.getDocBYId(QUOTE_ID) != null){
							String quoteIdDoc= quoteData.getDocBYId(QUOTE_ID).content().toString();
							JsonNode quoteIdDocNode = objectMapper.readTree(quoteIdDoc);
							((ObjectNode)quoteIdDocNode).put("messageId", reqNode.get("messageId"));
							if(quoteIdDocNode.has("bikeQuoteRequest")){
								log.info("Storing messageId in BikeQuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("bikeQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("carQuoteRequest")){
								log.info("Storing messageId in CarQuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("carQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("lifeQuoteRequest") ){
								log.info("Storing messageId in LifeQuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("lifeQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("quoteRequest")){
								log.info("Storing messageId in QuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("quoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("healthQuoteRequest")){
								log.info("Storing messageId in HealthQuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("healthQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("travelQuoteRequest")){
								log.info("Storing messageId in TravelQuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("travelQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							if(quoteIdDocNode.has("commonInfo")){
								log.info("Storing for commonInfo in QuoteRequest node !");
								((ObjectNode)quoteIdDocNode.get("carQuoteRequest")).put("messageId", reqNode.get("messageId"));
							}
							JsonObject documentContent = JsonObject.fromJson(quoteIdDocNode.toString());
							log.info("documentContent :"+documentContent); 
							String doc_status = quoteData.replaceDocument(QUOTE_ID, documentContent);
							log.info("docId :"+QUOTE_ID+" doc_status :"+doc_status);
						}
						else{
							log.info("QuoteId Doc Not created!");
						}
					}	
					else{
						log.info("Request does not have QuoteId !");
					}

				}
				else{
					log.error("Given Affiliate is not active or isActive flag is not present.");
					throw new ExecutionTerminator();
				}
			}
			else{
				log.error("Given Affiliate is not valid.");
			}
			((ObjectNode)reqNode).put("leadReqDate", leaddate.format(new Date()));


			if(reqNode.has("isProfessionalJourney") && reqNode.get("isProfessionalJourney").asBoolean()){
				JsonNode quoteParam = objectMapper.createObjectNode();
				((ObjectNode) quoteParam).put("quoteType", "ProfessionalJourney");
				((ObjectNode) reqNode).put(SugarCRMConstants.QUOTE_PARAM,quoteParam);
			}
			if(reqNode.has("calcQuote"))
			{
				if(reqNode.get("calcQuote").asBoolean())
				{
					exchange.getIn().setHeader("createQuoteDelay","Y");
				}
				else
				{
					exchange.getIn().setHeader("createQuoteDelay","N");
				}
			}
			else
			{
				exchange.getIn().setHeader("createQuoteDelay","N");

			}
			log.info("Final Lead Req :"+reqNode);
			//validate for RAMP create quote calculation in the time of create lead
			exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
			exchange.getIn().setHeader("leadSourceFlag",reqNode.get("requestSource"));
			if(!reqNode.get("requestSource").asText().equalsIgnoreCase("renewal")){
				exchange.getIn().setHeader("iMATRenewalFlag","Y");
			}
			else{
				exchange.getIn().setHeader("iMATRenewalFlag","N");
			}

		}
		catch(Exception e)
		{
			log.error("Error while preparing a message for Lead request Q : LeadsQ ",e);
		}
	}

}