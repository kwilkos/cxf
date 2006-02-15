package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.bindings.soap.SOAPClientBinding;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TestClientBinding extends SOAPClientBinding {
    public TestClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
    }

    protected ClientTransport createTransport(EndpointReferenceType ref) 
        throws WSDLException, IOException {
        return new TestClientTransport(bus, ref);
    }
    
    public TestClientTransport getClientTransport() throws IOException {
        return (TestClientTransport)getTransport();
    }
}
