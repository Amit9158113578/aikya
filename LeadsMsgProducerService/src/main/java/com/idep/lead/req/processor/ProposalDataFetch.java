package com.idep.lead.req.processor;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class ProposalDataFetch {

	CBService policyTrans = CBInstanceProvider.getPolicyTransInstance();
	Logger log = Logger.getLogger(ProposalDataFetch.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public JsonNode readCarProposalData(String proposalId)
	{
		try
		{
			JsonDocument proposalDocument = policyTrans.getDocBYId(proposalId);
			if(proposalDocument!=null)
			{
				JsonNode carProposalDataNode = objectMapper.readTree(proposalDocument.content().toString());
				return carProposalDataNode;
			}
			else
			{
				log.error("car proposal document is not available : "+proposalId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching car proposal document from DB ",e);
			return null;
		}
	}
	public JsonNode readBikeProposalData(String proposalId)
	{
		try
		{
			JsonDocument proposalDocument = policyTrans.getDocBYId(proposalId);
			if(proposalDocument!=null)
			{
				JsonNode bikeProposalDataNode = objectMapper.readTree(proposalDocument.content().toString());
				return bikeProposalDataNode;
			}
			else
			{
				log.error("bike proposal document is not available : "+proposalId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching bike proposal document from DB ",e);
			return null;
		}
	}
	public JsonNode readLifeProposalData(String proposalId)
	{
		try
		{
			JsonDocument proposalDocument = policyTrans.getDocBYId(proposalId);
			if(proposalDocument!=null)
			{
				JsonNode lifeProposalDataNode = objectMapper.readTree(proposalDocument.content().toString());
				return lifeProposalDataNode;
			}
			else
			{
				log.error("life proposal document is not available : "+proposalId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching life proposal document from DB ",e);
			return null;
		}
	}
	public JsonNode readHealthProposalData(String proposalId)
	{
		try
		{
			JsonDocument proposalDocument = policyTrans.getDocBYId(proposalId);
			if(proposalDocument!=null)
			{
				JsonNode ProposalDataNode = objectMapper.readTree(proposalDocument.content().toString());
				return ProposalDataNode;
			}
			else
			{
				log.error("health proposal document is not available : "+proposalId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching health proposal document from DB ",e);
			return null;
		}
	}
	public JsonNode readTravelProposalData(String proposalId)
	{
		try
		{
			JsonDocument proposalDocument = policyTrans.getDocBYId(proposalId);
			if(proposalDocument!=null)
			{
				JsonNode ProposalDataNode = objectMapper.readTree(proposalDocument.content().toString());
				return ProposalDataNode;
			}
			else
			{
				log.error("travel proposal document is not available : "+proposalId);
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Exception while fetching travel proposal document from DB ",e);
			return null;
		}
	}

}
