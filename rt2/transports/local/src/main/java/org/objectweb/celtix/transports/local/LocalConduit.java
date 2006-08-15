package org.objectweb.celtix.transports.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class LocalConduit implements Conduit {

    private LocalDestination destination;

    public LocalConduit(LocalDestination destination) {
        this.destination = destination;
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

        final Runnable receiver = new Runnable() {
            public void run() {
                Message m = new MessageImpl();
                m.setContent(InputStream.class, stream);

                destination.getMessageObserver().onMessage(m);
            }
        };
        final Thread readThread = new Thread(receiver);

        final PipedOutputStream outStream = new PipedOutputStream(stream) {

            @Override
            public void close() throws IOException {
                super.close();

                try {
                    readThread.join();
                } catch (InterruptedException e) {
                    // IGNORE
                }
            }

        };

        message.setContent(OutputStream.class, outStream);

        // TODO: put on executor
        readThread.start();
    }

    public void setMessageObserver(MessageObserver observer) {
    }
}
