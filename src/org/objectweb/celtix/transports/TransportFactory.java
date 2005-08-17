package org.objectweb.celtix.transports;

public interface TransportFactory {
    ServerTransport createServerTransport();
    
    ClientTransport createClientTransport();
}
