package org.apache.cxf.endpoint;

import java.io.IOException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.messaging.ChainInitiationObserver;
import org.apache.cxf.messaging.Destination;
import org.apache.cxf.messaging.DestinationFactory;
import org.apache.cxf.messaging.DestinationFactoryManager;
import org.apache.cxf.messaging.MessageObserver;
import org.apache.cxf.service.model.EndpointInfo;

public class ServerImpl implements Server {
    
    private Destination destination;
    private MessageObserver messageObserver;
    private Endpoint endpoint;

    public ServerImpl(Bus bus, Endpoint endpoint, ChainInitiationObserver observer) 
        throws BusException, IOException {
        this.endpoint = endpoint;
        this.messageObserver = observer;

        EndpointInfo ei = endpoint.getEndpointInfo();
        DestinationFactory destinationFactory = bus.getExtension(DestinationFactoryManager.class)
            .getDestinationFactory(ei.getTransportId());
        destination = destinationFactory.getDestination(ei);
    }
    
    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void start() {
        getDestination().setMessageObserver(messageObserver);
    }

    public void stop() {
        getDestination().setMessageObserver(null);
        
    }

    public MessageObserver getMessageObserver() {
        return messageObserver;
    }

    public void setMessageObserver(MessageObserver messageObserver) {
        this.messageObserver = messageObserver;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    
}
