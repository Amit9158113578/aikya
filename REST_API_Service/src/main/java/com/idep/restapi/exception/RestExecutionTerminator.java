package com.idep.restapi.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.idep.restapi.utils.RestAPIConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class RestExecutionTerminator implements Processor {
  public static final long serialVersionUID = -4192267514550615282L;
  
  String excpmsg;
  
  public RestExecutionTerminator() {}
  
  public RestExecutionTerminator(String response) {
    this.excpmsg = response;
  }
  
  public String toString() {
    return "Exception MSG : " + this.excpmsg;
  }
  
  public void process(Exchange exchange) throws Exception {
    String inputReq = (String)exchange.getIn().getBody(String.class);
    JsonNode inputReqNode = RestAPIConstants.objectMapper.readTree(inputReq);
    exchange.getIn().setBody(inputReqNode.toString());
  }
}
