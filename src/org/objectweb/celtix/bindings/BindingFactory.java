package org.objectweb.celtix.bindings;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;

public interface BindingFactory {
    void init(Bus bus);
    
    ClientBinding createClientBinding(EndpointReferenceType reference);

    ServerBinding createServerBinding(EndpointReferenceType reference, Endpoint endpoint);
}
