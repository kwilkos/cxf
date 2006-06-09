package org.objectweb.celtix.bus.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;

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

    public ClientBinding createClientBinding(EndpointReferenceType reference) 
        throws IOException, WSDLException {
        return new TestClientBinding(bus, reference);
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference,
                                             ServerBindingEndpointCallback cbFactory) {
        return new TestServerBinding(bus, reference, cbFactory);
    }

    public final void init(Bus b) {
        bus = b;
    }
}
