/**
 * 
 */
package com.idep.insurancerecommender.util;

/**
 * @author deepak.surapaneni
 *
 */
public class InsuranceRecConstants {
	public static final String RISK_CONFIG="select riskName,documentType,RiskFactorList,riskId from ServerConfig where documentType='RiskAssessmentConfig'";
	public static final String LOB_CONFIG="select RiskDetails,insuranceType,insuranceId from ServerConfig where documentType='LOBAssessmentConfig'";
	public static final String RES_CONFIG="select responseConfig from ServerConfig where documentType='insuranceRecResConfig'";
}
