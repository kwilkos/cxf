package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;

public class HTTPTransportFactory implements TransportFactory {
    Bus bus;
    
    public void init(Bus b) {
        bus = b;
    }

    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        // TODO Auto-generated method stub
        return new HTTPServerTransport(bus, address);
    }

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        
        // TODO Auto-generated method stub
        return null;
    }

    public ClientTransport createClientTransport(EndpointReferenceType address)
        throws WSDLException, IOException {
        // TODO Auto-generated method stub
        return new HTTPClientTransport(bus, address);
    }

}
