package com.idep.WebServConsumer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class WebServConsumerComponentTest extends CamelTestSupport {

    @Test
    public void testWebServConsumer() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("webservconsumer://foo")
                  .to("webservconsumer://bar")
                  .to("mock:result");
            }
        };
    }
}
