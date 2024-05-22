package com.idep.xpath.mapper.component;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * Represents a p365xpathmapper endpoint.
 * @author sandeep.jadhav
 */
public class p365xpathmapperEndpoint extends DefaultEndpoint {

    public p365xpathmapperEndpoint() {
    }

    public p365xpathmapperEndpoint(String uri, p365xpathmapperComponent component) {
        super(uri, component);
    }

    @SuppressWarnings("deprecation")
	public p365xpathmapperEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new p365xpathmapperProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new p365xpathmapperConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }
}
