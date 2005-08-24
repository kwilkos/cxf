package org.objectweb.celtix.bus.bindings.soap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;

public class SOAPBindingFactory implements BindingFactory {
    private Bus bus;
    
    public SOAPBindingFactory() {
        //Complete
    }
    
    public void init(Bus b) {
        bus = b;
    }
    
    public ClientBinding createClientBinding(EndpointReferenceType reference) {        
        return null;
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference) {
        return null;
    }
}
