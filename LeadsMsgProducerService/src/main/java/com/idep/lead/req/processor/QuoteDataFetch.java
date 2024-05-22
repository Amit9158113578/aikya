package com.idep.lead.req.processor;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.queue.util.QueueConstants;

public class QuoteDataFetch {

	CBService policyTrans = CBInstanceProvider.getBucketInstance(QueueConstants.QUOTE_BUCKET);
	Logger log = Logger.getLogger(QuoteDataFetch.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public JsonNode readCarQuoteReqData(String quoteId)
	{
		try
		{
			JsonDocument quoteDocument = policyTrans.getDocBYId(quoteId);
			if(quoteDocument!=null)
			{
				JsonNode carQuoteDataNode = objectMapper.readTree(quoteDocument.content().toString());

				ObjectNode carQuoteDataReq = (ObjectNode)carQuoteDataNode.get("carQuoteRequest");

				if(carQuoteDataReq.has("carrierVehicleInfo"))
				{
					carQuoteDataReq.remove("carrierVehicleInfo");
				}
				if(carQuoteDataReq.has("carrierRTOInfo"))
				{
					carQuoteDataReq.remove("carrierRTOInfo");
				}
				if(carQuoteDataReq.has("occupationInfo"))
				{
					carQuoteDataReq.remove("occupationInfo");
				}
				return carQuoteDataReq;
			}
			else
			{
				log.error("car quote document is not available : "+quoteId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching car quote document from DB ",e);
			return null;
		}



	}
	public JsonNode readBikeQuoteReqData(String quoteId)
	{
		try
		{
			JsonDocument quoteDocument = policyTrans.getDocBYId(quoteId);
			if(quoteDocument!=null)
			{
				JsonNode bikeQuoteDataNode = objectMapper.readTree(quoteDocument.content().toString());

				ObjectNode bikeQuoteDataReq = (ObjectNode)bikeQuoteDataNode.get("bikeQuoteRequest");
				if(bikeQuoteDataReq.has("carrierVehicleInfo"))
				{
					bikeQuoteDataReq.remove("carrierVehicleInfo");
				}
				if(bikeQuoteDataReq.has("carrierRTOInfo"))
				{
					bikeQuoteDataReq.remove("carrierRTOInfo");
				}
				if(bikeQuoteDataReq.has("occupationInfo"))
				{
					bikeQuoteDataReq.remove("occupationInfo");
				}

				return bikeQuoteDataReq;

			}
			else
			{
				log.error("bike quote document is not available : "+quoteId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching bike quote document from DB ",e);
			return null;
		}
	}
	public JsonNode readLifeQuoteReqData(String quoteId)
	{
		try
		{
			JsonDocument quoteDocument = policyTrans.getDocBYId(quoteId);
			if(quoteDocument!=null)
			{
				JsonNode lifeQuoteDataNode = objectMapper.readTree(quoteDocument.content().toString());


				return lifeQuoteDataNode;
			}
			else
			{
				log.error("life quote document is not available : "+quoteId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching life quote document from DB ",e);
			return null;
		}
	}
	public JsonNode readHealthQuoteReqData(String quoteId)
	{
		try
		{
			JsonDocument quoteDocument = policyTrans.getDocBYId(quoteId);
			if(quoteDocument!=null)
			{
				JsonNode healthQuoteDataNode = objectMapper.readTree(quoteDocument.content().toString());

				ObjectNode healthQuoteDataReq = (ObjectNode)healthQuoteDataNode.get("quoteRequest");
				return healthQuoteDataReq;
			}
			else
			{
				log.error("health quote document is not available : "+quoteId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching health quote document from DB ",e);
			return null;
		}
	}
	public JsonNode readTravelQuoteReqData(String quoteId)
	{
		try
		{
			JsonDocument quoteDocument = policyTrans.getDocBYId(quoteId);
			if(quoteDocument!=null)
			{
				JsonNode travelQuoteDataNode = objectMapper.readTree(quoteDocument.content().toString());

				ObjectNode travelQuoteDataReq = (ObjectNode)travelQuoteDataNode.get("quoteRequest");
				return travelQuoteDataReq;
			}
			else
			{
				log.error("travel quote document is not available : "+quoteId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching travel quote document from DB ",e);
			return null;
		}
 }
}
