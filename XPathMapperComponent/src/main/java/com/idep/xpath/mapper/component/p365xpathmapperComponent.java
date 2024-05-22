package com.idep.xpath.mapper.component;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link p365xpathmapperEndpoint}.
 */
public class p365xpathmapperComponent extends DefaultComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new p365xpathmapperEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
