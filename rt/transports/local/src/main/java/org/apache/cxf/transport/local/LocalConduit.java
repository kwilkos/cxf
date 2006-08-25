package org.apache.cxf.transport.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class LocalConduit implements Conduit {

    public static final String IN_CONDUIT = LocalConduit.class.getName() + ".inConduit";
    public static final String IN_EXCHANGE = LocalConduit.class.getName() + ".inExchange";

    private LocalDestination destination;
    private MessageObserver observer;

    public LocalConduit(LocalDestination destination) {
        this.destination = destination;
    }

    public void close(Message msg) throws IOException {
        msg.getContent(OutputStream.class).close();        
    }
    public void close() {
    }

    public Destination getBackChannel() {
        return null;
    }

    public EndpointReferenceType getTarget() {
        return destination.getAddress();
    }

    public void send(Message message) throws IOException {
        final PipedInputStream stream = new PipedInputStream();
        final LocalConduit conduit = this;
        final Exchange exchange = message.getExchange();
        
        final Runnable receiver = new Runnable() {
            public void run() {
                MessageImpl m = new MessageImpl();
                m.setContent(InputStream.class, stream);
                m.setDestination(destination);
                m.put(IN_CONDUIT, conduit);
                m.put(IN_EXCHANGE, exchange);
                destination.getMessageObserver().onMessage(m);
            }
        };

        final PipedOutputStream outStream = new PipedOutputStream(stream);

        message.setContent(OutputStream.class, outStream);

        // TODO: put on executor
        new Thread(receiver).start();
    }

    public void setMessageObserver(MessageObserver o) {
        this.observer = o;
    }

    public MessageObserver getMessageObserver() {
        return observer;
    }
}
