package org.objectweb.celtix.bus.bindings;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;

public class TestBindingFactory implements BindingFactory {
    private Bus bus;

    public TestBindingFactory(Bus b) {
        init(b);
    }

    public ClientBinding createClientBinding(EndpointReferenceType reference) {
        return null;
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference, Endpoint endpoint) {
        return new TestServerBinding(bus, reference, endpoint);
    }

    public void init(Bus b) {
        bus = b;
    }
}
