package com.idep.bikequote.impl.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BikeMQMsgProcessor implements Processor {
  public void process(Exchange exchange) throws Exception {
    String message = exchange.getIn().getBody().toString();
    exchange.getIn().setHeader("JMSCorrelationID", exchange.getProperty("messageId"));
    exchange.getIn().setBody(message);
  }
}
