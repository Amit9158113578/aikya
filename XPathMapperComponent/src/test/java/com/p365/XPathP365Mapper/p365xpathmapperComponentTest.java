package com.p365.XPathP365Mapper;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class p365xpathmapperComponentTest extends CamelTestSupport {

    @Test
    public void testp365xpathmapper() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("xpathmapper://foo")
                  .to("xpathmapper://bar")
                  .to("mock:result");
            }
        };
    }
}
