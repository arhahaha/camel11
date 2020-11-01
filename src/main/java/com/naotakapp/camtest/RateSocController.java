package com.naotakapp.camtest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.io.InputStream;

/**
 * Used for demonstrations purpose to simulate some external system event bus/broker, where messages are sent to, and
 * this component can consume from.
 */
public class RateSocController {

    // TODO: Delete me when you implementy your custom component
    private static String            hostname = "localhost";    //追加：接続先ホスト名
    private static int               port     = 9998;           //追加：接続先ポート
    private static Socket            soc      = null;           //追加；ソケット
    private static OutputStream      out      = null;           //追加；出力用ストリーム
    private static RecvThread        rcv      = null;           //追加：受信用スレッド 
    private static int               intsize  = 4;
    private static int               count    = 0;

    private static RateSocController INSTANCE;

    final private Set<Consumer> subscribers = ConcurrentHashMap.newKeySet();

    private RateSocController(){ }

    public static RateSocController getInstance(){
        if( INSTANCE == null ){
            INSTANCE = new RateSocController();
        }

        if( soc == null ){
            try {
                System.out.println( "RateSocController : TRY CONNECT... (" + hostname + ":" + port + ")" );
                soc = new Socket( hostname, port );
                out = soc.getOutputStream();
                System.out.println( "RateSocController : CONNECTED. (" + hostname + ":" + port + ")" );

                rcv = new RecvThread( soc, INSTANCE );
                rcv.start();
    
            } catch (final Exception e) {
                e.printStackTrace();    
            }
        }

        System.out.println( "RateSocController : CALLED [getInstance()]" );

        return INSTANCE;
    }

    public <T> void subscribe(final Consumer<T> subscriber) {
        System.out.println( "RateSocController : subscribe() [" + subscriber + "]" );
        subscribers.add(subscriber);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(final T event){
        // Notify all subscribers
        subscribers.forEach(consumer -> publishSingleEvent(event, consumer));
    }

    private <T> void publishSingleEvent(final T event, final Consumer<T> subscriber){
        //System.out.println( "RateSocController : publishSingleEvent() EVENT=[" + event + "], SUBSCR=[" + subscriber + "]" );
        subscriber.accept(event);
    }

    public void SendMessage( String msg ){

        try {
            final byte[] strbuf = msg.getBytes( "UTF-8" );
            final ByteBuffer buf = ByteBuffer.allocate( intsize );
            buf.putInt( strbuf.length );
            out.write( buf.array() );
            out.write( strbuf );

        } catch (final Exception e) {
            e.printStackTrace();
        }
        count++;
    }
}

class RecvThread extends Thread {

    private static int         intsize = 4;

    private RateSocController  rateCon = null;
    private Socket             rcvsoc  = null;

    public RecvThread( Socket soc, RateSocController con ){
        rcvsoc  = soc;
        rateCon = con;
    }

    public void run() {

        System.out.println("Client Receive Thread : Thread=[" + getName() + "]");
        InputStream  in = null;
 
        try {
            in = rcvsoc.getInputStream();
            
            while( !rcvsoc.isClosed() ) {

                int     ret    = 0;
                byte[]  lendat = new byte[intsize];
                
                ret = readData( rcvsoc, in, lendat );
                if( ret != lendat.length ){
                    System.out.println("Socket Error [Length Read].");
                    break;
                }
                ByteBuffer buf = ByteBuffer.wrap( lendat );
                buf.order( ByteOrder.BIG_ENDIAN );
                int soclen = buf.getInt(0);
                System.out.println("Socket Data Length : [" + soclen + "].");

                byte[]  bodydat = new byte[soclen];
                ret = readData( rcvsoc, in, bodydat );
                if( ret != bodydat.length ){
                    System.out.println("Socket Error [Body Read].");
                    break;
                }

                String trhsbt = new String( Arrays.copyOfRange( bodydat,  0,  4), "SJIS");
                String trhkbn = new String( Arrays.copyOfRange( bodydat,  4,  6), "SJIS");
                String shname = new String( Arrays.copyOfRange( bodydat,  6, 38), "SJIS");
                String sprice = new String( Arrays.copyOfRange( bodydat, 38, 49), "SJIS");
                String plathm = new String( Arrays.copyOfRange( bodydat, 49, 65), "SJIS");
                String shflag = new String( Arrays.copyOfRange( bodydat, 65, 66), "SJIS");

                String body = "Socket Data Body : 取引種別=[" + trhsbt +
                                                  "] 区分=[" + trhkbn +
                                                  "] 名称=[" + shname +
                                                  "] 価格=[" + sprice +
                                                  "] 媒体=[" + plathm +
                                                  "] フラグ=[" + shflag + "]";
                rateCon.publish( body );
            }

        } catch ( Exception e ) {
            e.printStackTrace();

        } finally {
            try {
                if( in != null ) {
                    in.close();
                }
                if( rcvsoc != null ) {
                    rcvsoc.close();
                }
            } catch ( final IOException e ) { 
            }
        }   
        System.out.println("Client RecvThread Terminate : [" + getName() + "]");
    }

    private static int readData( Socket soc, InputStream in, byte[] data ) throws IOException
    {
        int     datsiz = data.length;
        int     total  = 0;

        while( !soc.isClosed() ) {

            int ret = in.read( data, total, datsiz - total );
            if( ret <= 0 ){
                return -1;
            }
            total += ret;

            if( total >= datsiz ){
                break;
            }
        }
        return total;
    }
}
