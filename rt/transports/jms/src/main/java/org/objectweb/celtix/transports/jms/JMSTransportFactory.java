package org.objectweb.celtix.transports.jms;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

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
     
    public ClientTransport createClientTransport(EndpointReferenceType address,
                                                 ClientBinding binding) 
        throws WSDLException, IOException {
        return new JMSClientTransport(theBus, address, binding);
    }
}
