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

public class LocalDestination implements Destination {
    private LocalTransportFactory localDestinationFactory;
    private MessageObserver messageObserver;
    private EndpointReferenceType epr;

    public LocalDestination(LocalTransportFactory localDestinationFactory, EndpointReferenceType epr) {
        super();
        this.localDestinationFactory = localDestinationFactory;
        this.epr = epr;
    }

    public EndpointReferenceType getAddress() {
        return epr;
    }

    public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType address) {
        Conduit conduit = (Conduit)inMessage.get(LocalConduit.IN_CONDUIT);
        if (conduit instanceof LocalConduit) {
            return new SynchronousConduit((LocalConduit)conduit);
        }
        return null;
    }

    public void shutdown() {
        localDestinationFactory.remove(this);
    }

    public void setMessageObserver(MessageObserver observer) {
        this.messageObserver = observer;
    }

    public MessageObserver getMessageObserver() {
        return messageObserver;
    }

    static class SynchronousConduit implements Conduit {
        private LocalConduit conduit;

        public SynchronousConduit(LocalConduit conduit) {
            super();
            this.conduit = conduit;
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
            return null;
        }

        public void send(Message message) throws IOException {

            final PipedInputStream stream = new PipedInputStream();
            final Exchange exchange = (Exchange)message.get(LocalConduit.IN_EXCHANGE);

            final Runnable receiver = new Runnable() {
                public void run() {
                    MessageImpl m = new MessageImpl();
                    m.setExchange(exchange);
                    m.setContent(InputStream.class, stream);
                    conduit.getMessageObserver().onMessage(m);
                }
            };

            PipedOutputStream outStream = new PipedOutputStream(stream);
            message.setContent(OutputStream.class, outStream);

            new Thread(receiver).start();
        }

        public void setMessageObserver(MessageObserver observer) {
        }
    }
}
