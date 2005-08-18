package org.objectweb.celtix.transports;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;


public interface TransportFactory {
    
    void init(Bus bus);
    
    ServerTransport createServerTransport(EndpointReferenceType address);

    ServerTransport createTransientServerTransport(EndpointReferenceType address);
    
    ClientTransport createClientTransport(EndpointReferenceType address);
}
