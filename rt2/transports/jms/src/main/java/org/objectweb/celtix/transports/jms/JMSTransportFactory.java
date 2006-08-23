package org.objectweb.celtix.transports.jms;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.objectweb.celtix.Bus;

import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactory;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.service.model.EndpointInfo;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class JMSTransportFactory implements ConduitInitiator, DestinationFactory  {

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
    
    /*
    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new JMSServerTransport(theBus, address);
    }
     
    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        return null;
    }
     
    public ClientTransport createClientTransport(EndpointReferenceType address,
                                                 ClientBinding binding) 
        throws WSDLException, IOException {
        return new JMSClientTransport(theBus, address, binding);
    }*/

    public Conduit getConduit(EndpointInfo targetInfo) throws IOException {
        return new JMSConduit(bus, targetInfo);
    }

    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        return new JMSConduit(bus, endpointInfo, target);
    }

    public Destination getDestination(EndpointInfo ei) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    @Resource
    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
