package org.objectweb.celtix.transports.local;

import org.objectweb.celtix.message.Message;
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

    public Conduit getBackChannel(Message inMessage,
                                  Message partialResponse,
                                  EndpointReferenceType address) {
        // TODO Auto-generated method stub
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
}
