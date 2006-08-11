package org.objectweb.celtix.endpoint;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.messaging.ChainInitiationObserver;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactory;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.model.EndpointInfo;

public class ServerImpl implements Server {
    
    private Destination destination;
    private MessageObserver messageObserver;
    private Endpoint endpoint;

    public ServerImpl(Bus bus, Endpoint endpoint, ChainInitiationObserver observer) 
        throws BusException,
        WSDLException, IOException {
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
