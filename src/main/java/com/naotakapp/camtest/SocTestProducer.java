package com.naotakapp.camtest;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SocTest producer.
 */
public class SocTestProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SocTestProducer.class);
    private RateSocController rateSoc  = null;
    private SocTestEndpoint   endpoint = null;

    public SocTestProducer( SocTestEndpoint endpoint, RateSocController ratesoc ) {
        super( endpoint );
        this.endpoint = endpoint;
        this.rateSoc  = ratesoc;
    }

    public void process( Exchange exchange ) throws Exception {
        //System.out.println("SocTestProducer : process [Fire]");
        //System.out.println(exchange.getIn().getBody());

        String body = exchange.getIn().getBody(String.class);
        rateSoc.SendMessage( body );
    }

}
