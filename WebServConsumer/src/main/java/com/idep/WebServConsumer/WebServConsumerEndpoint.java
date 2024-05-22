package com.idep.WebServConsumer;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * Represents a WebServConsumer endpoint.
 */
public class WebServConsumerEndpoint extends DefaultEndpoint {

    public WebServConsumerEndpoint() {
    }

    public WebServConsumerEndpoint(String uri, WebServConsumerComponent component) {
        super(uri, component);
    }

    @SuppressWarnings("deprecation")
	public WebServConsumerEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new WebServConsumerProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new WebServConsumerConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }
}
