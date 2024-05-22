package com.idep.insassessment.util;
public class ProfessionalRecomServiceConstants {
	public static final String RISK_CONFIG="select riskName,documentType,riskLabel,RiskFactorList,riskId,professionCode from ServerConfig where documentType='RiskAssessmentConfig'";
	public static final String INSURANCE_CONFIG="select insuranceName,insuranceLabel,insuranceId,documentType,professionCode,FactorList from ServerConfig where documentType='InsuranceAssessmentConfig'";
	public static final String RIDER_CONFIG="select riderName,insuranceId,IsSIApplicable,LOB,riderId,documentType,professionCode,RiderFactorList from ServerConfig where documentType='RiderAssessmentRecommendationConfig'";
	public static final String RIDER_SI_CONFIG="select riderName,LOB,sicategory,riderId,documentType,professionCode from ServerConfig where documentType='RiderSICalculationConfig'";

	
}



