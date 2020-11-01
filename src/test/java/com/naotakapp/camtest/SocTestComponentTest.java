package com.naotakapp.camtest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

//import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SocTestComponentTest extends CamelTestSupport {

    //private final RateSocController rateSoc = RateSocController.getInstance();

    @Test
    public void testSocTest() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(5);

        System.out.println( "TEST : CALL [simulateEventTrigger()]" );

        // Trigger events to subscribers
        simulateEventTrigger();

        mock.await();

        System.out.println( "TEST : FINISHED [mock.await()]" );
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("soctest://foo")
                  .process( new SocTestProcess() )
                  .to("soctest://bar")
                  .to("mock:result");
            }
        };
    }

    private void simulateEventTrigger() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //final Date now = new Date();
                // publish events to the event bus
                //final String now = "HOGE HOGE" + new Date();
                //rateSoc.publish(now);
                System.out.println( "DAMMY TASK..." );
            }
        };

        new Timer().scheduleAtFixedRate(task, 1000L, 3000L);
    }
}
