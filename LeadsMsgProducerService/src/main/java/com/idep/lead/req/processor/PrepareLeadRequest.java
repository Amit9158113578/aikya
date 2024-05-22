package com.idep.lead.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class PrepareLeadRequest implements Processor
{

	Logger log = Logger.getLogger(PrepareLeadRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode leadConfigNode = null;
	QuoteDataFetch quoteData = new QuoteDataFetch();
	ProposalDataFetch proposalData = new ProposalDataFetch();

	@Override
	public void process(Exchange exchange) {
		try
		{
			if(leadConfigNode == null)
			{
				leadConfigNode = objectMapper.readTree(serverConfig.getDocBYId("LeadStagesConfig").content().toString());
			}
			String request = exchange.getIn().getBody().toString();
			JsonNode reqNode = objectMapper.readTree(request);
			JsonNode headerReqNode  = reqNode.get("header");
			JsonNode bodyReqNode = reqNode.get("body");
			log.info("PreparedLeadRequest : REQ NODE :"+ reqNode);
			ObjectNode leadDataNode = objectMapper.createObjectNode();
			JsonNode leadConfig = leadConfigNode.get("servicesList");	
			String transaction= null;
			if(headerReqNode.has("transactionName"))
			{
				/**
				 * check if transaction name is configured in lead configuration document
				 */
				if(leadConfig.has(headerReqNode.get("transactionName").asText()))
				{
					transaction=headerReqNode.get("transactionName").asText();
					log.info("This is in Transaction Name");

					if(headerReqNode.has("messageId"))
					{

						if(headerReqNode.get("messageId")!=null)
						{

							/**
							 * call function to get the desired object based on transaction name
							 */
							JsonNode updatedReqNode = prepareLeadData(headerReqNode,bodyReqNode,leadConfig);
							if(updatedReqNode!=null)
							{
								
							((ObjectNode) updatedReqNode).put("transaction",transaction);
							/**
							 * send lead data node to LeadsQ to process
							 */
							leadDataNode.putAll((ObjectNode)updatedReqNode);

								if(updatedReqNode.has("isLeadRequest") && updatedReqNode.get("isLeadRequest").asText().equalsIgnoreCase("N")){
									log.info("False Request For Create Leads");
									exchange.getIn().setHeader("isLeadRequest", "N");
								}
								else
								{
									log.info("Valid Request For Create Leads");
									exchange.getIn().setHeader("isLeadRequest", "Y");	
								}
								leadDataNode.put("LastVisitedQuoteId",updatedReqNode.findValue("QUOTE_ID"));
								leadDataNode.put("messageId",headerReqNode.get("messageId"));
								leadDataNode.put("msgIdStatus","old");

								log.info("final lead data set : "+leadDataNode);
								exchange.getIn().setBody(objectMapper.writeValueAsString(leadDataNode));
							}
							else
							{
								exchange.getIn().setHeader("isLeadRequest", "N");
							}
						}
						else
						{
							exchange.getIn().setHeader("isLeadRequest", "N");
						}
					}
					else
					{
						exchange.getIn().setHeader("isLeadRequest", "N");
					}
				}
				else
				{
					exchange.getIn().setHeader("isLeadRequest", "N");
				}
			}
			else
			{
				log.error("transactionName is missing in header request");
				exchange.getIn().setHeader("isLeadRequest", "N"); 
			}
		}
		catch(Exception e)
		{
			log.error("Exception while preparing lead request based on transaction name : ",e);
			exchange.getIn().setHeader("isLeadRequest", "N"); 
		}
	}

	public JsonNode prepareLeadData(JsonNode headerNode, JsonNode bodyNode, JsonNode leadConfig) throws Exception
	{
		/**
		 * write transaction based logic here
		 */
		log.info("Transaction Inside preapareLead data "+headerNode.get("transactionName").asText());
		if(headerNode.get("transactionName").asText().equalsIgnoreCase("getCarQuote") ||
				headerNode.get("transactionName").asText().equalsIgnoreCase("getHealthQuote") ||
				headerNode.get("transactionName").asText().equalsIgnoreCase("getBikeQuote") ||
				headerNode.get("transactionName").asText().equalsIgnoreCase("getLifeQuote") ||
				headerNode.get("transactionName").asText().equalsIgnoreCase("getTravelQuote") ||
				headerNode.get("transactionName").asText().equalsIgnoreCase("getCriticalIllnessQuote"))
		{
			return bodyNode;			
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("calculateBikeIDVQuote"))
		{
			return quoteData.readBikeQuoteReqData(bodyNode.get("QUOTE_ID").asText());

		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("calculateCarIDVQuote"))
		{	
			return quoteData.readCarQuoteReqData(bodyNode.get("QUOTE_ID").asText());
		}
		/*
		 * Added for QUOTEID updation in the LMS`
		 */
		/*			 else if(headerNode.get("transactionName").asText().equalsIgnoreCase("getCarQuoteResult"))
		{
			ObjectNode carQuoteResultNode = objectMapper.createObjectNode();
			log.info("Quote ID in getCarQuoteResult: "+bodyNode.get("QUOTE_ID").asText());
			carQuoteResultNode.put("isLeadRequest", "N");

			//log.info("ENC_QUOTE_ID : "+bodyNode.get("ENC_QUOTE_ID").asText());
			//carQuoteResultNode.put("ENC_QUOTE_ID",bodyNode.get("ENC_QUOTE_ID").asText());


			 // Commented because of we are not using quoteID


			//carQuoteResultNode.putAll((ObjectNode)quoteData.readCarQuoteReqData(bodyNode.get("QUOTE_ID").asText()));
			//carQuoteResultNode.put("LastVisitedQuoteId",bodyNode.get("QUOTE_ID").asText());
			if(headerNode.has("messageId"))
			{
				carQuoteResultNode.put("messageId",headerNode.get("messageId").asText());
			}else
			{
				log.info("messageId is not present in header");
			}
			return carQuoteResultNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("getLifeQuoteResult"))
		{
			ObjectNode lifeQuoteResultNode = objectMapper.createObjectNode();
			log.info("Quote ID in getLifeQuoteResult: "+bodyNode.get("QUOTE_ID").asText());
			lifeQuoteResultNode.put("isLeadRequest", "N");
			//log.info("ENC_QUOTE_ID : "+bodyNode.get("ENC_QUOTE_ID").asText());
			//lifeQuoteResultNode.put("ENC_QUOTE_ID",bodyNode.get("ENC_QUOTE_ID").asText());

			 // Commented because of we are not using quoteID

			//lifeQuoteResultNode.putAll((ObjectNode)quoteData.readLifeQuoteReqData(bodyNode.get("QUOTE_ID").asText()));
			//lifeQuoteResultNode.put("LastVisitedQuoteId",bodyNode.get("QUOTE_ID").asText());
			if(headerNode.has("messageId"))
			{
				lifeQuoteResultNode.put("messageId",headerNode.get("messageId").asText());
			}else
			{
				log.info("messageId is not present in header");
			}
			return lifeQuoteResultNode;
		}

		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("getBikeQuoteResult"))
		{
			ObjectNode bikeQuoteResultNode = objectMapper.createObjectNode();
			log.info("Quote ID in getBikeQuoteResult: "+bodyNode.get("QUOTE_ID").asText());
			bikeQuoteResultNode.put("isLeadRequest", "N");
			// log.info("ENC_QUOTE_ID : "+bodyNode.get("ENC_QUOTE_ID").asText());
			//bikeQuoteResultNode.put("ENC_QUOTE_ID",bodyNode.get("ENC_QUOTE_ID").asText());

			 // Commented because of we are not using quoteID

			//bikeQuoteResultNode.putAll((ObjectNode)quoteData.readBikeQuoteReqData(bodyNode.get("QUOTE_ID").asText()));
			//bikeQuoteResultNode.put("LastVisitedQuoteId",bodyNode.get("QUOTE_ID").asText());
			if(headerNode.has("messageId"))
			{
				bikeQuoteResultNode.put("messageId",headerNode.get("messageId").asText());
			}else
			{
				log.info("messageId is not present in header");
			}
			bikeQuoteResultNode.put("messageId",headerNode.get("messageId").asText());
			return bikeQuoteResultNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("getHealthQuoteResult"))
		{
			ObjectNode healthQuoteResultNode = objectMapper.createObjectNode();
			log.info("Quote ID in getHealthQuoteResult: "+bodyNode.get("QUOTE_ID").asText());
			healthQuoteResultNode.put("isLeadRequest", "N");
			//log.info("ENC_QUOTE_ID : "+bodyNode.get("ENC_QUOTE_ID").asText());
			//healthQuoteResultNode.put("ENC_QUOTE_ID",bodyNode.get("ENC_QUOTE_ID").asText());

			 // Commented because of we are not using quoteID

			//healthQuoteResultNode.put("QUOTE_ID",bodyNode.get("QUOTE_ID").asText());
			//healthQuoteResultNode.put("LastVisitedQuoteId",bodyNode.get("QUOTE_ID").asText());
			//healthQuoteResultNode.putAll((ObjectNode)quoteData.readHealthQuoteReqData(bodyNode.get("QUOTE_ID").asText()));

			if(headerNode.has("messageId"))
			{
				healthQuoteResultNode.put("messageId",headerNode.get("messageId").asText());
			}else
			{
				log.info("messageId is not present in header");
			}
			return healthQuoteResultNode;
		}
		 */
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("calculateCarProductQuote"))
		{
			ObjectNode carQuoteResultNode = objectMapper.createObjectNode();
			carQuoteResultNode.putAll((ObjectNode)quoteData.readCarQuoteReqData(bodyNode.get("QUOTE_ID").asText()));
			return carQuoteResultNode;
		}

		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("submitCarProposal"))
		{
			log.info("submitCarProposal :Payment Link");
			ObjectNode carProposalNode = objectMapper.createObjectNode();
			carProposalNode.put("carProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
			carProposalNode.putAll((ObjectNode) quoteData.readCarQuoteReqData(bodyNode.findValue("QUOTE_ID").asText()));
			//log.info("value of carproposalnode from preapare lead"+carProposalNode);
			return carProposalNode;

		}

		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("submitBikeProposal"))
		{
			log.info("submitBikeProposal :Payment Link");
			ObjectNode bikeProposalNode = objectMapper.createObjectNode();
			bikeProposalNode.put("bikeProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
			bikeProposalNode.putAll((ObjectNode) quoteData.readBikeQuoteReqData(bodyNode.findValue("QUOTE_ID").asText()));
			//log.info("value of carproposalnode from preapare lead"+bikeProposalNode);
			return bikeProposalNode;
		}

		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("submitHealthProposal"))
		{
			ObjectNode healthProposalNode = objectMapper.createObjectNode();
			healthProposalNode.put("healthProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());

			healthProposalNode.putAll((ObjectNode) quoteData.readHealthQuoteReqData(bodyNode.findValue("QUOTE_ID").asText()));
			return healthProposalNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("submitTravelProposal"))
		{
			ObjectNode travelProposalNode = objectMapper.createObjectNode();
			travelProposalNode.put("travelProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());

			travelProposalNode.putAll((ObjectNode) quoteData.readTravelQuoteReqData(bodyNode.findValue("QUOTE_ID").asText()));
			return travelProposalNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("paymentService"))
		{
			log.info("paymentService Called");
			ObjectNode paymentProposalNode = objectMapper.createObjectNode();

			if(bodyNode.findValue("proposalId")!=null)
			{
				log.info("Prepared Lead ::proposalId"+bodyNode.findValue("proposalId"));
				paymentProposalNode.put("proposalId", bodyNode.findValue("proposalId").asText());
				paymentProposalNode.put("commonProposalId", bodyNode.findValue("proposalId").asText());
				if(bodyNode.get("businessLineId").asInt() == 1){
					paymentProposalNode.put("commonProposalId", "");
				}

				if(bodyNode.has("businessLineId"))
				{
					if(bodyNode.get("businessLineId").asInt()==2)
					{
						JsonNode proposalDataNode = proposalData.readBikeProposalData(bodyNode.findValue("proposalId").asText());
						paymentProposalNode.put("bikeProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
						paymentProposalNode.putAll((ObjectNode) quoteData.readBikeQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

					}
					if(bodyNode.get("businessLineId").asInt()==3)
					{
						JsonNode proposalDataNode = proposalData.readCarProposalData(bodyNode.findValue("proposalId").asText());
						paymentProposalNode.put("carProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
						paymentProposalNode.putAll((ObjectNode) quoteData.readCarQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

					}
					if(bodyNode.get("businessLineId").asInt()==4)
					{
						JsonNode proposalDataNode = proposalData.readHealthProposalData(bodyNode.findValue("proposalId").asText());
						paymentProposalNode.put("healthProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
						paymentProposalNode.putAll((ObjectNode) quoteData.readHealthQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

					}
					if(bodyNode.get("businessLineId").asInt()==5)
					{
						JsonNode proposalDataNode = proposalData.readHealthProposalData(bodyNode.findValue("proposalId").asText());
						paymentProposalNode.put("travelProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("proposalStage").asText());
						paymentProposalNode.putAll((ObjectNode) quoteData.readHealthQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

					}
					if(bodyNode.get("businessLineId").asInt() == 6){
						paymentProposalNode.put("commonProposalId", "");
					}
					log.info("payment service node : "+paymentProposalNode);
					return paymentProposalNode;
				}
				else
				{
					log.error("businessLineId is missing in payment request service");
					return null;
				}

			}
			else
			{
				log.error("proposal id is missing in payment request service");
				return null;
			}


		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("createCarPolicy"))
		{
			ObjectNode createCarPolicyNode = objectMapper.createObjectNode();

			if(bodyNode.findValue("proposalId")!=null)
			{
				createCarPolicyNode.put("proposalId", bodyNode.findValue("proposalId").asText());
				createCarPolicyNode.put("transactionName", "createCarPolicy");
				/**
				 * read proposal document from DB
				 */
				JsonNode proposalDataNode = proposalData.readCarProposalData(bodyNode.findValue("proposalId").asText());
				createCarPolicyNode.putAll((ObjectNode)proposalDataNode);
				/**
				 * read QUOTE document from DB
				 */
				createCarPolicyNode.putAll((ObjectNode) quoteData.readCarQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

				if(bodyNode.findValue("transactionStatusCode")!=null)
				{
					if(bodyNode.findValue("transactionStatusCode").asInt()==1)
					{
						createCarPolicyNode.put("carProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("paySuccess").asText());
						createCarPolicyNode.put("status", "convert");
					}
					else if(bodyNode.findValue("transactionStatusCode").asInt()==0)
					{
						createCarPolicyNode.put("carProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("payFailure").asText());
					}
					//log.info("create policy node : "+createCarPolicyNode);
					return createCarPolicyNode;
				}
				else
				{
					log.error("transactionStatusCode is missing in create car policy request : "+bodyNode);
					return null;
				}
			}
			else
			{
				log.error("proposal id is missing in create Car Policy service");
				return null;
			}

			//return createCarPolicyNode;
			/* ObjectNode healthProposalNode = objectMapper.createObjectNode();
			  healthProposalNode.put("healthProposalStatus", "proposalSubmitted");
			  healthProposalNode.putAll((ObjectNode) quoteData.readHealthQuoteReqData(bodyNode.findValue("QUOTE_ID").asText()));
			 return healthProposalNode;*/
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("createBikePolicy"))
		{
			ObjectNode createBikePolicyNode = objectMapper.createObjectNode();

			if(bodyNode.findValue("proposalId")!=null)
			{
				createBikePolicyNode.put("proposalId", bodyNode.findValue("proposalId").asText());
				createBikePolicyNode.put("transactionName", "createBikePolicy");
				/**
				 * read proposal document from DB
				 */
				JsonNode proposalDataNode = proposalData.readBikeProposalData(bodyNode.findValue("proposalId").asText());
				createBikePolicyNode.putAll((ObjectNode)proposalDataNode);
				/**
				 * read QUOTE document from DB
				 */
				createBikePolicyNode.putAll((ObjectNode) quoteData.readBikeQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

				if(bodyNode.findValue("transactionStatusCode")!=null)
				{
					if(bodyNode.findValue("transactionStatusCode").asInt()==1)
					{
						createBikePolicyNode.put("bikeProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("paySuccess").asText());
						createBikePolicyNode.put("status", "convert");
					}
					else if(bodyNode.findValue("transactionStatusCode").asInt()==0)
					{
						createBikePolicyNode.put("bikeProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("payFailure").asText());
					}
					//log.info("create bike policy node : "+createBikePolicyNode);
					return createBikePolicyNode;
				}
				else
				{
					log.error("transactionStatusCode is missing in create bike policy request : "+bodyNode);
					return null;
				}
			}

			return createBikePolicyNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("createHealthPolicy"))
		{
			/*ObjectNode createHealthPolicyNode = objectMapper.createObjectNode();
			return createHealthPolicyNode;*/


			ObjectNode createHealthPolicyNode = objectMapper.createObjectNode();

			if(bodyNode.findValue("proposalId")!=null)
			{
				createHealthPolicyNode.put("proposalId", bodyNode.findValue("proposalId").asText());
				createHealthPolicyNode.put("transactionName","createHealthPolicy");
				createHealthPolicyNode.put("quoteType",4);
				/**
				 * read proposal document from DB
				 */
				JsonNode proposalDataNode = proposalData.readHealthProposalData(bodyNode.findValue("proposalId").asText());
				createHealthPolicyNode.putAll((ObjectNode)proposalDataNode);
				/**
				 * read QUOTE document from DB
				 */
				createHealthPolicyNode.putAll((ObjectNode) quoteData.readHealthQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

				if(bodyNode.findValue("transactionStatusCode")!=null)
				{
					if(bodyNode.findValue("transactionStatusCode").asInt()==1)
					{
						createHealthPolicyNode.put("healthProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("paySuccess").asText());
						createHealthPolicyNode.put("status", "convert");
					}
					else if(bodyNode.findValue("transactionStatusCode").asInt()==0)
					{
						createHealthPolicyNode.put("healthProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("payFailure").asText());
					}
					//log.info("create health policy node : "+createHealthPolicyNode);
					return createHealthPolicyNode;
				}
				else
				{
					log.error("transactionStatusCode is missing in create health policy request : "+bodyNode);
					return null;
				}
			}

			return createHealthPolicyNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("createTravelPolicy"))
		{
		
			ObjectNode createTravelPolicyNode = objectMapper.createObjectNode();

			if(bodyNode.findValue("proposalId")!=null)
			{
				createTravelPolicyNode.put("proposalId", bodyNode.findValue("proposalId").asText());
				createTravelPolicyNode.put("transactionName","createTravelPolicy");
				createTravelPolicyNode.put("quoteType",5);
				/**
				 * read proposal document from DB
				 */
				JsonNode proposalDataNode = proposalData.readTravelProposalData(bodyNode.findValue("proposalId").asText());
				createTravelPolicyNode.putAll((ObjectNode)proposalDataNode);
				/**
				 * read QUOTE document from DB
				 */
				createTravelPolicyNode.putAll((ObjectNode) quoteData.readTravelQuoteReqData(proposalDataNode.findValue("QUOTE_ID").asText()));

				if(bodyNode.findValue("transactionStatusCode")!=null)
				{
					if(bodyNode.findValue("transactionStatusCode").asInt()==1)
					{
						createTravelPolicyNode.put("travelProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("paySuccess").asText());
						createTravelPolicyNode.put("status", "convert");
					}
					else if(bodyNode.findValue("transactionStatusCode").asInt()==0)
					{
						createTravelPolicyNode.put("travelProposalStatus", leadConfig.get(headerNode.get("transactionName").asText()).get("payFailure").asText());
					}
					
					return createTravelPolicyNode;
				}
				else
				{
					log.error("transactionStatusCode is missing in create travel policy request : "+bodyNode);
					return null;
				}
			}

			return createTravelPolicyNode;
		}
		else if(headerNode.get("transactionName").asText().equalsIgnoreCase("BreakInInspectionStatus"))
		{
			ObjectNode BreakInInspectionStatus = objectMapper.createObjectNode();
			BreakInInspectionStatus.put("BreakInStatus",bodyNode.findValue("BreakInStatus").asText());

			//log.info("mail through break in policy"+BreakInInspectionStatus);
			BreakInInspectionStatus.putAll((ObjectNode)bodyNode);
			//log.info("mail through break in policy added body"+BreakInInspectionStatus);
			return BreakInInspectionStatus;

		}
		else
		{
			return null;
		}

	}

}
