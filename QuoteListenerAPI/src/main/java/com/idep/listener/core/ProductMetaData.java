package com.idep.listener.core;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.idep.listener.services.IProductaMetaData;
import com.idep.listener.utils.QuoteListenerConstants;

public class ProductMetaData implements IProductaMetaData {
	Logger log = Logger.getLogger(ProductMetaData.class.getName());
	@Override
	public JsonObject getRTODetails(String rtoCode, String carrierId, String businessLineId) {
		String documentId = rtoCode.trim()+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId+QuoteListenerConstants.DOCIDVALUESEPERATOR+businessLineId;
		//log.info("Document Id for RTO Details :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getVehicleDetails(String variantId, String carrierId) {
		String documentId = variantId.trim()+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId;
		//log.info("Document Id for Vehicle Details :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getOccupationDetails(String occupationId,
			String carrierId) {
		String documentId = QuoteListenerConstants.OCCUPATIONMAPPING_TAG+QuoteListenerConstants.DOCIDVALUESEPERATOR+occupationId.trim()+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId;
		//log.info("Document Id to get OccupationDetails Details :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getBlockedRTODetails(String rtoCode) {
		String documentId = QuoteListenerConstants.BLOCKEDRTODOCTYPE+QuoteListenerConstants.DOCIDVALUESEPERATOR+rtoCode.trim();
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getValidationDetails(String carrierId, String productId) {
		String documentId = QuoteListenerConstants.BIKEQUOTEVALIDATIONDOCTYPE+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId+QuoteListenerConstants.DOCIDVALUESEPERATOR+productId;
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}
	@Override
	public JsonObject getCarValidationDetails(String carrierId, String productId) {
		String documentId = QuoteListenerConstants.CARQUOTEVALIDATIONDOCTYPE+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId+QuoteListenerConstants.DOCIDVALUESEPERATOR+productId;
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getPreInsurerMapping(String carrierId, String preInsurerCarrierId) {
		// TODO Auto-generated method stub
		String documentId = QuoteListenerConstants.UI_CARINSURERMAPPING+QuoteListenerConstants.DOCIDVALUESEPERATOR+preInsurerCarrierId+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId;
		//log.info("CarInsurerMapping Doc :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getNomineeRelMapping(String carrierId, String nominationRelationId) {
		// TODO Auto-generated method stub
		String documentId = QuoteListenerConstants.UI_CARRELATIONSHIPMAPPING+QuoteListenerConstants.DOCIDVALUESEPERATOR+nominationRelationId+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId;
		//log.info("CarRelationshipMapping Doc :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getDistrictMapping(String carrierId, String city) {
		// TODO Auto-generated method stub
		String documentId = QuoteListenerConstants.UI_DISTRICTMAPPING+QuoteListenerConstants.DOCIDVALUESEPERATOR+city+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId;
	//	log.info("DistrictMapping Doc :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	@Override
	public JsonObject getExshowroomPriceDetails(String carrierId,
			String businessLineId, String modelCode, String stateGroupId) {
		// TODO Auto-generated method stub
		String documentId = QuoteListenerConstants.UI_EXSHOWROOMPRICE+QuoteListenerConstants.DOCIDVALUESEPERATOR+carrierId+QuoteListenerConstants.DOCIDVALUESEPERATOR
				+businessLineId+QuoteListenerConstants.DOCIDVALUESEPERATOR+modelCode+QuoteListenerConstants.DOCIDVALUESEPERATOR+stateGroupId;
		//log.info("ExshowroomPriceMapping Doc :"+documentId);
		//System.out.println("ExshowroomPriceMapping Doc :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}


	@Override
	public JsonObject getRTODetailsForZone(String RtoCode) {
		// TODO Auto-generated method stub
		String documentId=QuoteListenerConstants.DB_RTODETAILS+QuoteListenerConstants.DOCIDVALUESEPERATOR+RtoCode;
		//log.info("RTO Details In RegistrationPlace :"+documentId);
		if(CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId) != null)
		{
			return CouchbaseAccessor.getServerConfigInstance().getDocBYId(documentId).content();
		}
		return null;
	}

	

}
