package org.objectweb.celtix.transports;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;


public interface TransportFactory {
    
    void init(Bus bus);
    
    ServerTransport createServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException;

    ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException;
    
    ClientTransport createClientTransport(EndpointReferenceType address)
        throws WSDLException, IOException;
}
