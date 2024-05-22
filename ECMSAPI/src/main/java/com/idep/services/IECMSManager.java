package com.idep.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author vipin.patil
 *
 * May 6, 2017
 */
public interface IECMSManager {
	/**
	 * 
	 * @param repositoryPath
	 * @return
	 * upload the document using file location.
	 * @throws IOException 
	 * @throws HttpException 
	 */
	String uploadPolicyDocument(JsonNode contentMgmtConfig ,String fileName, JsonObject metaData) throws HttpException, IOException;
	String formDownloadPolicyDocURL(String documentId);
	String generateAuthTicket() throws HttpException, IOException;
	int updateMetaDataProp(String documentId, JsonObject metaData);
}
