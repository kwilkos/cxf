package org.objectweb.celtix.bus.transports.jms;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;

public class JMSTransportFactory implements TransportFactory {

    protected Bus theBus;

    public void init(Bus bus) {
        theBus = bus;
    }

    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new JMSServerTransport(theBus, address);
    }
     
    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        return null;
    }
     
    public ClientTransport createClientTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new JMSClientTransport(theBus, address);
    }
}
