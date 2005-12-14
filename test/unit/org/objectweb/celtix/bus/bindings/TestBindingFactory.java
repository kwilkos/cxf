package org.objectweb.celtix.bus.bindings;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TestBindingFactory implements BindingFactory {
    private Bus bus;

    public TestBindingFactory(Bus b) {
        init(b);
    }

    public ClientBinding createClientBinding(EndpointReferenceType reference) {
        return null;
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference,
                                             Endpoint endpoint,
                                             ServerBindingEndpointCallback cbFactory) {
        return new TestServerBinding(bus, reference, endpoint, cbFactory);
    }

    public final void init(Bus b) {
        bus = b;
    }
}
