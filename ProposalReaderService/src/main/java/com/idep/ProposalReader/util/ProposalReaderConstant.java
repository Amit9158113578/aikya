/**
 * 
 */
package com.idep.ProposalReader.util;

/**
 * @author sayli.boralkar
 *
 */
public class ProposalReaderConstant {
 
	public static final String PROPOSALID = "proposalId";
	public static final String BUSINID ="businessLineId";
	public static final String PROPOSALQUERY ="select * from PolicyTransaction where documentType = 'carProposalRequest' and proposalId = $1 and businessLineId = $2";
	public static final String PROPOSALQUERY1 ="select * from PolicyTransaction where documentType = 'bikeProposalRequest' and proposalId = $1 and businessLineId = $2";
	public static final String PROPOSALQUERY2 ="select * from PolicyTransaction where documentType = 'healthProposalRequest' and proposalId = $1 and businessLineId = $2";
	
}
