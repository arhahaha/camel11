package com.naotakapp.camtest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class SocTestProcess implements Processor {
    
    private static int count = 0;

    @Override
    public void process( Exchange exchange ) throws Exception {

        String body = exchange.getIn().getBody(String.class);

        System.out.println( "Exchange Body : " + body );

        String line = null;

        switch( count ){
            case 0:
                line = "一発目のメッセージを送信します";
                break;
            case 1:
                line = "二通目です";
                break;
            case 2:
                line = "3rd Messageですね";
                break;
            case 3:
                line = "これで最後のメッセージです(次はENDを送信)";
                break;
            default:
                line = "end";
                break;
        }
        count++;

        exchange.getIn().setBody( line );
    }


}