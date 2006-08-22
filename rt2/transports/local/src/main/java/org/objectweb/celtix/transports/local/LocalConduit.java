package org.objectweb.celtix.transports.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

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
