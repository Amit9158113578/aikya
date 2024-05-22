/**
 * 
 */
package com.idep.policydownload.req.processor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policydownload.util.PolicyDownloadViewConstants;
import com.idep.services.impl.ECMSManagerAPI;
import com.idep.user.profile.impl.UserProfileServices;

/**
 * @author vipin.patil
 *
 *         Apr 27, 2017
 */
public class PolicyDocDownloaderReqProcessor implements Processor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PolicyDocDownloaderReqProcessor.class.getName());
	CBService service = null;
	JsonNode responseConfigNode = null;
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	JsonNode errorNode = null;
	UserProfileServices profileServices = new UserProfileServices();

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub

		try {
			/*
			 * spool strategy changed to allow large size PDF stream in response -1 used to
			 * disable overflow to disk, use RAM instead
			 */
			exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1);
			if (this.service == null) {
				this.service = CBInstanceProvider.getServerConfigInstance();
				this.responseConfigNode = this.objectMapper.readTree(
						this.service.getDocBYId(PolicyDownloadViewConstants.RESPONSE_MSG).content().toString());
			}

			String docDownloadReq = exchange.getIn().getBody(String.class);
			JsonNode docDownloadReqNode = this.objectMapper.readTree(docDownloadReq);
			String policySecretKey = docDownloadReqNode.get("pKey").textValue();
			JsonNode userProfileDetailsNode = null;

			// get user profile from database

			if (docDownloadReqNode.has("uKey"))
				userProfileDetailsNode = profileServices.getUserProfileByUkey(docDownloadReqNode.get("uKey").asText());

			if (docDownloadReqNode.has("mobile")) {
				userProfileDetailsNode = objectMapper.readTree(((JsonObject) this.transService
						.getDocBYId("UserProfile-" + docDownloadReqNode.get("mobile").asText()).content())
						.toString());
			}

			if (userProfileDetailsNode != null) {

				String base64string = null;
				JsonObject policyDocNode = JsonObject.create();
				JsonNode policyDoc = null;

				JsonNode policyNode = profileServices
						.getPolicyRecordByPkey(userProfileDetailsNode.findValue("mobile").asText(), policySecretKey);

				if (policyNode.has("DownloadLink")) {
					String filePath = policyNode.get("DownloadLink").asText();
					log.info("DownloadLink : " + filePath);
					String contentRepoType = policyNode.get("contentRepoType").asText();
					if (contentRepoType.equals("FileSystem")) {
						Path path = Paths.get(filePath);
						byte[] data = Files.readAllBytes(path);
						base64string = Base64.encodeBase64String(data);
						/**
						 * pdfString data requires to understand UI side in which format string has to
						 * convert.
						 */
						String pdfString = "data:application/pdf;base64,";
						base64string = pdfString + base64string;
						policyDocNode.put("policyNo", policyNode.get("policyNo").asText());
						policyDocNode.put("policyDocData", base64string);
						policyDocNode.put("performDocumentAction", "DownloadDocument");
						String policyDocString = policyDocNode.toString();
						policyDoc = objectMapper.readTree(policyDocString);
					}
					/**
					 * Code added to check to view the document using URL.
					 */
					else if (contentRepoType.equals("ContentRepository")) {
						log.info("inside ContentRepository ");
						policyDocNode.put("policyNo", policyNode.get("policyNo").asText());
						policyDocNode.put("performDocumentAction", "RedirectDocument");
						String fileURL = policyNode.get("DownloadLink").asText();
						if (policyNode.has("contentRepoName")) {
							if (policyNode.get("contentRepoName").asText().equals("Alfresco")) {
								ECMSManagerAPI ecmsManagerAPI = new ECMSManagerAPI();
								String authTicket = ecmsManagerAPI.generateAuthTicket();
								fileURL = fileURL.concat(authTicket);
							}
						}
						policyDocNode.put("DownloadLink", fileURL);
						policyDoc = objectMapper.readTree(policyDocNode.toString());
					} else {
						policyDocNode.put("policyNo", policyNode.get("policyNo").asText());
						policyDocNode.put("DownloadLink", policyNode.get("DownloadLink").asText());
						policyDoc = objectMapper.readTree(policyDocNode.toString());
					}

					ObjectNode obj = this.objectMapper.createObjectNode();
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_CODE,
							this.responseConfigNode.get(PolicyDownloadViewConstants.SUCC_CONFIG_CODE).intValue());
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_MSG,
							this.responseConfigNode.get(PolicyDownloadViewConstants.SUCC_CONFIG_MSG).textValue());
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_DATA, policyDoc);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));

				} else {
					ObjectNode obj = this.objectMapper.createObjectNode();
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_CODE,
							this.responseConfigNode.get(PolicyDownloadViewConstants.ERROR_CONFIG_CODE).intValue());
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_MSG,
							this.responseConfigNode.get(PolicyDownloadViewConstants.ERROR_CONFIG_MSG).textValue());
					obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_DATA, errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
				}
			}

		}

		catch (Exception e) {
			this.log.error("PolicyDocDownloaderReqProcessor Exception : ", e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_CODE,
					this.responseConfigNode.get(PolicyDownloadViewConstants.ERROR_CONFIG_CODE).intValue());
			obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_MSG,
					this.responseConfigNode.get(PolicyDownloadViewConstants.ERROR_CONFIG_MSG).textValue());
			obj.put(PolicyDownloadViewConstants.PROPOSAL_RES_DATA, errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));

		}

	}

}
