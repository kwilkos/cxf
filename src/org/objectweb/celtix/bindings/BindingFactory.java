package org.objectweb.celtix.bindings;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;

public interface BindingFactory {
    void init(Bus bus);
    
    ClientBinding createClientBinding(EndpointReferenceType reference);

    ServerBinding createServerBinding(EndpointReferenceType reference);
}
