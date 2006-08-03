package org.objectweb.celtix.transports.http;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class HTTPTransportFactory implements ConduitInitiator, DestinationFactory {

    protected Bus bus;
    
    public void init(Bus b) {
        bus = b;
    }

    public Conduit getConduit(EndpointReferenceType target)
        throws WSDLException, IOException {
        return new HTTPConduit(bus, target);
    }

    public Destination getDestination(EndpointReferenceType reference) {
        // TODO Auto-generated method stub
        return null;
    }
}
