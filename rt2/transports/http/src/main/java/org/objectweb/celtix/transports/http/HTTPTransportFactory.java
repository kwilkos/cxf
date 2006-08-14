package org.objectweb.celtix.transports.http;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactory;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public class HTTPTransportFactory implements ConduitInitiator, DestinationFactory {
    
    Bus bus;
    
    @Resource
    Collection<String> activationNamespaces;
    
    @PostConstruct
    void registerWithBindingManager() {
        ConduitInitiatorManager cim = bus.getExtension(ConduitInitiatorManager.class);
        for (String ns : activationNamespaces) {
            cim.registerConduitInitiator(ns, this);
        }
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        for (String ns : activationNamespaces) {
            dfm.registerDestinationFactory(ns, this);
        }
    }

    public Conduit getConduit(EndpointReferenceType target)
        throws WSDLException, IOException {
        return new HTTPConduit(bus, target);
    }

    public Destination getDestination(EndpointReferenceType reference)
        throws WSDLException, IOException {
        return new JettyHTTPDestination(bus, reference);
    }

    public Destination getDestination(EndpointInfo ei) throws WSDLException, IOException {
        throw new UnsupportedOperationException();
    }

    public Bus getBus() {
        return bus;
    }

    @Resource
    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
