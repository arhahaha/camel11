package com.naotakapp.camtest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
//import org.apache.camel.RuntimeCamelException;
import org.apache.camel.support.DefaultConsumer;

import java.util.concurrent.ExecutorService;

/**
 * The SocTest consumer.
 */
public class SocTestConsumer extends DefaultConsumer {
    private final SocTestEndpoint   endpoint;
    private final RateSocController rateSoc;

    private ExecutorService executorService;

    public SocTestConsumer(SocTestEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        rateSoc = RateSocController.getInstance();
    }

    @Override
    protected void doStart() throws Exception {

        System.out.println( "SocTestConsumer : doStart [Fire]" );

        super.doStart();

        // start a single threaded pool to monitor events
        executorService = endpoint.createExecutor();

        // submit task to the thread pool
        executorService.submit(() -> {
            // subscribe to an event
            rateSoc.subscribe(this::onEventListener);
        });
    }

    @Override
    protected void doStop() throws Exception {

        System.out.println( "SocTestConsumer : doStop [Fire]" );

        super.doStop();

        // shutdown the thread pool gracefully
        getEndpoint().getCamelContext().getExecutorServiceManager().shutdownGraceful(executorService);
    }

    private void onEventListener(final Object event) {
        final Exchange exchange = endpoint.createExchange();

        //System.out.println( "SocTestConsumer : onEventListener [Fire]" );

        exchange.getIn().setBody( event );

        try {
            // send message to next processor in the route
            //System.out.println( "SocTestConsumer : getProcessor().process [Next]" );
            getProcessor().process( exchange );
        } catch (Exception e) {
            exchange.setException(e);
        } finally {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
    }
}
