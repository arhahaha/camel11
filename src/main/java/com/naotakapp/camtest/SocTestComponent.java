package com.naotakapp.camtest;

import java.util.Map;

//import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.support.DefaultComponent;

/**
 * Represents the component that manages {@link SocTestEndpoint}.
 */
public class SocTestComponent extends DefaultComponent {
    
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new SocTestEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
