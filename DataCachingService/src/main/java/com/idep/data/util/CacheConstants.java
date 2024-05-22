package com.idep.data.util;

public class CacheConstants {
	
	public static final String SEARCHCACHEQUERIES 	= 	"SearchCacheQueries";
	public static final String QUERY_STRING 		= 	"queryString";
	public static final String SELECT_FIELDS 		= 	"selectFields";
	public static final String CAR_DEDUCTIBLE 		= 	"carDeductible";
	public static final String BIKE_DEDUCTIBLE 		= 	"bikeDeductible";
	public static final String OCCUPATION_LIST		=	"occupationList";
	public static final String BIKEMODELS			=	"bikeModels";
	public static final String CARMODELS			=	"carModels";
	public static final String HEALTHRIDERS			=	"healthRiders";
	public static final String LIFERIDERS			=	"lifeRiders";
	public static final String BIKERIDERS			=	"bikeRiders";
	public static final String CARRIDERS			=	"carRiders";
	public static final String DISEASELIST			=	"diseaseList";
	public static final String HOSPLIMITLIST		=	"hospitalizationLimitList";
	public static final String CARRIERLOGOLIST		=	"carrierLogoList";
	public static final String CARCARRIERLIST		=	"carCarrierList";
	public static final String BIKECARRIERLIST		=	"bikeCarrierList";
	public static final String LIFECARRIERLIST		=	"lifeCarrierList";
	public static final String HEALTHCARRIERLIST	=	"healthCarrierList";
	public static final String MASTERDATALIST	=	"MasterDataList";
	
	public static final String RESPONSEMESSAGES 	= 	"ResponseMessages";
	public static final String SEARCHCACHEDATACONF 	= 	"SearchCacheDataConfig";
	public static final String SEARCHCONFIG			=	"SearchConfiguration";	
	public static final String DOCUMETIDCONFIG		=	"DocumentIDConfig";
	public static final String BIKECARRIER_Q_LIST 	= 	"BikeCarrierQList";
	public static final String BIKEVARIANTSLIST 	= 	"BikeVariantsList";
	
	public static final String RTOLIST				=	"RTOList";
	
	
	public static final String BIKE_PRODUCTS_QUERY 	= 	" SELECT a.carrierName,a.insurerIndex,a.claimIndex,a.claimRatio,b.carrierId,b.productId,b.productName,b.insuranceType,b.maxAllowedVehicleAge,b.vehicleOwneredBy,"
														+ " b.minusAllowedNewVehicleIDV,b.plusAllowedNewVehicleIDV,b.minusAllowedOldVehicleIDV,b.plusAllowedOldVehicleIDV,b.riderDetails,b.policyTypeSupport,b.maxAllowedIDV, "
														+ " b.isExpiredPolicyAllowed,b.isShortFallPolicySupported,b.compulsoryDeductible"
														+ " FROM ProductData b INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
														+ " b.documentType='BikeProduct'"
														+ " and a.documentType='Carrier'"
														+ " and b.riderApplicable='Y'"
														+ " and b.isActive='Y'"
														;
	
	public static final String CARCARRIER_Q_LIST 	= 	"CarCarrierQList";
	
	public static final String CAR_PRODUCTS_QUERY 	= 	" SELECT a.carrierName,a.insurerIndex,a.claimIndex,a.claimRatio,b.carrierId,b.productId,b.productName,b.insuranceType,b.maxAllowedVehicleAge,b.vehicleOwneredBy,"
														+ " b.minusAllowedNewVehicleIDV,b.plusAllowedNewVehicleIDV,b.minusAllowedOldVehicleIDV,b.plusAllowedOldVehicleIDV,b.riderDetails,b.maxAllowedIDV,"
														+ " b.isExpiredPolicyAllowed,b.isShortFallPolicySupported,b.compulsoryDeductible,b.implicitRidersApplicable,b.policyTypeSupport"
														+ " FROM ProductData b INNER JOIN ProductData a ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) where "
														+ " b.documentType='CarProduct'"
														+ " and a.documentType='Carrier'"
														+ " and b.riderApplicable='Y'"
														+ " and b.isActive='Y'"
														;

}
